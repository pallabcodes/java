package com.netflix.reporting.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    
    @NotNull(message = "Project ID is required")
    private String projectId;
    
    @NotNull(message = "From date is required")
    private OffsetDateTime fromDate;
    
    @NotNull(message = "To date is required")
    private OffsetDateTime toDate;
    
    private String groupBy = "day"; // day, week, month
    
    private String reportType = "productivity"; // productivity, sla, throughput
    
    private String templateId; // Optional template to use
    
    private boolean includeTimeSeries = true;
    
    private boolean includeBreakdowns = true;
    
    private boolean includeTopPerformers = true;
}
