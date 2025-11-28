package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Command to start playback of video content.
 * This is an imperative command that gets processed by the Playback aggregate.
 */
public class StartPlaybackCommand extends BaseCommand {

    /**
     * User requesting playback
     */
    @NotBlank
    @JsonProperty("userId")
    private final String userId;

    /**
     * Content to play
     */
    @NotBlank
    @JsonProperty("contentId")
    private final String contentId;

    /**
     * Type of content
     */
    @NotBlank
    @JsonProperty("contentType")
    private final String contentType;

    /**
     * Device type initiating the request
     */
    @NotBlank
    @JsonProperty("deviceType")
    private final String deviceType;

    /**
     * Desired video quality
     */
    @NotBlank
    @JsonProperty("quality")
    private final String quality;

    /**
     * Client application version
     */
    @JsonProperty("clientVersion")
    private final String clientVersion;

    /**
     * Geographic region
     */
    @JsonProperty("region")
    private final String region;

    /**
     * Network type
     */
    @JsonProperty("networkType")
    private final String networkType;

    /**
     * Position to start playback from (for resume)
     */
    @JsonProperty("startPosition")
    private final long startPosition;

    @JsonCreator
    public StartPlaybackCommand(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("userId") String userId,
            @JsonProperty("contentId") String contentId,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("deviceType") String deviceType,
            @JsonProperty("quality") String quality,
            @JsonProperty("clientVersion") String clientVersion,
            @JsonProperty("region") String region,
            @JsonProperty("networkType") String networkType,
            @JsonProperty("startPosition") long startPosition,
            @JsonProperty("expectedVersion") Long expectedVersion) {
        super(correlationId, causationId, tenantId, expectedVersion);
        this.userId = userId;
        this.contentId = contentId;
        this.contentType = contentType;
        this.deviceType = deviceType;
        this.quality = quality;
        this.clientVersion = clientVersion;
        this.region = region;
        this.networkType = networkType;
        this.startPosition = startPosition;
    }

    @Override
    public String getAggregateId() {
        // Commands target aggregates by business key, not technical ID
        // The actual aggregate ID will be generated when processing the command
        return null; // Will be set by the command handler
    }

    @Override
    public String getAggregateType() {
        return "PlaybackSession";
    }

    // Getters
    public String getUserId() { return userId; }
    public String getContentId() { return contentId; }
    public String getContentType() { return contentType; }
    public String getDeviceType() { return deviceType; }
    public String getQuality() { return quality; }
    public String getClientVersion() { return clientVersion; }
    public String getRegion() { return region; }
    public String getNetworkType() { return networkType; }
    public long getStartPosition() { return startPosition; }
}