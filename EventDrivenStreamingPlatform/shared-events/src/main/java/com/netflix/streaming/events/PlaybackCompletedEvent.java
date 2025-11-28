package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Domain event emitted when a user completes playback of video content.
 * This event is crucial for analytics, recommendations, and billing calculations.
 */
public class PlaybackCompletedEvent extends BaseEvent {

    /**
     * Session that completed
     */
    @JsonProperty("sessionId")
    private final String sessionId;

    /**
     * User who completed playback
     */
    @JsonProperty("userId")
    private final String userId;

    /**
     * Content that was completed
     */
    @JsonProperty("contentId")
    private final String contentId;

    /**
     * Total duration of content in milliseconds
     */
    @JsonProperty("contentDuration")
    private final long contentDuration;

    /**
     * Actual time watched in milliseconds
     */
    @JsonProperty("timeWatched")
    private final long timeWatched;

    /**
     * Completion percentage (0-100)
     */
    @JsonProperty("completionPercentage")
    private final double completionPercentage;

    /**
     * Final quality at completion
     */
    @JsonProperty("finalQuality")
    private final String finalQuality;

    /**
     * Total buffering time during playback
     */
    @JsonProperty("totalBufferingTime")
    private final long totalBufferingTime;

    /**
     * Number of quality changes during playback
     */
    @JsonProperty("qualityChanges")
    private final int qualityChanges;

    /**
     * Average bitrate during playback
     */
    @JsonProperty("averageBitrate")
    private final long averageBitrate;

    @JsonCreator
    public PlaybackCompletedEvent(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("userId") String userId,
            @JsonProperty("contentId") String contentId,
            @JsonProperty("contentDuration") long contentDuration,
            @JsonProperty("timeWatched") long timeWatched,
            @JsonProperty("completionPercentage") double completionPercentage,
            @JsonProperty("finalQuality") String finalQuality,
            @JsonProperty("totalBufferingTime") long totalBufferingTime,
            @JsonProperty("qualityChanges") int qualityChanges,
            @JsonProperty("averageBitrate") long averageBitrate) {
        super(correlationId, causationId, tenantId);
        this.sessionId = sessionId;
        this.userId = userId;
        this.contentId = contentId;
        this.contentDuration = contentDuration;
        this.timeWatched = timeWatched;
        this.completionPercentage = completionPercentage;
        this.finalQuality = finalQuality;
        this.totalBufferingTime = totalBufferingTime;
        this.qualityChanges = qualityChanges;
        this.averageBitrate = averageBitrate;
    }

    @Override
    public String getAggregateId() {
        return sessionId;
    }

    @Override
    public String getAggregateType() {
        return "PlaybackSession";
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public String getUserId() { return userId; }
    public String getContentId() { return contentId; }
    public long getContentDuration() { return contentDuration; }
    public long getTimeWatched() { return timeWatched; }
    public double getCompletionPercentage() { return completionPercentage; }
    public String getFinalQuality() { return finalQuality; }
    public long getTotalBufferingTime() { return totalBufferingTime; }
    public int getQualityChanges() { return qualityChanges; }
    public long getAverageBitrate() { return averageBitrate; }
}