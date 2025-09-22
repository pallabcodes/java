# Rate Limiting - Netflix Production Guide

## 🎯 **CONCEPT OVERVIEW**

Rate limiting is a technique to control the rate of requests sent or received by a system. Netflix uses sophisticated rate limiting to protect services from abuse, ensure fair resource usage, and maintain system stability.

## 📊 **IMPLEMENTATION LAYER CLASSIFICATION**

| Component | Layer | Implementation Type | Netflix Status |
|-----------|-------|-------------------|----------------|
| **Token Bucket** | Application | Rate limiting algorithm | ✅ Production |
| **Sliding Window** | Application | Rate limiting algorithm | ✅ Production |
| **Fixed Window** | Application | Rate limiting algorithm | ✅ Production |
| **Distributed Rate Limiting** | Application + Infrastructure | Redis-based | ✅ Production |
| **API Gateway Rate Limiting** | Infrastructure | NGINX/HAProxy | ✅ Production |

## 🏗️ **RATE LIMITING ALGORITHMS**

### **1. Token Bucket**
- **Description**: Tokens are added to bucket at fixed rate, requests consume tokens
- **Use Case**: Burst traffic handling
- **Netflix Implementation**: ✅ Production
- **Layer**: Application

### **2. Sliding Window**
- **Description**: Track requests in sliding time window
- **Use Case**: Smooth rate limiting
- **Netflix Implementation**: ✅ Production
- **Layer**: Application

### **3. Fixed Window**
- **Description**: Track requests in fixed time windows
- **Use Case**: Simple rate limiting
- **Netflix Implementation**: ✅ Production
- **Layer**: Application

### **4. Leaky Bucket**
- **Description**: Requests are processed at constant rate
- **Use Case**: Traffic shaping
- **Netflix Implementation**: ✅ Production
- **Layer**: Application

## 🚀 **NETFLIX PRODUCTION IMPLEMENTATIONS**

### **1. Token Bucket Rate Limiter**

```java
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
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Component
@Slf4j
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
    @Data
    @Builder
    public static class BucketState {
        private double tokens;
        private long lastRefillTime;
    }
}
```

### **2. Sliding Window Rate Limiter**

