package com.example.kotlinpay.shared.resilience

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retry.Retry
import org.slf4j.LoggerFactory

/**
 * Extension functions for easier resilience pattern usage.
 */
object ResilienceServiceExtensions {
    private val logger = LoggerFactory.getLogger(ResilienceServiceExtensions::class.java)

    /**
     * Execute operation with circuit breaker and retry
     */
    fun <T> executeWithResilience(
        circuitBreaker: CircuitBreaker,
        retry: Retry,
        operation: () -> T,
        fallback: () -> T
    ): T {
        return try {
            Retry.decorateSupplier(retry) {
                CircuitBreaker.decorateSupplier(circuitBreaker, operation)
            }.get()
        } catch (e: CallNotPermittedException) {
            logger.warn("Circuit breaker is OPEN, using fallback")
            fallback()
        } catch (e: Exception) {
            logger.error("Operation failed after retries, using fallback", e)
            fallback()
        }
    }

    /**
     * Execute operation with circuit breaker only
     */
    fun <T> executeWithCircuitBreaker(
        circuitBreaker: CircuitBreaker,
        operation: () -> T,
        fallback: () -> T
    ): T {
        return try {
            CircuitBreaker.decorateSupplier(circuitBreaker, operation).get()
        } catch (e: CallNotPermittedException) {
            logger.warn("Circuit breaker is OPEN, using fallback")
            fallback()
        } catch (e: Exception) {
            logger.error("Operation failed, using fallback", e)
            fallback()
        }
    }

    /**
     * Execute operation with retry only
     */
    fun <T> executeWithRetry(
        retry: Retry,
        operation: () -> T
    ): T {
        return Retry.decorateSupplier(retry, operation).get()
    }
}

