package com.example.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    // Rate limits: 100 requests per minute per IP
    private static final int REQUESTS_PER_MINUTE = 100;
    private static final int REFILL_PERIOD_MINUTES = 1;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(REQUESTS_PER_MINUTE,
                        Refill.intervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(REFILL_PERIOD_MINUTES))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {

        String clientIp = getClientIpAddress(request);

        // Skip rate limiting for actuator endpoints
        if (request.getRequestURI().startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());

        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            response.setHeader("X-Rate-Limit-Retry-After-Seconds",
                    String.valueOf(Duration.ofMinutes(REFILL_PERIOD_MINUTES).getSeconds()));

            chain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            logger.warn("Rate limit exceeded for IP: {}", clientIp);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(Duration.ofMinutes(REFILL_PERIOD_MINUTES).getSeconds()));
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "error": "Too Many Requests",
                    "message": "Rate limit exceeded. Please try again later.",
                    "retryAfterSeconds": """ + Duration.ofMinutes(REFILL_PERIOD_MINUTES).getSeconds() + """
                }
            """);
        }
    }

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
