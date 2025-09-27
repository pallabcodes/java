package com.netflix.productivity.label.repository;

import com.netflix.productivity.label.entity.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, String> {
    Page<Label> findByTenantIdAndDeletedAtIsNullOrderByNameAsc(String tenantId, Pageable pageable);
    Optional<Label> findByTenantIdAndName(String tenantId, String name);
}

