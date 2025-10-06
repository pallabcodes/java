package com.netflix.productivity.auth.service;

import com.netflix.productivity.auth.entity.DeviceFingerprint;
import com.netflix.productivity.auth.repository.DeviceFingerprintRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class DeviceTrustService {

    private final DeviceFingerprintRepository repo;

    public DeviceTrustService(DeviceFingerprintRepository repo) { this.repo = repo; }

    public String fingerprintHash(String userAgent, String ip, String extra) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = (userAgent + "|" + ip + "|" + (extra == null ? "" : extra)).getBytes(StandardCharsets.UTF_8);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest(bytes));
        } catch (Exception e) {
            throw new IllegalStateException("hash failed", e);
        }
    }

    public boolean isTrusted(String tenantId, String userId, String hash) {
        return repo.findByTenantIdAndUserIdAndHash(tenantId, userId, hash).isPresent();
    }

    public void register(String tenantId, String userId, String hash, String label) {
        if (isTrusted(tenantId, userId, hash)) return;
        DeviceFingerprint d = new DeviceFingerprint();
        d.setTenantId(tenantId);
        d.setUserId(userId);
        d.setHash(hash);
        d.setLabel(label);
        repo.save(d);
    }
}


