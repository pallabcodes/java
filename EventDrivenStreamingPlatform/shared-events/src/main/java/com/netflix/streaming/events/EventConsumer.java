package com.netflix.streaming.events;

import java.util.Set;

/**
 * Interface for consuming domain events.
 * Services implement this interface to react to events published by other services.
 *
 * Key characteristics:
 * - Idempotent processing (can handle duplicate events)
 * - Fault-tolerant (handles processing failures)
 * - Observable (provides metrics and tracing)
 */
public interface EventConsumer<T extends BaseEvent> {

    /**
     * Returns the event types this consumer can handle.
     * Used for event routing and filtering.
     */
    Set<String> getSupportedEventTypes();

    /**
     * Processes an incoming event.
     * Implementations should be idempotent and handle failures gracefully.
     *
     * @param event The event to process
     * @param metadata Additional event metadata
     * @throws EventProcessingException if processing fails
     */
    void consume(T event, EventMetadata metadata) throws EventProcessingException;

    /**
     * Returns the consumer group for this consumer.
     * Multiple instances can share the same group for load balancing.
     */
    String getConsumerGroup();

    /**
     * Exception thrown when event processing fails.
     */
    class EventProcessingException extends Exception {
        private final boolean shouldRetry;
        private final boolean shouldDlq;

        public EventProcessingException(String message) {
            this(message, true, false);
        }

        public EventProcessingException(String message, boolean shouldRetry, boolean shouldDlq) {
            super(message);
            this.shouldRetry = shouldRetry;
            this.shouldDlq = shouldDlq;
        }

        public boolean shouldRetry() { return shouldRetry; }
        public boolean shouldDlq() { return shouldDlq; }
    }

    /**
     * Metadata associated with event consumption.
     */
    class EventMetadata {
        private final String topic;
        private final int partition;
        private final long offset;
        private final java.util.Map<String, String> headers;
        private final String messageId;

        public EventMetadata(String topic, int partition, long offset,
                           java.util.Map<String, String> headers, String messageId) {
            this.topic = topic;
            this.partition = partition;
            this.offset = offset;
            this.headers = headers != null ? headers : java.util.Collections.emptyMap();
            this.messageId = messageId;
        }

        public String getTopic() { return topic; }
        public int getPartition() { return partition; }
        public long getOffset() { return offset; }
        public java.util.Map<String, String> getHeaders() { return headers; }
        public String getMessageId() { return messageId; }
    }
}