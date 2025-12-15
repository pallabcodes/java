package com.netflix.streaming.infrastructure.kafka;

import com.netflix.streaming.events.BaseEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Kafka Batch Processor for efficient batch processing.
 * 
 * Processes multiple Kafka messages in a single transaction,
 * improving throughput and reducing database round trips.
 */
@Component
public class KafkaBatchProcessor implements BatchMessageListener<String, BaseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaBatchProcessor.class);
    private static final int DEFAULT_BATCH_SIZE = 100;

    private final Consumer<List<ConsumerRecord<String, BaseEvent>>> batchHandler;
    private final KafkaMetricsService metricsService;

    public KafkaBatchProcessor(Consumer<List<ConsumerRecord<String, BaseEvent>>> batchHandler,
                              KafkaMetricsService metricsService) {
        this.batchHandler = batchHandler;
        this.metricsService = metricsService;
    }

    @Override
    public void onMessage(List<ConsumerRecord<String, BaseEvent>> records, Acknowledgment acknowledgment) {
        if (records == null || records.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        String topic = records.get(0).topic();

        try {
            logger.debug("Processing batch of {} messages from topic: {}", records.size(), topic);

            // Process batch
            batchHandler.accept(records);

            // Record metrics
            long processingTime = System.currentTimeMillis() - startTime;
            for (ConsumerRecord<String, BaseEvent> record : records) {
                metricsService.recordMessageConsumed(record, processingTime / records.size());
            }

            // Acknowledge entire batch
            acknowledgment.acknowledge();

            logger.debug("Successfully processed batch of {} messages in {}ms",
                records.size(), processingTime);

        } catch (Exception e) {
            logger.error("Failed to process batch of {} messages from topic: {}",
                records.size(), topic, e);

            // Record failure metrics
            for (ConsumerRecord<String, BaseEvent> record : records) {
                metricsService.recordMessageConsumeFailed(record, e.getClass().getName());
            }

            // Don't acknowledge - will be retried or sent to DLQ
            throw new RuntimeException("Batch processing failed", e);
        }
    }

    /**
     * Create a batch processor with custom batch handler.
     */
    public static KafkaBatchProcessor create(Consumer<List<ConsumerRecord<String, BaseEvent>>> handler,
                                            KafkaMetricsService metricsService) {
        return new KafkaBatchProcessor(handler, metricsService);
    }
}

