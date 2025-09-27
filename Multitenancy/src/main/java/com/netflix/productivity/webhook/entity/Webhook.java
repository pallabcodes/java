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
@Table(name = "webhooks")
public class Webhook {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "secret")
    private String secret;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "event_filter")
    private String eventFilter;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

