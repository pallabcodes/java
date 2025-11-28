package org.example.compliance;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogger.class);

    private final AuditEventRepository auditEventRepository;
    private final MeterRegistry meterRegistry;

    // Audit event counters
    private final Counter auditEventsLogged;
    private final Counter securityEvents;
    private final Counter dataAccessEvents;
    private final Counter complianceEvents;

    @Autowired
    public AuditLogger(AuditEventRepository auditEventRepository, MeterRegistry meterRegistry) {
        this.auditEventRepository = auditEventRepository;
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.auditEventsLogged = Counter.builder("producer_consumer.audit.events.logged")
                .description("Total number of audit events logged")
                .register(meterRegistry);

        this.securityEvents = Counter.builder("producer_consumer.audit.security_events")
                .description("Total number of security-related audit events")
                .register(meterRegistry);

        this.dataAccessEvents = Counter.builder("producer_consumer.audit.data_access_events")
                .description("Total number of data access audit events")
                .register(meterRegistry);

        this.complianceEvents = Counter.builder("producer_consumer.audit.compliance_events")
                .description("Total number of compliance-related audit events")
                .register(meterRegistry);
    }

    @Transactional
    public void logUserAction(String userId, String action, String resource, Map<String, Object> details, String ipAddress) {
        AuditEvent event = createAuditEvent(userId, AuditEventType.USER_ACTION, action, resource, details, ipAddress);
        auditEventRepository.save(event);

        auditEventsLogged.increment();
        logger.info("AUDIT: User {} performed {} on {}", userId, action, resource);
    }

    @Transactional
    public void logDataAccess(String userId, String operation, String tableName, String recordId, String ipAddress) {
        Map<String, Object> details = Map.of(
            "operation", operation,
            "tableName", tableName,
            "recordId", recordId,
            "dataClassification", determineDataClassification(tableName)
        );

        AuditEvent event = createAuditEvent(userId, AuditEventType.DATA_ACCESS, operation, tableName + "/" + recordId, details, ipAddress);
        auditEventRepository.save(event);

        dataAccessEvents.increment();
        auditEventsLogged.increment();

        logger.info("AUDIT: Data access by {} - {} on {}.{}", userId, operation, tableName, recordId);
    }

    @Transactional
    public void logSecurityEvent(String userId, String eventType, String description, Map<String, Object> details, String ipAddress) {
        AuditEvent event = createAuditEvent(userId, AuditEventType.SECURITY_EVENT, eventType, "security", details, ipAddress);
        event.setDescription(description);
        auditEventRepository.save(event);

        securityEvents.increment();
        auditEventsLogged.increment();

        logger.warn("AUDIT: Security event - {} by {}: {}", eventType, userId, description);
    }

    @Transactional
    public void logSystemChange(String changedBy, String component, String changeType, Map<String, Object> beforeState, Map<String, Object> afterState) {
        Map<String, Object> details = new HashMap<>();
        details.put("component", component);
        details.put("changeType", changeType);
        details.put("beforeState", beforeState);
        details.put("afterState", afterState);

        AuditEvent event = createAuditEvent(changedBy, AuditEventType.SYSTEM_CHANGE, changeType, component, details, "system");
        auditEventRepository.save(event);

        auditEventsLogged.increment();
        logger.info("AUDIT: System change - {} modified {}: {}", changedBy, component, changeType);
    }

    @Transactional
    public void logComplianceEvent(String eventType, String description, Map<String, Object> details) {
        AuditEvent event = createAuditEvent("SYSTEM", AuditEventType.COMPLIANCE_EVENT, eventType, "compliance", details, "system");
        event.setDescription(description);
        auditEventRepository.save(event);

        complianceEvents.increment();
        auditEventsLogged.increment();

        logger.info("AUDIT: Compliance event - {}: {}", eventType, description);
    }

    @Transactional
    public void logGDPRRequest(String userId, String requestType, String status, Map<String, Object> details) {
        Map<String, Object> gdprDetails = new HashMap<>(details);
        gdprDetails.put("gdprCompliant", true);
        gdprDetails.put("requestType", requestType);

        AuditEvent event = createAuditEvent(userId, AuditEventType.GDPR_REQUEST, requestType, "gdpr", gdprDetails, "user_request");
        event.setDescription("GDPR " + requestType + " request: " + status);
        auditEventRepository.save(event);

        complianceEvents.increment();
        auditEventsLogged.increment();

        logger.info("AUDIT: GDPR request - {} for user {}: {}", requestType, userId, status);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditTrail(Pageable pageable) {
        return auditEventRepository.findAllByOrderByTimestampDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> getUserAuditTrail(String userId, Pageable pageable) {
        return auditEventRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditTrailByType(AuditEventType eventType, Pageable pageable) {
        return auditEventRepository.findByEventTypeOrderByTimestampDesc(eventType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditTrailByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditEventRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getRecentSecurityEvents(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditEventRepository.findByEventTypeAndTimestampAfterOrderByTimestampDesc(
            AuditEventType.SECURITY_EVENT, since);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getAuditEventSummary(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditEvent> events = auditEventRepository.findByTimestampBetween(startDate, endDate);

        return events.stream()
            .collect(Collectors.groupingBy(
                event -> event.getEventType().name(),
                Collectors.counting()
            ));
    }

    @Transactional
    public void cleanupOldAuditEvents(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        long deletedCount = auditEventRepository.deleteByTimestampBefore(cutoffDate);

        if (deletedCount > 0) {
            logComplianceEvent("AUDIT_CLEANUP", "Cleaned up " + deletedCount + " audit events older than " + retentionDays + " days",
                Map.of("deletedCount", deletedCount, "retentionDays", retentionDays));
        }
    }

    @Transactional(readOnly = true)
    public boolean hasSuspiciousActivity(String userId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<AuditEvent> events = auditEventRepository.findByUserIdAndEventTypeAndTimestampAfter(
            userId, AuditEventType.SECURITY_EVENT, since);

        // Check for patterns that might indicate suspicious activity
        long failedAuthCount = events.stream()
            .filter(e -> e.getAction().contains("FAILED") || e.getAction().contains("DENIED"))
            .count();

        return failedAuthCount >= 5; // 5 or more security events in the time window
    }

    @Transactional(readOnly = true)
    public AuditComplianceReport generateComplianceReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditEvent> events = auditEventRepository.findByTimestampBetween(startDate, endDate);

        long totalEvents = events.size();
        long securityEvents = events.stream().mapToLong(e -> e.getEventType() == AuditEventType.SECURITY_EVENT ? 1 : 0).sum();
        long dataAccessEvents = events.stream().mapToLong(e -> e.getEventType() == AuditEventType.DATA_ACCESS ? 1 : 0).sum();
        long complianceEvents = events.stream().mapToLong(e -> e.getEventType() == AuditEventType.COMPLIANCE_EVENT ? 1 : 0).sum();

        // Check for required audit coverage
        boolean hasAllRequiredEventTypes = events.stream()
            .map(AuditEvent::getEventType)
            .distinct()
            .count() >= AuditEventType.values().length - 1; // Exclude SYSTEM_CHANGE if not applicable

        // Check for data retention compliance
        long eventsOlderThanRetention = auditEventRepository.countByTimestampBefore(startDate.minusDays(90));

        return new AuditComplianceReport(
            startDate,
            endDate,
            totalEvents,
            securityEvents,
            dataAccessEvents,
            complianceEvents,
            hasAllRequiredEventTypes,
            eventsOlderThanRetention == 0,
            LocalDateTime.now()
        );
    }

    private AuditEvent createAuditEvent(String userId, AuditEventType eventType, String action, String resource,
                                      Map<String, Object> details, String ipAddress) {
        AuditEvent event = new AuditEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setAction(action);
        event.setResource(resource);
        event.setDetails(details);
        event.setIpAddress(ipAddress);
        event.setTimestamp(LocalDateTime.now());
        event.setSessionId(generateSessionId());

        return event;
    }

    private String generateSessionId() {
        // In a real implementation, this would come from the session management
        return "session_" + System.currentTimeMillis();
    }

    private String determineDataClassification(String tableName) {
        // Determine data classification based on table name
        if (tableName.toLowerCase().contains("payment") || tableName.toLowerCase().contains("financial")) {
            return "FINANCIAL";
        } else if (tableName.toLowerCase().contains("user") || tableName.toLowerCase().contains("profile")) {
            return "PERSONAL";
        } else if (tableName.toLowerCase().contains("audit") || tableName.toLowerCase().contains("log")) {
            return "SENSITIVE";
        } else {
            return "INTERNAL";
        }
    }

    // Metrics getters
    public double getAuditEventsLoggedCount() {
        return auditEventsLogged.count();
    }

    public double getSecurityEventsCount() {
        return securityEvents.count();
    }

    public double getDataAccessEventsCount() {
        return dataAccessEvents.count();
    }

    public double getComplianceEventsCount() {
        return complianceEvents.count();
    }
}