package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Domain event emitted when a user starts playback of video content.
 * This is a core business event that triggers analytics, recommendations, and billing.
 */
public class PlaybackStartedEvent extends BaseEvent {

    /**
     * Unique session identifier for this playback session
     */
    @JsonProperty("sessionId")
    private final String sessionId;

    /**
     * User who initiated the playback
     */
    @JsonProperty("userId")
    private final String userId;

    /**
     * Content being played (movie/show ID)
     */
    @JsonProperty("contentId")
    private final String contentId;

    /**
     * Type of content (MOVIE, EPISODE, TRAILER, etc.)
     */
    @JsonProperty("contentType")
    private final String contentType;

    /**
     * Device type (MOBILE, DESKTOP, TV, etc.)
     */
    @JsonProperty("deviceType")
    private final String deviceType;

    /**
     * Selected video quality (720p, 1080p, 4K, etc.)
     */
    @JsonProperty("quality")
    private final String quality;

    /**
     * Client application version
     */
    @JsonProperty("clientVersion")
    private final String clientVersion;

    /**
     * Geographic region of the user
     */
    @JsonProperty("region")
    private final String region;

    /**
     * Network type (WIFI, CELLULAR, ETHERNET)
     */
    @JsonProperty("networkType")
    private final String networkType;

    /**
     * CDN node serving the content
     */
    @JsonProperty("cdnNode")
    private final String cdnNode;

    /**
     * Playback position when started (usually 0)
     */
    @JsonProperty("startPosition")
    private final long startPosition;

    @JsonCreator
    public PlaybackStartedEvent(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("userId") String userId,
            @JsonProperty("contentId") String contentId,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("deviceType") String deviceType,
            @JsonProperty("quality") String quality,
            @JsonProperty("clientVersion") String clientVersion,
            @JsonProperty("region") String region,
            @JsonProperty("networkType") String networkType,
            @JsonProperty("cdnNode") String cdnNode,
            @JsonProperty("startPosition") long startPosition) {
        super(correlationId, causationId, tenantId);
        this.sessionId = sessionId;
        this.userId = userId;
        this.contentId = contentId;
        this.contentType = contentType;
        this.deviceType = deviceType;
        this.quality = quality;
        this.clientVersion = clientVersion;
        this.region = region;
        this.networkType = networkType;
        this.cdnNode = cdnNode;
        this.startPosition = startPosition;
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
    public String getContentType() { return contentType; }
    public String getDeviceType() { return deviceType; }
    public String getQuality() { return quality; }
    public String getClientVersion() { return clientVersion; }
    public String getRegion() { return region; }
    public String getNetworkType() { return networkType; }
    public String getCdnNode() { return cdnNode; }
    public long getStartPosition() { return startPosition; }
}