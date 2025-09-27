package com.netflix.productivity.workflow.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.workflow.entity.WorkflowTransition;
import com.netflix.productivity.workflow.service.WorkflowEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workflow/ux")
@Tag(name = "Workflows")
public class WorkflowUXController {
    private final WorkflowEngineService engine;
    private final ResponseMapper responses;

    @GetMapping("/issues/{key}/transitions")
    @Operation(summary = "Discover allowed transitions for an issue")
    public ResponseEntity<ApiResponse<List<WorkflowTransition>>> discover(@RequestHeader("X-Tenant-ID") String tenantId,
                                                                         @RequestParam String projectId,
                                                                         @RequestParam String workflowId,
                                                                         @PathVariable String key) {
        return responses.ok(engine.discoverTransitions(tenantId, projectId, workflowId, key));
    }

    public record BulkTransitionRequest(String tenantId, String projectId, String workflowId, List<String> keys, String toStateId) {}

    @PostMapping("/issues/bulk-transition")
    @Operation(summary = "Apply a transition to multiple issues with guards")
    public ResponseEntity<ApiResponse<Map<String, String>>> bulk(@RequestBody BulkTransitionRequest req) {
        return responses.ok(engine.bulkTransition(req.tenantId(), req.projectId(), req.workflowId(), req.keys(), req.toStateId()));
    }
}

