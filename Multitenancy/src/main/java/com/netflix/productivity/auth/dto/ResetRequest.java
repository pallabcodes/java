package com.netflix.productivity.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResetRequest {
    @NotBlank
    private String tenantId;
    @NotBlank @Email
    private String email;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}


