package com.netflix.productivity.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_events")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "project_id", length = 36)
    private String projectId;

    @Column(name = "issue_id", length = 36)
    private String issueId;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 64)
    private String entityId;

    @Column(name = "action", nullable = false, length = 64)
    private String action;

    @Column(name = "actor_user_id", nullable = false, length = 36)
    private String actorUserId;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = OffsetDateTime.now(); }
}


