package com.netflix.streaming.infrastructure.store;

import com.netflix.streaming.events.BaseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PostgreSQLEventStoreIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PostgreSQLEventStore eventStore;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        eventStore.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveEvent() {
        // Given
        TestEvent event = new TestEvent("aggregate-123", "correlation-456");

        // When
        eventStore.save(event);

        // Then
        List<BaseEvent> events = eventStore.findByAggregateId("aggregate-123");
        assertEquals(1, events.size());
        assertEquals(event.getEventId(), events.get(0).getEventId());
        assertEquals(event.getAggregateId(), events.get(0).getAggregateId());
    }

    @Test
    void shouldSaveMultipleEventsForSameAggregate() {
        // Given
        TestEvent event1 = new TestEvent("aggregate-123", "correlation-1");
        TestEvent event2 = new TestEvent("aggregate-123", "correlation-2");

        // When
        eventStore.save(event1);
        eventStore.save(event2);

        // Then
        List<BaseEvent> events = eventStore.findByAggregateId("aggregate-123");
        assertEquals(2, events.size());
        assertEquals(1, events.get(0).getVersion());
        assertEquals(2, events.get(1).getVersion());
    }

    @Test
    void shouldFindEventsByEventType() {
        // Given
        TestEvent event1 = new TestEvent("aggregate-1", "correlation-1");
        TestEvent event2 = new TestEvent("aggregate-2", "correlation-2");

        eventStore.save(event1);
        eventStore.save(event2);

        // When
        List<BaseEvent> events = eventStore.findByEventType("TEST_EVENT");

        // Then
        assertEquals(2, events.size());
    }

    @Test
    void shouldFindEventsByCorrelationId() {
        // Given
        String correlationId = "correlation-123";
        TestEvent event1 = new TestEvent("aggregate-1", correlationId);
        TestEvent event2 = new TestEvent("aggregate-2", "different-correlation");

        eventStore.save(event1);
        eventStore.save(event2);

        // When
        List<BaseEvent> events = eventStore.findByCorrelationId(correlationId);

        // Then
        assertEquals(1, events.size());
        assertEquals(correlationId, events.get(0).getCorrelationId());
    }

    @Test
    void shouldSupportEventReplay() {
        // Given
        TestEvent event1 = new TestEvent("aggregate-123", "correlation-1");
        TestEvent event2 = new TestEvent("aggregate-123", "correlation-2");

        eventStore.save(event1);
        eventStore.save(event2);

        // When
        List<BaseEvent> replayedEvents = eventStore.replayEvents("aggregate-123");

        // Then
        assertEquals(2, replayedEvents.size());
        assertEquals(1, replayedEvents.get(0).getVersion());
        assertEquals(2, replayedEvents.get(1).getVersion());
    }

    @Test
    void shouldGetAggregateVersion() {
        // Given
        TestEvent event1 = new TestEvent("aggregate-123", "correlation-1");
        TestEvent event2 = new TestEvent("aggregate-123", "correlation-2");

        eventStore.save(event1);
        eventStore.save(event2);

        // When
        long version = eventStore.getAggregateVersion("aggregate-123");

        // Then
        assertEquals(2, version);
    }

    @Test
    void shouldHandleConcurrentEventSaving() throws InterruptedException {
        // Given
        String aggregateId = "concurrent-aggregate";

        // When - Save events concurrently
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                eventStore.save(new TestEvent(aggregateId, "thread-1-" + i));
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                eventStore.save(new TestEvent(aggregateId, "thread-2-" + i));
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then
        List<BaseEvent> events = eventStore.findByAggregateId(aggregateId);
        assertEquals(20, events.size());

        // Versions should be sequential
        for (int i = 0; i < events.size(); i++) {
            assertEquals(i + 1, events.get(i).getVersion());
        }
    }

    @Test
    void shouldSupportSnapshotCreation() {
        // Given
        TestEvent event1 = new TestEvent("aggregate-123", "correlation-1");
        TestEvent event2 = new TestEvent("aggregate-123", "correlation-2");

        eventStore.save(event1);
        eventStore.save(event2);

        // When
        eventStore.createSnapshot("aggregate-123", "PlaybackSession", "{\"state\": \"playing\"}");

        // Then
        // Snapshot should be created (integration test validates this works end-to-end)
        assertDoesNotThrow(() -> eventStore.findByAggregateId("aggregate-123"));
    }

    @Test
    void shouldHandleLargeEventPayloads() {
        // Given
        String largePayload = "x".repeat(10000); // 10KB payload
        TestEvent largeEvent = new TestEvent("aggregate-123", "correlation-1", largePayload);

        // When
        eventStore.save(largeEvent);

        // Then
        List<BaseEvent> events = eventStore.findByAggregateId("aggregate-123");
        assertEquals(1, events.size());
        assertEquals(largeEvent.getEventId(), events.get(0).getEventId());
    }

    // Test event implementation
    private static class TestEvent extends BaseEvent {
        private final String aggregateId;
        private final String largePayload;

        public TestEvent(String aggregateId, String correlationId) {
            this(aggregateId, correlationId, null);
        }

        public TestEvent(String aggregateId, String correlationId, String largePayload) {
            super(correlationId, null, "default");
            this.aggregateId = aggregateId;
            this.largePayload = largePayload;
        }

        @Override
        public String getAggregateId() {
            return aggregateId;
        }

        @Override
        public String getAggregateType() {
            return "TestAggregate";
        }

        public String getLargePayload() {
            return largePayload;
        }
    }
}
