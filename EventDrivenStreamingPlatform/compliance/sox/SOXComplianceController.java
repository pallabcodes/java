package com.netflix.streaming.compliance.sox;

import com.netflix.streaming.events.EventPublisher;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * SOX Compliance Controller - Financial Controls & Audit Trails
 *
 * Implements SOX Section 302, 404, 409: Financial reporting controls,
 * internal controls assessment, and enhanced financial disclosures.
 *
 * Production-ready SOX compliance for financial data integrity.
 */
@RestController
@RequestMapping("/api/v1/sox")
public class SOXComplianceController {

    private static final Logger logger = LoggerFactory.getLogger(SOXComplianceController.class);

    private final SOXAuditTrailManager auditManager;
    private final SOXControlAssessmentService controlService;
    private final SOXFinancialReportingService reportingService;
    private final EventPublisher eventPublisher;
    private final Tracer tracer;

    public SOXComplianceController(SOXAuditTrailManager auditManager,
                                 SOXControlAssessmentService controlService,
                                 SOXFinancialReportingService reportingService,
                                 EventPublisher eventPublisher,
                                 Tracer tracer) {
        this.auditManager = auditManager;
        this.controlService = controlService;
        this.reportingService = reportingService;
        this.eventPublisher = eventPublisher;
        this.tracer = tracer;
    }

