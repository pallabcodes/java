package com.netflix.streaming.infrastructure.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.streaming.events.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL-based Event Store implementation.
 * Provides append-only event storage with versioning and replay capabilities.
 */
public class PostgreSQLEventStore implements EventStore {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLEventStore.class);

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;
    private final String eventTableName;
    private final String snapshotTableName;

    public PostgreSQLEventStore(JdbcTemplate jdbcTemplate,
                               PlatformTransactionManager transactionManager,
                               String eventTableName,
                               String snapshotTableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.objectMapper = new ObjectMapper();
        this.eventTableName = eventTableName;
        this.snapshotTableName = snapshotTableName;
    }

    @Override
    public void append(BaseEvent event) throws EventStoreException {
        try {
            String sql = String.format(
                "INSERT INTO %s (event_id, event_type, aggregate_id, aggregate_type, " +
                "event_data, correlation_id, causation_id, tenant_id, schema_version, created_at) " +
                "VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)",
                eventTableName
            );

            String eventData = objectMapper.writeValueAsString(event);

            jdbcTemplate.update(sql,
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getAggregateType(),
                eventData,
                event.getCorrelationId(),
                event.getCausationId(),
                event.getTenantId(),
                event.getSchemaVersion(),
                event.getTimestamp()
            );

            logger.debug("Appended event: {} for aggregate: {}",
                event.getEventId(), event.getAggregateId());

        } catch (Exception e) {
            logger.error("Failed to append event: {}", event, e);
            throw new EventStoreException("Failed to append event", e);
        }
    }

    @Override
    public void appendBatch(List<BaseEvent> events) throws EventStoreException {
        transactionTemplate.execute(status -> {
            try {
                for (BaseEvent event : events) {
                    append(event);
                }
                return null;
            } catch (EventStoreException e) {
                status.setRollbackOnly();
                throw e;
            }
        });
    }

    @Override
    public List<StoredEvent> loadEvents(String aggregateId) {
        return loadEvents(aggregateId, 0);
    }

    @Override
    public List<StoredEvent> loadEvents(String aggregateId, long fromVersion) {
        String sql = String.format(
            "SELECT id, event_id, event_type, aggregate_id, aggregate_type, " +
            "event_data, correlation_id, causation_id, tenant_id, schema_version, " +
            "created_at, version FROM %s WHERE aggregate_id = ? AND version > ? " +
            "ORDER BY version ASC",
            eventTableName
        );

        return jdbcTemplate.query(sql, new StoredEventRowMapper(), aggregateId, fromVersion);
    }

    @Override
    public List<StoredEvent> loadEvents(String aggregateId, Instant fromTime) {
        String sql = String.format(
            "SELECT id, event_id, event_type, aggregate_id, aggregate_type, " +
            "event_data, correlation_id, causation_id, tenant_id, schema_version, " +
            "created_at, version FROM %s WHERE aggregate_id = ? AND created_at >= ? " +
            "ORDER BY version ASC",
            eventTableName
        );

        return jdbcTemplate.query(sql, new StoredEventRowMapper(), aggregateId, fromTime);
    }

    @Override
    public Optional<StoredEvent> loadEvent(String eventId) {
        String sql = String.format(
            "SELECT id, event_id, event_type, aggregate_id, aggregate_type, " +
            "event_data, correlation_id, causation_id, tenant_id, schema_version, " +
            "created_at, version FROM %s WHERE event_id = ?",
            eventTableName
        );

        try {
            StoredEvent event = jdbcTemplate.queryForObject(sql, new StoredEventRowMapper(), eventId);
            return Optional.ofNullable(event);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public long getCurrentVersion(String aggregateId) {
        String sql = String.format(
            "SELECT COALESCE(MAX(version), 0) FROM %s WHERE aggregate_id = ?",
            eventTableName
        );

        return jdbcTemplate.queryForObject(sql, Long.class, aggregateId);
    }

    @Override
    public List<String> getAggregateIds(String aggregateType) {
        String sql = String.format(
            "SELECT DISTINCT aggregate_id FROM %s WHERE aggregate_type = ?",
            eventTableName
        );

        return jdbcTemplate.queryForList(sql, String.class, aggregateType);
    }

    @Override
    public List<StoredEvent> replayAll(Instant fromTime) {
        String sql = String.format(
            "SELECT id, event_id, event_type, aggregate_id, aggregate_type, " +
            "event_data, correlation_id, causation_id, tenant_id, schema_version, " +
            "created_at, version FROM %s WHERE created_at >= ? " +
            "ORDER BY created_at ASC",
            eventTableName
        );

        return jdbcTemplate.query(sql, new StoredEventRowMapper(), fromTime);
    }

    @Override
    public List<StoredEvent> replayByType(String eventType, Instant fromTime) {
        String sql = String.format(
            "SELECT id, event_id, event_type, aggregate_id, aggregate_type, " +
            "event_data, correlation_id, causation_id, tenant_id, schema_version, " +
            "created_at, version FROM %s WHERE event_type = ? AND created_at >= ? " +
            "ORDER BY created_at ASC",
            eventTableName
        );

        return jdbcTemplate.query(sql, new StoredEventRowMapper(), eventType, fromTime);
    }

    /**
     * Row mapper for converting database rows to StoredEvent objects
     */
    private static class StoredEventRowMapper implements RowMapper<StoredEvent> {
        @Override
        public StoredEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                String eventData = rs.getString("event_data");
                BaseEvent event = new ObjectMapper().readValue(eventData, BaseEvent.class);

                return new StoredEvent(
                    rs.getLong("id"),
                    rs.getString("event_id"),
                    rs.getString("event_type"),
                    rs.getString("aggregate_id"),
                    rs.getString("aggregate_type"),
                    event,
                    rs.getString("correlation_id"),
                    rs.getString("causation_id"),
                    rs.getString("tenant_id"),
                    rs.getString("schema_version"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getLong("version")
                );
            } catch (Exception e) {
                throw new SQLException("Failed to deserialize event data", e);
            }
        }
    }
}