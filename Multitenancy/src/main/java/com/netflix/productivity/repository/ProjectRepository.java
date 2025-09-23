package com.netflix.productivity.repository;

import com.netflix.productivity.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, String> {

    @Query("select p from Project p where p.tenantId = :tenantId and p.deletedAt is null")
    Page<Project> findAllActiveByTenant(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("select p from Project p where p.tenantId = :tenantId and p.key = :key and p.deletedAt is null")
    Optional<Project> findByTenantAndKey(@Param("tenantId") String tenantId, @Param("key") String key);
}


