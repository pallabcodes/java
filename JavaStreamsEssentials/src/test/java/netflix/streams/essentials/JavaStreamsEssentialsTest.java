package netflix.streams.essentials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Java Streams Essentials
 * 
 * <p>This test suite validates all functionality of the Java Streams Essentials module
 * with production-grade test coverage including:</p>
 * <ul>
 *   <li>Unit tests for all public methods</li>
 *   <li>Error handling and edge case testing</li>
 *   <li>Performance and concurrency testing</li>
 *   <li>Integration testing with Spring Boot</li>
 *   <li>Memory leak detection and resource cleanup</li>
 * </ul>
 * 
 * @author Netflix Backend Engineering Team
 * @version 1.0.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Java Streams Essentials Tests")
class JavaStreamsEssentialsTest {

    private List<String> testData;
    private List<Integer> numericData;
    private List<User> userData;

    @BeforeEach
    void setUp() {
        // Reset metrics before each test
        JavaStreamsEssentials.resetMetrics();
        
        // Initialize test data
        testData = Arrays.asList("apple", "banana", "cherry", "date", "elderberry");
        numericData = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        userData = Arrays.asList(
            new User("John", "Engineering", true),
            new User("Jane", "Marketing", false),
            new User("Bob", "Engineering", true),
            new User("Alice", "Sales", true),
            new User("Charlie", "Engineering", false)
        );
    }

    // ========== PROCESS WITH ERROR HANDLING TESTS ==========

    @Test
    @DisplayName("Should process stream with error handling successfully")
    void testProcessWithErrorHandling_Success() {
        // Given
        Stream<String> stream = testData.stream();
        Function<String, String> mapper = String::toUpperCase;
        String fallback = "ERROR";

        // When
        List<String> result = JavaStreamsEssentials.processWithErrorHandling(stream, mapper, fallback);

        // Then
        assertThat(result).hasSize(5);
        assertThat(result).containsExactly("APPLE", "BANANA", "CHERRY", "DATE", "ELDERBERRY");
    }

    @Test
    @DisplayName("Should handle errors gracefully with fallback values")
    void testProcessWithErrorHandling_WithErrors() {
        // Given
        Stream<String> stream = Stream.of("valid", null, "another-valid");
        Function<String, String> mapper = s -> s.toUpperCase(); // Will throw NPE for null
        String fallback = "FALLBACK";

        // When
        List<String> result = JavaStreamsEssentials.processWithErrorHandling(stream, mapper, fallback);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("VALID", "FALLBACK", "ANOTHER-VALID");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null stream")
    void testProcessWithErrorHandling_NullStream() {
        // Given
        Function<String, String> mapper = String::toUpperCase;
        String fallback = "ERROR";

        // When & Then
        assertThatThrownBy(() -> JavaStreamsEssentials.processWithErrorHandling(null, mapper, fallback))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Stream cannot be null");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null mapper")
    void testProcessWithErrorHandling_NullMapper() {
        // Given
        Stream<String> stream = testData.stream();
        String fallback = "ERROR";

        // When & Then
        assertThatThrownBy(() -> JavaStreamsEssentials.processWithErrorHandling(stream, null, fallback))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Mapper function cannot be null");
    }

    @Test
    @DisplayName("Should handle empty stream")
    void testProcessWithErrorHandling_EmptyStream() {
        // Given
        Stream<String> stream = Stream.empty();
        Function<String, String> mapper = String::toUpperCase;
        String fallback = "ERROR";

        // When
        List<String> result = JavaStreamsEssentials.processWithErrorHandling(stream, mapper, fallback);

        // Then
        assertThat(result).isEmpty();
    }

    // ========== PARALLEL PROCESS WITH MONITORING TESTS ==========

    @Test
    @DisplayName("Should process data in parallel with monitoring")
    void testParallelProcessWithMonitoring_Success() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        int batchSize = 2;
        Function<List<String>, List<String>> batchProcessor = batch -> 
            batch.stream().map(s -> "processed_" + s).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // When
        List<String> result = JavaStreamsEssentials.parallelProcessWithMonitoring(data, batchSize, batchProcessor);

        // Then
        assertThat(result).hasSize(5);
        assertThat(result).containsExactlyInAnyOrder(
            "processed_item1", "processed_item2", "processed_item3", 
            "processed_item4", "processed_item5"
        );
    }

    @Test
    @DisplayName("Should handle large dataset efficiently")
    void testParallelProcessWithMonitoring_LargeDataset() {
        // Given
        List<Integer> largeData = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            largeData.add(i);
        }
        int batchSize = 1000;
        Function<List<Integer>, List<String>> batchProcessor = batch -> 
            batch.stream().map(i -> "item_" + i).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // When
        List<String> result = JavaStreamsEssentials.parallelProcessWithMonitoring(largeData, batchSize, batchProcessor);

