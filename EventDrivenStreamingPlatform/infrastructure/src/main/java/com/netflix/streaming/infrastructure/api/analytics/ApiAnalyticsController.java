package com.netflix.streaming.infrastructure.api.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * API Analytics Controller.
 *
 * Provides endpoints for accessing API analytics and metrics.
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class ApiAnalyticsController {

    private final ApiAnalyticsService analyticsService;

    public ApiAnalyticsController(ApiAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get analytics summary for a specific endpoint.
     */
    @GetMapping("/endpoints/{method}/{path}")
    public ResponseEntity<ApiAnalyticsService.ApiEndpointSummary> getEndpointSummary(
            @PathVariable String method,
            @PathVariable String path) {

        ApiAnalyticsService.ApiEndpointSummary summary = analyticsService.getEndpointSummary(method, path);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get top endpoints by request count.
     */
    @GetMapping("/endpoints/top")
    public ResponseEntity<List<ApiAnalyticsService.ApiEndpointSummary>> getTopEndpoints(
            @RequestParam(defaultValue = "10") int limit) {

        List<ApiAnalyticsService.ApiEndpointSummary> topEndpoints = analyticsService.getTopEndpoints(limit);
        return ResponseEntity.ok(topEndpoints);
    }

    /**
     * Get API analytics overview.
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getAnalyticsOverview() {
        List<ApiAnalyticsService.ApiEndpointSummary> topEndpoints = analyticsService.getTopEndpoints(20);

        long totalRequests = topEndpoints.stream()
            .mapToLong(ApiAnalyticsService.ApiEndpointSummary::getTotalRequests)
            .sum();

        long totalErrors = topEndpoints.stream()
            .mapToLong(ApiAnalyticsService.ApiEndpointSummary::getErrorCount)
            .sum();

        double avgResponseTime = topEndpoints.stream()
            .mapToDouble(ApiAnalyticsService.ApiEndpointSummary::getAvgResponseTime)
            .average()
            .orElse(0.0);

        Map<String, Object> overview = Map.of(
            "totalRequests", totalRequests,
            "totalErrors", totalErrors,
            "errorRate", totalRequests > 0 ? (double) totalErrors / totalRequests : 0.0,
            "avgResponseTime", avgResponseTime,
            "topEndpoints", topEndpoints.subList(0, Math.min(5, topEndpoints.size()))
        );

        return ResponseEntity.ok(overview);
    }

    /**
     * Get API health metrics.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getApiHealth() {
        List<ApiAnalyticsService.ApiEndpointSummary> topEndpoints = analyticsService.getTopEndpoints(10);

        double overallErrorRate = topEndpoints.stream()
            .mapToDouble(ApiAnalyticsService.ApiEndpointSummary::getErrorRate)
            .average()
            .orElse(0.0);

        double overallAvgResponseTime = topEndpoints.stream()
            .mapToDouble(ApiAnalyticsService.ApiEndpointSummary::getAvgResponseTime)
            .average()
            .orElse(0.0);

        String healthStatus = determineHealthStatus(overallErrorRate, overallAvgResponseTime);

        Map<String, Object> health = Map.of(
            "status", healthStatus,
            "errorRate", overallErrorRate,
            "avgResponseTime", overallAvgResponseTime,
            "totalEndpoints", topEndpoints.size(),
            "timestamp", java.time.Instant.now()
        );

        return ResponseEntity.ok(health);
    }

    /**
     * Determine API health status based on metrics.
     */
    private String determineHealthStatus(double errorRate, double avgResponseTime) {
        if (errorRate > 0.05 || avgResponseTime > 5000) { // 5% error rate or 5s avg response time
            return "CRITICAL";
        } else if (errorRate > 0.01 || avgResponseTime > 2000) { // 1% error rate or 2s avg response time
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }
}
