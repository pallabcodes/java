package com.netflix.productivity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TotpDtos {
    public static class ProvisionRequest {
        @NotBlank
        private String tenantId;
        @NotBlank
        private String userId;
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    public static class ProvisionResponse {
        private String secret;
        private String otpauth;
        public ProvisionResponse() {}
        public ProvisionResponse(String secret, String otpauth) { this.secret = secret; this.otpauth = otpauth; }
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getOtpauth() { return otpauth; }
        public void setOtpauth(String otpauth) { this.otpauth = otpauth; }
    }

    public static class VerifyRequest {
        @NotBlank
        private String tenantId;
        @NotBlank
        private String userId;
        @NotNull
        private Integer code;
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public Integer getCode() { return code; }
        public void setCode(Integer code) { this.code = code; }
    }
}


