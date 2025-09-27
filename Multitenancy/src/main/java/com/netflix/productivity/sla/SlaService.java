package com.netflix.productivity.sla;

import com.netflix.productivity.audit.entity.AuditEvent;
import com.netflix.productivity.audit.repository.AuditEventRepository;
import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.security.RequirePermission;
import com.netflix.productivity.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SlaService {
    private final IssueService issueService;
    private final AuditEventRepository audits;

    @RequirePermission("ISSUE_WRITE")
    public Issue setDueDate(String tenantId, String key, LocalDateTime due, String actorUserId) {
        var dto = issueService.getByKey(tenantId, key);
        dto.setDueDate(due);
        var updated = issueService.update(dto);
        audits.save(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(actorUserId)
                .eventType("ISSUE_DUE_SET")
                .entityType("Issue")
                .entityId(updated.getId())
                .build());
        return issueService.findEntityById(updated.getId());
    }

    @RequirePermission("TENANT_ADMIN")
    public void markBreached(String tenantId, Issue issue) {
        if (issue.getSlaBreachedAt() != null) return;
        issue.setSlaBreachedAt(LocalDateTime.now());
        issueService.saveEntity(issue);
        audits.save(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(null)
                .eventType("ISSUE_SLA_BREACHED")
                .entityType("Issue")
                .entityId(issue.getId())
                .build());
    }
}

