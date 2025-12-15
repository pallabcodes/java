package com.netflix.streaming.infrastructure.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Kafka Metrics Service.
 * 
 * Provides comprehensive metrics for Kafka producers and consumers:
 * - Message counts (sent, received, failed)
 * - Processing times
 * - Error rates
 * - Topic-level metrics
 */
@Service
public class KafkaMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMetricsService.class);

    private final MeterRegistry meterRegistry;

    public KafkaMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record a successfully published message.
     */
    public void recordMessagePublished(String topic, String key, RecordMetadata metadata) {
        Counter.builder("kafka.producer.messages.published")
            .tag("topic", topic)
            .tag("partition", String.valueOf(metadata.partition()))
            .description("Number of messages successfully published to Kafka")
            .register(meterRegistry)
            .increment();

        logger.debug("Recorded published message: topic={}, partition={}, offset={}",
            topic, metadata.partition(), metadata.offset());
    }

    /**
     * Record a failed message publication.
     */
    public void recordMessagePublishFailed(String topic, String errorType) {
        Counter.builder("kafka.producer.messages.failed")
            .tag("topic", topic)
            .tag("error.type", errorType)
            .description("Number of failed message publications")
            .register(meterRegistry)
            .increment();

        logger.debug("Recorded failed message publication: topic={}, errorType={}", topic, errorType);
    }

    /**
     * Record a successfully consumed message.
     */
    public void recordMessageConsumed(ConsumerRecord<?, ?> record, long processingTimeMs) {
        Counter.builder("kafka.consumer.messages.consumed")
            .tag("topic", record.topic())
            .tag("partition", String.valueOf(record.partition()))
            .description("Number of messages successfully consumed from Kafka")
            .register(meterRegistry)
            .increment();

        Timer.builder("kafka.consumer.processing.time")
            .tag("topic", record.topic())
            .tag("partition", String.valueOf(record.partition()))
            .description("Time taken to process Kafka messages")
            .register(meterRegistry)
            .record(processingTimeMs, TimeUnit.MILLISECONDS);

        logger.debug("Recorded consumed message: topic={}, partition={}, processingTime={}ms",
            record.topic(), record.partition(), processingTimeMs);
    }

    /**
     * Record a failed message consumption.
     */
    public void recordMessageConsumeFailed(ConsumerRecord<?, ?> record, String errorType) {
        Counter.builder("kafka.consumer.messages.failed")
            .tag("topic", record.topic())
            .tag("partition", String.valueOf(record.partition()))
            .tag("error.type", errorType)
            .description("Number of failed message consumptions")
            .register(meterRegistry)
            .increment();

        logger.debug("Recorded failed message consumption: topic={}, partition={}, errorType={}",
            record.topic(), record.partition(), errorType);
    }

    /**
     * Record a message sent to DLQ.
     */
    public void recordDlqMessage(String originalTopic, String errorType, int retryCount) {
        Counter.builder("kafka.dlq.messages")
            .tag("original.topic", originalTopic)
            .tag("error.type", errorType)
            .tag("retry.count", String.valueOf(retryCount))
            .description("Number of messages sent to Dead Letter Queue")
            .register(meterRegistry)
            .increment();

        logger.debug("Recorded DLQ message: originalTopic={}, errorType={}, retryCount={}",
            originalTopic, errorType, retryCount);
    }

    /**
     * Record consumer lag.
     */
    public void recordConsumerLag(String consumerGroup, String topic, int partition, long lag) {
        meterRegistry.gauge("kafka.consumer.lag",
            java.util.Arrays.asList(
                "consumer.group", consumerGroup,
                "topic", topic,
                "partition", String.valueOf(partition)
            ),
            lag);

        logger.debug("Recorded consumer lag: group={}, topic={}, partition={}, lag={}",
            consumerGroup, topic, partition, lag);
    }

    /**
     * Record producer batch size.
     */
    public void recordProducerBatchSize(String topic, int batchSize) {
        meterRegistry.gauge("kafka.producer.batch.size",
            java.util.Arrays.asList("topic", topic),
            batchSize);
    }

    /**
     * Record consumer poll time.
     */
    public void recordConsumerPollTime(String consumerGroup, long pollTimeMs) {
        Timer.builder("kafka.consumer.poll.time")
            .tag("consumer.group", consumerGroup)
            .description("Time taken to poll messages from Kafka")
            .register(meterRegistry)
            .record(pollTimeMs, TimeUnit.MILLISECONDS);
    }
}

