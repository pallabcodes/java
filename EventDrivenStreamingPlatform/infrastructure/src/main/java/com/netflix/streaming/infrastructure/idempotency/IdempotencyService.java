package com.netflix.streaming.infrastructure.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Idempotency service for ensuring exactly-once semantics for API requests.
 * 
 * Uses Redis to store idempotency keys with TTL-based expiration.
 * Prevents duplicate processing of requests with the same idempotency key.
 */
@Service
public class IdempotencyService {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyService.class);
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if an idempotency key has been processed.
     * 
     * @param key The idempotency key
     * @return true if key exists (already processed), false otherwise
     */
    public boolean isProcessed(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        
        String redisKey = buildRedisKey(key);
        Boolean exists = redisTemplate.hasKey(redisKey);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Claim an idempotency key (mark as processing).
     * 
     * @param key The idempotency key
     * @param ttl Time-to-live for the key
     * @return true if successfully claimed, false if already exists
     */
    public boolean claim(String key, Duration ttl) {
        if (key == null || key.isBlank()) {
            return false;
        }

        String redisKey = buildRedisKey(key);
        String value = UUID.randomUUID().toString(); // Unique value for verification
        
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(redisKey, value, ttl);
        boolean claimed = Boolean.TRUE.equals(setIfAbsent);
        
        if (claimed) {
            logger.debug("Claimed idempotency key: {}", key);
        } else {
            logger.debug("Idempotency key already exists: {}", key);
        }
        
        return claimed;
    }

    /**
     * Claim an idempotency key with default TTL (24 hours).
     */
    public boolean claim(String key) {
        return claim(key, DEFAULT_TTL);
    }

    /**
     * Store the response for an idempotency key.
     * This allows returning the same response for duplicate requests.
     * 
     * @param key The idempotency key
     * @param response The response to store
     * @param ttl Time-to-live for the stored response
     */
    public void storeResponse(String key, String response, Duration ttl) {
        if (key == null || key.isBlank()) {
            return;
        }

        String responseKey = buildResponseKey(key);
        redisTemplate.opsForValue().set(responseKey, response, ttl);
        logger.debug("Stored response for idempotency key: {}", key);
    }

    /**
     * Retrieve the stored response for an idempotency key.
     * 
     * @param key The idempotency key
     * @return The stored response, or null if not found
     */
    public String getStoredResponse(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        String responseKey = buildResponseKey(key);
        return redisTemplate.opsForValue().get(responseKey);
    }

    /**
     * Release an idempotency key (remove from Redis).
     * Used when request processing fails and we want to allow retry.
     * 
     * @param key The idempotency key
     */
    public void release(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        String redisKey = buildRedisKey(key);
        redisTemplate.delete(redisKey);
        logger.debug("Released idempotency key: {}", key);
    }

    /**
     * Extend the TTL of an idempotency key.
     * 
     * @param key The idempotency key
     * @param additionalTtl Additional time to add
     */
    public void extendTtl(String key, Duration additionalTtl) {
        if (key == null || key.isBlank()) {
            return;
        }

        String redisKey = buildRedisKey(key);
        redisTemplate.expire(redisKey, additionalTtl);
        logger.debug("Extended TTL for idempotency key: {}", key);
    }

    /**
     * Build Redis key for idempotency storage.
     */
    private String buildRedisKey(String key) {
        return IDEMPOTENCY_KEY_PREFIX + key;
    }

    /**
     * Build Redis key for response storage.
     */
    private String buildResponseKey(String key) {
        return IDEMPOTENCY_KEY_PREFIX + "response:" + key;
    }
}

