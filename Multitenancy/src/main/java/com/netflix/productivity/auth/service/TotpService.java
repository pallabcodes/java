package com.netflix.productivity.auth.service;

import com.netflix.productivity.auth.entity.UserMfa;
import com.netflix.productivity.auth.repository.UserMfaRepository;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;

@Service
public class TotpService {

    private final UserMfaRepository userMfaRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public TotpService(UserMfaRepository userMfaRepository) {
        this.userMfaRepository = userMfaRepository;
    }

    public String generateSecret() {
        byte[] buffer = new byte[20];
        secureRandom.nextBytes(buffer);
        return new Base32().encodeToString(buffer).replace("=", "");
    }

    public boolean verifyCode(String base32Secret, int code) {
        long timestep = 30L;
        long counter = Instant.now().getEpochSecond() / timestep;
        for (long i = -1; i <= 1; i++) {
            if (code == generateTotp(base32Secret, counter + i)) return true;
        }
        return false;
    }

    private int generateTotp(String base32Secret, long counter) {
        byte[] key = new Base32().decode(base32Secret);
        byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
            return binary % 1000000;
        } catch (Exception e) {
            throw new IllegalStateException("TOTP generation failed", e);
        }
    }

    public UserMfa provision(String tenantId, String userId) {
        String secret = generateSecret();
        UserMfa m = userMfaRepository.findByTenantIdAndUserId(tenantId, userId).orElseGet(UserMfa::new);
        m.setTenantId(tenantId);
        m.setUserId(userId);
        m.setTotpSecret(secret);
        m.setEnabled(false);
        return userMfaRepository.save(m);
    }

    public boolean enable(String tenantId, String userId, int code) {
        UserMfa m = userMfaRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("mfa not provisioned"));
        if (verifyCode(m.getTotpSecret(), code)) {
            m.setEnabled(true);
            userMfaRepository.save(m);
            return true;
        }
        return false;
    }
}


