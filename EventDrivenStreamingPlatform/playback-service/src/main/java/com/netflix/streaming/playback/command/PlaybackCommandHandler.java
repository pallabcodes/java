package com.netflix.streaming.playback.command;

import com.netflix.streaming.events.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;

/**
 * CQRS Command Handler for Playback operations.
 *
 * This demonstrates:
 * - CQRS Command side: Handles commands and coordinates aggregates
 * - Command validation and business logic
 * - Integration with Axon Framework
 * - Observability and error handling
 */
@Service
public class PlaybackCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PlaybackCommandHandler.class);

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final Tracer tracer;

    public PlaybackCommandHandler(CommandGateway commandGateway,
                                 QueryGateway queryGateway,
                                 Tracer tracer) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.tracer = tracer;
    }

    /**
     * REST API endpoint equivalent - Start Playback
     */
    public String startPlayback(@Valid StartPlaybackRequest request) {
        Span span = tracer.spanBuilder("playback.start")
            .setAttribute("user.id", request.getUserId())
            .setAttribute("content.id", request.getContentId())
            .startSpan();

        try {
            var command = new StartPlaybackCommand(
                request.getCorrelationId(),
                null, // No causation for initial command
                request.getTenantId(),
                request.getUserId(),
                request.getContentId(),
                request.getContentType(),
                request.getDeviceType(),
                request.getQuality(),
                request.getClientVersion(),
                request.getRegion(),
                request.getNetworkType(),
                request.getStartPosition(),
                null // No expected version for creation
            );

            // Send command via Axon Gateway (async)
            var result = commandGateway.sendAndWait(command);

            span.setAttribute("session.id", (String) result);
            span.setStatus(StatusCode.OK);

            logger.info("Started playback session for user: {}, content: {}, session: {}",
                       request.getUserId(), request.getContentId(), result);

            return (String) result;

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to start playback for user: {}, content: {}",
                        request.getUserId(), request.getContentId(), e);
            throw new PlaybackException("Failed to start playback", e);
        } finally {
            span.end();
        }
    }

    /**
     * Pause Playback
     */
    public void pausePlayback(@Valid PausePlaybackRequest request) {
        Span span = tracer.spanBuilder("playback.pause")
            .setAttribute("session.id", request.getSessionId())
            .startSpan();

        try {
            var command = new PausePlaybackCommand(
                request.getCorrelationId(),
                null,
                request.getTenantId(),
                request.getSessionId(),
                request.getCurrentPosition(),
                request.getExpectedVersion()
            );

            commandGateway.sendAndWait(command);
            span.setStatus(StatusCode.OK);

            logger.info("Paused playback session: {}", request.getSessionId());

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to pause playback session: {}", request.getSessionId(), e);
            throw new PlaybackException("Failed to pause playback", e);
        } finally {
            span.end();
        }
    }

    /**
     * Resume Playback
     */
    public void resumePlayback(@Valid ResumePlaybackRequest request) {
        Span span = tracer.spanBuilder("playback.resume")
            .setAttribute("session.id", request.getSessionId())
            .startSpan();

        try {
            var command = new ResumePlaybackCommand(
                request.getCorrelationId(),
                null,
                request.getTenantId(),
                request.getSessionId(),
                request.getExpectedVersion()
            );

            commandGateway.sendAndWait(command);
            span.setStatus(StatusCode.OK);

            logger.info("Resumed playback session: {}", request.getSessionId());

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to resume playback session: {}", request.getSessionId(), e);
            throw new PlaybackException("Failed to resume playback", e);
        } finally {
            span.end();
        }
    }

    /**
     * Complete Playback
     */
    public void completePlayback(@Valid CompletePlaybackRequest request) {
        Span span = tracer.spanBuilder("playback.complete")
            .setAttribute("session.id", request.getSessionId())
            .setAttribute("content.duration", request.getContentDuration())
            .startSpan();

        try {
            var command = new CompletePlaybackCommand(
                request.getCorrelationId(),
                null,
                request.getTenantId(),
                request.getSessionId(),
                request.getContentDuration(),
                request.getExpectedVersion()
            );

            commandGateway.sendAndWait(command);
            span.setStatus(StatusCode.OK);

            logger.info("Completed playback session: {}", request.getSessionId());

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to complete playback session: {}", request.getSessionId(), e);
            throw new PlaybackException("Failed to complete playback", e);
        } finally {
            span.end();
        }
    }

    // Request DTOs (equivalent to REST API request bodies)

    public static class StartPlaybackRequest {
        private String correlationId;
        private String tenantId = "default";
        private String userId;
        private String contentId;
        private String contentType = "MOVIE";
        private String deviceType = "DESKTOP";
        private String quality = "720p";
        private String clientVersion = "1.0.0";
        private String region = "us-east-1";
        private String networkType = "WIFI";
        private long startPosition = 0;

        // Getters and setters
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getContentId() { return contentId; }
        public void setContentId(String contentId) { this.contentId = contentId; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public String getQuality() { return quality; }
        public void setQuality(String quality) { this.quality = quality; }
        public String getClientVersion() { return clientVersion; }
        public void setClientVersion(String clientVersion) { this.clientVersion = clientVersion; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getNetworkType() { return networkType; }
        public void setNetworkType(String networkType) { this.networkType = networkType; }
        public long getStartPosition() { return startPosition; }
        public void setStartPosition(long startPosition) { this.startPosition = startPosition; }
    }

    public static class PausePlaybackRequest {
        private String correlationId;
        private String tenantId = "default";
        private String sessionId;
        private long currentPosition;
        private Long expectedVersion;

        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public long getCurrentPosition() { return currentPosition; }
        public void setCurrentPosition(long currentPosition) { this.currentPosition = currentPosition; }
        public Long getExpectedVersion() { return expectedVersion; }
        public void setExpectedVersion(Long expectedVersion) { this.expectedVersion = expectedVersion; }
    }

    public static class ResumePlaybackRequest {
        private String correlationId;
        private String tenantId = "default";
        private String sessionId;
        private Long expectedVersion;

        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public Long getExpectedVersion() { return expectedVersion; }
        public void setExpectedVersion(Long expectedVersion) { this.expectedVersion = expectedVersion; }
    }

    public static class CompletePlaybackRequest {
        private String correlationId;
        private String tenantId = "default";
        private String sessionId;
        private long contentDuration;
        private Long expectedVersion;

        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public long getContentDuration() { return contentDuration; }
        public void setContentDuration(long contentDuration) { this.contentDuration = contentDuration; }
        public Long getExpectedVersion() { return expectedVersion; }
        public void setExpectedVersion(Long expectedVersion) { this.expectedVersion = expectedVersion; }
    }

    /**
     * Custom exception for playback operations
     */
    public static class PlaybackException extends RuntimeException {
        public PlaybackException(String message) {
            super(message);
        }

        public PlaybackException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}