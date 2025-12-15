package com.netflix.streaming.infrastructure.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Resilience service for executing operations with circuit breaker, retry, and timeout.
 * 
 * Provides production-grade fault tolerance patterns.
 */
@Service
public class ResilienceService {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceService.class);

    private final CircuitBreaker defaultCircuitBreaker;
    private final CircuitBreaker kafkaCircuitBreaker;
    private final CircuitBreaker databaseCircuitBreaker;
    private final Retry defaultRetry;
    private final Retry kafkaRetry;
    private final TimeLimiter defaultTimeLimiter;
    private final TimeLimiter kafkaTimeLimiter;

    public ResilienceService(
            CircuitBreaker defaultCircuitBreaker,
            CircuitBreaker kafkaCircuitBreaker,
            CircuitBreaker databaseCircuitBreaker,
            Retry defaultRetry,
            Retry kafkaRetry,
            TimeLimiter defaultTimeLimiter,
            TimeLimiter kafkaTimeLimiter) {
        this.defaultCircuitBreaker = defaultCircuitBreaker;
        this.kafkaCircuitBreaker = kafkaCircuitBreaker;
        this.databaseCircuitBreaker = databaseCircuitBreaker;
        this.defaultRetry = defaultRetry;
        this.kafkaRetry = kafkaRetry;
        this.defaultTimeLimiter = defaultTimeLimiter;
        this.kafkaTimeLimiter = kafkaTimeLimiter;
    }

    /**
     * Execute operation with circuit breaker, retry, and timeout
     */
    public <T> T executeWithResilience(Supplier<T> operation, Supplier<T> fallback) {
        try {
            return Retry.decorateSupplier(defaultRetry,
                CircuitBreaker.decorateSupplier(defaultCircuitBreaker, operation)
            ).get();
        } catch (CallNotPermittedException e) {
            logger.warn("Circuit breaker is OPEN, using fallback");
            return fallback.get();
        } catch (Exception e) {
            logger.error("Operation failed after retries, using fallback", e);
            return fallback.get();
        }
    }

    /**
     * Execute Kafka operation with Kafka-specific resilience
     */
    public <T> T executeKafkaOperation(Supplier<T> operation, Supplier<T> fallback) {
        try {
            return Retry.decorateSupplier(kafkaRetry,
                CircuitBreaker.decorateSupplier(kafkaCircuitBreaker, operation)
            ).get();
        } catch (CallNotPermittedException e) {
            logger.warn("Kafka circuit breaker is OPEN, using fallback");
            return fallback.get();
        } catch (Exception e) {
            logger.error("Kafka operation failed after retries, using fallback", e);
            return fallback.get();
        }
    }

    /**
     * Execute database operation with database-specific resilience
     */
    public <T> T executeDatabaseOperation(Supplier<T> operation, Supplier<T> fallback) {
        try {
            return Retry.decorateSupplier(defaultRetry,
                CircuitBreaker.decorateSupplier(databaseCircuitBreaker, operation)
            ).get();
        } catch (CallNotPermittedException e) {
            logger.warn("Database circuit breaker is OPEN, using fallback");
            return fallback.get();
        } catch (Exception e) {
            logger.error("Database operation failed after retries, using fallback", e);
            return fallback.get();
        }
    }

    /**
     * Execute async operation with timeout
     */
    public <T> CompletableFuture<T> executeAsyncWithTimeout(
            Supplier<CompletableFuture<T>> operation, Supplier<T> fallback) {
        try {
            return TimeLimiter.decorateFutureSupplier(defaultTimeLimiter, operation)
                .get();
        } catch (TimeoutException e) {
            logger.warn("Operation timed out, using fallback");
            return CompletableFuture.completedFuture(fallback.get());
        } catch (Exception e) {
            logger.error("Async operation failed, using fallback", e);
            return CompletableFuture.completedFuture(fallback.get());
        }
    }

    /**
     * Execute Kafka async operation with timeout
     */
    public <T> CompletableFuture<T> executeKafkaAsyncWithTimeout(
            Supplier<CompletableFuture<T>> operation, Supplier<T> fallback) {
        try {
            return TimeLimiter.decorateFutureSupplier(kafkaTimeLimiter, operation)
                .get();
        } catch (TimeoutException e) {
            logger.warn("Kafka operation timed out, using fallback");
            return CompletableFuture.completedFuture(fallback.get());
        } catch (Exception e) {
            logger.error("Kafka async operation failed, using fallback", e);
            return CompletableFuture.completedFuture(fallback.get());
        }
    }
}

