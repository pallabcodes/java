package com.netflix.reporting.repository;

import com.netflix.reporting.entity.ReportMetricsAgg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ReportMetricsAggRepository extends JpaRepository<ReportMetricsAgg, String> {
    
    @Query("SELECT rma FROM ReportMetricsAgg rma WHERE rma.tenantId = :tenantId " +
           "AND (:projectId IS NULL OR rma.projectId = :projectId) " +
           "AND rma.metricType = :metricType " +
           "AND rma.periodStart >= :fromDate " +
           "AND rma.periodEnd <= :toDate " +
           "ORDER BY rma.periodStart")
    List<ReportMetricsAgg> findMetricsByPeriod(@Param("tenantId") String tenantId,
                                               @Param("projectId") String projectId,
                                               @Param("metricType") String metricType,
                                               @Param("fromDate") OffsetDateTime fromDate,
                                               @Param("toDate") OffsetDateTime toDate);
    
    @Query("SELECT rma FROM ReportMetricsAgg rma WHERE rma.tenantId = :tenantId " +
           "AND rma.aggregationPeriod = :period " +
           "AND rma.periodStart >= :fromDate " +
           "AND rma.periodEnd <= :toDate " +
           "ORDER BY rma.periodStart")
    List<ReportMetricsAgg> findMetricsByAggregationPeriod(@Param("tenantId") String tenantId,
                                                           @Param("period") String aggregationPeriod,
                                                           @Param("fromDate") OffsetDateTime fromDate,
                                                           @Param("toDate") OffsetDateTime toDate);
    
    @Query("SELECT rma FROM ReportMetricsAgg rma WHERE rma.tenantId = :tenantId " +
           "AND rma.projectId = :projectId " +
           "AND rma.periodStart >= :fromDate " +
           "AND rma.periodEnd <= :toDate " +
           "ORDER BY rma.metricType, rma.metricName")
    List<ReportMetricsAgg> findProjectMetrics(@Param("tenantId") String tenantId,
                                              @Param("projectId") String projectId,
                                              @Param("fromDate") OffsetDateTime fromDate,
                                              @Param("toDate") OffsetDateTime toDate);
    
    ReportMetricsAgg findByTenantIdAndProjectIdAndMetricTypeAndMetricNameAndAggregationPeriodAndPeriodStart(
        String tenantId, String projectId, String metricType, String metricName, 
        String aggregationPeriod, OffsetDateTime periodStart);
}
