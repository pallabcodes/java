package com.netflix.systemdesign.ratelimiting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Netflix Production-Grade Token Bucket Rate Limiter
 * 
 * This class demonstrates Netflix production standards for token bucket rate limiting including:
 * 1. Token bucket algorithm implementation
 * 2. Burst traffic handling
 * 3. Distributed rate limiting
 * 4. Performance optimization
 * 5. Monitoring and metrics
 * 6. Configuration management
 * 7. Error handling
 * 8. Thread safety
 * 
 * For C/C++ engineers:
 * - Token bucket is like a leaky bucket with tokens
 * - Tokens are added at a fixed rate
 * - Requests consume tokens
 * - When bucket is empty, requests are rejected
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@Component
public class NetflixTokenBucketRateLimiter {
    
    private final RateLimiterConfiguration configuration;
    private final MetricsCollector metricsCollector;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DistributedLockService distributedLockService;
    
    // Local token bucket state
    private volatile double tokens;
    private volatile long lastRefillTime;
    private final Object lock = new Object();
    
    /**
     * Constructor for token bucket rate limiter
     * 
     * @param configuration Rate limiter configuration
     * @param metricsCollector Metrics collection service
     * @param redisTemplate Redis template for distributed operations
     * @param distributedLockService Distributed lock service
     */
    public NetflixTokenBucketRateLimiter(RateLimiterConfiguration configuration,
                                       MetricsCollector metricsCollector,
                                       RedisTemplate<String, Object> redisTemplate,
                                       DistributedLockService distributedLockService) {
        this.configuration = configuration;
        this.metricsCollector = metricsCollector;
        this.redisTemplate = redisTemplate;
        this.distributedLockService = distributedLockService;
        
        this.tokens = configuration.getCapacity();
        this.lastRefillTime = System.currentTimeMillis();
        
        log.info("Initialized Netflix token bucket rate limiter with capacity: {}, refill rate: {}", 
                configuration.getCapacity(), configuration.getRefillRate());
    }
    
    /**
     * Try to acquire tokens
     * 
     * @param requestedTokens Number of tokens requested
     * @return true if tokens were acquired
     */
    public boolean tryAcquire(int requestedTokens) {
        return tryAcquire(requestedTokens, null);
    }
    
    /**
     * Try to acquire tokens with key
     * 
     * @param requestedTokens Number of tokens requested
     * @param key Rate limiting key
     * @return true if tokens were acquired
     */
    public boolean tryAcquire(int requestedTokens, String key) {
        if (requestedTokens <= 0) {
            return true;
        }
        
        if (key != null && !key.trim().isEmpty()) {
            return tryAcquireDistributed(requestedTokens, key);
        } else {
            return tryAcquireLocal(requestedTokens);
        }
    }
    
    /**
     * Try to acquire tokens locally
     * 
     * @param requestedTokens Number of tokens requested
     * @return true if tokens were acquired
     */
    private boolean tryAcquireLocal(int requestedTokens) {
        synchronized (lock) {
            refillTokens();
            
            if (tokens >= requestedTokens) {
                tokens -= requestedTokens;
                
                metricsCollector.recordRateLimitSuccess("local", requestedTokens);
                
                log.debug("Acquired {} tokens locally, remaining: {}", requestedTokens, tokens);
                return true;
            } else {
                metricsCollector.recordRateLimitFailure("local", requestedTokens);
                
                log.debug("Failed to acquire {} tokens locally, available: {}", requestedTokens, tokens);
                return false;
            }
        }
    }
    
    /**
     * Try to acquire tokens in distributed manner
     * 
     * @param requestedTokens Number of tokens requested
     * @param key Rate limiting key
     * @return true if tokens were acquired
     */
    private boolean tryAcquireDistributed(int requestedTokens, String key) {
        String lockKey = "rate_limit_lock:" + key;
        String bucketKey = "rate_limit_bucket:" + key;
        
        try {
            // Acquire distributed lock
            if (!distributedLockService.tryLock(lockKey, Duration.ofSeconds(1))) {
                log.warn("Failed to acquire distributed lock for key: {}", key);
                return false;
            }
            
            try {
                // Get current bucket state
                BucketState bucketState = getBucketState(bucketKey);
                
                // Refill tokens
                refillTokensDistributed(bucketState);
                
                // Check if we can acquire tokens
                if (bucketState.getTokens() >= requestedTokens) {
                    bucketState.setTokens(bucketState.getTokens() - requestedTokens);
                    bucketState.setLastRefillTime(System.currentTimeMillis());
                    
                    // Update bucket state
                    updateBucketState(bucketKey, bucketState);
                    
                    metricsCollector.recordRateLimitSuccess(key, requestedTokens);
                    
                    log.debug("Acquired {} tokens for key: {}, remaining: {}", 
                            requestedTokens, key, bucketState.getTokens());
                    return true;
                } else {
                    metricsCollector.recordRateLimitFailure(key, requestedTokens);
                    
                    log.debug("Failed to acquire {} tokens for key: {}, available: {}", 
                            requestedTokens, key, bucketState.getTokens());
                    return false;
                }
                
            } finally {
                distributedLockService.unlock(lockKey);
            }
            
        } catch (Exception e) {
            log.error("Error acquiring tokens for key: {}", key, e);
            metricsCollector.recordRateLimitError(key, e);
            return false;
        }
    }
    
