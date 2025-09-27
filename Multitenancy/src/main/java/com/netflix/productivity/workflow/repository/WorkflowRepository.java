package com.netflix.productivity.workflow.repository;

import com.netflix.productivity.workflow.entity.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowRepository extends JpaRepository<Workflow, String> {
    Optional<Workflow> findByTenantIdAndProjectIdAndKey(String tenantId, String projectId, String key);
    Page<Workflow> findByTenantIdAndProjectId(String tenantId, String projectId, Pageable pageable);
}


