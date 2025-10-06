package com.netflix.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreUserData {
    private String id;
    private String tenantId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastLoginAt;
    private boolean isActive;
}
