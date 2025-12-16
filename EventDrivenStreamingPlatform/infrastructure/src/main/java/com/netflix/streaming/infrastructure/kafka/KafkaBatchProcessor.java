package com.netflix.streaming.infrastructure.kafka;

import com.netflix.streaming.events.BaseEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        int batchSize = records.size();

        try {
            logger.debug("Processing batch of {} messages from topic: {}", batchSize, topic);

            // Validate batch before processing
            validateBatch(records);

            // Process batch with timeout protection
            processBatchWithTimeout(records, startTime);

            // Record success metrics
            long processingTime = System.currentTimeMillis() - startTime;
            recordBatchSuccessMetrics(records, processingTime);

            // Acknowledge entire batch
            acknowledgment.acknowledge();

            logger.info("Successfully processed batch of {} messages from topic {} in {}ms",
                batchSize, topic, processingTime);

        } catch (BatchValidationException e) {
            logger.warn("Batch validation failed for topic {}: {}", topic, e.getMessage());
            handleBatchValidationFailure(records, e);
            throw e;

        } catch (BatchProcessingTimeoutException e) {
            logger.error("Batch processing timeout for topic {} after {}ms", topic, e.getTimeoutMs());
            handleBatchTimeout(records, e);
            throw e;

        } catch (Exception e) {
            logger.error("Failed to process batch of {} messages from topic: {}",
                batchSize, topic, e);

            // Record failure metrics
            recordBatchFailureMetrics(records, e);

            // Attempt partial processing if configured
            if (shouldAttemptPartialProcessing(e)) {
                processBatchPartially(records, e);
            }

            // Don't acknowledge - will be retried or sent to DLQ
            throw new RuntimeException("Batch processing failed", e);
        }
    }

    /**
     * Validate batch before processing.
     */
    private void validateBatch(List<ConsumerRecord<String, BaseEvent>> records) {
        if (records.size() > DEFAULT_BATCH_SIZE * 2) {
            throw new BatchValidationException("Batch size " + records.size() + " exceeds maximum allowed " + DEFAULT_BATCH_SIZE * 2);
        }

        // Check for duplicate keys within batch
        Set<String> keys = new HashSet<>();
        for (ConsumerRecord<String, BaseEvent> record : records) {
            if (record.key() != null && !keys.add(record.key())) {
                logger.warn("Duplicate key found in batch: {}", record.key());
                // Continue processing but log warning
            }
        }
    }

    /**
     * Process batch with timeout protection.
     */
    private void processBatchWithTimeout(List<ConsumerRecord<String, BaseEvent>> records, long startTime)
            throws BatchProcessingTimeoutException {

        long timeoutMs = getBatchProcessingTimeoutMs();

        // Process in separate thread with timeout
        Thread processingThread = new Thread(() -> {
            try {
                batchHandler.accept(records);
            } catch (Exception e) {
                Thread.currentThread().interrupt(); // Signal timeout
            }
        });

        processingThread.start();

        try {
            processingThread.join(timeoutMs);
            if (processingThread.isAlive()) {
                processingThread.interrupt();
                throw new BatchProcessingTimeoutException(timeoutMs, "Batch processing timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BatchProcessingTimeoutException(timeoutMs, "Batch processing interrupted");
        }
    }

    /**
     * Record batch success metrics.
     */
    private void recordBatchSuccessMetrics(List<ConsumerRecord<String, BaseEvent>> records, long processingTime) {
        long avgProcessingTime = processingTime / records.size();

        for (ConsumerRecord<String, BaseEvent> record : records) {
            metricsService.recordMessageConsumed(record, avgProcessingTime);
        }

        // Record batch-level metrics
        recordBatchMetrics(records.get(0).topic(), records.size(), processingTime, true);
    }

    /**
     * Record batch failure metrics.
     */
    private void recordBatchFailureMetrics(List<ConsumerRecord<String, BaseEvent>> records, Exception e) {
        for (ConsumerRecord<String, BaseEvent> record : records) {
            metricsService.recordMessageConsumeFailed(record, e.getClass().getName());
        }

        // Record batch-level failure metrics
        recordBatchMetrics(records.get(0).topic(), records.size(), 0, false);
    }

    /**
     * Handle batch validation failure.
     */
    private void handleBatchValidationFailure(List<ConsumerRecord<String, BaseEvent>> records, BatchValidationException e) {
        logger.warn("Batch validation failed, skipping batch: {}", e.getMessage());
        // Could implement different strategies: skip, retry, or partial processing
    }

    /**
     * Handle batch timeout.
     */
    private void handleBatchTimeout(List<ConsumerRecord<String, BaseEvent>> records, BatchProcessingTimeoutException e) {
        logger.error("Batch processing timed out, attempting cleanup");
        // Could implement cleanup logic, partial commits, etc.
    }

    /**
     * Attempt partial processing for recoverable errors.
     */
    private void processBatchPartially(List<ConsumerRecord<String, BaseEvent>> records, Exception e) {
        logger.info("Attempting partial batch processing");

        // Try to process individual messages
        for (ConsumerRecord<String, BaseEvent> record : records) {
            try {
                // Process individual message
                List<ConsumerRecord<String, BaseEvent>> singleRecord = List.of(record);
                batchHandler.accept(singleRecord);
                metricsService.recordMessageConsumed(record, 0); // Individual processing time not tracked
                logger.debug("Successfully processed individual message from batch");
            } catch (Exception individualException) {
                logger.error("Failed to process individual message from batch: {}", record.key(), individualException);
                metricsService.recordMessageConsumeFailed(record, individualException.getClass().getName());
            }
        }
    }

    /**
     * Check if partial processing should be attempted.
     */
    private boolean shouldAttemptPartialProcessing(Exception e) {
        // Attempt partial processing for certain types of errors
        return e instanceof org.springframework.dao.DataIntegrityViolationException ||
               e instanceof org.springframework.dao.ConcurrencyFailureException;
    }

    /**
     * Get batch processing timeout.
     */
    private long getBatchProcessingTimeoutMs() {
        return Long.parseLong(System.getProperty("kafka.batch.processing.timeout.ms", "30000")); // 30 seconds default
    }

    /**
     * Record batch-level metrics.
     */
    private void recordBatchMetrics(String topic, int batchSize, long processingTime, boolean success) {
        // Additional batch metrics can be implemented here
        logger.debug("Batch metrics - topic: {}, size: {}, time: {}ms, success: {}", topic, batchSize, processingTime, success);
    }

    /**
     * Batch validation exception.
     */
    public static class BatchValidationException extends RuntimeException {
        public BatchValidationException(String message) {
            super(message);
        }
    }

    /**
     * Batch processing timeout exception.
     */
    public static class BatchProcessingTimeoutException extends RuntimeException {
        private final long timeoutMs;

        public BatchProcessingTimeoutException(long timeoutMs, String message) {
            super(message);
            this.timeoutMs = timeoutMs;
        }

        public long getTimeoutMs() {
            return timeoutMs;
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

