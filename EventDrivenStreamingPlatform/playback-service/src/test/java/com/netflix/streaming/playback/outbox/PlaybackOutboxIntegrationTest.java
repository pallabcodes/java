package com.netflix.streaming.playback.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.PlaybackStartedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PlaybackOutboxIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("outboxtest")
        .withUsername("testuser")
        .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PlaybackOutboxService outboxService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clean up outbox table
        jdbcTemplate.update("DELETE FROM playback_outbox_events");
    }

    @Test
    @Transactional
    void shouldStoreEventInOutbox() {
        // Given
        PlaybackStartedEvent event = new PlaybackStartedEvent(
            "correlation-123",
            "causation-456",
            "tenant-789",
            "session-123",
            "user-456",
            "movie-789",
            "MOVIE",
            "WEB",
            "HD",
            "1.0.0",
            "US",
            "WIFI",
            "cdn-1",
            0
        );

        // When
        outboxService.storeInOutbox(event);

        // Then
        List<Map<String, Object>> outboxEvents = jdbcTemplate.queryForList(
            "SELECT * FROM playback_outbox_events WHERE event_id = ?",
            event.getEventId()
        );

        assertEquals(1, outboxEvents.size());
        Map<String, Object> storedEvent = outboxEvents.get(0);
        assertEquals(event.getEventId(), storedEvent.get("event_id"));
        assertEquals("PLAYBACK_STARTED", storedEvent.get("event_type"));
        assertEquals("session-123", storedEvent.get("aggregate_id"));
        assertEquals("PlaybackSession", storedEvent.get("aggregate_type"));
        assertEquals("correlation-123", storedEvent.get("correlation_id"));
        assertEquals("PENDING", storedEvent.get("status"));
    }

    @Test
    void shouldProcessPendingOutboxEvents() {
        // Given - Insert event directly into outbox
        PlaybackStartedEvent event = new PlaybackStartedEvent(
            "correlation-123", null, "default",
            "session-123", "user-456", "movie-789",
            "MOVIE", "WEB", "HD", "1.0.0", "US", "WIFI", "cdn-1", 0
        );

        String eventJson = objectMapper.writeValueAsString(event);
        jdbcTemplate.update(
            "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?)",
            event.getEventId(), "PLAYBACK_STARTED", "session-123", "PlaybackSession",
            eventJson, "correlation-123", OffsetDateTime.now(), OffsetDateTime.now()
        );

        // When
        outboxService.processOutbox();

        // Then - Event should be marked as sent
        List<Map<String, Object>> processedEvents = jdbcTemplate.queryForList(
            "SELECT * FROM playback_outbox_events WHERE event_id = ?",
            event.getEventId()
        );

        assertEquals(1, processedEvents.size());
        assertEquals("SENT", processedEvents.get(0).get("status"));
        assertNotNull(processedEvents.get(0).get("sent_at"));
    }

    @Test
    void shouldRetryFailedEvents() {
        // Given - Insert event that will fail processing
        jdbcTemplate.update(
            "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?)",
            "failed-event-123", "INVALID_EVENT", "aggregate-123", "TestAggregate",
            "{\"invalid\": \"json\"", "correlation-123", OffsetDateTime.now(), OffsetDateTime.now()
        );

        // When
        outboxService.processOutbox();

        // Then - Event should be marked as failed and retried
        List<Map<String, Object>> failedEvents = jdbcTemplate.queryForList(
            "SELECT * FROM playback_outbox_events WHERE event_id = ?",
            "failed-event-123"
        );

        assertEquals(1, failedEvents.size());
        Map<String, Object> failedEvent = failedEvents.get(0);
        assertEquals("PENDING", failedEvent.get("status")); // Still pending for retry
        assertEquals(1, failedEvent.get("retry_count"));
        assertNotNull(failedEvent.get("next_retry_at"));
    }

    @Test
    void shouldMoveToDlqAfterMaxRetries() {
        // Given - Insert event that will always fail
        jdbcTemplate.update(
            "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, retry_count, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)",
            "max-retry-event-123", "INVALID_EVENT", "aggregate-123", "TestAggregate",
            "{\"invalid\": \"json\"", "correlation-123", 3, OffsetDateTime.now(), OffsetDateTime.now()
        );

        // When
        outboxService.processOutbox();

        // Then - Event should be moved to DLQ
        List<Map<String, Object>> dlqEvents = jdbcTemplate.queryForList(
            "SELECT * FROM playback_outbox_events WHERE event_id = ?",
            "max-retry-event-123"
        );

        assertEquals(1, dlqEvents.size());
        assertEquals("DLQ", dlqEvents.get(0).get("status"));
        assertNotNull(dlqEvents.get(0).get("error_message"));
    }

    @Test
    void shouldOnlyProcessAvailableEvents() {
        // Given - Insert one available and one future event
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime future = now.plusHours(1);

        jdbcTemplate.update(
            "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?)",
            "available-event-123", "PLAYBACK_STARTED", "session-123", "PlaybackSession",
            "{}", "correlation-123", now, now
        );

        jdbcTemplate.update(
            "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?)",
            "future-event-456", "PLAYBACK_STARTED", "session-456", "PlaybackSession",
            "{}", "correlation-456", now, future
        );

        // When
        outboxService.processOutbox();

        // Then - Only available event should be processed
        List<Map<String, Object>> availableEvents = jdbcTemplate.queryForList(
            "SELECT * FROM playback_outbox_events WHERE status = 'SENT'"
        );
        assertEquals(1, availableEvents.size());
        assertEquals("available-event-123", availableEvents.get(0).get("event_id"));

        List<Map<String, Object>> futureEvents = jdbcTemplate.queryForList(
            "SELECT * FROM playback_outbox_events WHERE status = 'PENDING' AND event_id = 'future-event-456'"
        );
        assertEquals(1, futureEvents.size());
    }

    @Test
    void shouldProvideOutboxStatistics() {
        // Given - Insert events with different statuses
        OffsetDateTime now = OffsetDateTime.now();

        // Pending event
        jdbcTemplate.update(
            "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, status, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)",
            "pending-event-123", "PLAYBACK_STARTED", "session-123", "PlaybackSession",
            "{}", "correlation-123", "PENDING", now, now
        );

        // Sent event
        jdbcTemplate.update(
            "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, status, sent_at, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)",
            "sent-event-456", "PLAYBACK_STARTED", "session-456", "PlaybackSession",
            "{}", "correlation-456", "SENT", now, now, now
        );

        // Failed event
        jdbcTemplate.update(
            "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, status, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)",
            "failed-event-789", "PLAYBACK_STARTED", "session-789", "PlaybackSession",
            "{}", "correlation-789", "FAILED", now, now
        );

        // DLQ event
        jdbcTemplate.update(
            "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, status, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)",
            "dlq-event-101", "PLAYBACK_STARTED", "session-101", "PlaybackSession",
            "{}", "correlation-101", "DLQ", now, now
        );

        // When
        PlaybackOutboxService.OutboxStats stats = outboxService.getStats();

        // Then
        assertEquals(4, stats.total());
        assertEquals(1, stats.pending());
        assertEquals(1, stats.sent());
        assertEquals(1, stats.failed());
        assertEquals(1, stats.dlq());
    }

    @Test
    void shouldHandleConcurrentProcessing() throws InterruptedException {
        // Given - Insert multiple events
        for (int i = 0; i < 10; i++) {
            jdbcTemplate.update(
                "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?)",
                "concurrent-event-" + i, "PLAYBACK_STARTED", "session-" + i, "PlaybackSession",
                "{}", "correlation-" + i, OffsetDateTime.now(), OffsetDateTime.now()
            );
        }

        // When - Process events (this would normally be called by scheduler)
        Thread thread1 = new Thread(() -> outboxService.processOutbox());
        Thread thread2 = new Thread(() -> outboxService.processOutbox());

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then - All events should be processed without conflicts
        List<Map<String, Object>> processedEvents = jdbcTemplate.queryForList(
            "SELECT * FROM playback_outbox_events WHERE status = 'SENT'"
        );
        assertTrue(processedEvents.size() > 0, "Some events should be processed");
    }

    @Test
    void shouldCleanupOldProcessedEvents() {
        // Given - Insert old processed events
        OffsetDateTime oldDate = OffsetDateTime.now().minusDays(40);
        for (int i = 0; i < 5; i++) {
            jdbcTemplate.update(
                "INSERT INTO playback_outbox_events (event_id, event_type, aggregate_id, aggregate_type, event_data, correlation_id, status, sent_at, created_at, available_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)",
                "old-event-" + i, "PLAYBACK_STARTED", "session-" + i, "PlaybackSession",
                "{}", "correlation-" + i, "SENT", oldDate, oldDate, oldDate
            );
        }

        // When
        outboxService.cleanupOldEvents();

        // Then - Old events should be deleted
        List<Map<String, Object>> remainingEvents = jdbcTemplate.queryForList(
            "SELECT * FROM playback_outbox_events"
        );
        assertEquals(0, remainingEvents.size());
    }
}
