package org.example.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
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
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:producer-consumer-group}")
    private String consumerGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${spring.kafka.producer.acks:all}")
    private String acks;

    @Value("${spring.kafka.producer.retries:3}")
    private int producerRetries;

    @Value("${spring.kafka.consumer.max-poll-records:500}")
    private int maxPollRecords;

    @Value("${spring.kafka.consumer.fetch-min-size:1MB}")
    private String fetchMinSize;

    @Value("${spring.kafka.consumer.fetch-max-wait:500ms}")
    private String fetchMaxWait;

    private final MeterRegistry meterRegistry;

    public KafkaConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Connection settings
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Performance optimizations
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(ProducerConfig.RETRIES_CONFIG, producerRetries);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MAX_MS_CONFIG, 1000);

        // Batching optimizations
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5); // 5ms
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB

        // Compression
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");

        // Connection pooling
        configProps.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 540000); // 9 minutes
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Serialization
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Metrics
        configProps.put(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG, 3);

        logger.info("Configured Kafka producer with bootstrap servers: {}", bootstrapServers);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory());

        // Add metrics monitoring
        meterRegistry.gauge("kafka.producer.outgoing.byte.rate", template.metrics(),
                metrics -> getMetricValue(metrics, "outgoing-byte-rate", 0.0));
        meterRegistry.gauge("kafka.producer.request.rate", template.metrics(),
                metrics -> getMetricValue(metrics, "request-rate", 0.0));
        meterRegistry.gauge("kafka.producer.response.rate", template.metrics(),
                metrics -> getMetricValue(metrics, "response-rate", 0.0));

        return template;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Connection settings
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        // Performance optimizations
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commits for better control
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        // Batching and polling optimizations
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinSize.replace("MB", "000000").replace("KB", "000"));
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, Integer.parseInt(fetchMaxWait.replace("ms", "")));
        configProps.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1048576); // 1MB

        // Connection pooling
        configProps.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 540000);
        configProps.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 50);
        configProps.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 1000);

        // Heartbeat and session timeouts
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        configProps.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 40000);

        // Deserialization
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Metrics
        configProps.put(ConsumerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.METRICS_NUM_SAMPLES_CONFIG, 3);

        logger.info("Configured Kafka consumer with group ID: {} and bootstrap servers: {}", consumerGroupId, bootstrapServers);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Concurrency settings - adjust based on partition count and throughput needs
        factory.setConcurrency(Runtime.getRuntime().availableProcessors() / 2);

        // Container properties
        ContainerProperties containerProps = factory.getContainerProperties();
        containerProps.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        containerProps.setSyncCommits(false);
        containerProps.setCommitCallback((offsets, exception) -> {
            if (exception != null) {
                logger.error("Error during offset commit", exception);
            }
        });

        // Error handling
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new ExponentialBackOffWithMaxRetries(3).withInitialInterval(1000L).withMultiplier(2.0).withMaxInterval(10000L)
        );

        errorHandler.addRetryableExceptions(
                org.springframework.kafka.KafkaException.class,
                java.net.SocketTimeoutException.class
        );

        errorHandler.addNotRetryableExceptions(
                org.apache.kafka.common.errors.RecordTooLargeException.class,
                org.apache.kafka.common.errors.SerializationException.class
        );

        factory.setCommonErrorHandler(errorHandler);

        // Monitoring
        factory.setRecordInterceptor((record, consumer) -> {
            meterRegistry.counter("kafka.consumer.messages.received",
                    "topic", record.topic(),
                    "partition", String.valueOf(record.partition())).increment();

            return record;
        });

        return factory;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000L);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000L);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);

        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    private double getMetricValue(Map<String, ? extends org.apache.kafka.common.Metric> metrics, String metricName, double defaultValue) {
        org.apache.kafka.common.Metric metric = metrics.get(metricName);
        return metric != null ? (Double) metric.metricValue() : defaultValue;
    }
}
