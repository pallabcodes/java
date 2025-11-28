package com.netflix.springai.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitingConfig {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        return RateLimiterRegistry.ofDefaults();
    }

    @Bean(name = "chatRateLimiter")
    public RateLimiter chatRateLimiter(RateLimiterRegistry registry) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(10) // 10 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(5)) // timeout for waiting
                .build();

        return registry.rateLimiter("chat", config);
    }

    @Bean(name = "embeddingRateLimiter")
    public RateLimiter embeddingRateLimiter(RateLimiterRegistry registry) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(50) // 50 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(5)) // timeout for waiting
                .build();

        return registry.rateLimiter("embedding", config);
    }
}
