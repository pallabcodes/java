package com.netflix.productivity.webhook.service;

import com.netflix.productivity.audit.entity.AuditEvent;
import com.netflix.productivity.webhook.entity.Webhook;
import com.netflix.productivity.webhook.entity.WebhookDelivery;
import com.netflix.productivity.webhook.repository.WebhookDeliveryRepository;
import com.netflix.productivity.webhook.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import com.netflix.productivity.outbox.service.OutboxService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WebhookService {
    private final WebhookRepository webhooks;
    private final WebhookDeliveryRepository deliveries;
    private final OutboxService outbox;

    public void enqueueDeliveries(AuditEvent event) {
        List<Webhook> targets = webhooks.findByTenantIdAndEnabledTrue(event.getTenantId());
        for (Webhook wh : targets) {
            if (wh.getEventFilter() != null && !wh.getEventFilter().isBlank()) {
                if (!event.getEventType().name().contains(wh.getEventFilter())) continue;
            }
            deliveries.save(WebhookDelivery.builder()
                    .id(UUID.randomUUID().toString())
                    .tenantId(event.getTenantId())
                    .webhookId(wh.getId())
                    .eventId(event.getId())
                    .status("PENDING")
                    .attempt(0)
                    .nextAttemptAt(OffsetDateTime.now())
                    .build());

            // Also enqueue to Kafka outbox for external webhook worker
            outbox.enqueueWebhook(event.getTenantId(), "WEBHOOK", String.valueOf(wh.getId()), "AUDIT_EVENT",
                    Map.of(
                            "eventId", event.getId(),
                            "webhookId", wh.getId(),
                            "url", wh.getUrl(),
                            "secret", wh.getSecret() == null ? "" : wh.getSecret(),
                            "createdAt", OffsetDateTime.now().toString()
                    )
            );
        }
    }

    @Scheduled(fixedDelayString = "${webhooks.dispatch.fixedDelay.ms:5000}")
    public void dispatch() {
        // naive tenant scan by distinct in deliveries
        deliveries.findAll().stream().map(WebhookDelivery::getTenantId).distinct().forEach(tenant -> {
            List<WebhookDelivery> due = deliveries.findDue(tenant, OffsetDateTime.now());
            for (WebhookDelivery d : due) {
                try {
                    Webhook wh = webhooks.findById(d.getWebhookId()).orElse(null);
                    if (wh == null || !wh.isEnabled()) { markFailed(d, 410, "Webhook disabled"); continue; }
                    String payload = "{\"eventId\":\"" + d.getEventId() + "\"}";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    if (wh.getSecret() != null && !wh.getSecret().isBlank()) {
                        headers.add("X-Signature", hmacSha256(payload, wh.getSecret()));
                    }
                    RestTemplate rt = new RestTemplate(new SimpleClientHttpRequestFactory());
                    var resp = rt.postForEntity(wh.getUrl(), new org.springframework.http.HttpEntity<>(payload, headers), String.class);
                    d.setStatus(resp.getStatusCode().is2xxSuccessful() ? "DELIVERED" : "FAILED");
                    d.setResponseStatus(resp.getStatusCodeValue());
                    d.setResponseBody(resp.getBody());
                } catch (Exception e) {
                    backoff(d);
                } finally {
                    deliveries.save(d);
                }
            }
        });
    }

    private static String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return java.util.Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void backoff(WebhookDelivery d) {
        int attempt = d.getAttempt() + 1;
        d.setAttempt(attempt);
        d.setStatus("PENDING");
        long delaySec = Math.min(300, (long) Math.pow(2, Math.min(8, attempt)));
        d.setNextAttemptAt(OffsetDateTime.now().plusSeconds(delaySec));
    }

    private void markFailed(WebhookDelivery d, int status, String body) {
        d.setStatus("FAILED");
        d.setResponseStatus(status);
        d.setResponseBody(body);
    }
}

