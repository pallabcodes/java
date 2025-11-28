package com.netflix.streaming.analytics.projection;

import com.netflix.streaming.events.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Content Analytics Projection.
 *
 * CQRS Read Model for content-centric analytics built from event streams.
 * This demonstrates complex event processing for content performance metrics.
 */
@Component
public class ContentAnalyticsProjection {

    private static final Logger logger = LoggerFactory.getLogger(ContentAnalyticsProjection.class);

    // In-memory cache for real-time metrics
    private final Map<String, ContentAnalyticsView> contentCache = new ConcurrentHashMap<>();

    /**
     * Handle playback started events
     */
    public void handle(PlaybackStartedEvent event) {
        String contentId = event.getContentId();
        ContentAnalyticsView analytics = getOrCreateContentAnalytics(contentId);

        // Update viewership metrics
        analytics.incrementTotalViews();
        analytics.incrementUniqueViewers(event.getUserId());
        analytics.getDeviceDistribution().merge(event.getDeviceType(), 1L, Long::sum);
        analytics.getQualityDistribution().merge(event.getQuality(), 1L, Long::sum);
        analytics.getGeographicDistribution().merge(event.getRegion(), 1L, Long::sum);

        // Update time-based metrics
        updateTimeBasedMetrics(analytics, event.getTimestamp());

        logger.debug("Updated content analytics for {} on playback start", contentId);
    }

    /**
     * Handle playback completed events
     */
    public void handle(PlaybackCompletedEvent event) {
        String contentId = event.getContentId();
        ContentAnalyticsView analytics = getOrCreateContentAnalytics(contentId);

        // Update completion metrics
        analytics.setTotalWatchTime(analytics.getTotalWatchTime() + event.getTimeWatched());
        analytics.incrementCompletedViews();

        // Update completion rate
        double completionRate = (double) analytics.getCompletedViews() / analytics.getTotalViews();
        analytics.setAvgCompletionPercentage(completionRate * 100);

        // Update buffering metrics
        analytics.updateBufferingMetrics(event.getTotalBufferingTime());

        // Update drop-off analysis
        analytics.analyzeDropOffPatterns(event);

        // Calculate engagement score
        double engagementScore = calculateEngagementScore(analytics);
        analytics.setEngagementScore(engagementScore);

        logger.debug("Updated content analytics for {} on playback complete", contentId);
    }

    /**
     * Handle buffering events
     */
    public void handle(BufferingEvent event) {
        // Extract contentId from correlationId or maintain session-to-content mapping
        String contentId = extractContentIdFromCorrelation(event.getCorrelationId());
        if (contentId != null) {
            ContentAnalyticsView analytics = getOrCreateContentAnalytics(contentId);
            analytics.updateBufferingMetrics(event.getBufferingDuration());
        }
    }

    /**
     * Handle quality change events
     */
    public void handle(QualityChangedEvent event) {
        String contentId = extractContentIdFromCorrelation(event.getCorrelationId());
        if (contentId != null) {
            ContentAnalyticsView analytics = getOrCreateContentAnalytics(contentId);
            analytics.incrementQualityChanges();
            analytics.getQualityDistribution().merge(event.getNewQuality(), 1L, Long::sum);
        }
    }

    /**
     * Get content analytics view
     */
    public ContentAnalyticsView getContentAnalytics(String contentId) {
        return contentCache.get(contentId);
    }

