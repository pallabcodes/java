package com.netflix.productivity.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_tenant_id", columnList = "tenantId"),
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_username", columnList = "username")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_tenant_email", columnNames = {"tenantId","email"}),
                @UniqueConstraint(name = "uk_users_tenant_username", columnNames = {"tenantId","username"})
        })
public class User {

    @Id
    @GeneratedValue(generator = "user-uuid")
    @GenericGenerator(name = "user-uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @NotBlank
    @Size(max = 60)
    @Column(name = "username", nullable = false, length = 60)
    private String username;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "locked", nullable = false)
    private boolean locked;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}


