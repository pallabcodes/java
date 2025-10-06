package com.netflix.productivity.auth.service;

import com.netflix.productivity.auth.entity.RefreshToken;
import com.netflix.productivity.auth.entity.User;
import com.netflix.productivity.auth.repository.RefreshTokenRepository;
import com.netflix.productivity.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               JwtTokenService jwtTokenService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public String issue(String tenantId, String userId, Duration ttl, String userAgent, String ip) {
        User user = findUserOrThrow(userId);
        String raw = TokenUtil.generateToken(48);
        String hash = sha256(raw);

        RefreshToken token = new RefreshToken();
        token.setTenantId(tenantId);
        token.setUser(user);
        token.setTokenHash(hash);
        token.setUserAgent(userAgent);
        token.setIpAddress(ip);
        token.setExpiresAt(Instant.now().plus(ttl));
        refreshTokenRepository.save(token);
        return raw;
    }

    @Transactional
    public String rotate(String tenantId, String oldRawToken, Duration ttl, String userAgent, String ip) {
        String oldHash = sha256(oldRawToken);
        RefreshToken old = findTokenOrThrow(tenantId, oldHash);
        // Reuse detection: if token was already used (revoked), revoke all sessions for the user
        if (old.getRevokedAt() != null) {
            revokeAllForUser(tenantId, String.valueOf(old.getUser().getId()));
            throw new IllegalArgumentException("refresh token reuse detected");
        }
        assertActive(old);

        String newRaw = TokenUtil.generateToken(48);
        String newHash = sha256(newRaw);

        RefreshToken next = new RefreshToken();
        next.setTenantId(old.getTenantId());
        next.setUser(old.getUser());
        next.setTokenHash(newHash);
        next.setParentTokenHash(old.getTokenHash());
        next.setUserAgent(userAgent);
        next.setIpAddress(ip);
        next.setExpiresAt(Instant.now().plus(ttl));
        refreshTokenRepository.save(next);

        old.setRevokedAt(Instant.now());
        return newRaw;
    }

    @Transactional
    public void revoke(String tenantId, String rawToken) {
        String hash = sha256(rawToken);
        RefreshToken token = findTokenOrThrow(tenantId, hash);
        token.setRevokedAt(Instant.now());
    }

    @Transactional
    public void revokeById(String tenantId, Long tokenId) {
        RefreshToken token = refreshTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("refresh token not found"));
        if (!tenantId.equals(token.getTenantId())) {
            throw new IllegalArgumentException("forbidden");
        }
        token.setRevokedAt(Instant.now());
    }

    public String mintAccessToken(User user) {
        return jwtTokenService.generateToken(user.getId().toString(), user.getTenantId(), user.getRolesAsNames());
    }

    @Transactional(readOnly = true)
    public User getUserFromRefresh(String tenantId, String rawToken) {
        String hash = sha256(rawToken);
        RefreshToken token = findTokenOrThrow(tenantId, hash);
        if (token.getRevokedAt() != null) {
            revokeAllForUser(tenantId, String.valueOf(token.getUser().getId()));
            throw new IllegalArgumentException("refresh token reuse detected");
        }
        assertActive(token);
        return token.getUser();
    }

    @Transactional(readOnly = true)
    public java.util.List<RefreshToken> listActiveSessions(String tenantId, String userId) {
        User user = findUserOrThrow(userId);
        return refreshTokenRepository.findByTenantIdAndUserAndRevokedAtIsNullAndExpiresAtAfter(tenantId, user, Instant.now());
    }

    @Transactional
    public void revokeAllForUser(String tenantId, String userId) {
        User user = findUserOrThrow(userId);
        java.util.List<RefreshToken> active = refreshTokenRepository.findByTenantIdAndUserAndRevokedAtIsNullAndExpiresAtAfter(tenantId, user, Instant.now());
        Instant now = Instant.now();
        for (RefreshToken t : active) {
            t.setRevokedAt(now);
        }
    }
}

    private User findUserOrThrow(String userId) {
        return userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
    }

    private RefreshToken findTokenOrThrow(String tenantId, String tokenHash) {
        return refreshTokenRepository.findByTenantIdAndTokenHash(tenantId, tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("invalid refresh token"));
    }

    private void assertActive(RefreshToken token) {
        if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("expired or revoked refresh token");
        }
    }

    private String sha256(String raw) {
        return TokenUtil.sha256(raw);
    }


