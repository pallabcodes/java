package com.netflix.streaming.playback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.streaming.events.EventPublisher;
import com.netflix.streaming.infrastructure.store.EventStore;
import com.netflix.streaming.playback.outbox.PlaybackOutboxService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.GenericAggregateFactory;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.modelling.command.GenericAggregateRepository;
import org.axonframework.modelling.command.Repository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Event-Sourced Playback Service Application.
 *
 * This service demonstrates:
 * - Event Sourcing: PlaybackSession aggregate built from events
 * - CQRS: Separate command and query models
 * - Transactional Outbox: Reliable event publishing
 * - Event-Driven Architecture: Integration via domain events
 *
 * Architecture:
 * - Commands → Aggregates → Events → Outbox → Kafka → Projections
 */
@SpringBootApplication
@EnableScheduling
public class PlaybackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaybackApplication.class, args);
    }

    /**
     * OpenTelemetry Tracer for distributed tracing
     */
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("playback-service", "1.0.0");
    }

    /**
     * OpenTelemetry TextMapPropagator for context propagation
     */
    @Bean
    public TextMapPropagator textMapPropagator(OpenTelemetry openTelemetry) {
        return openTelemetry.getPropagators().getTextMapPropagator();
    }

    /**
     * Scheduled task to process outbox events
     */
    @Bean
    public Runnable outboxProcessor(PlaybackOutboxService outboxService) {
        return () -> {
            try {
                outboxService.processOutbox();
            } catch (Exception e) {
                // Log error but don't propagate to avoid scheduler shutdown
                System.err.println("Error processing outbox: " + e.getMessage());
            }
        };
    }
}

// Configuration class for Axon Framework
@Configuration
class AxonConfig {

    @Bean
    public AggregateFactory<com.netflix.streaming.playback.aggregate.PlaybackSession> playbackSessionAggregateFactory() {
        return new GenericAggregateFactory<>(com.netflix.streaming.playback.aggregate.PlaybackSession.class);
    }

    @Bean
    public Repository<com.netflix.streaming.playback.aggregate.PlaybackSession> playbackSessionRepository(
            EventStore eventStore,
            AggregateFactory<com.netflix.streaming.playback.aggregate.PlaybackSession> aggregateFactory) {
        return GenericAggregateRepository.builder(com.netflix.streaming.playback.aggregate.PlaybackSession.class)
                .eventStore(eventStore)
                .aggregateFactory(aggregateFactory)
                .build();
    }
}

// Configuration for outbox processing
@Configuration
class OutboxConfig {

    @Bean
    public PlaybackOutboxService playbackOutboxService(
            JdbcTemplate jdbcTemplate,
            EventPublisher eventPublisher,
            ObjectMapper objectMapper) {
        return new PlaybackOutboxService(jdbcTemplate, eventPublisher, objectMapper);
    }
}