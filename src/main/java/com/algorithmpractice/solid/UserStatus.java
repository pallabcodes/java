package com.algorithmpractice.solid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade User Status Enumeration
 * 
 * <p>This enum represents the possible statuses of a user account in the Netflix ecosystem.
 * It demonstrates advanced Java type inference patterns, enum methods, and production-grade
 * practices expected at Netflix for SDE-2 Senior Backend Engineers.</p>
 * 
 * <p><strong>Key Features for Cross-Language Developers (TypeScript/Node.js background):</strong></p>
 * <ul>
 *   <li><strong>Type Safety:</strong> Unlike TypeScript unions, Java enums provide compile-time type safety</li>
 *   <li><strong>Method Support:</strong> Enums can have methods, unlike TypeScript string literals</li>
 *   <li><strong>Immutability:</strong> All enum values are immutable and thread-safe by default</li>
 *   <li><strong>Serialization:</strong> Built-in JSON serialization support for REST APIs</li>
 *   <li><strong>Pattern Matching:</strong> Switch expressions work seamlessly with enums</li>
 * </ul>
 * 
 * <p><strong>Netflix Production Standards:</strong></p>
 * <ul>
 *   <li>Comprehensive documentation for onboarding engineers from other languages</li>
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
public enum UserStatus {

    /**
     * User account is active and can access the system.
     */
    ACTIVE("Active", "User account is active and can access the system"),

    /**
     * User account has been deactivated and cannot access the system.
     */
    DEACTIVATED("Deactivated", "User account has been deactivated and cannot access the system"),

    /**
     * User account is suspended temporarily.
     */
    SUSPENDED("Suspended", "User account is temporarily suspended"),

    /**
     * User account is pending activation.
     */
    PENDING("Pending", "User account is pending activation"),

    /**
     * User account has been locked due to security concerns.
     */
    LOCKED("Locked", "User account has been locked due to security concerns");

    private final String displayName;
    private final String description;

