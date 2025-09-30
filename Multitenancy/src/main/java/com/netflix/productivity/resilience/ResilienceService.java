package com.netflix.productivity.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Service
@Slf4j
public class ResilienceService {

    @Autowired
    private CircuitBreaker circuitBreaker;

    @Autowired
    private TimeLimiter timeLimiter;

    @Autowired
    private Bulkhead bulkhead;

    @Autowired
    private ThreadPoolBulkhead threadPoolBulkhead;

    // Webhook processing with resilience
    public <T> T executeWebhookCommand(Supplier<T> operation, Supplier<T> fallback) {
        return CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
            return Bulkhead.decorateSupplier(bulkhead, () -> {
                try {
                    return operation.get();
                } catch (Exception e) {
                    log.warn("Webhook operation failed, using fallback", e);
                    return fallback.get();
                }
            }).get();
        }).get();
    }

    public <T> CompletableFuture<T> executeWebhookCommandAsync(Supplier<T> operation, Supplier<T> fallback) {
        return TimeLimiter.decorateCompletionStage(timeLimiter, () -> {
            return CompletableFuture.supplyAsync(() -> {
                return CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                    return Bulkhead.decorateSupplier(bulkhead, () -> {
                        try {
                            return operation.get();
                        } catch (Exception e) {
                            log.warn("Webhook async operation failed, using fallback", e);
                            return fallback.get();
                        }
                    }).get();
                }).get();
            });
        }).toCompletableFuture();
    }

    // External API calls with resilience
    public <T> T executeExternalApiCommand(Supplier<T> operation, Supplier<T> fallback) {
        return CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
            return Bulkhead.decorateSupplier(bulkhead, () -> {
                try {
                    return operation.get();
                } catch (Exception e) {
                    log.warn("External API operation failed, using fallback", e);
                    return fallback.get();
                }
            }).get();
        }).get();
    }

    public <T> CompletableFuture<T> executeExternalApiCommandAsync(Supplier<T> operation, Supplier<T> fallback) {
        return TimeLimiter.decorateCompletionStage(timeLimiter, () -> {
            return CompletableFuture.supplyAsync(() -> {
                return CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                    return Bulkhead.decorateSupplier(bulkhead, () -> {
                        try {
                            return operation.get();
                        } catch (Exception e) {
                            log.warn("External API async operation failed, using fallback", e);
                            return fallback.get();
                        }
                    }).get();
                }).get();
            });
        }).toCompletableFuture();
    }

    // Database operations with resilience
    public <T> T executeDatabaseCommand(Supplier<T> operation, Supplier<T> fallback) {
        return CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
            return Bulkhead.decorateSupplier(bulkhead, () -> {
                try {
                    return operation.get();
                } catch (Exception e) {
                    log.warn("Database operation failed, using fallback", e);
                    return fallback.get();
                }
            }).get();
        }).get();
    }

    public <T> CompletableFuture<T> executeDatabaseCommandAsync(Supplier<T> operation, Supplier<T> fallback) {
        return TimeLimiter.decorateCompletionStage(timeLimiter, () -> {
            return CompletableFuture.supplyAsync(() -> {
                return CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                    return Bulkhead.decorateSupplier(bulkhead, () -> {
                        try {
                            return operation.get();
                        } catch (Exception e) {
                            log.warn("Database async operation failed, using fallback", e);
                            return fallback.get();
                        }
                    }).get();
                }).get();
            });
        }).toCompletableFuture();
    }

    // Thread pool bulkhead for CPU-intensive operations
    public <T> CompletableFuture<T> executeCpuIntensiveCommand(Supplier<T> operation, Supplier<T> fallback) {
        return ThreadPoolBulkhead.decorateSupplier(threadPoolBulkhead, () -> {
            return CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                try {
                    return operation.get();
                } catch (Exception e) {
                    log.warn("CPU intensive operation failed, using fallback", e);
                    return fallback.get();
                }
            }).get();
        }).get().toCompletableFuture();
    }

    public String fallbackMethod(String input, Exception ex) {
        if (ex instanceof CallNotPermittedException) {
            return "Fallback: Circuit breaker is open";
        } else if (ex instanceof TimeoutException) {
            return "Fallback: Request timed out";
        } else {
            return "Fallback: Service temporarily unavailable";
        }
    }

    // Health check for resilience components
    public ResilienceHealth getHealth() {
        return ResilienceHealth.builder()
                .circuitBreakerState(circuitBreaker.getState().name())
                .bulkheadAvailablePermits(bulkhead.getMetrics().getAvailableConcurrentCalls())
                .threadPoolBulkheadAvailablePermits(threadPoolBulkhead.getMetrics().getAvailableConcurrentCalls())
                .build();
    }
}
