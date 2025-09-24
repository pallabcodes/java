package com.netflix.productivity.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class ResetConfirm {
    @NotBlank
    private String tenantId;
    @NotBlank
    private String token;
    @NotBlank
    private String newPassword;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}


