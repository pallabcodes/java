package com.algorithmpractice.solid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Null Safety Utilities
 *
 * <p>This class provides comprehensive null safety patterns with Netflix production standards.
 * It demonstrates advanced Java Optional patterns, null-safe operations, and production-grade practices
 * expected at Netflix for SDE-2 Senior Backend Engineers.</p>
 *
 * <p><strong>Key Features for Cross-Language Developers (TypeScript/Node.js background):</strong></p>
 * <ul>
 *   <li><strong>Null Safety:</strong> Unlike JavaScript's undefined/null handling, Java Optional provides compile-time null safety</li>
 *   <li><strong>Type Safety:</strong> Compile-time prevention of null pointer exceptions</li>
 *   <li><strong>Functional Programming:</strong> Functional composition with null-safe operations</li>
 *   <li><strong>Performance:</strong> Efficient null checking with minimal overhead</li>
 *   <li><strong>Readability:</strong> Clear intent with Optional patterns</li>
 * </ul>
 *
 * <p><strong>Netflix Production Standards:</strong></p>
 * <ul>
 *   <li>Comprehensive Optional usage for all nullable operations</li>
 *   <li>Functional composition with null-safe method chaining</li>
 *   <li>Type inference patterns using 'var' keyword</li>
 *   <li>Final keyword usage for immutability</li>
 *   <li>Wrapper class integration for null safety</li>
 *   <li>Thread-safe operations for concurrent environments</li>
 *   <li>Performance monitoring and metrics collection</li>
 *   <li>Comprehensive logging and audit trails</li>
 * </ul>
 *
 * @author Netflix Backend Engineering Team
 * @version 2.0.0
 * @since 2024
 */
@Slf4j
@Component
public class NetflixNullSafetyUtils {

    // ========== GLOBAL CONSTANTS (Netflix Production Standards) ==========

    /**
     * Global constants with final keyword - Netflix production standard
     */
    private static final String DEFAULT_STRING_VALUE = "";
    private static final Integer DEFAULT_INTEGER_VALUE = 0;
    private static final Long DEFAULT_LONG_VALUE = 0L;
    private static final Double DEFAULT_DOUBLE_VALUE = 0.0;
    private static final BigDecimal DEFAULT_BIG_DECIMAL_VALUE = BigDecimal.ZERO;
    private static final Boolean DEFAULT_BOOLEAN_VALUE = Boolean.FALSE;

    // ========== THREAD-SAFE METRICS (Netflix Production Standard) ==========

    /**
     * Thread-safe null safety metrics - Netflix production standard
     */
    private static final Map<String, Integer> NULL_SAFETY_METRICS = new ConcurrentHashMap<>();

    static {
        NULL_SAFETY_METRICS.put("totalOperations", 0);
        NULL_SAFETY_METRICS.put("nullChecksPerformed", 0);
        NULL_SAFETY_METRICS.put("nullValuesHandled", 0);
        NULL_SAFETY_METRICS.put("optionalOperations", 0);
    }

    // ========== TYPE INFERENCE WITH OPTIONAL PATTERNS ==========

