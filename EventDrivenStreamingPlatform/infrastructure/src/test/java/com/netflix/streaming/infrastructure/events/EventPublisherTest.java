package com.netflix.streaming.infrastructure.events;

import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.EventPublisher;
import com.netflix.streaming.infrastructure.kafka.KafkaEventPublisher;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @Mock
    private Tracer tracer;

    @Mock
    private TextMapPropagator propagator;

    @Mock
    private CompletableFuture<SendResult<String, BaseEvent>> future;

    private EventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        eventPublisher = new KafkaEventPublisher(kafkaTemplate, tracer, propagator);
    }

    @Test
    void shouldPublishEventSuccessfully() {
        // Given
        TestEvent testEvent = new TestEvent("test-aggregate-id", "correlation-id");
        when(kafkaTemplate.send(any(), any(), eq(testEvent))).thenReturn(future);
        when(future.whenComplete(any())).thenReturn(future);
        when(future.join()).thenReturn(null);

        // When
        eventPublisher.publish(testEvent);

        // Then
        verify(kafkaTemplate).send(eq("system.events"), eq("test-aggregate-id"), eq(testEvent));
        verify(future).join();
    }

    @Test
    void shouldPublishBatchEventsSuccessfully() {
        // Given
        TestEvent event1 = new TestEvent("aggregate-1", "correlation-1");
        TestEvent event2 = new TestEvent("aggregate-2", "correlation-2");
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);
        when(future.whenComplete(any())).thenReturn(future);
        when(future.join()).thenReturn(null);

        // When
        eventPublisher.publishBatch(java.util.List.of(event1, event2));

        // Then
        verify(kafkaTemplate, times(2)).send(any(), any(), any());
    }

    @Test
    void shouldThrowExceptionForNullEvent() {
        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            eventPublisher.publish(null);
        });
    }

    // Test event implementation
    private static class TestEvent extends BaseEvent {
        public TestEvent(String correlationId, String causationId, String tenantId) {
            super(correlationId, causationId, tenantId);
        }

        public TestEvent(String correlationId, String causationId) {
            this(correlationId, causationId, "default");
        }

        @Override
        public String getAggregateId() {
            return "test-aggregate-id";
        }

        @Override
        public String getAggregateType() {
            return "TestAggregate";
        }
    }
}