    /**
     * SOX Section 302: Financial Controls Certification
     * Management certification of financial controls effectiveness
     */
    @PostMapping("/controls/certify")
    public ResponseEntity<SOXResponse> certifyFinancialControls(
            @Valid @RequestBody SOXControlsCertificationRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("sox.controls.certification")
            .setAttribute("certifier.id", userId)
            .setAttribute("period.end", request.getPeriodEnd().toString())
            .setAttribute("sox.section", "302")
            .startSpan();

        try {
            // Validate certifier authority
            if (!controlService.hasCertificationAuthority(userId)) {
                span.setStatus(StatusCode.ERROR, "Insufficient certification authority");
                return ResponseEntity.forbidden().build();
            }

            // Perform certification
            SOXCertificationResult result = controlService.certifyControls(
                request, userId, correlationId);

            // Log SOX audit event
            auditManager.logSOXEvent(userId, "CONTROLS_CERTIFICATION",
                                   Map.of("certificationId", result.getCertificationId(),
                                         "periodEnd", request.getPeriodEnd().toString()),
                                   correlationId);

            // Publish compliance event
            eventPublisher.publish(new SOXControlsCertifiedEvent(
                correlationId, "default", result.getCertificationId(),
                "SECTION_302", userId, request.getPeriodEnd(), Instant.now()
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new SOXResponse("Financial controls certified successfully"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("SOX controls certification failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    /**
     * SOX Section 404: Internal Controls Assessment
     * Annual assessment of internal controls effectiveness
     */
    @PostMapping("/controls/assessment")
    public ResponseEntity<SOXAssessmentResponse> performControlsAssessment(
            @Valid @RequestBody SOXControlsAssessmentRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("sox.controls.assessment")
            .setAttribute("assessor.id", userId)
            .setAttribute("fiscal.year", request.getFiscalYear())
            .setAttribute("sox.section", "404")
            .startSpan();

        try {
            // Perform comprehensive controls assessment
            SOXAssessmentResult result = controlService.performAssessment(request, userId, correlationId);

            // Log SOX audit event
            auditManager.logSOXEvent(userId, "CONTROLS_ASSESSMENT",
                                   Map.of("assessmentId", result.getAssessmentId(),
                                         "fiscalYear", request.getFiscalYear().toString()),
                                   correlationId);

            // Publish compliance event
            eventPublisher.publish(new SOXControlsAssessedEvent(
                correlationId, "default", result.getAssessmentId(),
                "SECTION_404", userId, request.getFiscalYear(), result.isEffective(), Instant.now()
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new SOXAssessmentResponse(result, "Controls assessment completed"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("SOX controls assessment failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    /**
     * SOX Financial Reporting Controls
     * Validate financial data integrity
     */
    @PostMapping("/financial/validate")
    public ResponseEntity<SOXValidationResponse> validateFinancialData(
            @Valid @RequestBody SOXFinancialValidationRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("sox.financial.validation")
            .setAttribute("validator.id", userId)
            .setAttribute("data.type", request.getDataType())
            .startSpan();

        try {
            // Perform financial data validation
            SOXValidationResult result = reportingService.validateFinancialData(
                request, userId, correlationId);

            // Log SOX audit event
            auditManager.logSOXEvent(userId, "FINANCIAL_VALIDATION",
                                   Map.of("validationId", result.getValidationId(),
                                         "dataType", request.getDataType()),
                                   correlationId);

            // Publish compliance event
            eventPublisher.publish(new SOXFinancialDataValidatedEvent(
                correlationId, "default", result.getValidationId(),
                userId, request.getDataType(), result.isValid(), result.getIssues(), Instant.now()
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new SOXValidationResponse(result, "Financial data validation completed"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("SOX financial validation failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    /**
     * SOX Audit Trail Query
     * Access to financial audit trails for compliance reviews
     */
    @GetMapping("/audit/{entityId}")
    public ResponseEntity<SOXAuditResponse> getAuditTrail(
            @PathVariable String entityId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("sox.audit.trail")
            .setAttribute("entity.id", entityId)
            .setAttribute("user.id", userId)
            .setAttribute("date.range", startDate + " to " + endDate)
            .startSpan();

        try {
            // Verify access permissions
            if (!auditManager.hasAuditAccess(userId)) {
                span.setStatus(StatusCode.ERROR, "Insufficient audit access permissions");
                return ResponseEntity.forbidden().build();
            }

            // Retrieve audit trail
            SOXAuditTrail auditTrail = auditManager.getAuditTrail(
                entityId, startDate, endDate, correlationId);

            // Log SOX audit access
            auditManager.logSOXEvent(userId, "AUDIT_TRAIL_ACCESS",
                                   Map.of("entityId", entityId,
                                         "startDate", startDate.toString(),
                                         "endDate", endDate.toString()),
                                   correlationId);

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new SOXAuditResponse(auditTrail, "Audit trail retrieved successfully"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("SOX audit trail access failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    /**
     * SOX Control Deficiency Reporting
     * Report identified control deficiencies
     */
    @PostMapping("/controls/deficiency")
    public ResponseEntity<SOXResponse> reportControlDeficiency(
            @Valid @RequestBody SOXControlDeficiencyRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("sox.control.deficiency")
            .setAttribute("reporter.id", userId)
            .setAttribute("severity", request.getSeverity())
            .startSpan();

        try {
            // Report control deficiency
            SOXDeficiencyReport report = controlService.reportDeficiency(
                request, userId, correlationId);

            // Log SOX audit event
            auditManager.logSOXEvent(userId, "CONTROL_DEFICIENCY_REPORTED",
                                   Map.of("deficiencyId", report.getDeficiencyId(),
                                         "severity", request.getSeverity()),
                                   correlationId);

            // Publish compliance event
            eventPublisher.publish(new SOXControlDeficiencyReportedEvent(
                correlationId, "default", report.getDeficiencyId(),
                userId, request.getControlId(), request.getSeverity(),
                request.getDescription(), Instant.now()
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new SOXResponse("Control deficiency reported successfully"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("SOX control deficiency reporting failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    /**
     * SOX Remediation Tracking
     * Track remediation of identified deficiencies
     */
    @PutMapping("/controls/deficiency/{deficiencyId}/remediate")
    public ResponseEntity<SOXResponse> remediateControlDeficiency(
            @PathVariable String deficiencyId,
            @Valid @RequestBody SOXRemediationRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("sox.deficiency.remediation")
            .setAttribute("deficiency.id", deficiencyId)
            .setAttribute("user.id", userId)
            .startSpan();

        try {
            // Update remediation status
            controlService.updateRemediationStatus(deficiencyId, request, userId, correlationId);

            // Log SOX audit event
            auditManager.logSOXEvent(userId, "DEFICIENCY_REMEDIATION",
                                   Map.of("deficiencyId", deficiencyId,
                                         "status", request.getStatus()),
                                   correlationId);

            // Publish compliance event
            eventPublisher.publish(new SOXDeficiencyRemediatedEvent(
                correlationId, "default", deficiencyId,
                userId, request.getStatus(), request.getRemediationDetails(), Instant.now()
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new SOXResponse("Deficiency remediation updated successfully"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("SOX deficiency remediation failed for deficiency: {}", deficiencyId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    // Request/Response DTOs

    public static class SOXControlsCertificationRequest {
        private LocalDate periodEnd;
        private String certificationStatement;
        private List<String> controlAreas;

        public LocalDate getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
        public String getCertificationStatement() { return certificationStatement; }
        public void setCertificationStatement(String certificationStatement) { this.certificationStatement = certificationStatement; }
        public List<String> getControlAreas() { return controlAreas; }
        public void setControlAreas(List<String> controlAreas) { this.controlAreas = controlAreas; }
    }

    public static class SOXControlsAssessmentRequest {
        private int fiscalYear;
        private String assessmentScope;
        private List<String> controlObjectives;

        public int getFiscalYear() { return fiscalYear; }
        public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }
        public String getAssessmentScope() { return assessmentScope; }
        public void setAssessmentScope(String assessmentScope) { this.assessmentScope = assessmentScope; }
        public List<String> getControlObjectives() { return controlObjectives; }
        public void setControlObjectives(List<String> controlObjectives) { this.controlObjectives = controlObjectives; }
    }

    public static class SOXFinancialValidationRequest {
        private String dataType;
        private LocalDate reportingPeriod;
        private List<String> validationRules;

        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        public LocalDate getReportingPeriod() { return reportingPeriod; }
        public void setReportingPeriod(LocalDate reportingPeriod) { this.reportingPeriod = reportingPeriod; }
        public List<String> getValidationRules() { return validationRules; }
        public void setValidationRules(List<String> validationRules) { this.validationRules = validationRules; }
    }

    public static class SOXControlDeficiencyRequest {
        private String controlId;
        private String severity;
        private String description;
        private LocalDate identifiedDate;

        public String getControlId() { return controlId; }
        public void setControlId(String controlId) { this.controlId = controlId; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDate getIdentifiedDate() { return identifiedDate; }
        public void setIdentifiedDate(LocalDate identifiedDate) { this.identifiedDate = identifiedDate; }
    }

    public static class SOXRemediationRequest {
        private String status;
        private String remediationDetails;
        private LocalDate completionDate;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRemediationDetails() { return remediationDetails; }
        public void setRemediationDetails(String remediationDetails) { this.remediationDetails = remediationDetails; }
        public LocalDate getCompletionDate() { return completionDate; }
        public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }
    }

    public static class SOXResponse {
        public final String message;
        public final Instant timestamp;

        public SOXResponse(String message) {
            this.message = message;
            this.timestamp = Instant.now();
        }
    }

    public static class SOXAssessmentResponse {
        public final SOXAssessmentResult result;
        public final String message;
        public final Instant timestamp;

        public SOXAssessmentResponse(SOXAssessmentResult result, String message) {
            this.result = result;
            this.message = message;
            this.timestamp = Instant.now();
        }
    }

    public static class SOXValidationResponse {
        public final SOXValidationResult result;
        public final String message;
        public final Instant timestamp;

        public SOXValidationResponse(SOXValidationResult result, String message) {
            this.result = result;
            this.message = message;
            this.timestamp = Instant.now();
        }
    }

    public static class SOXAuditResponse {
        public final SOXAuditTrail auditTrail;
        public final String message;
        public final Instant timestamp;

        public SOXAuditResponse(SOXAuditTrail auditTrail, String message) {
            this.auditTrail = auditTrail;
            this.message = message;
            this.timestamp = Instant.now();
        }
    }
}