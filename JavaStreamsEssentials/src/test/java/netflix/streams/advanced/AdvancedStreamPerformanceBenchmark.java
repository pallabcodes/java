package netflix.streams.advanced;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Advanced Stream Performance Benchmark Suite
 * 
 * <p>This comprehensive benchmark suite validates the performance characteristics
 * of the Advanced Stream Processor under various load conditions and scenarios.
 * It ensures that the implementation meets Netflix's stringent performance
 * requirements for production systems.</p>
 * 
 * <p><strong>Performance Criteria:</strong></p>
 * <ul>
 *   <li>Memory efficiency for large datasets (1M+ elements)</li>
 *   <li>CPU utilization optimization and parallel processing effectiveness</li>
 *   <li>Circuit breaker performance impact and recovery time</li>
 *   <li>Backpressure handling under high load</li>
 *   <li>Scalability under increasing load and data size</li>
 *   <li>Latency characteristics for real-time processing</li>
 * </ul>
 * 
 * @author Netflix Backend Engineering Team
 * @version 1.0.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Advanced Stream Performance Benchmarks")
class AdvancedStreamPerformanceBenchmark {

    private static final int SMALL_DATASET_SIZE = 1000;
    private static final int MEDIUM_DATASET_SIZE = 10000;
    private static final int LARGE_DATASET_SIZE = 100000;
    private static final int EXTRA_LARGE_DATASET_SIZE = 1000000;
    private static final int MASSIVE_DATASET_SIZE = 10000000;

    @BeforeEach
    void setUp() {
        AdvancedStreamProcessor.resetAllMetrics();
    }

    // ========== BASIC PERFORMANCE BENCHMARKS ==========

    @Test
    @DisplayName("Should process small datasets efficiently")
    void benchmarkSmallDatasetPerformance() {
        // Given
        List<String> data = generateTestData(SMALL_DATASET_SIZE);
        AdvancedStream<String> stream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.SEQUENTIAL,
            AdvancedStreamProcessor.MemoryStrategy.EAGER
        );

        // When
        long startTime = System.nanoTime();
        List<String> result = stream.mapWithMonitoring(String::toUpperCase, "smallDataset")
                                   .stream()
                                   .collect(Collectors.toList());
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(SMALL_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(100); // Less than 100ms
        
        // Performance metrics
        double elementsPerSecond = (double) SMALL_DATASET_SIZE / TimeUnit.NANOSECONDS.toSeconds(processingTime);
        System.out.printf("Small Dataset Performance: %d elements in %dms (%.2f elements/sec)%n",
            SMALL_DATASET_SIZE, TimeUnit.NANOSECONDS.toMillis(processingTime), elementsPerSecond);
    }

    @Test
    @DisplayName("Should process medium datasets with good performance")
    void benchmarkMediumDatasetPerformance() {
        // Given
        List<String> data = generateTestData(MEDIUM_DATASET_SIZE);
        AdvancedStream<String> stream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.ADAPTIVE,
            AdvancedStreamProcessor.MemoryStrategy.BATCHED
        );

        // When
        long startTime = System.nanoTime();
        List<String> result = stream.mapAdaptive(String::toUpperCase)
                                   .stream()
                                   .collect(Collectors.toList());
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(MEDIUM_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(1000); // Less than 1 second
        
        // Performance metrics
        double elementsPerSecond = (double) MEDIUM_DATASET_SIZE / TimeUnit.NANOSECONDS.toSeconds(processingTime);
        System.out.printf("Medium Dataset Performance: %d elements in %dms (%.2f elements/sec)%n",
            MEDIUM_DATASET_SIZE, TimeUnit.NANOSECONDS.toMillis(processingTime), elementsPerSecond);
    }

    @Test
    @DisplayName("Should process large datasets with parallel processing")
    void benchmarkLargeDatasetPerformance() {
        // Given
        List<String> data = generateTestData(LARGE_DATASET_SIZE);
        AdvancedStream<String> stream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.PARALLEL,
            AdvancedStreamProcessor.MemoryStrategy.BATCHED
        );

        // When
        long startTime = System.nanoTime();
        List<String> result = stream.mapWithMonitoring(String::toUpperCase, "largeDataset")
                                   .stream()
                                   .collect(Collectors.toList());
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(LARGE_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(5000); // Less than 5 seconds
        
        // Performance metrics
        double elementsPerSecond = (double) LARGE_DATASET_SIZE / TimeUnit.NANOSECONDS.toSeconds(processingTime);
        System.out.printf("Large Dataset Performance: %d elements in %dms (%.2f elements/sec)%n",
            LARGE_DATASET_SIZE, TimeUnit.NANOSECONDS.toMillis(processingTime), elementsPerSecond);
    }