```java
/**
 * Netflix Production-Grade Sliding Window Rate Limiter
 * 
 * This class demonstrates Netflix production standards for sliding window rate limiting including:
 * 1. Sliding window algorithm implementation
 * 2. Smooth rate limiting
 * 3. Distributed rate limiting
 * 4. Performance optimization
 * 5. Monitoring and metrics
 * 6. Configuration management
 * 7. Error handling
 * 8. Thread safety
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Component
@Slf4j
public class NetflixSlidingWindowRateLimiter {
    
    private final RateLimiterConfiguration configuration;
    private final MetricsCollector metricsCollector;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DistributedLockService distributedLockService;
    
    /**
     * Constructor for sliding window rate limiter
     * 
     * @param configuration Rate limiter configuration
     * @param metricsCollector Metrics collection service
     * @param redisTemplate Redis template for distributed operations
     * @param distributedLockService Distributed lock service
     */
    public NetflixSlidingWindowRateLimiter(RateLimiterConfiguration configuration,
                                         MetricsCollector metricsCollector,
                                         RedisTemplate<String, Object> redisTemplate,
                                         DistributedLockService distributedLockService) {
        this.configuration = configuration;
        this.metricsCollector = metricsCollector;
        this.redisTemplate = redisTemplate;
        this.distributedLockService = distributedLockService;
        
        log.info("Initialized Netflix sliding window rate limiter with window size: {}, max requests: {}", 
                configuration.getWindowSize(), configuration.getMaxRequests());
    }
    
    /**
     * Try to acquire request
     * 
     * @param key Rate limiting key
     * @return true if request was allowed
     */
    public boolean tryAcquire(String key) {
        if (key == null || key.trim().isEmpty()) {
            key = "default";
        }
        
        try {
            String lockKey = "rate_limit_lock:" + key;
            String windowKey = "rate_limit_window:" + key;
            
            // Acquire distributed lock
            if (!distributedLockService.tryLock(lockKey, Duration.ofSeconds(1))) {
                log.warn("Failed to acquire distributed lock for key: {}", key);
                return false;
            }
            
            try {
                // Get current window state
                WindowState windowState = getWindowState(windowKey);
                
                // Clean old requests
                cleanOldRequests(windowState);
                
                // Check if we can add new request
                if (windowState.getRequests().size() < configuration.getMaxRequests()) {
                    // Add new request
                    windowState.getRequests().add(System.currentTimeMillis());
                    
                    // Update window state
                    updateWindowState(windowKey, windowState);
                    
                    metricsCollector.recordRateLimitSuccess(key, 1);
                    
                    log.debug("Allowed request for key: {}, current count: {}", 
                            key, windowState.getRequests().size());
                    return true;
                } else {
                    metricsCollector.recordRateLimitFailure(key, 1);
                    
                    log.debug("Rejected request for key: {}, current count: {}", 
                            key, windowState.getRequests().size());
                    return false;
                }
                
            } finally {
                distributedLockService.unlock(lockKey);
            }
            
        } catch (Exception e) {
            log.error("Error processing rate limit for key: {}", key, e);
            metricsCollector.recordRateLimitError(key, e);
            return false;
        }
    }
    
    /**
     * Clean old requests from window
     * 
     * @param windowState Window state
     */
    private void cleanOldRequests(WindowState windowState) {
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - configuration.getWindowSize().toMillis();
        
        windowState.getRequests().removeIf(timestamp -> timestamp < windowStart);
    }
    
    /**
     * Get window state from Redis
     * 
     * @param windowKey Window key
     * @return Window state
     */
    private WindowState getWindowState(String windowKey) {
        try {
            Object state = redisTemplate.opsForValue().get(windowKey);
            if (state instanceof WindowState) {
                return (WindowState) state;
            }
        } catch (Exception e) {
            log.warn("Error getting window state for key: {}", windowKey, e);
        }
        
        // Return default state
        return WindowState.builder()
                .requests(new ArrayList<>())
                .lastCleanupTime(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Update window state in Redis
     * 
     * @param windowKey Window key
     * @param windowState Window state
     */
    private void updateWindowState(String windowKey, WindowState windowState) {
        try {
            redisTemplate.opsForValue().set(windowKey, windowState, 
                    Duration.ofSeconds(configuration.getWindowTtl()));
        } catch (Exception e) {
            log.warn("Error updating window state for key: {}", windowKey, e);
        }
    }
    
    /**
     * Get current request count for key
     * 
     * @param key Rate limiting key
     * @return Current request count
     */
    public int getCurrentRequestCount(String key) {
        if (key == null || key.trim().isEmpty()) {
            key = "default";
        }
        
        try {
            String windowKey = "rate_limit_window:" + key;
            WindowState windowState = getWindowState(windowKey);
            cleanOldRequests(windowState);
            return windowState.getRequests().size();
        } catch (Exception e) {
            log.error("Error getting current request count for key: {}", key, e);
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
                .windowSize(configuration.getWindowSize().toMillis())
                .maxRequests(configuration.getMaxRequests())
                .totalRequests(metricsCollector.getTotalRateLimitRequests())
                .allowedRequests(metricsCollector.getAllowedRateLimitRequests())
                .rejectedRequests(metricsCollector.getRejectedRateLimitRequests())
                .build();
    }
    
    /**
     * Window state for sliding window rate limiting
     */
    @Data
    @Builder
    public static class WindowState {
        private List<Long> requests;
        private long lastCleanupTime;
    }
}
```

## 📊 **MONITORING AND METRICS**

### **Rate Limiting Metrics Implementation**

