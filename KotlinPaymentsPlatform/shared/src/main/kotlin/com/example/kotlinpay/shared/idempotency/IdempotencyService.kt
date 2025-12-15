package com.example.kotlinpay.shared.idempotency

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

/**
 * Idempotency service for ensuring exactly-once semantics for API requests.
 * 
 * Uses Redis to store idempotency keys with TTL-based expiration.
 * Prevents duplicate processing of requests with the same idempotency key.
 */
@Service
class IdempotencyService(
    private val redisTemplate: StringRedisTemplate
) {
    private val logger = LoggerFactory.getLogger(IdempotencyService::class.java)
    
    companion object {
        private const val IDEMPOTENCY_KEY_PREFIX = "idempotency:"
        private val DEFAULT_TTL = Duration.ofHours(24)
    }

    /**
     * Check if an idempotency key has been processed.
     */
    fun isProcessed(key: String?): Boolean {
        if (key.isNullOrBlank()) {
            return false
        }
        
        val redisKey = buildRedisKey(key)
        return redisTemplate.hasKey(redisKey) == true
    }

    /**
     * Claim an idempotency key (mark as processing).
     */
    fun claim(key: String?, ttl: Duration = DEFAULT_TTL): Boolean {
        if (key.isNullOrBlank()) {
            return false
        }

        val redisKey = buildRedisKey(key)
        val value = UUID.randomUUID().toString()
        
        val claimed = redisTemplate.opsForValue().setIfAbsent(redisKey, value, ttl) == true
        
        if (claimed) {
            logger.debug("Claimed idempotency key: {}", key)
        } else {
            logger.debug("Idempotency key already exists: {}", key)
        }
        
        return claimed
    }

    /**
     * Store the response for an idempotency key.
     */
    fun storeResponse(key: String?, response: String, ttl: Duration = DEFAULT_TTL) {
        if (key.isNullOrBlank()) {
            return
        }

        val responseKey = buildResponseKey(key)
        redisTemplate.opsForValue().set(responseKey, response, ttl)
        logger.debug("Stored response for idempotency key: {}", key)
    }

    /**
     * Retrieve the stored response for an idempotency key.
     */
    fun getStoredResponse(key: String?): String? {
        if (key.isNullOrBlank()) {
            return null
        }

        val responseKey = buildResponseKey(key)
        return redisTemplate.opsForValue().get(responseKey)
    }

    /**
     * Release an idempotency key (remove from Redis).
     */
    fun release(key: String?) {
        if (key.isNullOrBlank()) {
            return
        }

        val redisKey = buildRedisKey(key)
        redisTemplate.delete(redisKey)
        logger.debug("Released idempotency key: {}", key)
    }

    /**
     * Extend the TTL of an idempotency key.
     */
    fun extendTtl(key: String?, additionalTtl: Duration) {
        if (key.isNullOrBlank()) {
            return
        }

        val redisKey = buildRedisKey(key)
        redisTemplate.expire(redisKey, additionalTtl)
        logger.debug("Extended TTL for idempotency key: {}", key)
    }

    private fun buildRedisKey(key: String): String {
        return "$IDEMPOTENCY_KEY_PREFIX$key"
    }

    private fun buildResponseKey(key: String): String {
        return "$IDEMPOTENCY_KEY_PREFIXresponse:$key"
    }
}

