package netflix.streams.essentials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Performance testing suite for Java Streams Essentials
 * 
 * <p>This test suite validates the performance characteristics of the Java Streams Essentials
 * module under various load conditions and data sizes. It ensures that the implementation
 * meets Netflix's performance requirements for production systems.</p>
 * 
 * <p><strong>Performance Criteria:</strong></p>
 * <ul>
 *   <li>Memory efficiency for large datasets</li>
 *   <li>CPU utilization optimization</li>
 *   <li>Parallel processing effectiveness</li>
 *   <li>Error handling performance impact</li>
 *   <li>Scalability under increasing load</li>
 * </ul>
 * 
 * @author Netflix Backend Engineering Team
 * @version 1.0.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Java Streams Essentials Performance Tests")
class JavaStreamsEssentialsPerformanceTest {

    private static final int SMALL_DATASET_SIZE = 1000;
    private static final int MEDIUM_DATASET_SIZE = 10000;
    private static final int LARGE_DATASET_SIZE = 100000;
    private static final int EXTRA_LARGE_DATASET_SIZE = 1000000;

    @BeforeEach
    void setUp() {
        JavaStreamsEssentials.resetMetrics();
    }

    @Test
    @DisplayName("Should process small datasets efficiently")
    void testSmallDatasetPerformance() {
        // Given
        List<String> data = generateTestData(SMALL_DATASET_SIZE);
        Function<String, String> mapper = s -> s.toUpperCase();
        String fallback = "ERROR";

        // When
        long startTime = System.nanoTime();
        List<String> result = JavaStreamsEssentials.processWithErrorHandling(
            data.stream(), mapper, fallback);
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(SMALL_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(1000); // Less than 1 second
    }

    @Test
    @DisplayName("Should process medium datasets with good performance")
    void testMediumDatasetPerformance() {
        // Given
        List<String> data = generateTestData(MEDIUM_DATASET_SIZE);
        Function<String, String> mapper = s -> s.toUpperCase();
        String fallback = "ERROR";

        // When
        long startTime = System.nanoTime();
        List<String> result = JavaStreamsEssentials.processWithErrorHandling(
            data.stream(), mapper, fallback);
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(MEDIUM_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(5000); // Less than 5 seconds
    }

    @Test
    @DisplayName("Should process large datasets with parallel processing")
    void testLargeDatasetPerformance() {
        // Given
        List<String> data = generateTestData(LARGE_DATASET_SIZE);
        int batchSize = 1000;
        Function<List<String>, List<String>> batchProcessor = batch -> 
            batch.stream().map(String::toUpperCase).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // When
        long startTime = System.nanoTime();
        List<String> result = JavaStreamsEssentials.parallelProcessWithMonitoring(
            data, batchSize, batchProcessor);
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(LARGE_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(10000); // Less than 10 seconds
    }

    @Test
    @DisplayName("Should handle extra large datasets without memory issues")
    void testExtraLargeDatasetPerformance() {
        // Given
        List<String> data = generateTestData(EXTRA_LARGE_DATASET_SIZE);
        int batchSize = 10000;
        Function<List<String>, List<String>> batchProcessor = batch -> 
            batch.stream().map(String::toUpperCase).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // When
        long startTime = System.nanoTime();
        List<String> result = JavaStreamsEssentials.parallelProcessWithMonitoring(
            data, batchSize, batchProcessor);
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
    }

    @Test
    @DisplayName("Should demonstrate parallel processing advantage")
    void testParallelVsSequentialPerformance() {
        // Given
        List<String> data = generateTestData(LARGE_DATASET_SIZE);
        Function<String, String> mapper = s -> {
            // Simulate some CPU-intensive work
            return s.toUpperCase() + "_PROCESSED";
        };
        String fallback = "ERROR";

        // Sequential processing
        long sequentialStart = System.nanoTime();
        List<String> sequentialResult = JavaStreamsEssentials.processWithErrorHandling(
            data.stream(), mapper, fallback);
        long sequentialTime = System.nanoTime() - sequentialStart;

        // Parallel processing
        int batchSize = 1000;
        Function<List<String>, List<String>> batchProcessor = batch -> 
            batch.stream().map(mapper).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        long parallelStart = System.nanoTime();
        List<String> parallelResult = JavaStreamsEssentials.parallelProcessWithMonitoring(
            data, batchSize, batchProcessor);
        long parallelTime = System.nanoTime() - parallelStart;

        // Then
        assertThat(sequentialResult).hasSize(LARGE_DATASET_SIZE);
        assertThat(parallelResult).hasSize(LARGE_DATASET_SIZE);
        
        // Parallel should be faster for large datasets
        double speedup = (double) sequentialTime / parallelTime;
        assertThat(speedup).isGreaterThan(1.0); // Parallel should be faster
        
        System.out.printf("Sequential: %dms, Parallel: %dms, Speedup: %.2fx%n",
            TimeUnit.NANOSECONDS.toMillis(sequentialTime),
            TimeUnit.NANOSECONDS.toMillis(parallelTime),
            speedup);
    }

    @Test
    @DisplayName("Should handle error scenarios without significant performance impact")
    void testErrorHandlingPerformance() {
        // Given
        List<String> data = generateTestDataWithErrors(MEDIUM_DATASET_SIZE, 0.1); // 10% error rate
        Function<String, String> mapper = s -> {
            if (s.contains("ERROR")) {
                throw new RuntimeException("Simulated error");
            }
            return s.toUpperCase();
        };
        String fallback = "FALLBACK";

        // When
        long startTime = System.nanoTime();
        List<String> result = JavaStreamsEssentials.processWithErrorHandling(
            data.stream(), mapper, fallback);
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(MEDIUM_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(10000); // Less than 10 seconds
        
        // Verify fallback values were used
        long fallbackCount = result.stream().filter("FALLBACK"::equals).count();
        assertThat(fallbackCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should maintain consistent performance across multiple operations")
    void testConsistentPerformance() {
        // Given
        List<String> data = generateTestData(SMALL_DATASET_SIZE);
        Function<String, String> mapper = String::toUpperCase;
        String fallback = "ERROR";

        List<Long> processingTimes = new ArrayList<>();

        // When - Run multiple operations
        for (int i = 0; i < 10; i++) {
            long startTime = System.nanoTime();
            JavaStreamsEssentials.processWithErrorHandling(data.stream(), mapper, fallback);
            long processingTime = System.nanoTime() - startTime;
            processingTimes.add(processingTime);
        }

        // Then
        assertThat(processingTimes).hasSize(10);
        
        // Calculate statistics
        long minTime = processingTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxTime = processingTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        double avgTime = processingTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        
        // Performance should be consistent (max should not be more than 2x min)
        assertThat(maxTime).isLessThan(minTime * 2);
        
        System.out.printf("Performance consistency - Min: %dms, Max: %dms, Avg: %.2fms%n",
            TimeUnit.NANOSECONDS.toMillis(minTime),
            TimeUnit.NANOSECONDS.toMillis(maxTime),
            TimeUnit.NANOSECONDS.toMillis((long) avgTime));
    }

    @Test
    @DisplayName("Should handle memory pressure gracefully")
    void testMemoryPressureHandling() {
        // Given
        List<String> data = generateTestData(EXTRA_LARGE_DATASET_SIZE);
        int batchSize = 5000; // Smaller batches to reduce memory pressure
        Function<List<String>, List<String>> batchProcessor = batch -> 
            batch.stream().map(String::toUpperCase).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // When
        long startTime = System.nanoTime();
        List<String> result = JavaStreamsEssentials.parallelProcessWithMonitoring(
            data, batchSize, batchProcessor);
        long processingTime = System.nanoTime() - startTime;

        // Then
        assertThat(result).hasSize(EXTRA_LARGE_DATASET_SIZE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(processingTime)).isLessThan(60000); // Less than 1 minute
        
        // Force garbage collection and check memory
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsageRatio = (double) usedMemory / maxMemory;
        
        assertThat(memoryUsageRatio).isLessThan(0.9); // Should use less than 90% of max memory
    }

    // ========== HELPER METHODS ==========

    /**
     * Generates test data of specified size.
     * 
     * @param size the number of elements to generate
     * @return a list of test strings
     */
    private List<String> generateTestData(int size) {
        return IntStream.range(0, size)
            .mapToObj(i -> "test_item_" + i)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
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
        return IntStream.range(0, size)
            .mapToObj(i -> {
                if (random.nextDouble() < errorRate) {
                    return "ERROR_item_" + i;
                }
                return "test_item_" + i;
            })
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