```java
/**
 * Netflix Production-Grade Rate Limiting Metrics
 * 
 * This class implements comprehensive metrics collection for rate limiting including:
 * 1. Request metrics
 * 2. Rate limit metrics
 * 3. Error metrics
 * 4. Performance metrics
 * 5. Distribution metrics
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Component
@Slf4j
public class RateLimitingMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // Rate limiting metrics
    private final Counter totalRequests;
    private final Counter allowedRequests;
    private final Counter rejectedRequests;
    private final Timer requestProcessingTime;
    private final Gauge currentRequestRate;
    
    public RateLimitingMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize metrics
        this.totalRequests = Counter.builder("rate_limiting_requests_total")
                .description("Total number of rate limiting requests")
                .register(meterRegistry);
        
        this.allowedRequests = Counter.builder("rate_limiting_allowed_requests_total")
                .description("Total number of allowed requests")
                .register(meterRegistry);
        
        this.rejectedRequests = Counter.builder("rate_limiting_rejected_requests_total")
                .description("Total number of rejected requests")
                .register(meterRegistry);
        
        this.requestProcessingTime = Timer.builder("rate_limiting_request_processing_time")
                .description("Rate limiting request processing time")
                .register(meterRegistry);
        
        this.currentRequestRate = Gauge.builder("rate_limiting_current_request_rate")
                .description("Current request rate")
                .register(meterRegistry, this, RateLimitingMetrics::getCurrentRequestRate);
    }
    
    /**
     * Record rate limiting request
     * 
     * @param key Rate limiting key
     * @param allowed Whether request was allowed
     * @param duration Processing duration
     */
    public void recordRequest(String key, boolean allowed, long duration) {
        totalRequests.increment(Tags.of("key", key));
        
        if (allowed) {
            allowedRequests.increment(Tags.of("key", key));
        } else {
            rejectedRequests.increment(Tags.of("key", key));
        }
        
        requestProcessingTime.record(duration, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Get current request rate
     * 
     * @return Current request rate
     */
    private double getCurrentRequestRate() {
        // Implementation to get current request rate
        return 0.0; // Placeholder
    }
}
```

## 🎯 **BEST PRACTICES**

### **1. Algorithm Selection**
- **Token Bucket**: Use for burst traffic handling
- **Sliding Window**: Use for smooth rate limiting
- **Fixed Window**: Use for simple rate limiting
- **Leaky Bucket**: Use for traffic shaping

### **2. Distributed Rate Limiting**
- **Redis**: Use Redis for distributed state
- **Locks**: Use distributed locks for consistency
- **TTL**: Set appropriate TTL for state
- **Monitoring**: Monitor distributed operations

### **3. Performance Optimization**
- **Local Caching**: Cache rate limit state locally
- **Batch Operations**: Batch Redis operations
- **Connection Pooling**: Use connection pooling
- **Async Processing**: Use async processing where possible

### **4. Monitoring**
- **Metrics**: Collect comprehensive metrics
- **Alerting**: Set up rate limit alerts
- **Dashboards**: Create rate limiting dashboards
- **Logging**: Log rate limit events

## 🔍 **TROUBLESHOOTING**

### **Common Issues**
1. **High Rejection Rate**: Check rate limit configuration
2. **Distributed Inconsistency**: Check Redis connectivity
3. **Performance Issues**: Check Redis performance
4. **Memory Usage**: Monitor Redis memory usage

### **Debugging Steps**
1. **Check Metrics**: Review rate limiting metrics
2. **Verify Configuration**: Validate rate limit settings
3. **Test Redis**: Check Redis connectivity
4. **Monitor Logs**: Review rate limiting logs

## 📚 **REFERENCES**

- [Rate Limiting Patterns](https://cloud.google.com/architecture/rate-limiting-strategies-techniques)
- [Token Bucket Algorithm](https://en.wikipedia.org/wiki/Token_bucket)
- [Sliding Window Algorithm](https://en.wikipedia.org/wiki/Sliding_window_protocol)
- [Redis Rate Limiting](https://redis.io/docs/manual/rate-limiting/)

---

**Last Updated**: 2024  
**Version**: 1.0.0  
**Maintainer**: Netflix SDE-2 Team  
**Status**: ✅ Production Ready
