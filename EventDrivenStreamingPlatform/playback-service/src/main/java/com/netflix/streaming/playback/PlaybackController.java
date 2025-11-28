package com.netflix.streaming.playback;

import com.netflix.streaming.playback.command.PlaybackCommandHandler;
import com.netflix.streaming.playback.query.PlaybackSessionProjection;
import com.netflix.streaming.playback.query.PlaybackSessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST API Controller for Playback Service.
 *
 * This demonstrates:
 * - CQRS API Design: Commands vs Queries separation
 * - Event-Sourced Aggregates: Command handling via aggregates
 * - RESTful API: HTTP endpoints for playback operations
 * - OpenAPI Documentation: Swagger annotations
 */
@RestController
@RequestMapping("/api/v1/playback")
@Tag(name = "Playback API", description = "Video streaming playback session management")
public class PlaybackController {

    private static final Logger logger = LoggerFactory.getLogger(PlaybackController.class);

    private final PlaybackCommandHandler commandHandler;
    private final PlaybackSessionRepository queryRepository;

    public PlaybackController(PlaybackCommandHandler commandHandler,
                            PlaybackSessionRepository queryRepository) {
        this.commandHandler = commandHandler;
        this.queryRepository = queryRepository;
    }

    /**
     * Start a new playback session
     */
    @PostMapping("/sessions")
    @Operation(summary = "Start playback session",
               description = "Creates a new playback session for video streaming")
    public ResponseEntity<StartPlaybackResponse> startPlayback(
            @Valid @RequestBody PlaybackCommandHandler.StartPlaybackRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        // Generate correlation ID if not provided
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        request.setCorrelationId(correlationId);

        logger.info("Starting playback session for user: {}, content: {}",
                   request.getUserId(), request.getContentId());

        try {
            String sessionId = commandHandler.startPlayback(request);

            var response = new StartPlaybackResponse(sessionId, "PLAYBACK_STARTED");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to start playback for user: {}", request.getUserId(), e);
            return ResponseEntity.internalServerError()
                    .body(new StartPlaybackResponse(null, "FAILED"));
        }
    }

    /**
     * Pause an active playback session
     */
    @PutMapping("/sessions/{sessionId}/pause")
    @Operation(summary = "Pause playback session",
               description = "Pauses an active playback session at the current position")
    public ResponseEntity<ApiResponse> pausePlayback(
            @PathVariable String sessionId,
            @Valid @RequestBody PausePlaybackRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        request.setCorrelationId(correlationId != null ? correlationId : UUID.randomUUID().toString());
        request.setSessionId(sessionId);

        logger.info("Pausing playback session: {}", sessionId);

        try {
            commandHandler.pausePlayback(request);
            return ResponseEntity.ok(new ApiResponse("PLAYBACK_PAUSED", "Session paused successfully"));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("INVALID_STATE", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to pause session: {}", sessionId, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("FAILED", "Failed to pause session"));
        }
    }

    /**
     * Resume a paused playback session
     */
    @PutMapping("/sessions/{sessionId}/resume")
    @Operation(summary = "Resume playback session",
               description = "Resumes a paused playback session")
    public ResponseEntity<ApiResponse> resumePlayback(
            @PathVariable String sessionId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        var request = new PlaybackCommandHandler.ResumePlaybackRequest();
        request.setCorrelationId(correlationId != null ? correlationId : UUID.randomUUID().toString());
        request.setSessionId(sessionId);

        logger.info("Resuming playback session: {}", sessionId);

        try {
            commandHandler.resumePlayback(request);
            return ResponseEntity.ok(new ApiResponse("PLAYBACK_RESUMED", "Session resumed successfully"));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("INVALID_STATE", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to resume session: {}", sessionId, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("FAILED", "Failed to resume session"));
        }
    }

    /**
     * Complete a playback session
     */
    @PutMapping("/sessions/{sessionId}/complete")
    @Operation(summary = "Complete playback session",
               description = "Marks a playback session as completed")
    public ResponseEntity<ApiResponse> completePlayback(
            @PathVariable String sessionId,
            @Valid @RequestBody CompletePlaybackRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        request.setCorrelationId(correlationId != null ? correlationId : UUID.randomUUID().toString());
        request.setSessionId(sessionId);

        logger.info("Completing playback session: {}", sessionId);

        try {
            commandHandler.completePlayback(request);
            return ResponseEntity.ok(new ApiResponse("PLAYBACK_COMPLETED", "Session completed successfully"));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("INVALID_STATE", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to complete session: {}", sessionId, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("FAILED", "Failed to complete session"));
        }
    }

    /**
     * Get playback session details (CQRS Query)
     */
    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get playback session",
               description = "Retrieves playback session details from the read model")
    public ResponseEntity<PlaybackSessionProjection> getPlaybackSession(@PathVariable String sessionId) {

        logger.debug("Retrieving playback session: {}", sessionId);

        Optional<PlaybackSessionProjection> session = queryRepository.findById(sessionId);

        if (session.isPresent()) {
            return ResponseEntity.ok(session.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get active sessions for a user (CQRS Query)
     */
    @GetMapping("/users/{userId}/sessions/active")
    @Operation(summary = "Get active sessions for user",
               description = "Retrieves all active playback sessions for a user")
    public ResponseEntity<List<PlaybackSessionProjection>> getActiveSessionsByUser(@PathVariable String userId) {

        logger.debug("Retrieving active sessions for user: {}", userId);

        List<PlaybackSessionProjection> sessions = queryRepository.findActiveSessionsByUser(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get recent sessions for a user (CQRS Query)
     */
    @GetMapping("/users/{userId}/sessions/recent")
    @Operation(summary = "Get recent sessions for user",
               description = "Retrieves recent playback sessions for a user")
    public ResponseEntity<List<PlaybackSessionProjection>> getRecentSessionsByUser(@PathVariable String userId) {

        logger.debug("Retrieving recent sessions for user: {}", userId);

        List<PlaybackSessionProjection> sessions = queryRepository.findRecentSessionsByUser(userId);
        return ResponseEntity.ok(sessions);
    }

    // Request/Response DTOs

    public static class StartPlaybackResponse {
        public final String sessionId;
        public final String status;

        public StartPlaybackResponse(String sessionId, String status) {
            this.sessionId = sessionId;
            this.status = status;
        }
    }

    public static class PausePlaybackRequest {
        private String correlationId;
        private String tenantId = "default";
        private String sessionId;
        private long currentPosition;
        private Long expectedVersion;

        // Getters and setters
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

    public static class CompletePlaybackRequest {
        private String correlationId;
        private String tenantId = "default";
        private String sessionId;
        private long contentDuration;
        private Long expectedVersion;

        // Getters and setters
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

    public static class ApiResponse {
        public final String status;
        public final String message;

        public ApiResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}