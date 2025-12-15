package com.netflix.streaming.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiting filter for API protection.
 * 
 * Implements token bucket algorithm to prevent API abuse.
 */
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final RateLimitingConfig rateLimitingConfig;

    public RateLimitingFilter(RateLimitingConfig rateLimitingConfig) {
        this.rateLimitingConfig = rateLimitingConfig;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Skip rate limiting for health checks
        if (request.getRequestURI().startsWith("/actuator/health") ||
            request.getRequestURI().startsWith("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get client identifier (IP address or user ID)
        String clientKey = getClientKey(request);
        
        // Get rate limit bucket
        io.github.bucket4j.Bucket bucket = rateLimitingConfig.getBucket(clientKey);

        // Try to consume a token
        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            response.setHeader("X-RateLimit-Limit", String.valueOf(bucket.getAvailableTokens() + 1));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(
                System.currentTimeMillis() / 1000 + 60)); // Reset in 1 minute

            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            logger.warn("Rate limit exceeded for client: {}", clientKey);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60"); // Retry after 60 seconds
            
            response.getWriter().write(String.format(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please retry after %d seconds.\",\"retryAfter\":60}",
                System.currentTimeMillis() / 1000 + 60
            ));
        }
    }

    /**
     * Get client identifier for rate limiting
     */
    private String getClientKey(HttpServletRequest request) {
        // Try to get user ID from header or attribute
        String userId = request.getHeader("X-User-ID");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }

        // Fall back to IP address
        String ipAddress = getClientIpAddress(request);
        return "ip:" + ipAddress;
    }

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

