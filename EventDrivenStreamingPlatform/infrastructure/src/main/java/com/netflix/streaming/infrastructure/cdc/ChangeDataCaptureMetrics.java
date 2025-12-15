package com.netflix.streaming.infrastructure.cdc;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Metrics for Change Data Capture.
 */
@Component
public class ChangeDataCaptureMetrics {

    private static final Logger logger = LoggerFactory.getLogger(ChangeDataCaptureMetrics.class);
    private final MeterRegistry meterRegistry;

    public ChangeDataCaptureMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record a processed change.
     */
    public void recordChangeProcessed(String tableName, String operation) {
        Counter.builder("cdc.changes.processed")
            .tag("table", tableName)
            .tag("operation", operation)
            .description("Number of database changes processed by CDC")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record a failed change.
     */
    public void recordChangeFailed(String tableName, String operation) {
        Counter.builder("cdc.changes.failed")
            .tag("table", tableName)
            .tag("operation", operation)
            .description("Number of failed database changes in CDC")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record change lag (time between change and processing).
     */
    public void recordChangeLag(String tableName, long lagMs) {
        meterRegistry.gauge("cdc.change.lag",
            java.util.Arrays.asList("table", tableName),
            lagMs);
    }
}

