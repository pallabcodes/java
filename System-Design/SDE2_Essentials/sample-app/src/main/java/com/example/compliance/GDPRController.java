package com.example.compliance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gdpr")
@Tag(name = "GDPR Compliance", description = "GDPR compliance endpoints for data protection")
public class GDPRController {

    private final DataProtectionService dataProtectionService;
    private final AuditLogger auditLogger;

    public GDPRController(DataProtectionService dataProtectionService, AuditLogger auditLogger) {
        this.dataProtectionService = dataProtectionService;
        this.auditLogger = auditLogger;
    }

    @PostMapping("/access")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Request access to personal data",
               description = "Submit a GDPR right of access request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data access request processed"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> requestDataAccess(
            @Parameter(description = "Additional details for the access request")
            @RequestBody(required = false) Map<String, Object> requestDetails,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        Map<String, Object> details = requestDetails != null ? requestDetails : new HashMap<>();

        Map<String, Object> response = dataProtectionService.processGDPRRequest(
                userId, DataProtectionService.GDPRRequestType.ACCESS, details);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rectification")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Request data rectification",
               description = "Submit a GDPR right to rectification request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rectification request processed"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Map<String, Object>> requestDataRectification(
            @Parameter(description = "Data to be rectified")
            @RequestBody Map<String, Object> rectificationData,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);

        Map<String, Object> response = dataProtectionService.processGDPRRequest(
                userId, DataProtectionService.GDPRRequestType.RECTIFICATION, rectificationData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/erasure")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Request data erasure (right to be forgotten)",
               description = "Submit a GDPR right to erasure request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Erasure request processed"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Map<String, Object>> requestDataErasure(
            @Parameter(description = "Reason for erasure request")
            @RequestBody(required = false) Map<String, Object> requestDetails,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        Map<String, Object> details = requestDetails != null ? requestDetails : new HashMap<>();
        details.put("erasureReason", "User requested data deletion");

        Map<String, Object> response = dataProtectionService.processGDPRRequest(
                userId, DataProtectionService.GDPRRequestType.ERASURE, details);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/portability")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Request data portability",
               description = "Submit a GDPR data portability request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data export generated"),
        @ApiResponse(responseCode = "500", description = "Export generation failed")
    })
    public ResponseEntity<Map<String, Object>> requestDataPortability(
            @Parameter(description = "Export format preference (JSON, XML, etc.)")
            @RequestParam(defaultValue = "JSON") String format,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        Map<String, Object> details = Map.of("exportFormat", format);

        Map<String, Object> response = dataProtectionService.processGDPRRequest(
                userId, DataProtectionService.GDPRRequestType.PORTABILITY, details);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("/restriction")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Request processing restriction",
               description = "Submit a GDPR right to restrict processing request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Processing restriction applied"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Map<String, Object>> requestProcessingRestriction(
            @Parameter(description = "Details about processing to restrict")
            @RequestBody Map<String, Object> restrictionDetails,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);

        Map<String, Object> response = dataProtectionService.processGDPRRequest(
                userId, DataProtectionService.GDPRRequestType.RESTRICTION, restrictionDetails);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/objection")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Object to data processing",
               description = "Submit a GDPR right to object request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Objection processed"),
        @ApiResponse(responseCode = "400", description = "Invalid objection details")
    })
    public ResponseEntity<Map<String, Object>> objectToProcessing(
            @Parameter(description = "Details about the objection")
            @RequestBody Map<String, Object> objectionDetails,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);

        Map<String, Object> response = dataProtectionService.processGDPRRequest(
                userId, DataProtectionService.GDPRRequestType.OBJECTION, objectionDetails);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/processing-report")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get data processing report",
               description = "Retrieve information about how user data is processed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Processing report generated")
    })
    public ResponseEntity<Map<String, Object>> getDataProcessingReport(Authentication authentication) {
        String userId = getUserIdFromAuthentication(authentication);

        Map<String, Object> report = dataProtectionService.getDataProcessingReport(userId);

        auditLogger.logUserAction(userId, null, AuditLogger.AuditEventType.GDPR_REQUEST,
                "PROCESSING_REPORT_ACCESS", "gdpr_processing_report", Map.of("reportGenerated", true));

        return ResponseEntity.ok(report);
    }

    @PutMapping("/consent/{category}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Update data processing consent",
               description = "Grant or revoke consent for specific data processing categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consent updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid consent category")
    })
    public ResponseEntity<Map<String, Object>> updateConsent(
            @Parameter(description = "Data category (PERSONAL_DATA, FINANCIAL_DATA, etc.)")
            @PathVariable String category,
            @Parameter(description = "Consent decision")
            @RequestParam boolean consent,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);

        try {
            DataProtectionService.DataCategory dataCategory =
                    DataProtectionService.DataCategory.valueOf(category.toUpperCase());

            boolean updated = dataProtectionService.updateUserConsent(userId, dataCategory, consent);

            Map<String, Object> response = Map.of(
                "success", updated,
                "category", category,
                "consent", consent,
                "timestamp", java.time.LocalDateTime.now().toString()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid data category: " + category));
        }
    }

    @GetMapping("/consent/{category}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Check consent status",
               description = "Check current consent status for a data category")
    public ResponseEntity<Map<String, Object>> checkConsent(
            @Parameter(description = "Data category to check")
            @PathVariable String category,
            Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);

        try {
            DataProtectionService.DataCategory dataCategory =
                    DataProtectionService.DataCategory.valueOf(category.toUpperCase());

            boolean hasConsent = dataProtectionService.hasUserConsent(userId, dataCategory);

            return ResponseEntity.ok(Map.of(
                "category", category,
                "consent", hasConsent,
                "checkedAt", java.time.LocalDateTime.now().toString()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid data category: " + category));
        }
    }

    @GetMapping("/data-categories")
    @Operation(summary = "List available data categories",
               description = "Get list of all data categories for consent management")
    public ResponseEntity<Map<String, Object>> getDataCategories() {
        Map<String, Object> response = new HashMap<>();
        response.put("categories", DataProtectionService.DataCategory.values());
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        // Extract user ID from JWT token or authentication principal
        // This would depend on your JWT token structure
        if (authentication != null && authentication.getPrincipal() != null) {
            // Assuming the principal contains the user ID or username
            return authentication.getName();
        }
        throw new IllegalStateException("Unable to determine user ID from authentication");
    }
}