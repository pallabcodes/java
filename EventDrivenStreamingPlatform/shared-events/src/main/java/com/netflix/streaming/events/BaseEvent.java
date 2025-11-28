package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the streaming platform.
 * All events are immutable facts that have occurred in the system.
 *
 * Key characteristics of domain events:
 * - Immutable (no setters)
 * - Represent facts, not commands
 * - Have unique event IDs
 * - Include correlation and causation IDs for tracing
 * - Versioned for schema evolution
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PlaybackStartedEvent.class, name = "PLAYBACK_STARTED"),
    @JsonSubTypes.Type(value = PlaybackPausedEvent.class, name = "PLAYBACK_PAUSED"),
    @JsonSubTypes.Type(value = PlaybackResumedEvent.class, name = "PLAYBACK_RESUMED"),
    @JsonSubTypes.Type(value = PlaybackCompletedEvent.class, name = "PLAYBACK_COMPLETED"),
    @JsonSubTypes.Type(value = QualityChangedEvent.class, name = "QUALITY_CHANGED"),
    @JsonSubTypes.Type(value = BufferingEvent.class, name = "BUFFERING"),
    @JsonSubTypes.Type(value = SessionEndedEvent.class, name = "SESSION_ENDED"),
    @JsonSubTypes.Type(value = ContentPerformanceCalculatedEvent.class, name = "CONTENT_PERFORMANCE_CALCULATED"),
    @JsonSubTypes.Type(value = UserEngagementCalculatedEvent.class, name = "USER_ENGAGEMENT_CALCULATED"),
    @JsonSubTypes.Type(value = AnomalyDetectedEvent.class, name = "ANOMALY_DETECTED")
})
public abstract class BaseEvent {

    /**
     * Unique identifier for this specific event occurrence
     */
    private final String eventId;

    /**
     * Type of the event (derived from class name)
     */
    private final String eventType;

    /**
     * When the event occurred (event time, not processing time)
     */
    private final Instant timestamp;

    /**
     * Correlation ID for tracing requests across services
     */
    private final String correlationId;

    /**
     * Causation ID for linking related events
     */
    private final String causationId;

    /**
     * Schema version for backward compatibility
     */
    private final String schemaVersion;

    /**
     * Tenant identifier for multi-tenancy
     */
    private final String tenantId;

    protected BaseEvent(String correlationId, String causationId, String tenantId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName().replace("Event", "").toUpperCase();
        this.timestamp = Instant.now();
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        this.causationId = causationId != null ? causationId : this.eventId;
        this.schemaVersion = "1.0";
        this.tenantId = tenantId != null ? tenantId : "default";
    }

    // Getters only - events are immutable
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Instant getTimestamp() { return timestamp; }
    public String getCorrelationId() { return correlationId; }
    public String getCausationId() { return causationId; }
    public String getSchemaVersion() { return schemaVersion; }
    public String getTenantId() { return tenantId; }

    /**
     * Returns the aggregate ID this event relates to (to be implemented by subclasses)
     */
    @JsonIgnore
    public abstract String getAggregateId();

    /**
     * Returns the aggregate type this event relates to (to be implemented by subclasses)
     */
    @JsonIgnore
    public abstract String getAggregateType();

    @Override
    public String toString() {
        return String.format("%s{eventId='%s', eventType='%s', aggregateId='%s', correlationId='%s'}",
            getClass().getSimpleName(), eventId, eventType, getAggregateId(), correlationId);
    }
}