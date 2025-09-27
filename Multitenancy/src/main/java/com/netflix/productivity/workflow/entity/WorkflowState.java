package com.netflix.productivity.workflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.ParamDef;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workflow_states")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowState {

    @Id
    @GeneratedValue(generator = "wfs-uuid")
    @GenericGenerator(name = "wfs-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    @Column(name = "workflow_id", nullable = false, length = 36)
    private String workflowId;

    @Column(name = "key", nullable = false, length = 64)
    private String key;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "is_initial", nullable = false)
    private Boolean initial;

    @Column(name = "is_terminal", nullable = false)
    private Boolean terminal;

    @Column(name = "ordinal", nullable = false)
    private Integer ordinal;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (initial == null) initial = Boolean.FALSE;
        if (terminal == null) terminal = Boolean.FALSE;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}


