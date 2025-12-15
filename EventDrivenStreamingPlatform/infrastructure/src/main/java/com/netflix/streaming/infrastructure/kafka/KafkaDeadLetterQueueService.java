package com.netflix.streaming.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Dead Letter Queue Service for Kafka.
 * 
 * Handles failed event processing by sending messages to DLQ topics
 * with comprehensive error metadata for debugging and reprocessing.
 */
@Service
public class KafkaDeadLetterQueueService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaDeadLetterQueueService.class);

    private final KafkaTemplate<String, Object> dlqKafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaDeadLetterQueueService(KafkaTemplate<String, Object> dlqKafkaTemplate,
                                      ObjectMapper objectMapper) {
        this.dlqKafkaTemplate = dlqKafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a failed message to the Dead Letter Queue.
     * 
     * @param originalTopic The original topic the message came from
     * @param originalKey The original message key
     * @param originalValue The original message value
     * @param exception The exception that caused the failure
     * @param retryCount Number of retry attempts made
     * @param consumerGroup The consumer group that failed to process
     */
    public void sendToDeadLetterQueue(
            String originalTopic,
            String originalKey,
            Object originalValue,
            Exception exception,
            int retryCount,
            String consumerGroup) {

        try {
            String dlqTopic = determineDlqTopic(originalTopic);
            
            // Create DLQ message with error metadata
            DeadLetterQueueMessage dlqMessage = DeadLetterQueueMessage.builder()
                .originalTopic(originalTopic)
                .originalKey(originalKey)
                .originalValue(originalValue)
                .errorMessage(exception.getMessage())
                .errorType(exception.getClass().getName())
                .stackTrace(getStackTrace(exception))
                .retryCount(retryCount)
                .consumerGroup(consumerGroup)
                .failedAt(Instant.now())
                .build();

            // Create producer record with headers
            ProducerRecord<String, Object> dlqRecord = new ProducerRecord<>(dlqTopic, originalKey, dlqMessage);
            
            // Add metadata headers
            dlqRecord.headers().add("dlq-original-topic", originalTopic.getBytes());
            dlqRecord.headers().add("dlq-error-type", exception.getClass().getName().getBytes());
            dlqRecord.headers().add("dlq-retry-count", String.valueOf(retryCount).getBytes());
            dlqRecord.headers().add("dlq-consumer-group", consumerGroup.getBytes());
            dlqRecord.headers().add("dlq-failed-at", Instant.now().toString().getBytes());

            // Send to DLQ
            dlqKafkaTemplate.send(dlqRecord).whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.error("Failed to send message to DLQ topic: {}", dlqTopic, throwable);
                } else {
                    logger.warn("Sent failed message to DLQ: {} -> {} (retry count: {})",
                        originalTopic, dlqTopic, retryCount);
                }
            });

        } catch (Exception e) {
            logger.error("Critical error sending message to DLQ: original topic: {}", originalTopic, e);
            // In a real system, you might want to:
            // 1. Store in database as fallback
            // 2. Send alert to operations team
            // 3. Log to external logging system
        }
    }

    /**
     * Send a failed message to DLQ with minimal parameters.
     */
    public void sendToDeadLetterQueue(String originalTopic, String originalKey, Object originalValue, Exception exception) {
        sendToDeadLetterQueue(originalTopic, originalKey, originalValue, exception, 0, "unknown");
    }

    /**
     * Determine the DLQ topic name from the original topic.
     */
    private String determineDlqTopic(String originalTopic) {
        // DLQ topic naming convention: {original-topic}.dlq
        if (originalTopic.endsWith(".dlq")) {
            return originalTopic; // Already a DLQ topic
        }
        return originalTopic + ".dlq";
    }

    /**
     * Get stack trace as string.
     */
    private String getStackTrace(Exception exception) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Dead Letter Queue message structure.
     */
    public static class DeadLetterQueueMessage {
        private String originalTopic;
        private String originalKey;
        private Object originalValue;
        private String errorMessage;
        private String errorType;
        private String stackTrace;
        private int retryCount;
        private String consumerGroup;
        private Instant failedAt;

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private DeadLetterQueueMessage message = new DeadLetterQueueMessage();

            public Builder originalTopic(String originalTopic) {
                message.originalTopic = originalTopic;
                return this;
            }

            public Builder originalKey(String originalKey) {
                message.originalKey = originalKey;
                return this;
            }

            public Builder originalValue(Object originalValue) {
                message.originalValue = originalValue;
                return this;
            }

            public Builder errorMessage(String errorMessage) {
                message.errorMessage = errorMessage;
                return this;
            }

            public Builder errorType(String errorType) {
                message.errorType = errorType;
                return this;
            }

            public Builder stackTrace(String stackTrace) {
                message.stackTrace = stackTrace;
                return this;
            }

            public Builder retryCount(int retryCount) {
                message.retryCount = retryCount;
                return this;
            }

            public Builder consumerGroup(String consumerGroup) {
                message.consumerGroup = consumerGroup;
                return this;
            }

            public Builder failedAt(Instant failedAt) {
                message.failedAt = failedAt;
                return this;
            }

            public DeadLetterQueueMessage build() {
                return message;
            }
        }

        // Getters
        public String getOriginalTopic() { return originalTopic; }
        public String getOriginalKey() { return originalKey; }
        public Object getOriginalValue() { return originalValue; }
        public String getErrorMessage() { return errorMessage; }
        public String getErrorType() { return errorType; }
        public String getStackTrace() { return stackTrace; }
        public int getRetryCount() { return retryCount; }
        public String getConsumerGroup() { return consumerGroup; }
        public Instant getFailedAt() { return failedAt; }
    }
}

