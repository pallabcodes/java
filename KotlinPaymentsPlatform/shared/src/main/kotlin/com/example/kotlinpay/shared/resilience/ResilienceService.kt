package com.example.kotlinpay.shared.resilience

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.timelimiter.TimeLimiter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException

/**
 * Resilience service for executing operations with circuit breaker, retry, and timeout.
 */
@Service
class ResilienceService(
    private val defaultCircuitBreaker: CircuitBreaker,
    private val paymentGatewayCircuitBreaker: CircuitBreaker,
    private val defaultRetry: Retry,
    private val paymentGatewayRetry: Retry,
    private val defaultTimeLimiter: TimeLimiter,
    private val paymentGatewayTimeLimiter: TimeLimiter
) {
    private val logger = LoggerFactory.getLogger(ResilienceService::class.java)

    /**
     * Execute operation with circuit breaker, retry, and timeout
     */
    fun <T> executeWithResilience(
        operation: () -> T,
        fallback: () -> T
    ): T {
        return try {
            Retry.decorateSupplier(defaultRetry) {
                CircuitBreaker.decorateSupplier(defaultCircuitBreaker, operation)
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
     * Execute payment gateway operation with payment-specific resilience
     */
    fun <T> executePaymentGatewayOperation(
        operation: () -> T,
        fallback: () -> T
    ): T {
        return try {
            Retry.decorateSupplier(paymentGatewayRetry) {
                CircuitBreaker.decorateSupplier(paymentGatewayCircuitBreaker, operation)
            }.get()
        } catch (e: CallNotPermittedException) {
            logger.warn("Payment gateway circuit breaker is OPEN, using fallback")
            fallback()
        } catch (e: Exception) {
            logger.error("Payment gateway operation failed after retries, using fallback", e)
            fallback()
        }
    }

    /**
     * Execute async operation with timeout
     */
    fun <T> executeAsyncWithTimeout(
        operation: () -> CompletableFuture<T>,
        fallback: () -> T
    ): CompletableFuture<T> {
        return try {
            TimeLimiter.decorateFutureSupplier(defaultTimeLimiter, operation).get()
        } catch (e: TimeoutException) {
            logger.warn("Operation timed out, using fallback")
            CompletableFuture.completedFuture(fallback())
        } catch (e: Exception) {
            logger.error("Async operation failed, using fallback", e)
            CompletableFuture.completedFuture(fallback())
        }
    }

    /**
     * Execute payment gateway async operation with timeout
     */
    fun <T> executePaymentGatewayAsyncWithTimeout(
        operation: () -> CompletableFuture<T>,
        fallback: () -> T
    ): CompletableFuture<T> {
        return try {
            TimeLimiter.decorateFutureSupplier(paymentGatewayTimeLimiter, operation).get()
        } catch (e: TimeoutException) {
            logger.warn("Payment gateway operation timed out, using fallback")
            CompletableFuture.completedFuture(fallback())
        } catch (e: Exception) {
            logger.error("Payment gateway async operation failed, using fallback", e)
            CompletableFuture.completedFuture(fallback())
        }
    }
}

