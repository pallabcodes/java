package com.netflix.reporting.controller;

import com.netflix.reporting.dto.ReportRequest;
import com.netflix.reporting.dto.ReportResponse;
import com.netflix.reporting.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Productivity and performance reporting endpoints")
public class ReportController {
    
    private final ReportService reportService;
    
    @PostMapping("/productivity")
    @Operation(summary = "Generate productivity report", 
               description = "Generate comprehensive productivity metrics including throughput, SLA, lead time, and cycle time")
    public ResponseEntity<ReportResponse> generateProductivityReport(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody ReportRequest request) {
        
        log.info("Generating productivity report for tenant {}", tenantId);
        ReportResponse report = reportService.generateReport(tenantId, request);
        return ResponseEntity.ok(report);
    }
    
    @PostMapping("/sla")
    @Operation(summary = "Generate SLA report", 
               description = "Generate SLA-specific metrics including breach rates and compliance")
    public ResponseEntity<ReportResponse> generateSlaReport(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody ReportRequest request) {
        
        log.info("Generating SLA report for tenant {}", tenantId);
        request.setReportType("sla");
        ReportResponse report = reportService.generateReport(tenantId, request);
        return ResponseEntity.ok(report);
    }
    
    @PostMapping("/throughput")
    @Operation(summary = "Generate throughput report", 
               description = "Generate throughput-specific metrics including velocity and completion rates")
    public ResponseEntity<ReportResponse> generateThroughputReport(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody ReportRequest request) {
        
        log.info("Generating throughput report for tenant {}", tenantId);
        request.setReportType("throughput");
        ReportResponse report = reportService.generateReport(tenantId, request);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the reporting service is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Reporting service is healthy");
    }
}