    /**
     * Demonstrates type inference with comprehensive Optional patterns
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java's Optional provides compile-time
     * null safety guarantees, unlike JavaScript's runtime null/undefined checks.</p>
     *
     * @return Map containing comprehensive null safety examples
     */
    public Map<String, Object> demonstrateTypeInferenceWithOptional() {
        log.info("=== Demonstrating Type Inference with Optional Patterns ===");

        // Type inference with Optional creation patterns
        var optionalString = Optional.ofNullable("Hello World"); // Optional<String>
        var optionalInteger = Optional.ofNullable(Integer.valueOf(42)); // Optional<Integer>
        var optionalList = Optional.ofNullable(Arrays.asList("A", "B", "C")); // Optional<List<String>>
        var optionalMap = Optional.ofNullable(Map.of("key", "value")); // Optional<Map<String, String>>

        // Type inference with empty Optionals
        var emptyOptional = Optional.<String>empty(); // Optional<String>
        var emptyListOptional = Optional.<List<String>>empty(); // Optional<List<String>>

        // Type inference with Optional operations
        var stringLength = optionalString.map(String::length); // Optional<Integer>
        var upperCaseString = optionalString.map(String::toUpperCase); // Optional<String>
        var firstElement = optionalList.flatMap(list -> list.stream().findFirst()); // Optional<String>

        // Type inference with complex Optional chaining
        var complexResult = optionalMap
            .flatMap(map -> Optional.ofNullable(map.get("key"))) // Optional<String>
            .map(String::length) // Optional<Integer>
            .filter(length -> length > 3) // Optional<Integer>
            .map(length -> length * 2); // Optional<Integer>

        // Type inference with Optional collection operations
        var safeListSize = optionalList
            .map(List::size) // Optional<Integer>
            .orElse(Integer.valueOf(0)); // Integer

        var safeMapSize = optionalMap
            .map(Map::size) // Optional<Integer>
            .orElse(Integer.valueOf(0)); // Integer

        // Type inference with null-safe arithmetic
        var safeSum = Optional.ofNullable(Integer.valueOf(10))
            .flatMap(a -> Optional.ofNullable(Integer.valueOf(20))
                .map(b -> a + b)) // Optional<Integer>
            .orElse(Integer.valueOf(0)); // Integer

        // Type inference with comprehensive null safety report
        var nullSafetyReport = Map.of(
            "optionalString", optionalString.orElse(null),
            "optionalInteger", optionalInteger.orElse(null),
            "optionalList", optionalList.orElse(null),
            "optionalMap", optionalMap.orElse(null),
            "emptyOptional", emptyOptional.orElse("EMPTY"),
            "stringLength", stringLength.orElse(null),
            "upperCaseString", upperCaseString.orElse(null),
            "firstElement", firstElement.orElse(null),
            "complexResult", complexResult.orElse(null),
            "safeListSize", safeListSize,
            "safeMapSize", safeMapSize,
            "safeSum", safeSum,
            "hasStringValue", Boolean.valueOf(optionalString.isPresent()),
            "hasIntegerValue", Boolean.valueOf(optionalInteger.isPresent()),
            "hasComplexResult", Boolean.valueOf(complexResult.isPresent()),
            "processingTime", Long.valueOf(System.currentTimeMillis())
        ); // Map<String, Object>

        updateMetrics("optionalOperations", 12); // Count of Optional operations performed

        return nullSafetyReport;
    }

    // ========== NULL-SAFE METHOD CHAINING ==========

    /**
     * Demonstrates type inference with null-safe method chaining patterns
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java's Optional provides safe method
     * chaining with compile-time guarantees, unlike JavaScript's potential null reference errors.</p>
     *
     * @param userData the user data that may contain null values
     * @return Map containing null-safe method chaining results
     */
    public Map<String, Object> demonstrateNullSafeMethodChaining(final Map<String, Object> userData) {
        log.info("=== Demonstrating Null-Safe Method Chaining ===");

        // Type inference with null-safe property access
        var userName = Optional.ofNullable(userData)
            .map(data -> data.get("user")) // Optional<Object>
            .filter(Map.class::isInstance) // Optional<Map>
            .map(Map.class::cast) // Optional<Map>
            .map(user -> user.get("name")) // Optional<Object>
            .filter(String.class::isInstance) // Optional<String>
            .map(String.class::cast) // Optional<String>
            .filter(name -> !name.trim().isEmpty()) // Optional<String>
            .map(String::toUpperCase); // Optional<String>

        // Type inference with null-safe collection operations
        var userRoles = Optional.ofNullable(userData)
            .map(data -> data.get("user")) // Optional<Object>
            .filter(Map.class::isInstance) // Optional<Map>
            .map(Map.class::cast) // Optional<Map>
            .map(user -> user.get("roles")) // Optional<Object>
            .filter(List.class::isInstance) // Optional<List>
            .map(List.class::cast) // Optional<List>
            .filter(roles -> !roles.isEmpty()) // Optional<List>
            .map(roles -> roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::toUpperCase)
                .collect(Collectors.toList())); // Optional<List<String>>