    /**
     * Get top content by engagement score
     */
    public Map<String, Double> getTopContentByEngagement(int limit) {
        return contentCache.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue().getEngagementScore(),
                                             e1.getValue().getEngagementScore()))
            .limit(limit)
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().getEngagementScore()
            ));
    }

    /**
     * Get content performance summary
     */
    public Map<String, Object> getContentPerformanceSummary(String contentId) {
        ContentAnalyticsView analytics = contentCache.get(contentId);
        if (analytics == null) return Map.of();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalViews", analytics.getTotalViews());
        summary.put("uniqueViewers", analytics.getUniqueViewers());
        summary.put("completionRate", analytics.getAvgCompletionPercentage());
        summary.put("avgBufferingTime", analytics.getAvgBufferingTime());
        summary.put("engagementScore", analytics.getEngagementScore());
        summary.put("qualityChanges", analytics.getQualityChanges());

        return summary;
    }

    private ContentAnalyticsView getOrCreateContentAnalytics(String contentId) {
        return contentCache.computeIfAbsent(contentId, id -> {
            ContentAnalyticsView analytics = new ContentAnalyticsView();
            analytics.setContentId(id);
            analytics.setCreatedAt(Instant.now());
            analytics.setLastActivityAt(Instant.now());
            return analytics;
        });
    }

    private void updateTimeBasedMetrics(ContentAnalyticsView analytics, Instant eventTime) {
        Instant now = Instant.now();
        analytics.setLastActivityAt(now);

        // Update hourly metrics (last 24 hours)
        if (eventTime.isAfter(now.minus(24, ChronoUnit.HOURS))) {
            analytics.setViewsLast24h(analytics.getViewsLast24h() + 1);
        }

        // Update daily metrics (last 7 days)
        if (eventTime.isAfter(now.minus(7, ChronoUnit.DAYS))) {
            analytics.setViewsLast7d(analytics.getViewsLast7d() + 1);
        }

        // Update monthly metrics (last 30 days)
        if (eventTime.isAfter(now.minus(30, ChronoUnit.DAYS))) {
            analytics.setViewsLast30d(analytics.getViewsLast30d() + 1);
        }
    }

    private double calculateEngagementScore(ContentAnalyticsView analytics) {
        // Netflix-style engagement score calculation
        double completionWeight = 0.4;
        double viewershipWeight = 0.3;
        double qualityWeight = 0.2;
        double bufferingPenalty = 0.1;

        double completionScore = Math.min(analytics.getAvgCompletionPercentage() / 100.0, 1.0);
        double viewershipScore = Math.min(analytics.getTotalViews() / 1000.0, 1.0); // Max at 1000 views
        double qualityScore = Math.max(0, 1.0 - (analytics.getQualityChanges() / (double) analytics.getTotalViews()));
        double bufferingPenaltyScore = Math.max(0, 1.0 - (analytics.getAvgBufferingTime() / 10000.0)); // Penalty over 10s

        return (completionScore * completionWeight +
                viewershipScore * viewershipWeight +
                qualityScore * qualityWeight +
                bufferingPenaltyScore * bufferingPenalty) * 100;
    }

    private String extractContentIdFromCorrelation(String correlationId) {
        // In production, you'd have a correlation ID to content mapping service
        // For demo, we'll use a simple parsing approach
        if (correlationId != null && correlationId.contains("_")) {
            String[] parts = correlationId.split("_");
            if (parts.length >= 2) {
                return parts[1]; // Extract content ID from correlation pattern
            }
        }
        return null;
    }

    /**
     * Content Analytics View - CQRS Read Model
     */
    @Data
    @NoArgsConstructor
    public static class ContentAnalyticsView {

        private String contentId;
        private Instant createdAt;
        private Instant lastActivityAt;

        // Core viewership metrics
        private long totalViews = 0;
        private long uniqueViewers = 0;
        private long completedViews = 0;
        private long totalWatchTime = 0; // milliseconds

        // Quality metrics
        private double avgCompletionPercentage = 0.0;
        private long totalBufferingTime = 0; // milliseconds
        private double avgBufferingTime = 0.0;
        private int qualityChanges = 0;

        // Engagement score (0-100)
        private double engagementScore = 0.0;

        // Distribution analytics
        private Map<String, Long> deviceDistribution = new HashMap<>();
        private Map<String, Long> qualityDistribution = new HashMap<>();
        private Map<String, Long> geographicDistribution = new HashMap<>();
        private Map<Long, Long> dropOffPoints = new HashMap<>(); // position -> drop-off count

        // Time-based metrics
        private long viewsLast24h = 0;
        private long viewsLast7d = 0;
        private long viewsLast30d = 0;

        // Behavioral insights
        private double avgSessionDuration = 0.0;
        private Map<String, Double> userRetentionByDay = new HashMap<>();

        // Helper methods
        public void incrementTotalViews() { this.totalViews++; }
        public void incrementCompletedViews() { this.completedViews++; }
        public void incrementQualityChanges() { this.qualityChanges++; }

        public void incrementUniqueViewers(String userId) {
            // In production, use a HyperLogLog or similar for unique counting
            // For demo, we'll use a simple approximation
            this.uniqueViewers = (long) (this.totalViews * 0.7); // Rough approximation
        }

        public void updateBufferingMetrics(long bufferingDuration) {
            this.totalBufferingTime += bufferingDuration;
            this.avgBufferingTime = (double) this.totalBufferingTime / this.totalViews;
        }

        public void analyzeDropOffPatterns(PlaybackCompletedEvent event) {
            // Simplified drop-off analysis
            // In production, you'd analyze where users drop off during playback
            long completionPosition = event.getContentDuration();
            this.dropOffPoints.merge(completionPosition, 1L, Long::sum);
        }
    }
}