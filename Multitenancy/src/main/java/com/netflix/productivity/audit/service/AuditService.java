package com.netflix.productivity.audit.service;

import com.netflix.productivity.audit.entity.AuditEvent;
import com.netflix.productivity.audit.repository.AuditEventRepository;
import com.netflix.productivity.security.RequirePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.netflix.productivity.webhook.service.WebhookService;

@Service
@Transactional
public class AuditService {

    private final AuditEventRepository audits;
    private final WebhookService webhooks;

    public AuditService(AuditEventRepository audits, WebhookService webhooks) { this.audits = audits; this.webhooks = webhooks; }

    public AuditEvent record(AuditEvent e) {
        AuditEvent saved = audits.save(e);
        webhooks.enqueueDeliveries(saved);
        return saved;
    }

    @RequirePermission("AUDIT_READ")
    @Transactional(readOnly = true)
    public Page<AuditEvent> listByIssue(String tenantId, String issueId, Pageable pageable) {
        return audits.findByTenantIdAndIssueIdOrderByCreatedAtDesc(tenantId, issueId, pageable);
    }
}


