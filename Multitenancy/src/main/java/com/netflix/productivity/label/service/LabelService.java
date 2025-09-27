package com.netflix.productivity.label.service;

import com.netflix.productivity.audit.entity.AuditEvent;
import com.netflix.productivity.audit.repository.AuditEventRepository;
import com.netflix.productivity.label.entity.IssueLabel;
import com.netflix.productivity.label.entity.Label;
import com.netflix.productivity.label.repository.IssueLabelRepository;
import com.netflix.productivity.label.repository.LabelRepository;
import com.netflix.productivity.security.RequirePermission;
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
public class LabelService {
    private final LabelRepository labels;
    private final IssueLabelRepository issueLabels;
    private final AuditEventRepository audits;

    @RequirePermission("LABEL_WRITE")
    public Label create(String tenantId, String name, String color, String description, String actorUserId) {
        labels.findByTenantIdAndName(tenantId, name).ifPresent(x -> { throw new IllegalArgumentException("Label exists"); });
        Label l = Label.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .name(name)
                .color(color)
                .description(description)
                .build();
        Label saved = labels.save(l);
        audits.save(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(actorUserId)
                .eventType("LABEL_CREATED")
                .entityType("Label")
                .entityId(saved.getId())
                .build());
        return saved;
    }

    @RequirePermission("LABEL_READ")
    @Transactional(readOnly = true)
    public Page<Label> list(String tenantId, Pageable pageable) {
        return labels.findByTenantIdAndDeletedAtIsNullOrderByNameAsc(tenantId, pageable);
    }

    @RequirePermission("LABEL_WRITE")
    public void delete(String tenantId, String labelId, String actorUserId) {
        Label l = labels.findById(labelId).orElseThrow(() -> new IllegalArgumentException("Label not found"));
        if (!tenantId.equals(l.getTenantId())) throw new IllegalArgumentException("Cross-tenant delete forbidden");
        l.setDeletedAt(OffsetDateTime.now());
        labels.save(l);
        audits.save(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(actorUserId)
                .eventType("LABEL_DELETED")
                .entityType("Label")
                .entityId(l.getId())
                .build());
    }

    @RequirePermission("ISSUE_WRITE")
    public void assignToIssue(String tenantId, String issueId, String labelId, String actorUserId) {
        if (issueLabels.existsByTenantIdAndIssueIdAndLabelId(tenantId, issueId, labelId)) return;
        IssueLabel il = IssueLabel.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .issueId(issueId)
                .labelId(labelId)
                .build();
        issueLabels.save(il);
        audits.save(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(actorUserId)
                .eventType("LABEL_ASSIGNED")
                .entityType("Issue")
                .entityId(issueId)
                .build());
    }

    @RequirePermission("ISSUE_WRITE")
    public void removeFromIssue(String tenantId, String issueId, String labelId, String actorUserId) {
        issueLabels.findAll().stream()
                .filter(x -> x.getTenantId().equals(tenantId) && x.getIssueId().equals(issueId) && x.getLabelId().equals(labelId))
                .findFirst()
                .ifPresent(x -> issueLabels.deleteById(x.getId()));
        audits.save(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(actorUserId)
                .eventType("LABEL_UNASSIGNED")
                .entityType("Issue")
                .entityId(issueId)
                .build());
    }
}

