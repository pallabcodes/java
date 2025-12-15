package com.netflix.streaming.infrastructure.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Change Data Capture (CDC) Service.
 * 
 * Captures database changes and publishes them as events.
 * Implements CDC using:
 * - Database triggers (PostgreSQL)
 * - Change log table polling
 * - Outbox pattern integration
 * 
 * Supports:
 * - INSERT, UPDATE, DELETE operations
 * - Before/after state capture
 * - Transactional consistency
 * - Event deduplication
 */
@Service
public class ChangeDataCaptureService {

    private static final Logger logger = LoggerFactory.getLogger(ChangeDataCaptureService.class);
    private static final String CHANGE_LOG_TABLE = "database_change_log";
    private static final int BATCH_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final ChangeDataCaptureMetrics metrics;

    public ChangeDataCaptureService(JdbcTemplate jdbcTemplate,
                                   EventPublisher eventPublisher,
                                   ObjectMapper objectMapper,
                                   ChangeDataCaptureMetrics metrics) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    /**
     * Process pending change log entries and publish as events.
     * Called by scheduler or triggered by database events.
     */
    @Scheduled(fixedRate = 5000) // Poll every 5 seconds
    @Transactional
    public void processChangeLog() {
        try {
            // Get pending change log entries
            List<ChangeLogEntry> pendingChanges = getPendingChanges();

            if (pendingChanges.isEmpty()) {
                return;
            }

            logger.debug("Processing {} change log entries", pendingChanges.size());

            for (ChangeLogEntry change : pendingChanges) {
                try {
                    // Convert change log entry to domain event
                    BaseEvent event = convertToEvent(change);

                    // Publish event
                    eventPublisher.publish(event);

                    // Mark as processed
                    markAsProcessed(change.getId());

                    // Record metrics
                    metrics.recordChangeProcessed(change.getTableName(), change.getOperation());

                    logger.debug("Processed change log entry: {} for table: {}", change.getId(), change.getTableName());

                } catch (Exception e) {
                    logger.error("Failed to process change log entry: {}", change.getId(), e);
                    markAsFailed(change.getId(), e.getMessage());
                    metrics.recordChangeFailed(change.getTableName(), change.getOperation());
                }
            }

        } catch (Exception e) {
            logger.error("Error processing change log", e);
        }
    }

    /**
     * Manually trigger CDC for a specific table.
     * Useful for backfilling or manual synchronization.
     */
    public void triggerCdcForTable(String tableName, String primaryKeyColumn, Object primaryKeyValue) {
        try {
            // Get current state from table
            ChangeLogEntry change = captureCurrentState(tableName, primaryKeyColumn, primaryKeyValue);

            if (change != null) {
                // Process immediately
                BaseEvent event = convertToEvent(change);
                eventPublisher.publish(event);
                metrics.recordChangeProcessed(tableName, "MANUAL");
            }

        } catch (Exception e) {
            logger.error("Failed to trigger CDC for table: {}, key: {}", tableName, primaryKeyValue, e);
            metrics.recordChangeFailed(tableName, "MANUAL");
        }
    }

