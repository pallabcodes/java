package com.algorithmpractice.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Operation-level metrics collection system for application monitoring.
 * 
 * <p>This class demonstrates Netflix engineering standards for operation metrics:</p>
 * <ul>
 *   <li><strong>Granular Monitoring</strong>: Detailed tracking of individual operations</li>
 *   <li><strong>Session Management</strong>: Per-session metric isolation and aggregation</li>
 *   <li><strong>Performance Analysis</strong>: Comprehensive timing and throughput analysis</li>
 *   <li><strong>Error Tracking</strong>: Detailed error categorization and frequency analysis</li>
 * </ul>
 * 
 * <p>Key Design Decisions:</p>
 * <ul>
 *   <li>Session-based metric isolation for multi-tenant scenarios</li>
 *   <li>Atomic operations for thread-safe metric updates</li>
 *   <li>Structured metric naming for easy aggregation and analysis</li>
 *   <li>Performance-optimized data structures for high-throughput scenarios</li>
 * </ul>
 * 
 * @author Netflix Backend Engineering Team
 * @version 1.0.0
 * @since 2024
 */
public final class OperationMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationMetrics.class);
    
    // Performance thresholds for alerting
    private static final long DEMO_THRESHOLD_MS = 5000;
    private static final long APPLICATION_THRESHOLD_MS = 30000;
    
    // Session-based metric storage
    private final ConcurrentHashMap<String, SessionOperationMetrics> sessionMetrics = new ConcurrentHashMap<>();
    
    // Global aggregated metrics
    private final ConcurrentHashMap<String, LongAdder> globalOperationCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> globalTotalDurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> globalMinDurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> globalMaxDurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongAdder> globalErrorCounts = new ConcurrentHashMap<>();

    /**
     * Initializes a new monitoring session for operation metrics.
     * 
     * @param sessionId the unique session identifier
     */
    public void initializeSession(final String sessionId) {
        final SessionOperationMetrics metrics = new SessionOperationMetrics(sessionId);
        sessionMetrics.put(sessionId, metrics);
        
        LOGGER.debug("Operation metrics session initialized: {}", sessionId);
    }

    /**
     * Records successful demonstration completion with comprehensive metrics.
     * 
     * @param sessionId the session identifier
     * @param demoId the demonstration identifier
     * @param duration the duration of the demonstration in milliseconds
     */
    public void recordDemoSuccess(final String sessionId, final String demoId, final long duration) {
        // Update session-specific metrics
        final SessionOperationMetrics sessionMetrics = this.sessionMetrics.get(sessionId);
        if (sessionMetrics != null) {
            sessionMetrics.recordDemoSuccess(demoId, duration);
        }
        
        // Update global metrics
        updateGlobalMetrics("demo." + demoId, duration);
        
        // Check for performance anomalies
        if (duration > DEMO_THRESHOLD_MS) {
            LOGGER.warn("Demo performance threshold exceeded: {}ms for demo {} in session {}", 
                       duration, demoId, sessionId);
        }
        
        LOGGER.debug("Demo success recorded: session={}, demo={}, duration={}ms", sessionId, demoId, duration);
    }

    /**
     * Records failed demonstration completion with comprehensive metrics.
     * 
     * @param sessionId the session identifier
     * @param demoId the demonstration identifier
     * @param duration the duration until failure in milliseconds
     * @param error the error that occurred
     */
    public void recordDemoFailure(final String sessionId, final String demoId, final long duration, final Exception error) {
        // Update session-specific metrics
        final SessionOperationMetrics sessionMetrics = this.sessionMetrics.get(sessionId);
        if (sessionMetrics != null) {
            sessionMetrics.recordDemoFailure(demoId, duration, error);
        }
        
        // Update global metrics with error
        updateGlobalMetricsWithError("demo." + demoId, duration, error);
        
        LOGGER.debug("Demo failure recorded: session={}, demo={}, duration={}ms, error={}", 
                    sessionId, demoId, duration, error.getClass().getSimpleName());
    }

    /**
     * Records successful application completion with comprehensive metrics.
     * 
     * @param sessionId the session identifier
     * @param duration the duration of the application in milliseconds
     */
    public void recordApplicationSuccess(final String sessionId, final long duration) {
        // Update session-specific metrics
        final SessionOperationMetrics sessionMetrics = this.sessionMetrics.get(sessionId);
        if (sessionMetrics != null) {
            sessionMetrics.recordApplicationSuccess(duration);
        }
        
        // Update global metrics
        updateGlobalMetrics("application", duration);
        
        // Check for performance anomalies
        if (duration > APPLICATION_THRESHOLD_MS) {
            LOGGER.warn("Application performance threshold exceeded: {}ms for session {}", duration, sessionId);
        }
        
        LOGGER.debug("Application success recorded: session={}, duration={}ms", sessionId, duration);
    }

    /**
     * Records failed application completion with comprehensive metrics.
     * 
     * @param sessionId the session identifier
     * @param duration the duration until failure in milliseconds
     * @param error the error that occurred
     */
    public void recordApplicationFailure(final String sessionId, final long duration, final Exception error) {
        // Update session-specific metrics
        final SessionOperationMetrics sessionMetrics = this.sessionMetrics.get(sessionId);
        if (sessionMetrics != null) {
            sessionMetrics.recordApplicationFailure(duration, error);
        }
        
        // Update global metrics with error
        updateGlobalMetricsWithError("application", duration, error);
        
        LOGGER.debug("Application failure recorded: session={}, duration={}ms, error={}", 
                    sessionId, duration, error.getClass().getSimpleName());
    }

    /**
     * Updates global metrics for successful operations.
     * 
     * @param operationKey the operation identifier
     * @param duration the operation duration
     */
    private void updateGlobalMetrics(final String operationKey, final long duration) {
        // Update operation count
        globalOperationCounts.computeIfAbsent(operationKey, k -> new LongAdder()).increment();
        
        // Update total duration
        globalTotalDurations.computeIfAbsent(operationKey, k -> new AtomicLong(0))
                           .addAndGet(duration);
        
        // Update minimum duration
        globalMinDurations.computeIfAbsent(operationKey, k -> new AtomicLong(Long.MAX_VALUE))
                          .updateAndGet(current -> Math.min(current, duration));
        
        // Update maximum duration
        globalMaxDurations.computeIfAbsent(operationKey, k -> new AtomicLong(Long.MIN_VALUE))
                          .updateAndGet(current -> Math.max(current, duration));
    }

    /**
     * Updates global metrics for failed operations with error tracking.
     * 
     * @param operationKey the operation identifier
     * @param duration the operation duration until failure
     * @param error the error that occurred
     */
    private void updateGlobalMetricsWithError(final String operationKey, final long duration, final Exception error) {
        // Update operation count (including failures)
        globalOperationCounts.computeIfAbsent(operationKey, k -> new LongAdder()).increment();
        
        // Update total duration
        globalTotalDurations.computeIfAbsent(operationKey, k -> new AtomicLong(0))
                           .addAndGet(duration);
        
        // Update minimum duration
        globalMinDurations.computeIfAbsent(operationKey, k -> new AtomicLong(Long.MAX_VALUE))
                          .updateAndGet(current -> Math.min(current, duration));
        
        // Update maximum duration
        globalMaxDurations.computeIfAbsent(operationKey, k -> new AtomicLong(Long.MIN_VALUE))
                          .updateAndGet(current -> Math.max(current, duration));
        
        // Update error count
        final String errorKey = operationKey + ".errors." + error.getClass().getSimpleName();
        globalErrorCounts.computeIfAbsent(errorKey, k -> new LongAdder()).increment();
    }

    /**
     * Retrieves comprehensive operation statistics for a specific operation.
     * 
     * @param operationKey the operation identifier
     * @return OperationStatistics containing all relevant metrics
     */
    public OperationStatistics getOperationStatistics(final String operationKey) {
        final LongAdder count = globalOperationCounts.get(operationKey);
        final AtomicLong total = globalTotalDurations.get(operationKey);
        final AtomicLong min = globalMinDurations.get(operationKey);
        final AtomicLong max = globalMaxDurations.get(operationKey);
        
        if (count == null || count.sum() == 0) {
            return new OperationStatistics(operationKey, 0, 0, 0, 0, 0);
        }
        
        final long operationCount = count.sum();
        final long totalDuration = total != null ? total.get() : 0;
        final long minDuration = min != null ? min.get() : 0;
        final long maxDuration = max != null ? max.get() : 0;
        
        // Calculate error count for this operation
        long errorCount = 0;
        for (final String errorKey : globalErrorCounts.keySet()) {
            if (errorKey.startsWith(operationKey + ".errors.")) {
                final LongAdder errorCounter = globalErrorCounts.get(errorKey);
                if (errorCounter != null) {
                    errorCount += errorCounter.sum();
                }
            }
        }
        
        return new OperationStatistics(operationKey, operationCount, totalDuration, minDuration, maxDuration, errorCount);
    }

    /**
     * Retrieves session-specific operation statistics.
     * 
     * @param sessionId the session identifier
     * @return SessionOperationMetrics for the specified session
     */
    public SessionOperationMetrics getSessionMetrics(final String sessionId) {
        return sessionMetrics.get(sessionId);
    }

    /**
     * Generates a comprehensive operation metrics report.
     * 
     * @return a formatted string containing all operation statistics
     */
    public String generateOperationReport() {
        final StringBuilder report = new StringBuilder();
        report.append("=== OPERATION METRICS REPORT ===\n");
        
        // Global metrics summary
        report.append("\n--- GLOBAL METRICS ---\n");
        generateGlobalMetricsReport(report);
        
        // Session-specific metrics
        report.append("\n--- SESSION METRICS ---\n");
        generateSessionMetricsReport(report);
        
        // Error analysis
        report.append("\n--- ERROR ANALYSIS ---\n");
        generateErrorAnalysisReport(report);
        
        report.append("\n=== END REPORT ===\n");
        
        return report.toString();
    }

    /**
     * Generates a report for global operation metrics.
     * 
     * @param report the StringBuilder to append the report to
     */
    private void generateGlobalMetricsReport(final StringBuilder report) {
        if (globalOperationCounts.isEmpty()) {
            report.append("No global metrics available\n");
            return;
        }
        
        globalOperationCounts.forEach((key, count) -> {
            final OperationStatistics stats = getOperationStatistics(key);
            if (stats.getOperationCount() > 0) {
                report.append(String.format("%s: %d operations, avg: %.2fms, min: %dms, max: %dms, errors: %d\n",
                    key, stats.getOperationCount(), stats.getAverageDuration(), 
                    stats.getMinDuration(), stats.getMaxDuration(), stats.getErrorCount()));
            }
        });
    }

    /**
     * Generates a report for session-specific metrics.
     * 
     * @param report the StringBuilder to append the report to
     */
    private void generateSessionMetricsReport(final StringBuilder report) {
        if (sessionMetrics.isEmpty()) {
            report.append("No session metrics available\n");
            return;
        }
        
        sessionMetrics.forEach((sessionId, metrics) -> {
            report.append(String.format("Session %s: %s\n", sessionId, metrics.generateSessionReport()));
        });
    }

    /**
     * Generates a report for error analysis.
     * 
     * @param report the StringBuilder to append the report to
     */
    private void generateErrorAnalysisReport(final StringBuilder report) {
        if (globalErrorCounts.isEmpty()) {
            report.append("No errors recorded\n");
            return;
        }
        
        // Group errors by operation type
        final ConcurrentHashMap<String, LongAdder> operationErrors = new ConcurrentHashMap<>();
        
        globalErrorCounts.forEach((errorKey, count) -> {
            final String operationKey = errorKey.substring(0, errorKey.lastIndexOf(".errors."));
            operationErrors.computeIfAbsent(operationKey, k -> new LongAdder()).add(count.sum());
        });
        
        operationErrors.forEach((operation, errorCount) -> {
            report.append(String.format("%s: %d errors\n", operation, errorCount.sum()));
        });
    }

    /**
     * Finalizes a monitoring session and generates final statistics.
     * 
     * @param sessionId the session identifier to finalize
     */
    public void finalizeSession(final String sessionId) {
        final SessionOperationMetrics metrics = sessionMetrics.get(sessionId);
        if (metrics != null) {
            metrics.finalizeSession();
            LOGGER.info("Session {} operation metrics finalized. Duration: {}ms", sessionId, metrics.getSessionDuration());
        }
        
        // Clean up session data
        sessionMetrics.remove(sessionId);
    }

    // ========== INNER CLASSES FOR ORGANIZATION ==========

    /**
     * Container for operation statistics.
     * This demonstrates how to organize related data without oversimplification.
     */
    public static final class OperationStatistics {
        private final String operationKey;
        private final long operationCount;
        private final long totalDuration;
        private final long minDuration;
        private final long maxDuration;
        private final long errorCount;

        public OperationStatistics(final String operationKey, final long operationCount, 
                                final long totalDuration, final long minDuration, 
                                final long maxDuration, final long errorCount) {
            this.operationKey = operationKey;
            this.operationCount = operationCount;
            this.totalDuration = totalDuration;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.errorCount = errorCount;
        }

        // Getters
        public String getOperationKey() { return operationKey; }
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
     * Session-specific operation metrics container.
     */
    public static final class SessionOperationMetrics {
        private final String sessionId;
        private final long startTime;
        private volatile long endTime;
        
        // Session-specific metric storage
        private final ConcurrentHashMap<String, LongAdder> demoSuccessCounts = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, LongAdder> demoFailureCounts = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, AtomicLong> demoTotalDurations = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, AtomicLong> demoMinDurations = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, AtomicLong> demoMaxDurations = new ConcurrentHashMap<>();
        
        private final AtomicLong applicationSuccessCount = new AtomicLong(0);
        private final AtomicLong applicationFailureCount = new AtomicLong(0);
        private final AtomicLong applicationTotalDuration = new AtomicLong(0);
        private final AtomicLong applicationMinDuration = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong applicationMaxDuration = new AtomicLong(Long.MIN_VALUE);

        public SessionOperationMetrics(final String sessionId) {
            this.sessionId = sessionId;
            this.startTime = System.currentTimeMillis();
        }

        /**
         * Records successful demonstration completion.
         * 
         * @param demoId the demonstration identifier
         * @param duration the duration of the demonstration
         */
        public void recordDemoSuccess(final String demoId, final long duration) {
            demoSuccessCounts.computeIfAbsent(demoId, k -> new LongAdder()).increment();
            updateDemoDurationMetrics(demoId, duration);
        }

        /**
         * Records failed demonstration completion.
         * 
         * @param demoId the demonstration identifier
         * @param duration the duration until failure
         * @param error the error that occurred
         */
        public void recordDemoFailure(final String demoId, final long duration, final Exception error) {
            demoFailureCounts.computeIfAbsent(demoId, k -> new LongAdder()).increment();
            updateDemoDurationMetrics(demoId, duration);
        }

        /**
         * Records successful application completion.
         * 
         * @param duration the duration of the application
         */
        public void recordApplicationSuccess(final long duration) {
            applicationSuccessCount.incrementAndGet();
            updateApplicationDurationMetrics(duration);
        }

        /**
         * Records failed application completion.
         * 
         * @param duration the duration until failure
         * @param error the error that occurred
         */
        public void recordApplicationFailure(final long duration, final Exception error) {
            applicationFailureCount.incrementAndGet();
            updateApplicationDurationMetrics(duration);
        }

        /**
         * Updates demonstration duration metrics.
         * 
         * @param demoId the demonstration identifier
         * @param duration the duration to record
         */
        private void updateDemoDurationMetrics(final String demoId, final long duration) {
            demoTotalDurations.computeIfAbsent(demoId, k -> new AtomicLong(0)).addAndGet(duration);
            demoMinDurations.computeIfAbsent(demoId, k -> new AtomicLong(Long.MAX_VALUE))
                           .updateAndGet(current -> Math.min(current, duration));
            demoMaxDurations.computeIfAbsent(demoId, k -> new AtomicLong(Long.MIN_VALUE))
                           .updateAndGet(current -> Math.max(current, duration));
        }

        /**
         * Updates application duration metrics.
         * 
         * @param duration the duration to record
         */
        private void updateApplicationDurationMetrics(final long duration) {
            applicationTotalDuration.addAndGet(duration);
            applicationMinDuration.updateAndGet(current -> Math.min(current, duration));
            applicationMaxDuration.updateAndGet(current -> Math.max(current, duration));
        }

        /**
         * Finalizes the session and records end time.
         */
        public void finalizeSession() {
            this.endTime = System.currentTimeMillis();
        }

        /**
         * Gets the total session duration.
         * 
         * @return the session duration in milliseconds
         */
        public long getSessionDuration() {
            return endTime - startTime;
        }

        /**
         * Generates a session-specific metrics report.
         * 
         * @return a formatted string containing session metrics
         */
        public String generateSessionReport() {
            final StringBuilder report = new StringBuilder();
            report.append(String.format("Duration: %dms, ", getSessionDuration()));
            
            // Application metrics
            final long appSuccess = applicationSuccessCount.get();
            final long appFailure = applicationFailureCount.get();
            report.append(String.format("App Success: %d, App Failure: %d, ", appSuccess, appFailure));
            
            if (appSuccess > 0) {
                final long totalAppDuration = applicationTotalDuration.get();
                final long minAppDuration = applicationMinDuration.get();
                final long maxAppDuration = applicationMaxDuration.get();
                report.append(String.format("App Avg: %.2fms, App Min: %dms, App Max: %dms", 
                    (double) totalAppDuration / appSuccess, minAppDuration, maxAppDuration));
            }
            
            return report.toString();
        }
    }
}
