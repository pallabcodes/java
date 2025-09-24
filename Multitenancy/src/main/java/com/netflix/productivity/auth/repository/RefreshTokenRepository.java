package com.netflix.productivity.auth.repository;

import com.netflix.productivity.auth.entity.RefreshToken;
import com.netflix.productivity.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTenantIdAndTokenHash(String tenantId, String tokenHash);
    long deleteByTenantIdAndUserAndExpiresAtBefore(String tenantId, User user, Instant cutoff);

    List<RefreshToken> findByTenantIdAndUserAndRevokedAtIsNullAndExpiresAtAfter(String tenantId, User user, Instant now);
}


