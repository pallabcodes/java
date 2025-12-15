package com.netflix.streaming.infrastructure.cdc;

import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Change Data Stream Service.
 * 
 * Provides streaming interface for database changes.
 * Supports:
 * - Real-time change streaming
 * - Change filtering
 * - Change transformation
 * - Multiple subscribers
 */
@Service
public class ChangeDataStreamService {

    private static final Logger logger = LoggerFactory.getLogger(ChangeDataStreamService.class);

    private final ChangeDataCaptureService cdcService;
    private final EventPublisher eventPublisher;
    private final JdbcTemplate jdbcTemplate;

    public ChangeDataStreamService(ChangeDataCaptureService cdcService,
                                   EventPublisher eventPublisher,
                                   JdbcTemplate jdbcTemplate) {
        this.cdcService = cdcService;
        this.eventPublisher = eventPublisher;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Stream changes for a specific table.
     * 
     * @param tableName Table to stream changes from
     * @param filter Optional filter for changes
     * @param handler Handler for each change
     */
    public void streamTableChanges(String tableName, ChangeFilter filter, Consumer<BaseEvent> handler) {
        // This would typically use database change streams or polling
        // For PostgreSQL, we use change log table polling
        
        logger.info("Starting change stream for table: {}", tableName);
        
        // Poll change log table for changes
        String sql = """
            SELECT id, table_name, operation, primary_key, before_state, after_state,
                   changed_columns, transaction_id, changed_at
            FROM database_change_log
            WHERE table_name = ?
              AND processed_at IS NULL
            ORDER BY changed_at ASC
            LIMIT 100
            """;

        List<Map<String, Object>> changes = jdbcTemplate.queryForList(sql, tableName);

        for (Map<String, Object> change : changes) {
            // Apply filter if provided
            if (filter != null && !filter.matches(change)) {
                continue;
            }

            // Convert to event
            BaseEvent event = convertChangeToEvent(change);

            // Handle change
            handler.accept(event);
        }
    }

    /**
     * Stream changes for multiple tables.
     */
    public void streamChanges(List<String> tableNames, ChangeFilter filter, Consumer<BaseEvent> handler) {
        for (String tableName : tableNames) {
            streamTableChanges(tableName, filter, handler);
        }
    }

    /**
     * Stream changes matching a filter.
     */
    public void streamFilteredChanges(ChangeFilter filter, Consumer<BaseEvent> handler) {
        String sql = """
            SELECT id, table_name, operation, primary_key, before_state, after_state,
                   changed_columns, transaction_id, changed_at
            FROM database_change_log
            WHERE processed_at IS NULL
            ORDER BY changed_at ASC
            LIMIT 100
            """;

        List<Map<String, Object>> changes = jdbcTemplate.queryForList(sql);

        for (Map<String, Object> change : changes) {
            if (filter != null && !filter.matches(change)) {
                continue;
            }

            BaseEvent event = convertChangeToEvent(change);
            handler.accept(event);
        }
    }

    /**
     * Convert change map to domain event.
     */
    private BaseEvent convertChangeToEvent(Map<String, Object> change) {
        String tableName = (String) change.get("table_name");
        String operation = (String) change.get("operation");
        String primaryKey = (String) change.get("primary_key");
        String transactionId = (String) change.get("transaction_id");
        Instant changedAt = ((java.sql.Timestamp) change.get("changed_at")).toInstant();

        return new DatabaseChangeEvent(
            transactionId != null ? transactionId : UUID.randomUUID().toString(),
            transactionId,
            "default",
            tableName,
            primaryKey,
            operation,
            change,
            changedAt
        );
    }

    /**
     * Determine event type from table and operation.
     */
    private String determineEventType(String tableName, String operation) {
        String tableNameCamelCase = toCamelCase(tableName);
        String operationCamelCase = capitalize(operation);
        return tableNameCamelCase + operationCamelCase + "Event";
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
     * Change filter interface.
     */
    @FunctionalInterface
    public interface ChangeFilter {
        boolean matches(Map<String, Object> change);
    }

    /**
     * Predefined filters.
     */
    public static class ChangeFilters {
        /**
         * Filter by table name.
         */
        public static ChangeFilter byTable(String tableName) {
            return change -> tableName.equals(change.get("table_name"));
        }

        /**
         * Filter by operation.
         */
        public static ChangeFilter byOperation(String operation) {
            return change -> operation.equals(change.get("operation"));
        }

        /**
         * Filter by column change.
         */
        public static ChangeFilter byColumnChange(String columnName) {
            return change -> {
                String[] changedColumns = (String[]) change.get("changed_columns");
                if (changedColumns == null) {
                    return false;
                }
                for (String column : changedColumns) {
                    if (columnName.equals(column)) {
                        return true;
                    }
                }
                return false;
            };
        }

        /**
         * Combine multiple filters with AND.
         */
        public static ChangeFilter and(ChangeFilter... filters) {
            return change -> {
                for (ChangeFilter filter : filters) {
                    if (!filter.matches(change)) {
                        return false;
                    }
                }
                return true;
            };
        }

        /**
         * Combine multiple filters with OR.
         */
        public static ChangeFilter or(ChangeFilter... filters) {
            return change -> {
                for (ChangeFilter filter : filters) {
                    if (filter.matches(change)) {
                        return true;
                    }
                }
                return false;
            };
        }
    }
}