    /**
     * Get pending change log entries.
     */
    private List<ChangeLogEntry> getPendingChanges() {
        String sql = """
            SELECT id, table_name, operation, primary_key, before_state, after_state,
                   changed_columns, transaction_id, changed_at, processed_at
            FROM %s
            WHERE processed_at IS NULL
              AND failed_at IS NULL
            ORDER BY changed_at ASC
            LIMIT ?
            FOR UPDATE SKIP LOCKED
            """.formatted(CHANGE_LOG_TABLE);

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ChangeLogEntry(
            rs.getLong("id"),
            rs.getString("table_name"),
            rs.getString("operation"),
            rs.getString("primary_key"),
            rs.getString("before_state"),
            rs.getString("after_state"),
            rs.getString("changed_columns"),
            rs.getString("transaction_id"),
            rs.getTimestamp("changed_at").toInstant(),
            rs.getTimestamp("processed_at") != null ? rs.getTimestamp("processed_at").toInstant() : null
        ), BATCH_SIZE);
    }

    /**
     * Convert change log entry to domain event.
     * Creates a generic CDC event that wraps the change data.
     */
    private BaseEvent convertToEvent(ChangeLogEntry change) {
        // Parse before/after states
        Map<String, Object> beforeState = parseJson(change.getBeforeState());
        Map<String, Object> afterState = parseJson(change.getAfterState());

        // Create event data
        Map<String, Object> eventData = createEventData(change, beforeState, afterState);

        // Create a generic CDC event
        return new DatabaseChangeEvent(
            change.getTransactionId(),
            change.getTransactionId(),
            "default",
            change.getTableName(),
            change.getPrimaryKey(),
            change.getOperation(),
            eventData,
            change.getChangedAt()
        );
    }

    /**
     * Determine event type from change log entry.
     */
    private String determineEventType(ChangeLogEntry change) {
        String tableName = change.getTableName();
        String operation = change.getOperation();

        // Event type naming: {TableName}{Operation}Event
        // e.g., PlaybackSessionCreatedEvent, PlaybackSessionUpdatedEvent
        String tableNameCamelCase = toCamelCase(tableName);
        String operationCamelCase = capitalize(operation);

        return tableNameCamelCase + operationCamelCase + "Event";
    }

    /**
     * Create event data from change log entry.
     */
    private Map<String, Object> createEventData(ChangeLogEntry change,
                                               Map<String, Object> beforeState,
                                               Map<String, Object> afterState) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("operation", change.getOperation());
        eventData.put("tableName", change.getTableName());
        eventData.put("primaryKey", change.getPrimaryKey());
        eventData.put("changedColumns", parseJsonArray(change.getChangedColumns()));

        if (change.getOperation().equals("INSERT")) {
            eventData.put("after", afterState);
        } else if (change.getOperation().equals("UPDATE")) {
            eventData.put("before", beforeState);
            eventData.put("after", afterState);
        } else if (change.getOperation().equals("DELETE")) {
            eventData.put("before", beforeState);
        }

        return eventData;
    }

    /**
     * Capture current state from table.
     */
    private ChangeLogEntry captureCurrentState(String tableName, String primaryKeyColumn, Object primaryKeyValue) {
        try {
            String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, primaryKeyColumn);
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, primaryKeyValue);

            String afterState = objectMapper.writeValueAsString(row);

            return new ChangeLogEntry(
                0L,
                tableName,
                "SELECT",
                primaryKeyValue.toString(),
                null,
                afterState,
                null,
                UUID.randomUUID().toString(),
                Instant.now(),
                null
            );

        } catch (Exception e) {
            logger.error("Failed to capture current state for table: {}, key: {}", tableName, primaryKeyValue, e);
            return null;
        }
    }

    /**
     * Mark change log entry as processed.
     */
    private void markAsProcessed(Long id) {
        String sql = """
            UPDATE %s
            SET processed_at = NOW()
            WHERE id = ?
            """.formatted(CHANGE_LOG_TABLE);

        jdbcTemplate.update(sql, id);
    }

    /**
     * Mark change log entry as failed.
     */
    private void markAsFailed(Long id, String errorMessage) {
        String sql = """
            UPDATE %s
            SET failed_at = NOW(), error_message = ?
            WHERE id = ?
            """.formatted(CHANGE_LOG_TABLE);

        jdbcTemplate.update(sql, errorMessage, id);
    }

    /**
     * Parse JSON string to map.
     */
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            logger.warn("Failed to parse JSON: {}", json, e);
            return new HashMap<>();
        }
    }

    /**
     * Parse JSON array string to list.
     */
    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            logger.warn("Failed to parse JSON array: {}", json, e);
            return new ArrayList<>();
        }
    }

    /**
     * Convert snake_case to CamelCase.
     */
    private String toCamelCase(String snakeCase) {
        String[] parts = snakeCase.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append(capitalize(part.toLowerCase()));
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
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Change log entry representation.
     */
    public static class ChangeLogEntry {
        private final Long id;
        private final String tableName;
        private final String operation;
        private final String primaryKey;
        private final String beforeState;
        private final String afterState;
        private final String changedColumns;
        private final String transactionId;
        private final Instant changedAt;
        private final Instant processedAt;

        public ChangeLogEntry(Long id, String tableName, String operation, String primaryKey,
                             String beforeState, String afterState, String changedColumns,
                             String transactionId, Instant changedAt, Instant processedAt) {
            this.id = id;
            this.tableName = tableName;
            this.operation = operation;
            this.primaryKey = primaryKey;
            this.beforeState = beforeState;
            this.afterState = afterState;
            this.changedColumns = changedColumns;
            this.transactionId = transactionId;
            this.changedAt = changedAt;
            this.processedAt = processedAt;
        }

        // Getters
        public Long getId() { return id; }
        public String getTableName() { return tableName; }
        public String getOperation() { return operation; }
        public String getPrimaryKey() { return primaryKey; }
        public String getBeforeState() { return beforeState; }
        public String getAfterState() { return afterState; }
        public String getChangedColumns() { return changedColumns; }
        public String getTransactionId() { return transactionId; }
        public Instant getChangedAt() { return changedAt; }
        public Instant getProcessedAt() { return processedAt; }
    }
}

