/*
 * Copyright 2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.mathlib.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance metrics collector for mathematical operations.
 *
 * This class provides comprehensive performance monitoring including:
 * - Execution time tracking
 * - Memory usage monitoring
 * - Operation count statistics
 * - Error rate tracking
 * - Throughput measurements
 *
 * All metrics are thread-safe and suitable for production monitoring.
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class OperationMetrics {

    private static final Logger logger = LoggerFactory.getLogger(OperationMetrics.class);

    // Execution time metrics
    @JsonProperty("total_execution_time_ns")
    private final AtomicLong totalExecutionTimeNs = new AtomicLong(0);

    @JsonProperty("min_execution_time_ns")
    private final AtomicLong minExecutionTimeNs = new AtomicLong(Long.MAX_VALUE);

    @JsonProperty("max_execution_time_ns")
    private final AtomicLong maxExecutionTimeNs = new AtomicLong(0);

    @JsonProperty("execution_count")
    private final AtomicLong executionCount = new AtomicLong(0);

    // Memory usage metrics
    @JsonProperty("total_memory_used_bytes")
    private final AtomicLong totalMemoryUsedBytes = new AtomicLong(0);

    @JsonProperty("max_memory_used_bytes")
    private final AtomicLong maxMemoryUsedBytes = new AtomicLong(0);

    // Error tracking
    @JsonProperty("error_count")
    private final AtomicLong errorCount = new AtomicLong(0);

    @JsonProperty("last_error_timestamp")
    private volatile long lastErrorTimestamp = 0;

    // Throughput metrics
    @JsonProperty("start_time_ms")
    private final long startTimeMs = System.currentTimeMillis();

    @JsonProperty("last_execution_timestamp")
    private volatile long lastExecutionTimestamp = 0;

    // Operation metadata
    private final String operationName;
    private final String complexity;
    private final boolean threadSafe;

    /**
     * Constructor for operation metrics.
     *
     * @param operationName name of the operation being monitored
     * @param complexity complexity class of the operation
     * @param threadSafe whether the operation is thread-safe
     */
    public OperationMetrics(String operationName, String complexity, boolean threadSafe) {
        this.operationName = operationName;
        this.complexity = complexity;
        this.threadSafe = threadSafe;
    }

    /**
     * Record a successful operation execution.
     *
     * @param executionTimeNs execution time in nanoseconds
     * @param memoryUsedBytes memory used in bytes
     */
    public void recordSuccess(long executionTimeNs, long memoryUsedBytes) {
        // Update execution time metrics
        totalExecutionTimeNs.addAndGet(executionTimeNs);
        updateMinMaxExecutionTime(executionTimeNs);
        executionCount.incrementAndGet();
        lastExecutionTimestamp = System.currentTimeMillis();

        // Update memory metrics
        totalMemoryUsedBytes.addAndGet(memoryUsedBytes);
        updateMaxMemoryUsed(memoryUsedBytes);

        logger.debug("Operation '{}' completed successfully - Time: {} ns, Memory: {} bytes",
                    operationName, executionTimeNs, memoryUsedBytes);
    }

    /**
     * Record a failed operation execution.
     */
    public void recordError() {
        errorCount.incrementAndGet();
        lastErrorTimestamp = System.currentTimeMillis();

        logger.warn("Operation '{}' failed - Total errors: {}", operationName, errorCount.get());
    }

    /**
     * Get the average execution time in nanoseconds.
     *
     * @return average execution time
     */
    public double getAverageExecutionTimeNs() {
        long count = executionCount.get();
        return count > 0 ? (double) totalExecutionTimeNs.get() / count : 0.0;
    }

    /**
     * Get the minimum execution time in nanoseconds.
     *
     * @return minimum execution time
     */
    public long getMinExecutionTimeNs() {
        return minExecutionTimeNs.get();
    }

    /**
     * Get the maximum execution time in nanoseconds.
     *
     * @return maximum execution time
     */
    public long getMaxExecutionTimeNs() {
        return maxExecutionTimeNs.get();
    }

    /**
     * Get the total number of executions.
     *
     * @return execution count
     */
    public long getExecutionCount() {
        return executionCount.get();
    }

    /**
     * Get the operations per second rate.
     *
     * @return operations per second
     */
    public double getOperationsPerSecond() {
        long elapsedMs = System.currentTimeMillis() - startTimeMs;
        if (elapsedMs <= 0) return 0.0;

        return (double) executionCount.get() / (elapsedMs / 1000.0);
    }

    /**
     * Get the error rate as a percentage.
     *
     * @return error rate percentage
     */
    public double getErrorRate() {
        long total = executionCount.get() + errorCount.get();
        return total > 0 ? (double) errorCount.get() / total * 100.0 : 0.0;
    }

    /**
     * Get the average memory usage in bytes.
     *
     * @return average memory usage
     */
    public double getAverageMemoryUsedBytes() {
        long count = executionCount.get();
        return count > 0 ? (double) totalMemoryUsedBytes.get() / count : 0.0;
    }

    /**
     * Get the maximum memory usage in bytes.
     *
     * @return maximum memory usage
     */
    public long getMaxMemoryUsedBytes() {
        return maxMemoryUsedBytes.get();
    }

    /**
     * Get the operation name.
     *
     * @return operation name
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Get the complexity class.
     *
     * @return complexity
     */
    public String getComplexity() {
        return complexity;
    }

    /**
     * Check if the operation is thread-safe.
     *
     * @return true if thread-safe
     */
    public boolean isThreadSafe() {
        return threadSafe;
    }

    /**
     * Get the uptime in milliseconds.
     *
     * @return uptime
     */
    public long getUptimeMs() {
        return System.currentTimeMillis() - startTimeMs;
    }

    /**
     * Get the last execution timestamp.
     *
     * @return last execution timestamp
     */
    public long getLastExecutionTimestamp() {
        return lastExecutionTimestamp;
    }

    /**
     * Get the last error timestamp.
     *
     * @return last error timestamp
     */
    public long getLastErrorTimestamp() {
        return lastErrorTimestamp;
    }

    /**
     * Reset all metrics to initial state.
     */
    public void reset() {
        totalExecutionTimeNs.set(0);
        minExecutionTimeNs.set(Long.MAX_VALUE);
        maxExecutionTimeNs.set(0);
        executionCount.set(0);
        totalMemoryUsedBytes.set(0);
        maxMemoryUsedBytes.set(0);
        errorCount.set(0);
        lastErrorTimestamp = 0;
        lastExecutionTimestamp = 0;

        logger.info("Metrics reset for operation: {}", operationName);
    }

    /**
     * Get a comprehensive metrics report.
     *
     * @return formatted metrics report
     */
    public String getMetricsReport() {
        return String.format("""
            Netflix Math Library - Operation Metrics Report
            ===============================================
            Operation: %s
            Complexity: %s
            Thread-Safe: %s
            Uptime: %d ms

            Execution Metrics:
            - Total Executions: %d
            - Average Time: %.2f ns
            - Min Time: %d ns
            - Max Time: %d ns
            - Operations/sec: %.2f

            Memory Metrics:
            - Average Memory: %.2f bytes
            - Max Memory: %d bytes

            Error Metrics:
            - Error Count: %d
            - Error Rate: %.2f%%
            - Last Error: %d ms ago
            """,
            operationName,
            complexity,
            threadSafe,
            getUptimeMs(),
            executionCount.get(),
            getAverageExecutionTimeNs(),
            getMinExecutionTimeNs(),
            getMaxExecutionTimeNs(),
            getOperationsPerSecond(),
            getAverageMemoryUsedBytes(),
            getMaxMemoryUsedBytes(),
            errorCount.get(),
            getErrorRate(),
            lastErrorTimestamp > 0 ? (System.currentTimeMillis() - lastErrorTimestamp) : 0
        );
    }

    /**
     * Update minimum and maximum execution time atomically.
     */
    private void updateMinMaxExecutionTime(long executionTimeNs) {
        // Update minimum
        long currentMin;
        do {
            currentMin = minExecutionTimeNs.get();
            if (executionTimeNs >= currentMin) break;
        } while (!minExecutionTimeNs.compareAndSet(currentMin, executionTimeNs));

        // Update maximum
        long currentMax;
        do {
            currentMax = maxExecutionTimeNs.get();
            if (executionTimeNs <= currentMax) break;
        } while (!maxExecutionTimeNs.compareAndSet(currentMax, executionTimeNs));
    }

    /**
     * Update maximum memory usage atomically.
     */
    private void updateMaxMemoryUsed(long memoryUsedBytes) {
        long currentMax;
        do {
            currentMax = maxMemoryUsedBytes.get();
            if (memoryUsedBytes <= currentMax) break;
        } while (!maxMemoryUsedBytes.compareAndSet(currentMax, memoryUsedBytes));
    }
}

