package com.netflix.productivity.repository;

import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.entity.Issue.IssuePriority;
import com.netflix.productivity.entity.Issue.IssueStatus;
import com.netflix.productivity.entity.Issue.IssueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, String> {

    @Query("select i from Issue i where i.tenantId = :tenantId and i.deletedAt is null")
    Page<Issue> findAllActiveByTenant(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.projectId = :projectId and i.deletedAt is null")
    Page<Issue> findByTenantAndProject(@Param("tenantId") String tenantId, @Param("projectId") String projectId, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.key = :key and i.deletedAt is null")
    Optional<Issue> findByTenantAndKey(@Param("tenantId") String tenantId, @Param("key") String key);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.status = :status and i.deletedAt is null")
    Page<Issue> findByStatus(@Param("tenantId") String tenantId, @Param("status") IssueStatus status, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.priority = :priority and i.deletedAt is null")
    Page<Issue> findByPriority(@Param("tenantId") String tenantId, @Param("priority") IssuePriority priority, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.type = :type and i.deletedAt is null")
    Page<Issue> findByType(@Param("tenantId") String tenantId, @Param("type") IssueType type, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.assigneeId = :assigneeId and i.deletedAt is null")
    Page<Issue> findByAssignee(@Param("tenantId") String tenantId, @Param("assigneeId") String assigneeId, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.createdAt between :from and :to and i.deletedAt is null")
    Page<Issue> findCreatedBetween(@Param("tenantId") String tenantId,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to,
                                   Pageable pageable);
}


