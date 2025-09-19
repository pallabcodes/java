package com.algorithmpractice.solid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Audit Log Level Enumeration
 * 
 * <p>This enum represents different severity levels for audit log entries in the Netflix ecosystem.
 * It demonstrates advanced Java type inference patterns, enum methods, and production-grade
 * practices expected at Netflix for SDE-2 Senior Backend Engineers.</p>
 * 
 * <p><strong>Key Features for Cross-Language Developers (TypeScript/Node.js background):</strong></p>
 * <ul>
 *   <li><strong>Severity Levels:</strong> Unlike TypeScript string literals, Java enums provide ordered severity levels</li>
 *   <li><strong>Type Safety:</strong> Compile-time type checking prevents invalid log levels</li>
 *   <li><strong>Method Support:</strong> Enums can have methods for severity comparison and validation</li>
 *   <li><strong>Immutability:</strong> All enum values are immutable and thread-safe by default</li>
 *   <li><strong>Serialization:</strong> Built-in JSON serialization for logging frameworks</li>
 * </ul>
 * 
 * <p><strong>Netflix Production Standards:</strong></p>
 * <ul>
 *   <li>Comprehensive documentation for cross-language developers</li>
 *   <li>Type inference patterns using 'var' keyword for local variables</li>
 *   <li>Final keyword usage for immutability and performance optimization</li>
 *   <li>Wrapper class integration for null safety and collection operations</li>
 *   <li>Explicit and implicit type casting examples</li>
 * </ul>
 * 
 * @author Netflix Backend Engineering Team
 * @version 2.0.0
 * @since 2024
 */
public enum AuditLogLevel {

    /**
     * Informational audit entries.
     */
    INFO("Info", "Informational audit entries", 1),

    /**
     * Warning audit entries.
     */
    WARNING("Warning", "Warning audit entries", 2),

    /**
     * Error audit entries.
     */
    ERROR("Error", "Error audit entries", 3),

    /**
     * Critical audit entries.
     */
    CRITICAL("Critical", "Critical audit entries", 4),

    /**
     * Security audit entries.
     */
    SECURITY("Security", "Security-related audit entries", 5);

    private final String displayName;
    private final String description;
    private final int severity;

    /**
     * Constructs a new AuditLogLevel with the specified display name, description, and severity.
     * 
     * @param displayName the human-readable name for the audit log level
     * @param description the detailed description of what the audit log level means
     * @param severity the numeric severity level (higher = more severe)
     */
    AuditLogLevel(final String displayName, final String description, final int severity) {
        this.displayName = displayName;
        this.description = description;
        this.severity = severity;
    }

    /**
     * Gets the human-readable display name for this audit log level.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of this audit log level.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the numeric severity level.
     * 
     * @return the severity level (higher = more severe)
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Checks if this audit log level is informational.
     * 
     * @return true if the audit log level is informational, false otherwise
     */
    public boolean isInformational() {
        return this == INFO;
    }

    /**
     * Checks if this audit log level is a warning.
     * 
     * @return true if the audit log level is a warning, false otherwise
     */
    public boolean isWarning() {
        return this == WARNING;
    }

    /**
     * Checks if this audit log level is an error.
     * 
     * @return true if the audit log level is an error, false otherwise
     */
    public boolean isError() {
        return this == ERROR;
    }

    /**
     * Checks if this audit log level is critical.
     * 
     * @return true if the audit log level is critical, false otherwise
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }

    /**
     * Checks if this audit log level is security-related.
     * 
     * @return true if the audit log level is security-related, false otherwise
     */
    public boolean isSecurityRelated() {
        return this == SECURITY;
    }

    /**
     * Checks if this audit log level requires immediate attention.
     * 
     * @return true if the audit log level requires immediate attention, false otherwise
     */
    public boolean requiresImmediateAttention() {
        return this == CRITICAL || this == SECURITY;
    }

    /**
     * Checks if this audit log level requires escalation.
     * 
     * @return true if the audit log level requires escalation, false otherwise
     */
    public boolean requiresEscalation() {
        return this == CRITICAL || this == SECURITY || this == ERROR;
    }

    /**
     * Compares this audit log level with another for severity.
     * 
     * @param other the other audit log level to compare with
     * @return negative if this is less severe, positive if more severe, zero if equal
     */
    public int compareSeverity(final AuditLogLevel other) {
        if (other == null) {
            return 1; // This is more severe than null
        }
        return Integer.compare(this.severity, other.severity);
    }

