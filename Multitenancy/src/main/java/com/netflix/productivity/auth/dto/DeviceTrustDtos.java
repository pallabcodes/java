package com.netflix.productivity.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceTrustDtos {
    public static class RegisterRequest {
        @NotBlank private String tenantId;
        @NotBlank private String userId;
        @NotBlank private String userAgent;
        @NotBlank private String ip;
        private String label;
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    public static class CheckRequest {
        @NotBlank private String tenantId;
        @NotBlank private String userId;
        @NotBlank private String userAgent;
        @NotBlank private String ip;
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
    }
}


