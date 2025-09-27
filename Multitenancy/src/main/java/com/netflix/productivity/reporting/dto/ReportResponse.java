package com.netflix.productivity.reporting.dto;

import com.netflix.productivity.reporting.entity.ReportMetrics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Report response with metrics and breakdowns")
public class ReportResponse {
    @Schema(description = "Report metadata")
    private ReportMetadata metadata;
    
    @Schema(description = "Overall metrics for the period")
    private ReportMetrics summary;
    
    @Schema(description = "Time series data grouped by period")
    private List<TimeSeriesData> timeSeries;
    
    @Schema(description = "Breakdown by project")
    private List<ProjectBreakdown> projectBreakdown;
    
    @Schema(description = "Breakdown by issue type")
    private Map<String, Long> issueTypeBreakdown;
    
    @Schema(description = "Breakdown by priority")
    private Map<String, Long> priorityBreakdown;
    
    @Schema(description = "Top performers")
    private List<UserMetrics> topPerformers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportMetadata {
        private String tenantId;
        private OffsetDateTime generatedAt;
        private String reportType;
        private String groupBy;
        private OffsetDateTime fromDate;
        private OffsetDateTime toDate;
        private Long totalRecords;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {
        private OffsetDateTime period;
        private Long issuesCreated;
        private Long issuesCompleted;
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
        private BigDecimal avgCycleTime;
        private Long slaBreaches;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserMetrics {
        private String userId;
        private String userName;
        private Long issuesAssigned;
        private Long issuesCompleted;
        private BigDecimal completionRate;
        private BigDecimal avgResolutionTime;
    }
}

