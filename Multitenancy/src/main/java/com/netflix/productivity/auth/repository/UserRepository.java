package com.netflix.productivity.auth.repository;

import com.netflix.productivity.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByTenantIdAndUsername(String tenantId, String username);
    Optional<User> findByTenantIdAndEmail(String tenantId, String email);
    List<User> findByTenantId(String tenantId);
}


