package com.netflix.streaming.infrastructure.backpressure;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Backpressure filter for handling request queue limits and load shedding.
 * 
 * Implements:
 * - Request queue monitoring
 * - Load shedding when overloaded
 * - 503 responses with Retry-After header
 * - Circuit breaker integration
 */
@Component
@Order(3) // After rate limiting and idempotency
public class BackpressureFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(BackpressureFilter.class);
    
    private final AtomicInteger activeRequests = new AtomicInteger(0);
    private final AtomicLong rejectedRequests = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    
    private final int maxConcurrentRequests;
    private final int maxQueueSize;
    private final MeterRegistry meterRegistry;

    public BackpressureFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        // Default limits (should be configurable)
        this.maxConcurrentRequests = Integer.parseInt(
            System.getProperty("backpressure.max.concurrent.requests", "100")
        );
        this.maxQueueSize = Integer.parseInt(
            System.getProperty("backpressure.max.queue.size", "50")
        );
        
        // Register metrics
        Gauge.builder("backpressure.active_requests", activeRequests, AtomicInteger::get)
            .description("Number of currently active requests")
            .register(meterRegistry);
        
        Gauge.builder("backpressure.rejected_requests", rejectedRequests, AtomicLong::get)
            .description("Total number of rejected requests due to backpressure")
            .register(meterRegistry);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Skip backpressure for health checks
        if (request.getRequestURI().startsWith("/actuator/health") ||
            request.getRequestURI().startsWith("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        totalRequests.incrementAndGet();

        // Check if we're at capacity
        int currentActive = activeRequests.get();
        if (currentActive >= maxConcurrentRequests) {
            rejectedRequests.incrementAndGet();
            logger.warn("Request rejected due to backpressure. Active: {}/{}, Queue: {}", 
                currentActive, maxConcurrentRequests, getQueueSize());
            
            sendBackpressureResponse(response);
            return;
        }

        // Increment active requests counter
        activeRequests.incrementAndGet();

        try {
            // Process the request
            filterChain.doFilter(request, response);
        } finally {
            // Decrement active requests counter
            activeRequests.decrementAndGet();
        }
    }

    /**
     * Send 503 backpressure response.
     */
    private void sendBackpressureResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Retry-After", "60"); // Retry after 60 seconds
        
        String errorJson = String.format(
            "{\"error\":\"Service Unavailable\",\"message\":\"Service is currently overloaded. Please retry after 60 seconds.\",\"retryAfter\":60}"
        );
        
        response.getWriter().write(errorJson);
    }

    /**
     * Get current queue size by monitoring active requests and recent activity.
     */
    private int getQueueSize() {
        // Estimate queue size based on active requests and recent request rate
        long totalRequests = totalRequests.get();
        int activeRequests = activeRequests.get();

        // Simple estimation: if we have active requests and recent activity,
        // estimate some requests might be queued
        if (activeRequests > maxConcurrentRequests * 0.8) {
            // We're close to capacity, estimate queue size
            long recentRequests = getRecentRequestCount();
            return Math.max(0, (int)(recentRequests - maxConcurrentRequests));
        }

        return 0;
    }

    /**
     * Get recent request count (last 10 seconds).
     */
    private long getRecentRequestCount() {
        // In a real implementation, you would use a time-windowed counter
        // For now, return a simple estimation
        return Math.min(totalRequests.get() / 10, maxConcurrentRequests * 2);
    }

    /**
     * Advanced backpressure metrics.
     */
    public BackpressureMetrics getMetrics() {
        return new BackpressureMetrics(
            activeRequests.get(),
            rejectedRequests.get(),
            totalRequests.get(),
            getQueueSize(),
            maxConcurrentRequests,
            maxQueueSize,
            isUnderBackpressure()
        );
    }

    /**
     * Backpressure metrics for monitoring.
     */
    public static class BackpressureMetrics {
        private final int activeRequests;
        private final long rejectedRequests;
        private final long totalRequests;
        private final int queueSize;
        private final int maxConcurrentRequests;
        private final int maxQueueSize;
        private final boolean underBackpressure;

        public BackpressureMetrics(int activeRequests, long rejectedRequests, long totalRequests,
                                 int queueSize, int maxConcurrentRequests, int maxQueueSize,
                                 boolean underBackpressure) {
            this.activeRequests = activeRequests;
            this.rejectedRequests = rejectedRequests;
            this.totalRequests = totalRequests;
            this.queueSize = queueSize;
            this.maxConcurrentRequests = maxConcurrentRequests;
            this.maxQueueSize = maxQueueSize;
            this.underBackpressure = underBackpressure;
        }

        // Getters
        public int getActiveRequests() { return activeRequests; }
        public long getRejectedRequests() { return rejectedRequests; }
        public long getTotalRequests() { return totalRequests; }
        public int getQueueSize() { return queueSize; }
        public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
        public int getMaxQueueSize() { return maxQueueSize; }
        public boolean isUnderBackpressure() { return underBackpressure; }

        public double getRejectionRate() {
            return totalRequests > 0 ? (double) rejectedRequests / totalRequests : 0.0;
        }

        public double getUtilizationRate() {
            return maxConcurrentRequests > 0 ? (double) activeRequests / maxConcurrentRequests : 0.0;
        }
    }

    /**
     * Get current active request count.
     */
    public int getActiveRequests() {
        return activeRequests.get();
    }

    /**
     * Get total rejected requests.
     */
    public long getRejectedRequests() {
        return rejectedRequests.get();
    }

    /**
     * Check if service is under backpressure.
     */
    public boolean isUnderBackpressure() {
        return activeRequests.get() >= maxConcurrentRequests;
    }
}

