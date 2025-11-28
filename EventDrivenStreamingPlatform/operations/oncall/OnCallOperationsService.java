package com.netflix.streaming.operations.oncall;

import com.netflix.streaming.alerting.AlertManager;
import com.netflix.streaming.disasterrecovery.runbooks.IncidentResponseRunbook;
import com.netflix.streaming.events.EventPublisher;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netflix-Grade On-Call Operations Service
 *
 * Manages production operations with:
 * - On-call rotation and escalation policies
 * - Incident detection and automated response
 * - Service level objective (SLO) monitoring
 * - Post-mortem automation and learning
 * - Operational excellence metrics
 */
@Service
public class OnCallOperationsService {

    private static final Logger logger = LoggerFactory.getLogger(OnCallOperationsService.class);

    private final AlertManager alertManager;
    private final IncidentResponseRunbook incidentRunbook;
    private final EventPublisher eventPublisher;
    private final Tracer tracer;

    // On-call configuration
    @Value("${oncall.primary-engineer:engineer@company.com}")
    private String primaryOnCallEngineer;

    @Value("${oncall.secondary-engineer:secondary@company.com}")
    private String secondaryOnCallEngineer;

    @Value("${oncall.manager:manager@company.com}")
    private String onCallManager;

    @Value("${oncall.escalation-timeout-minutes:15}")
    private int escalationTimeoutMinutes;

    @Value("${oncall.p1-slo-target:99.9}")
    private double p1ServiceLevelObjective;

    @Value("${oncall.p2-slo-target:99.5}")
    private double p2ServiceLevelObjective;

    // Operational state
    private final Map<String, OnCallIncident> activeIncidents = new ConcurrentHashMap<>();
    private final Map<String, SLOStatus> serviceLevelObjectives = new ConcurrentHashMap<>();

    public OnCallOperationsService(AlertManager alertManager,
                                 IncidentResponseRunbook incidentRunbook,
                                 EventPublisher eventPublisher,
                                 Tracer tracer) {
        this.alertManager = alertManager;
        this.incidentRunbook = incidentRunbook;
        this.eventPublisher = eventPublisher;
        this.tracer = tracer;

        // Initialize SLO tracking
        initializeSLOTracking();
    }

    /**
     * Handle incoming alerts and trigger incident response
     */
    public void handleAlert(String alertId, String severity, String message,
                           Map<String, Object> context) {

        Span span = tracer.spanBuilder("oncall.alert.handling")
            .setAttribute("alert.id", alertId)
            .setAttribute("severity", severity)
            .startSpan();

        try {
            logger.warn("ALERT RECEIVED: {} - {} - {}", alertId, severity, message);

            // Create incident record
            OnCallIncident incident = OnCallIncident.builder()
                .incidentId(alertId)
                .severity(severity)
                .description(message)
                .context(context)
                .detectedAt(Instant.now())
                .status("INVESTIGATING")
                .build();

            // Store active incident
            activeIncidents.put(alertId, incident);

            // Notify on-call engineer
            notifyOnCallEngineer(incident);

            // Start escalation timer
            scheduleEscalationCheck(alertId);

            // Execute automated incident response
            IncidentResponseRunbook.IncidentResponse response =
                executeIncidentResponse(alertId, severity, message, context);

            // Update incident status
            incident.setResponse(response);
            incident.setStatus(response.isSuccessful() ? "MITIGATED" : "ESCALATED");

            // Publish incident event
            eventPublisher.publish(new OnCallIncidentHandledEvent(
                "incident-" + alertId, "default",
                alertId, severity, response.isSuccessful(),
                response.getDurationMs(), Instant.now()
            ));

            span.setAttribute("incident.handled", response.isSuccessful());
            span.setAttribute("response.time.ms", response.getDurationMs());

            logger.info("Incident {} handled in {}ms: {}",
                       alertId, response.getDurationMs(),
                       response.isSuccessful() ? "RESOLVED" : "ESCALATED");

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Alert handling failed for: {}", alertId, e);

            // Emergency escalation
            emergencyEscalation(alertId, severity, e.getMessage());
        } finally {
            span.end();
        }
    }

