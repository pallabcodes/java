package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Domain event emitted when paused playback is resumed.
 */
public class PlaybackResumedEvent extends BaseEvent {

    /**
     * Session that was resumed
     */
    @JsonProperty("sessionId")
    private final String sessionId;

    /**
     * Position where playback resumed
     */
    @JsonProperty("resumedAtPosition")
    private final long resumedAtPosition;

    @JsonCreator
    public PlaybackResumedEvent(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("resumedAtPosition") long resumedAtPosition) {
        super(correlationId, causationId, tenantId);
        this.sessionId = sessionId;
        this.resumedAtPosition = resumedAtPosition;
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
    public long getResumedAtPosition() { return resumedAtPosition; }
}