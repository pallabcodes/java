package com.netflix.productivity.reporting.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.reporting.dto.ReportRequest;
import com.netflix.productivity.reporting.dto.ReportResponse;
import com.netflix.productivity.reporting.service.ReportService;
import com.netflix.productivity.security.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Productivity and performance reporting endpoints")
public class ReportController {
    private final ReportService reportService;
    private final ResponseMapper responses;
    
    @PostMapping("/productivity")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Generate productivity report", 
               description = "Generate comprehensive productivity metrics including throughput, SLA, lead time, and cycle time")
    public ResponseEntity<ApiResponse<ReportResponse>> generateProductivityReport(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody ReportRequest request) {
        ReportResponse report = reportService.generateReport(tenantId, request);
        return responses.ok(report);
    }
    
    @PostMapping("/sla")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Generate SLA report", 
               description = "Generate SLA-specific metrics including breach rates and compliance")
    public ResponseEntity<ApiResponse<ReportResponse>> generateSlaReport(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody ReportRequest request) {
        ReportResponse report = reportService.generateSlaReport(tenantId, request);
        return responses.ok(report);
    }
    
    @PostMapping("/throughput")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Generate throughput report", 
               description = "Generate throughput-specific metrics including velocity and completion rates")
    public ResponseEntity<ApiResponse<ReportResponse>> generateThroughputReport(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody ReportRequest request) {
        ReportResponse report = reportService.generateThroughputReport(tenantId, request);
        return responses.ok(report);
    }
    
    @GetMapping("/productivity/quick")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Quick productivity report", 
               description = "Generate a quick productivity report for the last 30 days")
    public ResponseEntity<ApiResponse<ReportResponse>> generateQuickProductivityReport(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId) {
        
        ReportRequest request = new ReportRequest();
        request.setProjectId(projectId);
        request.setFromDate(OffsetDateTime.now().minusDays(30));
        request.setToDate(OffsetDateTime.now());
        request.setGroupBy("DAILY");
        request.setIncludeDetails(false);
        
        ReportResponse report = reportService.generateReport(tenantId, request);
        return responses.ok(report);
    }
    
    @GetMapping("/sla/breaches")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Get SLA breaches", 
               description = "Get current SLA breaches and at-risk issues")
    public ResponseEntity<ApiResponse<ReportResponse>> getSlaBreaches(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId,
            @Parameter(description = "Days to look back")
            @RequestParam(defaultValue = "7") int days) {
        
        ReportRequest request = new ReportRequest();
        request.setProjectId(projectId);
        request.setFromDate(OffsetDateTime.now().minusDays(days));
        request.setToDate(OffsetDateTime.now());
        request.setGroupBy("DAILY");
        request.setIncludeDetails(true);
        
        ReportResponse report = reportService.generateSlaReport(tenantId, request);
        return responses.ok(report);
    }
    
    @GetMapping("/throughput/velocity")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Get team velocity", 
               description = "Get team velocity metrics for sprint planning")
    public ResponseEntity<ApiResponse<ReportResponse>> getTeamVelocity(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId,
            @Parameter(description = "Number of sprints to analyze")
            @RequestParam(defaultValue = "4") int sprints) {
        
        ReportRequest request = new ReportRequest();
        request.setProjectId(projectId);
        request.setFromDate(OffsetDateTime.now().minusDays(sprints * 14)); // Assuming 2-week sprints
        request.setToDate(OffsetDateTime.now());
        request.setGroupBy("WEEKLY");
        request.setIncludeDetails(true);
        
        ReportResponse report = reportService.generateThroughputReport(tenantId, request);
        return responses.ok(report);
    }
    
    @GetMapping(value = "/export/csv", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Export report as CSV", 
               description = "Export productivity report data as CSV file")
    public ResponseEntity<byte[]> exportReportAsCsv(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId,
            @Parameter(description = "Start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @Parameter(description = "End date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate) {
        
        ReportRequest request = new ReportRequest();
        request.setProjectId(projectId);
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        request.setGroupBy("DAILY");
        request.setIncludeDetails(true);
        
        ReportResponse report = reportService.generateReport(tenantId, request);
        
        // Convert to CSV (simplified implementation)
        StringBuilder csv = new StringBuilder();
        csv.append("Period,Issues Created,Issues Completed,SLA Breaches,Avg Lead Time,Avg Cycle Time\n");
        
        for (ReportResponse.TimeSeriesData data : report.getTimeSeries()) {
            csv.append(String.format("%s,%d,%d,%d,%.2f,%.2f\n",
                data.getPeriod(),
                data.getIssuesCreated(),
                data.getIssuesCompleted(),
                data.getSlaBreaches(),
                data.getAvgLeadTime(),
                data.getAvgCycleTime()
            ));
        }
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=productivity_report.csv")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(csv.toString().getBytes());
    }
}
