package com.netflix.streaming.infrastructure.kafka;

import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.EventPublisher;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka-based implementation of EventPublisher.
 * Provides reliable, observable event publishing with tracing and error handling.
 */
@Component
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;
    private final Tracer tracer;
    private final TextMapPropagator propagator;

    public KafkaEventPublisher(KafkaTemplate<String, BaseEvent> kafkaTemplate,
                              Tracer tracer,
                              TextMapPropagator propagator) {
        this.kafkaTemplate = kafkaTemplate;
        this.tracer = tracer;
        this.propagator = propagator;
    }

    @Override
    public void publish(BaseEvent event) throws EventPublishingException {
        publish(event, EventMetadata.builder().build());
    }

    @Override
    public void publish(BaseEvent event, EventMetadata metadata) throws EventPublishingException {
        if (event == null) {
            throw new EventPublishingException("Cannot publish null event");
        }

        Span span = tracer.spanBuilder("event.publish")
            .setAttribute("event.type", event.getEventType())
            .setAttribute("event.id", event.getEventId())
            .setAttribute("correlation.id", event.getCorrelationId())
            .setAttribute("aggregate.id", event.getAggregateId())
            .setAttribute("aggregate.type", event.getAggregateType())
            .startSpan();

        try (var scope = span.makeCurrent()) {
            String topic = determineTopic(event);
            String key = determineKey(event);

            logger.debug("Publishing event: {} to topic: {} with key: {}",
                event, topic, key);

            // Add tracing headers
            var headers = kafkaTemplate.getMessageConverter()
                .toMessageHeaders(event, null);

            // Propagate tracing context
            propagator.inject(Context.current(), headers, (carrier, key, value) -> {
                if (carrier instanceof org.springframework.messaging.MessageHeaders) {
                    ((org.springframework.messaging.MessageHeaders) carrier).put(key, value);
                }
            });

            // Publish event
            CompletableFuture<SendResult<String, BaseEvent>> future =
                kafkaTemplate.send(topic, key, event);

            // Handle result
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    span.setStatus(StatusCode.ERROR, throwable.getMessage());
                    span.recordException(throwable);
                    logger.error("Failed to publish event: {}", event, throwable);
                } else {
                    span.setAttribute("kafka.topic", result.getRecordMetadata().topic());
                    span.setAttribute("kafka.partition", result.getRecordMetadata().partition());
                    span.setAttribute("kafka.offset", result.getRecordMetadata().offset());
                    logger.debug("Successfully published event: {} to {}-{} at offset {}",
                        event.getEventId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });

            // Wait for completion (could be made async)
            future.join();
            span.setStatus(StatusCode.OK);

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw new EventPublishingException("Failed to publish event: " + event, e);
        } finally {
            span.end();
        }
    }

    @Override
    public void publishBatch(Iterable<BaseEvent> events) throws EventPublishingException {
        for (BaseEvent event : events) {
            publish(event); // Could be optimized for batch publishing
        }
    }

    /**
     * Determines the Kafka topic for an event based on its type and aggregate.
     */
    private String determineTopic(BaseEvent event) {
        String eventType = event.getEventType().toLowerCase();
        String aggregateType = event.getAggregateType().toLowerCase();

        // Topic naming convention: {domain}.{events|commands}
        if (aggregateType.contains("playback") || aggregateType.contains("session")) {
            return "playback.events";
        } else if (aggregateType.contains("analytics") || aggregateType.contains("performance")) {
            return "analytics.events";
        } else if (aggregateType.contains("ml") || aggregateType.contains("model")) {
            return "ml.events";
        } else if (aggregateType.contains("dashboard")) {
            return "dashboard.events";
        } else {
            return "system.events";
        }
    }

    /**
     * Determines the partition key for an event (for ordering within partitions).
     */
    private String determineKey(BaseEvent event) {
        // Use aggregate ID for partitioning to ensure order
        String key = event.getAggregateId();

        // For events that don't have a natural aggregate ID, use correlation ID
        if (!StringUtils.hasText(key)) {
            key = event.getCorrelationId();
        }

        return key;
    }
}