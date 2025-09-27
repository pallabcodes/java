package com.netflix.productivity.reporting.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.reporting.service.SimplifiedReportService;
import com.netflix.productivity.security.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
@Tag(name = "Simplified Reports", description = "Simplified reporting endpoints")
public class SimplifiedReportController {
    
    private final SimplifiedReportService reportService;
    private final ResponseMapper responses;
    
    @GetMapping("/metrics")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Get productivity metrics", 
               description = "Get simplified productivity metrics for a tenant and project")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetrics(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId,
            @Parameter(description = "Start date")
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @Parameter(description = "End date")
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate) {
        
        Map<String, Object> metrics = reportService.getMetrics(tenantId, projectId, fromDate, toDate);
        return responses.ok(metrics);
    }
    
    @GetMapping("/throughput")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Get throughput metrics", 
               description = "Get throughput metrics for team velocity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getThroughput(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId,
            @Parameter(description = "Days to look back")
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> throughput = reportService.getThroughput(tenantId, projectId, days);
        return responses.ok(throughput);
    }
    
    @GetMapping("/sla")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Get SLA metrics", 
               description = "Get SLA compliance and breach metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlaMetrics(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId,
            @Parameter(description = "Days to look back")
            @RequestParam(defaultValue = "7") int days) {
        
        Map<String, Object> slaMetrics = reportService.getSlaMetrics(tenantId, projectId, days);
        return responses.ok(slaMetrics);
    }
    
    @GetMapping("/team-performance")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Get team performance metrics", 
               description = "Get team performance and productivity metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTeamPerformance(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId,
            @Parameter(description = "Days to look back")
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> teamPerformance = reportService.getTeamPerformance(tenantId, projectId, days);
        return responses.ok(teamPerformance);
    }
    
    @GetMapping("/quick-stats")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Get quick statistics", 
               description = "Get quick statistics for dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuickStats(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId) {
        
        Map<String, Object> quickStats = reportService.getQuickStats(tenantId, projectId);
        return responses.ok(quickStats);
    }
}