    /**
     * Refill tokens locally
     */
    private void refillTokens() {
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastRefillTime;
        
        if (timePassed > 0) {
            double tokensToAdd = (timePassed / 1000.0) * configuration.getRefillRate();
            tokens = Math.min(configuration.getCapacity(), tokens + tokensToAdd);
            lastRefillTime = currentTime;
        }
    }
    
    /**
     * Refill tokens in distributed manner
     * 
     * @param bucketState Current bucket state
     */
    private void refillTokensDistributed(BucketState bucketState) {
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - bucketState.getLastRefillTime();
        
        if (timePassed > 0) {
            double tokensToAdd = (timePassed / 1000.0) * configuration.getRefillRate();
            bucketState.setTokens(Math.min(configuration.getCapacity(), 
                    bucketState.getTokens() + tokensToAdd));
            bucketState.setLastRefillTime(currentTime);
        }
    }
    
    /**
     * Get bucket state from Redis
     * 
     * @param bucketKey Bucket key
     * @return Bucket state
     */
    private BucketState getBucketState(String bucketKey) {
        try {
            Object state = redisTemplate.opsForValue().get(bucketKey);
            if (state instanceof BucketState) {
                return (BucketState) state;
            }
        } catch (Exception e) {
            log.warn("Error getting bucket state for key: {}", bucketKey, e);
        }
        
        // Return default state
        return BucketState.builder()
                .tokens(configuration.getCapacity())
                .lastRefillTime(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Update bucket state in Redis
     * 
     * @param bucketKey Bucket key
     * @param bucketState Bucket state
     */
    private void updateBucketState(String bucketKey, BucketState bucketState) {
        try {
            redisTemplate.opsForValue().set(bucketKey, bucketState, 
                    Duration.ofSeconds(configuration.getBucketTtl()));
        } catch (Exception e) {
            log.warn("Error updating bucket state for key: {}", bucketKey, e);
        }
    }
    
    /**
     * Get current token count
     * 
     * @return Current token count
     */
    public double getCurrentTokens() {
        synchronized (lock) {
            refillTokens();
            return tokens;
        }
    }
    
    /**
     * Get current token count for key
     * 
     * @param key Rate limiting key
     * @return Current token count
     */
    public double getCurrentTokens(String key) {
        if (key == null || key.trim().isEmpty()) {
            return getCurrentTokens();
        }
        
        try {
            String bucketKey = "rate_limit_bucket:" + key;
            BucketState bucketState = getBucketState(bucketKey);
            refillTokensDistributed(bucketState);
            return bucketState.getTokens();
        } catch (Exception e) {
            log.error("Error getting current tokens for key: {}", key, e);
            return 0;
        }
    }
    
    /**
     * Get rate limiter statistics
     * 
     * @return Rate limiter statistics
     */
    public RateLimiterStatistics getStatistics() {
        return RateLimiterStatistics.builder()
                .capacity(configuration.getCapacity())
                .refillRate(configuration.getRefillRate())
                .currentTokens(getCurrentTokens())
                .totalRequests(metricsCollector.getTotalRateLimitRequests())
                .allowedRequests(metricsCollector.getAllowedRateLimitRequests())
                .rejectedRequests(metricsCollector.getRejectedRateLimitRequests())
                .build();
    }
    
    /**
     * Bucket state for distributed rate limiting
     */
    public static class BucketState {
        private double tokens;
        private long lastRefillTime;
        
        public BucketState() {}
        
        public BucketState(double tokens, long lastRefillTime) {
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
        
        public double getTokens() {
            return tokens;
        }
        
        public void setTokens(double tokens) {
            this.tokens = tokens;
        }
        
        public long getLastRefillTime() {
            return lastRefillTime;
        }
        
        public void setLastRefillTime(long lastRefillTime) {
            this.lastRefillTime = lastRefillTime;
        }
        
        public static BucketStateBuilder builder() {
            return new BucketStateBuilder();
        }
        
        public static class BucketStateBuilder {
            private double tokens;
            private long lastRefillTime;
            
            public BucketStateBuilder tokens(double tokens) {
                this.tokens = tokens;
                return this;
            }
            
            public BucketStateBuilder lastRefillTime(long lastRefillTime) {
                this.lastRefillTime = lastRefillTime;
                return this;
            }
            
            public BucketState build() {
                return new BucketState(tokens, lastRefillTime);
            }
        }
    }
}
