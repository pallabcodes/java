package com.netflix.productivity.workflow.repository;

import com.netflix.productivity.workflow.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, String> {
    List<WorkflowTransition> findByTenantIdAndProjectIdAndWorkflowIdOrderByOrdinalAsc(String tenantId, String projectId, String workflowId);
    List<WorkflowTransition> findByTenantIdAndProjectIdAndWorkflowIdAndFromStateIdOrderByOrdinalAsc(String tenantId, String projectId, String workflowId, String fromStateId);
}


