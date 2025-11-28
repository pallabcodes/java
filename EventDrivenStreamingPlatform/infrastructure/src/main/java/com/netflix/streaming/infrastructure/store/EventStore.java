package com.netflix.streaming.infrastructure.store;

import com.netflix.streaming.events.BaseEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Event Store interface for append-only event storage.
 * Provides capabilities for storing, retrieving, and replaying domain events.
 */
public interface EventStore {

    /**
     * Appends a single event to the store.
     */
    void append(BaseEvent event) throws EventStoreException;

    /**
     * Appends multiple events atomically.
     */
    void appendBatch(List<BaseEvent> events) throws EventStoreException;

    /**
     * Loads all events for an aggregate.
     */
    List<StoredEvent> loadEvents(String aggregateId);

    /**
     * Loads events for an aggregate from a specific version.
     */
    List<StoredEvent> loadEvents(String aggregateId, long fromVersion);

    /**
     * Loads events for an aggregate from a specific time.
     */
    List<StoredEvent> loadEvents(String aggregateId, Instant fromTime);

    /**
     * Loads a specific event by ID.
     */
    Optional<StoredEvent> loadEvent(String eventId);

    /**
     * Gets the current version of an aggregate.
     */
    long getCurrentVersion(String aggregateId);

    /**
     * Gets all aggregate IDs for a given type.
     */
    List<String> getAggregateIds(String aggregateType);

    /**
     * Replays all events from a given time (for rebuilding projections).
     */
    List<StoredEvent> replayAll(Instant fromTime);

    /**
     * Replays events of a specific type from a given time.
     */
    List<StoredEvent> replayByType(String eventType, Instant fromTime);

    /**
     * Exception thrown when event store operations fail.
     */
    class EventStoreException extends Exception {
        public EventStoreException(String message) {
            super(message);
        }

        public EventStoreException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Represents a stored event with metadata.
     */
    class StoredEvent {
        private final long id;
        private final String eventId;
        private final String eventType;
        private final String aggregateId;
        private final String aggregateType;
        private final BaseEvent event;
        private final String correlationId;
        private final String causationId;
        private final String tenantId;
        private final String schemaVersion;
        private final Instant createdAt;
        private final long version;

        public StoredEvent(long id, String eventId, String eventType, String aggregateId,
                          String aggregateType, BaseEvent event, String correlationId,
                          String causationId, String tenantId, String schemaVersion,
                          Instant createdAt, long version) {
            this.id = id;
            this.eventId = eventId;
            this.eventType = eventType;
            this.aggregateId = aggregateId;
            this.aggregateType = aggregateType;
            this.event = event;
            this.correlationId = correlationId;
            this.causationId = causationId;
            this.tenantId = tenantId;
            this.schemaVersion = schemaVersion;
            this.createdAt = createdAt;
            this.version = version;
        }

        // Getters
        public long getId() { return id; }
        public String getEventId() { return eventId; }
        public String getEventType() { return eventType; }
        public String getAggregateId() { return aggregateId; }
        public String getAggregateType() { return aggregateType; }
        public BaseEvent getEvent() { return event; }
        public String getCorrelationId() { return correlationId; }
        public String getCausationId() { return causationId; }
        public String getTenantId() { return tenantId; }
        public String getSchemaVersion() { return schemaVersion; }
        public Instant getCreatedAt() { return createdAt; }
        public long getVersion() { return version; }
    }
}