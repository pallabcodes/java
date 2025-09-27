package com.netflix.productivity.workflow.repository;

import com.netflix.productivity.workflow.entity.WorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowStateRepository extends JpaRepository<WorkflowState, String> {
    List<WorkflowState> findByTenantIdAndProjectIdAndWorkflowIdOrderByOrdinalAsc(String tenantId, String projectId, String workflowId);
    Optional<WorkflowState> findByTenantIdAndProjectIdAndWorkflowIdAndKey(String tenantId, String projectId, String workflowId, String key);
    Optional<WorkflowState> findByTenantIdAndProjectIdAndWorkflowIdAndInitialTrue(String tenantId, String projectId, String workflowId);
}


