package com.netflix.productivity.webhook.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.webhook.entity.Webhook;
import com.netflix.productivity.webhook.repository.WebhookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks")
public class WebhookController {
    private final WebhookRepository webhooks;
    private final ResponseMapper responses;

    @GetMapping
    @Operation(summary = "List webhooks")
    public ResponseEntity<ApiResponse<List<Webhook>>> list(@RequestHeader("X-Tenant-ID") String tenantId) {
        return responses.ok(webhooks.findByTenantIdAndEnabledTrue(tenantId));
    }

    @PostMapping
    @Operation(summary = "Create webhook")
    public ResponseEntity<ApiResponse<Webhook>> create(@RequestHeader("X-Tenant-ID") String tenantId,
                                                       @RequestParam String name,
                                                       @RequestParam String url,
                                                       @RequestParam(required = false) String secret,
                                                       @RequestParam(required = false) String eventFilter) {
        Webhook wh = Webhook.builder().id(UUID.randomUUID().toString()).tenantId(tenantId).name(name).url(url).secret(secret).eventFilter(eventFilter).enabled(true).build();
        return responses.created(webhooks.save(wh));
    }
}

