package com.netflix.streaming.infrastructure.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Event-Driven Test Base Class.
 *
 * Provides comprehensive testing infrastructure for event-driven systems:
 * - Embedded Kafka for event bus testing
 * - PostgreSQL for event store testing
 * - Event capture and verification
 * - Async event testing utilities
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@EmbeddedKafka(partitions = 1,
               brokerProperties = {
                   "listeners=PLAINTEXT://localhost:9092",
                   "port=9092"
               })
public abstract class EventDrivenTestBase {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:15-alpine")
    ).withDatabaseName("testdb")
     .withUsername("test")
     .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );

    @Autowired
    protected EventPublisher eventPublisher;

    @Autowired
    protected ObjectMapper objectMapper;

    protected EventCapture eventCapture;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        eventCapture = new EventCapture();
    }

    /**
     * Event Capture Utility for testing event flows
     */
    protected static class EventCapture {
        private final List<BaseEvent> capturedEvents = new ArrayList<>();

        public void capture(BaseEvent event) {
            synchronized (capturedEvents) {
                capturedEvents.add(event);
                capturedEvents.notifyAll();
            }
        }

        public List<BaseEvent> getCapturedEvents() {
            synchronized (capturedEvents) {
                return new ArrayList<>(capturedEvents);
            }
        }

        public void clear() {
            synchronized (capturedEvents) {
                capturedEvents.clear();
            }
        }

        public boolean waitForEvents(int count, long timeoutMs) throws InterruptedException {
            long startTime = System.currentTimeMillis();
            synchronized (capturedEvents) {
                while (capturedEvents.size() < count) {
                    long remaining = timeoutMs - (System.currentTimeMillis() - startTime);
                    if (remaining <= 0) {
                        return false;
                    }
                    capturedEvents.wait(remaining);
                }
                return true;
            }
        }

        public BaseEvent waitForEvent(String eventType, long timeoutMs) throws InterruptedException {
            long startTime = System.currentTimeMillis();
            synchronized (capturedEvents) {
                while (true) {
                    for (BaseEvent event : capturedEvents) {
                        if (event.getEventType().equals(eventType)) {
                            return event;
                        }
                    }

                    long remaining = timeoutMs - (System.currentTimeMillis() - startTime);
                    if (remaining <= 0) {
                        return null;
                    }
                    capturedEvents.wait(Math.min(remaining, 100));
                }
            }
        }
    }

    /**
     * Test Event Consumer for capturing events in tests
     */
    protected class TestEventConsumer {
        private final EventCapture capture;

        public TestEventConsumer(EventCapture capture) {
            this.capture = capture;
        }

        public void consume(BaseEvent event) {
            capture.capture(event);
        }
    }

    /**
     * Assertion utilities for event testing
     */
    protected static class EventAssertions {

        public static void assertEventPublished(EventCapture capture, String eventType, long timeoutMs) {
            try {
                BaseEvent event = capture.waitForEvent(eventType, timeoutMs);
                if (event == null) {
                    throw new AssertionError("Expected event " + eventType + " was not published within " + timeoutMs + "ms");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted while waiting for event " + eventType, e);
            }
        }

        public static void assertEventSequence(EventCapture capture, String... eventTypes) {
            List<BaseEvent> events = capture.getCapturedEvents();
            if (events.size() < eventTypes.length) {
                throw new AssertionError("Expected " + eventTypes.length + " events, but got " + events.size());
            }

            for (int i = 0; i < eventTypes.length; i++) {
                if (!events.get(i).getEventType().equals(eventTypes[i])) {
                    throw new AssertionError("Expected event " + eventTypes[i] + " at position " + i +
                                           ", but got " + events.get(i).getEventType());
                }
            }
        }

        public static void assertCorrelationIdPropagated(BaseEvent sourceEvent, BaseEvent targetEvent) {
            if (!sourceEvent.getCorrelationId().equals(targetEvent.getCorrelationId())) {
                throw new AssertionError("Correlation ID not propagated: expected " +
                                       sourceEvent.getCorrelationId() + ", got " + targetEvent.getCorrelationId());
            }
        }

        public static void assertCausationChain(BaseEvent cause, BaseEvent effect) {
            if (!cause.getEventId().equals(effect.getCausationId())) {
                throw new AssertionError("Causation chain broken: " + cause.getEventId() +
                                       " should cause " + effect.getEventId());
            }
        }
    }
}