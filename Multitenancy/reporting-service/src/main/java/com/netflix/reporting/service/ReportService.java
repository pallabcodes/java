package com.netflix.reporting.service;

import com.netflix.reporting.client.CoreServiceClient;
import com.netflix.reporting.dto.*;
import com.netflix.reporting.entity.ReportCache;
import com.netflix.reporting.repository.ReportCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final CoreServiceClient coreServiceClient;
    private final ReportCacheRepository reportCacheRepository;
    private final ReportMetricsService reportMetricsService;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "reports", key = "#tenantId + '_' + #request.hashCode()")
    public ReportResponse generateReport(String tenantId, ReportRequest request) {
        log.info("Generating report for tenant {} from {} to {}", 
                tenantId, request.getFromDate(), request.getToDate());
        
        long startTime = System.currentTimeMillis();
        
        // Check cache first
        String cacheKey = generateCacheKey(request);
        Optional<ReportCache> cached = reportCacheRepository.findValidCache(
            tenantId, cacheKey, OffsetDateTime.now());
        
        if (cached.isPresent()) {
            log.info("Returning cached report for tenant {}", tenantId);
            return parseCachedReport(cached.get().getReportData());
        }
        
        // Fetch data from core service
        List<CoreIssueData> issues = coreServiceClient.getIssues(
            tenantId, request.getProjectId(), request.getFromDate(), request.getToDate());
        
        List<CoreProjectData> projects = coreServiceClient.getProjects(
            tenantId, request.getProjectId());
        
        List<CoreUserData> users = coreServiceClient.getUsers(tenantId);
        
        // Generate report
        ReportResponse report = buildReport(tenantId, request, issues, projects, users);
        
        // Cache the result
        cacheReport(tenantId, cacheKey, report);
        
        long executionTime = System.currentTimeMillis() - startTime;
        report.getMetadata().setExecutionTimeMs(executionTime);
        
        log.info("Generated report for tenant {} in {}ms", tenantId, executionTime);
        return report;
    }
    
    private String generateCacheKey(ReportRequest request) {
        return String.format("report_%s_%s_%s_%s_%s", 
            request.getProjectId(),
            request.getFromDate().toString(),
            request.getToDate().toString(),
            request.getGroupBy(),
            request.getReportType());
    }
    
    private ReportResponse buildReport(String tenantId, ReportRequest request, 
                                     List<CoreIssueData> issues, 
                                     List<CoreProjectData> projects,
                                     List<CoreUserData> users) {
        
        ReportResponse.ReportMetadata metadata = ReportResponse.ReportMetadata.builder()
            .reportId(UUID.randomUUID().toString())
            .reportType(request.getReportType())
            .tenantId(tenantId)
            .projectId(request.getProjectId())
            .generatedAt(OffsetDateTime.now())
            .fromDate(request.getFromDate())
            .toDate(request.getToDate())
            .groupBy(request.getGroupBy())
            .build();
        
        ReportResponse.ReportSummary summary = calculateSummary(issues);
        
        List<ReportResponse.TimeSeriesData> timeSeries = request.isIncludeTimeSeries() ? 
            calculateTimeSeries(issues, request.getGroupBy()) : Collections.emptyList();
        
        List<ReportResponse.ProjectBreakdown> projectBreakdown = request.isIncludeBreakdowns() ? 
            calculateProjectBreakdown(issues, projects) : Collections.emptyList();
        
        List<ReportResponse.IssueTypeBreakdown> issueTypeBreakdown = request.isIncludeBreakdowns() ? 
            calculateIssueTypeBreakdown(issues) : Collections.emptyList();
        
        List<ReportResponse.PriorityBreakdown> priorityBreakdown = request.isIncludeBreakdowns() ? 
            calculatePriorityBreakdown(issues) : Collections.emptyList();
        
        List<ReportResponse.TopPerformer> topPerformers = request.isIncludeTopPerformers() ? 
            calculateTopPerformers(issues, users) : Collections.emptyList();
        
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
    
    private ReportResponse.ReportSummary calculateSummary(List<CoreIssueData> issues) {
        if (issues.isEmpty()) {
            return ReportResponse.ReportSummary.builder()
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
                .build();
        }
        
        long totalIssues = issues.size();
        long completedIssues = issues.stream()
            .mapToLong(i -> "COMPLETED".equals(i.getStatus()) ? 1L : 0L)
            .sum();
        long createdIssues = issues.stream()
            .mapToLong(i -> i.getCreatedAt() != null ? 1L : 0L)
            .sum();
        long slaBreaches = issues.stream()
            .mapToLong(i -> i.getSlaBreachedAt() != null ? 1L : 0L)
            .sum();
        
        BigDecimal throughputRate = totalIssues > 0 ? 
            BigDecimal.valueOf(completedIssues).divide(BigDecimal.valueOf(totalIssues), 4, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        BigDecimal slaBreachRate = totalIssues > 0 ? 
            BigDecimal.valueOf(slaBreaches).divide(BigDecimal.valueOf(totalIssues), 4, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        // Calculate lead times
        List<BigDecimal> leadTimes = issues.stream()
            .filter(i -> i.getCompletedAt() != null && i.getCreatedAt() != null)
            .map(i -> BigDecimal.valueOf(i.getCompletedAt().toEpochSecond() - i.getCreatedAt().toEpochSecond()))
            .collect(Collectors.toList());
        
        BigDecimal avgLeadTime = calculateAverage(leadTimes);
        BigDecimal medianLeadTime = calculateMedian(leadTimes);
        BigDecimal p95LeadTime = calculatePercentile(leadTimes, 95);
        
        // Calculate cycle times (time from in-progress to completed)
        List<BigDecimal> cycleTimes = issues.stream()
            .filter(i -> i.getCompletedAt() != null && i.getUpdatedAt() != null)
            .map(i -> BigDecimal.valueOf(i.getCompletedAt().toEpochSecond() - i.getUpdatedAt().toEpochSecond()))
            .collect(Collectors.toList());
        
        BigDecimal avgCycleTime = calculateAverage(cycleTimes);
        BigDecimal medianCycleTime = calculateMedian(cycleTimes);
        BigDecimal p95CycleTime = calculatePercentile(cycleTimes, 95);
        
        long totalComments = issues.stream()
            .mapToLong(i -> i.getCommentsCount() != null ? i.getCommentsCount() : 0L)
            .sum();
        
        long totalAttachments = issues.stream()
            .mapToLong(i -> i.getAttachmentsCount() != null ? i.getAttachmentsCount() : 0L)
            .sum();
        
        long activeWatchers = issues.stream()
            .mapToLong(i -> i.getWatchersCount() != null ? i.getWatchersCount() : 0L)
            .sum();
        
        Set<String> activeUsers = issues.stream()
            .map(CoreIssueData::getAssigneeId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        return ReportResponse.ReportSummary.builder()
            .totalIssues(totalIssues)
            .completedIssues(completedIssues)
            .createdIssues(createdIssues)
            .throughputRate(throughputRate)
            .slaBreaches(slaBreaches)
            .totalSlaIssues(totalIssues)
            .slaBreachRate(slaBreachRate)
            .avgLeadTime(avgLeadTime)
            .medianLeadTime(medianLeadTime)
            .p95LeadTime(p95LeadTime)
            .avgCycleTime(avgCycleTime)
            .medianCycleTime(medianCycleTime)
            .p95CycleTime(p95CycleTime)
            .workflowTransitions(0L) // Would need workflow data
            .avgTimeInProgress(BigDecimal.ZERO) // Would need workflow state data
            .avgTimeInReview(BigDecimal.ZERO) // Would need workflow state data
            .activeUsers((long) activeUsers.size())
            .activeWatchers(activeWatchers)
            .totalComments(totalComments)
            .totalAttachments(totalAttachments)
            .build();
    }
    
    private List<ReportResponse.TimeSeriesData> calculateTimeSeries(List<CoreIssueData> issues, String groupBy) {
        // Group issues by time period
        Map<OffsetDateTime, List<CoreIssueData>> grouped = issues.stream()
            .collect(Collectors.groupingBy(issue -> {
                OffsetDateTime date = issue.getCreatedAt();
                if (date == null) return OffsetDateTime.now();
                
                // Simple grouping by day for now
                return date.withHour(0).withMinute(0).withSecond(0).withNano(0);
            }));
        
        return grouped.entrySet().stream()
            .map(entry -> {
                List<CoreIssueData> periodIssues = entry.getValue();
                long created = periodIssues.size();
                long completed = periodIssues.stream()
                    .mapToLong(i -> "COMPLETED".equals(i.getStatus()) ? 1L : 0L)
                    .sum();
                long slaBreaches = periodIssues.stream()
                    .mapToLong(i -> i.getSlaBreachedAt() != null ? 1L : 0L)
                    .sum();
                
                return ReportResponse.TimeSeriesData.builder()
                    .period(entry.getKey())
                    .issuesCreated(created)
                    .issuesCompleted(completed)
                    .throughput(BigDecimal.valueOf(completed))
                    .slaBreaches(slaBreaches)
                    .avgLeadTime(BigDecimal.ZERO) // Simplified for now
                    .avgCycleTime(BigDecimal.ZERO) // Simplified for now
                    .build();
            })
            .sorted(Comparator.comparing(ReportResponse.TimeSeriesData::getPeriod))
            .collect(Collectors.toList());
    }
    
    private List<ReportResponse.ProjectBreakdown> calculateProjectBreakdown(List<CoreIssueData> issues, List<CoreProjectData> projects) {
        Map<String, List<CoreIssueData>> byProject = issues.stream()
            .collect(Collectors.groupingBy(CoreIssueData::getProjectId));
        
        return byProject.entrySet().stream()
            .map(entry -> {
                String projectId = entry.getKey();
                List<CoreIssueData> projectIssues = entry.getValue();
                
                String projectName = projects.stream()
                    .filter(p -> p.getId().equals(projectId))
                    .findFirst()
                    .map(CoreProjectData::getName)
                    .orElse("Unknown Project");
                
                long total = projectIssues.size();
                long completed = projectIssues.stream()
                    .mapToLong(i -> "COMPLETED".equals(i.getStatus()) ? 1L : 0L)
                    .sum();
                long slaBreaches = projectIssues.stream()
                    .mapToLong(i -> i.getSlaBreachedAt() != null ? 1L : 0L)
                    .sum();
                
                BigDecimal completionRate = total > 0 ? 
                    BigDecimal.valueOf(completed).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;
                
                return ReportResponse.ProjectBreakdown.builder()
                    .projectId(projectId)
                    .projectName(projectName)
                    .totalIssues(total)
                    .completedIssues(completed)
                    .completionRate(completionRate)
                    .avgLeadTime(BigDecimal.ZERO) // Simplified
                    .slaBreaches(slaBreaches)
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private List<ReportResponse.IssueTypeBreakdown> calculateIssueTypeBreakdown(List<CoreIssueData> issues) {
        Map<String, Long> byType = issues.stream()
            .collect(Collectors.groupingBy(CoreIssueData::getType, Collectors.counting()));
        
        long total = issues.size();
        
        return byType.entrySet().stream()
            .map(entry -> {
                String type = entry.getKey();
                Long count = entry.getValue();
                BigDecimal percentage = total > 0 ? 
                    BigDecimal.valueOf(count).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;
                
                return ReportResponse.IssueTypeBreakdown.builder()
                    .issueType(type)
                    .count(count)
                    .percentage(percentage)
                    .avgLeadTime(BigDecimal.ZERO) // Simplified
                    .slaBreaches(0L) // Simplified
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private List<ReportResponse.PriorityBreakdown> calculatePriorityBreakdown(List<CoreIssueData> issues) {
        Map<String, Long> byPriority = issues.stream()
            .collect(Collectors.groupingBy(CoreIssueData::getPriority, Collectors.counting()));
        
        long total = issues.size();
        
        return byPriority.entrySet().stream()
            .map(entry -> {
                String priority = entry.getKey();
                Long count = entry.getValue();
                BigDecimal percentage = total > 0 ? 
                    BigDecimal.valueOf(count).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;
                
                return ReportResponse.PriorityBreakdown.builder()
                    .priority(priority)
                    .count(count)
                    .percentage(percentage)
                    .avgLeadTime(BigDecimal.ZERO) // Simplified
                    .slaBreaches(0L) // Simplified
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private List<ReportResponse.TopPerformer> calculateTopPerformers(List<CoreIssueData> issues, List<CoreUserData> users) {
        Map<String, List<CoreIssueData>> byUser = issues.stream()
            .filter(i -> i.getAssigneeId() != null)
            .collect(Collectors.groupingBy(CoreIssueData::getAssigneeId));
        
        return byUser.entrySet().stream()
            .map(entry -> {
                String userId = entry.getKey();
                List<CoreIssueData> userIssues = entry.getValue();
                
                String userName = users.stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst()
                    .map(u -> u.getFirstName() + " " + u.getLastName())
                    .orElse("Unknown User");
                
                long completed = userIssues.stream()
                    .mapToLong(i -> "COMPLETED".equals(i.getStatus()) ? 1L : 0L)
                    .sum();
                long slaBreaches = userIssues.stream()
                    .mapToLong(i -> i.getSlaBreachedAt() != null ? 1L : 0L)
                    .sum();
                
                BigDecimal performanceScore = userIssues.size() > 0 ? 
                    BigDecimal.valueOf(completed).divide(BigDecimal.valueOf(userIssues.size()), 4, RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;
                
                return ReportResponse.TopPerformer.builder()
                    .userId(userId)
                    .userName(userName)
                    .issuesCompleted(completed)
                    .avgLeadTime(BigDecimal.ZERO) // Simplified
                    .slaBreaches(slaBreaches)
                    .performanceScore(performanceScore)
                    .build();
            })
            .sorted(Comparator.comparing(ReportResponse.TopPerformer::getPerformanceScore).reversed())
            .limit(10)
            .collect(Collectors.toList());
    }
    
    private BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO;
        return values.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateMedian(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO;
        List<BigDecimal> sorted = values.stream().sorted().collect(Collectors.toList());
        int size = sorted.size();
        if (size % 2 == 0) {
            return sorted.get(size / 2 - 1).add(sorted.get(size / 2))
                .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        } else {
            return sorted.get(size / 2);
        }
    }
    
    private BigDecimal calculatePercentile(List<BigDecimal> values, int percentile) {
        if (values.isEmpty()) return BigDecimal.ZERO;
        List<BigDecimal> sorted = values.stream().sorted().collect(Collectors.toList());
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }
    
    private void cacheReport(String tenantId, String cacheKey, ReportResponse report) {
        try {
            // Convert report to JSON string (simplified)
            String reportData = "{}"; // Would use ObjectMapper in real implementation
            
            ReportCache cache = ReportCache.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .cacheKey(cacheKey)
                .reportData(reportData)
                .expiresAt(OffsetDateTime.now().plusHours(1)) // 1 hour cache
                .createdBy("system")
                .build();
            
            reportCacheRepository.save(cache);
        } catch (Exception e) {
            log.warn("Failed to cache report for tenant {}", tenantId, e);
        }
    }
    
    private ReportResponse parseCachedReport(String reportData) {
        // Would use ObjectMapper in real implementation
        return ReportResponse.builder().build();
    }
}
