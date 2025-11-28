package org.example.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Rate limiting tiers
    private static final RateLimitTier FREE_TIER = new RateLimitTier(10, Duration.ofMinutes(1), "FREE");
    private static final RateLimitTier BASIC_TIER = new RateLimitTier(60, Duration.ofMinutes(1), "BASIC");
    private static final RateLimitTier PREMIUM_TIER = new RateLimitTier(300, Duration.ofMinutes(1), "PREMIUM");
    private static final RateLimitTier ENTERPRISE_TIER = new RateLimitTier(1000, Duration.ofMinutes(1), "ENTERPRISE");

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MeterRegistry meterRegistry;

    private final Counter rateLimitHits;
    private final Counter rateLimitBlocked;

    public RateLimitingFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.rateLimitHits = Counter.builder("producer_consumer.rate_limit.hits")
                .description("Number of rate limit hits")
                .register(meterRegistry);
        this.rateLimitBlocked = Counter.builder("producer_consumer.rate_limit.blocked")
                .description("Number of requests blocked by rate limiting")
                .register(meterRegistry);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String clientIp = getClientIpAddress(request);
        String userId = getUserId(request);
        String endpoint = request.getRequestURI();

        // Determine rate limit tier based on user/api key
        RateLimitTier tier = determineRateLimitTier(userId, request);

        // Check rate limit using Redis
        String rateLimitKey = buildRateLimitKey(clientIp, userId, endpoint);
        RateLimitResult limitResult = checkRateLimit(rateLimitKey, tier);

        if (!limitResult.allowed) {
            rateLimitBlocked.increment();

            // Record blocked request for monitoring
            meterRegistry.counter("producer_consumer.rate_limit.blocked_by_tier",
                    "tier", tier.name).increment();

            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(limitResult.retryAfterSeconds));
            response.getWriter().write(String.format(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Rate limit of %d requests per %s exceeded\",\"retryAfter\":\"%d\"}",
                tier.requests, formatDuration(tier.window), limitResult.retryAfterSeconds
            ));
            return;
        }

        rateLimitHits.increment();

        // Add rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(tier.requests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(limitResult.remainingRequests));
        response.setHeader("X-RateLimit-Reset", String.valueOf(limitResult.resetTime));
        response.setHeader("X-RateLimit-Tier", tier.name);

        chain.doFilter(request, response);
    }

    private RateLimitTier determineRateLimitTier(String userId, HttpServletRequest request) {
        // Check for API key in header
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null) {
            // In production, validate API key against database/cache
            return determineTierByApiKey(apiKey);
        }

        // Check user subscription tier (would come from JWT or database)
        if (userId != null && !userId.equals("anonymous")) {
            return determineTierByUser(userId);
        }

        // Default to free tier for anonymous users
        return FREE_TIER;
    }

    private RateLimitTier determineTierByApiKey(String apiKey) {
        // In production, this would query a database/cache
        // For demo, use a simple hash-based approach
        return switch (Math.abs(apiKey.hashCode()) % 4) {
            case 0 -> FREE_TIER;
            case 1 -> BASIC_TIER;
            case 2 -> PREMIUM_TIER;
            case 3 -> ENTERPRISE_TIER;
            default -> FREE_TIER;
        };
    }

    private RateLimitTier determineTierByUser(String userId) {
        // In production, this would query user database/cache
        // For demo, use user ID hash
        return switch (Math.abs(userId.hashCode()) % 4) {
            case 0 -> FREE_TIER;
            case 1 -> BASIC_TIER;
            case 2 -> PREMIUM_TIER;
            case 3 -> ENTERPRISE_TIER;
            default -> FREE_TIER;
        };
    }

    private RateLimitResult checkRateLimit(String key, RateLimitTier tier) {
        try {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - tier.window.toMillis();

            // Use Redis sorted set to track requests within the time window
            String requestsKey = key + ":requests";

            // Remove old requests outside the window
            redisTemplate.opsForZSet().removeRangeByScore(requestsKey, 0, windowStart);

            // Count current requests in window
            Long currentCount = redisTemplate.opsForZSet().size(requestsKey);
            if (currentCount == null) currentCount = 0L;

            if (currentCount >= tier.requests) {
                // Calculate retry-after time
                Double oldestTimestamp = redisTemplate.opsForZSet().rangeWithScores(requestsKey, 0, 0)
                    .stream().findFirst().map(tuple -> tuple.getScore()).orElse(0.0);

                long retryAfterSeconds = Math.max(1, (tier.window.toMillis() - (currentTime - oldestTimestamp.longValue())) / 1000);

                return new RateLimitResult(false, tier.requests - currentCount.intValue(), currentTime + tier.window.toMillis(), retryAfterSeconds);
            }

            // Add current request
            redisTemplate.opsForZSet().add(requestsKey, String.valueOf(currentTime), currentTime * 1.0);
            redisTemplate.expire(requestsKey, tier.window);

            return new RateLimitResult(true, tier.requests - currentCount.intValue() - 1, currentTime + tier.window.toMillis(), 0);

        } catch (Exception e) {
            // Fallback to allowing request if Redis fails
            logger.warn("Rate limiting check failed, allowing request", e);
            return new RateLimitResult(true, Integer.MAX_VALUE, System.currentTimeMillis() + 60000, 0);
        }
    }

    private String buildRateLimitKey(String clientIp, String userId, String endpoint) {
        // Create a hierarchical key structure for different granularities
        String userPart = (userId != null && !userId.equals("anonymous")) ? userId : "anon";
        return String.format("ratelimit:%s:%s:%s", userPart, clientIp, endpoint.replace("/", "_"));
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

    private String getUserId(HttpServletRequest request) {
        // Extract user ID from JWT token or session
        // This would depend on your authentication mechanism
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // In production, decode JWT to get user ID
            return "user_from_jwt"; // Placeholder
        }
        return "anonymous";
    }

    private String formatDuration(Duration duration) {
        if (duration.toMinutes() == 1L) {
            return "minute";
        } else if (duration.toHours() == 1L) {
            return "hour";
        } else {
            return duration.toString().toLowerCase();
        }
    }

    // Supporting data classes
    private static class RateLimitTier {
        final int requests;
        final Duration window;
        final String name;

        RateLimitTier(int requests, Duration window, String name) {
            this.requests = requests;
            this.window = window;
            this.name = name;
        }
    }

    private static class RateLimitResult {
        final boolean allowed;
        final int remainingRequests;
        final long resetTime;
        final long retryAfterSeconds;

        RateLimitResult(boolean allowed, int remainingRequests, long resetTime, long retryAfterSeconds) {
            this.allowed = allowed;
            this.remainingRequests = remainingRequests;
            this.resetTime = resetTime;
            this.retryAfterSeconds = retryAfterSeconds;
        }
    }
}
