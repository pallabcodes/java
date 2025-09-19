package com.algorithmpractice.solid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Data Access Type Enumeration
 * 
 * <p>This enum represents different types of data access operations in the Netflix ecosystem.
 * It demonstrates advanced Java type inference patterns, enum methods, and production-grade
 * practices expected at Netflix for SDE-2 Senior Backend Engineers.</p>
 * 
 * <p><strong>Key Features for Cross-Language Developers (TypeScript/Node.js background):</strong></p>
 * <ul>
 *   <li><strong>Enum Methods:</strong> Unlike TypeScript string literals, Java enums support methods</li>
 *   <li><strong>Type Safety:</strong> Compile-time type checking prevents invalid access types</li>
 *   <li><strong>Pattern Matching:</strong> Switch expressions work seamlessly with enums</li>
 *   <li><strong>Serialization:</strong> Built-in JSON serialization for REST API responses</li>
 *   <li><strong>Immutability:</strong> All enum values are immutable and thread-safe</li>
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
public enum DataAccessType {

    /**
     * Data viewed by the user.
     */
    VIEW("View", "Data was viewed by the user"),

    /**
     * Data exported by the user.
     */
    EXPORT("Export", "Data was exported by the user"),

    /**
     * Data shared by the user.
     */
    SHARE("Share", "Data was shared by the user"),

    /**
     * Data accessed by an administrator.
     */
    ADMIN_ACCESS("Admin Access", "Data was accessed by an administrator"),

    /**
     * Data accessed for audit purposes.
     */
    AUDIT("Audit", "Data was accessed for audit purposes"),

    /**
     * Data accessed for backup purposes.
     */
    BACKUP("Backup", "Data was accessed for backup purposes"),

    /**
     * Data accessed for system maintenance.
     */
    MAINTENANCE("Maintenance", "Data was accessed for system maintenance"),

    /**
     * Data accessed for analytics.
     */
    ANALYTICS("Analytics", "Data was accessed for analytics purposes"),

    /**
     * Data accessed for compliance reporting.
     */
    COMPLIANCE("Compliance", "Data was accessed for compliance reporting"),

    /**
     * Data accessed for debugging.
     */
    DEBUG("Debug", "Data was accessed for debugging purposes");

    private final String displayName;
    private final String description;

