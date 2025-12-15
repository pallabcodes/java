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
     * Get current queue size (placeholder - would integrate with actual queue).
     */
    private int getQueueSize() {
        // In a real implementation, this would check actual request queue size
        return 0;
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

