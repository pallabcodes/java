package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Command to resume a paused playback session.
 */
public class ResumePlaybackCommand extends BaseCommand {

    /**
     * Session to resume
     */
    private final String sessionId;

    @JsonCreator
    public ResumePlaybackCommand(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("expectedVersion") Long expectedVersion) {
        super(correlationId, causationId, tenantId, expectedVersion);
        this.sessionId = sessionId;
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
}