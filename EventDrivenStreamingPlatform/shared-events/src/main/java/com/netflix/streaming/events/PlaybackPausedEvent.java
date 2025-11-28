package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Domain event emitted when playback is paused.
 */
public class PlaybackPausedEvent extends BaseEvent {

    /**
     * Session that was paused
     */
    @JsonProperty("sessionId")
    private final String sessionId;

    /**
     * Position where playback was paused
     */
    @JsonProperty("pausedAtPosition")
    private final long pausedAtPosition;

    @JsonCreator
    public PlaybackPausedEvent(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("pausedAtPosition") long pausedAtPosition) {
        super(correlationId, causationId, tenantId);
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