package com.example.kotlinpay.shared.security

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Rate limiting configuration for API protection.
 * 
 * Implements token bucket algorithm for rate limiting.
 */
@Configuration
class RateLimitingConfig {

    /**
     * Rate limit buckets per client/IP
     */
    private val buckets = ConcurrentHashMap<String, Bucket>()

    /**
     * Default rate limit: 100 requests per minute
     */
    @Bean
    fun defaultRateLimitBucket(): Bucket {
        val limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)))
        return Bucket.builder()
            .addLimit(limit)
            .build()
    }

    /**
     * Get or create rate limit bucket for a key (IP, user ID, etc.)
     */
    fun getBucket(key: String): Bucket {
        return buckets.computeIfAbsent(key) {
            val limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)))
            Bucket.builder()
                .addLimit(limit)
                .build()
        }
    }

    /**
     * Get bucket with custom rate limit
     */
    fun getBucket(key: String, requestsPerMinute: Int): Bucket {
        return buckets.compute(key) { _, _ ->
            val limit = Bandwidth.classic(
                requestsPerMinute.toLong(),
                Refill.greedy(requestsPerMinute.toLong(), Duration.ofMinutes(1))
            )
            Bucket.builder()
                .addLimit(limit)
                .build()
        }!!
    }

    /**
     * Rate limit tiers
     */
    enum class RateLimitTier(
        val requests: Int,
        val window: Duration
    ) {
        FREE(60, Duration.ofMinutes(1)),           // 60 req/min
        BASIC(200, Duration.ofMinutes(1)),         // 200 req/min
        PREMIUM(1000, Duration.ofMinutes(1)),      // 1000 req/min
        ENTERPRISE(5000, Duration.ofMinutes(1))    // 5000 req/min
    }
}