    /**
     * Constructs a new UserStatus with the specified display name and description.
     * 
     * @param displayName the human-readable name for the status
     * @param description the detailed description of what the status means
     */
    UserStatus(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name for this status.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of this status.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this status allows the user to access the system.
     * 
     * @return true if the user can access the system, false otherwise
     */
    public boolean allowsAccess() {
        return this == ACTIVE;
    }

    /**
     * Checks if this status prevents the user from accessing the system.
     * 
     * @return true if the user cannot access the system, false otherwise
     */
    public boolean preventsAccess() {
        return this == DEACTIVATED || this == SUSPENDED || this == LOCKED;
    }

    /**
     * Checks if this status requires action from the user or administrator.
     * 
     * @return true if action is required, false otherwise
     */
    public boolean requiresAction() {
        return this == PENDING || this == SUSPENDED || this == LOCKED;
    }

    /**
     * Gets a UserStatus from its display name.
     * 
     * @param displayName the display name to search for
     * @return the matching UserStatus, or null if not found
     */
    public static UserStatus fromDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (final UserStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
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
     * Demonstrates type inference with enum collections and wrapper classes
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> This shows how Java's type inference
     * works with collections, similar to TypeScript's type inference but with compile-time safety.</p>
     * 
     * @return Map containing status information with type inference
     */
    public static Map<String, Object> demonstrateTypeInferenceWithEnums() {
        // Type inference with var keyword - Netflix production standard
        var statusList = Arrays.asList(ACTIVE, PENDING, SUSPENDED); // List<UserStatus>
        var statusMap = Map.of(
            "active", ACTIVE,
            "pending", PENDING,
            "suspended", SUSPENDED
        ); // Map<String, UserStatus>
        
        // Wrapper class usage for null safety - critical for production
        var activeCount = statusList.stream()
            .filter(UserStatus::allowsAccess)
            .count(); // long (primitive)
        
        var activeCountWrapper = Integer.valueOf((int) activeCount); // Integer (wrapper)
        
        // Type inference with Optional wrapper class
        var optionalStatus = Optional.of(ACTIVE); // Optional<UserStatus>
        var statusDescription = optionalStatus
            .map(UserStatus::getDescription)
            .orElse("Unknown status"); // String
        
        // Complex type inference with nested generics
        var statusMetadata = Map.of(
            "count", activeCountWrapper,
            "description", statusDescription,
            "statuses", statusList,
            "isActive", Boolean.valueOf(ACTIVE.allowsAccess())
        ); // Map<String, Object>
        
        return statusMetadata;
    }
    
    /**
     * Demonstrates explicit and implicit type casting with enums
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> Java requires explicit casting
     * for narrowing types, unlike TypeScript's structural typing.</p>
     * 
     * @param statusValue the status value to process
     * @return processed status information
     */
    public static Map<String, Object> demonstrateTypeCasting(final String statusValue) {
        // Implicit type casting (widening) - safe and automatic
        var status = fromDisplayName(statusValue); // UserStatus (nullable)
        var statusString = status != null ? status.name() : "UNKNOWN"; // String
        
        // Explicit type casting with wrapper classes
        var statusOrdinal = status != null ? Integer.valueOf(status.ordinal()) : Integer.valueOf(-1);
        var statusHashCode = status != null ? Integer.valueOf(status.hashCode()) : Integer.valueOf(0);
        
        // Type casting for arithmetic operations
        var numericValue = status != null ? Double.valueOf(status.ordinal() * 10.0) : Double.valueOf(0.0);
        var integerValue = numericValue.intValue(); // Explicit narrowing cast
        
        // Type casting with collections
        var statusArray = new UserStatus[]{ACTIVE, PENDING, SUSPENDED}; // UserStatus[]
        var statusList = Arrays.asList(statusArray); // List<UserStatus>
        
        // Type casting with streams and functional interfaces
        var statusNames = statusList.stream()
            .map(UserStatus::name) // String
            .collect(Collectors.toList()); // List<String>
        
        return Map.of(
            "status", statusString,
            "ordinal", statusOrdinal,
            "hashCode", statusHashCode,
            "numericValue", numericValue,
            "integerValue", Integer.valueOf(integerValue),
            "statusNames", statusNames
        );
    }
    
    /**
     * Demonstrates global vs local variable scoping with type inference
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> Java has block scoping similar to
     * TypeScript, but with more explicit type declarations and final keyword usage.</p>
     * 
     * @param processType the type of processing to perform
     * @return processing results with proper scoping
     */
    public static Map<String, Object> demonstrateVariableScoping(final String processType) {
        // Global-like variables (method scope) - Netflix production standard
        final var GLOBAL_PROCESSING_LIMIT = 1000; // final var for constants
        final var PROCESSING_TIMEOUT_MS = 5000L; // final var for timeouts
        
        // Local variables with type inference
        var processingResults = new java.util.ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>
        var currentBatch = 0; // int
        var totalProcessed = 0L; // long
        
        // Nested scope demonstration
        {
            var localScopeVariable = "Processing started"; // String
            var localCounter = 0; // int
            
            // Type inference with wrapper classes in local scope
            var localWrapper = Integer.valueOf(localCounter); // Integer
            var localBoolean = Boolean.valueOf(true); // Boolean
            var localDouble = Double.valueOf(3.14159); // Double
            
            processingResults.add(Map.of(
                "scope", "local",
                "message", localScopeVariable,
                "counter", localWrapper,
                "isActive", localBoolean,
                "pi", localDouble
            ));
        }
        
        // Loop scope with type inference
        for (var i = 0; i < 3; i++) { // int
            var loopVariable = "Iteration " + i; // String
            var loopWrapper = Integer.valueOf(i * 10); // Integer
            
            // Type inference with enum methods
            var status = i % 2 == 0 ? ACTIVE : PENDING; // UserStatus
            var statusInfo = Map.of(
                "iteration", loopWrapper,
                "message", loopVariable,
                "status", status.name(),
                "allowsAccess", Boolean.valueOf(status.allowsAccess())
            );
            
            processingResults.add(statusInfo);
            totalProcessed++;
        }
        
        // Final processing with type inference
        var finalResults = Map.of(
            "processType", processType,
            "totalProcessed", Long.valueOf(totalProcessed),
            "processingLimit", Integer.valueOf(GLOBAL_PROCESSING_LIMIT),
            "timeout", Long.valueOf(PROCESSING_TIMEOUT_MS),
            "results", processingResults,
            "completed", Boolean.valueOf(totalProcessed > 0)
        );
        
        return finalResults;
    }
    
    /**
     * Demonstrates wrapper class usage for production-grade null safety
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> Java wrapper classes provide
     * null safety similar to TypeScript's strict null checks, but with runtime guarantees.</p>
     * 
     * @param statusData the status data to process
     * @return processed data with wrapper classes
     */
    public static Map<String, Object> demonstrateWrapperClasses(final Map<String, Object> statusData) {
        // Wrapper class usage for null safety
        var statusName = Optional.ofNullable(statusData.get("status"))
            .map(Object::toString)
            .orElse("UNKNOWN"); // String
        
        var statusOrdinal = Optional.ofNullable(statusData.get("ordinal"))
            .filter(Integer.class::isInstance)
            .map(Integer.class::cast)
            .orElse(Integer.valueOf(0)); // Integer wrapper
        
        var statusCount = Optional.ofNullable(statusData.get("count"))
            .filter(Long.class::isInstance)
            .map(Long.class::cast)
            .orElse(Long.valueOf(0L)); // Long wrapper
        
        var isActive = Optional.ofNullable(statusData.get("isActive"))
            .filter(Boolean.class::isInstance)
            .map(Boolean.class::cast)
            .orElse(Boolean.FALSE); // Boolean wrapper
        
        var priority = Optional.ofNullable(statusData.get("priority"))
            .filter(Double.class::isInstance)
            .map(Double.class::cast)
            .orElse(Double.valueOf(0.0)); // Double wrapper
        
        // Type inference with wrapper class operations
        var calculatedValue = statusOrdinal.intValue() * priority.doubleValue(); // double
        var calculatedWrapper = Double.valueOf(calculatedValue); // Double wrapper
        
        // Wrapper class collections
        var wrapperList = List.of(
            statusOrdinal,
            Long.valueOf(statusCount.longValue()),
            isActive,
            calculatedWrapper
        ); // List<Number>
        
        return Map.of(
            "statusName", statusName,
            "statusOrdinal", statusOrdinal,
            "statusCount", statusCount,
            "isActive", isActive,
            "priority", priority,
            "calculatedValue", calculatedWrapper,
            "wrapperList", wrapperList,
            "hasData", Boolean.valueOf(statusData != null && !statusData.isEmpty())
        );
    }
    
    /**
     * Demonstrates enum method chaining with type inference
     * 
     * <p><strong>For TypeScript/Node.js developers:</strong> This shows method chaining
     * similar to JavaScript/TypeScript, but with compile-time type safety.</p>
     * 
     * @param status the status to process
     * @return chained method results
     */
    public static Map<String, Object> demonstrateMethodChaining(final UserStatus status) {
        // Type inference with method chaining
        var statusInfo = Optional.ofNullable(status)
            .map(s -> Map.of(
                "name", s.name(),
                "displayName", s.getDisplayName(),
                "description", s.getDescription(),
                "allowsAccess", Boolean.valueOf(s.allowsAccess()),
                "preventsAccess", Boolean.valueOf(s.preventsAccess()),
                "requiresAction", Boolean.valueOf(s.requiresAction())
            ))
            .orElse(Map.of("error", "Invalid status")); // Map<String, Object>
        
        return statusInfo;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
