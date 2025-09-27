package com.netflix.productivity.watcher.service;

import com.netflix.productivity.audit.entity.AuditEvent;
import com.netflix.productivity.audit.repository.AuditEventRepository;
import com.netflix.productivity.security.RequirePermission;
import com.netflix.productivity.watcher.entity.IssueWatcher;
import com.netflix.productivity.watcher.repository.IssueWatcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WatcherService {
    private final IssueWatcherRepository watchers;
    private final AuditEventRepository audits;

    @RequirePermission("ISSUE_READ")
    @Transactional(readOnly = true)
    public Page<IssueWatcher> list(String tenantId, String issueId, Pageable pageable) {
        return watchers.findByTenantIdAndIssueId(tenantId, issueId, pageable);
    }

    @RequirePermission("ISSUE_WATCH")
    public void add(String tenantId, String issueId, String userId, String actorUserId) {
        if (watchers.existsByTenantIdAndIssueIdAndUserId(tenantId, issueId, userId)) return;
        IssueWatcher w = IssueWatcher.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .issueId(issueId)
                .userId(userId)
                .build();
        watchers.save(w);
        audits.save(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(actorUserId)
                .eventType("ISSUE_WATCHER_ADDED")
                .entityType("Issue")
                .entityId(issueId)
                .build());
    }

    @RequirePermission("ISSUE_WATCH")
    public void remove(String tenantId, String issueId, String userId, String actorUserId) {
        watchers.deleteByTenantIdAndIssueIdAndUserId(tenantId, issueId, userId);
        audits.save(AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(actorUserId)
                .eventType("ISSUE_WATCHER_REMOVED")
                .entityType("Issue")
                .entityId(issueId)
                .build());
    }
}

