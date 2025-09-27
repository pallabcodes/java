package com.netflix.productivity.reporting.repository;

import com.netflix.productivity.reporting.entity.ReportMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportMetrics, String> {
    
    @Query("""
        SELECT new com.netflix.productivity.reporting.entity.ReportMetrics(
            i.tenantId,
            i.projectId,
            :fromDate,
            :toDate,
            COUNT(i),
            COUNT(CASE WHEN i.status = 'DONE' THEN 1 END),
            COUNT(CASE WHEN i.createdAt >= :fromDate THEN 1 END),
            CAST(COUNT(CASE WHEN i.createdAt >= :fromDate THEN 1 END) AS DECIMAL) / 
                GREATEST(1, EXTRACT(EPOCH FROM (:toDate - :fromDate)) / 86400),
            COUNT(CASE WHEN i.slaBreached = true THEN 1 END),
            COUNT(CASE WHEN i.slaDueAt IS NOT NULL THEN 1 END),
            CASE WHEN COUNT(CASE WHEN i.slaDueAt IS NOT NULL THEN 1 END) > 0 
                THEN CAST(COUNT(CASE WHEN i.slaBreached = true THEN 1 END) AS DECIMAL) / 
                     COUNT(CASE WHEN i.slaDueAt IS NOT NULL THEN 1 END) * 100
                ELSE 0 END,
            AVG(CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                THEN EXTRACT(EPOCH FROM (i.completedAt - i.createdAt)) / 86400 END),
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY 
                CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                    THEN EXTRACT(EPOCH FROM (i.completedAt - i.createdAt)) / 86400 END),
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY 
                CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                    THEN EXTRACT(EPOCH FROM (i.completedAt - i.createdAt)) / 86400 END),
            AVG(CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                THEN EXTRACT(EPOCH FROM (i.completedAt - i.startedAt)) / 86400 END),
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY 
                CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                    THEN EXTRACT(EPOCH FROM (i.completedAt - i.startedAt)) / 86400 END),
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY 
                CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                    THEN EXTRACT(EPOCH FROM (i.completedAt - i.startedAt)) / 86400 END),
            0L, 0.0, 0.0, 0L, 0L, 0L, 0L
        )
        FROM Issue i
        WHERE i.tenantId = :tenantId
        AND i.createdAt BETWEEN :fromDate AND :toDate
        AND (:projectId IS NULL OR i.projectId = :projectId)
        GROUP BY i.tenantId, i.projectId
        """)
    List<ReportMetrics> generateReportMetrics(
        @Param("tenantId") String tenantId,
        @Param("projectId") String projectId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate
    );
    
    @Query("""
        SELECT 
            DATE_TRUNC(:groupBy, i.createdAt) as period,
            COUNT(CASE WHEN i.createdAt >= :fromDate THEN 1 END) as issuesCreated,
            COUNT(CASE WHEN i.status = 'DONE' AND i.completedAt BETWEEN :fromDate AND :toDate THEN 1 END) as issuesCompleted,
            COUNT(CASE WHEN i.slaBreached = true AND i.completedAt BETWEEN :fromDate AND :toDate THEN 1 END) as slaBreaches,
            AVG(CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                THEN EXTRACT(EPOCH FROM (i.completedAt - i.createdAt)) / 86400 END) as avgLeadTime,
            AVG(CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                THEN EXTRACT(EPOCH FROM (i.completedAt - i.startedAt)) / 86400 END) as avgCycleTime
        FROM Issue i
        WHERE i.tenantId = :tenantId
        AND i.createdAt BETWEEN :fromDate AND :toDate
        AND (:projectId IS NULL OR i.projectId = :projectId)
        GROUP BY DATE_TRUNC(:groupBy, i.createdAt)
        ORDER BY period
        """)
    List<Object[]> generateTimeSeriesData(
        @Param("tenantId") String tenantId,
        @Param("projectId") String projectId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate,
        @Param("groupBy") String groupBy
    );
    
    @Query("""
        SELECT 
            i.projectId,
            p.name,
            COUNT(i) as totalIssues,
            COUNT(CASE WHEN i.status = 'DONE' THEN 1 END) as completedIssues,
            CASE WHEN COUNT(i) > 0 
                THEN CAST(COUNT(CASE WHEN i.status = 'DONE' THEN 1 END) AS DECIMAL) / COUNT(i) * 100
                ELSE 0 END as completionRate,
            AVG(CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                THEN EXTRACT(EPOCH FROM (i.completedAt - i.createdAt)) / 86400 END) as avgLeadTime,
            AVG(CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                THEN EXTRACT(EPOCH FROM (i.completedAt - i.startedAt)) / 86400 END) as avgCycleTime,
            COUNT(CASE WHEN i.slaBreached = true THEN 1 END) as slaBreaches
        FROM Issue i
        LEFT JOIN Project p ON i.projectId = p.id AND i.tenantId = p.tenantId
        WHERE i.tenantId = :tenantId
        AND i.createdAt BETWEEN :fromDate AND :toDate
        AND (:projectId IS NULL OR i.projectId = :projectId)
        GROUP BY i.projectId, p.name
        ORDER BY totalIssues DESC
        """)
    List<Object[]> generateProjectBreakdown(
        @Param("tenantId") String tenantId,
        @Param("projectId") String projectId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate
    );
    
    @Query("""
        SELECT 
            i.type,
            COUNT(i) as count
        FROM Issue i
        WHERE i.tenantId = :tenantId
        AND i.createdAt BETWEEN :fromDate AND :toDate
        AND (:projectId IS NULL OR i.projectId = :projectId)
        GROUP BY i.type
        ORDER BY count DESC
        """)
    List<Object[]> generateIssueTypeBreakdown(
        @Param("tenantId") String tenantId,
        @Param("projectId") String projectId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate
    );
    
    @Query("""
        SELECT 
            i.priority,
            COUNT(i) as count
        FROM Issue i
        WHERE i.tenantId = :tenantId
        AND i.createdAt BETWEEN :fromDate AND :toDate
        AND (:projectId IS NULL OR i.projectId = :projectId)
        GROUP BY i.priority
        ORDER BY count DESC
        """)
    List<Object[]> generatePriorityBreakdown(
        @Param("tenantId") String tenantId,
        @Param("projectId") String projectId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate
    );
    
    @Query("""
        SELECT 
            i.assigneeId,
            u.username,
            COUNT(i) as issuesAssigned,
            COUNT(CASE WHEN i.status = 'DONE' THEN 1 END) as issuesCompleted,
            CASE WHEN COUNT(i) > 0 
                THEN CAST(COUNT(CASE WHEN i.status = 'DONE' THEN 1 END) AS DECIMAL) / COUNT(i) * 100
                ELSE 0 END as completionRate,
            AVG(CASE WHEN i.status = 'DONE' AND i.completedAt IS NOT NULL 
                THEN EXTRACT(EPOCH FROM (i.completedAt - i.startedAt)) / 86400 END) as avgResolutionTime
        FROM Issue i
        LEFT JOIN User u ON i.assigneeId = u.id AND i.tenantId = u.tenantId
        WHERE i.tenantId = :tenantId
        AND i.createdAt BETWEEN :fromDate AND :toDate
        AND (:projectId IS NULL OR i.projectId = :projectId)
        AND i.assigneeId IS NOT NULL
        GROUP BY i.assigneeId, u.username
        ORDER BY issuesCompleted DESC, completionRate DESC
        LIMIT 10
        """)
    List<Object[]> generateTopPerformers(
        @Param("tenantId") String tenantId,
        @Param("projectId") String projectId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate
    );
}

