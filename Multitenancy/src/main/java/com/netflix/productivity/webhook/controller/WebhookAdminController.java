package com.netflix.productivity.webhook.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.security.RequirePermission;
import com.netflix.productivity.webhook.entity.WebhookDelivery;
import com.netflix.productivity.webhook.repository.WebhookDeliveryRepository;
import com.netflix.productivity.webhook.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks/admin")
@Tag(name = "Webhooks")
public class WebhookAdminController {
    private final WebhookDeliveryRepository deliveries;
    private final WebhookService service;
    private final ResponseMapper responses;

    @GetMapping("/deliveries/due")
    @RequirePermission("TENANT_ADMIN")
    @Operation(summary = "List due deliveries")
    public ResponseEntity<ApiResponse<List<WebhookDelivery>>> due(@RequestHeader("X-Tenant-ID") String tenantId) {
        return responses.ok(deliveries.findDue(tenantId, OffsetDateTime.now()));
    }

    @PostMapping("/deliveries/{id}/retry")
    @RequirePermission("TENANT_ADMIN")
    @Operation(summary = "Force retry for a delivery now")
    public ResponseEntity<ApiResponse<Void>> retry(@PathVariable String id) {
        deliveries.findById(id).ifPresent(d -> { d.setNextAttemptAt(OffsetDateTime.now()); d.setStatus("PENDING"); deliveries.save(d); });
        return responses.noContent();
    }

    @PostMapping("/deliveries/{id}/dlq")
    @RequirePermission("TENANT_ADMIN")
    @Operation(summary = "Move a delivery to DLQ (mark failed)")
    public ResponseEntity<ApiResponse<Void>> dlq(@PathVariable String id) {
        deliveries.findById(id).ifPresent(d -> { d.setStatus("FAILED"); deliveries.save(d); });
        return responses.noContent();
    }
}