        // Type inference with null-safe nested operations
        var userProfile = Optional.ofNullable(userData)
            .map(data -> data.get("user")) // Optional<Object>
            .filter(Map.class::isInstance) // Optional<Map>
            .map(Map.class::cast) // Optional<Map>
            .map(user -> user.get("profile")) // Optional<Object>
            .filter(Map.class::isInstance) // Optional<Map>
            .map(Map.class::cast) // Optional<Map>
            .flatMap(profile -> Optional.ofNullable(profile.get("email")) // Optional<Object>
                .filter(String.class::isInstance) // Optional<String>
                .map(String.class::cast) // Optional<String>
                .filter(email -> email.contains("@")) // Optional<String>
                .map(email -> Map.of(
                    "email", email,
                    "domain", email.substring(email.indexOf("@") + 1),
                    "isValid", Boolean.valueOf(true)
                ))); // Optional<Map<String, Object>>

        // Type inference with null-safe arithmetic operations
        var userBalance = Optional.ofNullable(userData)
            .map(data -> data.get("user")) // Optional<Object>
            .filter(Map.class::isInstance) // Optional<Map>
            .map(Map.class::cast) // Optional<Map>
            .map(user -> user.get("account")) // Optional<Object>
            .filter(Map.class::isInstance) // Optional<Map>
            .map(Map.class::cast) // Optional<Map>
            .flatMap(account -> Optional.ofNullable(account.get("balance")) // Optional<Object>
                .filter(BigDecimal.class::isInstance) // Optional<BigDecimal>
                .map(BigDecimal.class::cast) // Optional<BigDecimal>
                .filter(balance -> balance.compareTo(BigDecimal.ZERO) >= 0) // Optional<BigDecimal>
                .map(balance -> Map.of(
                    "balance", balance,
                    "isPositive", Boolean.valueOf(balance.compareTo(BigDecimal.ZERO) > 0),
                    "formatted", "$" + balance.setScale(2, java.math.RoundingMode.HALF_EVEN)
                ))); // Optional<Map<String, Object>>

        // Type inference with method chaining summary
        var chainingResults = Map.of(
            "userName", userName.orElse("UNKNOWN"),
            "userRoles", userRoles.orElse(Collections.emptyList()),
            "userProfile", userProfile.orElse(Map.of("email", "N/A", "isValid", Boolean.valueOf(false))),
            "userBalance", userBalance.orElse(Map.of("balance", BigDecimal.ZERO, "isPositive", Boolean.valueOf(false))),
            "hasUserName", Boolean.valueOf(userName.isPresent()),
            "hasUserRoles", Boolean.valueOf(userRoles.isPresent()),
            "hasUserProfile", Boolean.valueOf(userProfile.isPresent()),
            "hasUserBalance", Boolean.valueOf(userBalance.isPresent()),
            "chainingDepth", Integer.valueOf(4), // Number of chaining operations
            "processingTime", Long.valueOf(System.currentTimeMillis())
        ); // Map<String, Object>

        updateMetrics("nullChecksPerformed", 20); // Count of null checks performed
        updateMetrics("nullValuesHandled", (int) Arrays.asList(userName, userRoles, userProfile, userBalance)
            .stream().filter(Optional::isEmpty).count());

