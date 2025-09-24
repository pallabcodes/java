package com.netflix.productivity.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ErrorCodes;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.netflix.productivity.config.DynamicConfigService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(10)
public class RateLimitingFilter extends HttpFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private DynamicConfigService dynamicConfigService;

    private Bucket resolveBucket(String tenantId) {
        String key = tenantId == null ? "default" : tenantId;
        return buckets.compute(key, (k, existing) -> {
            int perMinute = dynamicConfigService == null ? 200 : dynamicConfigService.getTenantRateLimitPerMinute(key);
            Bandwidth limit = Bandwidth.classic(perMinute, Refill.greedy(perMinute, Duration.ofMinutes(1)));
            if (existing == null) {
                return Bucket.builder().addLimit(limit).build();
            }
            // Rebuild bucket if config changed
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
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "10");
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            ApiResponse<Void> body = ApiResponse.error(HttpStatus.TOO_MANY_REQUESTS.value(), "Rate limit exceeded", null, ErrorCodes.RATE_LIMITED);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }
}


