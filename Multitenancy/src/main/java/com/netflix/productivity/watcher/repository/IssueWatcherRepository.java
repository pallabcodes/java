package com.netflix.productivity.watcher.repository;

import com.netflix.productivity.watcher.entity.IssueWatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueWatcherRepository extends JpaRepository<IssueWatcher, String> {
    Page<IssueWatcher> findByTenantIdAndIssueId(String tenantId, String issueId, Pageable pageable);
    boolean existsByTenantIdAndIssueIdAndUserId(String tenantId, String issueId, String userId);
    void deleteByTenantIdAndIssueIdAndUserId(String tenantId, String issueId, String userId);
}

