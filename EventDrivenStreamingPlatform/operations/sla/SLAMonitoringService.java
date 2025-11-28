package com.netflix.streaming.operations.sla;

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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service Level Agreement (SLA) Monitoring Service
 *
 * Monitors and enforces Netflix-grade SLA commitments:
 * - Availability SLAs (99.9%, 99.5%, etc.)
 * - Performance SLAs (response times, throughput)
 * - Data quality SLAs (accuracy, completeness)
 * - Incident response SLAs (MTTR, escalation times)
 * - Compliance reporting and enforcement
 */
@Service
public class SLAMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(SLAMonitoringService.class);

    private final EventPublisher eventPublisher;
    private final Tracer tracer;

    // SLA Configuration
    @Value("${sla.availability.target:99.9}")
    private double availabilityTarget;

    @Value("${sla.performance.p95-target:200}")
    private long performanceP95TargetMs;

    @Value("${sla.incident-response.target:15}")
    private long incidentResponseTargetMinutes;

    @Value("${sla.monitoring-window-days:30}")
    private int monitoringWindowDays;

    // SLA Tracking
    private final Map<String, SLAMetrics> serviceSLAs = new ConcurrentHashMap<>();
    private final Map<String, SLAIncident> slaIncidents = new ConcurrentHashMap<>();

    public SLAMonitoringService(EventPublisher eventPublisher, Tracer tracer) {
        this.eventPublisher = eventPublisher;
        this.tracer = tracer;

        initializeServiceSLAs();
    }

    /**
     * Scheduled SLA compliance monitoring
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    public void monitorSLACompliance() {
        Span span = tracer.spanBuilder("sla.compliance.monitoring").startSpan();

        try {
            logger.info("Monitoring SLA compliance across all services");

            Map<String, SLAViolation> violations = new HashMap<>();

            for (Map.Entry<String, SLAMetrics> entry : serviceSLAs.entrySet()) {
                String serviceName = entry.getKey();
                SLAMetrics metrics = entry.getValue();

                // Check availability SLA
                double actualAvailability = calculateActualAvailability(serviceName);
                if (actualAvailability < metrics.getAvailabilityTarget()) {
                    violations.put(serviceName + "_availability",
                        createAvailabilityViolation(serviceName, metrics.getAvailabilityTarget(), actualAvailability));
                }

                // Check performance SLA
                long actualP95Latency = calculateActualP95Latency(serviceName);
                if (actualP95Latency > metrics.getPerformanceP95TargetMs()) {
                    violations.put(serviceName + "_performance",
                        createPerformanceViolation(serviceName, metrics.getPerformanceP95TargetMs(), actualP95Latency));
                }

                // Check incident response SLA
                double actualMTTR = calculateActualMTTR(serviceName);
                if (actualMTTR > metrics.getIncidentResponseTargetMinutes()) {
                    violations.put(serviceName + "_incident_response",
                        createIncidentResponseViolation(serviceName, metrics.getIncidentResponseTargetMinutes(), actualMTTR));
                }
            }

            // Report violations
            if (!violations.isEmpty()) {
                handleSLAViolations(violations);
            }

            span.setAttribute("violations.detected", violations.size());
            span.setAttribute("services.monitored", serviceSLAs.size());

            logger.info("SLA monitoring completed: {} violations detected across {} services",
                       violations.size(), serviceSLAs.size());

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("SLA monitoring failed", e);
        } finally {
            span.end();
        }
    }

    /**
     * Generate monthly SLA report
     */
    @Scheduled(cron = "0 0 1 * *") // First day of each month
    public void generateMonthlySLAReport() {
        Span span = tracer.spanBuilder("sla.monthly.report").startSpan();

        try {
            logger.info("Generating monthly SLA report");

            LocalDate reportMonth = LocalDate.now().minusMonths(1);
            SLAReport report = generateSLAReport(reportMonth);

            // Publish SLA report event
            eventPublisher.publish(new SLAMonthlyReportGeneratedEvent(
                "sla-report-" + reportMonth.toString(), "default",
                reportMonth.toString(), report.getOverallCompliancePercentage(),
                report.getViolationsCount(), Instant.now()
            ));

            // Distribute report
            distributeSLAReport(report);

            span.setAttribute("report.month", reportMonth.toString());
            span.setAttribute("overall.compliance", report.getOverallCompliancePercentage());
            span.setAttribute("violations", report.getViolationsCount());

            logger.info("Monthly SLA report generated for {}: {:.1f}% compliance, {} violations",
                       reportMonth, report.getOverallCompliancePercentage(), report.getViolationsCount());

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Monthly SLA report generation failed", e);
        } finally {
            span.end();
        }
    }

    /**
     * Track incident response times for SLA compliance
     */
    public void recordIncidentResponse(String incidentId, String serviceName,
                                     long detectionToResponseMinutes, long resolutionTimeMinutes) {

        SLAIncident incident = SLAIncident.builder()
            .incidentId(incidentId)
            .serviceName(serviceName)
            .detectionToResponseMinutes(detectionToResponseMinutes)
            .resolutionTimeMinutes(resolutionTimeMinutes)
            .recordedAt(Instant.now())
            .build();

        slaIncidents.put(incidentId, incident);

        // Check if this violates SLA
        SLAMetrics slaMetrics = serviceSLAs.get(serviceName);
        if (slaMetrics != null && detectionToResponseMinutes > slaMetrics.getIncidentResponseTargetMinutes()) {
            eventPublisher.publish(new SLAIncidentResponseViolationEvent(
                "sla-violation-" + incidentId, "default",
                incidentId, serviceName, slaMetrics.getIncidentResponseTargetMinutes(),
                detectionToResponseMinutes, Instant.now()
            ));

            logger.warn("SLA violation: Incident {} response time {}min exceeds target {}min",
                       incidentId, detectionToResponseMinutes, slaMetrics.getIncidentResponseTargetMinutes());
        }
    }

    /**
     * Calculate actual SLA metrics for reporting
     */
    public SLAMetrics calculateActualSLAMetrics(String serviceName, LocalDate startDate, LocalDate endDate) {
        // Calculate actual metrics from monitoring data
        // In production, this would query metrics databases

        double actualAvailability = calculateActualAvailability(serviceName);
        long actualP95Latency = calculateActualP95Latency(serviceName);
        double actualMTTR = calculateActualMTTR(serviceName);

        return SLAMetrics.builder()
            .serviceName(serviceName)
            .availabilityTarget(availabilityTarget)
            .actualAvailability(actualAvailability)
            .performanceP95TargetMs(performanceP95TargetMs)
            .actualP95LatencyMs(actualP95Latency)
            .incidentResponseTargetMinutes(incidentResponseTargetMinutes)
            .actualMTTRMinutes(actualMTTR)
            .measuredFrom(startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))
            .measuredTo(endDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))
            .build();
    }

    /**
     * Get current SLA status for dashboard
     */
    public Map<String, SLADashboardStatus> getSLADashboardStatus() {
        Map<String, SLADashboardStatus> dashboard = new HashMap<>();

        for (Map.Entry<String, SLAMetrics> entry : serviceSLAs.entrySet()) {
            String serviceName = entry.getKey();
            SLAMetrics sla = entry.getValue();

            double currentAvailability = calculateActualAvailability(serviceName);
            long currentP95Latency = calculateActualP95Latency(serviceName);

            SLADashboardStatus status = SLADashboardStatus.builder()
                .serviceName(serviceName)
                .availabilityTarget(sla.getAvailabilityTarget())
                .currentAvailability(currentAvailability)
                .performanceTargetMs(sla.getPerformanceP95TargetMs())
                .currentP95LatencyMs(currentP95Latency)
                .incidentResponseTargetMinutes(sla.getIncidentResponseTargetMinutes())
                .isAvailabilityViolation(currentAvailability < sla.getAvailabilityTarget())
                .isPerformanceViolation(currentP95Latency > sla.getPerformanceP95TargetMs())
                .lastUpdated(Instant.now())
                .build();

            dashboard.put(serviceName, status);
        }

        return dashboard;
    }

    // Implementation methods

    private void initializeServiceSLAs() {
        // Initialize SLA targets for each service
        serviceSLAs.put("playback-service", SLAMetrics.builder()
            .serviceName("playback-service")
            .availabilityTarget(99.9) // P1 service
            .performanceP95TargetMs(200)
            .incidentResponseTargetMinutes(15)
            .build());

        serviceSLAs.put("analytics-service", SLAMetrics.builder()
            .serviceName("analytics-service")
            .availabilityTarget(99.5) // P2 service
            .performanceP95TargetMs(500)
            .incidentResponseTargetMinutes(30)
            .build());

        serviceSLAs.put("ml-pipeline-service", SLAMetrics.builder()
            .serviceName("ml-pipeline-service")
            .availabilityTarget(99.0) // Best effort
            .performanceP95TargetMs(2000)
            .incidentResponseTargetMinutes(120)
            .build());
    }

    private double calculateActualAvailability(String serviceName) {
        // In production: Calculate from uptime metrics over monitoring window
        // For demo: Return simulated availability
        double baseAvailability = 99.95;
        double randomVariation = (Math.random() - 0.5) * 0.1; // ±5% variation
        return Math.max(95.0, Math.min(100.0, baseAvailability + randomVariation));
    }

    private long calculateActualP95Latency(String serviceName) {
        // In production: Query P95 latency from metrics database
        // For demo: Return simulated latency
        long baseLatency = serviceName.contains("analytics") ? 300 : 150;
        long randomVariation = (long) ((Math.random() - 0.5) * 100); // ±50ms variation
        return Math.max(50, baseLatency + randomVariation);
    }

    private double calculateActualMTTR(String serviceName) {
        // Calculate Mean Time To Resolution from incident data
        // In production: Query from incident tracking system
        List<SLAIncident> serviceIncidents = slaIncidents.values().stream()
            .filter(incident -> serviceName.equals(incident.getServiceName()))
            .filter(incident -> incident.getRecordedAt().isAfter(
                Instant.now().minus(monitoringWindowDays, ChronoUnit.DAYS)))
            .toList();

        if (serviceIncidents.isEmpty()) {
            return 0.0; // No incidents
        }

        double totalResolutionTime = serviceIncidents.stream()
            .mapToDouble(SLAIncident::getResolutionTimeMinutes)
            .sum();

        return totalResolutionTime / serviceIncidents.size();
    }

    private SLAViolation createAvailabilityViolation(String serviceName, double target, double actual) {
        return SLAViolation.builder()
            .serviceName(serviceName)
            .violationType("AVAILABILITY")
            .targetValue(target)
            .actualValue(actual)
            .thresholdExceeded(target - actual)
            .timeWindow(monitoringWindowDays + " days")
            .severity(actual < 99.0 ? "CRITICAL" : "WARNING")
            .detectedAt(Instant.now())
            .build();
    }

    private SLAViolation createPerformanceViolation(String serviceName, long target, long actual) {
        return SLAViolation.builder()
            .serviceName(serviceName)
            .violationType("PERFORMANCE")
            .targetValue(target)
            .actualValue(actual)
            .thresholdExceeded(actual - target)
            .timeWindow("1 hour rolling")
            .severity(actual > target * 2 ? "CRITICAL" : "WARNING")
            .detectedAt(Instant.now())
            .build();
    }

    private SLAViolation createIncidentResponseViolation(String serviceName, long target, double actual) {
        return SLAViolation.builder()
            .serviceName(serviceName)
            .violationType("INCIDENT_RESPONSE")
            .targetValue(target)
            .actualValue(actual)
            .thresholdExceeded(actual - target)
            .timeWindow(monitoringWindowDays + " days")
            .severity(actual > target * 2 ? "CRITICAL" : "WARNING")
            .detectedAt(Instant.now())
            .build();
    }

    private void handleSLAViolations(Map<String, SLAViolation> violations) {
        for (SLAViolation violation : violations.values()) {
            // Create alert for SLA violation
            eventPublisher.publish(new SLAViolationAlertEvent(
                "sla-violation-" + violation.getServiceName() + "-" + Instant.now().toEpochMilli(),
                "default", violation.getServiceName(), violation.getViolationType(),
                violation.getTargetValue(), violation.getActualValue(),
                violation.getSeverity(), Instant.now()
            ));

            logger.warn("SLA VIOLATION: {} - {} target: {} actual: {} severity: {}",
                       violation.getServiceName(), violation.getViolationType(),
                       violation.getTargetValue(), violation.getActualValue(), violation.getSeverity());
        }
    }

    private SLAReport generateSLAReport(LocalDate reportMonth) {
        // Generate comprehensive SLA report for the month
        Map<String, SLAMetrics> monthlyMetrics = new HashMap<>();

        for (String serviceName : serviceSLAs.keySet()) {
            LocalDate monthStart = reportMonth.withDayOfMonth(1);
            LocalDate monthEnd = reportMonth.withDayOfMonth(reportMonth.lengthOfMonth());

            SLAMetrics metrics = calculateActualSLAMetrics(serviceName, monthStart, monthEnd);
            monthlyMetrics.put(serviceName, metrics);
        }

        // Calculate overall compliance
        double totalCompliancePoints = 0;
        int totalMeasurements = 0;

        for (SLAMetrics metrics : monthlyMetrics.values()) {
            // Availability compliance (weighted)
            if (metrics.getActualAvailability() >= metrics.getAvailabilityTarget()) {
                totalCompliancePoints += 40; // 40% weight
            }
            totalMeasurements += 40;

            // Performance compliance (weighted)
            if (metrics.getActualP95LatencyMs() <= metrics.getPerformanceP95TargetMs()) {
                totalCompliancePoints += 35; // 35% weight
            }
            totalMeasurements += 35;

            // Incident response compliance (weighted)
            if (metrics.getActualMTTRMinutes() <= metrics.getIncidentResponseTargetMinutes()) {
                totalCompliancePoints += 25; // 25% weight
            }
            totalMeasurements += 25;
        }

        double overallCompliance = totalMeasurements > 0 ?
            (totalCompliancePoints / totalMeasurements) * 100 : 100.0;

        return SLAReport.builder()
            .reportMonth(reportMonth)
            .serviceMetrics(monthlyMetrics)
            .overallCompliancePercentage(overallCompliance)
            .violationsCount(0) // Would be calculated from actual violations
            .generatedAt(Instant.now())
            .build();
    }

    private void distributeSLAReport(SLAReport report) {
        // In production: Send via email, upload to reporting systems, etc.
        logger.info("SLA report for {} distributed: {:.1f}% compliance",
                   report.getReportMonth(), report.getOverallCompliancePercentage());
    }

    // Data classes

    @lombok.Data
    @lombok.Builder
    public static class SLAMetrics {
        private String serviceName;
        private double availabilityTarget;
        private double actualAvailability;
        private long performanceP95TargetMs;
        private long actualP95LatencyMs;
        private long incidentResponseTargetMinutes;
        private double actualMTTRMinutes;
        private Instant measuredFrom;
        private Instant measuredTo;
    }

    @lombok.Data
    @lombok.Builder
    public static class SLAViolation {
        private String serviceName;
        private String violationType;
        private double targetValue;
        private double actualValue;
        private double thresholdExceeded;
        private String timeWindow;
        private String severity;
        private Instant detectedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class SLAIncident {
        private String incidentId;
        private String serviceName;
        private long detectionToResponseMinutes;
        private long resolutionTimeMinutes;
        private Instant recordedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class SLAReport {
        private LocalDate reportMonth;
        private Map<String, SLAMetrics> serviceMetrics;
        private double overallCompliancePercentage;
        private int violationsCount;
        private Instant generatedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class SLADashboardStatus {
        private String serviceName;
        private double availabilityTarget;
        private double currentAvailability;
        private long performanceTargetMs;
        private long currentP95LatencyMs;
        private long incidentResponseTargetMinutes;
        private boolean isAvailabilityViolation;
        private boolean isPerformanceViolation;
        private Instant lastUpdated;
    }
}