    /**
     * Constructs a new DataAccessType with the specified display name and description.
     * 
     * @param displayName the human-readable name for the data access type
     * @param description the detailed description of what the data access type means
     */
    DataAccessType(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name for this data access type.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of this data access type.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this data access type is user-initiated.
     * 
     * @return true if the data access type is user-initiated, false otherwise
     */
    public boolean isUserInitiated() {
        return this == VIEW || this == EXPORT || this == SHARE;
    }

    /**
     * Checks if this data access type is system-initiated.
     * 
     * @return true if the data access type is system-initiated, false otherwise
     */
    public boolean isSystemInitiated() {
        return this == BACKUP || this == MAINTENANCE || this == ANALYTICS || this == DEBUG;
    }

    /**
     * Checks if this data access type is administrative.
     * 
     * @return true if the data access type is administrative, false otherwise
     */
    public boolean isAdministrative() {
        return this == ADMIN_ACCESS || this == AUDIT || this == COMPLIANCE;
    }

    /**
     * Checks if this data access type requires special permissions.
     * 
     * @return true if the data access type requires special permissions, false otherwise
     */
    public boolean requiresSpecialPermissions() {
        return this == ADMIN_ACCESS || this == AUDIT || this == COMPLIANCE || this == DEBUG;
    }

    /**
     * Checks if this data access type is for compliance purposes.
     * 
     * @return true if the data access type is for compliance purposes, false otherwise
     */
    public boolean isComplianceRelated() {
        return this == AUDIT || this == COMPLIANCE;
    }

    /**
     * Gets a DataAccessType from its display name.
     * 
     * @param displayName the display name to search for
     * @return the matching DataAccessType, or null if not found
     */
    public static DataAccessType fromDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (final DataAccessType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
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
     * Demonstrates type inference with data access type collections and wrapper classes
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> This shows how Java's type inference
     * works with collections and enums, providing compile-time safety for data access patterns.</p>
     * 
     * @return Map containing access type information with type inference
     */
    public static Map<String, Object> demonstrateTypeInferenceWithAccessTypes() {
        // Type inference with var keyword - Netflix production standard
        var userInitiatedTypes = Arrays.asList(VIEW, EXPORT, SHARE); // List<DataAccessType>
        var systemInitiatedTypes = Arrays.asList(BACKUP, MAINTENANCE, ANALYTICS, DEBUG); // List<DataAccessType>
        var adminTypes = Arrays.asList(ADMIN_ACCESS, AUDIT, COMPLIANCE); // List<DataAccessType>
        
        // Type inference with Map collections
        var accessTypeMap = Map.of(
            "user", userInitiatedTypes,
            "system", systemInitiatedTypes,
            "admin", adminTypes
        ); // Map<String, List<DataAccessType>>
        
        // Wrapper class usage for counting and statistics
        var userInitiatedCount = Integer.valueOf(userInitiatedTypes.size()); // Integer wrapper
        var systemInitiatedCount = Integer.valueOf(systemInitiatedTypes.size()); // Integer wrapper
        var adminCount = Integer.valueOf(adminTypes.size()); // Integer wrapper
        
        // Type inference with stream operations and wrapper classes
        var totalTypes = userInitiatedTypes.stream()
            .mapToInt(type -> 1)
            .sum(); // int primitive
        
        var totalTypesWrapper = Integer.valueOf(totalTypes); // Integer wrapper
        
        // Type inference with Optional wrapper class
        var primaryAccessType = Optional.of(VIEW); // Optional<DataAccessType>
        var primaryDescription = primaryAccessType
            .map(DataAccessType::getDescription)
            .orElse("No primary access type"); // String
        
        // Complex type inference with nested generics and wrapper classes
        var accessMetadata = Map.of(
            "userInitiatedCount", userInitiatedCount,
            "systemInitiatedCount", systemInitiatedCount,
            "adminCount", adminCount,
            "totalTypes", totalTypesWrapper,
            "primaryDescription", primaryDescription,
            "accessTypes", accessTypeMap,
            "hasUserAccess", Boolean.valueOf(!userInitiatedTypes.isEmpty())
        ); // Map<String, Object>
        
        return accessMetadata;
    }
    
    /**
     * Demonstrates explicit and implicit type casting with data access types
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> Java requires explicit casting
     * for type narrowing, providing runtime safety for data access operations.</p>
     * 
     * @param accessTypeValue the access type value to process
     * @return processed access type information with type casting
     */
    public static Map<String, Object> demonstrateTypeCastingWithAccessTypes(final String accessTypeValue) {
        // Implicit type casting (widening) - safe and automatic
        var accessType = fromDisplayName(accessTypeValue); // DataAccessType (nullable)
        var accessTypeString = accessType != null ? accessType.name() : "UNKNOWN"; // String
        
        // Explicit type casting with wrapper classes
        var accessTypeOrdinal = accessType != null ? Integer.valueOf(accessType.ordinal()) : Integer.valueOf(-1);
        var accessTypeHashCode = accessType != null ? Integer.valueOf(accessType.hashCode()) : Integer.valueOf(0);
        
        // Type casting for permission calculations
        var permissionLevel = accessType != null ? Double.valueOf(accessType.ordinal() * 2.5) : Double.valueOf(0.0);
        var permissionLevelInt = permissionLevel.intValue(); // Explicit narrowing cast
        
        // Type casting with arrays and collections
        var accessTypeArray = new DataAccessType[]{VIEW, EXPORT, SHARE, ADMIN_ACCESS}; // DataAccessType[]
        var accessTypeList = Arrays.asList(accessTypeArray); // List<DataAccessType>
        
        // Type casting with streams and functional interfaces
        var accessTypeNames = accessTypeList.stream()
            .map(DataAccessType::name) // String
            .collect(Collectors.toList()); // List<String>
        
        // Type casting with wrapper class operations
        var permissionWrapper = Integer.valueOf(permissionLevelInt); // Integer wrapper
        var isHighPermission = Boolean.valueOf(permissionLevelInt > 5); // Boolean wrapper
        
        return Map.of(
            "accessType", accessTypeString,
            "ordinal", accessTypeOrdinal,
            "hashCode", accessTypeHashCode,
            "permissionLevel", permissionLevel,
            "permissionLevelInt", permissionWrapper,
            "isHighPermission", isHighPermission,
            "accessTypeNames", accessTypeNames
        );
    }
    
    /**
     * Demonstrates global vs local variable scoping with data access types
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> Java has block scoping similar to
     * TypeScript, but with explicit type declarations and final keyword usage for constants.</p>
     * 
     * @param accessContext the context of data access
     * @return processing results with proper scoping
     */
    public static Map<String, Object> demonstrateVariableScopingWithAccessTypes(final String accessContext) {
        // Global-like variables (method scope) - Netflix production standard
        final var MAX_ACCESS_ATTEMPTS = 100; // final var for constants
        final var ACCESS_TIMEOUT_MS = 30000L; // final var for timeouts
        final var AUDIT_RETENTION_DAYS = 2555; // 7 years in days
        
        // Local variables with type inference
        var accessLogs = new java.util.ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>
        var currentAccessCount = 0; // int
        var totalAccessTime = 0L; // long
        
        // Nested scope demonstration with access types
        {
            var localAccessType = "Data processing"; // String
            var localPermissionLevel = 0; // int
            
            // Type inference with wrapper classes in local scope
            var localPermissionWrapper = Integer.valueOf(localPermissionLevel); // Integer
            var localAccessAllowed = Boolean.valueOf(true); // Boolean
            var localAccessScore = Double.valueOf(85.5); // Double
            
            accessLogs.add(Map.of(
                "scope", "local",
                "accessType", localAccessType,
                "permissionLevel", localPermissionWrapper,
                "accessAllowed", localAccessAllowed,
                "accessScore", localAccessScore
            ));
        }
        
        // Loop scope with type inference and access types
        for (var i = 0; i < 4; i++) { // int
            var loopAccessType = "Access iteration " + i; // String
            var loopPermissionWrapper = Integer.valueOf(i * 5); // Integer
            
            // Type inference with enum methods and access types
            var accessType = switch (i % 4) {
                case 0 -> VIEW;
                case 1 -> EXPORT;
                case 2 -> ADMIN_ACCESS;
                default -> AUDIT;
            }; // DataAccessType
            
            var accessInfo = Map.of(
                "iteration", loopPermissionWrapper,
                "accessType", loopAccessType,
                "enumType", accessType.name(),
                "isUserInitiated", Boolean.valueOf(accessType.isUserInitiated()),
                "requiresSpecialPermissions", Boolean.valueOf(accessType.requiresSpecialPermissions())
            );
            
            accessLogs.add(accessInfo);
            totalAccessTime += 1000L; // Simulate access time
        }
        
        // Final processing with type inference
        var finalResults = Map.of(
            "accessContext", accessContext,
            "totalAccessTime", Long.valueOf(totalAccessTime),
            "maxAccessAttempts", Integer.valueOf(MAX_ACCESS_ATTEMPTS),
            "accessTimeout", Long.valueOf(ACCESS_TIMEOUT_MS),
            "auditRetentionDays", Integer.valueOf(AUDIT_RETENTION_DAYS),
            "accessLogs", accessLogs,
            "hasAccessData", Boolean.valueOf(!accessLogs.isEmpty())
        );
        
        return finalResults;
    }
    
    /**
     * Demonstrates wrapper class usage for data access type operations
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> Java wrapper classes provide
     * null safety and type safety for data access operations, similar to TypeScript's
     * strict null checks but with runtime guarantees.</p>
     * 
     * @param accessData the access data to process
     * @return processed data with wrapper classes
     */
    public static Map<String, Object> demonstrateWrapperClassesWithAccessTypes(final Map<String, Object> accessData) {
        // Wrapper class usage for null safety
        var accessTypeName = Optional.ofNullable(accessData.get("accessType"))
            .map(Object::toString)
            .orElse("UNKNOWN"); // String
        
        var accessLevel = Optional.ofNullable(accessData.get("accessLevel"))
            .filter(Integer.class::isInstance)
            .map(Integer.class::cast)
            .orElse(Integer.valueOf(0)); // Integer wrapper
        
        var accessCount = Optional.ofNullable(accessData.get("accessCount"))
            .filter(Long.class::isInstance)
            .map(Long.class::cast)
            .orElse(Long.valueOf(0L)); // Long wrapper
        
        var isAuditable = Optional.ofNullable(accessData.get("isAuditable"))
            .filter(Boolean.class::isInstance)
            .map(Boolean.class::cast)
            .orElse(Boolean.FALSE); // Boolean wrapper
        
        var accessScore = Optional.ofNullable(accessData.get("accessScore"))
            .filter(Double.class::isInstance)
            .map(Double.class::cast)
            .orElse(Double.valueOf(0.0)); // Double wrapper
        
        // Type inference with wrapper class operations
        var calculatedRisk = accessLevel.intValue() * accessScore.doubleValue(); // double
        var calculatedRiskWrapper = Double.valueOf(calculatedRisk); // Double wrapper
        
        // Wrapper class collections for data access types
        var accessMetrics = List.of(
            accessLevel,
            Long.valueOf(accessCount.longValue()),
            isAuditable,
            calculatedRiskWrapper
        ); // List<Number>
        
        // Type inference with access type validation
        var accessType = fromDisplayName(accessTypeName);
        var isValidAccessType = Boolean.valueOf(accessType != null);
        var requiresSpecialPermissions = accessType != null ? 
            Boolean.valueOf(accessType.requiresSpecialPermissions()) : Boolean.FALSE;
        
        return Map.of(
            "accessTypeName", accessTypeName,
            "accessLevel", accessLevel,
            "accessCount", accessCount,
            "isAuditable", isAuditable,
            "accessScore", accessScore,
            "calculatedRisk", calculatedRiskWrapper,
            "accessMetrics", accessMetrics,
            "isValidAccessType", isValidAccessType,
            "requiresSpecialPermissions", requiresSpecialPermissions,
            "hasAccessData", Boolean.valueOf(accessData != null && !accessData.isEmpty())
        );
    }
    
    /**
     * Demonstrates enum method chaining with data access types
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> This shows method chaining
     * similar to JavaScript/TypeScript, but with compile-time type safety for data access.</p>
     * 
     * @param accessType the access type to process
     * @return chained method results
     */
    public static Map<String, Object> demonstrateMethodChainingWithAccessTypes(final DataAccessType accessType) {
        // Type inference with method chaining
        var accessInfo = Optional.ofNullable(accessType)
            .map(type -> Map.of(
                "name", type.name(),
                "displayName", type.getDisplayName(),
                "description", type.getDescription(),
                "isUserInitiated", Boolean.valueOf(type.isUserInitiated()),
                "isSystemInitiated", Boolean.valueOf(type.isSystemInitiated()),
                "isAdministrative", Boolean.valueOf(type.isAdministrative()),
                "requiresSpecialPermissions", Boolean.valueOf(type.requiresSpecialPermissions()),
                "isComplianceRelated", Boolean.valueOf(type.isComplianceRelated())
            ))
            .orElse(Map.of("error", "Invalid access type")); // Map<String, Object>
        
        return accessInfo;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
