package com.netflix.streaming.events;

/**
 * Interface for publishing domain events.
 * This is the primary mechanism for services to emit events that other services can react to.
 *
 * Implementations should handle:
 * - Reliable delivery (at-least-once semantics)
 * - Schema validation
 * - Tracing propagation
 * - Error handling and retries
 */
public interface EventPublisher {

    /**
     * Publishes a domain event to the event bus.
     * The event will be delivered to all interested consumers.
     *
     * @param event The domain event to publish
     * @throws EventPublishingException if publishing fails
     */
    void publish(BaseEvent event) throws EventPublishingException;

    /**
     * Publishes a domain event with additional metadata.
     *
     * @param event The domain event to publish
     * @param metadata Additional metadata (routing, headers, etc.)
     * @throws EventPublishingException if publishing fails
     */
    void publish(BaseEvent event, EventMetadata metadata) throws EventPublishingException;

    /**
     * Publishes multiple events in a batch.
     * Implementations should ensure atomicity where possible.
     *
     * @param events The events to publish
     * @throws EventPublishingException if publishing fails
     */
    void publishBatch(Iterable<BaseEvent> events) throws EventPublishingException;

    /**
     * Exception thrown when event publishing fails.
     */
    class EventPublishingException extends Exception {
        public EventPublishingException(String message) {
            super(message);
        }

        public EventPublishingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Metadata associated with event publishing.
     */
    class EventMetadata {
        private final String routingKey;
        private final Integer partitionKey;
        private final java.util.Map<String, String> headers;

        public EventMetadata(String routingKey, Integer partitionKey, java.util.Map<String, String> headers) {
            this.routingKey = routingKey;
            this.partitionKey = partitionKey;
            this.headers = headers != null ? headers : java.util.Collections.emptyMap();
        }

        public String getRoutingKey() { return routingKey; }
        public Integer getPartitionKey() { return partitionKey; }
        public java.util.Map<String, String> getHeaders() { return headers; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String routingKey;
            private Integer partitionKey;
            private java.util.Map<String, String> headers = new java.util.HashMap<>();

            public Builder routingKey(String routingKey) {
                this.routingKey = routingKey;
                return this;
            }

            public Builder partitionKey(Integer partitionKey) {
                this.partitionKey = partitionKey;
                return this;
            }

            public Builder header(String key, String value) {
                this.headers.put(key, value);
                return this;
            }

            public Builder headers(java.util.Map<String, String> headers) {
                this.headers.putAll(headers);
                return this;
            }

            public EventMetadata build() {
                return new EventMetadata(routingKey, partitionKey, headers);
            }
        }
    }
}