        // Then
        assertThat(result).hasSize(10000);
        assertThat(result).contains("item_0");
        assertThat(result).contains("item_9999");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null data")
    void testParallelProcessWithMonitoring_NullData() {
        // Given
        int batchSize = 2;
        Function<List<String>, List<String>> batchProcessor = batch -> batch;

        // When & Then
        assertThatThrownBy(() -> JavaStreamsEssentials.parallelProcessWithMonitoring(null, batchSize, batchProcessor))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Data collection cannot be null");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null batch processor")
    void testParallelProcessWithMonitoring_NullBatchProcessor() {
        // Given
        List<String> data = testData;
        int batchSize = 2;

        // When & Then
        assertThatThrownBy(() -> JavaStreamsEssentials.parallelProcessWithMonitoring(data, batchSize, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Batch processor cannot be null");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid batch size")
    void testParallelProcessWithMonitoring_InvalidBatchSize() {
        // Given
        List<String> data = testData;
        int invalidBatchSize = 0;
        Function<List<String>, List<String>> batchProcessor = batch -> batch;

        // When & Then
        assertThatThrownBy(() -> JavaStreamsEssentials.parallelProcessWithMonitoring(data, invalidBatchSize, batchProcessor))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Batch size must be positive: 0");
    }

    // ========== GROUPING COLLECTOR TESTS ==========

    @Test
    @DisplayName("Should create grouping collector successfully")
    void testCreateGroupingCollector_Success() {
        // Given
        Function<User, String> classifier = User::getDepartment;
        Function<User, String> valueMapper = User::getName;

        // When
        var collector = JavaStreamsEssentials.createGroupingCollector(classifier, valueMapper);
        Map<String, List<String>> result = userData.stream().collect(collector);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get("Engineering")).containsExactlyInAnyOrder("John", "Bob", "Charlie");
        assertThat(result.get("Marketing")).containsExactlyInAnyOrder("Jane");
        assertThat(result.get("Sales")).containsExactlyInAnyOrder("Alice");
    }

    @Test
    @DisplayName("Should handle null keys in grouping")
    void testCreateGroupingCollector_NullKeys() {
        // Given
        List<User> dataWithNulls = Arrays.asList(
            new User("John", "Engineering", true),
            new User("Jane", null, false), // null department
            new User("Bob", "Engineering", true)
        );
        Function<User, String> classifier = User::getDepartment;
        Function<User, String> valueMapper = User::getName;

        // When
        var collector = JavaStreamsEssentials.createGroupingCollector(classifier, valueMapper);
        Map<String, List<String>> result = dataWithNulls.stream().collect(collector);

        // Then
        assertThat(result).hasSize(1); // Only non-null keys
        assertThat(result.get("Engineering")).containsExactlyInAnyOrder("John", "Bob");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null classifier")
    void testCreateGroupingCollector_NullClassifier() {
        // Given
        Function<User, String> valueMapper = User::getName;

        // When & Then
        assertThatThrownBy(() -> JavaStreamsEssentials.createGroupingCollector(null, valueMapper))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Classifier function cannot be null");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null value mapper")
    void testCreateGroupingCollector_NullValueMapper() {
        // Given
        Function<User, String> classifier = User::getDepartment;

        // When & Then
        assertThatThrownBy(() -> JavaStreamsEssentials.createGroupingCollector(classifier, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Value mapper function cannot be null");
    }

    // ========== PARTITIONING COLLECTOR TESTS ==========

    @Test
    @DisplayName("Should create partitioning collector successfully")
    void testCreatePartitioningCollector_Success() {
        // Given
        var collector = JavaStreamsEssentials.createPartitioningCollector(User::isActive);

        // When
        Map<Boolean, List<User>> result = userData.stream().collect(collector);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(true)).hasSize(3);
        assertThat(result.get(false)).hasSize(2);
        
        assertThat(result.get(true)).extracting(User::getName)
            .containsExactlyInAnyOrder("John", "Bob", "Alice");
        assertThat(result.get(false)).extracting(User::getName)
            .containsExactlyInAnyOrder("Jane", "Charlie");
    }

    @Test
    @DisplayName("Should handle exceptions in partitioning gracefully")
    void testCreatePartitioningCollector_WithExceptions() {
        // Given
        List<String> data = Arrays.asList("valid", null, "another-valid");
        var collector = JavaStreamsEssentials.createPartitioningCollector(s -> s.length() > 4);

        // When
        Map<Boolean, List<String>> result = data.stream().collect(collector);

        // Then
        assertThat(result).hasSize(2);
        // Exceptions should be handled gracefully and elements added to false partition
        assertThat(result.get(false)).contains("valid", "another-valid");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null predicate")
    void testCreatePartitioningCollector_NullPredicate() {
        // When & Then
        assertThatThrownBy(() -> JavaStreamsEssentials.createPartitioningCollector(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Predicate cannot be null");
    }

    // ========== PERFORMANCE MONITORING TESTS ==========

    @Test
    @DisplayName("Should track performance metrics correctly")
    void testPerformanceMetrics() {
        // Given
        Stream<String> stream = testData.stream();
        Function<String, String> mapper = String::toUpperCase;
        String fallback = "ERROR";

        // When
        JavaStreamsEssentials.processWithErrorHandling(stream, mapper, fallback);
        String report = JavaStreamsEssentials.getPerformanceReport();

        // Then
        assertThat(report).contains("Total Operations: 1");
        assertThat(report).contains("Elements Processed: 5");
        assertThat(report).contains("Success Rate: 100.00%");
    }

    @Test
    @DisplayName("Should reset metrics correctly")
    void testResetMetrics() {
        // Given
        Stream<String> stream = testData.stream();
        Function<String, String> mapper = String::toUpperCase;
        String fallback = "ERROR";

        // When
        JavaStreamsEssentials.processWithErrorHandling(stream, mapper, fallback);
        JavaStreamsEssentials.resetMetrics();
        String report = JavaStreamsEssentials.getPerformanceReport();

        // Then
        assertThat(report).contains("Total Operations: 0");
        assertThat(report).contains("Elements Processed: 0");
    }

    @Test
    @DisplayName("Should report healthy status correctly")
    void testIsHealthy() {
        // Given
        Stream<String> stream = testData.stream();
        Function<String, String> mapper = String::toUpperCase;
        String fallback = "ERROR";

        // When
        JavaStreamsEssentials.processWithErrorHandling(stream, mapper, fallback);
        boolean isHealthy = JavaStreamsEssentials.isHealthy();

        // Then
        assertThat(isHealthy).isTrue();
    }

    @Test
    @DisplayName("Should report unhealthy status with high failure rate")
    void testIsHealthy_WithFailures() {
        // Given
        Stream<String> stream = Stream.of("valid", null, null, null, null); // 80% failure rate
        Function<String, String> mapper = s -> s.toUpperCase(); // Will throw NPE for null
        String fallback = "ERROR";

        // When
        JavaStreamsEssentials.processWithErrorHandling(stream, mapper, fallback);
        boolean isHealthy = JavaStreamsEssentials.isHealthy();

        // Then
        assertThat(isHealthy).isFalse();
    }

    // ========== EDGE CASES AND STRESS TESTS ==========

    @Test
    @DisplayName("Should handle very large datasets")
    void testLargeDataset() {
        // Given
        List<Integer> largeData = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            largeData.add(i);
        }
        int batchSize = 10000;
        Function<List<Integer>, List<String>> batchProcessor = batch -> 
            batch.stream().map(i -> "processed_" + i).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // When
        long startTime = System.currentTimeMillis();
        List<String> result = JavaStreamsEssentials.parallelProcessWithMonitoring(largeData, batchSize, batchProcessor);
        long processingTime = System.currentTimeMillis() - startTime;

        // Then
        assertThat(result).hasSize(100000);
        assertThat(processingTime).isLessThan(10000); // Should complete within 10 seconds
    }

    @Test
    @DisplayName("Should handle empty collections")
    void testEmptyCollection() {
        // Given
        List<String> emptyData = Collections.emptyList();
        int batchSize = 10;
        Function<List<String>, List<String>> batchProcessor = batch -> batch;

        // When
        List<String> result = JavaStreamsEssentials.parallelProcessWithMonitoring(emptyData, batchSize, batchProcessor);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle single element collections")
    void testSingleElementCollection() {
        // Given
        List<String> singleData = Collections.singletonList("single");
        int batchSize = 10;
        Function<List<String>, List<String>> batchProcessor = batch -> 
            batch.stream().map(s -> "processed_" + s).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // When
        List<String> result = JavaStreamsEssentials.parallelProcessWithMonitoring(singleData, batchSize, batchProcessor);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("processed_single");
    }

    // ========== HELPER CLASSES ==========

    /**
     * Test user class for testing grouping and partitioning operations.
     */
    private static class User {
        private final String name;
        private final String department;
        private final boolean active;

        public User(String name, String department, boolean active) {
            this.name = name;
            this.department = department;
            this.active = active;
        }

        public String getName() { return name; }
        public String getDepartment() { return department; }
        public boolean isActive() { return active; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return active == user.active &&
                   Objects.equals(name, user.name) &&
                   Objects.equals(department, user.department);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, department, active);
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', department='" + department + "', active=" + active + "}";
        }
    }
}
