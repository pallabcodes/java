package com.netflix.productivity.auth.service;

import com.netflix.productivity.auth.entity.User;
import com.netflix.productivity.auth.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class PasswordResetService {

    private final StringRedisTemplate redis;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public PasswordResetService(StringRedisTemplate redis,
                                UserRepository userRepository,
                                PasswordService passwordService) {
        this.redis = redis;
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Transactional(readOnly = true)
    public String issueResetToken(String tenantId, String email, Duration ttl) {
        User user = userRepository.findByTenantIdAndEmail(tenantId, email)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        String token = TokenUtil.generateToken(32);
        String tokenHash = TokenUtil.sha256(token);
        String key = key(tenantId, tokenHash);
        redis.opsForValue().set(key, user.getId().toString(), ttl);
        return token;
    }

    @Transactional
    public void consumeResetToken(String tenantId, String token, String newRawPassword) {
        String tokenHash = TokenUtil.sha256(token);
        String key = key(tenantId, tokenHash);
        String userId = redis.opsForValue().get(key);
        if (userId == null) {
            throw new IllegalArgumentException("invalid or expired token");
        }
        redis.delete(key);
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        user.setPasswordHash(passwordService.hashPassword(newRawPassword));
    }

    private static String key(String tenantId, String tokenHash) {
        return "reset:" + tenantId + ":" + tokenHash;
    }
}


