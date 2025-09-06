package netflix.functional.async;

import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade CompletableFuture Examples
 * 
 * This class demonstrates comprehensive CompletableFuture concepts including:
 * - CompletableFuture creation and basic operations
 * - Asynchronous execution and composition
 * - Exception handling and recovery
 * - Timeout and cancellation
 * - Parallel execution and coordination
 * - Custom thread pools and executors
 * - Performance optimization and monitoring
 * - Integration with streams and collections
 * - Error handling and resilience patterns
 * - Testing and debugging
 * - Production best practices
 * - Netflix-specific patterns
 * 
 * @author Netflix Java Functional Programming Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class CompletableFutureExamples {

    private static final ExecutorService customExecutor = Executors.newFixedThreadPool(10);

    /**
     * Demonstrates CompletableFuture creation and basic operations
     * 
     * CompletableFuture can be created in various ways for asynchronous execution.
     */
    public static void demonstrateCompletableFutureCreation() {
        log.info("=== Demonstrating CompletableFuture Creation ===");
        
        // Completed future
        CompletableFuture<String> completed = CompletableFuture.completedFuture("Hello World");
        log.debug("Completed future: {}", completed.join());
        
        // Failed future
        CompletableFuture<String> failed = CompletableFuture.failedFuture(new RuntimeException("Test error"));
        try {
            failed.join();
        } catch (CompletionException e) {
            log.debug("Failed future error: {}", e.getCause().getMessage());
        }
        
        // Future with supplier
        CompletableFuture<String> supplier = CompletableFuture.supplyAsync(() -> {
            log.debug("Executing supplier");
            return "Hello from supplier";
        });
        log.debug("Supplier future: {}", supplier.join());
        
        // Future with runnable
        CompletableFuture<Void> runnable = CompletableFuture.runAsync(() -> {
            log.debug("Executing runnable");
        });
        runnable.join();
        log.debug("Runnable future completed");
        
        // Future with custom executor
        CompletableFuture<String> customExecutorFuture = CompletableFuture.supplyAsync(() -> {
            log.debug("Executing with custom executor");
            return "Hello from custom executor";
        }, customExecutor);
        log.debug("Custom executor future: {}", customExecutorFuture.join());
        
        // Future with delayed execution
        CompletableFuture<String> delayed = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Hello after delay";
        });
        log.debug("Delayed future: {}", delayed.join());
    }

    /**
     * Demonstrates asynchronous execution and composition
     * 
     * CompletableFuture supports various composition patterns for complex async operations.
     */
    public static void demonstrateAsyncComposition() {
        log.info("=== Demonstrating Async Composition ===");
        
        // Basic composition with thenApply
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello")
                .thenApply(s -> s + " World")
                .thenApply(String::toUpperCase);
        log.debug("Basic composition: {}", future1.join());
        
        // Composition with thenCompose
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "Hello")
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World"))
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + "!"));
        log.debug("Compose composition: {}", future2.join());
        
        // Composition with thenCombine
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> "Hello")
                .thenCombine(CompletableFuture.supplyAsync(() -> "World"), (s1, s2) -> s1 + " " + s2)
                .thenApply(String::toUpperCase);
        log.debug("Combine composition: {}", future3.join());
        
        // Composition with thenAccept
        CompletableFuture<Void> future4 = CompletableFuture.supplyAsync(() -> "Hello World")
                .thenApply(String::toUpperCase)
                .thenAccept(s -> log.debug("Accepted: {}", s));
        future4.join();
        
        // Composition with thenRun
        CompletableFuture<Void> future5 = CompletableFuture.supplyAsync(() -> "Hello World")
                .thenApply(String::toUpperCase)
                .thenRun(() -> log.debug("Running after completion"));
        future5.join();
        
        // Composition with handle
        CompletableFuture<String> future6 = CompletableFuture.supplyAsync(() -> "Hello World")
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        log.debug("Error occurred: {}", throwable.getMessage());
                        return "Error occurred";
                    }
                    return result.toUpperCase();
                });
        log.debug("Handle composition: {}", future6.join());
        
        // Composition with whenComplete
        CompletableFuture<String> future7 = CompletableFuture.supplyAsync(() -> "Hello World")
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.debug("Error occurred: {}", throwable.getMessage());
                    } else {
                        log.debug("Completed successfully: {}", result);
                    }
                });
        log.debug("When complete composition: {}", future7.join());
    }

    /**
     * Demonstrates exception handling and recovery
     * 
     * CompletableFuture provides comprehensive exception handling capabilities.
     */
    public static void demonstrateExceptionHandling() {
        log.info("=== Demonstrating Exception Handling ===");
        
        // Exception handling with handle
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Test error");
        }).handle((result, throwable) -> {
            if (throwable != null) {
                log.debug("Error handled: {}", throwable.getMessage());
                return "Error occurred";
            }
            return result;
        });
        log.debug("Handle exception: {}", future1.join());
        
        // Exception handling with exceptionally
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Test error");
        }).exceptionally(throwable -> {
            log.debug("Error handled exceptionally: {}", throwable.getMessage());
            return "Error occurred";
        });
        log.debug("Exceptionally handled: {}", future2.join());
        
        // Exception handling with whenComplete
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Test error");
        }).whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.debug("Error in whenComplete: {}", throwable.getMessage());
            }
        });
        try {
            future3.join();
        } catch (CompletionException e) {
            log.debug("Completion exception: {}", e.getCause().getMessage());
        }
        
        // Exception handling with recover
        CompletableFuture<String> future4 = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Test error");
        }).handle((result, throwable) -> {
            if (throwable != null) {
                return "Recovered from error";
            }
            return result;
        });
        log.debug("Recovered: {}", future4.join());
        
        // Exception handling with retry
        CompletableFuture<String> future5 = retryAsync(() -> {
            throw new RuntimeException("Test error");
        }, 3);
        log.debug("Retry result: {}", future5.join());
    }

    /**
     * Demonstrates timeout and cancellation
     * 
     * CompletableFuture supports timeout and cancellation for better control.
     */
    public static void demonstrateTimeoutAndCancellation() {
        log.info("=== Demonstrating Timeout and Cancellation ===");
        
        // Timeout with orTimeout
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Hello after delay";
        }).orTimeout(1, TimeUnit.SECONDS);
        
        try {
            future1.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof TimeoutException) {
                log.debug("Timeout occurred: {}", e.getCause().getMessage());
            }
        }
        
        // Timeout with completeOnTimeout
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Hello after delay";
        }).completeOnTimeout("Timeout value", 1, TimeUnit.SECONDS);
        
        log.debug("Complete on timeout: {}", future2.join());
        
        // Cancellation
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Interrupted";
            }
            return "Hello after delay";
        });
        
        future3.cancel(true);
        log.debug("Cancelled: {}", future3.isCancelled());
        
        // Timeout with custom executor
        CompletableFuture<String> future4 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Hello after delay";
        }, customExecutor).orTimeout(1, TimeUnit.SECONDS);
        
        try {
            future4.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof TimeoutException) {
                log.debug("Custom executor timeout: {}", e.getCause().getMessage());
            }
        }
    }

    /**
     * Demonstrates parallel execution and coordination
     * 
     * CompletableFuture supports parallel execution and coordination patterns.
     */
    public static void demonstrateParallelExecution() {
        log.info("=== Demonstrating Parallel Execution ===");
        
        // Parallel execution with allOf
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "World");
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> "!");
        
        CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2, future3);
        allOf.join();
        
        log.debug("All completed: {}", allOf.isDone());
        log.debug("Future1: {}", future1.join());
        log.debug("Future2: {}", future2.join());
        log.debug("Future3: {}", future3.join());
        
        // Parallel execution with anyOf
        CompletableFuture<String> anyOf = CompletableFuture.anyOf(future1, future2, future3);
        log.debug("Any completed: {}", anyOf.join());
        
        // Parallel execution with custom coordination
        CompletableFuture<String> coordinated = future1
                .thenCombine(future2, (s1, s2) -> s1 + " " + s2)
                .thenCombine(future3, (s1, s2) -> s1 + s2);
        log.debug("Coordinated: {}", coordinated.join());
        
        // Parallel execution with streams
        List<CompletableFuture<String>> futures = Arrays.asList(
                CompletableFuture.supplyAsync(() -> "Hello"),
                CompletableFuture.supplyAsync(() -> "World"),
                CompletableFuture.supplyAsync(() -> "!")
        );
        
        CompletableFuture<List<String>> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        ).thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
        
        log.debug("All futures: {}", allFutures.join());
        
        // Parallel execution with custom thread pool
        List<CompletableFuture<String>> customFutures = Arrays.asList(
                CompletableFuture.supplyAsync(() -> "Hello", customExecutor),
                CompletableFuture.supplyAsync(() -> "World", customExecutor),
                CompletableFuture.supplyAsync(() -> "!", customExecutor)
        );
        
        CompletableFuture<List<String>> customAllFutures = CompletableFuture.allOf(
                customFutures.toArray(new CompletableFuture[0])
        ).thenApply(v -> customFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
        
        log.debug("Custom all futures: {}", customAllFutures.join());
    }

    /**
     * Demonstrates custom thread pools and executors
     * 
     * CompletableFuture can use custom thread pools for better control.
     */
    public static void demonstrateCustomThreadPools() {
        log.info("=== Demonstrating Custom Thread Pools ===");
        
        // Custom thread pool
        ExecutorService customPool = Executors.newFixedThreadPool(5);
        
        // Future with custom thread pool
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            log.debug("Executing in custom thread pool: {}", Thread.currentThread().getName());
            return "Hello from custom pool";
        }, customPool);
        log.debug("Custom pool result: {}", future1.join());
        
        // Future with custom thread pool and composition
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "Hello", customPool)
                .thenApplyAsync(s -> s + " World", customPool)
                .thenApplyAsync(String::toUpperCase, customPool);
        log.debug("Custom pool composition: {}", future2.join());
        
        // Future with custom thread pool and exception handling
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Test error");
        }, customPool).handle((result, throwable) -> {
            if (throwable != null) {
                log.debug("Error in custom pool: {}", throwable.getMessage());
                return "Error occurred";
            }
            return result;
        });
        log.debug("Custom pool error handling: {}", future3.join());
        
        // Shutdown custom thread pool
        customPool.shutdown();
        try {
            if (!customPool.awaitTermination(5, TimeUnit.SECONDS)) {
                customPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            customPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Demonstrates performance optimization and monitoring
     * 
     * CompletableFuture can be optimized for better performance and monitoring.
     */
    public static void demonstratePerformanceOptimization() {
        log.info("=== Demonstrating Performance Optimization ===");
        
        // Performance measurement
        long startTime = System.currentTimeMillis();
        
        // Sequential execution
        CompletableFuture<String> sequential = CompletableFuture.supplyAsync(() -> "Hello")
                .thenApply(s -> s + " World")
                .thenApply(String::toUpperCase);
        
        long sequentialTime = System.currentTimeMillis() - startTime;
        log.debug("Sequential execution time: {} ms", sequentialTime);
        log.debug("Sequential result: {}", sequential.join());
        
        // Parallel execution
        startTime = System.currentTimeMillis();
        
        CompletableFuture<String> parallel1 = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> parallel2 = CompletableFuture.supplyAsync(() -> "World");
        
        CompletableFuture<String> parallel = parallel1
                .thenCombine(parallel2, (s1, s2) -> s1 + " " + s2)
                .thenApply(String::toUpperCase);
        
        long parallelTime = System.currentTimeMillis() - startTime;
        log.debug("Parallel execution time: {} ms", parallelTime);
        log.debug("Parallel result: {}", parallel.join());
        
        // Performance with custom executor
        startTime = System.currentTimeMillis();
        
        CompletableFuture<String> customExecutor = CompletableFuture.supplyAsync(() -> "Hello", customExecutor)
                .thenApplyAsync(s -> s + " World", customExecutor)
                .thenApplyAsync(String::toUpperCase, customExecutor);
        
        long customExecutorTime = System.currentTimeMillis() - startTime;
        log.debug("Custom executor time: {} ms", customExecutorTime);
        log.debug("Custom executor result: {}", customExecutor.join());
        
        // Performance with monitoring
        CompletableFuture<String> monitored = CompletableFuture.supplyAsync(() -> {
            log.debug("Starting monitored execution");
            return "Hello";
        }).thenApply(s -> {
            log.debug("Processing: {}", s);
            return s + " World";
        }).thenApply(s -> {
            log.debug("Finalizing: {}", s);
            return s.toUpperCase();
        });
        
        log.debug("Monitored result: {}", monitored.join());
    }

    /**
     * Demonstrates integration with streams and collections
     * 
     * CompletableFuture can be integrated with streams and collections.
     */
    public static void demonstrateStreamIntegration() {
        log.info("=== Demonstrating Stream Integration ===");
        
        List<String> inputs = Arrays.asList("Hello", "World", "Java", "Programming");
        
        // Stream with CompletableFuture
        List<CompletableFuture<String>> futures = inputs.stream()
                .map(input -> CompletableFuture.supplyAsync(() -> input.toUpperCase()))
                .collect(Collectors.toList());
        
        // Wait for all futures
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        allFutures.join();
        
        // Collect results
        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        log.debug("Stream results: {}", results);
        
        // Stream with parallel processing
        List<String> parallelResults = inputs.parallelStream()
                .map(input -> CompletableFuture.supplyAsync(() -> input.toUpperCase()))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        log.debug("Parallel stream results: {}", parallelResults);
        
        // Stream with custom executor
        List<String> customExecutorResults = inputs.stream()
                .map(input -> CompletableFuture.supplyAsync(() -> input.toUpperCase(), customExecutor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        log.debug("Custom executor stream results: {}", customExecutorResults);
        
        // Stream with exception handling
        List<String> errorHandledResults = inputs.stream()
                .map(input -> CompletableFuture.supplyAsync(() -> {
                    if (input.equals("Java")) {
                        throw new RuntimeException("Test error");
                    }
                    return input.toUpperCase();
                }))
                .map(future -> future.handle((result, throwable) -> {
                    if (throwable != null) {
                        log.debug("Error occurred: {}", throwable.getMessage());
                        return "Error";
                    }
                    return result;
                }))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        log.debug("Error handled stream results: {}", errorHandledResults);
    }

    /**
     * Demonstrates error handling and resilience patterns
     * 
     * CompletableFuture supports various error handling and resilience patterns.
     */
    public static void demonstrateErrorHandlingAndResilience() {
        log.info("=== Demonstrating Error Handling and Resilience ===");
        
        // Circuit breaker pattern
        CompletableFuture<String> circuitBreaker = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Service unavailable");
        }).handle((result, throwable) -> {
            if (throwable != null) {
                log.debug("Circuit breaker triggered: {}", throwable.getMessage());
                return "Service unavailable, using fallback";
            }
            return result;
        });
        log.debug("Circuit breaker result: {}", circuitBreaker.join());
        
        // Retry pattern
        CompletableFuture<String> retry = retryAsync(() -> {
            throw new RuntimeException("Temporary error");
        }, 3);
        log.debug("Retry result: {}", retry.join());
        
        // Fallback pattern
        CompletableFuture<String> fallback = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Primary service failed");
        }).handle((result, throwable) -> {
            if (throwable != null) {
                log.debug("Primary service failed, using fallback");
                return "Fallback result";
            }
            return result;
        });
        log.debug("Fallback result: {}", fallback.join());
        
        // Timeout pattern
        CompletableFuture<String> timeout = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Slow result";
        }).orTimeout(1, TimeUnit.SECONDS)
                .handle((result, throwable) -> {
                    if (throwable != null && throwable.getCause() instanceof TimeoutException) {
                        log.debug("Timeout occurred, using default");
                        return "Timeout default";
                    }
                    return result;
                });
        log.debug("Timeout result: {}", timeout.join());
        
        // Bulkhead pattern
        CompletableFuture<String> bulkhead = CompletableFuture.supplyAsync(() -> {
            log.debug("Executing in bulkhead");
            return "Bulkhead result";
        }, customExecutor).handle((result, throwable) -> {
            if (throwable != null) {
                log.debug("Bulkhead error: {}", throwable.getMessage());
                return "Bulkhead error";
            }
            return result;
        });
        log.debug("Bulkhead result: {}", bulkhead.join());
    }

    /**
     * Demonstrates testing and debugging
     * 
     * CompletableFuture can be tested and debugged effectively.
     */
    public static void demonstrateTestingAndDebugging() {
        log.info("=== Demonstrating Testing and Debugging ===");
        
        // Test with completed future
        CompletableFuture<String> completed = CompletableFuture.completedFuture("Hello");
        assert completed.isDone();
        assert !completed.isCancelled();
        assert completed.join().equals("Hello");
        
        // Test with failed future
        CompletableFuture<String> failed = CompletableFuture.failedFuture(new RuntimeException("Test error"));
        assert failed.isDone();
        assert !failed.isCancelled();
        try {
            failed.join();
            assert false; // Should not reach here
        } catch (CompletionException e) {
            assert e.getCause().getMessage().equals("Test error");
        }
        
        // Test with async execution
        CompletableFuture<String> async = CompletableFuture.supplyAsync(() -> "Hello World");
        assert !async.isDone();
        String result = async.join();
        assert result.equals("Hello World");
        assert async.isDone();
        
        // Test with composition
        CompletableFuture<String> composed = CompletableFuture.supplyAsync(() -> "Hello")
                .thenApply(s -> s + " World")
                .thenApply(String::toUpperCase);
        assert composed.join().equals("HELLO WORLD");
        
        // Test with exception handling
        CompletableFuture<String> exceptionHandled = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Test error");
        }).handle((result, throwable) -> {
            if (throwable != null) {
                return "Error handled";
            }
            return result;
        });
        assert exceptionHandled.join().equals("Error handled");
        
        // Test with timeout
        CompletableFuture<String> timeout = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Slow result";
        }).orTimeout(1, TimeUnit.SECONDS);
        
        try {
            timeout.join();
            assert false; // Should not reach here
        } catch (CompletionException e) {
            assert e.getCause() instanceof TimeoutException;
        }
        
        log.debug("All tests passed!");
    }

    /**
     * Demonstrates production best practices
     * 
     * CompletableFuture should follow production best practices.
     */
    public static void demonstrateProductionBestPractices() {
        log.info("=== Demonstrating Production Best Practices ===");
        
        // Always handle exceptions
        CompletableFuture<String> exceptionHandled = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Service error");
        }).handle((result, throwable) -> {
            if (throwable != null) {
                log.error("Service error occurred: {}", throwable.getMessage());
                return "Service unavailable";
            }
            return result;
        });
        log.debug("Exception handled: {}", exceptionHandled.join());
        
        // Use appropriate timeouts
        CompletableFuture<String> timeoutHandled = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Slow result";
        }).orTimeout(1, TimeUnit.SECONDS)
                .handle((result, throwable) -> {
                    if (throwable != null && throwable.getCause() instanceof TimeoutException) {
                        log.warn("Operation timed out");
                        return "Operation timed out";
                    }
                    return result;
                });
        log.debug("Timeout handled: {}", timeoutHandled.join());
        
        // Use custom thread pools for better control
        CompletableFuture<String> customPool = CompletableFuture.supplyAsync(() -> {
            log.debug("Executing in custom thread pool");
            return "Custom pool result";
        }, customExecutor).handle((result, throwable) -> {
            if (throwable != null) {
                log.error("Custom pool error: {}", throwable.getMessage());
                return "Custom pool error";
            }
            return result;
        });
        log.debug("Custom pool: {}", customPool.join());
        
        // Monitor and log execution
        CompletableFuture<String> monitored = CompletableFuture.supplyAsync(() -> {
            log.debug("Starting monitored execution");
            return "Hello";
        }).thenApply(s -> {
            log.debug("Processing: {}", s);
            return s + " World";
        }).thenApply(s -> {
            log.debug("Finalizing: {}", s);
            return s.toUpperCase();
        }).handle((result, throwable) -> {
            if (throwable != null) {
                log.error("Monitored execution error: {}", throwable.getMessage());
                return "Error occurred";
            }
            log.debug("Monitored execution completed: {}", result);
            return result;
        });
        log.debug("Monitored result: {}", monitored.join());
    }

    // Helper methods

    private static CompletableFuture<String> retryAsync(Supplier<String> supplier, int maxRetries) {
        return CompletableFuture.supplyAsync(() -> {
            int retries = 0;
            while (retries < maxRetries) {
                try {
                    return supplier.get();
                } catch (Exception e) {
                    retries++;
                    if (retries >= maxRetries) {
                        log.debug("Max retries reached, returning error");
                        return "Max retries reached";
                    }
                    log.debug("Retry {} of {}", retries, maxRetries);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "Interrupted";
                    }
                }
            }
            return "Unknown error";
        });
    }
}
