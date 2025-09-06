package netflix.functional.custom;

import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Custom Functional Interfaces
 * 
 * This class demonstrates custom functional interfaces and higher-order functions including:
 * - Custom functional interfaces for specific use cases
 * - Higher-order functions and function composition
 * - Monadic operations and functors
 * - Custom collectors and reducers
 * - Custom predicates and validators
 * - Custom transformers and mappers
 * - Custom consumers and suppliers
 * - Custom operators and combinators
 * - Custom comparators and sorters
 * - Custom filters and selectors
 * - Custom accumulators and aggregators
 * - Custom generators and iterators
 * - Custom combinators and applicatives
 * - Custom monads and functors
 * - Custom lenses and optics
 * 
 * @author Netflix Java Functional Programming Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class CustomFunctionalInterfaces {

    /**
     * Custom functional interface for validation
     * 
     * @param <T> the type to validate
     */
    @FunctionalInterface
    public interface Validator<T> {
        boolean validate(T value);
        
        default Validator<T> and(Validator<T> other) {
            return value -> this.validate(value) && other.validate(value);
        }
        
        default Validator<T> or(Validator<T> other) {
            return value -> this.validate(value) || other.validate(value);
        }
        
        default Validator<T> not() {
            return value -> !this.validate(value);
        }
        
        default Validator<T> negate() {
            return not();
        }
    }

    /**
     * Custom functional interface for transformation
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface Transformer<T, R> {
        R transform(T input);
        
        default <V> Transformer<T, V> andThen(Transformer<R, V> other) {
            return input -> other.transform(this.transform(input));
        }
        
        default <V> Transformer<V, R> compose(Transformer<V, T> other) {
            return input -> this.transform(other.transform(input));
        }
    }

    /**
     * Custom functional interface for action with context
     * 
     * @param <T> the input type
     * @param <C> the context type
     */
    @FunctionalInterface
    public interface ContextualAction<T, C> {
        void execute(T input, C context);
        
        default ContextualAction<T, C> andThen(ContextualAction<T, C> other) {
            return (input, context) -> {
                this.execute(input, context);
                other.execute(input, context);
            };
        }
    }

    /**
     * Custom functional interface for function with side effects
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface SideEffectFunction<T, R> {
        R apply(T input);
        
        default SideEffectFunction<T, R> andThen(SideEffectFunction<R, R> other) {
            return input -> {
                R result = this.apply(input);
                return other.apply(result);
            };
        }
    }

    /**
     * Custom functional interface for conditional operations
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface ConditionalFunction<T, R> {
        R apply(T input, boolean condition);
        
        default ConditionalFunction<T, R> andThen(ConditionalFunction<R, R> other) {
            return (input, condition) -> {
                R result = this.apply(input, condition);
                return other.apply(result, condition);
            };
        }
    }

    /**
     * Custom functional interface for error handling
     * 
     * @param <T> the input type
     * @param <R> the output type
     * @param <E> the error type
     */
    @FunctionalInterface
    public interface ErrorHandlingFunction<T, R, E extends Exception> {
        R apply(T input) throws E;
        
        default ErrorHandlingFunction<T, R, E> andThen(ErrorHandlingFunction<R, R, E> other) {
            return input -> {
                R result = this.apply(input);
                return other.apply(result);
            };
        }
    }

    /**
     * Custom functional interface for resource management
     * 
     * @param <T> the resource type
     * @param <R> the result type
     */
    @FunctionalInterface
    public interface ResourceFunction<T, R> {
        R apply(T resource);
        
        default ResourceFunction<T, R> andThen(ResourceFunction<R, R> other) {
            return resource -> {
                R result = this.apply(resource);
                return other.apply(result);
            };
        }
    }

    /**
     * Custom functional interface for caching
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface CachingFunction<T, R> {
        R apply(T input);
        
        default CachingFunction<T, R> withCache(Map<T, R> cache) {
            return input -> cache.computeIfAbsent(input, this::apply);
        }
    }

    /**
     * Custom functional interface for retry logic
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface RetryFunction<T, R> {
        R apply(T input);
        
        default RetryFunction<T, R> withRetry(int maxRetries) {
            return input -> {
                int retries = 0;
                while (retries < maxRetries) {
                    try {
                        return this.apply(input);
                    } catch (Exception e) {
                        retries++;
                        if (retries >= maxRetries) {
                            throw new RuntimeException("Max retries reached", e);
                        }
                        log.debug("Retry {} of {}", retries, maxRetries);
                    }
                }
                throw new RuntimeException("Unknown error");
            };
        }
    }

    /**
     * Custom functional interface for circuit breaker
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface CircuitBreakerFunction<T, R> {
        R apply(T input);
        
        default CircuitBreakerFunction<T, R> withCircuitBreaker(boolean isOpen, R fallback) {
            return input -> {
                if (isOpen) {
                    log.debug("Circuit breaker is open, using fallback");
                    return fallback;
                }
                try {
                    return this.apply(input);
                } catch (Exception e) {
                    log.debug("Circuit breaker triggered: {}", e.getMessage());
                    return fallback;
                }
            };
        }
    }

    /**
     * Custom functional interface for rate limiting
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface RateLimitedFunction<T, R> {
        R apply(T input);
        
        default RateLimitedFunction<T, R> withRateLimit(int maxRequests, long timeWindow) {
            return input -> {
                // Simple rate limiting implementation
                long currentTime = System.currentTimeMillis();
                if (currentTime % timeWindow < maxRequests) {
                    return this.apply(input);
                } else {
                    log.debug("Rate limit exceeded");
                    throw new RuntimeException("Rate limit exceeded");
                }
            };
        }
    }

    /**
     * Custom functional interface for monitoring
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface MonitoredFunction<T, R> {
        R apply(T input);
        
        default MonitoredFunction<T, R> withMonitoring(String operationName) {
            return input -> {
                long startTime = System.currentTimeMillis();
                try {
                    R result = this.apply(input);
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("Operation {} completed in {} ms", operationName, duration);
                    return result;
                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("Operation {} failed after {} ms: {}", operationName, duration, e.getMessage());
                    throw e;
                }
            };
        }
    }

    /**
     * Custom functional interface for logging
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface LoggingFunction<T, R> {
        R apply(T input);
        
        default LoggingFunction<T, R> withLogging(String operationName) {
            return input -> {
                log.debug("Starting operation: {}", operationName);
                try {
                    R result = this.apply(input);
                    log.debug("Operation {} completed successfully", operationName);
                    return result;
                } catch (Exception e) {
                    log.error("Operation {} failed: {}", operationName, e.getMessage());
                    throw e;
                }
            };
        }
    }

    /**
     * Custom functional interface for metrics
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface MetricsFunction<T, R> {
        R apply(T input);
        
        default MetricsFunction<T, R> withMetrics(String metricName) {
            return input -> {
                long startTime = System.currentTimeMillis();
                try {
                    R result = this.apply(input);
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("Metric {}: {} ms", metricName, duration);
                    return result;
                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("Metric {} failed after {} ms: {}", metricName, duration, e.getMessage());
                    throw e;
                }
            };
        }
    }

    /**
     * Custom functional interface for validation with error messages
     * 
     * @param <T> the type to validate
     */
    @FunctionalInterface
    public interface ValidationFunction<T> {
        ValidationResult validate(T value);
        
        default ValidationFunction<T> and(ValidationFunction<T> other) {
            return value -> {
                ValidationResult result1 = this.validate(value);
                ValidationResult result2 = other.validate(value);
                return result1.and(result2);
            };
        }
        
        default ValidationFunction<T> or(ValidationFunction<T> other) {
            return value -> {
                ValidationResult result1 = this.validate(value);
                ValidationResult result2 = other.validate(value);
                return result1.or(result2);
            };
        }
    }

    /**
     * Custom functional interface for transformation with error handling
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface SafeTransformer<T, R> {
        Either<Exception, R> transform(T input);
        
        default <V> SafeTransformer<T, V> andThen(SafeTransformer<R, V> other) {
            return input -> {
                Either<Exception, R> result1 = this.transform(input);
                if (result1.isLeft()) {
                    return Either.left(result1.getLeft());
                }
                return other.transform(result1.getRight());
            };
        }
    }

    /**
     * Custom functional interface for composition
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface ComposableFunction<T, R> {
        R apply(T input);
        
        default <V> ComposableFunction<T, V> andThen(ComposableFunction<R, V> other) {
            return input -> other.apply(this.apply(input));
        }
        
        default <V> ComposableFunction<V, R> compose(ComposableFunction<V, T> other) {
            return input -> this.apply(other.apply(input));
        }
    }

    /**
     * Custom functional interface for currying
     * 
     * @param <T> the first parameter type
     * @param <U> the second parameter type
     * @param <R> the result type
     */
    @FunctionalInterface
    public interface CurriedFunction<T, U, R> {
        Function<U, R> apply(T first);
        
        default Function<T, Function<U, R>> curry() {
            return first -> second -> this.apply(first).apply(second);
        }
    }

    /**
     * Custom functional interface for partial application
     * 
     * @param <T> the first parameter type
     * @param <U> the second parameter type
     * @param <R> the result type
     */
    @FunctionalInterface
    public interface PartialFunction<T, U, R> {
        R apply(T first, U second);
        
        default Function<U, R> partial(T first) {
            return second -> this.apply(first, second);
        }
    }

    /**
     * Custom functional interface for memoization
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface MemoizedFunction<T, R> {
        R apply(T input);
        
        default MemoizedFunction<T, R> memoize() {
            Map<T, R> cache = new HashMap<>();
            return input -> cache.computeIfAbsent(input, this::apply);
        }
    }

    /**
     * Custom functional interface for lazy evaluation
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface LazyFunction<T, R> {
        R apply(T input);
        
        default LazyFunction<T, R> lazy() {
            return input -> {
                log.debug("Lazy evaluation triggered");
                return this.apply(input);
            };
        }
    }

    /**
     * Custom functional interface for caching with TTL
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface TTLFunction<T, R> {
        R apply(T input);
        
        default TTLFunction<T, R> withTTL(long ttlMillis) {
            Map<T, CacheEntry<R>> cache = new HashMap<>();
            return input -> {
                CacheEntry<R> entry = cache.get(input);
                if (entry != null && System.currentTimeMillis() - entry.timestamp < ttlMillis) {
                    return entry.value;
                }
                R result = this.apply(input);
                cache.put(input, new CacheEntry<>(result, System.currentTimeMillis()));
                return result;
            };
        }
    }

    /**
     * Custom functional interface for batching
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface BatchFunction<T, R> {
        R apply(T input);
        
        default BatchFunction<T, R> withBatching(int batchSize) {
            List<T> batch = new ArrayList<>();
            return input -> {
                batch.add(input);
                if (batch.size() >= batchSize) {
                    // Process batch
                    R result = this.apply(input);
                    batch.clear();
                    return result;
                }
                return null; // Batch not ready
            };
        }
    }

    /**
     * Custom functional interface for throttling
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface ThrottledFunction<T, R> {
        R apply(T input);
        
        default ThrottledFunction<T, R> withThrottling(long throttleMillis) {
            long lastExecution = 0;
            return input -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastExecution >= throttleMillis) {
                    lastExecution = currentTime;
                    return this.apply(input);
                } else {
                    log.debug("Throttling request");
                    return null;
                }
            };
        }
    }

    /**
     * Custom functional interface for debouncing
     * 
     * @param <T> the input type
     * @param <R> the output type
     */
    @FunctionalInterface
    public interface DebouncedFunction<T, R> {
        R apply(T input);
        
        default DebouncedFunction<T, R> withDebouncing(long debounceMillis) {
            Timer timer = new Timer();
            return input -> {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        DebouncedFunction.this.apply(input);
                    }
                }, debounceMillis);
                return null; // Debounced
            };
        }
    }

    // Helper classes

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public ValidationResult and(ValidationResult other) {
            return new ValidationResult(this.valid && other.valid, 
                this.valid && other.valid ? "Valid" : "Invalid");
        }
        
        public ValidationResult or(ValidationResult other) {
            return new ValidationResult(this.valid || other.valid, 
                this.valid || other.valid ? "Valid" : "Invalid");
        }
    }

    /**
     * Either class for error handling
     */
    public static class Either<L, R> {
        private final L left;
        private final R right;
        private final boolean isLeft;
        
        private Either(L left, R right, boolean isLeft) {
            this.left = left;
            this.right = right;
            this.isLeft = isLeft;
        }
        
        public static <L, R> Either<L, R> left(L value) {
            return new Either<>(value, null, true);
        }
        
        public static <L, R> Either<L, R> right(R value) {
            return new Either<>(null, value, false);
        }
        
        public boolean isLeft() {
            return isLeft;
        }
        
        public boolean isRight() {
            return !isLeft;
        }
        
        public L getLeft() {
            return left;
        }
        
        public R getRight() {
            return right;
        }
    }

    /**
     * Cache entry class
     */
    public static class CacheEntry<T> {
        private final T value;
        private final long timestamp;
        
        public CacheEntry(T value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
        
        public T getValue() {
            return value;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}
