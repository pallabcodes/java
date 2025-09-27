package com.netflix.productivity.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.dto.IssueDto;
import com.netflix.productivity.dto.IssueTransitionRequest;
import com.netflix.productivity.service.IssueService;
import com.netflix.productivity.workflow.service.WorkflowEngineService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.netflix.productivity.repository.projection.IssueListProjection;

@RestController
@RequestMapping("/api/issues")
@Tag(name = "Issues")
public class IssueController {

    private final IssueService issueService;
    private final WorkflowEngineService workflowEngineService;
    private final ResponseMapper responseMapper;

    public IssueController(IssueService issueService, WorkflowEngineService workflowEngineService, ResponseMapper responseMapper) {
        this.issueService = issueService;
        this.workflowEngineService = workflowEngineService;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IssueDto>>> list(@RequestHeader("X-Tenant-ID") String tenantId, Pageable pageable) {
        return responseMapper.ok(issueService.list(tenantId, pageable));
    }

    @GetMapping("/lite")
    public ResponseEntity<ApiResponse<Page<IssueListProjection>>> listLite(@RequestHeader("X-Tenant-ID") String tenantId, Pageable pageable) {
        return responseMapper.ok(issueService.listLite(tenantId, pageable));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<Page<IssueDto>>> listByProject(@RequestHeader("X-Tenant-ID") String tenantId,
                                                                     @PathVariable String projectId,
                                                                     Pageable pageable) {
        final String normalizedProjectId = normalize(projectId);
        return responseMapper.ok(issueService.listByProject(tenantId, normalizedProjectId, pageable));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IssueDto>> create(@Validated @RequestBody IssueDto dto) {
        return responseMapper.created(issueService.create(dto));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<IssueDto>> getByKey(@RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String key) {
        final String normalizedKey = normalize(key);
        return responseMapper.ok(issueService.getByKey(tenantId, normalizedKey));
    }

    @PostMapping("/{key}/transition")
    @Operation(summary = "Transition issue", description = "Apply a workflow transition to an issue")
    public ResponseEntity<ApiResponse<IssueDto>> transition(@PathVariable String key,
                                                            @Validated @RequestBody IssueTransitionRequest req) {
        workflowEngineService.transition(req.getTenantId(), req.getProjectId(), req.getWorkflowId(), normalize(key), req.getFromStateId(), req.getToStateId());
        return responseMapper.ok(issueService.getByKey(req.getTenantId(), normalize(key)));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}