    @Test
    @DisplayName("Should handle extra large datasets without memory issues")
    void benchmarkExtraLargeDatasetPerformance() {
        // Given
        List<String> data = generateTestData(EXTRA_LARGE_DATASET_SIZE);
        AdvancedStream<String> stream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.PARALLEL,
            AdvancedStreamProcessor.MemoryStrategy.STREAMING
        );

        // When
        long startTime = System.nanoTime();
        List<String> result = stream.mapMemoryEfficient(String::toUpperCase)
                                   .stream()
                                   .collect(Collectors.toList());
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(EXTRA_LARGE_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(30000); // Less than 30 seconds
        
        // Verify memory usage is reasonable
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsageRatio = (double) usedMemory / maxMemory;
        
        assertThat(memoryUsageRatio).isLessThan(0.8); // Should use less than 80% of max memory
        
        // Performance metrics
        double elementsPerSecond = (double) EXTRA_LARGE_DATASET_SIZE / TimeUnit.NANOSECONDS.toSeconds(processingTime);
        System.out.printf("Extra Large Dataset Performance: %d elements in %dms (%.2f elements/sec, %.2f%% memory usage)%n",
            EXTRA_LARGE_DATASET_SIZE, TimeUnit.NANOSECONDS.toMillis(processingTime), elementsPerSecond, memoryUsageRatio * 100);
    }

    // ========== PARALLEL VS SEQUENTIAL BENCHMARKS ==========

    @Test
    @DisplayName("Should demonstrate parallel processing advantage for large datasets")
    void benchmarkParallelVsSequentialPerformance() {
        // Given
        List<String> data = generateTestData(LARGE_DATASET_SIZE);
        Function<String, String> mapper = s -> {
            // Simulate some CPU-intensive work
            return s.toUpperCase() + "_PROCESSED";
        };

        // Sequential processing
        AdvancedStream<String> sequentialStream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.SEQUENTIAL,
            AdvancedStreamProcessor.MemoryStrategy.EAGER
        );

        long sequentialStart = System.nanoTime();
        List<String> sequentialResult = sequentialStream.mapWithMonitoring(mapper, "sequential")
                                                       .stream()
                                                       .collect(Collectors.toList());
        long sequentialTime = System.nanoTime() - sequentialStart;

