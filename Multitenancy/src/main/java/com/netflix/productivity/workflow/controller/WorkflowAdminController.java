package com.netflix.productivity.workflow.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.security.RequirePermission;
import com.netflix.productivity.workflow.dto.WorkflowStateDto;
import com.netflix.productivity.workflow.dto.WorkflowTransitionDto;
import com.netflix.productivity.workflow.entity.WorkflowState;
import com.netflix.productivity.workflow.entity.WorkflowTransition;
import com.netflix.productivity.workflow.mapper.WorkflowStateMapper;
import com.netflix.productivity.workflow.mapper.WorkflowTransitionMapper;
import com.netflix.productivity.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflows/admin")
@Tag(name = "Workflow Admin")
public class WorkflowAdminController {

    private final WorkflowService workflowService;
    private final WorkflowStateMapper stateMapper;
    private final WorkflowTransitionMapper transitionMapper;
    private final ResponseMapper responses;

    public WorkflowAdminController(WorkflowService workflowService, WorkflowStateMapper stateMapper, WorkflowTransitionMapper transitionMapper, ResponseMapper responses) {
        this.workflowService = workflowService;
        this.stateMapper = stateMapper;
        this.transitionMapper = transitionMapper;
        this.responses = responses;
    }

    @PostMapping("/states")
    @RequirePermission("WORKFLOW_WRITE")
    public ResponseEntity<ApiResponse<WorkflowStateDto>> createState(@RequestBody WorkflowStateDto dto) {
        WorkflowState saved = workflowService.createState(toEntity(dto));
        return responses.created(stateMapper.toDto(saved));
    }

    @PutMapping("/states/{id}")
    @RequirePermission("WORKFLOW_WRITE")
    public ResponseEntity<ApiResponse<WorkflowStateDto>> updateState(@PathVariable String id, @RequestBody WorkflowStateDto dto) {
        WorkflowState entity = toEntity(dto);
        entity.setId(id);
        return responses.ok(stateMapper.toDto(workflowService.updateState(entity)));
    }

    @DeleteMapping("/states/{id}")
    @RequirePermission("WORKFLOW_WRITE")
    public ResponseEntity<ApiResponse<Void>> deleteState(@PathVariable String id) {
        workflowService.deleteState(id);
        return responses.noContent();
    }

    @PostMapping("/transitions")
    @RequirePermission("WORKFLOW_WRITE")
    public ResponseEntity<ApiResponse<WorkflowTransitionDto>> createTransition(@RequestBody WorkflowTransitionDto dto) {
        WorkflowTransition saved = workflowService.createTransition(toEntity(dto));
        return responses.created(transitionMapper.toDto(saved));
    }

    @PutMapping("/transitions/{id}")
    @RequirePermission("WORKFLOW_WRITE")
    public ResponseEntity<ApiResponse<WorkflowTransitionDto>> updateTransition(@PathVariable String id, @RequestBody WorkflowTransitionDto dto) {
        WorkflowTransition entity = toEntity(dto);
        entity.setId(id);
        return responses.ok(transitionMapper.toDto(workflowService.updateTransition(entity)));
    }

    @DeleteMapping("/transitions/{id}")
    @RequirePermission("WORKFLOW_WRITE")
    public ResponseEntity<ApiResponse<Void>> deleteTransition(@PathVariable String id) {
        workflowService.deleteTransition(id);
        return responses.noContent();
    }

    private WorkflowState toEntity(WorkflowStateDto dto) {
        WorkflowState e = new WorkflowState();
        e.setId(dto.getId());
        e.setKey(dto.getKey());
        e.setName(dto.getName());
        e.setInitial(dto.getInitial());
        e.setTerminal(dto.getTerminal());
        e.setOrdinal(dto.getOrdinal());
        e.setStatus(dto.getStatus());
        return e;
    }

    private WorkflowTransition toEntity(WorkflowTransitionDto dto) {
        WorkflowTransition e = new WorkflowTransition();
        e.setId(dto.getId());
        e.setFromStateId(dto.getFromStateId());
        e.setToStateId(dto.getToStateId());
        e.setKey(dto.getKey());
        e.setName(dto.getName());
        e.setRequiredPermission(dto.getRequiredPermission());
        e.setOrdinal(dto.getOrdinal());
        return e;
    }
}


