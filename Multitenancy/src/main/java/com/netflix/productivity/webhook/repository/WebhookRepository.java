package com.netflix.productivity.webhook.repository;

import com.netflix.productivity.webhook.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookRepository extends JpaRepository<Webhook, String> {
    List<Webhook> findByTenantIdAndEnabledTrue(String tenantId);
}

