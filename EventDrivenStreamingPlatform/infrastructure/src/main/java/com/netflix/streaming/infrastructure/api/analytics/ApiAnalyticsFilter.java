package com.netflix.streaming.infrastructure.api.analytics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API Analytics Filter.
 *
 * Captures API request/response metrics for analytics and monitoring.
 * Records request timing, response codes, payload sizes, and client information.
 */
@Component
@Order(1) // Run before other filters
public class ApiAnalyticsFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiAnalyticsFilter.class);

    private final ApiAnalyticsService analyticsService;

    public ApiAnalyticsFilter(ApiAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.nanoTime();

        // Capture request details
        String method = request.getMethod();
        String path = getRequestPath(request);
        String clientId = getClientId(request);
        String userAgent = request.getHeader("User-Agent");
        long requestSize = getRequestSize(request);

        // Wrap response to capture response details
        ResponseWrapper responseWrapper = new ResponseWrapper(response);

        try {
            // Process request
            filterChain.doFilter(request, responseWrapper);

            // Capture response details
            long endTime = System.nanoTime();
            long responseTimeMs = (endTime - startTime) / 1_000_000; // Convert to milliseconds
            int statusCode = responseWrapper.getStatus();
            long responseSize = responseWrapper.getContentLength();

            // Record analytics
            analyticsService.recordApiRequest(method, path, clientId, userAgent,
                                            responseTimeMs, statusCode, requestSize, responseSize);

            // Record errors separately for better error tracking
            if (statusCode >= 400) {
                String errorType = getErrorType(statusCode);
                analyticsService.recordApiError(method, path, statusCode, errorType,
                                              clientId, responseTimeMs);
            }

        } catch (Exception e) {
            // Record failed request
            long endTime = System.nanoTime();
            long responseTimeMs = (endTime - startTime) / 1_000_000;

            analyticsService.recordApiError(method, path, 500, "INTERNAL_SERVER_ERROR",
                                          clientId, responseTimeMs);

            throw e;
        }
    }

    /**
     * Get normalized request path (remove path variables).
     */
    private String getRequestPath(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Normalize path by removing numeric IDs (common pattern)
        // e.g., /api/users/123 -> /api/users/{id}
        path = path.replaceAll("/\\d+", "/{id}");

        return path;
    }

    /**
     * Extract client ID from request headers.
     */
    private String getClientId(HttpServletRequest request) {
        // Try different headers for client identification
        String clientId = request.getHeader("X-Client-ID");
        if (clientId != null) return clientId;

        clientId = request.getHeader("X-API-Key");
        if (clientId != null) return clientId;

        // For authenticated requests, could use user ID
        // For now, return null for anonymous requests
        return null;
    }

    /**
     * Estimate request size.
     */
    private long getRequestSize(HttpServletRequest request) {
        try {
            // Try to get content length from header
            String contentLength = request.getHeader("Content-Length");
            if (contentLength != null) {
                return Long.parseLong(contentLength);
            }

            // For requests with body, estimate size
            if (request.getContentLength() > 0) {
                return request.getContentLength();
            }

            // Default estimate for requests without body
            return request.getRequestURI().length() + request.getQueryString().length();

        } catch (Exception e) {
            return 0; // Default if unable to determine
        }
    }

    /**
     * Get error type from status code.
     */
    private String getErrorType(int statusCode) {
        if (statusCode >= 400 && statusCode < 500) {
            return "CLIENT_ERROR";
        } else if (statusCode >= 500) {
            return "SERVER_ERROR";
        } else {
            return "UNKNOWN_ERROR";
        }
    }

    /**
     * Skip analytics for certain paths.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip health checks, metrics, and static resources
        return path.startsWith("/actuator/") ||
               path.startsWith("/health") ||
               path.startsWith("/metrics") ||
               path.startsWith("/favicon.ico") ||
               path.contains("/static/") ||
               path.contains("/css/") ||
               path.contains("/js/") ||
               path.contains("/images/");
    }

    /**
     * Response wrapper to capture response details.
     */
    private static class ResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {

        private int status = 200;
        private long contentLength = 0;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        @Override
        public void setContentLength(int len) {
            this.contentLength = len;
            super.setContentLength(len);
        }

        @Override
        public void setContentLengthLong(long len) {
            this.contentLength = len;
            super.setContentLengthLong(len);
        }

        public int getStatus() {
            return status;
        }

        public long getContentLength() {
            return contentLength;
        }
    }
}
