package com.netflix.productivity.reporting.service;

import com.netflix.productivity.reporting.dto.ReportRequest;
import com.netflix.productivity.reporting.dto.ReportResponse;
import com.netflix.productivity.reporting.entity.ReportMetrics;
import com.netflix.productivity.reporting.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "reports", key = "#tenantId + '_' + #request.hashCode()")
    public ReportResponse generateReport(String tenantId, ReportRequest request) {
        log.info("Generating report for tenant {} from {} to {}", 
                tenantId, request.getFromDate(), request.getToDate());
        
        // Generate summary metrics
        List<ReportMetrics> metrics = reportRepository.generateReportMetrics(
            tenantId, request.getProjectId(), request.getFromDate(), request.getToDate()
        );
        
        ReportMetrics summary = metrics.isEmpty() ? 
            ReportMetrics.builder()
                .tenantId(tenantId)
                .projectId(request.getProjectId())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .totalIssues(0L)
                .completedIssues(0L)
                .createdIssues(0L)
                .throughputRate(BigDecimal.ZERO)
                .slaBreaches(0L)
                .totalSlaIssues(0L)
                .slaBreachRate(BigDecimal.ZERO)
                .avgLeadTime(BigDecimal.ZERO)
                .medianLeadTime(BigDecimal.ZERO)
                .p95LeadTime(BigDecimal.ZERO)
                .avgCycleTime(BigDecimal.ZERO)
                .medianCycleTime(BigDecimal.ZERO)
                .p95CycleTime(BigDecimal.ZERO)
                .workflowTransitions(0L)
                .avgTimeInProgress(BigDecimal.ZERO)
                .avgTimeInReview(BigDecimal.ZERO)
                .activeUsers(0L)
                .activeWatchers(0L)
                .totalComments(0L)
                .totalAttachments(0L)
                .build() : 
            metrics.get(0);
        
        // Generate time series data
        List<Object[]> timeSeriesRaw = reportRepository.generateTimeSeriesData(
            tenantId, request.getProjectId(), request.getFromDate(), 
            request.getToDate(), request.getGroupBy()
        );
        
        List<ReportResponse.TimeSeriesData> timeSeries = timeSeriesRaw.stream()
            .map(row -> ReportResponse.TimeSeriesData.builder()
                .period((OffsetDateTime) row[0])
                .issuesCreated(((Number) row[1]).longValue())
                .issuesCompleted(((Number) row[2]).longValue())
                .slaBreaches(((Number) row[3]).longValue())
                .avgLeadTime(row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO)
                .avgCycleTime(row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO)
                .build())
            .collect(Collectors.toList());
        
        // Generate project breakdown
        List<Object[]> projectBreakdownRaw = reportRepository.generateProjectBreakdown(
            tenantId, request.getProjectId(), request.getFromDate(), request.getToDate()
        );
        
        List<ReportResponse.ProjectBreakdown> projectBreakdown = projectBreakdownRaw.stream()
            .map(row -> ReportResponse.ProjectBreakdown.builder()
                .projectId((String) row[0])
                .projectName((String) row[1])
                .totalIssues(((Number) row[2]).longValue())
                .completedIssues(((Number) row[3]).longValue())
                .completionRate(row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO)
                .avgLeadTime(row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO)
                .avgCycleTime(row[6] != null ? (BigDecimal) row[6] : BigDecimal.ZERO)
                .slaBreaches(((Number) row[7]).longValue())
                .build())
            .collect(Collectors.toList());
        
        // Generate issue type breakdown
        List<Object[]> issueTypeRaw = reportRepository.generateIssueTypeBreakdown(
            tenantId, request.getProjectId(), request.getFromDate(), request.getToDate()
        );
        
        Map<String, Long> issueTypeBreakdown = issueTypeRaw.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
            ));
        
        // Generate priority breakdown
        List<Object[]> priorityRaw = reportRepository.generatePriorityBreakdown(
            tenantId, request.getProjectId(), request.getFromDate(), request.getToDate()
        );
        
        Map<String, Long> priorityBreakdown = priorityRaw.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
            ));
        
        // Generate top performers
        List<Object[]> topPerformersRaw = reportRepository.generateTopPerformers(
            tenantId, request.getProjectId(), request.getFromDate(), request.getToDate()
        );
        
        List<ReportResponse.UserMetrics> topPerformers = topPerformersRaw.stream()
            .map(row -> ReportResponse.UserMetrics.builder()
                .userId((String) row[0])
                .userName((String) row[1])
                .issuesAssigned(((Number) row[2]).longValue())
                .issuesCompleted(((Number) row[3]).longValue())
                .completionRate(row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO)
                .avgResolutionTime(row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO)
                .build())
            .collect(Collectors.toList());
        
        // Build metadata
        ReportResponse.ReportMetadata metadata = ReportResponse.ReportMetadata.builder()
            .tenantId(tenantId)
            .generatedAt(OffsetDateTime.now())
            .reportType("PRODUCTIVITY")
            .groupBy(request.getGroupBy())
            .fromDate(request.getFromDate())
            .toDate(request.getToDate())
            .totalRecords(summary.getTotalIssues())
            .build();
        
        return ReportResponse.builder()
            .metadata(metadata)
            .summary(summary)
            .timeSeries(timeSeries)
            .projectBreakdown(projectBreakdown)
            .issueTypeBreakdown(issueTypeBreakdown)
            .priorityBreakdown(priorityBreakdown)
            .topPerformers(topPerformers)
            .build();
    }
    
    @Transactional(readOnly = true)
    public ReportResponse generateSlaReport(String tenantId, ReportRequest request) {
        log.info("Generating SLA report for tenant {} from {} to {}", 
                tenantId, request.getFromDate(), request.getToDate());
        
        // Focus on SLA-specific metrics
        ReportResponse report = generateReport(tenantId, request);
        
        // Override metadata for SLA report
        report.getMetadata().setReportType("SLA");
        
        return report;
    }
    
    @Transactional(readOnly = true)
    public ReportResponse generateThroughputReport(String tenantId, ReportRequest request) {
        log.info("Generating throughput report for tenant {} from {} to {}", 
                tenantId, request.getFromDate(), request.getToDate());
        
        // Focus on throughput-specific metrics
        ReportResponse report = generateReport(tenantId, request);
        
        // Override metadata for throughput report
        report.getMetadata().setReportType("THROUGHPUT");
        
        return report;
    }
}
