package com.netflix.productivity.auth.repository;

import com.netflix.productivity.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByTenantIdAndUserId(String tenantId, String userId);
}


