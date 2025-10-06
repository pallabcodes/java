package com.netflix.productivity.auth.service;

import com.netflix.productivity.auth.entity.UserWebAuthnCredential;
import com.netflix.productivity.auth.repository.UserWebAuthnCredentialRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class WebAuthnService {
    private final UserWebAuthnCredentialRepository repo;
    private final SecureRandom rnd = new SecureRandom();

    public WebAuthnService(UserWebAuthnCredentialRepository repo) { this.repo = repo; }

    public String beginRegister(String tenantId, String userId) {
        byte[] c = new byte[32]; rnd.nextBytes(c);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(c);
    }

    public void finishRegister(String tenantId, String userId, String credentialId, String publicKey) {
        UserWebAuthnCredential cred = new UserWebAuthnCredential();
        cred.setTenantId(tenantId);
        cred.setUserId(userId);
        cred.setCredentialId(credentialId);
        cred.setPublicKey(publicKey);
        cred.setSignCount(0);
        repo.save(cred);
    }

    public String beginAssert(String tenantId, String userId) {
        byte[] c = new byte[32]; rnd.nextBytes(c);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(c);
    }

    public boolean finishAssert(String tenantId, String userId, String credentialId) {
        return repo.findByTenantIdAndUserIdAndCredentialId(tenantId, userId, credentialId).isPresent();
    }
}


