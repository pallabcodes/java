package com.netflix.productivity.modules.issues.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueJpaRepository extends JpaRepository<IssueEntity, String> {
}


