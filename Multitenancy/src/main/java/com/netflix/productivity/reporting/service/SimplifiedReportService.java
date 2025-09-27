package com.netflix.productivity.reporting.service;

import com.netflix.productivity.repository.IssueRepository;
import com.netflix.productivity.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimplifiedReportService {
    
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "reports", key = "#tenantId + '_' + #projectId + '_' + #fromDate + '_' + #toDate")
    public Map<String, Object> getMetrics(String tenantId, String projectId, OffsetDateTime fromDate, OffsetDateTime toDate) {
        log.info("Generating metrics for tenant {} from {} to {}", tenantId, fromDate, toDate);
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Basic counts
        long totalIssues = issueRepository.countByTenantIdAndProjectIdAndCreatedAtBetween(tenantId, projectId, fromDate, toDate);
        long completedIssues = issueRepository.countByTenantIdAndProjectIdAndStatusAndCompletedAtBetween(tenantId, projectId, "DONE", fromDate, toDate);
        long slaBreaches = issueRepository.countByTenantIdAndProjectIdAndSlaBreachedTrueAndCompletedAtBetween(tenantId, projectId, fromDate, toDate);
        
        // Calculate rates
        double completionRate = totalIssues > 0 ? (double) completedIssues / totalIssues * 100 : 0;
        double slaBreachRate = completedIssues > 0 ? (double) slaBreaches / completedIssues * 100 : 0;
        
        // Calculate throughput (issues per day)
        long days = java.time.Duration.between(fromDate, toDate).toDays();
        double throughput = days > 0 ? (double) completedIssues / days : 0;
        
        metrics.put("totalIssues", totalIssues);
        metrics.put("completedIssues", completedIssues);
        metrics.put("slaBreaches", slaBreaches);
        metrics.put("completionRate", round(completionRate, 2));
        metrics.put("slaBreachRate", round(slaBreachRate, 2));
        metrics.put("throughput", round(throughput, 2));
        metrics.put("period", Map.of(
            "from", fromDate,
            "to", toDate,
            "days", days
        ));
        
        return metrics;
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "throughput", key = "#tenantId + '_' + #projectId + '_' + #days")
    public Map<String, Object> getThroughput(String tenantId, String projectId, int days) {
        log.info("Generating throughput metrics for tenant {} for last {} days", tenantId, days);
        
        OffsetDateTime fromDate = OffsetDateTime.now().minusDays(days);
        OffsetDateTime toDate = OffsetDateTime.now();
        
        Map<String, Object> throughput = new HashMap<>();
        
        // Weekly throughput
        long weeklyCompleted = issueRepository.countByTenantIdAndProjectIdAndStatusAndCompletedAtBetween(
            tenantId, projectId, "DONE", fromDate, toDate);
        double weeklyThroughput = days > 0 ? (double) weeklyCompleted / (days / 7.0) : 0;
        
        // Daily average
        double dailyAverage = days > 0 ? (double) weeklyCompleted / days : 0;
        
        // Trend (compare with previous period)
        OffsetDateTime prevFromDate = fromDate.minusDays(days);
        long prevWeeklyCompleted = issueRepository.countByTenantIdAndProjectIdAndStatusAndCompletedAtBetween(
            tenantId, projectId, "DONE", prevFromDate, fromDate);
        double trend = prevWeeklyCompleted > 0 ? 
            ((double) weeklyCompleted - prevWeeklyCompleted) / prevWeeklyCompleted * 100 : 0;
        
        throughput.put("weeklyThroughput", round(weeklyThroughput, 2));
        throughput.put("dailyAverage", round(dailyAverage, 2));
        throughput.put("trend", round(trend, 2));
        throughput.put("period", Map.of(
            "from", fromDate,
            "to", toDate,
            "days", days
        ));
        
        return throughput;
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "sla", key = "#tenantId + '_' + #projectId + '_' + #days")
    public Map<String, Object> getSlaMetrics(String tenantId, String projectId, int days) {
        log.info("Generating SLA metrics for tenant {} for last {} days", tenantId, days);
        
        OffsetDateTime fromDate = OffsetDateTime.now().minusDays(days);
        OffsetDateTime toDate = OffsetDateTime.now();
        
        Map<String, Object> slaMetrics = new HashMap<>();
        
        // SLA compliance
        long totalSlaIssues = issueRepository.countByTenantIdAndProjectIdAndSlaDueAtIsNotNullAndCreatedAtBetween(
            tenantId, projectId, fromDate, toDate);
        long slaBreaches = issueRepository.countByTenantIdAndProjectIdAndSlaBreachedTrueAndCompletedAtBetween(
            tenantId, projectId, fromDate, toDate);
        double complianceRate = totalSlaIssues > 0 ? 
            (double) (totalSlaIssues - slaBreaches) / totalSlaIssues * 100 : 100;
        
        // Current breaches
        long currentBreaches = issueRepository.countByTenantIdAndProjectIdAndSlaBreachedTrueAndStatusNot(
            tenantId, projectId, "DONE");
        
        slaMetrics.put("totalSlaIssues", totalSlaIssues);
        slaMetrics.put("slaBreaches", slaBreaches);
        slaMetrics.put("complianceRate", round(complianceRate, 2));
        slaMetrics.put("currentBreaches", currentBreaches);
        slaMetrics.put("period", Map.of(
            "from", fromDate,
            "to", toDate,
            "days", days
        ));
        
        return slaMetrics;
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "team-performance", key = "#tenantId + '_' + #projectId + '_' + #days")
    public Map<String, Object> getTeamPerformance(String tenantId, String projectId, int days) {
        log.info("Generating team performance metrics for tenant {} for last {} days", tenantId, days);
        
        OffsetDateTime fromDate = OffsetDateTime.now().minusDays(days);
        OffsetDateTime toDate = OffsetDateTime.now();
        
        Map<String, Object> teamPerformance = new HashMap<>();
        
        // Team activity
        long activeUsers = issueRepository.countDistinctAssigneeIdByTenantIdAndProjectIdAndUpdatedAtBetween(
            tenantId, projectId, fromDate, toDate);
        long totalComments = issueRepository.countCommentsByTenantIdAndProjectIdAndCreatedAtBetween(
            tenantId, projectId, fromDate, toDate);
        long totalAttachments = issueRepository.countAttachmentsByTenantIdAndProjectIdAndCreatedAtBetween(
            tenantId, projectId, fromDate, toDate);
        
        // Average resolution time (simplified)
        Double avgResolutionTime = issueRepository.getAverageResolutionTimeByTenantIdAndProjectIdAndCompletedAtBetween(
            tenantId, projectId, fromDate, toDate);
        
        teamPerformance.put("activeUsers", activeUsers);
        teamPerformance.put("totalComments", totalComments);
        teamPerformance.put("totalAttachments", totalAttachments);
        teamPerformance.put("avgResolutionTime", avgResolutionTime != null ? round(avgResolutionTime, 2) : 0);
        teamPerformance.put("period", Map.of(
            "from", fromDate,
            "to", toDate,
            "days", days
        ));
        
        return teamPerformance;
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "quick-stats", key = "#tenantId + '_' + #projectId")
    public Map<String, Object> getQuickStats(String tenantId, String projectId) {
        log.info("Generating quick stats for tenant {}", tenantId);
        
        Map<String, Object> quickStats = new HashMap<>();
        
        // Current counts
        long totalIssues = issueRepository.countByTenantIdAndProjectId(tenantId, projectId);
        long openIssues = issueRepository.countByTenantIdAndProjectIdAndStatusNot(tenantId, projectId, "DONE");
        long completedToday = issueRepository.countByTenantIdAndProjectIdAndStatusAndCompletedAtBetween(
            tenantId, projectId, "DONE", 
            OffsetDateTime.now().toLocalDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset()),
            OffsetDateTime.now());
        long slaBreaches = issueRepository.countByTenantIdAndProjectIdAndSlaBreachedTrueAndStatusNot(
            tenantId, projectId, "DONE");
        
        quickStats.put("totalIssues", totalIssues);
        quickStats.put("openIssues", openIssues);
        quickStats.put("completedToday", completedToday);
        quickStats.put("slaBreaches", slaBreaches);
        quickStats.put("completionRate", totalIssues > 0 ? 
            round((double) (totalIssues - openIssues) / totalIssues * 100, 2) : 0);
        
        return quickStats;
    }
    
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
