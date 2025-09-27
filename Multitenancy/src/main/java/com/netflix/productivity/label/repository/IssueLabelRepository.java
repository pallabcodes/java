package com.netflix.productivity.label.repository;

import com.netflix.productivity.label.entity.IssueLabel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueLabelRepository extends JpaRepository<IssueLabel, String> {
    Page<IssueLabel> findByTenantIdAndIssueId(String tenantId, String issueId, Pageable pageable);
    boolean existsByTenantIdAndIssueIdAndLabelId(String tenantId, String issueId, String labelId);
}