        // Parallel processing
        AdvancedStream<String> parallelStream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.PARALLEL,
            AdvancedStreamProcessor.MemoryStrategy.BATCHED
        );

        long parallelStart = System.nanoTime();
        List<String> parallelResult = parallelStream.mapWithMonitoring(mapper, "parallel")
                                                   .stream()
                                                   .collect(Collectors.toList());
        long parallelTime = System.nanoTime() - parallelStart;

        // Then
        assertThat(sequentialResult).hasSize(LARGE_DATASET_SIZE);
        assertThat(parallelResult).hasSize(LARGE_DATASET_SIZE);
        
        // Parallel should be faster for large datasets
        double speedup = (double) sequentialTime / parallelTime;
        assertThat(speedup).isGreaterThan(1.0); // Parallel should be faster
        
        System.out.printf("Parallel vs Sequential Performance:%n");
        System.out.printf("  Sequential: %dms%n", TimeUnit.NANOSECONDS.toMillis(sequentialTime));
        System.out.printf("  Parallel: %dms%n", TimeUnit.NANOSECONDS.toMillis(parallelTime));
        System.out.printf("  Speedup: %.2fx%n", speedup);
    }

    // ========== CIRCUIT BREAKER PERFORMANCE BENCHMARKS ==========

    @Test
    @DisplayName("Should handle circuit breaker scenarios efficiently")
    void benchmarkCircuitBreakerPerformance() {
        // Given
        List<String> data = generateTestDataWithErrors(MEDIUM_DATASET_SIZE, 0.1); // 10% error rate
        AdvancedStream<String> stream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.SEQUENTIAL,
            AdvancedStreamProcessor.MemoryStrategy.EAGER
        );

        Function<String, String> mapper = s -> {
            if (s.contains("ERROR")) {
                throw new RuntimeException("Simulated error");
            }
            return s.toUpperCase();
        };
        String fallback = "FALLBACK";

        // When
        long startTime = System.nanoTime();
        List<String> result = stream.mapWithCircuitBreaker(mapper, fallback, 3)
                                   .stream()
                                   .collect(Collectors.toList());
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(MEDIUM_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(10000); // Less than 10 seconds
        
        // Verify fallback values were used
        long fallbackCount = result.stream().filter("FALLBACK"::equals).count();
        assertThat(fallbackCount).isGreaterThan(0);
        
        // Performance metrics
        double elementsPerSecond = (double) MEDIUM_DATASET_SIZE / TimeUnit.NANOSECONDS.toSeconds(processingTime);
        System.out.printf("Circuit Breaker Performance: %d elements in %dms (%.2f elements/sec, %d fallbacks)%n",
            MEDIUM_DATASET_SIZE, TimeUnit.NANOSECONDS.toMillis(processingTime), elementsPerSecond, fallbackCount);
    }

    // ========== BACKPRESSURE PERFORMANCE BENCHMARKS ==========

    @Test
    @DisplayName("Should handle backpressure scenarios efficiently")
    void benchmarkBackpressurePerformance() {
        // Given
        List<String> data = generateTestData(LARGE_DATASET_SIZE);
        AdvancedStream<String> stream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.SEQUENTIAL,
            AdvancedStreamProcessor.MemoryStrategy.EAGER
        );

        Function<String, String> mapper = s -> s.toUpperCase();
        int bufferSize = 1000;
        long rateLimit = 10000; // 10,000 elements per second

        // When
        long startTime = System.nanoTime();
        List<String> result = stream.mapWithBackpressure(mapper, bufferSize, rateLimit)
                                   .stream()
                                   .collect(Collectors.toList());
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(LARGE_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(15000); // Less than 15 seconds
        
        // Performance metrics
        double elementsPerSecond = (double) LARGE_DATASET_SIZE / TimeUnit.NANOSECONDS.toSeconds(processingTime);
        System.out.printf("Backpressure Performance: %d elements in %dms (%.2f elements/sec)%n",
            LARGE_DATASET_SIZE, TimeUnit.NANOSECONDS.toMillis(processingTime), elementsPerSecond);
    }

    // ========== MEMORY EFFICIENCY BENCHMARKS ==========

    @Test
    @DisplayName("Should maintain memory efficiency under load")
    void benchmarkMemoryEfficiencyPerformance() {
        // Given
        List<String> data = generateTestData(EXTRA_LARGE_DATASET_SIZE);
        AdvancedStream<String> stream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.PARALLEL,
            AdvancedStreamProcessor.MemoryStrategy.STREAMING
        );

        // When
        long memoryBefore = getMemoryUsage();
        long startTime = System.nanoTime();
        
        List<String> result = stream.mapMemoryEfficient(String::toUpperCase)
                                   .stream()
                                   .collect(Collectors.toList());
        
        long processingTime = System.nanoTime() - startTime;
        long memoryAfter = getMemoryUsage();
        long memoryDelta = memoryAfter - memoryBefore;

        // Then
        assertThat(result).hasSize(EXTRA_LARGE_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(30000); // Less than 30 seconds
        
        // Memory efficiency check
        double memoryPerElement = (double) memoryDelta / EXTRA_LARGE_DATASET_SIZE;
        assertThat(memoryPerElement).isLessThan(100); // Less than 100 bytes per element
        
        // Performance metrics
        double elementsPerSecond = (double) EXTRA_LARGE_DATASET_SIZE / TimeUnit.NANOSECONDS.toSeconds(processingTime);
        System.out.printf("Memory Efficiency Performance: %d elements in %dms (%.2f elements/sec, %.2f bytes/element)%n",
            EXTRA_LARGE_DATASET_SIZE, TimeUnit.NANOSECONDS.toMillis(processingTime), elementsPerSecond, memoryPerElement);
    }

    // ========== CONCURRENT PROCESSING BENCHMARKS ==========

    @Test
    @DisplayName("Should handle concurrent operations efficiently")
    void benchmarkConcurrentProcessingPerformance() {
        // Given
        List<String> data = generateTestData(MEDIUM_DATASET_SIZE);
        AdvancedStream<String> stream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.PARALLEL,
            AdvancedStreamProcessor.MemoryStrategy.BATCHED
        );

        // When - Run multiple concurrent operations
        List<CompletableFuture<List<String>>> futures = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            final int operationId = i;
            CompletableFuture<List<String>> future = stream.mapWithMonitoring(
                s -> s.toUpperCase() + "_OP" + operationId, "concurrentOp" + operationId)
                .collectAsync(Collectors.toList(), "concurrentCollection" + operationId);
            futures.add(future);
        }

        long startTime = System.nanoTime();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(futures).hasSize(10);
        futures.forEach(future -> {
            List<String> result = future.join();
            assertThat(result).hasSize(MEDIUM_DATASET_SIZE);
        });
        
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(10000); // Less than 10 seconds
        
        // Performance metrics
        double operationsPerSecond = 10.0 / TimeUnit.NANOSECONDS.toSeconds(processingTime);
        System.out.printf("Concurrent Processing Performance: 10 operations in %dms (%.2f operations/sec)%n",
            TimeUnit.NANOSECONDS.toMillis(processingTime), operationsPerSecond);
    }

    // ========== STRESS TEST BENCHMARKS ==========

    @Test
    @DisplayName("Should handle massive datasets without failure")
    void benchmarkMassiveDatasetPerformance() {
        // Given
        List<String> data = generateTestData(MASSIVE_DATASET_SIZE);
        AdvancedStream<String> stream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.PARALLEL,
            AdvancedStreamProcessor.MemoryStrategy.STREAMING
        );

        // When
        long startTime = System.nanoTime();
        List<String> result = stream.mapMemoryEfficient(String::toUpperCase)
                                   .stream()
                                   .collect(Collectors.toList());
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(MASSIVE_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(300000); // Less than 5 minutes
        
        // Performance metrics
        double elementsPerSecond = (double) MASSIVE_DATASET_SIZE / TimeUnit.NANOSECONDS.toSeconds(processingTime);
        System.out.printf("Massive Dataset Performance: %d elements in %dms (%.2f elements/sec)%n",
            MASSIVE_DATASET_SIZE, TimeUnit.NANOSECONDS.toMillis(processingTime), elementsPerSecond);
    }

    @Test
    @DisplayName("Should maintain consistent performance across multiple operations")
    void benchmarkConsistentPerformance() {
        // Given
        List<String> data = generateTestData(SMALL_DATASET_SIZE);
        AdvancedStream<String> stream = AdvancedStreamProcessor.createAdvancedStream(
            data,
            AdvancedStreamProcessor.ProcessingStrategy.SEQUENTIAL,
            AdvancedStreamProcessor.MemoryStrategy.EAGER
        );

        List<Long> processingTimes = new ArrayList<>();

        // When - Run multiple operations
        for (int i = 0; i < 20; i++) {
            long startTime = System.nanoTime();
            stream.mapWithMonitoring(String::toUpperCase, "consistentTest" + i)
                  .stream()
                  .collect(Collectors.toList());
            long processingTime = System.nanoTime() - startTime;
            processingTimes.add(processingTime);
        }

        // Then
        assertThat(processingTimes).hasSize(20);
        
        // Calculate statistics
        long minTime = processingTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxTime = processingTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        double avgTime = processingTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        
        // Performance should be consistent (max should not be more than 3x min)
        assertThat(maxTime).isLessThan(minTime * 3);
        
        System.out.printf("Consistent Performance Test:%n");
        System.out.printf("  Min: %dms%n", TimeUnit.NANOSECONDS.toMillis(minTime));
        System.out.printf("  Max: %dms%n", TimeUnit.NANOSECONDS.toMillis(maxTime));
        System.out.printf("  Avg: %.2fms%n", TimeUnit.NANOSECONDS.toMillis((long) avgTime));
        System.out.printf("  Consistency Ratio: %.2f%n", (double) maxTime / minTime);
    }

    // ========== HELPER METHODS ==========

    /**
     * Generates test data of specified size.
     * 
     * @param size the number of elements to generate
     * @return a list of test strings
     */
    private List<String> generateTestData(int size) {
        return new ArrayList<String>() {{
            for (int i = 0; i < size; i++) {
                add("test_item_" + i);
            }
        }};
    }

    /**
     * Generates test data with a specified error rate.
     * 
     * @param size the number of elements to generate
     * @param errorRate the fraction of elements that should cause errors (0.0 to 1.0)
     * @return a list of test strings with some causing errors
     */
    private List<String> generateTestDataWithErrors(int size, double errorRate) {
        Random random = new Random(42); // Fixed seed for reproducible tests
        return new ArrayList<String>() {{
            for (int i = 0; i < size; i++) {
                if (random.nextDouble() < errorRate) {
                    add("ERROR_item_" + i);
                } else {
                    add("test_item_" + i);
                }
            }
        }};
    }

    /**
     * Gets current memory usage.
     * 
     * @return memory usage in bytes
     */
    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
