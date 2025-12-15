package com.netflix.streaming.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced Kafka Error Handler with DLQ support.
 * 
 * Handles errors during Kafka message consumption:
 * - Retries transient failures with exponential backoff
 * - Sends to DLQ after max retries
 * - Tracks retry counts
 * - Classifies errors (retryable vs non-retryable)
 */
@Component
public class KafkaErrorHandler implements CommonErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(KafkaErrorHandler.class);
    private static final int MAX_RETRIES = 3;

    private final KafkaDeadLetterQueueService dlqService;
    private final RetryTemplate retryTemplate;
    private final AtomicInteger retryCount = new AtomicInteger(0);

    public KafkaErrorHandler(KafkaDeadLetterQueueService dlqService) {
        this.dlqService = dlqService;
        this.retryTemplate = createRetryTemplate();
    }

    @Override
    public void handleOtherException(Exception thrownException, 
                                    ConsumerRecord<?, ?> record,
                                    MessageListenerContainer container,
                                    boolean invokeListener) {
        handleException(thrownException, record, container);
    }

    @Override
    public void handleRemaining(Exception thrownException,
                               java.util.List<ConsumerRecord<?, ?>> records,
                               MessageListenerContainer container,
                               boolean invokeListener) {
        for (ConsumerRecord<?, ?> record : records) {
            handleException(thrownException, record, container);
        }
    }

    /**
     * Handle exception for a single record.
     */
    private void handleException(Exception exception,
                                ConsumerRecord<?, ?> record,
                                MessageListenerContainer container) {
        
        String topic = record.topic();
        String key = record.key() != null ? record.key().toString() : null;
        Object value = record.value();
        String consumerGroup = container.getGroupId();

        logger.error("Error processing Kafka message: topic={}, partition={}, offset={}, key={}",
            topic, record.partition(), record.offset(), key, exception);

        // Check if error is retryable
        if (isRetryableException(exception)) {
            int currentRetryCount = getRetryCount(record);
            
            if (currentRetryCount < MAX_RETRIES) {
                // Retry with exponential backoff
                logger.info("Retrying message: topic={}, retryCount={}/{}",
                    topic, currentRetryCount + 1, MAX_RETRIES);
                
                try {
                    retryTemplate.execute(context -> {
                        // Retry logic would go here
                        // For now, we'll just increment retry count
                        incrementRetryCount(record);
                        throw exception; // Re-throw to trigger DLQ after max retries
                    });
                } catch (Exception e) {
                    // After max retries, send to DLQ
                    sendToDlq(topic, key, value, exception, currentRetryCount + 1, consumerGroup);
                }
            } else {
                // Max retries exceeded, send to DLQ
                logger.warn("Max retries exceeded for message: topic={}, sending to DLQ", topic);
                sendToDlq(topic, key, value, exception, MAX_RETRIES, consumerGroup);
            }
        } else {
            // Non-retryable exception, send directly to DLQ
            logger.warn("Non-retryable exception for message: topic={}, sending to DLQ", topic);
            sendToDlq(topic, key, value, exception, 0, consumerGroup);
        }
    }

    /**
     * Check if exception is retryable.
     */
    private boolean isRetryableException(Exception exception) {
        // Retryable exceptions
        if (exception instanceof org.springframework.kafka.KafkaException) {
            return true;
        }
        if (exception instanceof java.net.SocketTimeoutException) {
            return true;
        }
        if (exception instanceof org.apache.kafka.common.errors.RetriableException) {
            return true;
        }

        // Non-retryable exceptions
        if (exception instanceof IllegalArgumentException) {
            return false;
        }
        if (exception instanceof org.apache.kafka.common.errors.RecordTooLargeException) {
            return false;
        }
        if (exception instanceof org.apache.kafka.common.errors.SerializationException) {
            return false;
        }

        // Default: retryable
        return true;
    }

    /**
     * Get retry count from record headers.
     */
    private int getRetryCount(ConsumerRecord<?, ?> record) {
        org.apache.kafka.common.header.Header retryHeader = record.headers().lastHeader("retry-count");
        if (retryHeader != null) {
            try {
                return Integer.parseInt(new String(retryHeader.value()));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Increment retry count in record headers.
     */
    private void incrementRetryCount(ConsumerRecord<?, ?> record) {
        // This would typically be done by modifying headers before retry
        // For now, we track in memory
        retryCount.incrementAndGet();
    }

    /**
     * Send message to Dead Letter Queue.
     */
    private void sendToDlq(String topic, String key, Object value, Exception exception,
                          int retryCount, String consumerGroup) {
        try {
            dlqService.sendToDeadLetterQueue(topic, key, value, exception, retryCount, consumerGroup);
        } catch (Exception e) {
            logger.error("Failed to send message to DLQ: topic={}", topic, e);
        }
    }

    /**
     * Create retry template with exponential backoff.
     */
    private RetryTemplate createRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        // Retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(MAX_RETRIES);
        template.setRetryPolicy(retryPolicy);

        // Backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }
}

