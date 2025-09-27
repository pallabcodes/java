package com.netflix.productivity.reporting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Schema(description = "Request for generating reports")
public class ReportRequest {
    @Schema(description = "Project ID to filter by (optional)", example = "proj_123")
    private String projectId;
    
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @Schema(description = "Start date for the report", example = "2024-01-01T00:00:00.000Z")
    private OffsetDateTime fromDate;
    
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @Schema(description = "End date for the report", example = "2024-01-31T23:59:59.999Z")
    private OffsetDateTime toDate;
    
    @Schema(description = "Group by period", example = "DAILY", allowableValues = {"HOURLY", "DAILY", "WEEKLY", "MONTHLY"})
    private String groupBy = "DAILY";
    
    @Schema(description = "Include detailed breakdown", example = "true")
    private Boolean includeDetails = false;
}

