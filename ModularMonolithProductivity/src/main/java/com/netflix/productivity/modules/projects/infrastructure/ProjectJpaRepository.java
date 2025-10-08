package com.netflix.productivity.modules.projects.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, String> {
}


