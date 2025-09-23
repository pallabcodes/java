package com.netflix.productivity.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_roles", columnNames = {"tenantId","userId","roleId"}))
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;
}


