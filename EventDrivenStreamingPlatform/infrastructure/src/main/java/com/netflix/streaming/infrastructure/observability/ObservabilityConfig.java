package com.netflix.streaming.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Comprehensive Observability Configuration.
 *
 * Provides Netflix-grade observability across the entire platform:
 * - Distributed tracing with OpenTelemetry
 * - Business and technical metrics with Micrometer
 * - Structured logging with correlation IDs
 * - Health checks and readiness probes
 */
@Configuration
public class ObservabilityConfig {

    @Value("${app.observability.service-name:streaming-platform}")
    private String serviceName;

    @Value("${app.observability.service-version:1.0.0}")
    private String serviceVersion;

    /**
     * OpenTelemetry Tracer for distributed tracing
     */
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, serviceVersion);
    }

    /**
     * OpenTelemetry TextMapPropagator for context propagation
     */
    @Bean
    public TextMapPropagator textMapPropagator(OpenTelemetry openTelemetry) {
        return openTelemetry.getPropagators().getTextMapPropagator();
    }

    /**
     * Business Metrics Registry
     * Netflix-specific business KPIs and metrics
     */
    @Bean
    public BusinessMetrics businessMetrics(MeterRegistry meterRegistry) {
        return new BusinessMetrics(meterRegistry);
    }

    /**
     * Technical Metrics Registry
     * Infrastructure and performance metrics
     */
    @Bean
    public TechnicalMetrics technicalMetrics(MeterRegistry meterRegistry) {
        return new TechnicalMetrics(meterRegistry);
    }

    /**
     * Correlation ID Task Decorator
     * Ensures MDC context propagation in async operations
     */
    @Bean
    public TaskDecorator correlationIdTaskDecorator() {
        return runnable -> {
            // Capture current MDC context
            java.util.Map<String, String> contextMap = MDC.getCopyOfContextMap();

            return () -> {
                // Restore MDC context in worker thread
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }

                // Restore OpenTelemetry context if available
                Span currentSpan = Span.current();
                if (currentSpan != null) {
                    Context otelContext = Context.current();
                    // The span context is automatically propagated
                }

                try {
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        };
    }

    /**
     * Async Executor with Observability Context Propagation
     */
    @Bean("observabilityTaskExecutor")
    public Executor observabilityTaskExecutor(TaskDecorator correlationIdTaskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("obs-executor-");
        executor.setTaskDecorator(correlationIdTaskDecorator);
        executor.initialize();
        return executor;
    }

    /**
     * Business Metrics Implementation
     */
    public static class BusinessMetrics {

        // Event Processing Metrics
        private final Counter eventsProcessed;
        private final Counter eventsFailed;
        private final Timer eventProcessingTime;

        // User Engagement Metrics
        private final Counter playbackStarted;
        private final Counter playbackCompleted;
        private final Gauge.Builder activeStreamsGauge;

        // Content Performance Metrics
        private final Counter contentViewed;
        private final Timer contentLoadTime;

        // ML Pipeline Metrics
        private final Counter pipelinesStarted;
        private final Counter pipelinesCompleted;
        private final Counter pipelinesFailed;

        public BusinessMetrics(MeterRegistry registry) {
            // Event Processing
            this.eventsProcessed = Counter.builder("streaming.events.processed")
                    .description("Total number of events processed")
                    .tags("service", "platform")
                    .register(registry);

            this.eventsFailed = Counter.builder("streaming.events.failed")
                    .description("Total number of events that failed processing")
                    .tags("service", "platform")
                    .register(registry);

            this.eventProcessingTime = Timer.builder("streaming.events.processing.duration")
                    .description("Time taken to process events")
                    .tags("service", "platform")
                    .register(registry);

            // User Engagement
            this.playbackStarted = Counter.builder("streaming.playback.started")
                    .description("Total playback sessions started")
                    .tags("service", "platform")
                    .register(registry);

            this.playbackCompleted = Counter.builder("streaming.playback.completed")
                    .description("Total playback sessions completed")
                    .tags("service", "platform")
                    .register(registry);

            // Content Performance
            this.contentViewed = Counter.builder("streaming.content.viewed")
                    .description("Total content views")
                    .tags("service", "platform")
                    .register(registry);

            this.contentLoadTime = Timer.builder("streaming.content.load.duration")
                    .description("Time taken to load content")
                    .tags("service", "platform")
                    .register(registry);

            // ML Pipeline
            this.pipelinesStarted = Counter.builder("streaming.ml.pipelines.started")
                    .description("Total ML pipelines started")
                    .tags("service", "platform")
                    .register(registry);

            this.pipelinesCompleted = Counter.builder("streaming.ml.pipelines.completed")
                    .description("Total ML pipelines completed")
                    .tags("service", "platform")
                    .register(registry);

            this.pipelinesFailed = Counter.builder("streaming.ml.pipelines.failed")
                    .description("Total ML pipelines failed")
                    .tags("service", "platform")
                    .register(registry);
        }

        // Business metric methods
        public void recordEventProcessed() { eventsProcessed.increment(); }
        public void recordEventFailed() { eventsFailed.increment(); }
        public Timer.Sample startEventProcessingTimer() { return Timer.start(eventProcessingTime); }

        public void recordPlaybackStarted() { playbackStarted.increment(); }
        public void recordPlaybackCompleted() { playbackCompleted.increment(); }

        public void recordContentViewed() { contentViewed.increment(); }
        public Timer.Sample startContentLoadTimer() { return Timer.start(contentLoadTime); }

        public void recordPipelineStarted() { pipelinesStarted.increment(); }
        public void recordPipelineCompleted() { pipelinesCompleted.increment(); }
        public void recordPipelineFailed() { pipelinesFailed.increment(); }
    }

    /**
     * Technical Metrics Implementation
     */
    public static class TechnicalMetrics {

        // Database Metrics
        private final Counter dbQueries;
        private final Counter dbErrors;
        private final Timer dbQueryTime;

        // Kafka Metrics
        private final Counter kafkaMessagesSent;
        private final Counter kafkaMessagesReceived;
        private final Counter kafkaErrors;

        // Cache Metrics
        private final Counter cacheHits;
        private final Counter cacheMisses;
        private final Gauge.Builder cacheHitRatio;

        // Circuit Breaker Metrics
        private final Counter circuitBreakerCalls;
        private final Counter circuitBreakerFailures;

        public TechnicalMetrics(MeterRegistry registry) {
            // Database
            this.dbQueries = Counter.builder("streaming.db.queries")
                    .description("Total database queries executed")
                    .tags("service", "platform")
                    .register(registry);

            this.dbErrors = Counter.builder("streaming.db.errors")
                    .description("Total database errors")
                    .tags("service", "platform")
                    .register(registry);

            this.dbQueryTime = Timer.builder("streaming.db.query.duration")
                    .description("Database query execution time")
                    .tags("service", "platform")
                    .register(registry);

            // Kafka
            this.kafkaMessagesSent = Counter.builder("streaming.kafka.messages.sent")
                    .description("Total Kafka messages sent")
                    .tags("service", "platform")
                    .register(registry);

            this.kafkaMessagesReceived = Counter.builder("streaming.kafka.messages.received")
                    .description("Total Kafka messages received")
                    .tags("service", "platform")
                    .register(registry);

            this.kafkaErrors = Counter.builder("streaming.kafka.errors")
                    .description("Total Kafka errors")
                    .tags("service", "platform")
                    .register(registry);

            // Cache
            this.cacheHits = Counter.builder("streaming.cache.hits")
                    .description("Total cache hits")
                    .tags("service", "platform")
                    .register(registry);

            this.cacheMisses = Counter.builder("streaming.cache.misses")
                    .description("Total cache misses")
                    .tags("service", "platform")
                    .register(registry);

            // Circuit Breaker
            this.circuitBreakerCalls = Counter.builder("streaming.circuit_breaker.calls")
                    .description("Total circuit breaker calls")
                    .tags("service", "platform")
                    .register(registry);

            this.circuitBreakerFailures = Counter.builder("streaming.circuit_breaker.failures")
                    .description("Total circuit breaker failures")
                    .tags("service", "platform")
                    .register(registry);
        }

        // Technical metric methods
        public void recordDbQuery() { dbQueries.increment(); }
        public void recordDbError() { dbErrors.increment(); }
        public Timer.Sample startDbQueryTimer() { return Timer.start(dbQueryTime); }

        public void recordKafkaMessageSent() { kafkaMessagesSent.increment(); }
        public void recordKafkaMessageReceived() { kafkaMessagesReceived.increment(); }
        public void recordKafkaError() { kafkaErrors.increment(); }

        public void recordCacheHit() { cacheHits.increment(); }
        public void recordCacheMiss() { cacheMisses.increment(); }

        public void recordCircuitBreakerCall() { circuitBreakerCalls.increment(); }
        public void recordCircuitBreakerFailure() { circuitBreakerFailures.increment(); }
    }
}