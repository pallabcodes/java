package com.netflix.streaming.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration for API protection.
 * 
 * Implements token bucket algorithm for rate limiting.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Rate limit buckets per client/IP
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Default rate limit: 100 requests per minute
     */
    @Bean
    public Bucket defaultRateLimitBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Get or create rate limit bucket for a key (IP, user ID, etc.)
     */
    public Bucket getBucket(String key) {
        return buckets.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        });
    }

    /**
     * Get bucket with custom rate limit
     */
    public Bucket getBucket(String key, int requestsPerMinute) {
        return buckets.compute(key, (k, existing) -> {
            Bandwidth limit = Bandwidth.classic(requestsPerMinute, 
                    Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        });
    }

    /**
     * Rate limit tiers
     */
    public enum RateLimitTier {
        FREE(60, Duration.ofMinutes(1)),           // 60 req/min
        BASIC(200, Duration.ofMinutes(1)),         // 200 req/min
        PREMIUM(1000, Duration.ofMinutes(1)),      // 1000 req/min
        ENTERPRISE(5000, Duration.ofMinutes(1));  // 5000 req/min

        private final int requests;
        private final Duration window;

        RateLimitTier(int requests, Duration window) {
            this.requests = requests;
            this.window = window;
        }

        public int getRequests() {
            return requests;
        }

        public Duration getWindow() {
            return window;
        }
    }
}

