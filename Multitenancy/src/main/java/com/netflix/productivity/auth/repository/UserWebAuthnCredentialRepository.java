package com.netflix.productivity.auth.repository;

import com.netflix.productivity.auth.entity.UserWebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserWebAuthnCredentialRepository extends JpaRepository<UserWebAuthnCredential, Long> {
    List<UserWebAuthnCredential> findByTenantIdAndUserId(String tenantId, String userId);
    Optional<UserWebAuthnCredential> findByTenantIdAndUserIdAndCredentialId(String tenantId, String userId, String credentialId);
}


