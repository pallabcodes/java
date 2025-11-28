package com.example.compliance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogger.class);

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    // Audit counters
    private final Counter auditEventsLogged;
    private final Counter dataAccessAudits;
    private final Counter userActionAudits;
    private final Counter systemChangeAudits;

    public enum AuditEventType {
        USER_LOGIN,
        USER_LOGOUT,
        DATA_ACCESS,
        DATA_MODIFICATION,
        DATA_DELETION,
        PERMISSION_CHANGE,
        SYSTEM_CONFIG_CHANGE,
        SECURITY_INCIDENT,
        GDPR_REQUEST,
        COMPLIANCE_CHECK
    }

    public enum AuditSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public AuditLogger(ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;

        // Initialize audit counters
        this.auditEventsLogged = Counter.builder("audit.events.logged")
                .description("Total number of audit events logged")
                .register(meterRegistry);

        this.dataAccessAudits = Counter.builder("audit.data_access")
                .description("Total number of data access audit events")
                .register(meterRegistry);

        this.userActionAudits = Counter.builder("audit.user_actions")
                .description("Total number of user action audit events")
                .register(meterRegistry);

        this.systemChangeAudits = Counter.builder("audit.system_changes")
                .description("Total number of system change audit events")
                .register(meterRegistry);
    }

    public void logUserAction(String userId, String username, AuditEventType eventType,
                             String action, String resource, Map<String, Object> details) {
        Map<String, Object> auditData = createAuditEvent(userId, username, eventType,
                AuditSeverity.MEDIUM, details);
        auditData.put("action", action);
        auditData.put("resource", resource);
        auditData.put("category", "USER_ACTION");

        logAuditEvent(auditData);
        userActionAudits.increment();
    }

    public void logDataAccess(String userId, String username, String operation,
                             String tableName, String recordId, Map<String, Object> details) {
        Map<String, Object> auditData = createAuditEvent(userId, username,
                AuditEventType.DATA_ACCESS, AuditSeverity.LOW, details);
        auditData.put("operation", operation);
        auditData.put("tableName", tableName);
        auditData.put("recordId", recordId);
        auditData.put("category", "DATA_ACCESS");

        // Add IP address and user agent for data access
        auditData.put("ipAddress", details.get("ipAddress"));
        auditData.put("userAgent", details.get("userAgent"));

        logAuditEvent(auditData);
        dataAccessAudits.increment();
    }

    public void logDataModification(String userId, String username, String operation,
                                   String tableName, String recordId, Map<String, Object> oldValues,
                                   Map<String, Object> newValues) {
        Map<String, Object> auditData = createAuditEvent(userId, username,
                AuditEventType.DATA_MODIFICATION, AuditSeverity.MEDIUM, null);
        auditData.put("operation", operation);
        auditData.put("tableName", tableName);
        auditData.put("recordId", recordId);
        auditData.put("oldValues", sanitizeMap(oldValues));
        auditData.put("newValues", sanitizeMap(newValues));
        auditData.put("category", "DATA_MODIFICATION");

        logAuditEvent(auditData);
        dataAccessAudits.increment();
    }

    public void logSecurityIncident(String incidentType, String description,
                                   AuditSeverity severity, Map<String, Object> details) {
        Map<String, Object> auditData = createAuditEvent(null, null,
                AuditEventType.SECURITY_INCIDENT, severity, details);
        auditData.put("incidentType", incidentType);
        auditData.put("description", description);
        auditData.put("category", "SECURITY_INCIDENT");

        logAuditEvent(auditData);
        auditEventsLogged.increment();
    }

    public void logGDPRRequest(String userId, String requestType, String status,
                              Map<String, Object> details) {
        Map<String, Object> auditData = createAuditEvent(userId, null,
                AuditEventType.GDPR_REQUEST, AuditSeverity.HIGH, details);
        auditData.put("requestType", requestType);
        auditData.put("status", status);
        auditData.put("gdprCompliant", true);
        auditData.put("category", "GDPR");

        logAuditEvent(auditData);
        auditEventsLogged.increment();
    }

    public void logSystemChange(String changedBy, String component, String changeType,
                               Map<String, Object> beforeState, Map<String, Object> afterState) {
        Map<String, Object> auditData = createAuditEvent(null, changedBy,
                AuditEventType.SYSTEM_CONFIG_CHANGE, AuditSeverity.HIGH, null);
        auditData.put("component", component);
        auditData.put("changeType", changeType);
        auditData.put("beforeState", beforeState);
        auditData.put("afterState", afterState);
        auditData.put("category", "SYSTEM_CHANGE");

        logAuditEvent(auditData);
        systemChangeAudits.increment();
    }

    private Map<String, Object> createAuditEvent(String userId, String username,
                                                AuditEventType eventType,
                                                AuditSeverity severity,
                                                Map<String, Object> additionalDetails) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("auditId", UUID.randomUUID().toString());
        auditEvent.put("timestamp", LocalDateTime.now().toString());
        auditEvent.put("eventType", eventType.name());
        auditEvent.put("severity", severity.name());
        auditEvent.put("userId", userId);
        auditEvent.put("username", sanitizeString(username));
        auditEvent.put("service", "sde2-essentials-sample");
        auditEvent.put("version", "1.0.0");
        auditEvent.put("environment", System.getProperty("spring.profiles.active", "unknown"));
        auditEvent.put("correlationId", UUID.randomUUID().toString());

        if (additionalDetails != null) {
            auditEvent.putAll(sanitizeMap(additionalDetails));
        }

        return auditEvent;
    }

    private void logAuditEvent(Map<String, Object> auditData) {
        try {
            String auditJson = objectMapper.writeValueAsString(auditData);

            // Log as structured JSON for compliance and SIEM integration
            logger.info("AUDIT_EVENT: {}", auditJson);

            // Also log in traditional format for human readability
            String eventType = (String) auditData.get("eventType");
            String severity = (String) auditData.get("severity");
            String username = (String) auditData.get("username");
            logger.info("Audit Event - Type: {}, Severity: {}, User: {}, Timestamp: {}",
                       eventType, severity, username, auditData.get("timestamp"));

        } catch (Exception e) {
            logger.error("Failed to serialize audit event", e);
        }
    }

    private String sanitizeString(String input) {
        if (input == null) return null;
        // Remove potentially sensitive information from audit logs
        return input.replaceAll("(?i)password|token|key|secret|ssn|credit_card", "***");
    }

    private Map<String, Object> sanitizeMap(Map<String, Object> input) {
        if (input == null) return null;

        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey().toLowerCase();
            Object value = entry.getValue();

            // Skip sensitive fields
            if (key.contains("password") || key.contains("token") || key.contains("key") ||
                key.contains("secret") || key.contains("ssn") || key.contains("credit")) {
                sanitized.put(entry.getKey(), "***");
            } else if (value instanceof String) {
                sanitized.put(entry.getKey(), sanitizeString((String) value));
            } else {
                sanitized.put(entry.getKey(), value);
            }
        }
        return sanitized;
    }

    // Metrics getters for monitoring
    public double getAuditEventsLoggedCount() {
        return auditEventsLogged.count();
    }

    public double getDataAccessAuditsCount() {
        return dataAccessAudits.count();
    }

    public double getUserActionAuditsCount() {
        return userActionAudits.count();
    }

    public double getSystemChangeAuditsCount() {
        return systemChangeAudits.count();
    }
}