package com.netflix.productivity.attachment.repository;

import com.netflix.productivity.attachment.entity.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    Page<Attachment> findByTenantIdAndIssueIdAndDeletedAtIsNullOrderByCreatedAtAsc(String tenantId, String issueId, Pageable pageable);
}

