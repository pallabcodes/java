package com.netflix.productivity.platform;

import com.netflix.productivity.multitenancy.TenantContext;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(10)
public class RateLimitingFilter extends HttpFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String tenantId) {
        return buckets.computeIfAbsent(tenantId == null ? "default" : tenantId, key -> {
            // 200 req/min burst, refilled smoothly
            Bandwidth limit = Bandwidth.classic(200, Refill.greedy(200, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        });
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String tenantId = TenantContext.getCurrentTenant();
        Bucket bucket = resolveBucket(tenantId);
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setHeader("Retry-After", "10");
            response.getWriter().write("Rate limit exceeded");
        }
    }
}


