package com.netflix.streaming.infrastructure.kafka;

import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.EventPublisher;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Event Bus Configuration for EDA.
 * Provides reliable, observable event publishing and consumption.
 */
@Configuration
@EnableKafka
public class KafkaEventBusConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventBusConfig.class);

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.schema-registry-url:http://localhost:8081}")
    private String schemaRegistryUrl;

    @Value("${app.kafka.event-bus.retention-hours:168}")
    private int retentionHours;

    @Value("${app.kafka.event-bus.partitions:6}")
    private int defaultPartitions;

    @Value("${app.kafka.event-bus.replication-factor:3}")
    private short replicationFactor;

    // OpenTelemetry (injected via Spring)
    private final Tracer tracer;
    private final TextMapPropagator propagator;

    public KafkaEventBusConfig(Tracer tracer, TextMapPropagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    /**
     * Kafka Admin Client for topic management
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Pre-create essential event bus topics
     */
    @Bean
    public KafkaAdmin.NewTopics eventBusTopics() {
        return new KafkaAdmin.NewTopics(
            // Core playback events
            createEventTopic("playback.events", "Raw playback telemetry events"),
            createEventTopic("playback.commands", "Playback control commands"),

            // Analytics events
            createEventTopic("analytics.events", "Calculated analytics events"),
            createEventTopic("analytics.commands", "Analytics processing commands"),

            // ML Pipeline events
            createEventTopic("ml.events", "ML pipeline events"),
            createEventTopic("ml.commands", "ML processing commands"),

            // Dashboard events
            createEventTopic("dashboard.events", "Real-time dashboard events"),

            // System events
            createEventTopic("system.events", "System health and monitoring events"),

            // Dead letter queues
            createEventTopic("playback.events.dlq", "Failed playback events"),
            createEventTopic("analytics.events.dlq", "Failed analytics events"),
            createEventTopic("ml.events.dlq", "Failed ML events")
        );
    }

    private NewTopic createEventTopic(String name, String description) {
        logger.info("Creating event topic: {} - {}", name, description);
        return NewTopic.builder()
            .name(name)
            .partitions(defaultPartitions)
            .replicas(replicationFactor)
            .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(retentionHours * 60 * 60 * 1000L))
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "lz4")
            .build();
    }

    /**
     * Producer Factory for reliable event publishing
     */
    @Bean
    public ProducerFactory<String, BaseEvent> eventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Performance tuning
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

        // Observability
        configProps.put("client.id", "event-bus-producer");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka Template for event publishing
     */
    @Bean
    public KafkaTemplate<String, BaseEvent> eventKafkaTemplate() {
        return new KafkaTemplate<>(eventProducerFactory());
    }

    /**
     * Kafka Template for DLQ (uses Object for flexibility)
     */
    @Bean
    public KafkaTemplate<String, Object> dlqKafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }

    /**
     * Event Publisher implementation using Kafka
     */
    @Bean
    public EventPublisher kafkaEventPublisher(KafkaTemplate<String, BaseEvent> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate, tracer, propagator);
    }

    /**
     * Kafka Template for DLQ (uses Object for flexibility)
     */
    @Bean
    public KafkaTemplate<String, Object> dlqKafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }

    /**
     * Consumer Factory for reliable event consumption
     */
    @Bean
    public ConsumerFactory<String, BaseEvent> eventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BaseEvent.class.getPackageName() + ".BaseEvent");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Reliability settings
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);

        // Performance tuning
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

        // Observability
        props.put("client.id", "event-bus-consumer");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Kafka Listener Container Factory with error handling
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BaseEvent> eventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BaseEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventConsumerFactory());

        // Manual acknowledgment for reliability
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Error handling with retry and DLQ
        // Note: KafkaErrorHandler is injected and provides DLQ functionality
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            (consumerRecord, exception) -> {
                logger.error("Error processing event: {}", consumerRecord.value(), exception);
                // DLQ handling is done by KafkaErrorHandler
            },
            new ExponentialBackOffPolicy()
        );

        // Don't retry certain exceptions
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        errorHandler.addNotRetryableExceptions(
            org.apache.kafka.common.errors.RecordTooLargeException.class,
            org.apache.kafka.common.errors.SerializationException.class
        );

        // Add retryable exceptions
        errorHandler.addRetryableExceptions(
            org.springframework.kafka.KafkaException.class,
            java.net.SocketTimeoutException.class
        );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    /**
     * Retry template for transient failures
     */
    @Bean
    public RetryTemplate eventRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(100);
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(1000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}