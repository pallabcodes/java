package netflix.streams.advanced;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Reactive Stream Interface for Netflix Production Systems
 * 
 * <p>This interface provides reactive programming patterns for high-throughput,
 * fault-tolerant stream processing. It implements advanced patterns including:</p>
 * 
 * <ul>
 *   <li><strong>Backpressure Handling</strong>: Automatic flow control to prevent memory overflow</li>
 *   <li><strong>Circuit Breaker Integration</strong>: Fault tolerance with automatic recovery</li>
 *   <li><strong>Rate Limiting</strong>: Adaptive rate control based on downstream capacity</li>
 *   <li><strong>Error Recovery</strong>: Comprehensive error handling and retry strategies</li>
 *   <li><strong>Observability</strong>: Built-in metrics, tracing, and monitoring</li>
 *   <li><strong>Resource Management</strong>: Advanced memory and CPU optimization</li>
 * </ul>
 * 
 * <p><strong>For TypeScript/Node.js Developers:</strong><br>
 * This is similar to RxJS Observables with advanced operators, backpressure
 * handling, and circuit breaker patterns. It's like building a production-grade
 * reactive programming framework with enterprise features.</p>
 * 
 * @param <T> the type of stream elements
 * @author Netflix Principal Engineering Team
 * @version 1.0.0
 * @since 2024
 */
public interface ReactiveStream<T> {

    // ========== REACTIVE STREAM OPERATIONS ==========
    
    /**
     * Maps elements with backpressure handling and rate limiting.
     * 
     * <p>This method implements reactive stream patterns for high-throughput
     * processing with automatic backpressure handling:</p>
     * <ul>
     *   <li>Backpressure handling to prevent memory overflow</li>
     *   <li>Rate limiting based on downstream capacity</li>
     *   <li>Adaptive buffering strategies</li>
     *   <li>Flow control and congestion management</li>
     * </ul>
     * 
     * @param <R> the type of result elements
     * @param mapper the mapping function
     * @param bufferSize the buffer size for backpressure handling
     * @param rateLimit the maximum processing rate (elements per second)
     * @return a new reactive stream with backpressure handling
     */
    <R> ReactiveStream<R> mapReactive(
            Function<? super T, ? extends R> mapper,
            int bufferSize,
            long rateLimit);
    
    /**
     * Maps elements with circuit breaker protection and automatic fallback.
     * 
     * <p>This method implements sophisticated fault tolerance patterns:</p>
     * <ul>
     *   <li>Circuit breaker integration for automatic failure detection</li>
     *   <li>Fallback strategies for degraded service scenarios</li>
     *   <li>Adaptive retry logic with exponential backoff</li>
     *   <li>Comprehensive error handling and recovery</li>
     * </ul>
     * 
     * @param <R> the type of result elements
     * @param mapper the mapping function that may fail
     * @param fallback the fallback value for failures
     * @param retryAttempts the number of retry attempts
     * @return a new reactive stream with circuit breaker protection
     */
    <R> ReactiveStream<R> mapWithCircuitBreaker(
            Function<? super T, ? extends R> mapper,
            R fallback,
            int retryAttempts);
    
    /**
     * Maps elements with comprehensive monitoring and observability.
     * 
     * <p>This method provides detailed observability for production systems:</p>
     * <ul>
     *   <li>Performance metrics collection</li>
     *   <li>Error rate monitoring</li>
     *   <li>Resource usage tracking</li>
     *   <li>Distributed tracing integration</li>
     * </ul>
     * 
     * @param <R> the type of result elements
     * @param mapper the mapping function
     * @param operationName the name for monitoring purposes
     * @return a new monitored reactive stream
     */
    <R> ReactiveStream<R> mapWithMonitoring(
            Function<? super T, ? extends R> mapper,
            String operationName);
    
    // ========== REACTIVE FILTERING ==========
    
    /**
     * Filters elements with backpressure handling and rate limiting.
     * 
     * @param predicate the predicate function
     * @param bufferSize the buffer size for backpressure handling
     * @param rateLimit the maximum filtering rate (elements per second)
     * @return a new reactive stream with backpressure filtering
     */
    ReactiveStream<T> filterReactive(
            Predicate<? super T> predicate,
            int bufferSize,
            long rateLimit);
    
