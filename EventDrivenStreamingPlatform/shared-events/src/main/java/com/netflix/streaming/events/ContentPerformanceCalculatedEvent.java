package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Domain event emitted when content performance metrics are calculated.
 * This aggregates viewing data across all users for a specific content item.
 */
public class ContentPerformanceCalculatedEvent extends BaseEvent {

    /**
     * Content identifier
     */
    @JsonProperty("contentId")
    private final String contentId;

    /**
     * Content type (MOVIE, EPISODE, etc.)
     */
    @JsonProperty("contentType")
    private final String contentType;

    /**
     * Time window for these metrics (HOURLY, DAILY, WEEKLY)
     */
    @JsonProperty("timeWindow")
    private final String timeWindow;

    /**
     * Start of the time window
     */
    @JsonProperty("windowStart")
    private final Instant windowStart;

    /**
     * End of the time window
     */
    @JsonProperty("windowEnd")
    private final Instant windowEnd;

    /**
     * Total number of unique viewers
     */
    @JsonProperty("uniqueViewers")
    private final long uniqueViewers;

    /**
     * Total number of viewing sessions
     */
    @JsonProperty("totalSessions")
    private final long totalSessions;

    /**
     * Total watch time in milliseconds
     */
    @JsonProperty("totalWatchTime")
    private final long totalWatchTime;

    /**
     * Average completion percentage
     */
    @JsonProperty("avgCompletionPercentage")
    private final double avgCompletionPercentage;

    /**
     * Number of completed viewings
     */
    @JsonProperty("completedViews")
    private final long completedViews;

    /**
     * Average buffering time per session
     */
    @JsonProperty("avgBufferingTime")
    private final long avgBufferingTime;

    /**
     * Quality distribution (map of quality -> count)
     */
    @JsonProperty("qualityDistribution")
    private final Map<String, Long> qualityDistribution;

    /**
     * Device type distribution
     */
    @JsonProperty("deviceDistribution")
    private final Map<String, Long> deviceDistribution;

    /**
     * Geographic distribution (region -> count)
     */
    @JsonProperty("geographicDistribution")
    private final Map<String, Long> geographicDistribution;

    /**
     * Drop-off points (position -> drop-off count)
     */
    @JsonProperty("dropOffPoints")
    private final Map<Long, Long> dropOffPoints;

    /**
     * Engagement score (0-100)
     */
    @JsonProperty("engagementScore")
    private final double engagementScore;

    @JsonCreator
    public ContentPerformanceCalculatedEvent(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("contentId") String contentId,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("timeWindow") String timeWindow,
            @JsonProperty("windowStart") Instant windowStart,
            @JsonProperty("windowEnd") Instant windowEnd,
            @JsonProperty("uniqueViewers") long uniqueViewers,
            @JsonProperty("totalSessions") long totalSessions,
            @JsonProperty("totalWatchTime") long totalWatchTime,
            @JsonProperty("avgCompletionPercentage") double avgCompletionPercentage,
            @JsonProperty("completedViews") long completedViews,
            @JsonProperty("avgBufferingTime") long avgBufferingTime,
            @JsonProperty("qualityDistribution") Map<String, Long> qualityDistribution,
            @JsonProperty("deviceDistribution") Map<String, Long> deviceDistribution,
            @JsonProperty("geographicDistribution") Map<String, Long> geographicDistribution,
            @JsonProperty("dropOffPoints") Map<Long, Long> dropOffPoints,
            @JsonProperty("engagementScore") double engagementScore) {
        super(correlationId, causationId, tenantId);
        this.contentId = contentId;
        this.contentType = contentType;
        this.timeWindow = timeWindow;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.uniqueViewers = uniqueViewers;
        this.totalSessions = totalSessions;
        this.totalWatchTime = totalWatchTime;
        this.avgCompletionPercentage = avgCompletionPercentage;
        this.completedViews = completedViews;
        this.avgBufferingTime = avgBufferingTime;
        this.qualityDistribution = qualityDistribution;
        this.deviceDistribution = deviceDistribution;
        this.geographicDistribution = geographicDistribution;
        this.dropOffPoints = dropOffPoints;
        this.engagementScore = engagementScore;
    }

    @Override
    public String getAggregateId() {
        return contentId;
    }

    @Override
    public String getAggregateType() {
        return "ContentAnalytics";
    }

    // Getters
    public String getContentId() { return contentId; }
    public String getContentType() { return contentType; }
    public String getTimeWindow() { return timeWindow; }
    public Instant getWindowStart() { return windowStart; }
    public Instant getWindowEnd() { return windowEnd; }
    public long getUniqueViewers() { return uniqueViewers; }
    public long getTotalSessions() { return totalSessions; }
    public long getTotalWatchTime() { return totalWatchTime; }
    public double getAvgCompletionPercentage() { return avgCompletionPercentage; }
    public long getCompletedViews() { return completedViews; }
    public long getAvgBufferingTime() { return avgBufferingTime; }
    public Map<String, Long> getQualityDistribution() { return qualityDistribution; }
    public Map<String, Long> getDeviceDistribution() { return deviceDistribution; }
    public Map<String, Long> getGeographicDistribution() { return geographicDistribution; }
    public Map<Long, Long> getDropOffPoints() { return dropOffPoints; }
    public double getEngagementScore() { return engagementScore; }
}