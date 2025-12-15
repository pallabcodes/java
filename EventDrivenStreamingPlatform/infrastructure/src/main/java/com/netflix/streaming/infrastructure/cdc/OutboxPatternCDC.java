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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Outbox Pattern-based Change Data Capture.
 * 
 * Integrates with transactional outbox pattern to capture
 * database changes as events. This ensures:
 * - Transactional consistency (event published only if DB transaction commits)
 * - Exactly-once semantics (via outbox deduplication)
 * - Reliable event publishing (via outbox retry mechanism)
 */
@Service
public class OutboxPatternCDC {

    private static final Logger logger = LoggerFactory.getLogger(OutboxPatternCDC.class);
    private static final String OUTBOX_TABLE = "outbox_events";
    private static final int BATCH_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final ChangeDataCaptureMetrics metrics;

    public OutboxPatternCDC(JdbcTemplate jdbcTemplate,
                           EventPublisher eventPublisher,
                           ObjectMapper objectMapper,
                           ChangeDataCaptureMetrics metrics) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    /**
     * Process outbox events and publish to Kafka.
     * This is the CDC mechanism for outbox pattern.
     */
    @Scheduled(fixedRate = 1000) // Poll every 1 second for low latency
    @Transactional
    public void processOutboxEvents() {
        try {
            // Get pending outbox events
            List<OutboxEvent> pendingEvents = getPendingOutboxEvents();

            if (pendingEvents.isEmpty()) {
                return;
            }

            logger.debug("Processing {} outbox events for CDC", pendingEvents.size());

            for (OutboxEvent outboxEvent : pendingEvents) {
                try {
                    // Deserialize event from outbox
                    BaseEvent event = objectMapper.readValue(outboxEvent.getEventData(), BaseEvent.class);

                    // Publish event to Kafka
                    eventPublisher.publish(event);

                    // Mark as sent
                    markAsSent(outboxEvent.getId());

                    // Record metrics
                    metrics.recordChangeProcessed(outboxEvent.getAggregateType(), "OUTBOX");

                    logger.debug("Processed outbox event: {} for aggregate: {}",
                        outboxEvent.getEventId(), outboxEvent.getAggregateId());

                } catch (Exception e) {
                    logger.error("Failed to process outbox event: {}", outboxEvent.getId(), e);
                    incrementRetryCount(outboxEvent.getId());
                    
                    // Move to DLQ after max retries
                    if (shouldMoveToDlq(outboxEvent.getId())) {
                        moveToDlq(outboxEvent.getId(), e.getMessage());
                        metrics.recordChangeFailed(outboxEvent.getAggregateType(), "OUTBOX");
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error processing outbox events", e);
        }
    }

    /**
     * Get pending outbox events.
     */
    private List<OutboxEvent> getPendingOutboxEvents() {
        String sql = """
            SELECT id, event_id, event_data, aggregate_id, aggregate_type,
                   correlation_id, created_at, available_at, retry_count
            FROM %s
            WHERE status = 'PENDING'
              AND available_at <= ?
            ORDER BY created_at ASC
            LIMIT ?
            FOR UPDATE SKIP LOCKED
            """.formatted(OUTBOX_TABLE);

        return jdbcTemplate.query(sql, (rs, rowNum) -> new OutboxEvent(
            rs.getLong("id"),
            rs.getString("event_id"),
            rs.getString("event_data"),
            rs.getString("aggregate_id"),
            rs.getString("aggregate_type"),
            rs.getString("correlation_id"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("available_at").toInstant(),
            rs.getInt("retry_count")
        ), OffsetDateTime.now(), BATCH_SIZE);
    }

    /**
     * Mark outbox event as sent.
     */
    private void markAsSent(Long id) {
        String sql = """
            UPDATE %s
            SET status = 'SENT', sent_at = NOW()
            WHERE id = ?
            """.formatted(OUTBOX_TABLE);

        jdbcTemplate.update(sql, id);
    }

    /**
     * Increment retry count.
     */
    private void incrementRetryCount(Long id) {
        String sql = """
            UPDATE %s
            SET retry_count = retry_count + 1
            WHERE id = ?
            """.formatted(OUTBOX_TABLE);

        jdbcTemplate.update(sql, id);
    }

    /**
     * Check if should move to DLQ.
     */
    private boolean shouldMoveToDlq(Long id) {
        String sql = "SELECT retry_count >= 3 FROM %s WHERE id = ?".formatted(OUTBOX_TABLE);
        Boolean maxRetriesExceeded = jdbcTemplate.queryForObject(sql, Boolean.class, id);
        return Boolean.TRUE.equals(maxRetriesExceeded);
    }

    /**
     * Move to DLQ.
     */
    private void moveToDlq(Long id, String errorMessage) {
        String sql = """
            UPDATE %s
            SET status = 'DLQ', error_message = ?, updated_at = NOW()
            WHERE id = ?
            """.formatted(OUTBOX_TABLE);

        jdbcTemplate.update(sql, errorMessage, id);
        logger.warn("Moved outbox event {} to DLQ: {}", id, errorMessage);
    }

    /**
     * Outbox event representation.
     */
    public static class OutboxEvent {
        private final Long id;
        private final String eventId;
        private final String eventData;
        private final String aggregateId;
        private final String aggregateType;
        private final String correlationId;
        private final java.time.Instant createdAt;
        private final java.time.Instant availableAt;
        private final int retryCount;

        public OutboxEvent(Long id, String eventId, String eventData, String aggregateId,
                          String aggregateType, String correlationId,
                          java.time.Instant createdAt, java.time.Instant availableAt, int retryCount) {
            this.id = id;
            this.eventId = eventId;
            this.eventData = eventData;
            this.aggregateId = aggregateId;
            this.aggregateType = aggregateType;
            this.correlationId = correlationId;
            this.createdAt = createdAt;
            this.availableAt = availableAt;
            this.retryCount = retryCount;
        }

        // Getters
        public Long getId() { return id; }
        public String getEventId() { return eventId; }
        public String getEventData() { return eventData; }
        public String getAggregateId() { return aggregateId; }
        public String getAggregateType() { return aggregateType; }
        public String getCorrelationId() { return correlationId; }
        public java.time.Instant getCreatedAt() { return createdAt; }
        public java.time.Instant getAvailableAt() { return availableAt; }
        public int getRetryCount() { return retryCount; }
    }
}

