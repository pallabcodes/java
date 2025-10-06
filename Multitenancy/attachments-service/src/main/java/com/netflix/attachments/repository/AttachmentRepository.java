package com.netflix.attachments.repository;

import com.netflix.attachments.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    List<Attachment> findByTenantIdAndIssueId(String tenantId, String issueId);
}
