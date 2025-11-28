package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Command to pause an active playback session.
 */
public class PausePlaybackCommand extends BaseCommand {

    /**
     * Session to pause
     */
    private final String sessionId;

    /**
     * Current playback position when paused
     */
    private final long pausedAtPosition;

    @JsonCreator
    public PausePlaybackCommand(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("pausedAtPosition") long pausedAtPosition,
            @JsonProperty("expectedVersion") Long expectedVersion) {
        super(correlationId, causationId, tenantId, expectedVersion);
        this.sessionId = sessionId;
        this.pausedAtPosition = pausedAtPosition;
    }

    @Override
    public String getAggregateId() {
        return sessionId;
    }

    @Override
    public String getAggregateType() {
        return "PlaybackSession";
    }

    public String getSessionId() { return sessionId; }
    public long getPausedAtPosition() { return pausedAtPosition; }
}