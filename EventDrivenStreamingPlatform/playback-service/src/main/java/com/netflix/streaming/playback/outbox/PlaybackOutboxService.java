package com.netflix.streaming.playback.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Transactional Outbox for Playback Service.
 *
 * This demonstrates:
 * - Transactional Outbox Pattern: Atomic DB + Event publishing
 * - Reliable event publishing: Store then publish
 * - At-least-once delivery: Idempotent event processing
 * - Outbox table cleanup: Automatic maintenance
 */
@Service
public class PlaybackOutboxService {

    private static final Logger logger = LoggerFactory.getLogger(PlaybackOutboxService.class);
    private static final String OUTBOX_TABLE = "playback_outbox_events";

    private final JdbcTemplate jdbcTemplate;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public PlaybackOutboxService(JdbcTemplate jdbcTemplate,
                                EventPublisher eventPublisher,
                                ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * Store event in outbox as part of the same transaction as business logic.
     * This ensures atomicity: either both business changes and event publishing succeed,
     * or both fail.
     */
    @Transactional
    public void storeInOutbox(BaseEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);

            String sql = """
                INSERT INTO %s (
                    event_id, event_type, aggregate_id, aggregate_type,
                    event_data, correlation_id, causation_id, tenant_id,
                    created_at, available_at
                ) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)
                """.formatted(OUTBOX_TABLE);

            jdbcTemplate.update(sql,
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getAggregateType(),
                eventData,
                event.getCorrelationId(),
                event.getCausationId(),
                event.getTenantId(),
                event.getTimestamp(),
                event.getTimestamp() // Available immediately
            );

            logger.debug("Stored event in outbox: {} for aggregate: {}",
                        event.getEventId(), event.getAggregateId());

        } catch (Exception e) {
            logger.error("Failed to store event in outbox: {}", event, e);
            throw new OutboxException("Failed to store event in outbox", e);
        }
    }

    /**
     * Process outbox events - called by scheduler or background job.
     * Publishes events to the event bus and marks them as sent.
     */
    @Transactional
    public void processOutbox() {
        // Get pending events (limit batch size for performance)
        String selectSql = """
            SELECT id, event_id, event_data, aggregate_id, correlation_id
            FROM %s
            WHERE status = 'PENDING'
              AND available_at <= ?
            ORDER BY created_at ASC
            LIMIT 100
            FOR UPDATE SKIP LOCKED
            """.formatted(OUTBOX_TABLE);

        List<OutboxEvent> pendingEvents = jdbcTemplate.query(selectSql,
            (rs, rowNum) -> new OutboxEvent(
                rs.getLong("id"),
                rs.getString("event_id"),
                rs.getString("event_data"),
                rs.getString("aggregate_id"),
                rs.getString("correlation_id")
            ),
            OffsetDateTime.now()
        );

        logger.debug("Processing {} outbox events", pendingEvents.size());

        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                // Deserialize event
                BaseEvent event = objectMapper.readValue(outboxEvent.eventData(), BaseEvent.class);

                // Publish to event bus
                eventPublisher.publish(event);

                // Mark as sent
                markAsSent(outboxEvent.id());

                logger.debug("Successfully published outbox event: {}", outboxEvent.eventId());

            } catch (Exception e) {
                logger.error("Failed to publish outbox event: {}", outboxEvent.eventId(), e);

                // Mark for retry (up to max attempts)
                incrementRetryCount(outboxEvent.id());

                // If max retries exceeded, mark as failed
                if (shouldMoveToDlq(outboxEvent.id())) {
                    moveToDlq(outboxEvent.id(), e.getMessage());
                }
            }
        }
    }

    /**
     * Clean up old processed events to prevent table growth.
     */
    @Transactional
    public void cleanupOldEvents() {
        String sql = """
            DELETE FROM %s
            WHERE status = 'SENT'
              AND created_at < ? - INTERVAL '30 days'
            """.formatted(OUTBOX_TABLE);

        int deleted = jdbcTemplate.update(sql, OffsetDateTime.now());
        if (deleted > 0) {
            logger.info("Cleaned up {} old outbox events", deleted);
        }
    }

    /**
     * Get outbox statistics for monitoring.
     */
    public OutboxStats getStats() {
        String sql = """
            SELECT
                COUNT(*) as total,
                COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending,
                COUNT(CASE WHEN status = 'SENT' THEN 1 END) as sent,
                COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed,
                COUNT(CASE WHEN status = 'DLQ' THEN 1 END) as dlq
            FROM %s
            """.formatted(OUTBOX_TABLE);

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new OutboxStats(
            rs.getLong("total"),
            rs.getLong("pending"),
            rs.getLong("sent"),
            rs.getLong("failed"),
            rs.getLong("dlq")
        ));
    }

    // Private helper methods

    private void markAsSent(Long id) {
        String sql = """
            UPDATE %s
            SET status = 'SENT', sent_at = ?, updated_at = ?
            WHERE id = ?
            """.formatted(OUTBOX_TABLE);

        jdbcTemplate.update(sql, OffsetDateTime.now(), OffsetDateTime.now(), id);
    }

    private void incrementRetryCount(Long id) {
        String sql = """
            UPDATE %s
            SET retry_count = retry_count + 1, updated_at = ?
            WHERE id = ?
            """.formatted(OUTBOX_TABLE);

        jdbcTemplate.update(sql, OffsetDateTime.now(), id);
    }

    private boolean shouldMoveToDlq(Long id) {
        String sql = "SELECT retry_count >= 3 FROM %s WHERE id = ?".formatted(OUTBOX_TABLE);
        Boolean maxRetriesExceeded = jdbcTemplate.queryForObject(sql, Boolean.class, id);
        return Boolean.TRUE.equals(maxRetriesExceeded);
    }

    private void moveToDlq(Long id, String errorMessage) {
        String sql = """
            UPDATE %s
            SET status = 'DLQ', error_message = ?, updated_at = ?
            WHERE id = ?
            """.formatted(OUTBOX_TABLE);

        jdbcTemplate.update(sql, errorMessage, OffsetDateTime.now(), id);
        logger.warn("Moved outbox event {} to DLQ: {}", id, errorMessage);
    }

    // Inner classes for data transfer

    private record OutboxEvent(
        Long id,
        String eventId,
        String eventData,
        String aggregateId,
        String correlationId
    ) {}

    public record OutboxStats(
        long total,
        long pending,
        long sent,
        long failed,
        long dlq
    ) {}

    public static class OutboxException extends RuntimeException {
        public OutboxException(String message) {
            super(message);
        }

        public OutboxException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}