    /**
     * Scheduled SLO monitoring and alerting
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void monitorServiceLevelObjectives() {
        Span span = tracer.spanBuilder("oncall.slo.monitoring").startSpan();

        try {
            logger.debug("Monitoring service level objectives");

            // Check SLO compliance for each service
            Map<String, SLOViolation> violations = checkSLOViolations();

            if (!violations.isEmpty()) {
                for (Map.Entry<String, SLOViolation> entry : violations.entrySet()) {
                    String serviceName = entry.getKey();
                    SLOViolation violation = entry.getValue();

                    // Create SLO violation alert
                    alertManager.processBusinessAlert(
                        "SLO_VIOLATION_" + serviceName,
                        String.format("SLO violation detected: %.2f%% (target: %.2f%%)",
                                    violation.getActualValue(), violation.getTargetValue()),
                        Map.of(
                            "serviceName", serviceName,
                            "actualValue", violation.getActualValue(),
                            "targetValue", violation.getTargetValue(),
                            "timeWindow", violation.getTimeWindow()
                        )
                    );

                    // Publish SLO event
                    eventPublisher.publish(new SLOViolationDetectedEvent(
                        "slo-violation-" + serviceName + "-" + Instant.now().toEpochMilli(),
                        "default", serviceName, violation.getActualValue(),
                        violation.getTargetValue(), Instant.now()
                    ));

                    logger.warn("SLO violation detected for {}: {:.2f}% (target: {:.2f}%)",
                               serviceName, violation.getActualValue(), violation.getTargetValue());
                }
            }

            span.setAttribute("violations.detected", violations.size());

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("SLO monitoring failed", e);
        } finally {
            span.end();
        }
    }

    /**
     * Generate operational status report
     */
    @Scheduled(cron = "0 0 9 * * MON") // Weekly on Monday at 9 AM
    public void generateOperationalStatusReport() {
        Span span = tracer.spanBuilder("oncall.status.report").startSpan();

        try {
            logger.info("Generating weekly operational status report");

            // Collect operational metrics
            OperationalMetrics metrics = collectOperationalMetrics();

            // Generate report
            OperationalStatusReport report = OperationalStatusReport.builder()
                .reportId("ops-report-" + Instant.now().toEpochMilli())
                .timeRange("Last 7 days")
                .generatedAt(Instant.now())
                .metrics(metrics)
                .build();

            // Publish report event
            eventPublisher.publish(new OperationalStatusReportGeneratedEvent(
                "ops-report-" + report.getReportId(), "default",
                report.getReportId(), report.getTimeRange(), Instant.now()
            ));

            // Send report to stakeholders (in production: email, Slack, etc.)
            distributeOperationalReport(report);

            span.setAttribute("incidents.handled", metrics.getIncidentsHandled());
            span.setAttribute("slo.compliance", metrics.getSloCompliancePercentage());

            logger.info("Operational status report generated: {} incidents handled, {:.1f}% SLO compliance",
                       metrics.getIncidentsHandled(), metrics.getSloCompliancePercentage());

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Operational status report generation failed", e);
        } finally {
            span.end();
        }
    }

    /**
     * Execute post-mortem analysis for resolved incidents
     */
    public PostMortemAnalysis performPostMortem(String incidentId) {
        Span span = tracer.spanBuilder("oncall.postmortem")
            .setAttribute("incident.id", incidentId)
            .startSpan();

        try {
            logger.info("Performing post-mortem analysis for incident: {}", incidentId);

            // Retrieve incident data
            OnCallIncident incident = activeIncidents.get(incidentId);
            if (incident == null) {
                throw new IllegalArgumentException("Incident not found: " + incidentId);
            }

            // Analyze incident timeline
            IncidentTimeline timeline = analyzeIncidentTimeline(incident);

            // Identify root causes
            List<String> rootCauses = identifyRootCauses(incident);

            // Generate improvement recommendations
            List<String> recommendations = generateImprovementRecommendations(incident, rootCauses);

            // Create post-mortem report
            PostMortemAnalysis analysis = PostMortemAnalysis.builder()
                .incidentId(incidentId)
                .analysisId("pm-" + incidentId)
                .timeline(timeline)
                .rootCauses(rootCauses)
                .recommendations(recommendations)
                .severity(incident.getSeverity())
                .durationMs(incident.getResponse() != null ? incident.getResponse().getDurationMs() : 0L)
                .analyzedAt(Instant.now())
                .build();

            // Publish post-mortem event
            eventPublisher.publish(new PostMortemCompletedEvent(
                "postmortem-" + analysis.getAnalysisId(), "default",
                incidentId, analysis.getAnalysisId(), rootCauses.size(),
                recommendations.size(), Instant.now()
            ));

            span.setAttribute("root.causes.identified", rootCauses.size());
            span.setAttribute("recommendations.generated", recommendations.size());

            logger.info("Post-mortem analysis completed for incident {}: {} root causes, {} recommendations",
                       incidentId, rootCauses.size(), recommendations.size());

            return analysis;

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Post-mortem analysis failed for incident: {}", incidentId, e);
            throw new RuntimeException("Post-mortem analysis failed", e);
        } finally {
            span.end();
        }
    }

