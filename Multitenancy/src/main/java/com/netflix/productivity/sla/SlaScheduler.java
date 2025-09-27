package com.netflix.productivity.sla;

import com.netflix.productivity.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SlaScheduler {
    private final IssueRepository issues;
    private final SlaService sla;

    @Scheduled(fixedDelayString = "${sla.scan.fixedDelay.ms:60000}")
    public void scanAndMarkBreaches() {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        try {
            // TODO iterate tenants properly; for now, assume single-tenant scan via distinct tenant ids from overdue issues
            List<String> tenantIds = issues.findAll().stream().map(i -> i.getTenantId()).distinct().toList();
            for (String tenantId : tenantIds) {
                issues.findOverdueUnmarked(tenantId, LocalDateTime.now()).forEach(issue -> sla.markBreached(tenantId, issue));
            }
        } finally {
            MDC.remove("correlationId");
        }
    }
}

