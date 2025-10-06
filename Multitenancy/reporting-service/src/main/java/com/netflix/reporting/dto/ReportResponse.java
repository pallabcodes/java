package com.netflix.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    
    private ReportMetadata metadata;
    private ReportSummary summary;
    private List<TimeSeriesData> timeSeries;
    private List<ProjectBreakdown> projectBreakdown;
    private List<IssueTypeBreakdown> issueTypeBreakdown;
    private List<PriorityBreakdown> priorityBreakdown;
    private List<TopPerformer> topPerformers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportMetadata {
        private String reportId;
        private String reportType;
        private String tenantId;
        private String projectId;
        private OffsetDateTime generatedAt;
        private OffsetDateTime fromDate;
        private OffsetDateTime toDate;
        private String groupBy;
        private Long executionTimeMs;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportSummary {
        private String tenantId;
        private String projectId;
        private OffsetDateTime fromDate;
        private OffsetDateTime toDate;
        private Long totalIssues;
        private Long completedIssues;
        private Long createdIssues;
        private BigDecimal throughputRate;
        private Long slaBreaches;
        private Long totalSlaIssues;
        private BigDecimal slaBreachRate;
        private BigDecimal avgLeadTime;
        private BigDecimal medianLeadTime;
        private BigDecimal p95LeadTime;
        private BigDecimal avgCycleTime;
        private BigDecimal medianCycleTime;
        private BigDecimal p95CycleTime;
        private Long workflowTransitions;
        private BigDecimal avgTimeInProgress;
        private BigDecimal avgTimeInReview;
        private Long activeUsers;
        private Long activeWatchers;
        private Long totalComments;
        private Long totalAttachments;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {
        private OffsetDateTime period;
        private Long issuesCreated;
        private Long issuesCompleted;
        private BigDecimal throughput;
        private Long slaBreaches;
        private BigDecimal avgLeadTime;
        private BigDecimal avgCycleTime;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectBreakdown {
        private String projectId;
        private String projectName;
        private Long totalIssues;
        private Long completedIssues;
        private BigDecimal completionRate;
        private BigDecimal avgLeadTime;
        private Long slaBreaches;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueTypeBreakdown {
        private String issueType;
        private Long count;
        private BigDecimal percentage;
        private BigDecimal avgLeadTime;
        private Long slaBreaches;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityBreakdown {
        private String priority;
        private Long count;
        private BigDecimal percentage;
        private BigDecimal avgLeadTime;
        private Long slaBreaches;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformer {
        private String userId;
        private String userName;
        private Long issuesCompleted;
        private BigDecimal avgLeadTime;
        private Long slaBreaches;
        private BigDecimal performanceScore;
    }
}