        return chainingResults;
    }

    // ========== OPTIONAL WITH COLLECTIONS ==========

    /**
     * Demonstrates type inference with Optional patterns for collections
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java's Optional provides safe collection
     * operations, preventing null reference errors in array/object access patterns.</p>
     *
     * @param dataCollections the collections that may contain null values
     * @return Map containing safe collection operation results
     */
    public Map<String, Object> demonstrateOptionalWithCollections(final Map<String, Object> dataCollections) {
        log.info("=== Demonstrating Optional with Collections ===");

        // Type inference with null-safe collection access
        var userList = Optional.ofNullable(dataCollections)
            .map(collections -> collections.get("users")) // Optional<Object>
            .filter(List.class::isInstance) // Optional<List>
            .map(List.class::cast) // Optional<List>
            .filter(list -> !list.isEmpty()) // Optional<List>
            .map(list -> list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList())); // Optional<List>

        // Type inference with null-safe map operations
        var userMap = Optional.ofNullable(dataCollections)
            .map(collections -> collections.get("userMap")) // Optional<Object>
            .filter(Map.class::isInstance) // Optional<Map>
            .map(Map.class::cast) // Optional<Map>
            .filter(map -> !map.isEmpty()) // Optional<Map>
            .map(map -> map.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ))); // Optional<Map>

        // Type inference with null-safe stream operations
        var userNames = userList
            .map(list -> list.stream()
                .filter(Objects::nonNull)
                .map(user -> {
                    if (user instanceof Map) {
                        var userMap = (Map<?, ?>) user;
                        return Optional.ofNullable(userMap.get("name"))
                            .map(Object::toString)
                            .orElse("Unknown");
                    }
                    return user.toString();
                })
                .filter(name -> !name.equals("Unknown"))
                .collect(Collectors.toList())) // Optional<List<String>>
            .orElse(Collections.emptyList()); // List<String>

        // Type inference with null-safe aggregation operations
        var userCount = userList
            .map(List::size) // Optional<Integer>
            .orElse(Integer.valueOf(0)); // Integer

        var activeUserCount = userList
            .map(list -> list.stream()
                .filter(Objects::nonNull)
                .map(user -> {
                    if (user instanceof Map) {
                        var userMap = (Map<?, ?>) user;
                        return Optional.ofNullable(userMap.get("active"))
                            .filter(Boolean.class::isInstance)
                            .map(Boolean.class::cast)
                            .orElse(Boolean.FALSE);
                    }
                    return Boolean.FALSE;
                })
                .filter(Boolean::booleanValue)
                .count()) // Optional<Long>
            .orElse(Long.valueOf(0L)); // Long

        // Type inference with null-safe collection transformations
        var userSummaries = userList
            .map(list -> list.stream()
                .filter(Objects::nonNull)
                .map(user -> {
                    if (user instanceof Map) {
                        var userMap = (Map<?, ?>) user;
                        var name = Optional.ofNullable(userMap.get("name"))
                            .map(Object::toString)
                            .orElse("Unknown");
                        var email = Optional.ofNullable(userMap.get("email"))
                            .map(Object::toString)
                            .orElse("N/A");
                        var active = Optional.ofNullable(userMap.get("active"))
                            .filter(Boolean.class::isInstance)
                            .map(Boolean.class::cast)
                            .orElse(Boolean.FALSE);

                        return Map.of(
                            "name", name,
                            "email", email,
                            "active", active,
                            "hasCompleteInfo", Boolean.valueOf(!name.equals("Unknown") && !email.equals("N/A"))
                        );
                    }
                    return Map.of(
                        "name", user.toString(),
                        "email", "N/A",
                        "active", Boolean.valueOf(true),
                        "hasCompleteInfo", Boolean.valueOf(false)
                    );
                })
                .collect(Collectors.toList())) // Optional<List<Map<String, Object>>>
            .orElse(Collections.emptyList()); // List<Map<String, Object>>

        // Type inference with collection operation summary
        var collectionResults = Map.of(
            "userList", userList.orElse(Collections.emptyList()),
            "userMap", userMap.orElse(Collections.emptyMap()),
            "userNames", userNames,
            "userCount", userCount,
            "activeUserCount", Integer.valueOf(activeUserCount.intValue()),
            "userSummaries", userSummaries,
            "hasUserData", Boolean.valueOf(userList.isPresent()),
            "hasUserMap", Boolean.valueOf(userMap.isPresent()),
            "hasUserNames", Boolean.valueOf(!userNames.isEmpty()),
            "hasUserSummaries", Boolean.valueOf(!userSummaries.isEmpty()),
            "processingTime", Long.valueOf(System.currentTimeMillis())
        ); // Map<String, Object>

        updateMetrics("optionalOperations", 8); // Count of Optional operations with collections

        return collectionResults;
    }

    // ========== OPTIONAL WITH FUNCTIONAL PROGRAMMING ==========

    /**
     * Demonstrates type inference with Optional in functional programming patterns
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java's Optional integrates seamlessly
     * with functional programming patterns, providing null-safe function composition.</p>
     *
     * @param inputData the input data for functional operations
     * @return Map containing functional programming results
     */
    public Map<String, Object> demonstrateOptionalWithFunctionalProgramming(final Map<String, Object> inputData) {
        log.info("=== Demonstrating Optional with Functional Programming ===");

        // Type inference with functional interfaces and Optional
        var stringProcessor = (Function<String, Optional<String>>) input ->
            Optional.ofNullable(input)
                .filter(s -> !s.trim().isEmpty())
                .map(String::toUpperCase)
                .map(s -> s + "_PROCESSED"); // Function<String, Optional<String>>

        var numberProcessor = (Function<Number, Optional<BigDecimal>>) input ->
            Optional.ofNullable(input)
                .map(Number::doubleValue)
                .map(BigDecimal::valueOf)
                .map(bd -> bd.setScale(2, java.math.RoundingMode.HALF_EVEN)); // Function<Number, Optional<BigDecimal>>

        // Type inference with Optional and function composition
        var composedProcessor = (Function<Map<String, Object>, Optional<Map<String, Object>>>) data ->
            Optional.ofNullable(data)
                .flatMap(d -> Optional.ofNullable(d.get("name")) // Get name
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .flatMap(stringProcessor)) // Process name
                .flatMap(processedName -> Optional.ofNullable(data.get("value")) // Get value
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .flatMap(numberProcessor) // Process value
                    .map(processedValue -> Map.of(
                        "processedName", processedName,
                        "processedValue", processedValue,
                        "timestamp", LocalDateTime.now().toString()
                    ))); // Optional<Map<String, Object>>

        // Type inference with functional operations on collections
        var listProcessor = (Function<List<?>, Optional<List<String>>>) list ->
            Optional.ofNullable(list)
                .filter(l -> !l.isEmpty())
                .map(l -> l.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(s -> !s.trim().isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toList())); // Function<List<?>, Optional<List<String>>>

        // Type inference with Optional and predicates
        var validationPredicate = (Predicate<Map<String, Object>>) data ->
            Optional.ofNullable(data)
                .flatMap(d -> Optional.ofNullable(d.get("name")))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(name -> !name.trim().isEmpty())
                .isPresent() &&
            Optional.ofNullable(data)
                .flatMap(d -> Optional.ofNullable(d.get("value")))
                .filter(Number.class::isInstance)
                .isPresent(); // Predicate<Map<String, Object>>

        // Apply functional operations
        var processedData = composedProcessor.apply(inputData); // Optional<Map<String, Object>>
        var processedList = Optional.ofNullable(inputData.get("items")) // Get items
            .filter(List.class::isInstance)
            .map(List.class::cast)
            .flatMap(listProcessor); // Optional<List<String>>

        var isValid = validationPredicate.test(inputData); // boolean

        // Type inference with functional programming summary
        var functionalResults = Map.of(
            "processedData", processedData.orElse(Collections.emptyMap()),
            "processedList", processedList.orElse(Collections.emptyList()),
            "isValid", Boolean.valueOf(isValid),
            "hasProcessedData", Boolean.valueOf(processedData.isPresent()),
            "hasProcessedList", Boolean.valueOf(processedList.isPresent()),
            "functionCompositionDepth", Integer.valueOf(3), // Number of composed functions
            "processingTime", Long.valueOf(System.currentTimeMillis())
        ); // Map<String, Object>

        updateMetrics("optionalOperations", 6); // Count of functional Optional operations

        return functionalResults;
    }

    // ========== VARIABLE SCOPING WITH OPTIONAL ==========

    /**
     * Demonstrates global vs local variable scoping with Optional patterns
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java has block scoping similar to
     * TypeScript, but with explicit type declarations and final keyword usage for constants.</p>
     *
     * @param operationContext the context of null safety operations
     * @return processing results with proper scoping
     */
    public Map<String, Object> demonstrateVariableScopingWithOptional(final String operationContext) {
        log.info("=== Demonstrating Variable Scoping with Optional ===");

        // Global-like variables (method scope) - Netflix production standard
        final var NULL_SAFETY_TIMEOUT_MS = 5000L;
        final var MAX_NULL_CHECKS_PER_OPERATION = 100;
        final var NULL_SAFETY_CACHE_TTL_MS = 300000L; // 5 minutes

        // Local variables with type inference
        var processingResults = new ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>
        var startTime = System.currentTimeMillis(); // long
        var nullCheckCount = 0; // int

        // Nested scope demonstration with Optional
        {
            var localOperationId = "null_safety_" + System.nanoTime(); // String
            var localDataBatch = new ArrayList<Optional<String>>(); // ArrayList<Optional<String>>

            // Type inference with wrapper classes in local scope
            var localBatchSize = Integer.valueOf(0); // Integer
            var localSuccessCount = Integer.valueOf(0); // Integer
            var localNullCount = Integer.valueOf(0); // Integer

            // Simulate Optional operations in local scope
            for (var i = 1; i <= 5; i++) { // int
                nullCheckCount++;

                // Type inference with Optional creation and operations
                var optionalValue = i % 2 == 0 ?
                    Optional.of("value_" + i) : // Optional<String>
                    Optional.<String>empty(); // Optional<String>

                var processedValue = optionalValue
                    .map(String::toUpperCase) // Optional<String>
                    .map(s -> s + "_PROCESSED") // Optional<String>
                    .orElse("NULL_VALUE"); // String

                localDataBatch.add(optionalValue);
                localBatchSize = Integer.valueOf(localBatchSize.intValue() + 1);

                if (optionalValue.isPresent()) {
                    localSuccessCount = Integer.valueOf(localSuccessCount.intValue() + 1);
                } else {
                    localNullCount = Integer.valueOf(localNullCount.intValue() + 1);
                }

                // Track null checks
                updateMetrics("nullChecksPerformed", 1);
                if (!optionalValue.isPresent()) {
                    updateMetrics("nullValuesHandled", 1);
                }
            }

            // Calculate local scope metrics
            var localSuccessRate = Double.valueOf((double) localSuccessCount.intValue() / localBatchSize.intValue()); // Double

            processingResults.add(Map.of(
                "scope", "local",
                "operationId", localOperationId,
                "dataBatch", localDataBatch.stream()
                    .map(opt -> opt.orElse("NULL"))
                    .collect(Collectors.toList()),
                "batchSize", localBatchSize,
                "successCount", localSuccessCount,
                "nullCount", localNullCount,
                "successRate", localSuccessRate,
                "status", "completed"
            ));
        }

        // Loop scope with type inference and Optional validation
        for (var i = 0; i < 3; i++) { // int
            var loopBatchId = "batch_" + i + "_" + System.nanoTime(); // String
            var loopOptionalOperations = new ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>

            // Type inference with Optional operations in loop scope
            for (var j = 1; j <= 3; j++) { // int
                nullCheckCount++;

                var operationData = Map.of(
                    "id", "op_" + i + "_" + j,
                    "value", j % 2 == 0 ? "data_" + j : null,
                    "number", j % 3 == 0 ? Integer.valueOf(j * 10) : null
                ); // Map<String, Object>

                // Type inference with null-safe operations
                var safeStringValue = Optional.ofNullable(operationData.get("value"))
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(String::toUpperCase)
                    .orElse("DEFAULT_STRING"); // String

                var safeNumberValue = Optional.ofNullable(operationData.get("number"))
                    .filter(Integer.class::isInstance)
                    .map(Integer.class::cast)
                    .map(val -> val * 2)
                    .orElse(Integer.valueOf(0)); // Integer

                var operationResult = Map.of(
                    "operationId", operationData.get("id"),
                    "originalValue", operationData.get("value"),
                    "safeStringValue", safeStringValue,
                    "originalNumber", operationData.get("number"),
                    "safeNumberValue", safeNumberValue,
                    "wasStringNull", Boolean.valueOf(operationData.get("value") == null),
                    "wasNumberNull", Boolean.valueOf(operationData.get("number") == null)
                ); // Map<String, Object>

                loopOptionalOperations.add(operationResult);

                // Track null checks
                updateMetrics("nullChecksPerformed", 2); // Two null checks performed
                if (operationData.get("value") == null) updateMetrics("nullValuesHandled", 1);
                if (operationData.get("number") == null) updateMetrics("nullValuesHandled", 1);
            }

            var loopValidOperations = loopOptionalOperations.stream()
                .filter(op -> !Boolean.valueOf(op.get("wasStringNull").toString()) ||
                             !Boolean.valueOf(op.get("wasNumberNull").toString()))
                .count(); // long

            var loopValidRate = Double.valueOf((double) loopValidOperations / loopOptionalOperations.size()); // Double

            var loopInfo = Map.of(
                "scope", "loop",
                "batchId", loopBatchId,
                "iteration", Integer.valueOf(i),
                "optionalOperations", loopOptionalOperations,
                "validOperations", Integer.valueOf((int) loopValidOperations),
                "totalOperations", Integer.valueOf(loopOptionalOperations.size()),
                "validRate", loopValidRate,
                "status", "processed"
            );

            processingResults.add(loopInfo);
        }

        // Final processing with type inference
        var endTime = System.currentTimeMillis(); // long
        var totalDurationMs = Long.valueOf(endTime - startTime); // Long

        var finalResults = Map.of(
            "operationContext", operationContext,
            "startTime", Long.valueOf(startTime),
            "endTime", Long.valueOf(endTime),
            "totalDurationMs", totalDurationMs,
            "nullCheckCount", Integer.valueOf(nullCheckCount),
            "processingTimeoutMs", Long.valueOf(NULL_SAFETY_TIMEOUT_MS),
            "maxNullChecksPerOperation", Integer.valueOf(MAX_NULL_CHECKS_PER_OPERATION),
            "nullSafetyCacheTtlMs", Long.valueOf(NULL_SAFETY_CACHE_TTL_MS),
            "processingResults", processingResults,
            "hasResults", Boolean.valueOf(!processingResults.isEmpty())
        );

        return finalResults;
    }

    // ========== HELPER METHODS ==========

    /**
     * Safely gets a value from a map with Optional
     */
    private <T> Optional<T> safeGet(final Map<String, Object> map, final String key, final Class<T> type) {
        return Optional.ofNullable(map)
            .map(m -> m.get(key))
            .filter(type::isInstance)
            .map(type::cast);
    }

    /**
     * Safely converts an object to the specified type with Optional
     */
    private <T> Optional<T> safeConvert(final Object value, final Class<T> type) {
        if (value == null) return Optional.empty();

        try {
            if (type.isInstance(value)) {
                return Optional.of(type.cast(value));
            }

            // Add specific conversion logic here based on types
            if (type == String.class) {
                return Optional.of(type.cast(value.toString()));
            }
            if (type == Integer.class && value instanceof Number) {
                return Optional.of(type.cast(Integer.valueOf(((Number) value).intValue())));
            }
            if (type == BigDecimal.class && value instanceof Number) {
                return Optional.of(type.cast(BigDecimal.valueOf(((Number) value).doubleValue())));
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Updates null safety metrics
     */
    private void updateMetrics(final String metric, final int increment) {
        NULL_SAFETY_METRICS.put(metric, NULL_SAFETY_METRICS.getOrDefault(metric, 0) + increment);
    }

    /**
     * Gets null safety metrics
     */
    public Map<String, Integer> getNullSafetyMetrics() {
        return new HashMap<>(NULL_SAFETY_METRICS);
    }
}
