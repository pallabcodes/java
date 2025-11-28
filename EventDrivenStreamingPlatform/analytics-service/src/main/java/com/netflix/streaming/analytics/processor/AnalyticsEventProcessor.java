package com.netflix.streaming.analytics.processor;

import com.netflix.streaming.analytics.projection.ContentAnalyticsProjection;
import com.netflix.streaming.analytics.projection.UserAnalyticsProjection;
import com.netflix.streaming.events.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Analytics Event Processor.
 *
 * Consumes events from Kafka and updates analytics projections.
 * This demonstrates event-driven processing for real-time analytics.
 */
@Component
public class AnalyticsEventProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsEventProcessor.class);

    private final UserAnalyticsProjection userProjection;
    private final ContentAnalyticsProjection contentProjection;
    private final RealTimeAnalyticsProcessor realTimeProcessor;
    private final Tracer tracer;

    public AnalyticsEventProcessor(UserAnalyticsProjection userProjection,
                                  ContentAnalyticsProjection contentProjection,
                                  RealTimeAnalyticsProcessor realTimeProcessor,
                                  Tracer tracer) {
        this.userProjection = userProjection;
        this.contentProjection = contentProjection;
        this.realTimeProcessor = realTimeProcessor;
        this.tracer = tracer;
    }

    /**
     * Process playback events from Kafka
     */
    @KafkaListener(
        topics = "playback.events",
        groupId = "analytics-service-playback",
        containerFactory = "analyticsKafkaListenerContainerFactory"
    )
    public void processPlaybackEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        Span span = tracer.spanBuilder("analytics.process.playback")
            .setAttribute("event.type", event.getEventType())
            .setAttribute("event.id", event.getEventId())
            .setAttribute("correlation.id", event.getCorrelationId())
            .setAttribute("kafka.topic", topic)
            .setAttribute("kafka.partition", partition)
            .setAttribute("kafka.offset", offset)
            .startSpan();

        try {
            logger.debug("Processing playback event: {} from {}-{} at offset {}",
                        event.getEventId(), topic, partition, offset);

            // Route event to appropriate projection handler
            processEvent(event);

            // Update real-time metrics
            realTimeProcessor.processEvent(event);

            // Acknowledge successful processing
            acknowledgment.acknowledge();

            span.setStatus(StatusCode.OK);
            logger.debug("Successfully processed playback event: {}", event.getEventId());

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to process playback event: {}", event, e);

            // Don't acknowledge - will be retried or sent to DLQ
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Process analytics events (derived analytics)
     */
    @KafkaListener(
        topics = "analytics.events",
        groupId = "analytics-service-analytics",
        containerFactory = "analyticsKafkaListenerContainerFactory"
    )
    public void processAnalyticsEvents(
            @Payload BaseEvent event,
            Acknowledgment acknowledgment) {

        Span span = tracer.spanBuilder("analytics.process.analytics")
            .setAttribute("event.type", event.getEventType())
            .startSpan();

        try {
            logger.debug("Processing analytics event: {}", event);

            // Handle derived analytics events
            if (event instanceof ContentPerformanceCalculatedEvent) {
                handleContentPerformanceCalculated((ContentPerformanceCalculatedEvent) event);
            } else if (event instanceof UserEngagementCalculatedEvent) {
                handleUserEngagementCalculated((UserEngagementCalculatedEvent) event);
            }

            acknowledgment.acknowledge();
            span.setStatus(StatusCode.OK);

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to process analytics event: {}", event, e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Route event to appropriate projection handler
     */
    private void processEvent(BaseEvent event) {
        if (event instanceof PlaybackStartedEvent) {
            userProjection.handle((PlaybackStartedEvent) event);
            contentProjection.handle((PlaybackStartedEvent) event);

        } else if (event instanceof PlaybackCompletedEvent) {
            userProjection.handle((PlaybackCompletedEvent) event);
            contentProjection.handle((PlaybackCompletedEvent) event);

        } else if (event instanceof PlaybackPausedEvent) {
            userProjection.handle((PlaybackPausedEvent) event);

        } else if (event instanceof PlaybackResumedEvent) {
            userProjection.handle((PlaybackResumedEvent) event);

        } else if (event instanceof BufferingEvent) {
            contentProjection.handle((BufferingEvent) event);

        } else if (event instanceof QualityChangedEvent) {
            contentProjection.handle((QualityChangedEvent) event);
        }
    }

    private void handleContentPerformanceCalculated(ContentPerformanceCalculatedEvent event) {
        // Update content projection with calculated metrics
        // This could trigger additional analytics or alerts
        logger.info("Received calculated content performance for: {}", event.getContentId());
    }

    private void handleUserEngagementCalculated(UserEngagementCalculatedEvent event) {
        // Update user projection with calculated engagement metrics
        logger.info("Received calculated user engagement for: {}", event.getUserId());
    }
}