package com.netflix.streaming.infrastructure.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience configuration for production-grade fault tolerance.
 * 
 * Implements:
 * - Circuit breakers for dependency protection
 * - Retry mechanisms with exponential backoff
 * - Time limiters for request timeouts
 */
@Configuration
public class ResilienceConfig {

    /**
     * Circuit Breaker Registry
     * Protects against cascading failures
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                // Failure rate threshold: 50% failures opens circuit
                .failureRateThreshold(50)
                // Wait 10 seconds before attempting half-open
                .waitDurationInOpenState(Duration.ofSeconds(10))
                // Sliding window: count-based, last 100 calls
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(100)
                // Minimum calls before calculating failure rate
                .minimumNumberOfCalls(10)
                // Permitted calls in half-open state
                .permittedNumberOfCallsInHalfOpenState(3)
                // Slow call rate threshold: 50% slow calls opens circuit
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                // Record exceptions (don't record client errors)
                .recordException(throwable -> !(throwable instanceof IllegalArgumentException))
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    /**
     * Default Circuit Breaker for general use
     */
    @Bean
    public CircuitBreaker defaultCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("default");
    }

    /**
     * Circuit Breaker for Kafka operations
     */
    @Bean
    public CircuitBreaker kafkaCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig kafkaConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(60) // Higher threshold for Kafka
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(50)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        return registry.circuitBreaker("kafka", kafkaConfig);
    }

    /**
     * Circuit Breaker for database operations
     */
    @Bean
    public CircuitBreaker databaseCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig dbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(40) // Lower threshold for DB
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .slidingWindowSize(100)
                .minimumNumberOfCalls(10)
                .permittedNumberOfCallsInHalfOpenState(3)
                .slowCallRateThreshold(30)
                .slowCallDurationThreshold(Duration.ofSeconds(1))
                .build();

        return registry.circuitBreaker("database", dbConfig);
    }

    /**
     * Retry Registry
     * Implements exponential backoff retry strategy
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                // Maximum 3 retry attempts
                .maxAttempts(3)
                // Wait 100ms before first retry
                .waitDuration(Duration.ofMillis(100))
                // Exponential backoff multiplier
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                        Duration.ofMillis(100), 2.0))
                // Maximum wait time between retries
                .maxInterval(Duration.ofSeconds(5))
                // Retry on these exceptions
                .retryOnException(throwable -> 
                    throwable instanceof java.net.SocketTimeoutException ||
                    throwable instanceof java.sql.SQLException ||
                    throwable instanceof org.springframework.dao.DataAccessException)
                // Don't retry on these exceptions
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return RetryRegistry.of(config);
    }

    /**
     * Default Retry for general use
     */
    @Bean
    public Retry defaultRetry(RetryRegistry registry) {
        return registry.retry("default");
    }

    /**
     * Retry for Kafka operations
     */
    @Bean
    public Retry kafkaRetry(RetryRegistry registry) {
        RetryConfig kafkaRetryConfig = RetryConfig.custom()
                .maxAttempts(5) // More retries for Kafka
                .waitDuration(Duration.ofMillis(200))
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                        Duration.ofMillis(200), 2.0))
                .maxInterval(Duration.ofSeconds(10))
                .build();

        return registry.retry("kafka", kafkaRetryConfig);
    }

    /**
     * Time Limiter Registry
     * Enforces request timeouts
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiterRegistry.of(config);
    }

    /**
     * Default Time Limiter
     */
    @Bean
    public TimeLimiter defaultTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("default");
    }

    /**
     * Time Limiter for Kafka operations (longer timeout)
     */
    @Bean
    public TimeLimiter kafkaTimeLimiter(TimeLimiterRegistry registry) {
        TimeLimiterConfig kafkaConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();

        return registry.timeLimiter("kafka", kafkaConfig);
    }
}