    /**
     * Checks if this audit log level is more severe than another.
     * 
     * @param other the other audit log level to compare with
     * @return true if this is more severe than the other, false otherwise
     */
    public boolean isMoreSevereThan(final AuditLogLevel other) {
        return compareSeverity(other) > 0;
    }

    /**
     * Checks if this audit log level is less severe than another.
     * 
     * @param other the other audit log level to compare with
     * @return true if this is less severe than the other, false otherwise
     */
    public boolean isLessSevereThan(final AuditLogLevel other) {
        return compareSeverity(other) < 0;
    }

    /**
     * Gets an AuditLogLevel from its display name.
     * 
     * @param displayName the display name to search for
     * @return the matching AuditLogLevel, or null if not found
     */
    public static AuditLogLevel fromDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (final AuditLogLevel level : values()) {
            if (level.displayName.equalsIgnoreCase(displayName)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Gets an AuditLogLevel from its severity level.
     * 
     * @param severity the severity level to search for
     * @return the matching AuditLogLevel, or null if not found
     */
    public static AuditLogLevel fromSeverity(final int severity) {
        for (final AuditLogLevel level : values()) {
            if (level.severity == severity) {
                return level;
            }
        }
        return null;
    }

    /**
     * Netflix Production-Grade Type Inference Methods
     * 
     * These methods demonstrate advanced type inference patterns using the 'var' keyword
     * and wrapper classes for production-grade code at Netflix.
     */
    
    /**
     * Demonstrates type inference with audit log level collections and wrapper classes
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> This shows how Java's type inference
     * works with collections and enums, providing compile-time safety for audit logging patterns.</p>
     * 
     * @return Map containing audit level information with type inference
     */
    public static Map<String, Object> demonstrateTypeInferenceWithAuditLevels() {
        // Type inference with var keyword - Netflix production standard
        var lowSeverityLevels = Arrays.asList(INFO, WARNING); // List<AuditLogLevel>
        var highSeverityLevels = Arrays.asList(ERROR, CRITICAL, SECURITY); // List<AuditLogLevel>
        var allLevels = Arrays.asList(values()); // List<AuditLogLevel>
        
        // Type inference with Map collections
        var severityMap = Map.of(
            "low", lowSeverityLevels,
            "high", highSeverityLevels,
            "all", allLevels
        ); // Map<String, List<AuditLogLevel>>
        
        // Wrapper class usage for counting and statistics
        var lowSeverityCount = Integer.valueOf(lowSeverityLevels.size()); // Integer wrapper
        var highSeverityCount = Integer.valueOf(highSeverityLevels.size()); // Integer wrapper
        var totalLevels = Integer.valueOf(allLevels.size()); // Integer wrapper
        
        // Type inference with stream operations and wrapper classes
        var maxSeverity = allLevels.stream()
            .mapToInt(AuditLogLevel::getSeverity)
            .max()
            .orElse(0); // int primitive
        
        var maxSeverityWrapper = Integer.valueOf(maxSeverity); // Integer wrapper
        
        // Type inference with Optional wrapper class
        var criticalLevel = Optional.of(CRITICAL); // Optional<AuditLogLevel>
        var criticalDescription = criticalLevel
            .map(AuditLogLevel::getDescription)
            .orElse("No critical level"); // String
        
        // Complex type inference with nested generics and wrapper classes
        var auditMetadata = Map.of(
            "lowSeverityCount", lowSeverityCount,
            "highSeverityCount", highSeverityCount,
            "totalLevels", totalLevels,
            "maxSeverity", maxSeverityWrapper,
            "criticalDescription", criticalDescription,
            "severityLevels", severityMap,
            "hasHighSeverity", Boolean.valueOf(!highSeverityLevels.isEmpty())
        ); // Map<String, Object>
        
        return auditMetadata;
    }
    
    /**
     * Demonstrates explicit and implicit type casting with audit log levels
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> Java requires explicit casting
     * for type narrowing, providing runtime safety for audit log operations.</p>
     * 
     * @param levelValue the audit level value to process
     * @return processed audit level information with type casting
     */
    public static Map<String, Object> demonstrateTypeCastingWithAuditLevels(final String levelValue) {
        // Implicit type casting (widening) - safe and automatic
        var auditLevel = fromDisplayName(levelValue); // AuditLogLevel (nullable)
        var levelString = auditLevel != null ? auditLevel.name() : "UNKNOWN"; // String
        
        // Explicit type casting with wrapper classes
        var levelOrdinal = auditLevel != null ? Integer.valueOf(auditLevel.ordinal()) : Integer.valueOf(-1);
        var levelSeverity = auditLevel != null ? Integer.valueOf(auditLevel.getSeverity()) : Integer.valueOf(0);
        var levelHashCode = auditLevel != null ? Integer.valueOf(auditLevel.hashCode()) : Integer.valueOf(0);
        
        // Type casting for severity calculations
        var severityScore = auditLevel != null ? Double.valueOf(auditLevel.getSeverity() * 10.0) : Double.valueOf(0.0);
        var severityScoreInt = severityScore.intValue(); // Explicit narrowing cast
        
        // Type casting with arrays and collections
        var levelArray = new AuditLogLevel[]{INFO, WARNING, ERROR, CRITICAL, SECURITY}; // AuditLogLevel[]
        var levelList = Arrays.asList(levelArray); // List<AuditLogLevel>
        
        // Type casting with streams and functional interfaces
        var levelNames = levelList.stream()
            .map(AuditLogLevel::name) // String
            .collect(Collectors.toList()); // List<String>
        
        // Type casting with wrapper class operations
        var severityWrapper = Integer.valueOf(severityScoreInt); // Integer wrapper
        var isHighSeverity = Boolean.valueOf(severityScoreInt > 30); // Boolean wrapper
        
        return Map.of(
            "auditLevel", levelString,
            "ordinal", levelOrdinal,
            "severity", levelSeverity,
            "hashCode", levelHashCode,
            "severityScore", severityScore,
            "severityScoreInt", severityWrapper,
            "isHighSeverity", isHighSeverity,
            "levelNames", levelNames
        );
    }
    
    /**
     * Demonstrates global vs local variable scoping with audit log levels
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> Java has block scoping similar to
     * TypeScript, but with explicit type declarations and final keyword usage for constants.</p>
     * 
     * @param auditContext the context of audit logging
     * @return processing results with proper scoping
     */
    public static Map<String, Object> demonstrateVariableScopingWithAuditLevels(final String auditContext) {
        // Global-like variables (method scope) - Netflix production standard
        final var MAX_AUDIT_ENTRIES = 10000; // final var for constants
        final var AUDIT_RETENTION_HOURS = 8760; // 1 year in hours
        final var CRITICAL_ALERT_THRESHOLD = 5; // Threshold for critical alerts
        
        // Local variables with type inference
        var auditLogs = new java.util.ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>
        var currentAuditCount = 0; // int
        var totalAuditTime = 0L; // long
        
        // Nested scope demonstration with audit levels
        {
            var localAuditLevel = "Processing audit"; // String
            var localSeverityLevel = 0; // int
            
            // Type inference with wrapper classes in local scope
            var localSeverityWrapper = Integer.valueOf(localSeverityLevel); // Integer
            var localAuditEnabled = Boolean.valueOf(true); // Boolean
            var localAuditScore = Double.valueOf(95.0); // Double
            
            auditLogs.add(Map.of(
                "scope", "local",
                "auditLevel", localAuditLevel,
                "severityLevel", localSeverityWrapper,
                "auditEnabled", localAuditEnabled,
                "auditScore", localAuditScore
            ));
        }
        
        // Loop scope with type inference and audit levels
        for (var i = 0; i < 5; i++) { // int
            var loopAuditLevel = "Audit iteration " + i; // String
            var loopSeverityWrapper = Integer.valueOf(i * 2); // Integer
            
            // Type inference with enum methods and audit levels
            var auditLevel = switch (i % 5) {
                case 0 -> INFO;
                case 1 -> WARNING;
                case 2 -> ERROR;
                case 3 -> CRITICAL;
                default -> SECURITY;
            }; // AuditLogLevel
            
            var auditInfo = Map.of(
                "iteration", loopSeverityWrapper,
                "auditLevel", loopAuditLevel,
                "enumLevel", auditLevel.name(),
                "severity", Integer.valueOf(auditLevel.getSeverity()),
                "requiresImmediateAttention", Boolean.valueOf(auditLevel.requiresImmediateAttention()),
                "requiresEscalation", Boolean.valueOf(auditLevel.requiresEscalation())
            );
            
            auditLogs.add(auditInfo);
            totalAuditTime += 500L; // Simulate audit processing time
        }
        
        // Final processing with type inference
        var finalResults = Map.of(
            "auditContext", auditContext,
            "totalAuditTime", Long.valueOf(totalAuditTime),
            "maxAuditEntries", Integer.valueOf(MAX_AUDIT_ENTRIES),
            "auditRetentionHours", Integer.valueOf(AUDIT_RETENTION_HOURS),
            "criticalAlertThreshold", Integer.valueOf(CRITICAL_ALERT_THRESHOLD),
            "auditLogs", auditLogs,
            "hasAuditData", Boolean.valueOf(!auditLogs.isEmpty())
        );
        
        return finalResults;
    }
    
    /**
     * Demonstrates wrapper class usage for audit log level operations
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> Java wrapper classes provide
     * null safety and type safety for audit operations, similar to TypeScript's
     * strict null checks but with runtime guarantees.</p>
     * 
     * @param auditData the audit data to process
     * @return processed data with wrapper classes
     */
    public static Map<String, Object> demonstrateWrapperClassesWithAuditLevels(final Map<String, Object> auditData) {
        // Wrapper class usage for null safety
        var auditLevelName = Optional.ofNullable(auditData.get("auditLevel"))
            .map(Object::toString)
            .orElse("UNKNOWN"); // String
        
        var severityLevel = Optional.ofNullable(auditData.get("severityLevel"))
            .filter(Integer.class::isInstance)
            .map(Integer.class::cast)
            .orElse(Integer.valueOf(0)); // Integer wrapper
        
        var auditCount = Optional.ofNullable(auditData.get("auditCount"))
            .filter(Long.class::isInstance)
            .map(Long.class::cast)
            .orElse(Long.valueOf(0L)); // Long wrapper
        
        var isCritical = Optional.ofNullable(auditData.get("isCritical"))
            .filter(Boolean.class::isInstance)
            .map(Boolean.class::cast)
            .orElse(Boolean.FALSE); // Boolean wrapper
        
        var auditScore = Optional.ofNullable(auditData.get("auditScore"))
            .filter(Double.class::isInstance)
            .map(Double.class::cast)
            .orElse(Double.valueOf(0.0)); // Double wrapper
        
        // Type inference with wrapper class operations
        var calculatedRisk = severityLevel.intValue() * auditScore.doubleValue(); // double
        var calculatedRiskWrapper = Double.valueOf(calculatedRisk); // Double wrapper
        
        // Wrapper class collections for audit levels
        var auditMetrics = List.of(
            severityLevel,
            Long.valueOf(auditCount.longValue()),
            isCritical,
            calculatedRiskWrapper
        ); // List<Number>
        
        // Type inference with audit level validation
        var auditLevel = fromDisplayName(auditLevelName);
        var isValidAuditLevel = Boolean.valueOf(auditLevel != null);
        var requiresEscalation = auditLevel != null ? 
            Boolean.valueOf(auditLevel.requiresEscalation()) : Boolean.FALSE;
        
        return Map.of(
            "auditLevelName", auditLevelName,
            "severityLevel", severityLevel,
            "auditCount", auditCount,
            "isCritical", isCritical,
            "auditScore", auditScore,
            "calculatedRisk", calculatedRiskWrapper,
            "auditMetrics", auditMetrics,
            "isValidAuditLevel", isValidAuditLevel,
            "requiresEscalation", requiresEscalation,
            "hasAuditData", Boolean.valueOf(auditData != null && !auditData.isEmpty())
        );
    }
    
    /**
     * Demonstrates enum method chaining with audit log levels
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> This shows method chaining
     * similar to JavaScript/TypeScript, but with compile-time type safety for audit logging.</p>
     * 
     * @param auditLevel the audit level to process
     * @return chained method results
     */
    public static Map<String, Object> demonstrateMethodChainingWithAuditLevels(final AuditLogLevel auditLevel) {
        // Type inference with method chaining
        var auditInfo = Optional.ofNullable(auditLevel)
            .map(level -> Map.of(
                "name", level.name(),
                "displayName", level.getDisplayName(),
                "description", level.getDescription(),
                "severity", Integer.valueOf(level.getSeverity()),
                "isInformational", Boolean.valueOf(level.isInformational()),
                "isWarning", Boolean.valueOf(level.isWarning()),
                "isError", Boolean.valueOf(level.isError()),
                "isCritical", Boolean.valueOf(level.isCritical()),
                "isSecurityRelated", Boolean.valueOf(level.isSecurityRelated()),
                "requiresImmediateAttention", Boolean.valueOf(level.requiresImmediateAttention()),
                "requiresEscalation", Boolean.valueOf(level.requiresEscalation())
            ))
            .orElse(Map.of("error", "Invalid audit level")); // Map<String, Object>
        
        return auditInfo;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
