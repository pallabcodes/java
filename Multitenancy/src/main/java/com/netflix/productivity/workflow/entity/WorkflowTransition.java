package com.netflix.productivity.workflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.ParamDef;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workflow_transitions")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTransition {

    @Id
    @GeneratedValue(generator = "wft-uuid")
    @GenericGenerator(name = "wft-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    @Column(name = "workflow_id", nullable = false, length = 36)
    private String workflowId;

    @Column(name = "from_state_id", nullable = false, length = 36)
    private String fromStateId;

    @Column(name = "to_state_id", nullable = false, length = 36)
    private String toStateId;

    @Column(name = "key", nullable = false, length = 64)
    private String key;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "required_permission", length = 64)
    private String requiredPermission;

    @Column(name = "guard_expression", length = 2000)
    private String guardExpression;

    @Column(name = "ordinal", nullable = false)
    private Integer ordinal;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}


