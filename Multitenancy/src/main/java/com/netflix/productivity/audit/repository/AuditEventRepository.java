package com.netflix.productivity.audit.repository;

import com.netflix.productivity.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {
    Page<AuditEvent> findByTenantIdAndIssueIdOrderByCreatedAtDesc(String tenantId, String issueId, Pageable pageable);
}


