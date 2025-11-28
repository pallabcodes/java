package com.netflix.streaming.playback.aggregate;

import com.netflix.streaming.events.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;

/**
 * Event-Sourced Aggregate for Playback Sessions.
 *
 * This aggregate demonstrates:
 * - Event Sourcing: State built from domain events
 * - CQRS Write Model: Handles commands and emits events
 * - Business Logic: Playback session lifecycle management
 * - Validation: Business rule enforcement
 */
@Aggregate
@NoArgsConstructor // Required by Axon
@Getter
public class PlaybackSession {

    private static final Logger logger = LoggerFactory.getLogger(PlaybackSession.class);

    // Aggregate Identifier - unique across all playback sessions
    @AggregateIdentifier
    private String sessionId;

    // Current state - rebuilt from events
    private String userId;
    private String contentId;
    private String contentType;
    private String deviceType;
    private String quality;
    private Instant startedAt;
    private Instant lastActivityAt;
    private long currentPosition; // in milliseconds
    private PlaybackStatus status;
    private long totalWatchTime; // accumulated watch time
    private int qualityChanges;
    private long totalBufferingTime;
    private String finalQuality; // set at completion

    // Business enums
    public enum PlaybackStatus {
        STARTED, PLAYING, PAUSED, BUFFERING, COMPLETED, INTERRUPTED
    }

    /**
     * Command Handler: Start Playback
     * Creates a new playback session aggregate.
     */
    @CommandHandler
    public PlaybackSession(StartPlaybackCommand command) {
        logger.info("Starting playback session for user: {} content: {}",
                   command.getUserId(), command.getContentId());

        // Business validation
        validateStartPlayback(command);

        // Emit domain event
        var event = new PlaybackStartedEvent(
            command.getCorrelationId(),
            null, // No causation for initial event
            command.getTenantId(),
            generateSessionId(command), // Generate session ID
            command.getUserId(),
            command.getContentId(),
            command.getContentType(),
            command.getDeviceType(),
            command.getQuality(),
            command.getClientVersion(),
            command.getRegion(),
            command.getNetworkType(),
            "cdn-" + command.getRegion() + "-001", // Simulated CDN assignment
            command.getStartPosition()
        );

        AggregateLifecycle.apply(event);
    }

    /**
     * Event Sourcing Handler: Apply PlaybackStartedEvent
     * Rebuilds aggregate state from the event.
     */
    @EventSourcingHandler
    public void on(PlaybackStartedEvent event) {
        this.sessionId = event.getSessionId();
        this.userId = event.getUserId();
        this.contentId = event.getContentId();
        this.contentType = event.getContentType();
        this.deviceType = event.getDeviceType();
        this.quality = event.getQuality();
        this.startedAt = event.getTimestamp();
        this.lastActivityAt = event.getTimestamp();
        this.currentPosition = event.getStartPosition();
        this.status = PlaybackStatus.STARTED;
        this.totalWatchTime = 0;
        this.qualityChanges = 0;
        this.totalBufferingTime = 0;

        logger.debug("Playback session {} started for user {} content {}",
                    sessionId, userId, contentId);
    }

    /**
     * Command Handler: Pause Playback
     */
    @CommandHandler
    public void handle(PausePlaybackCommand command) {
        validateSessionActive();
        validateCommandCorrelation(command);

        var event = new PlaybackPausedEvent(
            command.getCorrelationId(),
            command.getCommandId(),
            command.getTenantId(),
            this.sessionId,
            this.currentPosition
        );

        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(PlaybackPausedEvent event) {
        this.status = PlaybackStatus.PAUSED;
        this.lastActivityAt = event.getTimestamp();
        this.currentPosition = event.getPausedAtPosition();
    }

    /**
     * Command Handler: Resume Playback
     */
    @CommandHandler
    public void handle(ResumePlaybackCommand command) {
        validateSessionActive();
        validateCommandCorrelation(command);

        var event = new PlaybackResumedEvent(
            command.getCorrelationId(),
            command.getCommandId(),
            command.getTenantId(),
            this.sessionId,
            this.currentPosition
        );

        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(PlaybackResumedEvent event) {
        this.status = PlaybackStatus.PLAYING;
        this.lastActivityAt = event.getTimestamp();
    }

    /**
     * Command Handler: Complete Playback
     */
    @CommandHandler
    public void handle(CompletePlaybackCommand command) {
        validateSessionActive();
        validateCommandCorrelation(command);

        // Calculate session metrics
        var sessionDuration = Duration.between(startedAt, Instant.now()).toMillis();
        var completionPercentage = calculateCompletionPercentage(command.getContentDuration());

        var event = new PlaybackCompletedEvent(
            command.getCorrelationId(),
            command.getCommandId(),
            command.getTenantId(),
            this.sessionId,
            this.userId,
            this.contentId,
            command.getContentDuration(),
            this.totalWatchTime + sessionDuration, // Include current session
            completionPercentage,
            this.quality, // final quality
            this.totalBufferingTime,
            this.qualityChanges,
            2500000L // simulated average bitrate
        );

        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(PlaybackCompletedEvent event) {
        this.status = PlaybackStatus.COMPLETED;
        this.lastActivityAt = event.getTimestamp();
        this.finalQuality = event.getFinalQuality();
        this.totalWatchTime = event.getTimeWatched();
    }

    // Private helper methods

    private void validateStartPlayback(StartPlaybackCommand command) {
        if (command.getStartPosition() < 0) {
            throw new IllegalArgumentException("Start position cannot be negative");
        }
        // Additional business validations...
    }

    private void validateSessionActive() {
        if (this.status == PlaybackStatus.COMPLETED || this.status == PlaybackStatus.INTERRUPTED) {
            throw new IllegalStateException("Session is not active: " + this.status);
        }
    }

    private void validateCommandCorrelation(BaseCommand command) {
        if (!command.getTenantId().equals(getTenantId())) {
            throw new IllegalArgumentException("Command tenant does not match session tenant");
        }
    }

    private String generateSessionId(StartPlaybackCommand command) {
        // Generate deterministic session ID based on user + content + timestamp
        return String.format("ps_%s_%s_%d",
                           command.getUserId(),
                           command.getContentId(),
                           command.getTimestamp().toEpochMilli());
    }

    private double calculateCompletionPercentage(long contentDuration) {
        if (contentDuration == 0) return 0.0;
        return Math.min(100.0, (double) totalWatchTime / contentDuration * 100.0);
    }

    private String getTenantId() {
        // Extract tenant from session ID or context
        return "default"; // Simplified for demo
    }
}