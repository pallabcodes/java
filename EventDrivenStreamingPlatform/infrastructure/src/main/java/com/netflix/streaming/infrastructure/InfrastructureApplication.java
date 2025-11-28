package com.netflix.streaming.infrastructure;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Event-Driven Streaming Infrastructure Application.
 *
 * Provides the foundational event infrastructure for the EDA platform:
 * - Kafka event bus with schema registry
 * - PostgreSQL event store with replay capabilities
 * - OpenTelemetry observability
 * - Resilience patterns
 *
 * This service acts as the backbone for event-driven communication
 * between all microservices in the platform.
 */
@SpringBootApplication
public class InfrastructureApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfrastructureApplication.class, args);
    }

    /**
     * OpenTelemetry Tracer for distributed tracing
     */
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("event-infrastructure", "1.0.0");
    }

    /**
     * OpenTelemetry TextMapPropagator for context propagation
     */
    @Bean
    public TextMapPropagator textMapPropagator(OpenTelemetry openTelemetry) {
        return openTelemetry.getPropagators().getTextMapPropagator();
    }
}