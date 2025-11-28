package com.netflix.streaming.compliance.gdpr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * GDPR Data Processor - Handles data subject rights operations
 *
 * Production-ready implementation of GDPR data processing operations
 * across all services and data stores in the platform.
 */
@Service
public class GDPRDataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(GDPRDataProcessor.class);

    private final JdbcTemplate jdbcTemplate;
    private final GDPRDataAnonymizer anonymizer;
    private final GDPRDataExporter exporter;

    public GDPRDataProcessor(JdbcTemplate jdbcTemplate,
                           GDPRDataAnonymizer anonymizer,
                           GDPRDataExporter exporter) {
        this.jdbcTemplate = jdbcTemplate;
        this.anonymizer = anonymizer;
        this.exporter = exporter;
    }

    /**
     * GDPR Article 15: Right of Access
     * Collect all user data from across the platform
     */
    public GDPRUserData getUserData(String userId) {
        logger.info("Collecting user data for GDPR access request: {}", userId);

        GDPRUserData userData = new GDPRUserData();
        userData.setUserId(userId);
        userData.setCollectedAt(Instant.now());

        // Collect data from user analytics
        List<Map<String, Object>> userAnalytics = getUserAnalyticsData(userId);
        userData.setUserAnalytics(userAnalytics);

        // Collect data from content analytics
        List<Map<String, Object>> contentAnalytics = getContentAnalyticsData(userId);
        userData.setContentAnalytics(contentAnalytics);

        // Collect data from playback sessions
        List<Map<String, Object>> playbackSessions = getPlaybackSessionsData(userId);
        userData.setPlaybackSessions(playbackSessions);

        // Collect data from ML pipeline runs
        List<Map<String, Object>> mlPipelineData = getMLPipelineData(userId);
        userData.setMlPipelineData(mlPipelineData);

        // Collect data from audit logs
        List<Map<String, Object>> auditLogs = getAuditLogsData(userId);
        userData.setAuditLogs(auditLogs);

        logger.info("Collected {} data records for user: {}", userData.getTotalRecords(), userId);
        return userData;
    }

    /**
     * GDPR Article 16: Right to Rectification
     * Update user data across all services
     */
    @Transactional
    public void rectifyUserData(String userId, Map<String, Object> corrections) {
        logger.info("Processing data rectification for user: {}, corrections: {}", userId, corrections.size());

        // Update user analytics data
        if (corrections.containsKey("preferences") || corrections.containsKey("demographics")) {
            updateUserAnalyticsData(userId, corrections);
        }

        // Update playback session metadata if needed
        if (corrections.containsKey("deviceType") || corrections.containsKey("region")) {
            updatePlaybackSessionMetadata(userId, corrections);
        }

        // Log rectification operation
        auditRectification(userId, corrections);

        logger.info("Data rectification completed for user: {}", userId);
    }

    /**
     * GDPR Article 17: Right to Erasure
     * Schedule complete data deletion (30-day grace period per GDPR)
     */
    @Transactional
    public String scheduleErasure(String userId, String reason, String correlationId) {
        String erasureId = UUID.randomUUID().toString();
        Instant erasureDate = Instant.now().plusSeconds(2592000L); // 30 days

        logger.info("Scheduling data erasure for user: {} (ID: {}, Date: {})",
                   userId, erasureId, erasureDate);

        // Create erasure request record
        jdbcTemplate.update("""
            INSERT INTO gdpr_erasure_requests (
                erasure_id, user_id, reason, scheduled_date,
                correlation_id, status, created_at
            ) VALUES (?, ?, ?, ?, ?, 'SCHEDULED', ?)
            """,
            erasureId, userId, reason, erasureDate, correlationId, Instant.now()
        );

        // Mark data for soft deletion (immediate)
        softDeleteUserData(userId, erasureId);

        return erasureId;
    }

    /**
     * Execute scheduled data erasure
     */
    @Transactional
    public void executeErasure(String erasureId) {
        logger.info("Executing data erasure: {}", erasureId);

        // Get erasure request
        Map<String, Object> erasureRequest = jdbcTemplate.queryForMap(
            "SELECT * FROM gdpr_erasure_requests WHERE erasure_id = ?", erasureId
        );

        String userId = (String) erasureRequest.get("user_id");

        // Hard delete all user data
        hardDeleteUserData(userId);

        // Update erasure status
        jdbcTemplate.update(
            "UPDATE gdpr_erasure_requests SET status = 'COMPLETED', completed_at = ? WHERE erasure_id = ?",
            Instant.now(), erasureId
        );

        logger.info("Data erasure completed for user: {} (Erasure ID: {})", userId, erasureId);
    }

    /**
     * GDPR Article 18: Restriction of Processing
     * Apply processing restrictions to user data
     */
    @Transactional
    public void restrictDataProcessing(String userId, List<String> restrictions) {
        logger.info("Applying processing restrictions for user: {}, restrictions: {}", userId, restrictions);

        // Store processing restrictions
        for (String restriction : restrictions) {
            jdbcTemplate.update("""
                INSERT INTO gdpr_processing_restrictions (
                    user_id, restriction_type, applied_at, status
                ) VALUES (?, ?, ?, 'ACTIVE')
                ON CONFLICT (user_id, restriction_type)
                DO UPDATE SET applied_at = EXCLUDED.applied_at, status = 'ACTIVE'
                """,
                userId, restriction, Instant.now()
            );
        }

        // Apply restrictions to data processing
        applyProcessingRestrictions(userId, restrictions);

        logger.info("Processing restrictions applied for user: {}", userId);
    }

    /**
     * GDPR Article 20: Data Portability
     * Export user data in portable format
     */
    public GDPRDataExport exportUserData(String userId, String format, String correlationId) {
        logger.info("Creating data export for user: {} in format: {}", userId, format);

        GDPRUserData userData = getUserData(userId);
        String exportId = UUID.randomUUID().toString();

        // Create export asynchronously
        CompletableFuture<GDPRDataExport> exportFuture = exporter.createExport(userData, format, exportId);

        // Store export request
        jdbcTemplate.update("""
            INSERT INTO gdpr_data_exports (
                export_id, user_id, format, status, correlation_id, created_at
            ) VALUES (?, ?, ?, 'PROCESSING', ?, ?)
            """,
            exportId, userId, format, correlationId, Instant.now()
        );

        // Complete export asynchronously
        exportFuture.thenAccept(export -> {
            jdbcTemplate.update(
                "UPDATE gdpr_data_exports SET status = 'COMPLETED', download_url = ?, completed_at = ? WHERE export_id = ?",
                export.getDownloadUrl(), Instant.now(), exportId
            );
            logger.info("Data export completed for user: {} (Export ID: {})", userId, exportId);
        });

        return new GDPRDataExport(exportId, format, "PROCESSING", null, Instant.now());
    }

    /**
     * Consent Management
     */
    @Transactional
    public void updateConsent(String userId, GDPRController.GDPRConsentRequest request) {
        logger.info("Updating consent for user: {}, type: {}, granted: {}",
                   userId, request.getConsentType(), request.isGranted());

        jdbcTemplate.update("""
            INSERT INTO gdpr_consent_records (
                user_id, consent_type, consent_version, granted, granted_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (user_id, consent_type)
            DO UPDATE SET
                consent_version = EXCLUDED.consent_version,
                granted = EXCLUDED.granted,
                granted_at = CASE WHEN EXCLUDED.granted THEN EXCLUDED.granted_at ELSE gdpr_consent_records.granted_at END,
                updated_at = EXCLUDED.updated_at
            """,
            userId, request.getConsentType(), request.getVersion(),
            request.isGranted(), Instant.now(), Instant.now()
        );
    }

    /**
     * Check for legal holds blocking erasure
     */
    public boolean hasLegalHold(String userId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM legal_holds WHERE user_id = ? AND status = 'ACTIVE'",
            Integer.class, userId
        );
        return count != null && count > 0;
    }

    // Private helper methods

    private List<Map<String, Object>> getUserAnalyticsData(String userId) {
        return jdbcTemplate.queryForList(
            "SELECT * FROM user_analytics WHERE user_id = ? AND tenant_id = 'default'",
            userId
        );
    }

    private List<Map<String, Object>> getContentAnalyticsData(String userId) {
        return jdbcTemplate.queryForList("""
            SELECT ca.* FROM content_analytics ca
            INNER JOIN playback_sessions ps ON ca.content_id = ps.content_id
            WHERE ps.user_id = ? AND ca.tenant_id = 'default'
            """,
            userId
        );
    }

    private List<Map<String, Object>> getPlaybackSessionsData(String userId) {
        return jdbcTemplate.queryForList(
            "SELECT * FROM playback_sessions WHERE user_id = ? AND tenant_id = 'default'",
            userId
        );
    }

    private List<Map<String, Object>> getMLPipelineData(String userId) {
        return jdbcTemplate.queryForList("""
            SELECT * FROM ml_pipeline_executions
            WHERE correlation_id IN (
                SELECT correlation_id FROM playback_sessions WHERE user_id = ?
            )
            """,
            userId
        );
    }

    private List<Map<String, Object>> getAuditLogsData(String userId) {
        return jdbcTemplate.queryForList(
            "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY created_at DESC LIMIT 1000",
            userId
        );
    }

    private void updateUserAnalyticsData(String userId, Map<String, Object> corrections) {
        // Update user analytics with corrections
        corrections.forEach((field, value) -> {
            jdbcTemplate.update(
                "UPDATE user_analytics SET " + field + " = ?, updated_at = ? WHERE user_id = ?",
                value, Instant.now(), userId
            );
        });
    }

    private void updatePlaybackSessionMetadata(String userId, Map<String, Object> corrections) {
        corrections.forEach((field, value) -> {
            jdbcTemplate.update(
                "UPDATE playback_sessions SET " + field + " = ?, updated_at = ? WHERE user_id = ?",
                value, Instant.now(), userId
            );
        });
    }

    private void auditRectification(String userId, Map<String, Object> corrections) {
        jdbcTemplate.update("""
            INSERT INTO gdpr_audit_log (
                user_id, operation_type, operation_details, performed_at
            ) VALUES (?, 'RECTIFICATION', ?, ?)
            """,
            userId, corrections.toString(), Instant.now()
        );
    }

    private void softDeleteUserData(String userId, String erasureId) {
        // Mark data for deletion (soft delete)
        String[] tables = {"user_analytics", "playback_sessions", "ml_pipeline_executions"};

        for (String table : tables) {
            jdbcTemplate.update(
                "UPDATE " + table + " SET status = 'PENDING_DELETION', erasure_id = ?, updated_at = ? WHERE user_id = ?",
                erasureId, Instant.now(), userId
            );
        }
    }

    private void hardDeleteUserData(String userId) {
        // Permanently delete user data
        String[] tables = {"user_analytics", "playback_sessions", "ml_pipeline_executions", "audit_logs"};

        for (String table : tables) {
            jdbcTemplate.update("DELETE FROM " + table + " WHERE user_id = ?", userId);
        }
    }

    private void applyProcessingRestrictions(String userId, List<String> restrictions) {
        // Apply processing restrictions (e.g., stop analytics, disable personalization)
        for (String restriction : restrictions) {
            switch (restriction) {
                case "ANALYTICS_PROCESSING" -> disableAnalyticsProcessing(userId);
                case "PERSONALIZATION" -> disablePersonalization(userId);
                case "MARKETING" -> disableMarketingCommunications(userId);
            }
        }
    }

    private void disableAnalyticsProcessing(String userId) {
        jdbcTemplate.update(
            "UPDATE user_analytics SET analytics_disabled = true, updated_at = ? WHERE user_id = ?",
            Instant.now(), userId
        );
    }

    private void disablePersonalization(String userId) {
        jdbcTemplate.update(
            "UPDATE user_analytics SET personalization_disabled = true, updated_at = ? WHERE user_id = ?",
            Instant.now(), userId
        );
    }

    private void disableMarketingCommunications(String userId) {
        jdbcTemplate.update(
            "UPDATE user_analytics SET marketing_disabled = true, updated_at = ? WHERE user_id = ?",
            Instant.now(), userId
        );
    }

    // Data classes

    public static class GDPRUserData {
        private String userId;
        private Instant collectedAt;
        private List<Map<String, Object>> userAnalytics = List.of();
        private List<Map<String, Object>> contentAnalytics = List.of();
        private List<Map<String, Object>> playbackSessions = List.of();
        private List<Map<String, Object>> mlPipelineData = List.of();
        private List<Map<String, Object>> auditLogs = List.of();

        public int getTotalRecords() {
            return userAnalytics.size() + contentAnalytics.size() +
                   playbackSessions.size() + mlPipelineData.size() + auditLogs.size();
        }

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public Instant getCollectedAt() { return collectedAt; }
        public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
        public List<Map<String, Object>> getUserAnalytics() { return userAnalytics; }
        public void setUserAnalytics(List<Map<String, Object>> userAnalytics) { this.userAnalytics = userAnalytics; }
        public List<Map<String, Object>> getContentAnalytics() { return contentAnalytics; }
        public void setContentAnalytics(List<Map<String, Object>> contentAnalytics) { this.contentAnalytics = contentAnalytics; }
        public List<Map<String, Object>> getPlaybackSessions() { return playbackSessions; }
        public void setPlaybackSessions(List<Map<String, Object>> playbackSessions) { this.playbackSessions = playbackSessions; }
        public List<Map<String, Object>> getMlPipelineData() { return mlPipelineData; }
        public void setMlPipelineData(List<Map<String, Object>> mlPipelineData) { this.mlPipelineData = mlPipelineData; }
        public List<Map<String, Object>> getAuditLogs() { return auditLogs; }
        public void setAuditLogs(List<Map<String, Object>> auditLogs) { this.auditLogs = auditLogs; }
    }

    public static class GDPRDataExport {
        private String exportId;
        private String format;
        private String status;
        private String downloadUrl;
        private Instant createdAt;

        public GDPRDataExport(String exportId, String format, String status, String downloadUrl, Instant createdAt) {
            this.exportId = exportId;
            this.format = format;
            this.status = status;
            this.downloadUrl = downloadUrl;
            this.createdAt = createdAt;
        }

        // Getters
        public String getExportId() { return exportId; }
        public String getFormat() { return format; }
        public String getStatus() { return status; }
        public String getDownloadUrl() { return downloadUrl; }
        public Instant getCreatedAt() { return createdAt; }
    }
}