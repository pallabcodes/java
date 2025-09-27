package com.netflix.productivity.comment.service;

import com.netflix.productivity.comment.entity.Comment;
import com.netflix.productivity.comment.repository.CommentRepository;
import com.netflix.productivity.audit.entity.AuditEvent;
import com.netflix.productivity.audit.service.AuditService;
import com.netflix.productivity.security.RequirePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CommentService {

    private final CommentRepository comments;
    private final AuditService audits;

    public CommentService(CommentRepository comments, AuditService audits) {
        this.comments = comments;
        this.audits = audits;
    }

    @RequirePermission("COMMENT_WRITE")
    public Comment add(Comment c) {
        Comment saved = comments.save(c);
        audits.record(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(saved.getTenantId())
                .projectId(saved.getProjectId())
                .issueId(saved.getIssueId())
                .entityType("COMMENT")
                .entityId(saved.getId())
                .action("CREATED")
                .actorUserId(saved.getAuthorUserId())
                .message("Comment added")
                .build());
        return saved;
    }

    @RequirePermission("COMMENT_READ")
    @Transactional(readOnly = true)
    public Page<Comment> list(String tenantId, String issueId, Pageable pageable) {
        return comments.findByTenantIdAndIssueIdOrderByCreatedAtAsc(tenantId, issueId, pageable);
    }

    @RequirePermission("COMMENT_WRITE")
    public void softDelete(String tenantId, String commentId, String actorUserId) {
        Comment c = comments.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!tenantId.equals(c.getTenantId())) {
            throw new IllegalArgumentException("Cross-tenant delete forbidden");
        }
        c.setDeleted(Boolean.TRUE);
        c.setEditedAt(java.time.OffsetDateTime.now());
        comments.save(c);
        audits.record(AuditEvent.builder()
                .id(java.util.UUID.randomUUID().toString())
                .tenantId(c.getTenantId())
                .projectId(c.getProjectId())
                .issueId(c.getIssueId())
                .entityType("COMMENT")
                .entityId(c.getId())
                .action("DELETED")
                .actorUserId(actorUserId)
                .message("Comment soft-deleted")
                .build());
    }
}


