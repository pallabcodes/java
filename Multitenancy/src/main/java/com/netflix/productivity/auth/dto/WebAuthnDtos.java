package com.netflix.productivity.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class WebAuthnDtos {
    public static class BeginRegisterRequest {
        @NotBlank private String tenantId;
        @NotBlank private String userId;
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    public static class BeginRegisterResponse {
        private String challenge;
        public BeginRegisterResponse() {}
        public BeginRegisterResponse(String challenge) { this.challenge = challenge; }
        public String getChallenge() { return challenge; }
        public void setChallenge(String challenge) { this.challenge = challenge; }
    }
    public static class FinishRegisterRequest {
        @NotBlank private String tenantId;
        @NotBlank private String userId;
        @NotBlank private String credentialId;
        @NotBlank private String publicKey;
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getCredentialId() { return credentialId; }
        public void setCredentialId(String credentialId) { this.credentialId = credentialId; }
        public String getPublicKey() { return publicKey; }
        public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    }
    public static class BeginAssertRequest {
        @NotBlank private String tenantId;
        @NotBlank private String userId;
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    public static class BeginAssertResponse {
        private String challenge;
        public BeginAssertResponse() {}
        public BeginAssertResponse(String challenge) { this.challenge = challenge; }
        public String getChallenge() { return challenge; }
        public void setChallenge(String challenge) { this.challenge = challenge; }
    }
    public static class FinishAssertRequest {
        @NotBlank private String tenantId;
        @NotBlank private String userId;
        @NotBlank private String credentialId;
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getCredentialId() { return credentialId; }
        public void setCredentialId(String credentialId) { this.credentialId = credentialId; }
    }
}


