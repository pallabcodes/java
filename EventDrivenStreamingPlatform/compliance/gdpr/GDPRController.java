package com.netflix.streaming.compliance.gdpr;

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
import java.util.List;
import java.util.Map;

/**
 * GDPR Compliance Controller - Data Subject Rights Implementation
 *
 * Implements GDPR Article 15-22: Right to access, rectification, erasure,
 * restriction, portability, and objection.
 *
 * Production-ready GDPR compliance for Netflix-scale data handling.
 */
@RestController
@RequestMapping("/api/v1/gdpr")
public class GDPRController {

    private static final Logger logger = LoggerFactory.getLogger(GDPRController.class);

    private final GDPRDataProcessor dataProcessor;
    private final GDPRAuditLogger auditLogger;
    private final EventPublisher eventPublisher;
    private final Tracer tracer;

    public GDPRController(GDPRDataProcessor dataProcessor,
                         GDPRAuditLogger auditLogger,
                         EventPublisher eventPublisher,
                         Tracer tracer) {
        this.dataProcessor = dataProcessor;
        this.auditLogger = auditLogger;
        this.eventPublisher = eventPublisher;
        this.tracer = tracer;
    }

    /**
     * GDPR Article 15: Right of Access
     * Data subject can obtain confirmation and copy of their data
     */
    @GetMapping("/data/{userId}")
    public ResponseEntity<GDPRDataResponse> getUserData(
            @PathVariable String userId,
            @RequestHeader("X-Data-Subject-ID") String dataSubjectId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("gdpr.data.access")
            .setAttribute("user.id", userId)
            .setAttribute("gdpr.article", "15")
            .startSpan();

        try {
            // Verify data subject identity
            if (!userId.equals(dataSubjectId)) {
                span.setStatus(StatusCode.ERROR, "Data subject identity verification failed");
                return ResponseEntity.forbidden().build();
            }

            // Get all user data across services
            GDPRUserData userData = dataProcessor.getUserData(userId);

            // Log audit event
            auditLogger.logDataAccess(userId, "DATA_ACCESS", correlationId);

            // Publish GDPR compliance event
            eventPublisher.publish(new GDPRDataAccessedEvent(
                correlationId, "default", userId, "ARTICLE_15", Instant.now()
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new GDPRDataResponse(userData, "Data access completed"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("GDPR data access failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    /**
     * GDPR Article 16: Right to Rectification
     * Data subject can have their data corrected
     */
    @PutMapping("/data/{userId}")
    public ResponseEntity<GDPRResponse> rectifyUserData(
            @PathVariable String userId,
            @Valid @RequestBody GDPRDataRectificationRequest request,
            @RequestHeader("X-Data-Subject-ID") String dataSubjectId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("gdpr.data.rectification")
            .setAttribute("user.id", userId)
            .setAttribute("gdpr.article", "16")
            .startSpan();

        try {
            // Verify data subject identity
            if (!userId.equals(dataSubjectId)) {
                return ResponseEntity.forbidden().build();
            }

            // Process data rectification
            dataProcessor.rectifyUserData(userId, request.getCorrections());

            // Log audit event
            auditLogger.logDataModification(userId, "DATA_RECTIFICATION",
                                          request.getCorrections(), correlationId);

            // Publish compliance event
            eventPublisher.publish(new GDPRDataRectifiedEvent(
                correlationId, "default", userId, "ARTICLE_16",
                request.getCorrections(), Instant.now()
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new GDPRResponse("Data rectification completed"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("GDPR data rectification failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    /**
     * GDPR Article 17: Right to Erasure ("Right to be Forgotten")
     * Data subject can request complete data deletion
     */
    @DeleteMapping("/data/{userId}")
    public ResponseEntity<GDPRResponse> eraseUserData(
            @PathVariable String userId,
            @RequestParam(required = false) String reason,
            @RequestHeader("X-Data-Subject-ID") String dataSubjectId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("gdpr.data.erasure")
            .setAttribute("user.id", userId)
            .setAttribute("gdpr.article", "17")
            .setAttribute("erasure.reason", reason != null ? reason : "USER_REQUEST")
            .startSpan();

        try {
            // Verify data subject identity
            if (!userId.equals(dataSubjectId)) {
                return ResponseEntity.forbidden().build();
            }

            // Check for legal holds or ongoing disputes
            if (dataProcessor.hasLegalHold(userId)) {
                return ResponseEntity.unprocessableEntity()
                    .body(new GDPRResponse("Data erasure blocked due to legal hold"));
            }

            // Schedule data erasure (GDPR requires 30 days)
            String erasureId = dataProcessor.scheduleErasure(userId, reason, correlationId);

            // Log audit event
            auditLogger.logDataErasure(userId, reason != null ? reason : "USER_REQUEST",
                                     erasureId, correlationId);

            // Publish compliance event
            eventPublisher.publish(new GDPRDataErasureScheduledEvent(
                correlationId, "default", userId, "ARTICLE_17",
                erasureId, reason, Instant.now().plusSeconds(2592000L) // 30 days
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.accepted()
                .body(new GDPRResponse("Data erasure scheduled. Will be completed within 30 days."));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("GDPR data erasure failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    /**
     * GDPR Article 18: Right to Restriction of Processing
     * Data subject can limit how their data is processed
     */
    @PostMapping("/data/{userId}/restrict")
    public ResponseEntity<GDPRResponse> restrictDataProcessing(
            @PathVariable String userId,
            @Valid @RequestBody GDPRProcessingRestrictionRequest request,
            @RequestHeader("X-Data-Subject-ID") String dataSubjectId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("gdpr.processing.restriction")
            .setAttribute("user.id", userId)
            .setAttribute("gdpr.article", "18")
            .startSpan();

        try {
            if (!userId.equals(dataSubjectId)) {
                return ResponseEntity.forbidden().build();
            }

            // Apply processing restrictions
            dataProcessor.restrictDataProcessing(userId, request.getRestrictions());

            // Log audit event
            auditLogger.logProcessingRestriction(userId, request.getRestrictions(), correlationId);

            // Publish compliance event
            eventPublisher.publish(new GDPRProcessingRestrictedEvent(
                correlationId, "default", userId, "ARTICLE_18",
                request.getRestrictions(), Instant.now()
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new GDPRResponse("Data processing restrictions applied"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("GDPR processing restriction failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    /**
     * GDPR Article 20: Right to Data Portability
     * Data subject can receive their data in portable format
     */
    @GetMapping("/data/{userId}/export")
    public ResponseEntity<GDPRDataExportResponse> exportUserData(
            @PathVariable String userId,
            @RequestParam(defaultValue = "JSON") String format,
            @RequestHeader("X-Data-Subject-ID") String dataSubjectId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("gdpr.data.portability")
            .setAttribute("user.id", userId)
            .setAttribute("gdpr.article", "20")
            .setAttribute("export.format", format)
            .startSpan();

        try {
            if (!userId.equals(dataSubjectId)) {
                return ResponseEntity.forbidden().build();
            }

            // Generate data export
            GDPRDataExport export = dataProcessor.exportUserData(userId, format, correlationId);

            // Log audit event
            auditLogger.logDataExport(userId, format, export.getExportId(), correlationId);

            // Publish compliance event
            eventPublisher.publish(new GDPRDataExportedEvent(
                correlationId, "default", userId, "ARTICLE_20",
                export.getExportId(), format, Instant.now()
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new GDPRDataExportResponse(export, "Data export completed"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("GDPR data export failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    /**
     * GDPR Consent Management
     */
    @PostMapping("/consent/{userId}")
    public ResponseEntity<GDPRResponse> updateConsent(
            @PathVariable String userId,
            @Valid @RequestBody GDPRConsentRequest request,
            @RequestHeader("X-Data-Subject-ID") String dataSubjectId,
            @RequestHeader("X-Correlation-ID") String correlationId) {

        Span span = tracer.spanBuilder("gdpr.consent.update")
            .setAttribute("user.id", userId)
            .setAttribute("consent.type", request.getConsentType())
            .startSpan();

        try {
            if (!userId.equals(dataSubjectId)) {
                return ResponseEntity.forbidden().build();
            }

            // Update consent preferences
            dataProcessor.updateConsent(userId, request);

            // Log audit event
            auditLogger.logConsentUpdate(userId, request, correlationId);

            // Publish compliance event
            eventPublisher.publish(new GDPRConsentUpdatedEvent(
                correlationId, "default", userId, request.getConsentType(),
                request.isGranted(), Instant.now()
            ));

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok(new GDPRResponse("Consent preferences updated"));

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("GDPR consent update failed for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            span.end();
        }
    }

    // Request/Response DTOs

    public static class GDPRDataRectificationRequest {
        private Map<String, Object> corrections;
        private String reason;

        public Map<String, Object> getCorrections() { return corrections; }
        public void setCorrections(Map<String, Object> corrections) { this.corrections = corrections; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class GDPRProcessingRestrictionRequest {
        private List<String> restrictions;
        private String reason;

        public List<String> getRestrictions() { return restrictions; }
        public void setRestrictions(List<String> restrictions) { this.restrictions = restrictions; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class GDPRConsentRequest {
        private String consentType;
        private boolean granted;
        private String version;

        public String getConsentType() { return consentType; }
        public void setConsentType(String consentType) { this.consentType = consentType; }
        public boolean isGranted() { return granted; }
        public void setGranted(boolean granted) { this.granted = granted; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }

    public static class GDPRResponse {
        public final String message;
        public final Instant timestamp;

        public GDPRResponse(String message) {
            this.message = message;
            this.timestamp = Instant.now();
        }
    }

    public static class GDPRDataResponse {
        public final GDPRUserData data;
        public final String message;
        public final Instant timestamp;

        public GDPRDataResponse(GDPRUserData data, String message) {
            this.data = data;
            this.message = message;
            this.timestamp = Instant.now();
        }
    }

    public static class GDPRDataExportResponse {
        public final GDPRDataExport export;
        public final String message;
        public final Instant timestamp;

        public GDPRDataExportResponse(GDPRDataExport export, String message) {
            this.export = export;
            this.message = message;
            this.timestamp = Instant.now();
        }
    }
}