package com.algorithmpractice.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Performance monitoring system for algorithm operations.
 * 
 * <p>This class demonstrates Netflix engineering standards for performance monitoring:</p>
 * <ul>
 *   <li><strong>Thread Safety</strong>: Concurrent data structures for multi-threaded environments</li>
 *   <li><strong>Performance Metrics</strong>: Comprehensive collection of timing and throughput data</li>
 *   <li><strong>Memory Efficiency</strong>: Atomic operations and efficient data structures</li>
 *   <li><strong>Production Readiness</strong>: Integration hooks for external monitoring systems</li>
 * </ul>
 * 
 * <p>Key Design Decisions:</p>
 * <ul>
 *   <li>Use of LongAdder for high-concurrency counting scenarios</li>
 *   <li>ConcurrentHashMap for thread-safe metric storage</li>
 *   <li>AtomicLong for individual metric updates</li>
 *   <li>Structured metric naming for easy aggregation</li>
 * </ul>
 * 
 * @author Netflix Backend Engineering Team
 * @version 1.0.0
 * @since 2024
 */
public final class PerformanceMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    // Performance thresholds for alerting
    private static final long SORTING_THRESHOLD_MS = 1000;
    private static final long SEARCHING_THRESHOLD_MS = 100;
    private static final long DATA_STRUCTURE_THRESHOLD_MS = 50;
    
    // Metric storage with thread-safe operations
    private final ConcurrentHashMap<String, LongAdder> operationCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalDurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> minDurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> maxDurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongAdder> errorCounts = new ConcurrentHashMap<>();
    
    // Session management
    private final ConcurrentHashMap<String, SessionMetrics> sessionMetrics = new ConcurrentHashMap<>();

    /**
     * Initializes a new monitoring session.
     * 
     * @param sessionId the unique session identifier
     */
    public void initializeSession(final String sessionId) {
        final SessionMetrics metrics = new SessionMetrics();
        sessionMetrics.put(sessionId, metrics);
        
        LOGGER.debug("Performance monitoring session initialized: {}", sessionId);
    }

    /**
     * Records sorting algorithm performance metrics.
     * 
     * @param arraySize the size of the array being sorted
     * @param durationMs the duration of the sorting operation in milliseconds
     */
    public void recordSortingPerformance(final int arraySize, final long durationMs) {
        final String metricKey = buildSortingMetricKey(arraySize);
        recordPerformanceMetric(metricKey, durationMs);
        
        // Check for performance anomalies
        if (durationMs > SORTING_THRESHOLD_MS) {
            LOGGER.warn("Sorting performance threshold exceeded: {}ms for array size {}", durationMs, arraySize);
        }
        
        // Record array size distribution
        recordArraySizeDistribution("sorting", arraySize);
    }

    /**
     * Records searching algorithm performance metrics.
     * 
     * @param arraySize the size of the array being searched
     * @param durationMs the duration of the search operation in milliseconds
     */
    public void recordSearchingPerformance(final int arraySize, final long durationMs) {
        final String metricKey = buildSearchingMetricKey(arraySize);
        recordPerformanceMetric(metricKey, durationMs);
        
        // Check for performance anomalies
        if (durationMs > SEARCHING_THRESHOLD_MS) {
            LOGGER.warn("Searching performance threshold exceeded: {}ms for array size {}", durationMs, arraySize);
        }
        
        // Record array size distribution
        recordArraySizeDistribution("searching", arraySize);
    }

    /**
     * Records data structure operation performance metrics.
     * 
     * @param arraySize the size of the data structure
     * @param addDurationMs the duration of add operations in milliseconds
     * @param getDurationMs the duration of get operations in milliseconds
     */
    public void recordDataStructurePerformance(final int arraySize, final long addDurationMs, final long getDurationMs) {
        // Record add operation performance
        final String addMetricKey = buildDataStructureMetricKey("add", arraySize);
        recordPerformanceMetric(addMetricKey, addDurationMs);
        
        // Record get operation performance
        final String getMetricKey = buildDataStructureMetricKey("get", arraySize);
        recordPerformanceMetric(getMetricKey, getDurationMs);
        
        // Check for performance anomalies
        if (addDurationMs > DATA_STRUCTURE_THRESHOLD_MS) {
            LOGGER.warn("Data structure add performance threshold exceeded: {}ms for size {}", addDurationMs, arraySize);
        }
        
        if (getDurationMs > DATA_STRUCTURE_THRESHOLD_MS) {
            LOGGER.warn("Data structure get performance threshold exceeded: {}ms for size {}", getDurationMs, arraySize);
        }
        
        // Record array size distribution
        recordArraySizeDistribution("data_structure", arraySize);
    }

    /**
     * Records a generic performance metric with comprehensive statistics.
     * 
     * @param metricKey the unique identifier for the metric
     * @param durationMs the duration in milliseconds
     */
    private void recordPerformanceMetric(final String metricKey, final long durationMs) {
        // Update operation count
        operationCounts.computeIfAbsent(metricKey, k -> new LongAdder()).increment();
        
        // Update total duration
        totalDurations.computeIfAbsent(metricKey, k -> new AtomicLong(0))
                     .addAndGet(durationMs);
        
        // Update minimum duration
        minDurations.computeIfAbsent(metricKey, k -> new AtomicLong(Long.MAX_VALUE))
                    .updateAndGet(current -> Math.min(current, durationMs));
        
        // Update maximum duration
        maxDurations.computeIfAbsent(metricKey, k -> new AtomicLong(Long.MIN_VALUE))
                    .updateAndGet(current -> Math.max(current, durationMs));
        
        LOGGER.debug("Performance metric recorded: {} = {}ms", metricKey, durationMs);
    }

    /**
     * Records array size distribution for analysis.
     * 
     * @param operationType the type of operation (sorting, searching, data_structure)
     * @param arraySize the size of the array
     */
    private void recordArraySizeDistribution(final String operationType, final int arraySize) {
        final String distributionKey = String.format("%s.array_size_distribution", operationType);
        
        // Categorize array sizes for meaningful analysis
        final String sizeCategory = categorizeArraySize(arraySize);
        final String categoryKey = String.format("%s.%s", distributionKey, sizeCategory);
        
        operationCounts.computeIfAbsent(categoryKey, k -> new LongAdder()).increment();
    }

    /**
     * Categorizes array sizes for meaningful performance analysis.
     * 
     * @param arraySize the size of the array
     * @return the size category string
     */
    private String categorizeArraySize(final int arraySize) {
        if (arraySize < 100) {
            return "small";
        } else if (arraySize < 10000) {
            return "medium";
        } else if (arraySize < 1000000) {
            return "large";
        } else {
            return "very_large";
        }
    }

    /**
     * Records an error occurrence for a specific metric.
     * 
     * @param metricKey the metric key where the error occurred
     */
    public void recordError(final String metricKey) {
        errorCounts.computeIfAbsent(metricKey, k -> new LongAdder()).increment();
        
        LOGGER.debug("Error recorded for metric: {}", metricKey);
    }

    /**
     * Retrieves comprehensive performance statistics for a metric.
     * 
     * @param metricKey the metric key to retrieve statistics for
     * @return PerformanceStatistics containing all relevant metrics
     */
    public PerformanceStatistics getPerformanceStatistics(final String metricKey) {
        final LongAdder count = operationCounts.get(metricKey);
        final AtomicLong total = totalDurations.get(metricKey);
        final AtomicLong min = minDurations.get(metricKey);
        final AtomicLong max = maxDurations.get(metricKey);
        final LongAdder errors = errorCounts.get(metricKey);
        
        if (count == null || count.sum() == 0) {
            return new PerformanceStatistics(metricKey, 0, 0, 0, 0, 0);
        }
        
        final long operationCount = count.sum();
        final long totalDuration = total != null ? total.get() : 0;
        final long minDuration = min != null ? min.get() : 0;
        final long maxDuration = max != null ? max.get() : 0;
        final long errorCount = errors != null ? errors.sum() : 0;
        
        return new PerformanceStatistics(metricKey, operationCount, totalDuration, minDuration, maxDuration, errorCount);
    }

    /**
     * Generates a comprehensive performance report for all metrics.
     * 
     * @return a formatted string containing all performance statistics
     */
    public String generatePerformanceReport() {
        final StringBuilder report = new StringBuilder();
        report.append("=== PERFORMANCE MONITORING REPORT ===\n");
        
        // Group metrics by operation type
        final ConcurrentHashMap<String, LongAdder> sortedMetrics = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, LongAdder> searchMetrics = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, LongAdder> dataStructureMetrics = new ConcurrentHashMap<>();
        
        operationCounts.forEach((key, value) -> {
            if (key.startsWith("sorting")) {
                sortedMetrics.put(key, value);
            } else if (key.startsWith("searching")) {
                searchMetrics.put(key, value);
            } else if (key.startsWith("data_structure")) {
                dataStructureMetrics.put(key, value);
            }
        });
        
        // Generate reports for each category
        report.append("\n--- SORTING ALGORITHMS ---\n");
        generateCategoryReport(report, sortedMetrics);
        
        report.append("\n--- SEARCHING ALGORITHMS ---\n");
        generateCategoryReport(report, searchMetrics);
        
        report.append("\n--- DATA STRUCTURES ---\n");
        generateCategoryReport(report, dataStructureMetrics);
        
        report.append("\n=== END REPORT ===\n");
        
        return report.toString();
    }

    /**
     * Generates a performance report for a specific category of metrics.
     * 
     * @param report the StringBuilder to append the report to
     * @param categoryMetrics the metrics for the specific category
     */
    private void generateCategoryReport(final StringBuilder report, final ConcurrentHashMap<String, LongAdder> categoryMetrics) {
        if (categoryMetrics.isEmpty()) {
            report.append("No metrics available\n");
            return;
        }
        
        categoryMetrics.forEach((key, count) -> {
            final PerformanceStatistics stats = getPerformanceStatistics(key);
            if (stats.getOperationCount() > 0) {
                report.append(String.format("%s: %d operations, avg: %.2fms, min: %dms, max: %dms, errors: %d\n",
                    key, stats.getOperationCount(), stats.getAverageDuration(), 
                    stats.getMinDuration(), stats.getMaxDuration(), stats.getErrorCount()));
            }
        });
    }

    /**
     * Finalizes a monitoring session and generates final statistics.
     * 
     * @param sessionId the session identifier to finalize
     */
    public void finalizeSession(final String sessionId) {
        final SessionMetrics metrics = sessionMetrics.get(sessionId);
        if (metrics != null) {
            metrics.setEndTime(System.currentTimeMillis());
            LOGGER.info("Session {} finalized. Duration: {}ms", sessionId, metrics.getDuration());
        }
        
        // Clean up session data
        sessionMetrics.remove(sessionId);
    }

    /**
     * Builds a metric key for sorting operations.
     * 
     * @param arraySize the size of the array
     * @return the metric key string
     */
    private String buildSortingMetricKey(final int arraySize) {
        return String.format("sorting.array_size_%d", arraySize);
    }

    /**
     * Builds a metric key for searching operations.
     * 
     * @param arraySize the size of the array
     * @return the metric key string
     */
    private String buildSearchingMetricKey(final int arraySize) {
        return String.format("searching.array_size_%d", arraySize);
    }

    /**
     * Builds a metric key for data structure operations.
     * 
     * @param operation the specific operation (add, get, etc.)
     * @param arraySize the size of the data structure
     * @return the metric key string
     */
    private String buildDataStructureMetricKey(final String operation, final int arraySize) {
        return String.format("data_structure.%s.size_%d", operation, arraySize);
    }

    // ========== INNER CLASSES FOR ORGANIZATION ==========

    /**
     * Container for performance statistics.
     * This demonstrates how to organize related data without oversimplification.
     */
    public static final class PerformanceStatistics {
        private final String metricKey;
        private final long operationCount;
        private final long totalDuration;
        private final long minDuration;
        private final long maxDuration;
        private final long errorCount;

        public PerformanceStatistics(final String metricKey, final long operationCount, 
                                  final long totalDuration, final long minDuration, 
                                  final long maxDuration, final long errorCount) {
            this.metricKey = metricKey;
            this.operationCount = operationCount;
            this.totalDuration = totalDuration;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.errorCount = errorCount;
        }

        // Getters
        public String getMetricKey() { return metricKey; }
        public long getOperationCount() { return operationCount; }
        public long getTotalDuration() { return totalDuration; }
        public long getMinDuration() { return minDuration; }
        public long getMaxDuration() { return maxDuration; }
        public long getErrorCount() { return errorCount; }
        
        /**
         * Calculates the average duration of operations.
         * 
         * @return the average duration in milliseconds
         */
        public double getAverageDuration() {
            return operationCount > 0 ? (double) totalDuration / operationCount : 0.0;
        }
        
        /**
         * Calculates the success rate of operations.
         * 
         * @return the success rate as a percentage
         */
        public double getSuccessRate() {
            return operationCount > 0 ? ((double) (operationCount - errorCount) / operationCount) * 100.0 : 0.0;
        }
    }

    /**
     * Container for session-specific metrics.
     */
    private static final class SessionMetrics {
        private final long startTime;
        private volatile long endTime;

        public SessionMetrics() {
            this.startTime = System.currentTimeMillis();
        }

        public void setEndTime(final long endTime) {
            this.endTime = endTime;
        }

        public long getDuration() {
            return endTime - startTime;
        }
    }
}
