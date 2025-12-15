package com.example.kotlinpay.shared.resilience

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import io.github.resilience4j.timelimiter.TimeLimiter
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Resilience configuration for production-grade fault tolerance.
 * 
 * Implements:
 * - Circuit breakers for dependency protection
 * - Retry mechanisms with exponential backoff
 * - Time limiters for request timeouts
 */
@Configuration
class ResilienceConfig {

    /**
     * Circuit Breaker Registry
     */
    @Bean
    fun circuitBreakerRegistry(): CircuitBreakerRegistry {
        val config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50f)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(100)
            .minimumNumberOfCalls(10)
            .permittedNumberOfCallsInHalfOpenState(3)
            .slowCallRateThreshold(50f)
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .recordException { throwable -> 
                throwable !is IllegalArgumentException 
            }
            .build()

        return CircuitBreakerRegistry.of(config)
    }

    /**
     * Default Circuit Breaker
     */
    @Bean
    fun defaultCircuitBreaker(registry: CircuitBreakerRegistry): CircuitBreaker {
        return registry.circuitBreaker("default")
    }

    /**
     * Circuit Breaker for Payment Gateway operations
     */
    @Bean
    fun paymentGatewayCircuitBreaker(registry: CircuitBreakerRegistry): CircuitBreaker {
        val config = CircuitBreakerConfig.custom()
            .failureRateThreshold(40f) // Lower threshold for payment gateways
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(50)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(2)
            .slowCallRateThreshold(30f)
            .slowCallDurationThreshold(Duration.ofSeconds(3))
            .build()

        return registry.circuitBreaker("payment-gateway", config)
    }

    /**
     * Retry Registry
     */
    @Bean
    fun retryRegistry(): RetryRegistry {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(100))
            .intervalFunction(
                io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                    Duration.ofMillis(100), 2.0
                )
            )
            .maxInterval(Duration.ofSeconds(5))
            .retryOnException { throwable ->
                throwable is java.net.SocketTimeoutException ||
                throwable is java.sql.SQLException ||
                throwable is org.springframework.dao.DataAccessException
            }
            .ignoreExceptions(IllegalArgumentException::class.java)
            .build()

        return RetryRegistry.of(config)
    }

    /**
     * Default Retry
     */
    @Bean
    fun defaultRetry(registry: RetryRegistry): Retry {
        return registry.retry("default")
    }

    /**
     * Retry for Payment Gateway operations
     */
    @Bean
    fun paymentGatewayRetry(registry: RetryRegistry): Retry {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(5) // More retries for payment operations
            .waitDuration(Duration.ofMillis(200))
            .intervalFunction(
                io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                    Duration.ofMillis(200), 2.0
                )
            )
            .maxInterval(Duration.ofSeconds(10))
            .build()

        return registry.retry("payment-gateway", config)
    }

    /**
     * Time Limiter Registry
     */
    @Bean
    fun timeLimiterRegistry(): TimeLimiterRegistry {
        val config = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(5))
            .cancelRunningFuture(true)
            .build()

        return TimeLimiterRegistry.of(config)
    }

    /**
     * Default Time Limiter
     */
    @Bean
    fun defaultTimeLimiter(registry: TimeLimiterRegistry): TimeLimiter {
        return registry.timeLimiter("default")
    }

    /**
     * Time Limiter for Payment Gateway operations
     */
    @Bean
    fun paymentGatewayTimeLimiter(registry: TimeLimiterRegistry): TimeLimiter {
        val config = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(10)) // Longer timeout for payments
            .cancelRunningFuture(true)
            .build()

        return registry.timeLimiter("payment-gateway", config)
    }
}

