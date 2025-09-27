package com.netflix.productivity.workflow.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.workflow.dto.WorkflowDto;
import com.netflix.productivity.workflow.entity.Workflow;
import com.netflix.productivity.workflow.entity.WorkflowTransition;
import com.netflix.productivity.workflow.dto.WorkflowStateDto;
import com.netflix.productivity.workflow.dto.WorkflowTransitionDto;
import com.netflix.productivity.workflow.mapper.WorkflowStateMapper;
import com.netflix.productivity.workflow.mapper.WorkflowTransitionMapper;
import com.netflix.productivity.workflow.mapper.WorkflowMapper;
import com.netflix.productivity.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@Tag(name = "Workflows", description = "Workflow definitions, states, and transitions")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowMapper workflowMapper;
    private final WorkflowStateMapper stateMapper;
    private final WorkflowTransitionMapper transitionMapper;
    private final ResponseMapper responses;

    public WorkflowController(WorkflowService workflowService, WorkflowMapper workflowMapper, WorkflowStateMapper stateMapper, WorkflowTransitionMapper transitionMapper, ResponseMapper responses) {
        this.workflowService = workflowService;
        this.workflowMapper = workflowMapper;
        this.stateMapper = stateMapper;
        this.transitionMapper = transitionMapper;
        this.responses = responses;
    }

    @GetMapping
    @Operation(summary = "List workflows", description = "List workflows for a project")
    public ResponseEntity<ApiResponse<Page<WorkflowDto>>> list(@RequestParam @Parameter(description = "Tenant id") String tenantId,
                                                              @RequestParam @Parameter(description = "Project id") String projectId,
                                                              Pageable pageable) {
        return responses.ok(workflowService.list(tenantId, projectId, pageable).map(workflowMapper::toDto));
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get workflow", description = "Get workflow by key")
    public ResponseEntity<ApiResponse<WorkflowDto>> get(@RequestParam @Parameter(description = "Tenant id") String tenantId,
                                                       @RequestParam @Parameter(description = "Project id") String projectId,
                                                       @PathVariable @Parameter(description = "Workflow key") String key) {
        return workflowService.get(tenantId, projectId, key)
                .map(w -> responses.ok(workflowMapper.toDto(w)))
                .orElseGet(() -> responses.notFound("Workflow not found"));
    }

    @PostMapping
    @Operation(summary = "Create workflow")
    public ResponseEntity<ApiResponse<WorkflowDto>> create(@RequestBody @Parameter(description = "Workflow body") WorkflowDto workflow) {
        Workflow saved = workflowService.create(workflowMapper.toEntity(workflow));
        return responses.created(workflowMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workflow")
    public ResponseEntity<ApiResponse<WorkflowDto>> update(@PathVariable @Parameter(description = "Workflow id") String id,
                                                          @RequestBody @Parameter(description = "Workflow body") WorkflowDto workflow) {
        Workflow entity = workflowMapper.toEntity(workflow);
        entity.setId(id);
        return responses.ok(workflowMapper.toDto(workflowService.update(entity)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workflow")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable @Parameter(description = "Workflow id") String id) {
        workflowService.delete(id);
        return responses.noContent();
    }

    @GetMapping("/{workflowId}/transitions")
    @Operation(summary = "List transitions", description = "List transitions for a workflow")
    public ResponseEntity<ApiResponse<List<WorkflowTransitionDto>>> listTransitions(@RequestParam @Parameter(description = "Tenant id") String tenantId,
                                                                                  @RequestParam @Parameter(description = "Project id") String projectId,
                                                                                  @PathVariable @Parameter(description = "Workflow id") String workflowId) {
        return responses.ok(workflowService.transitions(tenantId, projectId, workflowId).stream().map(transitionMapper::toDto).toList());
    }

    @GetMapping("/{workflowId}/transitions/from/{fromStateId}")
    @Operation(summary = "Transitions from state", description = "List transitions from a state")
    public ResponseEntity<ApiResponse<List<WorkflowTransitionDto>>> transitionsFrom(@RequestParam @Parameter(description = "Tenant id") String tenantId,
                                                                                  @RequestParam @Parameter(description = "Project id") String projectId,
                                                                                  @PathVariable @Parameter(description = "Workflow id") String workflowId,
                                                                                  @PathVariable @Parameter(description = "From state id") String fromStateId) {
        return responses.ok(workflowService.transitionsFrom(tenantId, projectId, workflowId, fromStateId).stream().map(transitionMapper::toDto).toList());
    }
}