    /**
     * Filters elements with circuit breaker protection and fallback strategies.
     * 
     * @param predicate the predicate that may fail
     * @param fallbackStrategy the fallback strategy for failures
     * @return a new reactive stream with circuit breaker filtering
     */
    ReactiveStream<T> filterWithCircuitBreaker(
            Predicate<? super T> predicate,
            FilterFallbackStrategy fallbackStrategy);
    
    // ========== REACTIVE COLLECTORS ==========
    
    /**
     * Collects elements using a reactive collector with backpressure handling.
     * 
     * @param <R> the type of result
     * @param collector the collector to use
     * @param bufferSize the buffer size for backpressure handling
     * @param rateLimit the maximum collection rate (elements per second)
     * @return a CompletableFuture containing the collection result
     */
    <R> CompletableFuture<R> collectReactive(
            Collector<? super T, ?, R> collector,
            int bufferSize,
            long rateLimit);
    
    /**
     * Collects elements using a circuit breaker collector with fallback.
     * 
     * @param <R> the type of result
     * @param collector the collector to use
     * @param fallback the fallback result
     * @return a CompletableFuture containing the collection result
     */
    <R> CompletableFuture<R> collectWithCircuitBreaker(
            Collector<? super T, ?, R> collector,
            R fallback);
    
    // ========== REACTIVE REDUCTION ==========
    
    /**
     * Reduces elements with backpressure handling and rate limiting.
     * 
     * @param identity the identity value
     * @param accumulator the accumulator function
     * @param combiner the combiner function
     * @param bufferSize the buffer size for backpressure handling
     * @param rateLimit the maximum reduction rate (elements per second)
     * @return a CompletableFuture containing the reduction result
     */
    CompletableFuture<T> reduceReactive(
            T identity,
            BinaryOperator<T> accumulator,
            BinaryOperator<T> combiner,
            int bufferSize,
            long rateLimit);
    
    /**
     * Reduces elements with circuit breaker protection and fallback strategies.
     * 
     * @param identity the identity value
     * @param accumulator the accumulator function
     * @param combiner the combiner function
     * @param fallback the fallback result
     * @return a CompletableFuture containing the reduction result
     */
    CompletableFuture<T> reduceWithCircuitBreaker(
            T identity,
            BinaryOperator<T> accumulator,
            BinaryOperator<T> combiner,
            T fallback);
    
    // ========== REACTIVE UTILITIES ==========
    
    /**
     * Subscribes to the stream with a consumer and error handler.
     * 
     * @param onNext the consumer for successful elements
     * @param onError the error handler
     * @param onComplete the completion handler
     * @return a CompletableFuture that completes when the stream is finished
     */
    CompletableFuture<Void> subscribe(
            Consumer<? super T> onNext,
            Consumer<Throwable> onError,
            Runnable onComplete);
    
    /**
     * Subscribes to the stream with backpressure handling.
     * 
     * @param onNext the consumer for successful elements
     * @param onError the error handler
     * @param onComplete the completion handler
     * @param bufferSize the buffer size for backpressure handling
     * @param rateLimit the maximum processing rate (elements per second)
     * @return a CompletableFuture that completes when the stream is finished
     */
    CompletableFuture<Void> subscribeWithBackpressure(
            Consumer<? super T> onNext,
            Consumer<Throwable> onError,
            Runnable onComplete,
            int bufferSize,
            long rateLimit);
    
    // ========== MONITORING AND OBSERVABILITY ==========
    
    /**
     * Gets performance metrics for this reactive stream.
     * 
     * @return a map of performance metrics
     */
    Map<String, Object> getReactiveMetrics();
    
    /**
     * Gets the current health status of this reactive stream.
     * 
     * @return true if the stream is healthy, false otherwise
     */
    boolean isReactiveHealthy();
    
    /**
     * Gets the current backpressure status.
     * 
     * @return the current backpressure level (0.0 to 1.0)
     */
    double getBackpressureLevel();
    
    /**
     * Gets the current processing rate.
     * 
     * @return the current processing rate (elements per second)
     */
    double getProcessingRate();
    
    // ========== ENUMS ==========
    
    /**
     * Fallback strategies for filter operations.
     */
    enum FilterFallbackStrategy {
        INCLUDE_ALL,    // Include all elements when predicate fails
        EXCLUDE_ALL,    // Exclude all elements when predicate fails
        USE_DEFAULT,    // Use a default predicate result
        FAIL_FAST       // Fail immediately when predicate fails
    }
}
