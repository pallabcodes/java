package com.netflix.productivity.reporting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMetrics {
    private String tenantId;
    private String projectId;
    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
    
    // Throughput metrics
    private Long totalIssues;
    private Long completedIssues;
    private Long createdIssues;
    private BigDecimal throughputRate; // issues per day
    
    // SLA metrics
    private Long slaBreaches;
    private Long totalSlaIssues;
    private BigDecimal slaBreachRate; // percentage
    
    // Lead time metrics
    private BigDecimal avgLeadTime; // days
    private BigDecimal medianLeadTime; // days
    private BigDecimal p95LeadTime; // days
    
    // Cycle time metrics
    private BigDecimal avgCycleTime; // days
    private BigDecimal medianCycleTime; // days
    private BigDecimal p95CycleTime; // days
    
    // Workflow metrics
    private Long workflowTransitions;
    private BigDecimal avgTimeInProgress; // days
    private BigDecimal avgTimeInReview; // days
    
    // Team metrics
    private Long activeUsers;
    private Long activeWatchers;
    private Long totalComments;
    private Long totalAttachments;
}

