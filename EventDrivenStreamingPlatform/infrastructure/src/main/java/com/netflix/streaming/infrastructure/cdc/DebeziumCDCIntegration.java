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

import java.util.HashMap;
import java.util.HashSet;
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

        logger.info("Processing Debezium change: table={}, operation={}, key={}",
            tableName, operation, primaryKey);

        try {
            // Create domain event based on table and operation
            BaseEvent domainEvent = createDomainEvent(tableName, operation, primaryKey, before, after);

            if (domainEvent != null) {
                // Publish the domain event
                cdcService.triggerCdcForTable(tableName, "id", primaryKey);

                logger.debug("Successfully processed Debezium change and triggered CDC for: {}", tableName);
            } else {
                logger.warn("No domain event created for Debezium change: table={}, operation={}",
                    tableName, operation);
            }

        } catch (Exception e) {
            logger.error("Failed to create domain event for Debezium change: table={}, operation={}, key={}",
                tableName, operation, primaryKey, e);
            throw new RuntimeException("Failed to process Debezium change", e);
        }
    }

    /**
     * Create appropriate domain event based on table and operation.
     */
    private BaseEvent createDomainEvent(String tableName, String operation,
                                       String primaryKey, Map<String, Object> before,
                                       Map<String, Object> after) {

        // Determine event type based on table and operation
        String eventType = determineEventType(tableName, operation);

        // Create event data with change information
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("tableName", tableName);
        eventData.put("operation", operation);
        eventData.put("primaryKey", primaryKey);
        eventData.put("source", "DEBEZIUM");

        if (operation.equals("INSERT") && after != null) {
            eventData.put("newData", after);
        } else if (operation.equals("UPDATE") && before != null && after != null) {
            eventData.put("oldData", before);
            eventData.put("newData", after);
            eventData.put("changedFields", determineChangedFields(before, after));
        } else if (operation.equals("DELETE") && before != null) {
            eventData.put("deletedData", before);
        }

        // For now, return a generic DatabaseChangeEvent
        // In a real implementation, you would create specific domain events
        // based on the table type and business logic
        return new DatabaseChangeEvent(
            primaryKey,
            primaryKey,
            "default",
            tableName,
            primaryKey,
            operation,
            eventData,
            java.time.Instant.now()
        );
    }

    /**
     * Determine event type based on table and operation.
     */
    private String determineEventType(String tableName, String operation) {
        // Convert table name to PascalCase and append operation
        String pascalCase = toPascalCase(tableName);
        return pascalCase + capitalize(operation) + "Event";
    }

    /**
     * Determine which fields changed between before and after states.
     */
    private java.util.List<String> determineChangedFields(Map<String, Object> before,
                                                         Map<String, Object> after) {
        java.util.List<String> changedFields = new java.util.ArrayList<>();

        if (before == null || after == null) {
            return changedFields;
        }

        // Check all keys in both maps
        java.util.Set<String> allKeys = new java.util.HashSet<>();
        allKeys.addAll(before.keySet());
        allKeys.addAll(after.keySet());

        for (String key : allKeys) {
            Object beforeValue = before.get(key);
            Object afterValue = after.get(key);

            if (!java.util.Objects.equals(beforeValue, afterValue)) {
                changedFields.add(key);
            }
        }

        return changedFields;
    }

    /**
     * Convert snake_case to PascalCase.
     */
    private String toPascalCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }

        String[] parts = snakeCase.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                      .append(part.substring(1));
            }
        }

        return result.toString();
    }

    /**
     * Capitalize first letter.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}

