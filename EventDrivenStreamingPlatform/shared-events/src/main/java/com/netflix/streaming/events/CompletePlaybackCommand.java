package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Command to complete a playback session.
 */
public class CompletePlaybackCommand extends BaseCommand {

    /**
     * Session to complete
     */
    private final String sessionId;

    /**
     * Total duration of the content
     */
    private final long contentDuration;

    @JsonCreator
    public CompletePlaybackCommand(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("contentDuration") long contentDuration,
            @JsonProperty("expectedVersion") Long expectedVersion) {
        super(correlationId, causationId, tenantId, expectedVersion);
        this.sessionId = sessionId;
        this.contentDuration = contentDuration;
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
    public long getContentDuration() { return contentDuration; }
}