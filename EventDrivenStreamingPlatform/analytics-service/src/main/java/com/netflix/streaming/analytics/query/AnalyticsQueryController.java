package com.netflix.streaming.analytics.query;

import com.netflix.streaming.analytics.dashboard.RealTimeDashboardService;
import com.netflix.streaming.analytics.projection.ContentAnalyticsProjection;
import com.netflix.streaming.analytics.projection.UserAnalyticsProjection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for Analytics Queries.
 *
 * CQRS Query side - optimized read models for analytics data.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics API", description = "Real-time streaming analytics queries")
public class AnalyticsQueryController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsQueryController.class);

    private final UserAnalyticsProjection userProjection;
    private final ContentAnalyticsProjection contentProjection;
    private final RealTimeMetricsService realTimeService;
    private final RealTimeDashboardService dashboardService;

    public AnalyticsQueryController(UserAnalyticsProjection userProjection,
                                   ContentAnalyticsProjection contentProjection,
                                   RealTimeMetricsService realTimeService,
                                   RealTimeDashboardService dashboardService) {
        this.userProjection = userProjection;
        this.contentProjection = contentProjection;
        this.realTimeService = realTimeService;
        this.dashboardService = dashboardService;
    }

    /**
     * Get real-time streaming metrics
     */
    @GetMapping("/realtime")
    @Operation(summary = "Get real-time streaming metrics",
               description = "Current active streams, completion rates, and live statistics")
    public ResponseEntity<RealTimeMetrics> getRealTimeMetrics() {
        logger.debug("Fetching real-time streaming metrics");
        RealTimeMetrics metrics = realTimeService.getRealTimeMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get user analytics
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user analytics",
               description = "User engagement metrics, preferences, and viewing patterns")
    public ResponseEntity<UserAnalyticsProjection.UserAnalyticsView> getUserAnalytics(@PathVariable String userId) {
        logger.debug("Fetching analytics for user: {}", userId);
        UserAnalyticsProjection.UserAnalyticsView analytics = userProjection.getUserAnalytics(userId);

        if (analytics != null) {
            return ResponseEntity.ok(analytics);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get content analytics
     */
    @GetMapping("/content/{contentId}")
    @Operation(summary = "Get content analytics",
               description = "Content performance metrics, viewership data, and engagement scores")
    public ResponseEntity<ContentAnalyticsProjection.ContentAnalyticsView> getContentAnalytics(@PathVariable String contentId) {
        logger.debug("Fetching analytics for content: {}", contentId);
        ContentAnalyticsProjection.ContentAnalyticsView analytics = contentProjection.getContentAnalytics(contentId);

        if (analytics != null) {
            return ResponseEntity.ok(analytics);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get top performing content
     */
    @GetMapping("/content/top/{limit}")
    @Operation(summary = "Get top performing content",
               description = "Content ranked by engagement score")
    public ResponseEntity<Map<String, Double>> getTopContent(@PathVariable int limit) {
        logger.debug("Fetching top {} content by engagement", limit);
        Map<String, Double> topContent = contentProjection.getTopContentByEngagement(limit);
        return ResponseEntity.ok(topContent);
    }

    /**
     * Get content performance summary
     */
    @GetMapping("/content/{contentId}/summary")
    @Operation(summary = "Get content performance summary",
               description = "Aggregated performance metrics for content")
    public ResponseEntity<Map<String, Object>> getContentSummary(@PathVariable String contentId) {
        logger.debug("Fetching performance summary for content: {}", contentId);
        Map<String, Object> summary = contentProjection.getContentPerformanceSummary(contentId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get all user analytics (admin endpoint)
     */
    @GetMapping("/users")
    @Operation(summary = "Get all user analytics (admin)",
               description = "Complete user analytics dataset - admin use only")
    public ResponseEntity<Map<String, UserAnalyticsProjection.UserAnalyticsView>> getAllUserAnalytics() {
        logger.debug("Fetching all user analytics");
        Map<String, UserAnalyticsProjection.UserAnalyticsView> allUsers = userProjection.getAllUserAnalytics();
        return ResponseEntity.ok(allUsers);
    }

    /**
     * Get dashboard statistics
     */
    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get dashboard statistics",
               description = "WebSocket connection stats and subscription counts")
    public ResponseEntity<RealTimeDashboardService.DashboardStats> getDashboardStats() {
        logger.debug("Fetching dashboard statistics");
        RealTimeDashboardService.DashboardStats stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check with analytics metrics
     */
    @GetMapping("/health")
    @Operation(summary = "Analytics service health",
               description = "Health check with analytics-specific metrics")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "analytics-service",
            "activeStreams", realTimeService.getRealTimeMetrics().getActiveStreams(),
            "connectedDashboards", dashboardService.getDashboardStats().getConnectedClients(),
            "timestamp", java.time.Instant.now()
        );
        return ResponseEntity.ok(health);
    }
}