package com.netflix.productivity.workflow.service;

import com.netflix.productivity.security.RequirePermission;
import com.netflix.productivity.workflow.entity.Workflow;
import com.netflix.productivity.workflow.entity.WorkflowState;
import com.netflix.productivity.workflow.entity.WorkflowTransition;
import com.netflix.productivity.workflow.repository.WorkflowRepository;
import com.netflix.productivity.workflow.repository.WorkflowStateRepository;
import com.netflix.productivity.workflow.repository.WorkflowTransitionRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStateRepository stateRepository;
    private final WorkflowTransitionRepository transitionRepository;

    public WorkflowService(WorkflowRepository workflowRepository,
                           WorkflowStateRepository stateRepository,
                           WorkflowTransitionRepository transitionRepository) {
        this.workflowRepository = workflowRepository;
        this.stateRepository = stateRepository;
        this.transitionRepository = transitionRepository;
    }

    @RequirePermission("WORKFLOW_READ")
    @Transactional(readOnly = true)
    public Page<Workflow> list(String tenantId, String projectId, Pageable pageable) {
        return workflowRepository.findByTenantIdAndProjectId(tenantId, projectId, pageable);
    }

    @RequirePermission("WORKFLOW_READ")
    @Transactional(readOnly = true)
    public Optional<Workflow> get(String tenantId, String projectId, String key) {
        return workflowRepository.findByTenantIdAndProjectIdAndKey(tenantId, projectId, normalize(key));
    }

    @RequirePermission("WORKFLOW_WRITE")
    public Workflow create(Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    @RequirePermission("WORKFLOW_WRITE")
    public Workflow update(Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    @RequirePermission("WORKFLOW_WRITE")
    public void delete(String id) {
        workflowRepository.deleteById(id);
    }

    @RequirePermission("WORKFLOW_READ")
    @Transactional(readOnly = true)
    public List<WorkflowState> states(String tenantId, String projectId, String workflowId) {
        return stateRepository.findByTenantIdAndProjectIdAndWorkflowIdOrderByOrdinalAsc(tenantId, projectId, workflowId);
    }

    @RequirePermission("WORKFLOW_READ")
    @Transactional(readOnly = true)
    public List<WorkflowTransition> transitions(String tenantId, String projectId, String workflowId) {
        return transitionRepository.findByTenantIdAndProjectIdAndWorkflowIdOrderByOrdinalAsc(tenantId, projectId, workflowId);
    }

    @RequirePermission("WORKFLOW_READ")
    @Transactional(readOnly = true)
    public List<WorkflowTransition> transitionsFrom(String tenantId, String projectId, String workflowId, String fromStateId) {
        return transitionRepository.findByTenantIdAndProjectIdAndWorkflowIdAndFromStateIdOrderByOrdinalAsc(tenantId, projectId, workflowId, fromStateId);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    // Admin operations for states and transitions
    @RequirePermission("WORKFLOW_WRITE")
    public WorkflowState createState(WorkflowState state) {
        validateState(state);
        if (Boolean.TRUE.equals(state.getInitial())) {
            stateRepository.findByTenantIdAndProjectIdAndWorkflowIdAndInitialTrue(state.getTenantId(), state.getProjectId(), state.getWorkflowId())
                    .ifPresent(s -> { throw new IllegalArgumentException("Initial state already exists"); });
        }
        return stateRepository.save(state);
    }

    @RequirePermission("WORKFLOW_WRITE")
    public WorkflowState updateState(WorkflowState state) {
        validateState(state);
        return stateRepository.save(state);
    }

    @RequirePermission("WORKFLOW_WRITE")
    public void deleteState(String id) {
        stateRepository.deleteById(id);
    }

    @RequirePermission("WORKFLOW_WRITE")
    public WorkflowTransition createTransition(WorkflowTransition tr) {
        validateTransition(tr);
        return transitionRepository.save(tr);
    }

    @RequirePermission("WORKFLOW_WRITE")
    public WorkflowTransition updateTransition(WorkflowTransition tr) {
        validateTransition(tr);
        return transitionRepository.save(tr);
    }

    @RequirePermission("WORKFLOW_WRITE")
    public void deleteTransition(String id) {
        transitionRepository.deleteById(id);
    }

    private void validateState(WorkflowState state) {
        if (state == null) throw new IllegalArgumentException("State required");
        if (StringUtils.isBlank(state.getTenantId()) || StringUtils.isBlank(state.getProjectId()) || StringUtils.isBlank(state.getWorkflowId())) {
            throw new IllegalArgumentException("Tenant, project and workflow required");
        }
        if (StringUtils.isBlank(state.getKey()) || StringUtils.isBlank(state.getName())) {
            throw new IllegalArgumentException("Key and name required");
        }
        if (state.getOrdinal() == null) state.setOrdinal(0);
    }

    private void validateTransition(WorkflowTransition tr) {
        if (tr == null) throw new IllegalArgumentException("Transition required");
        if (StringUtils.isBlank(tr.getTenantId()) || StringUtils.isBlank(tr.getProjectId()) || StringUtils.isBlank(tr.getWorkflowId())) {
            throw new IllegalArgumentException("Tenant, project and workflow required");
        }
        if (StringUtils.isBlank(tr.getFromStateId()) || StringUtils.isBlank(tr.getToStateId())) {
            throw new IllegalArgumentException("From and to state required");
        }
        if (StringUtils.isBlank(tr.getKey()) || StringUtils.isBlank(tr.getName())) {
            throw new IllegalArgumentException("Key and name required");
        }
        if (tr.getOrdinal() == null) tr.setOrdinal(0);
    }
}


