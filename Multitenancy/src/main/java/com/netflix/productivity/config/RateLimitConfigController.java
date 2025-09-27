package com.netflix.productivity.config;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.security.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/runtime/ratelimit")
@Tag(name = "Runtime Config")
public class RateLimitConfigController {
    private final DynamicConfigService config;
    private final ResponseMapper responses;

    @GetMapping
    @Operation(summary = "Get per-tenant rate limit per minute")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(@RequestHeader("X-Tenant-ID") String tenantId) {
        int v = config.getTenantRateLimitPerMinute(tenantId);
        return responses.ok(Map.of("tenantId", tenantId, "perMinute", v));
    }

    @PostMapping
    @RequirePermission("TENANT_ADMIN")
    @Operation(summary = "Set per-tenant rate limit per minute")
    public ResponseEntity<ApiResponse<Void>> set(@RequestHeader("X-Tenant-ID") String tenantId,
                                                 @RequestParam int perMinute,
                                                 @RequestParam(defaultValue = "PT1H") String ttl) {
        config.setTenantRateLimitPerMinute(tenantId, perMinute, Duration.parse(ttl));
        return responses.noContent();
    }
}

