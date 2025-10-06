package com.netflix.productivity.auth.repository;

import com.netflix.productivity.auth.entity.UserMfa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMfaRepository extends JpaRepository<UserMfa, Long> {
    Optional<UserMfa> findByTenantIdAndUserId(String tenantId, String userId);
}


