package org.example.compliance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/compliance")
@Tag(name = "Compliance", description = "Compliance and audit endpoints")
public class ComplianceController {

    private final AuditLogger auditLogger;
    private final DataProtectionService dataProtectionService;

    @Autowired
    public ComplianceController(AuditLogger auditLogger, DataProtectionService dataProtectionService) {
        this.auditLogger = auditLogger;
        this.dataProtectionService = dataProtectionService;
    }

    @GetMapping("/audit-trail")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Operation(summary = "Get audit trail", description = "Retrieve audit events with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit trail retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Map<String, Object>> getAuditTrail(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditEvent> auditPage = auditLogger.getAuditTrail(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("events", auditPage.getContent());
        response.put("currentPage", auditPage.getNumber());
        response.put("totalPages", auditPage.getTotalPages());
        response.put("totalElements", auditPage.getTotalElements());
        response.put("size", auditPage.getSize());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit-trail/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or #userId == authentication.name")
    @Operation(summary = "Get user audit trail", description = "Retrieve audit events for a specific user")
    public ResponseEntity<Map<String, Object>> getUserAuditTrail(
            @Parameter(description = "User ID")
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditEvent> auditPage = auditLogger.getUserAuditTrail(userId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("events", auditPage.getContent());
        response.put("currentPage", auditPage.getNumber());
        response.put("totalPages", auditPage.getTotalPages());
        response.put("totalElements", auditPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit-trail/type/{eventType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Operation(summary = "Get audit events by type", description = "Retrieve audit events filtered by event type")
    public ResponseEntity<Map<String, Object>> getAuditTrailByType(
            @Parameter(description = "Audit event type")
            @PathVariable AuditEventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditEvent> auditPage = auditLogger.getAuditTrailByType(eventType, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("eventType", eventType);
        response.put("events", auditPage.getContent());
        response.put("currentPage", auditPage.getNumber());
        response.put("totalPages", auditPage.getTotalPages());
        response.put("totalElements", auditPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit-trail/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Operation(summary = "Get audit events by date range", description = "Retrieve audit events within a date range")
    public ResponseEntity<Map<String, Object>> getAuditTrailByDateRange(
            @Parameter(description = "Start date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditEvent> auditPage = auditLogger.getAuditTrailByDateRange(startDate, endDate, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("events", auditPage.getContent());
        response.put("currentPage", auditPage.getNumber());
        response.put("totalPages", auditPage.getTotalPages());
        response.put("totalElements", auditPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit-trail/security-events")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY')")
    @Operation(summary = "Get recent security events", description = "Retrieve recent security-related audit events")
    public ResponseEntity<List<AuditEvent>> getRecentSecurityEvents(
            @Parameter(description = "Hours to look back")
            @RequestParam(defaultValue = "24") int hours) {

        List<AuditEvent> securityEvents = auditLogger.getRecentSecurityEvents(hours);

        return ResponseEntity.ok(securityEvents);
    }

    @GetMapping("/audit-trail/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Operation(summary = "Get audit event summary", description = "Get summary statistics of audit events")
    public ResponseEntity<Map<String, Object>> getAuditEventSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Long> summary = auditLogger.getAuditEventSummary(startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("summary", summary);
        response.put("totalEvents", summary.values().stream().mapToLong(Long::longValue).sum());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/gdpr/data-access")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "GDPR Data Access Request", description = "Request access to personal data under GDPR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data access request processed"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Map<String, Object>> requestDataAccess(
            @Parameter(description = "Additional details for the request")
            @RequestBody(required = false) Map<String, Object> requestDetails,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        Map<String, Object> details = requestDetails != null ? requestDetails : new HashMap<>();
        details.put("requestType", "DATA_ACCESS");

        Map<String, Object> response = dataProtectionService.processGDPRRequest(
                userId, DataProtectionService.GDPRRequestType.ACCESS, details);

        auditLogger.logGDPRRequest(userId, "DATA_ACCESS", (String) response.get("status"), details);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/gdpr/data-erasure")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "GDPR Data Erasure Request", description = "Request deletion of personal data under GDPR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data erasure request processed"),
        @ApiResponse(responseCode = "403", description = "Erasure not allowed for this data")
    })
    public ResponseEntity<Map<String, Object>> requestDataErasure(
            @Parameter(description = "Reason for data erasure")
            @RequestBody Map<String, Object> erasureDetails,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        erasureDetails.put("requestType", "DATA_ERASURE");

        Map<String, Object> response = dataProtectionService.processGDPRRequest(
                userId, DataProtectionService.GDPRRequestType.ERASURE, erasureDetails);

        auditLogger.logGDPRRequest(userId, "DATA_ERASURE", (String) response.get("status"), erasureDetails);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/gdpr/data-portability")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "GDPR Data Portability Request", description = "Request data export for portability")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data export generated"),
        @ApiResponse(responseCode = "500", description = "Export generation failed")
    })
    public ResponseEntity<Map<String, Object>> requestDataPortability(
            @Parameter(description = "Export format preference")
            @RequestParam(defaultValue = "JSON") String format,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        Map<String, Object> details = Map.of("exportFormat", format, "requestType", "DATA_PORTABILITY");

        Map<String, Object> response = dataProtectionService.processGDPRRequest(
                userId, DataProtectionService.GDPRRequestType.PORTABILITY, details);

        auditLogger.logGDPRRequest(userId, "DATA_PORTABILITY", (String) response.get("status"), details);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/gdpr/data-restriction")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "GDPR Processing Restriction", description = "Request restriction of data processing")
    public ResponseEntity<Map<String, Object>> requestProcessingRestriction(
            @Parameter(description = "Details about processing to restrict")
            @RequestBody Map<String, Object> restrictionDetails,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        restrictionDetails.put("requestType", "PROCESSING_RESTRICTION");

        Map<String, Object> response = dataProtectionService.processGDPRRequest(
                userId, DataProtectionService.GDPRRequestType.RESTRICTION, restrictionDetails);

        auditLogger.logGDPRRequest(userId, "PROCESSING_RESTRICTION", (String) response.get("status"), restrictionDetails);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/gdpr/processing-report")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get Data Processing Report", description = "Retrieve information about data processing activities")
    public ResponseEntity<Map<String, Object>> getDataProcessingReport(Authentication authentication) {
        String userId = getUserIdFromAuthentication(authentication);

        Map<String, Object> report = dataProtectionService.getDataProcessingReport(userId);

        auditLogger.logUserAction(userId, "PROCESSING_REPORT_ACCESS", "gdpr_processing_report",
                Map.of("reportGenerated", true), getClientIpFromAuthentication(authentication));

        return ResponseEntity.ok(report);
    }

    @GetMapping("/compliance/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Operation(summary = "Generate Compliance Report", description = "Generate audit compliance report for a date range")
    public ResponseEntity<AuditComplianceReport> generateComplianceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        AuditComplianceReport report = auditLogger.generateComplianceReport(startDate, endDate);

        auditLogger.logComplianceEvent("COMPLIANCE_REPORT_GENERATED",
                "Compliance report generated for period: " + startDate + " to " + endDate,
                Map.of("startDate", startDate, "endDate", endDate, "reportGenerated", true));

        return ResponseEntity.ok(report);
    }

    @PostMapping("/data-retention/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cleanup Old Audit Data", description = "Remove audit events older than retention period")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cleanup completed successfully"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ResponseEntity<Map<String, Object>> cleanupOldAuditData(
            @Parameter(description = "Retention period in days")
            @RequestParam(defaultValue = "90") int retentionDays) {

        auditLogger.cleanupOldAuditEvents(retentionDays);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Audit data cleanup completed");
        response.put("retentionDays", retentionDays);
        response.put("timestamp", LocalDateTime.now());

        auditLogger.logComplianceEvent("AUDIT_DATA_CLEANUP",
                "Cleaned up audit events older than " + retentionDays + " days",
                Map.of("retentionDays", retentionDays));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/data-classification")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Operation(summary = "Get Data Classification Info", description = "Retrieve data classification information")
    public ResponseEntity<Map<String, Object>> getDataClassificationInfo() {
        Map<String, Object> classifications = new HashMap<>();
        classifications.put("FINANCIAL", "Payment and financial transaction data");
        classifications.put("PERSONAL", "User profile and personal information");
        classifications.put("SENSITIVE", "Audit logs and security-related data");
        classifications.put("INTERNAL", "System configuration and operational data");

        Map<String, Object> response = new HashMap<>();
        response.put("classifications", classifications);
        response.put("retentionPolicies", Map.of(
            "FINANCIAL", "7 years",
            "PERSONAL", "Account active + 3 years",
            "SENSITIVE", "3 years",
            "INTERNAL", "2 years"
        ));

        return ResponseEntity.ok(response);
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            return authentication.getName();
        }
        throw new IllegalStateException("Unable to determine user ID from authentication");
    }

    private String getClientIpFromAuthentication(Authentication authentication) {
        // In a real implementation, extract IP from request attributes
        return "system";
    }
}