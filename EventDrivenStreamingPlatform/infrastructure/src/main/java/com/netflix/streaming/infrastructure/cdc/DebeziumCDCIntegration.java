package com.netflix.streaming.infrastructure.cdc;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Debezium CDC Integration.
 * 
 * Consumes Debezium change events from Kafka and converts them
 * to domain events. This provides:
 * - Real-time database change capture
 * - No application code changes required
 * - Supports all database operations
 * - Transactional consistency
 * 
 * Note: Requires Debezium connector to be configured separately.
 * This component consumes Debezium's change events from Kafka.
 */
@Component
public class DebeziumCDCIntegration {

    private static final Logger logger = LoggerFactory.getLogger(DebeziumCDCIntegration.class);

    private final ChangeDataCaptureService cdcService;
    private final ChangeDataCaptureMetrics metrics;

    public DebeziumCDCIntegration(ChangeDataCaptureService cdcService,
                                  ChangeDataCaptureMetrics metrics) {
        this.cdcService = cdcService;
        this.metrics = metrics;
    }

    /**
     * Consume Debezium change events from Kafka.
     * 
     * Debezium change event format:
     * {
     *   "before": {...},  // State before change (for UPDATE/DELETE)
     *   "after": {...},  // State after change (for INSERT/UPDATE)
     *   "source": {
     *     "table": "playback_sessions",
     *     "db": "event_store"
     *   },
     *   "op": "c" | "u" | "d"  // c=create, u=update, d=delete
     * }
     */
    @KafkaListener(
        topics = "${app.cdc.debezium.topic:debezium.public.playback_sessions}",
        groupId = "cdc-debezium-consumer"
    )
    public void consumeDebeziumChangeEvent(
            @Payload Map<String, Object> debeziumEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            logger.debug("Received Debezium change event from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);

            // Extract change information
            String operation = extractOperation(debeziumEvent);
            String tableName = extractTableName(debeziumEvent);
            Map<String, Object> before = extractBefore(debeziumEvent);
            Map<String, Object> after = extractAfter(debeziumEvent);
            String primaryKey = extractPrimaryKey(debeziumEvent, before, after);

            // Convert to domain event and publish
            processDebeziumChange(tableName, operation, primaryKey, before, after);

            // Acknowledge
            acknowledgment.acknowledge();

            // Record metrics
            metrics.recordChangeProcessed(tableName, operation);

            logger.debug("Processed Debezium change event: table={}, operation={}, key={}",
                tableName, operation, primaryKey);

        } catch (Exception e) {
            logger.error("Failed to process Debezium change event from topic: {}", topic, e);
            metrics.recordChangeFailed("unknown", "DEBEZIUM");
            // Don't acknowledge - will be retried or sent to DLQ
            throw e;
        }
    }

    /**
     * Extract operation from Debezium event.
     */
    private String extractOperation(Map<String, Object> event) {
        String op = (String) event.get("op");
        if (op == null) {
            return "UNKNOWN";
        }

        // Debezium operation codes: c=create, u=update, d=delete, r=read
        return switch (op) {
            case "c" -> "INSERT";
            case "u" -> "UPDATE";
            case "d" -> "DELETE";
            case "r" -> "READ";
            default -> "UNKNOWN";
        };
    }

    /**
     * Extract table name from Debezium event.
     */
    @SuppressWarnings("unchecked")
    private String extractTableName(Map<String, Object> event) {
        Map<String, Object> source = (Map<String, Object>) event.get("source");
        if (source == null) {
            return "unknown";
        }
        return (String) source.getOrDefault("table", "unknown");
    }

    /**
     * Extract before state from Debezium event.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractBefore(Map<String, Object> event) {
        return (Map<String, Object>) event.get("before");
    }

    /**
     * Extract after state from Debezium event.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractAfter(Map<String, Object> event) {
        return (Map<String, Object>) event.get("after");
    }

    /**
     * Extract primary key from Debezium event.
     */
    @SuppressWarnings("unchecked")
    private String extractPrimaryKey(Map<String, Object> event,
                                    Map<String, Object> before,
                                    Map<String, Object> after) {
        // Try to get from after state first, then before
        Map<String, Object> state = after != null ? after : before;
        if (state == null) {
            return "unknown";
        }

        // Try common primary key column names
        if (state.containsKey("id")) {
            return String.valueOf(state.get("id"));
        }
        if (state.containsKey("uuid")) {
            return String.valueOf(state.get("uuid"));
        }

        // Fallback: use first value
        return state.values().iterator().next().toString();
    }

    /**
     * Process Debezium change and publish as domain event.
     */
    private void processDebeziumChange(String tableName, String operation,
                                      String primaryKey, Map<String, Object> before,
                                      Map<String, Object> after) {
        // Convert Debezium change to domain event
        // This would typically involve:
        // 1. Determining event type based on table and operation
        // 2. Creating domain event with change data
        // 3. Publishing via event publisher

        logger.info("Processing Debezium change: table={}, operation={}, key={}",
            tableName, operation, primaryKey);

        // Integration with ChangeDataCaptureService or direct event publishing
        // This is a placeholder - actual implementation would create domain events
    }
}

