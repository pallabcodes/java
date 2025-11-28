package com.netflix.streaming.analytics.processor;

import com.netflix.streaming.analytics.dashboard.RealTimeDashboardService;
import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.PlaybackCompletedEvent;
import com.netflix.streaming.events.PlaybackStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Real-Time Analytics Processor.
 *
 * Processes events for immediate metrics and dashboard updates.
 * Uses Redis for fast, ephemeral real-time data.
 */
@Component
public class RealTimeAnalyticsProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeAnalyticsProcessor.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RealTimeDashboardService dashboardService;

    // In-memory counters for ultra-fast access
    private final AtomicLong activeStreams = new AtomicLong(0);
    private final AtomicLong totalStreamsToday = new AtomicLong(0);
    private final AtomicLong completedStreamsToday = new AtomicLong(0);

    public RealTimeAnalyticsProcessor(RedisTemplate<String, Object> redisTemplate,
                                     RealTimeDashboardService dashboardService) {
        this.redisTemplate = redisTemplate;
        this.dashboardService = dashboardService;
    }

    /**
     * Process event for real-time metrics
     */
    public void processEvent(BaseEvent event) {
        if (event instanceof PlaybackStartedEvent) {
            handlePlaybackStarted((PlaybackStartedEvent) event);
        } else if (event instanceof PlaybackCompletedEvent) {
            handlePlaybackCompleted((PlaybackCompletedEvent) event);
        }

        // Broadcast real-time updates to dashboard subscribers
        dashboardService.broadcastMetricsUpdate(getRealTimeMetrics());
    }

    private void handlePlaybackStarted(PlaybackStartedEvent event) {
        activeStreams.incrementAndGet();

        // Update Redis for persistence and cross-instance sharing
        redisTemplate.opsForValue().increment("analytics:active_streams");
        redisTemplate.opsForValue().increment("analytics:total_streams_today");

        // Track by content
        String contentKey = "analytics:content:" + event.getContentId() + ":active_streams";
        redisTemplate.opsForValue().increment(contentKey);

        // Track by region
        String regionKey = "analytics:region:" + event.getRegion() + ":active_streams";
        redisTemplate.opsForValue().increment(regionKey);

        logger.debug("Real-time: Playback started - active streams: {}", activeStreams.get());
    }

    private void handlePlaybackCompleted(PlaybackCompletedEvent event) {
        activeStreams.decrementAndGet();
        completedStreamsToday.incrementAndGet();

        // Update Redis
        redisTemplate.opsForValue().decrement("analytics:active_streams");
        redisTemplate.opsForValue().increment("analytics:completed_streams_today");

        // Update completion rate
        long total = totalStreamsToday.get();
        long completed = completedStreamsToday.get();
        if (total > 0) {
            double completionRate = (double) completed / total * 100;
            redisTemplate.opsForValue().set("analytics:completion_rate_today", completionRate);
        }

        logger.debug("Real-time: Playback completed - active streams: {}", activeStreams.get());
    }

    /**
     * Get real-time metrics snapshot
     */
    public RealTimeMetrics getRealTimeMetrics() {
        return RealTimeMetrics.builder()
            .activeStreams(activeStreams.get())
            .totalStreamsToday(totalStreamsToday.get())
            .completedStreamsToday(completedStreamsToday.get())
            .completionRateToday(calculateCompletionRate())
            .topContentByViews(getTopContentByViews(5))
            .regionalDistribution(getRegionalDistribution())
            .timestamp(Instant.now())
            .build();
    }

    /**
     * Get top content by current active views
     */
    private java.util.Map<String, Long> getTopContentByViews(int limit) {
        // In production, use Redis sorted sets or similar
        // For demo, return mock data
        java.util.Map<String, Long> topContent = new java.util.HashMap<>();
        topContent.put("movie-123", 1250L);
        topContent.put("show-456", 890L);
        topContent.put("movie-789", 675L);
        return topContent;
    }

    /**
     * Get regional distribution of active streams
     */
    private java.util.Map<String, Long> getRegionalDistribution() {
        java.util.Map<String, Long> regions = new java.util.HashMap<>();
        regions.put("us-east-1", 5200L);
        regions.put("eu-west-1", 3800L);
        regions.put("ap-southeast-1", 2900L);
        regions.put("us-west-2", 2100L);
        return regions;
    }

    private double calculateCompletionRate() {
        long total = totalStreamsToday.get();
        long completed = completedStreamsToday.get();
        return total > 0 ? (double) completed / total * 100 : 0.0;
    }

    /**
     * Reset daily counters (called by scheduled job)
     */
    public void resetDailyCounters() {
        totalStreamsToday.set(0);
        completedStreamsToday.set(0);

        // Reset Redis counters
        redisTemplate.delete("analytics:total_streams_today");
        redisTemplate.delete("analytics:completed_streams_today");

        logger.info("Reset daily real-time counters");
    }

    /**
     * Real-time metrics data structure
     */
    @lombok.Data
    @lombok.Builder
    public static class RealTimeMetrics {
        private long activeStreams;
        private long totalStreamsToday;
        private long completedStreamsToday;
        private double completionRateToday;
        private java.util.Map<String, Long> topContentByViews;
        private java.util.Map<String, Long> regionalDistribution;
        private Instant timestamp;
    }
}