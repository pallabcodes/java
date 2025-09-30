package com.netflix.productivity.audit.service;

import com.netflix.productivity.audit.entity.AuditEvent;
import com.netflix.productivity.audit.repository.AuditEventRepository;
import com.netflix.productivity.security.RequirePermission;
import com.netflix.productivity.outbox.service.OutboxService;
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
    private final OutboxService outbox;

    public AuditService(AuditEventRepository audits, WebhookService webhooks, OutboxService outbox) { this.audits = audits; this.webhooks = webhooks; this.outbox = outbox; }

    public AuditEvent record(AuditEvent e) {
        AuditEvent saved = audits.save(e);
        // enqueue audit outbox (non-blocking publish)
        outbox.enqueueAudit(saved.getTenantId(), "AUDIT_EVENT", saved.getId(), saved.getEventType().name(), java.util.Map.of(
                "eventId", saved.getId(),
                "tenantId", saved.getTenantId(),
                "type", saved.getEventType().name(),
                "createdAt", saved.getCreatedAt().toString()
        ));
        // still enqueue webhook deliveries for internal retry pipeline
        webhooks.enqueueDeliveries(saved);
        return saved;
    }

    @RequirePermission("AUDIT_READ")
    @Transactional(readOnly = true)
    public Page<AuditEvent> listByIssue(String tenantId, String issueId, Pageable pageable) {
        return audits.findByTenantIdAndIssueIdOrderByCreatedAtDesc(tenantId, issueId, pageable);
    }
}


