package com.algorithmpractice.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility class for performance testing and benchmarking algorithms.
 * 
 * <p>This class provides methods to measure execution time, memory usage,
 * and other performance metrics for algorithm implementations.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class PerformanceUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceUtils.class);
    private static final int PROGRESS_LOG_INTERVAL = 10;

    private PerformanceUtils() {
        // Utility class - prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Measures the execution time of a supplier operation.
     * 
     * @param <T>      the type of the result
     * @param name     the name of the operation for logging
     * @param supplier the operation to measure
     * @return the result of the operation
     */
    public static <T> T measureExecutionTime(final String name, final Supplier<T> supplier) {
        final long startTime = System.nanoTime();
        final long startMemory = getMemoryUsage();
        
        try {
            final T result = supplier.get();
            final long endTime = System.nanoTime();
            final long endMemory = getMemoryUsage();
            
            logPerformanceMetrics(name, startTime, endTime, startMemory, endMemory);
            return result;
        } catch (final Exception e) {
            final long endTime = System.nanoTime();
            LOGGER.error("❌ Operation '{}' failed after {} ms", name, 
                TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
            throw e;
        }
    }

    /**
     * Measures the execution time of a runnable operation.
     * 
     * @param name     the name of the operation for logging
     * @param runnable the operation to measure
     */
    public static void measureExecutionTime(final String name, final Runnable runnable) {
        final long startTime = System.nanoTime();
        final long startMemory = getMemoryUsage();
        
        try {
            runnable.run();
            final long endTime = System.nanoTime();
            final long endMemory = getMemoryUsage();
            
            logPerformanceMetrics(name, startTime, endTime, startMemory, endMemory);
        } catch (final Exception e) {
            final long endTime = System.nanoTime();
            LOGGER.error("❌ Operation '{}' failed after {} ms", name, 
                TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
            throw e;
        }
    }

    /**
     * Runs a performance test with multiple iterations and provides statistics.
     * 
     * @param name        the name of the test
     * @param iterations  the number of iterations
     * @param runnable    the operation to test
     * @return performance statistics
     */
    public static PerformanceStats runPerformanceTest(final String name, final int iterations, final Runnable runnable) {
        LOGGER.info("🚀 Starting performance test: {} ({} iterations)", name, iterations);
        
        final long[] executionTimes = new long[iterations];
        final long[] memoryUsage = new long[iterations];
        
        for (int i = 0; i < iterations; i++) {
            final long startTime = System.nanoTime();
            final long startMemory = getMemoryUsage();
            
            runnable.run();
            
            final long endTime = System.nanoTime();
            final long endMemory = getMemoryUsage();
            
            executionTimes[i] = endTime - startTime;
            memoryUsage[i] = endMemory - startMemory;
            
            if ((i + 1) % PROGRESS_LOG_INTERVAL == 0) {
                LOGGER.debug("Completed {}/{} iterations", i + 1, iterations);
            }
        }
        
        final PerformanceStats stats = new PerformanceStats(name, executionTimes, memoryUsage);
        logPerformanceStats(stats);
        return stats;
    }

    /**
     * Gets the current memory usage in bytes.
     * 
     * @return memory usage in bytes
     */
    private static long getMemoryUsage() {
        final Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Logs performance metrics for a single operation.
     * 
     * @param name        the operation name
     * @param startTime   the start time in nanoseconds
     * @param endTime     the end time in nanoseconds
     * @param startMemory the start memory usage in bytes
     * @param endMemory   the end memory usage in bytes
     */
    private static void logPerformanceMetrics(final String name, final long startTime, final long endTime,
                                           final long startMemory, final long endMemory) {
        final long executionTimeMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        final long memoryDelta = endMemory - startMemory;
        
        LOGGER.info("⏱️  {} completed in {} ms, memory delta: {} bytes", 
            name, executionTimeMs, memoryDelta);
    }

    /**
     * Logs performance statistics for a test run.
     * 
     * @param stats the performance statistics
     */
    private static void logPerformanceStats(final PerformanceStats stats) {
        LOGGER.info("📊 Performance Test Results for: {}", stats.getName());
        LOGGER.info("   Iterations: {}", stats.getIterations());
        LOGGER.info("   Average Execution Time: {:.2f} ms", stats.getAverageExecutionTimeMs());
        LOGGER.info("   Min Execution Time: {:.2f} ms", stats.getMinExecutionTimeMs());
        LOGGER.info("   Max Execution Time: {:.2f} ms", stats.getMaxExecutionTimeMs());
        LOGGER.info("   Standard Deviation: {:.2f} ms", stats.getStandardDeviationMs());
        LOGGER.info("   Average Memory Usage: {} bytes", stats.getAverageMemoryUsage());
    }

    /**
     * Performance statistics for a test run.
     */
    public static final class PerformanceStats {
        private final String name;
        private final int iterations;
        private final double averageExecutionTimeMs;
        private final double minExecutionTimeMs;
        private final double maxExecutionTimeMs;
        private final double standardDeviationMs;
        private final long averageMemoryUsage;

        public PerformanceStats(final String name, final long[] executionTimes, final long[] memoryUsage) {
            this.name = name;
            this.iterations = executionTimes.length;
            this.averageExecutionTimeMs = calculateAverageExecutionTime(executionTimes);
            this.minExecutionTimeMs = calculateMinExecutionTime(executionTimes);
            this.maxExecutionTimeMs = calculateMaxExecutionTime(executionTimes);
            this.standardDeviationMs = calculateStandardDeviation(executionTimes, averageExecutionTimeMs);
            this.averageMemoryUsage = calculateAverageMemoryUsage(memoryUsage);
        }

        private double calculateAverageExecutionTime(final long[] executionTimes) {
            long total = 0;
            for (final long time : executionTimes) {
                total += time;
            }
            return TimeUnit.NANOSECONDS.toMillis(total) / (double) executionTimes.length;
        }

        private double calculateMinExecutionTime(final long[] executionTimes) {
            long min = Long.MAX_VALUE;
            for (final long time : executionTimes) {
                if (time < min) {
                    min = time;
                }
            }
            return TimeUnit.NANOSECONDS.toMillis(min);
        }

        private double calculateMaxExecutionTime(final long[] executionTimes) {
            long max = Long.MIN_VALUE;
            for (final long time : executionTimes) {
                if (time > max) {
                    max = time;
                }
            }
            return TimeUnit.NANOSECONDS.toMillis(max);
        }

        private double calculateStandardDeviation(final long[] executionTimes, final double mean) {
            double sumSquaredDiff = 0;
            for (final long time : executionTimes) {
                final double diff = TimeUnit.NANOSECONDS.toMillis(time) - mean;
                sumSquaredDiff += diff * diff;
            }
            return Math.sqrt(sumSquaredDiff / executionTimes.length);
        }

        private long calculateAverageMemoryUsage(final long[] memoryUsage) {
            long total = 0;
            for (final long memory : memoryUsage) {
                total += memory;
            }
            return total / memoryUsage.length;
        }

        // Getters
        public String getName() { 
            return name; 
        }
        
        public int getIterations() { 
            return iterations; 
        }
        
        public double getAverageExecutionTimeMs() { 
            return averageExecutionTimeMs; 
        }
        
        public double getMinExecutionTimeMs() { 
            return minExecutionTimeMs; 
        }
        
        public double getMaxExecutionTimeMs() { 
            return maxExecutionTimeMs; 
        }
        
        public double getStandardDeviationMs() { 
            return standardDeviationMs; 
        }
        
        public long getAverageMemoryUsage() { 
            return averageMemoryUsage; 
        }
    }
}
