package org.example.compliance;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataProtectionService {

    private static final Logger logger = LoggerFactory.getLogger(DataProtectionService.class);

    private final AuditLogger auditLogger;
    private final MeterRegistry meterRegistry;

    // GDPR compliance counters
    private final Counter gdprRequestsProcessed;
    private final Counter dataExportRequests;
    private final Counter dataDeletionRequests;
    private final Counter consentUpdates;

    // Data retention tracking
    private final Map<String, LocalDateTime> dataRetentionSchedule = new ConcurrentHashMap<>();
    private final Map<String, Boolean> userConsents = new ConcurrentHashMap<>();

    // Data categories
    public enum DataCategory {
        PERSONAL_DATA,
        FINANCIAL_DATA,
        COMMUNICATION_DATA,
        TECHNICAL_DATA
    }

    public enum GDPRRequestType {
        ACCESS,
        RECTIFICATION,
        ERASURE,
        RESTRICTION,
        PORTABILITY,
        OBJECTION
    }

    @Autowired
    public DataProtectionService(AuditLogger auditLogger, MeterRegistry meterRegistry) {
        this.auditLogger = auditLogger;
        this.meterRegistry = meterRegistry;

        // Initialize GDPR counters
        this.gdprRequestsProcessed = Counter.builder("producer_consumer.gdpr.requests.processed")
                .description("Total number of GDPR requests processed")
                .register(meterRegistry);

        this.dataExportRequests = Counter.builder("producer_consumer.gdpr.data_exports")
                .description("Total number of data export requests")
                .register(meterRegistry);

        this.dataDeletionRequests = Counter.builder("producer_consumer.gdpr.data_deletions")
                .description("Total number of data deletion requests")
                .register(meterRegistry);

        this.consentUpdates = Counter.builder("producer_consumer.gdpr.consent_updates")
                .description("Total number of consent updates")
                .register(meterRegistry);

        // Register gauges for compliance monitoring
        Gauge.builder("producer_consumer.gdpr.active_consents", userConsents::size)
                .description("Number of active user consents")
                .register(meterRegistry);
    }

    @Transactional
    public Map<String, Object> processGDPRRequest(String userId, GDPRRequestType requestType, Map<String, Object> requestDetails) {
        Map<String, Object> response = new HashMap<>();
        response.put("requestId", UUID.randomUUID().toString());
        response.put("timestamp", LocalDateTime.now());
        response.put("requestType", requestType.name());
        response.put("status", "PROCESSING");

        try {
            switch (requestType) {
                case ACCESS:
                    response.put("data", getUserData(userId));
                    response.put("status", "COMPLETED");
                    break;

                case ERASURE:
                    boolean deleted = deleteUserData(userId);
                    response.put("status", deleted ? "COMPLETED" : "FAILED");
                    dataDeletionRequests.increment();
                    break;

                case PORTABILITY:
                    response.put("data", exportUserData(userId));
                    response.put("status", "COMPLETED");
                    dataExportRequests.increment();
                    break;

                case RECTIFICATION:
                    boolean updated = updateUserData(userId, requestDetails);
                    response.put("status", updated ? "COMPLETED" : "FAILED");
                    break;

                case RESTRICTION:
                    boolean restricted = restrictDataProcessing(userId);
                    response.put("status", restricted ? "COMPLETED" : "FAILED");
                    break;

                case OBJECTION:
                    boolean objected = processObjection(userId, requestDetails);
                    response.put("status", objected ? "COMPLETED" : "FAILED");
                    break;
            }

            gdprRequestsProcessed.increment();

        } catch (Exception e) {
            logger.error("Error processing GDPR request for user: {}", userId, e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        }

        return response;
    }

    public boolean updateUserConsent(String userId, DataCategory category, boolean consent) {
        boolean previousConsent = userConsents.getOrDefault(userId + ":" + category, false);
        userConsents.put(userId + ":" + category, consent);

        Map<String, Object> consentDetails = Map.of(
            "userId", userId,
            "category", category.name(),
            "consent", consent,
            "previousConsent", previousConsent,
            "timestamp", LocalDateTime.now().toString()
        );

        auditLogger.logUserAction(userId, "CONSENT_UPDATE", "user_consent", consentDetails, "system");

        consentUpdates.increment();
        return true;
    }

    public boolean hasUserConsent(String userId, DataCategory category) {
        return userConsents.getOrDefault(userId + ":" + category, false);
    }

    public void scheduleDataRetention(String userId, LocalDateTime retentionDate) {
        dataRetentionSchedule.put(userId, retentionDate);

        Map<String, Object> retentionDetails = Map.of(
            "userId", userId,
            "retentionDate", retentionDate.toString(),
            "scheduledAt", LocalDateTime.now().toString()
        );

        auditLogger.logComplianceEvent("DATA_RETENTION_SCHEDULED",
                "Data retention scheduled for user: " + userId, retentionDetails);
    }

    public List<String> getExpiredDataUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredUsers = new ArrayList<>();

        for (Map.Entry<String, LocalDateTime> entry : dataRetentionSchedule.entrySet()) {
            if (entry.getValue().isBefore(now)) {
                expiredUsers.add(entry.getKey());
            }
        }

        return expiredUsers;
    }

    public Map<String, Object> getDataProcessingReport(String userId) {
        Map<String, Object> report = new HashMap<>();
        report.put("userId", userId);
        report.put("generatedAt", LocalDateTime.now().toString());

        // Data categories and processing status
        Map<String, Object> dataCategories = new HashMap<>();
        for (DataCategory category : DataCategory.values()) {
            dataCategories.put(category.name(), Map.of(
                "consent", hasUserConsent(userId, category),
                "processing", true,
                "purpose", getDataPurpose(category),
                "retentionPeriod", getRetentionPeriod(category),
                "legalBasis", getLegalBasis(category)
            ));
        }
        report.put("dataCategories", dataCategories);

        // Data sharing information
        report.put("dataSharing", getDataSharingInfo(userId));

        // Security measures
        report.put("securityMeasures", Arrays.asList(
            "Encryption at rest",
            "Encryption in transit",
            "Access controls and authentication",
            "Regular security audits",
            "Data anonymization",
            "Secure deletion procedures"
        ));

        // Rights exercised
        report.put("rightsExercised", getUserRightsHistory(userId));

        return report;
    }

    private Map<String, Object> getUserData(String userId) {
        // In a real implementation, this would aggregate data from all user-related tables
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("profile", Map.of(
            "userId", userId,
            "registrationDate", "2023-01-01",
            "lastLogin", LocalDateTime.now().toString(),
            "accountStatus", "ACTIVE"
        ));
        userData.put("preferences", Map.of(
            "theme", "dark",
            "notifications", true,
            "language", "en"
        ));
        userData.put("activity", Map.of(
            "totalRequests", 150,
            "lastActivity", LocalDateTime.now().minusHours(2).toString(),
            "favoriteEndpoints", Arrays.asList("/api/health", "/api/producer")
        ));

        // Include consent history
        userData.put("consents", getUserConsents(userId));

        return userData;
    }

    private boolean deleteUserData(String userId) {
        logger.info("GDPR: Deleting data for user: {}", userId);

        // In a real implementation, this would:
        // 1. Mark user account as deleted
        // 2. Anonymize or delete personal data
        // 3. Remove from data retention schedule
        // 4. Log the deletion for compliance

        dataRetentionSchedule.remove(userId);

        // Remove consents
        userConsents.keySet().removeIf(key -> key.startsWith(userId + ":"));

        auditLogger.logComplianceEvent("USER_DATA_DELETED",
                "User data deleted for GDPR compliance: " + userId,
                Map.of("userId", userId, "deletionTimestamp", LocalDateTime.now()));

        return true;
    }

    private Map<String, Object> exportUserData(String userId) {
        Map<String, Object> exportData = getUserData(userId);
        exportData.put("exportFormat", "JSON");
        exportData.put("exportedAt", LocalDateTime.now().toString());
        exportData.put("gdprCompliant", true);
        exportData.put("retentionNotice", "This data export is provided under GDPR Article 20. " +
                "You have the right to have your data erased after this export if you wish.");

        return exportData;
    }

    private boolean updateUserData(String userId, Map<String, Object> updates) {
        logger.info("GDPR: Updating data for user: {} with: {}", userId, updates);

        // In a real implementation, this would update user profile data
        // and validate the changes

        auditLogger.logUserAction(userId, "DATA_RECTIFICATION", "user_profile",
                Map.of("updates", updates), "gdpr_request");

        return true;
    }

    private boolean restrictDataProcessing(String userId) {
        logger.info("GDPR: Restricting data processing for user: {}", userId);

        // In a real implementation, this would:
        // 1. Mark user data as restricted
        // 2. Stop automated processing
        // 3. Limit data usage to legal requirements only

        auditLogger.logComplianceEvent("DATA_PROCESSING_RESTRICTED",
                "Data processing restricted for user: " + userId,
                Map.of("userId", userId, "restrictionTimestamp", LocalDateTime.now()));

        return true;
    }

    private boolean processObjection(String userId, Map<String, Object> details) {
        logger.info("GDPR: Processing objection for user: {} with details: {}", userId, details);

        // In a real implementation, this would:
        // 1. Stop the specific processing activity
        // 2. Document the objection
        // 3. Provide confirmation

        auditLogger.logGDPRRequest(userId, "OBJECTION", "COMPLETED", details);

        return true;
    }

    private Map<String, Object> getUserConsents(String userId) {
        Map<String, Object> consents = new HashMap<>();
        for (DataCategory category : DataCategory.values()) {
            consents.put(category.name(), Map.of(
                "granted", hasUserConsent(userId, category),
                "lastUpdated", LocalDateTime.now().toString(),
                "version", "1.0"
            ));
        }
        return consents;
    }

    private String getDataPurpose(DataCategory category) {
        return switch (category) {
            case PERSONAL_DATA -> "User account management and personalization";
            case FINANCIAL_DATA -> "Processing financial transactions and payments";
            case COMMUNICATION_DATA -> "Communication and support services";
            case TECHNICAL_DATA -> "Technical support and system optimization";
        };
    }

    private String getRetentionPeriod(DataCategory category) {
        return switch (category) {
            case PERSONAL_DATA -> "Account active + 3 years";
            case FINANCIAL_DATA -> "7 years (legal requirement)";
            case COMMUNICATION_DATA -> "2 years";
            case TECHNICAL_DATA -> "1 year";
        };
    }

    private String getLegalBasis(DataCategory category) {
        return switch (category) {
            case PERSONAL_DATA -> "Contract performance (Article 6.1b GDPR)";
            case FINANCIAL_DATA -> "Legal obligation (Article 6.1c GDPR)";
            case COMMUNICATION_DATA -> "Legitimate interest (Article 6.1f GDPR)";
            case TECHNICAL_DATA -> "Legitimate interest (Article 6.1f GDPR)";
        };
    }

    private List<Map<String, Object>> getDataSharingInfo(String userId) {
        return Arrays.asList(
            Map.of(
                "recipient", "Payment Processor",
                "purpose", "Payment processing and fraud prevention",
                "categories", Arrays.asList("FINANCIAL_DATA", "PERSONAL_DATA"),
                "location", "EU",
                "retention", "7 years",
                "legalBasis", "Contract performance"
            ),
            Map.of(
                "recipient", "Analytics Provider",
                "purpose", "Service improvement and analytics",
                "categories", Arrays.asList("TECHNICAL_DATA"),
                "location", "US",
                "retention", "2 years",
                "legalBasis", "Legitimate interest"
            ),
            Map.of(
                "recipient", "Cloud Provider",
                "purpose", "Data storage and processing",
                "categories", Arrays.asList("PERSONAL_DATA", "FINANCIAL_DATA", "TECHNICAL_DATA"),
                "location", "EU",
                "retention", "As required by data controller",
                "legalBasis", "Contract performance"
            )
        );
    }

    private List<Map<String, Object>> getUserRightsHistory(String userId) {
        // In a real implementation, this would track actual GDPR requests
        return Arrays.asList(
            Map.of(
                "right", "ACCESS",
                "exercisedAt", LocalDateTime.now().minusDays(30).toString(),
                "status", "COMPLETED",
                "requestId", "gdpr_access_001"
            ),
            Map.of(
                "right", "PORTABILITY",
                "exercisedAt", LocalDateTime.now().minusDays(15).toString(),
                "status", "COMPLETED",
                "requestId", "gdpr_portability_001"
            )
        );
    }

    // Metrics getters
    public double getGdprRequestsProcessedCount() {
        return gdprRequestsProcessed.count();
    }

    public double getDataExportRequestsCount() {
        return dataExportRequests.count();
    }

    public double getDataDeletionRequestsCount() {
        return dataDeletionRequests.count();
    }

    public double getConsentUpdatesCount() {
        return consentUpdates.count();
    }

    public int getActiveConsentsCount() {
        return userConsents.size();
    }
}