    // Implementation methods

    private void notifyOnCallEngineer(OnCallIncident incident) {
        // In production: Send PagerDuty alert, SMS, Slack notification
        logger.error("🚨 ON-CALL ALERT: {} - {} - Notifying: {}",
                    incident.getIncidentId(), incident.getSeverity(), primaryOnCallEngineer);

        eventPublisher.publish(new OnCallEngineerNotifiedEvent(
            "notification-" + incident.getIncidentId(), "default",
            incident.getIncidentId(), primaryOnCallEngineer, "PAGE_DUTY", Instant.now()
        ));
    }

    private void scheduleEscalationCheck(String incidentId) {
        // Schedule escalation check after timeout
        // In production, this would use a scheduled task or timer
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkEscalationNeeded(incidentId);
            }
        }, escalationTimeoutMinutes * 60 * 1000L);
    }

    private void checkEscalationNeeded(String incidentId) {
        OnCallIncident incident = activeIncidents.get(incidentId);
        if (incident != null && !"MITIGATED".equals(incident.getStatus())) {
            logger.error("🚨 ESCALATION TRIGGERED for incident: {} - Escalating to: {}",
                        incidentId, onCallManager);

            // Escalate to manager
            eventPublisher.publish(new IncidentEscalatedEvent(
                "escalation-" + incidentId, "default",
                incidentId, secondaryOnCallEngineer, onCallManager, Instant.now()
            ));
        }
    }

    private IncidentResponseRunbook.IncidentResponse executeIncidentResponse(
            String alertId, String severity, String message, Map<String, Object> context) {

        // Create incident for runbook
        IncidentResponseRunbook.Incident incident = new IncidentResponseRunbook.Incident(
            alertId, message, "unknown-service" // Would be determined from context
        );

        // Set incident properties based on severity
        configureIncidentProperties(incident, severity, context);

        // Execute runbook
        return incidentRunbook.handleIncident(incident);
    }

    private void configureIncidentProperties(IncidentResponseRunbook.Incident incident,
                                          String severity, Map<String, Object> context) {
        switch (severity.toUpperCase()) {
            case "CRITICAL", "P1" -> {
                incident.setSeverity(IncidentResponseRunbook.IncidentSeverity.P1);
                incident.setServiceDown(true);
            }
            case "HIGH", "P2" -> {
                incident.setSeverity(IncidentResponseRunbook.IncidentSeverity.P2);
                incident.setPartialDegradation(true);
            }
            case "WARNING", "P3" -> {
                incident.setSeverity(IncidentResponseRunbook.IncidentSeverity.P3);
                incident.setMonitoringAlert(true);
            }
        }

        // Extract additional context
        if (context.containsKey("affectedUsers")) {
            incident.setAffectedUsers(((Number) context.get("affectedUsers")).intValue());
        }
    }

    private void emergencyEscalation(String alertId, String severity, String errorMessage) {
        logger.error("🚨 EMERGENCY ESCALATION for alert: {} - Escalating to: {}",
                    alertId, onCallManager);

        eventPublisher.publish(new EmergencyEscalationEvent(
            "emergency-" + alertId, "default",
            alertId, severity, onCallManager, errorMessage, Instant.now()
        ));
    }

    private void initializeSLOTracking() {
        // Initialize SLO tracking for different services
        serviceLevelObjectives.put("playback-service",
            SLOStatus.builder().serviceName("playback-service").targetSLO(p1ServiceLevelObjective).build());
        serviceLevelObjectives.put("analytics-service",
            SLOStatus.builder().serviceName("analytics-service").targetSLO(p2ServiceLevelObjective).build());
        serviceLevelObjectives.put("ml-pipeline-service",
            SLOStatus.builder().serviceName("ml-pipeline-service").targetSLO(p2ServiceLevelObjective).build());
    }

    private Map<String, SLOViolation> checkSLOViolations() {
        Map<String, SLOViolation> violations = new HashMap<>();

        // In production, this would query actual metrics from monitoring systems
        // For demo, simulating SLO checks
        for (Map.Entry<String, SLOStatus> entry : serviceLevelObjectives.entrySet()) {
            String serviceName = entry.getKey();
            SLOStatus sloStatus = entry.getValue();

            // Simulate current SLO achievement (in production: query Prometheus/monitoring)
            double currentSLO = simulateCurrentSLO(serviceName);

            if (currentSLO < sloStatus.getTargetSLO()) {
                violations.put(serviceName, SLOViolation.builder()
                    .serviceName(serviceName)
                    .targetValue(sloStatus.getTargetSLO())
                    .actualValue(currentSLO)
                    .timeWindow("1h")
                    .build());
            }
        }

        return violations;
    }

    private double simulateCurrentSLO(String serviceName) {
        // Simulate SLO calculation (in production: calculate from actual metrics)
        // Return values that occasionally trigger violations for demo purposes
        double baseSLO = 99.95; // High availability baseline
        double randomVariation = (Math.random() - 0.5) * 0.1; // ±5% variation
        return Math.max(95.0, Math.min(100.0, baseSLO + randomVariation));
    }

    private OperationalMetrics collectOperationalMetrics() {
        // Collect operational metrics for the past week
        return OperationalMetrics.builder()
            .incidentsHandled(12)
            .averageResolutionTimeMinutes(45.0)
            .sloCompliancePercentage(98.5)
            .uptimePercentage(99.97)
            .falsePositiveAlerts(2)
            .automatedRemediations(8)
            .build();
    }

    private void distributeOperationalReport(OperationalStatusReport report) {
        // In production: Send via email, Slack, generate PDF reports, etc.
        logger.info("Operational status report distributed to stakeholders: {}", report.getReportId());
    }

    private IncidentTimeline analyzeIncidentTimeline(OnCallIncident incident) {
        // Analyze incident timeline (detection, response, resolution)
        return IncidentTimeline.builder()
            .incidentId(incident.getIncidentId())
            .detectedAt(incident.getDetectedAt())
            .respondedAt(incident.getDetectedAt().plusSeconds(120)) // Simulate 2min response
            .resolvedAt(incident.getDetectedAt().plusSeconds(1800)) // Simulate 30min resolution
            .build();
    }

    private List<String> identifyRootCauses(OnCallIncident incident) {
        // Analyze incident to identify root causes
        // In production: Use logs, metrics, and automated analysis
        return List.of(
            "High memory utilization on analytics-service pods",
            "Database connection pool exhaustion",
            "Inefficient query patterns in user analytics"
        );
    }

    private List<String> generateImprovementRecommendations(OnCallIncident incident,
                                                         List<String> rootCauses) {
        // Generate improvement recommendations based on root cause analysis
        List<String> recommendations = new ArrayList<>();

        for (String rootCause : rootCauses) {
            if (rootCause.contains("memory")) {
                recommendations.add("Implement horizontal pod autoscaling based on memory utilization");
                recommendations.add("Optimize data structures and reduce memory footprint");
            } else if (rootCause.contains("database")) {
                recommendations.add("Implement database connection pooling with circuit breaker");
                recommendations.add("Add database query performance monitoring");
            } else if (rootCause.contains("query")) {
                recommendations.add("Implement query result caching");
                recommendations.add("Add database indexes for frequently queried columns");
            }
        }

        return recommendations;
    }

    // Data classes

    @lombok.Data
    @lombok.Builder
    public static class OnCallIncident {
        private String incidentId;
        private String severity;
        private String description;
        private Map<String, Object> context;
        private Instant detectedAt;
        private String status;
        private IncidentResponseRunbook.IncidentResponse response;
    }

    @lombok.Data
    @lombok.Builder
    public static class SLOStatus {
        private String serviceName;
        private double targetSLO;
        private double currentSLO;
        private Instant lastChecked;
    }

    @lombok.Data
    @lombok.Builder
    public static class SLOViolation {
        private String serviceName;
        private double targetValue;
        private double actualValue;
        private String timeWindow;
    }

    @lombok.Data
    @lombok.Builder
    public static class OperationalMetrics {
        private int incidentsHandled;
        private double averageResolutionTimeMinutes;
        private double sloCompliancePercentage;
        private double uptimePercentage;
        private int falsePositiveAlerts;
        private int automatedRemediations;
    }

    @lombok.Data
    @lombok.Builder
    public static class OperationalStatusReport {
        private String reportId;
        private String timeRange;
        private Instant generatedAt;
        private OperationalMetrics metrics;
    }

    @lombok.Data
    @lombok.Builder
    public static class IncidentTimeline {
        private String incidentId;
        private Instant detectedAt;
        private Instant respondedAt;
        private Instant resolvedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class PostMortemAnalysis {
        private String incidentId;
        private String analysisId;
        private IncidentTimeline timeline;
        private List<String> rootCauses;
        private List<String> recommendations;
        private String severity;
        private long durationMs;
        private Instant analyzedAt;
    }
}