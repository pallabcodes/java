package com.netflix.streaming.infrastructure.api.analytics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * API Analytics Service.
 *
 * Provides comprehensive analytics for API usage, performance, and patterns.
 * Tracks request/response metrics, error rates, and usage patterns.
 */
@Service
public class ApiAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(ApiAnalyticsService.class);

    private final MeterRegistry meterRegistry;
    private final Map<String, ApiEndpointMetrics> endpointMetrics = new ConcurrentHashMap<>();

    public ApiAnalyticsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record API request metrics.
     */
    public void recordApiRequest(String method, String path, String clientId, String userAgent,
                                long responseTimeMs, int statusCode, long requestSize, long responseSize) {

        String endpointKey = method + " " + path;

        // Get or create endpoint metrics
        ApiEndpointMetrics metrics = endpointMetrics.computeIfAbsent(endpointKey, k -> new ApiEndpointMetrics(k));

        // Record metrics
        metrics.recordRequest(responseTimeMs, statusCode, requestSize, responseSize);

        // Record client-specific metrics if available
        if (clientId != null && !clientId.isEmpty()) {
            recordClientMetrics(clientId, method, path, responseTimeMs, statusCode);
        }

        // Record user agent analytics
        if (userAgent != null && !userAgent.isEmpty()) {
            recordUserAgentAnalytics(userAgent, method, path);
        }

        logger.debug("Recorded API request: {} {} - {}ms, status: {}, client: {}",
            method, path, responseTimeMs, statusCode, clientId);
    }

    /**
     * Record API error with details.
     */
    public void recordApiError(String method, String path, int statusCode, String errorType,
                              String clientId, long responseTimeMs) {

        String endpointKey = method + " " + path;

        // Record error counter
        Counter.builder("api.errors")
            .tag("method", method)
            .tag("endpoint", path)
            .tag("status", String.valueOf(statusCode))
            .tag("error_type", errorType)
            .description("API error count by endpoint and type")
            .register(meterRegistry)
            .increment();

        // Record error rate for endpoint
        ApiEndpointMetrics metrics = endpointMetrics.get(endpointKey);
        if (metrics != null) {
            metrics.recordError();
        }

        logger.warn("Recorded API error: {} {} - status: {}, error: {}, client: {}",
            method, path, statusCode, errorType, clientId);
    }

    /**
     * Record client-specific metrics.
     */
    private void recordClientMetrics(String clientId, String method, String path,
                                   long responseTimeMs, int statusCode) {

        // Client request rate
        Counter.builder("api.client.requests")
            .tag("client_id", clientId)
            .tag("method", method)
            .description("API requests by client")
            .register(meterRegistry)
            .increment();

        // Client response time
        Timer.builder("api.client.response_time")
            .tag("client_id", clientId)
            .tag("method", method)
            .description("Client response times")
            .register(meterRegistry)
            .record(responseTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Client error rate
        if (statusCode >= 400) {
            Counter.builder("api.client.errors")
                .tag("client_id", clientId)
                .tag("status", String.valueOf(statusCode))
                .description("Client error count")
                .register(meterRegistry)
                .increment();
        }
    }

    /**
     * Record user agent analytics.
     */
    private void recordUserAgentAnalytics(String userAgent, String method, String path) {
        // Extract browser/client type from user agent
        String clientType = extractClientType(userAgent);

        Counter.builder("api.user_agent.requests")
            .tag("client_type", clientType)
            .tag("method", method)
            .description("Requests by user agent type")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Extract client type from user agent.
     */
    private String extractClientType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "unknown";
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "mobile";
        } else if (ua.contains("postman") || ua.contains("curl") || ua.contains("httpie")) {
            return "api_client";
        } else if (ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")) {
            return "bot";
        } else {
            return "web_browser";
        }
    }

    /**
     * Get analytics summary for an endpoint.
     */
    public ApiEndpointSummary getEndpointSummary(String method, String path) {
        String endpointKey = method + " " + path;
        ApiEndpointMetrics metrics = endpointMetrics.get(endpointKey);

        if (metrics == null) {
            return new ApiEndpointSummary(endpointKey, 0, 0, 0, 0, 0, 0);
        }

        return new ApiEndpointSummary(
            endpointKey,
            metrics.getTotalRequests(),
            metrics.getErrorCount(),
            metrics.getAvgResponseTime(),
            metrics.get95thPercentileResponseTime(),
            metrics.getTotalRequestSize(),
            metrics.getTotalResponseSize()
        );
    }

    /**
     * Get top endpoints by request count.
     */
    public java.util.List<ApiEndpointSummary> getTopEndpoints(int limit) {
        return endpointMetrics.values().stream()
            .sorted((a, b) -> Long.compare(b.getTotalRequests(), a.getTotalRequests()))
            .limit(limit)
            .map(metrics -> new ApiEndpointSummary(
                metrics.getEndpoint(),
                metrics.getTotalRequests(),
                metrics.getErrorCount(),
                metrics.getAvgResponseTime(),
                metrics.get95thPercentileResponseTime(),
                metrics.getTotalRequestSize(),
                metrics.getTotalResponseSize()
            ))
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Reset analytics data (useful for testing or periodic cleanup).
     */
    public void resetAnalytics() {
        endpointMetrics.clear();
        logger.info("Reset API analytics data");
    }

    /**
     * API endpoint metrics tracking.
     */
    private static class ApiEndpointMetrics {
        private final String endpoint;
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicLong totalRequestSize = new AtomicLong(0);
        private final AtomicLong totalResponseSize = new AtomicLong(0);

        // For percentile calculation (simplified)
        private final java.util.List<Long> responseTimes = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

        public ApiEndpointMetrics(String endpoint) {
            this.endpoint = endpoint;
        }

        public void recordRequest(long responseTimeMs, int statusCode, long requestSize, long responseSize) {
            totalRequests.incrementAndGet();
            totalResponseTime.addAndGet(responseTimeMs);
            totalRequestSize.addAndGet(requestSize);
            totalResponseSize.addAndGet(responseSize);

            // Keep last 1000 response times for percentile calculation
            synchronized (responseTimes) {
                responseTimes.add(responseTimeMs);
                if (responseTimes.size() > 1000) {
                    responseTimes.remove(0);
                }
            }

            if (statusCode >= 400) {
                errorCount.incrementAndGet();
            }
        }

        public void recordError() {
            errorCount.incrementAndGet();
        }

        public String getEndpoint() { return endpoint; }
        public long getTotalRequests() { return totalRequests.get(); }
        public long getErrorCount() { return errorCount.get(); }
        public long getTotalRequestSize() { return totalRequestSize.get(); }
        public long getTotalResponseSize() { return totalResponseSize.get(); }

        public double getAvgResponseTime() {
            long requests = totalRequests.get();
            return requests > 0 ? (double) totalResponseTime.get() / requests : 0.0;
        }

        public double get95thPercentileResponseTime() {
            synchronized (responseTimes) {
                if (responseTimes.isEmpty()) {
                    return 0.0;
                }

                java.util.List<Long> sorted = new java.util.ArrayList<>(responseTimes);
                java.util.Collections.sort(sorted);
                int index = (int) (sorted.size() * 0.95);
                return sorted.get(Math.min(index, sorted.size() - 1));
            }
        }
    }

    /**
     * API endpoint summary for reporting.
     */
    public static class ApiEndpointSummary {
        private final String endpoint;
        private final long totalRequests;
        private final long errorCount;
        private final double avgResponseTime;
        private final double p95ResponseTime;
        private final long totalRequestSize;
        private final long totalResponseSize;

        public ApiEndpointSummary(String endpoint, long totalRequests, long errorCount,
                                double avgResponseTime, double p95ResponseTime,
                                long totalRequestSize, long totalResponseSize) {
            this.endpoint = endpoint;
            this.totalRequests = totalRequests;
            this.errorCount = errorCount;
            this.avgResponseTime = avgResponseTime;
            this.p95ResponseTime = p95ResponseTime;
            this.totalRequestSize = totalRequestSize;
            this.totalResponseSize = totalResponseSize;
        }

        // Getters
        public String getEndpoint() { return endpoint; }
        public long getTotalRequests() { return totalRequests; }
        public long getErrorCount() { return errorCount; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public double getP95ResponseTime() { return p95ResponseTime; }
        public long getTotalRequestSize() { return totalRequestSize; }
        public long getTotalResponseSize() { return totalResponseSize; }

        public double getErrorRate() {
            return totalRequests > 0 ? (double) errorCount / totalRequests : 0.0;
        }
    }
}
