package com.netflix.productivity.label.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.label.entity.Label;
import com.netflix.productivity.label.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labels")
@Tag(name = "Labels")
public class LabelController {
    private final LabelService labels;
    private final ResponseMapper responses;

    @GetMapping
    @Operation(summary = "List labels")
    public ResponseEntity<ApiResponse<Page<Label>>> list(@RequestHeader("X-Tenant-ID") String tenantId,
                                                         Pageable pageable) {
        return responses.ok(labels.list(tenantId, pageable));
    }

    @PostMapping
    @Operation(summary = "Create label")
    public ResponseEntity<ApiResponse<Label>> create(@RequestHeader("X-Tenant-ID") String tenantId,
                                                     @RequestParam String name,
                                                     @RequestParam(required = false) String color,
                                                     @RequestParam(required = false) String description,
                                                     @RequestHeader(value = "X-User-ID", required = false) String userId) {
        return responses.created(labels.create(tenantId, name, color, description, userId == null ? "unknown" : userId));
    }

    @DeleteMapping("/{labelId}")
    @Operation(summary = "Delete label")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Tenant-ID") String tenantId,
                                                    @PathVariable String labelId,
                                                    @RequestHeader(value = "X-User-ID", required = false) String userId) {
        labels.delete(tenantId, labelId, userId == null ? "unknown" : userId);
        return responses.noContent();
    }
}

