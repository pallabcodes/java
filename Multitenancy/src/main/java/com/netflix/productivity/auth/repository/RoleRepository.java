package com.netflix.productivity.auth.repository;

import com.netflix.productivity.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByTenantIdAndName(String tenantId, String name);
}


