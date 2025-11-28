package com.netflix.streaming.analytics.projection;

import com.netflix.streaming.events.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.stereotype.Component;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User Analytics Projection.
 *
 * CQRS Read Model for user-centric analytics built from event streams.
 * This demonstrates event-driven projections for personalized analytics.
 */
@Component
public class UserAnalyticsProjection {

    private static final Logger logger = LoggerFactory.getLogger(UserAnalyticsProjection.class);

    // In-memory cache for real-time metrics (Redis-backed for production)
    private final Map<String, UserAnalyticsView> userCache = new ConcurrentHashMap<>();

    /**
     * Handle playback started events
     */
    public void handle(PlaybackStartedEvent event) {
        String userId = event.getUserId();
        UserAnalyticsView analytics = getOrCreateUserAnalytics(userId);

        // Update session tracking
        analytics.incrementActiveSessions();
        analytics.setLastActivityAt(event.getTimestamp());
        analytics.getDevicePreferences().merge(event.getDeviceType(), 1L, Long::sum);
        analytics.getQualityPreferences().merge(event.getQuality(), 1L, Long::sum);

        logger.debug("Updated user analytics for {} on playback start", userId);
    }

    /**
     * Handle playback completed events
     */
    public void handle(PlaybackCompletedEvent event) {
        String userId = event.getUserId();
        UserAnalyticsView analytics = getOrCreateUserAnalytics(userId);

        // Update completion metrics
        analytics.setTotalWatchTime(analytics.getTotalWatchTime() + event.getTimeWatched());
        analytics.setTotalSessions(analytics.getTotalSessions() + 1);
        analytics.setCompletedSessions(analytics.getCompletedSessions() + 1);
        analytics.setLastActivityAt(event.getTimestamp());

        // Calculate completion rate
        double completionRate = (double) analytics.getCompletedSessions() / analytics.getTotalSessions();
        analytics.setCompletionRate(completionRate);

        // Update content preferences
        analytics.getContentTypePreferences().merge(event.getContentType(), 1L, Long::sum);

        // Update engagement score (simplified calculation)
        double engagementScore = calculateEngagementScore(analytics);
        analytics.setEngagementScore(engagementScore);

        logger.debug("Updated user analytics for {} on playback complete", userId);
    }

    /**
     * Handle playback paused events
     */
    public void handle(PlaybackPausedEvent event) {
        // Could track pause patterns, but minimal impact on user analytics
        logger.trace("Playback paused for session: {}", event.getSessionId());
    }

    /**
     * Handle playback resumed events
     */
    public void handle(PlaybackResumedEvent event) {
        // Could track resume patterns for engagement analysis
        logger.trace("Playback resumed for session: {}", event.getSessionId());
    }

    /**
     * Get user analytics view
     */
    public UserAnalyticsView getUserAnalytics(String userId) {
        return userCache.get(userId);
    }

    /**
     * Get all user analytics (for admin/batch operations)
     */
    public Map<String, UserAnalyticsView> getAllUserAnalytics() {
        return new HashMap<>(userCache);
    }

    /**
     * Clear old/inactive user analytics (cleanup job)
     */
    public void cleanupInactiveUsers(Instant cutoffTime) {
        userCache.entrySet().removeIf(entry ->
            entry.getValue().getLastActivityAt().isBefore(cutoffTime));
        logger.info("Cleaned up inactive user analytics before {}", cutoffTime);
    }

    private UserAnalyticsView getOrCreateUserAnalytics(String userId) {
        return userCache.computeIfAbsent(userId, id -> {
            UserAnalyticsView analytics = new UserAnalyticsView();
            analytics.setUserId(id);
            analytics.setCreatedAt(Instant.now());
            analytics.setLastActivityAt(Instant.now());
            return analytics;
        });
    }

    private double calculateEngagementScore(UserAnalyticsView analytics) {
        // Simplified engagement score calculation
        // Netflix-like: combines watch time, completion rate, session frequency
        double watchTimeScore = Math.min(analytics.getTotalWatchTime() / (30.0 * 60 * 60 * 1000), 1.0); // Max 30 hours
        double completionScore = analytics.getCompletionRate();
        double sessionFrequencyScore = Math.min(analytics.getTotalSessions() / 10.0, 1.0); // Max 10 sessions

        return (watchTimeScore * 0.4 + completionScore * 0.4 + sessionFrequencyScore * 0.2) * 100;
    }

    /**
     * User Analytics View - CQRS Read Model
     */
    @Data
    @NoArgsConstructor
    public static class UserAnalyticsView {

        private String userId;
        private Instant createdAt;
        private Instant lastActivityAt;

        // Session metrics
        private int activeSessions = 0;
        private int totalSessions = 0;
        private int completedSessions = 0;
        private long totalWatchTime = 0; // milliseconds

        // Engagement metrics
        private double completionRate = 0.0;
        private double engagementScore = 0.0;

        // Preference tracking
        private Map<String, Long> devicePreferences = new HashMap<>();
        private Map<String, Long> qualityPreferences = new HashMap<>();
        private Map<String, Long> contentTypePreferences = new HashMap<>();

        // Behavioral patterns
        private Instant firstSessionAt;
        private Instant lastCompletedSessionAt;
        private int consecutiveDayStreak = 0;
        private boolean isPremiumUser = false;

        // Getters and setters provided by Lombok @Data
    }

    /**
     * Redis-backed version for production (when we need persistence)
     */
    @RedisHash("user_analytics")
    @Data
    public static class PersistentUserAnalyticsView {

        @Id
        private String userId;

        private Instant createdAt;
        private Instant lastActivityAt;

        // Core metrics
        private int totalSessions;
        private int completedSessions;
        private long totalWatchTime;

        private double completionRate;
        private double engagementScore;

        // Preferences as JSON strings (Redis limitation)
        private String devicePreferencesJson;
        private String qualityPreferencesJson;
        private String contentTypePreferencesJson;

        @TimeToLive
        private Long ttl = 30 * 24 * 60 * 60L; // 30 days TTL
    }
}