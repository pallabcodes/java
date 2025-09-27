package com.netflix.productivity.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "comments")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(generator = "cmt-uuid")
    @GenericGenerator(name = "cmt-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    @Column(name = "issue_id", nullable = false, length = 36)
    private String issueId;

    @Column(name = "author_user_id", nullable = false, length = 36)
    private String authorUserId;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "edited_at")
    private OffsetDateTime editedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (deleted == null) deleted = Boolean.FALSE;
        if (version == null) version = 0L;
    }

    @PreUpdate
    void onUpdate() { updatedAt = OffsetDateTime.now(); }
}


