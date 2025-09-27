package com.netflix.productivity.webhook.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "webhook_deliveries")
public class WebhookDelivery {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "webhook_id", length = 36, nullable = false)
    private String webhookId;

    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "attempt", nullable = false)
    private int attempt;

    @Column(name = "next_attempt_at")
    private OffsetDateTime nextAttemptAt;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body")
    private String responseBody;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

