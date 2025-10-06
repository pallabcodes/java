package com.netflix.productivity.auth.repository;

import com.netflix.productivity.auth.entity.DeviceFingerprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceFingerprintRepository extends JpaRepository<DeviceFingerprint, Long> {
    Optional<DeviceFingerprint> findByTenantIdAndUserIdAndHash(String tenantId, String userId, String hash);
}


