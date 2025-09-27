package com.netflix.productivity.attachment.service;

import com.netflix.productivity.attachment.entity.Attachment;
import com.netflix.productivity.attachment.repository.AttachmentRepository;
import com.netflix.productivity.audit.entity.AuditEvent;
import com.netflix.productivity.audit.repository.AuditEventRepository;
import com.netflix.productivity.security.RequirePermission;
import com.netflix.productivity.attachment.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentService {
    private final AttachmentRepository attachments;
    private final AuditEventRepository audits;
    private final StorageService storage;

    @RequirePermission("ATTACHMENT_READ")
    @Transactional(readOnly = true)
    public Page<Attachment> list(String tenantId, String issueId, Pageable pageable) {
        return attachments.findByTenantIdAndIssueIdAndDeletedAtIsNullOrderByCreatedAtAsc(tenantId, issueId, pageable);
    }

    @RequirePermission("ATTACHMENT_WRITE")
    public Attachment create(String tenantId, String issueId, String filename, String contentType, long sizeBytes, String storageKey, String actorUserId) {
        Attachment a = Attachment.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .issueId(issueId)
                .filename(filename)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .storageKey(storageKey)
                .build();
        Attachment saved = attachments.save(a);

        audits.save(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(actorUserId)
                .eventType("ATTACHMENT_CREATED")
                .entityType("Attachment")
                .entityId(saved.getId())
                .build());

        return saved;
    }

    @RequirePermission("ATTACHMENT_WRITE")
    public void softDelete(String tenantId, String attachmentId, String actorUserId) {
        Attachment a = attachments.findById(attachmentId).orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
        if (!tenantId.equals(a.getTenantId())) {
            throw new IllegalArgumentException("Cross-tenant delete forbidden");
        }
        a.setDeletedAt(OffsetDateTime.now());
        attachments.save(a);

        audits.save(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(actorUserId)
                .eventType("ATTACHMENT_DELETED")
                .entityType("Attachment")
                .entityId(a.getId())
                .build());
    }

    @RequirePermission("ATTACHMENT_READ")
    @Transactional(readOnly = true)
    public java.io.InputStream openForDownload(String tenantId, String attachmentId) {
        Attachment a = attachments.findById(attachmentId).orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
        if (!tenantId.equals(a.getTenantId())) {
            throw new IllegalArgumentException("Cross-tenant read forbidden");
        }
        if (a.getDeletedAt() != null) {
            throw new IllegalArgumentException("Attachment deleted");
        }
        return storage.openStream(a.getStorageKey());
    }

    @RequirePermission("ATTACHMENT_READ")
    @Transactional(readOnly = true)
    public Attachment getMetadata(String tenantId, String attachmentId) {
        Attachment a = attachments.findById(attachmentId).orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
        if (!tenantId.equals(a.getTenantId())) {
            throw new IllegalArgumentException("Cross-tenant read forbidden");
        }
        if (a.getDeletedAt() != null) {
            throw new IllegalArgumentException("Attachment deleted");
        }
        return a;
    }
}

