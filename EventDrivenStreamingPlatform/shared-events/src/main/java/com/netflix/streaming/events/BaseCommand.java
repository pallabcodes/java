package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all commands in the streaming platform.
 * Commands are imperative - they tell the system to perform an action.
 *
 * Key characteristics of commands:
 * - Imperative (StartPlayback, not PlaybackStarted)
 * - Can be rejected (validation, business rules)
 * - Have unique command IDs
 * - Include correlation IDs for tracing
 * - Are processed by command handlers
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = StartPlaybackCommand.class, name = "START_PLAYBACK"),
    @JsonSubTypes.Type(value = PausePlaybackCommand.class, name = "PAUSE_PLAYBACK"),
    @JsonSubTypes.Type(value = ResumePlaybackCommand.class, name = "RESUME_PLAYBACK"),
    @JsonSubTypes.Type(value = StopPlaybackCommand.class, name = "STOP_PLAYBACK"),
    @JsonSubTypes.Type(value = ChangeQualityCommand.class, name = "CHANGE_QUALITY"),
    @JsonSubTypes.Type(value = SeekToCommand.class, name = "SEEK_TO"),
    @JsonSubTypes.Type(value = CalculateContentPerformanceCommand.class, name = "CALCULATE_CONTENT_PERFORMANCE"),
    @JsonSubTypes.Type(value = CalculateUserEngagementCommand.class, name = "CALCULATE_USER_ENGAGEMENT")
})
public abstract class BaseCommand {

    /**
     * Unique identifier for this command
     */
    @NotBlank
    private final String commandId;

    /**
     * Type of the command (derived from class name)
     */
    private final String commandType;

    /**
     * When the command was issued
     */
    @NotNull
    private final Instant timestamp;

    /**
     * Correlation ID for tracing this command across services
     */
    @NotBlank
    private final String correlationId;

    /**
     * Causation ID linking this command to previous events
     */
    private final String causationId;

    /**
     * Expected version for optimistic concurrency (if applicable)
     */
    private final Long expectedVersion;

    /**
     * Tenant identifier for multi-tenancy
     */
    @NotBlank
    private final String tenantId;

    protected BaseCommand(String correlationId, String causationId, String tenantId, Long expectedVersion) {
        this.commandId = UUID.randomUUID().toString();
        this.commandType = this.getClass().getSimpleName().replace("Command", "").toUpperCase();
        this.timestamp = Instant.now();
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        this.causationId = causationId;
        this.expectedVersion = expectedVersion;
        this.tenantId = tenantId != null ? tenantId : "default";
    }

    // Getters only - commands are immutable once created
    public String getCommandId() { return commandId; }
    public String getCommandType() { return commandType; }
    public Instant getTimestamp() { return timestamp; }
    public String getCorrelationId() { return correlationId; }
    public String getCausationId() { return causationId; }
    public Long getExpectedVersion() { return expectedVersion; }
    public String getTenantId() { return tenantId; }

    /**
     * Returns the aggregate ID this command targets (to be implemented by subclasses)
     */
    @JsonIgnore
    public abstract String getAggregateId();

    /**
     * Returns the aggregate type this command targets (to be implemented by subclasses)
     */
    @JsonIgnore
    public abstract String getAggregateType();

    @Override
    public String toString() {
        return String.format("%s{commandId='%s', commandType='%s', aggregateId='%s', correlationId='%s'}",
            getClass().getSimpleName(), commandId, commandType, getAggregateId(), correlationId);
    }
}