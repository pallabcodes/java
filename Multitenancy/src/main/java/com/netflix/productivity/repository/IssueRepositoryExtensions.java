package com.netflix.productivity.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface IssueRepositoryExtensions {
    
    @Query("SELECT COUNT(DISTINCT i.assigneeId) FROM Issue i WHERE i.tenantId = :tenantId AND (:projectId IS NULL OR i.projectId = :projectId) AND i.updatedAt BETWEEN :fromDate AND :toDate")
    long countDistinctAssigneeIdByTenantIdAndProjectIdAndUpdatedAtBetween(
        @Param("tenantId") String tenantId, 
        @Param("projectId") String projectId, 
        @Param("fromDate") OffsetDateTime fromDate, 
        @Param("toDate") OffsetDateTime toDate);
    
    @Query("SELECT COUNT(c) FROM Comment c JOIN c.issue i WHERE i.tenantId = :tenantId AND (:projectId IS NULL OR i.projectId = :projectId) AND c.createdAt BETWEEN :fromDate AND :toDate")
    long countCommentsByTenantIdAndProjectIdAndCreatedAtBetween(
        @Param("tenantId") String tenantId, 
        @Param("projectId") String projectId, 
        @Param("fromDate") OffsetDateTime fromDate, 
        @Param("toDate") OffsetDateTime toDate);
    
    @Query("SELECT COUNT(a) FROM Attachment a JOIN a.issue i WHERE i.tenantId = :tenantId AND (:projectId IS NULL OR i.projectId = :projectId) AND a.createdAt BETWEEN :fromDate AND :toDate")
    long countAttachmentsByTenantIdAndProjectIdAndCreatedAtBetween(
        @Param("tenantId") String tenantId, 
        @Param("projectId") String projectId, 
        @Param("fromDate") OffsetDateTime fromDate, 
        @Param("toDate") OffsetDateTime toDate);
    
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (i.completedAt - i.startedAt)) / 86400) FROM Issue i WHERE i.tenantId = :tenantId AND (:projectId IS NULL OR i.projectId = :projectId) AND i.status = 'DONE' AND i.completedAt BETWEEN :fromDate AND :toDate")
    Double getAverageResolutionTimeByTenantIdAndProjectIdAndCompletedAtBetween(
        @Param("tenantId") String tenantId, 
        @Param("projectId") String projectId, 
        @Param("fromDate") OffsetDateTime fromDate, 
        @Param("toDate") OffsetDateTime toDate);
}
