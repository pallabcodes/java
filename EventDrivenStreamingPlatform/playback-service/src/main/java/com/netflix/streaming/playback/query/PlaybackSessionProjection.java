package com.netflix.streaming.playback.query;

import com.netflix.streaming.events.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.annotation.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * CQRS Read Model for Playback Sessions.
 *
 * This demonstrates:
 * - CQRS Query side: Optimized read models
 * - Event-Driven Projections: State updated via events
 * - Multiple query patterns: Different views for different needs
 */

/**
 * Main read model for playback sessions
 */
@Entity
@Table(name = "playback_sessions")
@Data
@NoArgsConstructor
public class PlaybackSessionProjection {

    @Id
    private String sessionId;

    private String userId;
    private String contentId;
    private String contentType;
    private String deviceType;
    private String quality;
    private Instant startedAt;
    private Instant lastActivityAt;
    private Long currentPosition;
    private String status; // STARTED, PLAYING, PAUSED, COMPLETED
    private Long totalWatchTime;
    private Integer qualityChanges;
    private Long totalBufferingTime;
    private String finalQuality;

    // Metadata
    private String tenantId;
    private Instant createdAt;
    private Instant updatedAt;

    // Business computed fields
    private Double completionPercentage;
    private Long sessionDuration; // in milliseconds
    private Boolean isActive;

    public PlaybackSessionProjection(String sessionId, String tenantId) {
        this.sessionId = sessionId;
        this.tenantId = tenantId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}

/**
 * Repository for playback session queries
 */
@Repository
public interface PlaybackSessionRepository extends JpaRepository<PlaybackSessionProjection, String> {

    // Find active sessions for a user
    @Query("SELECT p FROM PlaybackSessionProjection p WHERE p.userId = :userId AND p.isActive = true")
    List<PlaybackSessionProjection> findActiveSessionsByUser(@Param("userId") String userId);

    // Find sessions for content analytics
    @Query("SELECT p FROM PlaybackSessionProjection p WHERE p.contentId = :contentId AND p.tenantId = :tenantId")
    List<PlaybackSessionProjection> findByContentId(@Param("contentId") String contentId, @Param("tenantId") String tenantId);

    // Find recent sessions for user
    @Query("SELECT p FROM PlaybackSessionProjection p WHERE p.userId = :userId ORDER BY p.lastActivityAt DESC")
    List<PlaybackSessionProjection> findRecentSessionsByUser(@Param("userId") String userId);

    // Count active sessions
    @Query("SELECT COUNT(p) FROM PlaybackSessionProjection p WHERE p.isActive = true AND p.tenantId = :tenantId")
    Long countActiveSessions(@Param("tenantId") String tenantId);
}

/**
 * Event Handler that projects events to read models
 */
@Component
public class PlaybackSessionProjectionHandler {

    private final PlaybackSessionRepository repository;

    public PlaybackSessionProjectionHandler(PlaybackSessionRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(PlaybackStartedEvent event) {
        var projection = new PlaybackSessionProjection(event.getSessionId(), event.getTenantId());
        projection.setUserId(event.getUserId());
        projection.setContentId(event.getContentId());
        projection.setContentType(event.getContentType());
        projection.setDeviceType(event.getDeviceType());
        projection.setQuality(event.getQuality());
        projection.setStartedAt(event.getTimestamp());
        projection.setLastActivityAt(event.getTimestamp());
        projection.setCurrentPosition(event.getStartPosition());
        projection.setStatus("STARTED");
        projection.setTotalWatchTime(0L);
        projection.setQualityChanges(0);
        projection.setTotalBufferingTime(0L);
        projection.setIsActive(true);

        repository.save(projection);
    }

    @EventHandler
    public void on(PlaybackPausedEvent event) {
        repository.findById(event.getSessionId()).ifPresent(projection -> {
            projection.setStatus("PAUSED");
            projection.setLastActivityAt(event.getTimestamp());
            projection.setCurrentPosition(event.getPausedAtPosition());
            projection.setUpdatedAt(Instant.now());
            repository.save(projection);
        });
    }

    @EventHandler
    public void on(PlaybackResumedEvent event) {
        repository.findById(event.getSessionId()).ifPresent(projection -> {
            projection.setStatus("PLAYING");
            projection.setLastActivityAt(event.getTimestamp());
            projection.setUpdatedAt(Instant.now());
            repository.save(projection);
        });
    }

    @EventHandler
    public void on(PlaybackCompletedEvent event) {
        repository.findById(event.getSessionId()).ifPresent(projection -> {
            projection.setStatus("COMPLETED");
            projection.setLastActivityAt(event.getTimestamp());
            projection.setFinalQuality(event.getFinalQuality());
            projection.setTotalWatchTime(event.getTimeWatched());
            projection.setCompletionPercentage(event.getCompletionPercentage());
            projection.setSessionDuration(
                projection.getLastActivityAt().toEpochMilli() - projection.getStartedAt().toEpochMilli()
            );
            projection.setIsActive(false);
            projection.setUpdatedAt(Instant.now());
            repository.save(projection);
        });
    }
}

/**
 * Query Handlers for CQRS Query side
 */
@Component
public class PlaybackSessionQueryHandler {

    private final PlaybackSessionRepository repository;

    public PlaybackSessionQueryHandler(PlaybackSessionRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public Optional<PlaybackSessionProjection> handle(GetPlaybackSessionQuery query) {
        return repository.findById(query.getSessionId());
    }

    @QueryHandler
    public List<PlaybackSessionProjection> handle(GetActiveSessionsByUserQuery query) {
        return repository.findActiveSessionsByUser(query.getUserId());
    }

    @QueryHandler
    public List<PlaybackSessionProjection> handle(GetSessionsByContentQuery query) {
        return repository.findByContentId(query.getContentId(), query.getTenantId());
    }

    @QueryHandler
    public PlaybackSessionStats handle(GetPlaybackStatsQuery query) {
        var activeCount = repository.countActiveSessions(query.getTenantId());
        // Additional stats can be computed here
        return new PlaybackSessionStats(activeCount, 0L, 0.0);
    }
}

// Query classes
class GetPlaybackSessionQuery {
    private final String sessionId;
    public GetPlaybackSessionQuery(String sessionId) { this.sessionId = sessionId; }
    public String getSessionId() { return sessionId; }
}

class GetActiveSessionsByUserQuery {
    private final String userId;
    public GetActiveSessionsByUserQuery(String userId) { this.userId = userId; }
    public String getUserId() { return userId; }
}

class GetSessionsByContentQuery {
    private final String contentId;
    private final String tenantId;
    public GetSessionsByContentQuery(String contentId, String tenantId) {
        this.contentId = contentId;
        this.tenantId = tenantId;
    }
    public String getContentId() { return contentId; }
    public String getTenantId() { return tenantId; }
}

class GetPlaybackStatsQuery {
    private final String tenantId;
    public GetPlaybackStatsQuery(String tenantId) { this.tenantId = tenantId; }
    public String getTenantId() { return tenantId; }
}

class PlaybackSessionStats {
    private final Long activeSessions;
    private final Long totalSessionsToday;
    private final Double avgCompletionRate;

    public PlaybackSessionStats(Long activeSessions, Long totalSessionsToday, Double avgCompletionRate) {
        this.activeSessions = activeSessions;
        this.totalSessionsToday = totalSessionsToday;
        this.avgCompletionRate = avgCompletionRate;
    }

    public Long getActiveSessions() { return activeSessions; }
    public Long getTotalSessionsToday() { return totalSessionsToday; }
    public Double getAvgCompletionRate() { return avgCompletionRate; }
}