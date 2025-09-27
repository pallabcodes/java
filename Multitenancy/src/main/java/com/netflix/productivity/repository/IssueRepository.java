package com.netflix.productivity.repository;

import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.entity.Issue.IssuePriority;
import com.netflix.productivity.entity.Issue.IssueStatus;
import com.netflix.productivity.entity.Issue.IssueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import com.netflix.productivity.repository.projection.IssueListProjection;
import com.netflix.productivity.repository.projection.SearchHitProjection;

public interface IssueRepository extends JpaRepository<Issue, String> {

    @Query("select i from Issue i where i.tenantId = :tenantId and i.deletedAt is null")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_TIMEOUT, value = "5000")
    })
    Page<Issue> findAllActiveByTenant(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.projectId = :projectId and i.deletedAt is null")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_TIMEOUT, value = "5000")
    })
    Page<Issue> findByTenantAndProject(@Param("tenantId") String tenantId, @Param("projectId") String projectId, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.key = :key and i.deletedAt is null")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_TIMEOUT, value = "3000")
    })
    Optional<Issue> findByTenantAndKey(@Param("tenantId") String tenantId, @Param("key") String key);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.status = :status and i.deletedAt is null")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_TIMEOUT, value = "5000")
    })
    Page<Issue> findByStatus(@Param("tenantId") String tenantId, @Param("status") IssueStatus status, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.priority = :priority and i.deletedAt is null")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_TIMEOUT, value = "5000")
    })
    Page<Issue> findByPriority(@Param("tenantId") String tenantId, @Param("priority") IssuePriority priority, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.type = :type and i.deletedAt is null")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_TIMEOUT, value = "5000")
    })
    Page<Issue> findByType(@Param("tenantId") String tenantId, @Param("type") IssueType type, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.assigneeId = :assigneeId and i.deletedAt is null")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_TIMEOUT, value = "5000")
    })
    Page<Issue> findByAssignee(@Param("tenantId") String tenantId, @Param("assigneeId") String assigneeId, Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.createdAt between :from and :to and i.deletedAt is null")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "1000"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_TIMEOUT, value = "10000")
    })
    Page<Issue> findCreatedBetween(@Param("tenantId") String tenantId,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to,
                                   Pageable pageable);

    @Query("select i from Issue i where i.tenantId = :tenantId and i.deletedAt is null and i.slaBreachedAt is null and i.dueDate is not null and i.dueDate < :now and i.status <> com.netflix.productivity.entity.Issue$IssueStatus.CLOSED and i.status <> com.netflix.productivity.entity.Issue$IssueStatus.RESOLVED")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_TIMEOUT, value = "5000")
    })
    List<Issue> findOverdueUnmarked(@Param("tenantId") String tenantId, @Param("now") LocalDateTime now);

    @Query("select i.id as id, i.key as key, i.title as title, cast(i.status as string) as status, cast(i.priority as string) as priority, i.assigneeId as assigneeId, i.updatedAt as updatedAt from Issue i where i.tenantId = :tenantId and i.deletedAt is null")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_TIMEOUT, value = "5000")
    })
    Page<IssueListProjection> findListProjection(@Param("tenantId") String tenantId, Pageable pageable);

    @Query(value = "select id, key, title, (greatest(similarity(title, :q), similarity(coalesce(description,''), :q))) as score from issues where tenant_id = :tenantId and deleted_at is null and (title % :q or coalesce(description,'') % :q) order by score desc", nativeQuery = true)
    Page<SearchHitProjection> searchCombined(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}


