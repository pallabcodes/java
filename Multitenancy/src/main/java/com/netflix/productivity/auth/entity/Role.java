package com.netflix.productivity.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_roles_tenant_name", columnNames = {"tenantId","name"}))
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 50)
    private String name; // TENANT_ADMIN, PROJECT_ADMIN, PROJECT_MEMBER, VIEWER
}


