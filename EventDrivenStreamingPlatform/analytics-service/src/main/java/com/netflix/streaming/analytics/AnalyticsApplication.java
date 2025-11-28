package com.netflix.streaming.analytics;

import com.netflix.streaming.analytics.processor.AnalyticsEventProcessor;
import com.netflix.streaming.analytics.projection.ContentAnalyticsProjection;
import com.netflix.streaming.analytics.projection.UserAnalyticsProjection;
import com.netflix.streaming.analytics.processor.RealTimeAnalyticsProcessor;
import com.netflix.streaming.analytics.query.AnalyticsQueryService;
import com.netflix.streaming.analytics.query.ContentAnalyticsRepository;
import com.netflix.streaming.analytics.query.RealTimeMetricsService;
import com.netflix.streaming.analytics.query.UserAnalyticsRepository;
import com.netflix.streaming.events.BaseEvent;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Real-Time Analytics Service Application.
 *
 * This service demonstrates:
 * - Event-Driven Projections: CQRS read models built from event streams
 * - Real-Time Analytics: Live processing of playback events
 * - WebSocket Streaming: Live dashboards with real-time updates
 * - Complex Event Processing: Analytics calculations and aggregations
 *
 * Architecture:
 * Kafka Events → Event Processors → Projections → WebSocket Clients
 */
@SpringBootApplication
@EnableKafka
public class AnalyticsApplication {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Netflix Streaming Analytics Service...");
        SpringApplication.run(AnalyticsApplication.class, args);
        logger.info("Analytics Service started successfully!");
    }

    /**
     * OpenTelemetry Tracer for distributed tracing
     */
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("analytics-service", "1.0.0");
    }

    /**
     * OpenTelemetry TextMapPropagator for context propagation
     */
    @Bean
    public TextMapPropagator textMapPropagator(OpenTelemetry openTelemetry) {
        return openTelemetry.getPropagators().getTextMapPropagator();
    }
}

// Configuration for analytics processing
@Configuration
class AnalyticsConfig {

    @Bean
    public AnalyticsEventProcessor analyticsEventProcessor(
            UserAnalyticsProjection userProjection,
            ContentAnalyticsProjection contentProjection,
            RealTimeAnalyticsProcessor realTimeProcessor) {
        return new AnalyticsEventProcessor(userProjection, contentProjection, realTimeProcessor);
    }

    @Bean
    public AnalyticsQueryService analyticsQueryService(
            UserAnalyticsRepository userRepo,
            ContentAnalyticsRepository contentRepo,
            RealTimeMetricsService metricsService) {
        return new AnalyticsQueryService(userRepo, contentRepo, metricsService);
    }
}

// Kafka Consumer Configuration
@Configuration
class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.group-id:analytics-service}")
    private String groupId;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BaseEvent> analyticsKafkaListenerContainerFactory(
            ConsumerFactory<String, BaseEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, BaseEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // Manual acknowledgment for reliability
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Error handling with retry and DLQ
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            (consumerRecord, exception) -> {
                logger.error("Error processing analytics event: {}", consumerRecord.value(), exception);
            },
            new FixedBackOff(1000L, 3L) // Retry 3 times with 1 second delay
        );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}