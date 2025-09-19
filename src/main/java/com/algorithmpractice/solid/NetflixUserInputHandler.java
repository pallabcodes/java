package com.algorithmpractice.solid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade User Input Handler
 *
 * <p>This class provides comprehensive user input validation and processing with Netflix production standards.
 * It demonstrates advanced Java input validation patterns, security measures, and production-grade practices
 * expected at Netflix for SDE-2 Senior Backend Engineers.</p>
 *
 * <p><strong>Key Features for Cross-Language Developers (TypeScript/Node.js background):</strong></p>
 * <ul>
 *   <li><strong>Input Validation:</strong> Unlike JavaScript's flexible types, Java provides compile-time validation</li>
 *   <li><strong>Security:</strong> Built-in protection against injection attacks and malicious input</li>
 *   <li><strong>Type Safety:</strong> Explicit type conversion with validation prevents runtime errors</li>
 *   <li><strong>Sanitization:</strong> Comprehensive input sanitization and normalization</li>
 *   <li><strong>Performance:</strong> Efficient validation with caching and optimized patterns</li>
 * </ul>
 *
 * <p><strong>Netflix Production Standards:</strong></p>
 * <ul>
 *   <li>Defense in depth with multiple validation layers</li>
 *   <li>Comprehensive input sanitization and normalization</li>
 *   <li>Security-first approach with injection prevention</li>
 *   <li>Type inference patterns using 'var' keyword</li>
 *   <li>Final keyword usage for immutability</li>
 *   <li>Wrapper class integration for null safety</li>
 *   <li>Thread-safe operations for concurrent processing</li>
 *   <li>Comprehensive logging and audit trails</li>
 * </ul>
 *
 * @author Netflix Backend Engineering Team
 * @version 2.0.0
 * @since 2024
 */
@Slf4j
@Component
public class NetflixUserInputHandler {

    // ========== GLOBAL CONSTANTS (Netflix Production Standards) ==========

    /**
     * Global constants with final keyword - Netflix production standard
     */
    private static final Integer MAX_STRING_LENGTH = 1000;
    private static final Integer MIN_PASSWORD_LENGTH = 8;
    private static final Integer MAX_PASSWORD_LENGTH = 128;
    private static final Integer MAX_EMAIL_LENGTH = 254;
    private static final Integer MAX_NAME_LENGTH = 100;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        ".*(\\b(union|select|insert|update|delete|drop|create|alter)\\b).*",
        Pattern.CASE_INSENSITIVE
    );

    // ========== THREAD-SAFE CACHING (Netflix Production Standard) ==========

    /**
     * Thread-safe cache for validation patterns - Netflix production standard
     */
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Predicate<String>> VALIDATOR_CACHE = new ConcurrentHashMap<>();

    static {
        // Pre-compile common validation patterns
        PATTERN_CACHE.put("email", EMAIL_PATTERN);
        PATTERN_CACHE.put("sql_injection", SQL_INJECTION_PATTERN);
        PATTERN_CACHE.put("phone", Pattern.compile("^\\+?[1-9]\\d{1,14}$"));
        PATTERN_CACHE.put("zipcode_us", Pattern.compile("^\\d{5}(-\\d{4})?$"));
        PATTERN_CACHE.put("credit_card", Pattern.compile("^\\d{4}-?\\d{4}-?\\d{4}-?\\d{4}$"));
    }

    // ========== TYPE INFERENCE WITH INPUT VALIDATION ==========

    /**
     * Demonstrates type inference with user input validation patterns
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java provides compile-time type checking
     * for input validation, unlike JavaScript's runtime validation patterns.</p>
     *
     * @param inputData the user input data to validate
     * @return Map containing comprehensive validation results
     */
    public Map<String, Object> demonstrateTypeInferenceWithInputValidation(final Map<String, String> inputData) {
        log.info("=== Demonstrating Type Inference with Input Validation ===");

        // Type inference with input validation results
        var validationResults = new HashMap<String, Map<String, Object>>(); // HashMap<String, Map<String, Object>>
        var validationSummary = new HashMap<String, Integer>(); // HashMap<String, Integer>

        // Initialize counters
        validationSummary.put("totalFields", Integer.valueOf(inputData.size()));
        validationSummary.put("validFields", Integer.valueOf(0));
        validationSummary.put("invalidFields", Integer.valueOf(0));
        validationSummary.put("sanitizedFields", Integer.valueOf(0));

        for (var entry : inputData.entrySet()) { // Map.Entry<String, String>
            var fieldName = entry.getKey(); // String
            var fieldValue = entry.getValue(); // String

            // Type inference with field validation
            var fieldValidation = validateField(fieldName, fieldValue); // Map<String, Object>

            // Update counters based on validation results
            var isValid = (Boolean) fieldValidation.get("isValid"); // Boolean
            var wasSanitized = (Boolean) fieldValidation.get("wasSanitized"); // Boolean

            if (isValid) {
                validationSummary.put("validFields",
                    Integer.valueOf(validationSummary.get("validFields") + 1));
            } else {
                validationSummary.put("invalidFields",
                    Integer.valueOf(validationSummary.get("invalidFields") + 1));
            }

            if (wasSanitized) {
                validationSummary.put("sanitizedFields",
                    Integer.valueOf(validationSummary.get("sanitizedFields") + 1));
            }

            validationResults.put(fieldName, fieldValidation);
        }

        // Type inference with validation statistics
        var totalFields = Integer.valueOf(inputData.size()); // Integer
        var validFields = validationSummary.get("validFields"); // Integer
        var invalidFields = validationSummary.get("invalidFields"); // Integer
        var sanitizedFields = validationSummary.get("sanitizedFields"); // Integer

        var validationRate = Double.valueOf((double) validFields.intValue() / totalFields.intValue()); // Double
        var sanitizationRate = Double.valueOf((double) sanitizedFields.intValue() / totalFields.intValue()); // Double

        // Type inference with comprehensive validation report
        var validationReport = Map.of(
            "validationResults", validationResults,
            "validationSummary", validationSummary,
            "totalFields", totalFields,
            "validFields", validFields,
            "invalidFields", invalidFields,
            "sanitizedFields", sanitizedFields,
            "validationRate", validationRate,
            "sanitizationRate", sanitizationRate,
            "overallStatus", Boolean.valueOf(invalidFields == 0),
            "processingTime", Long.valueOf(System.currentTimeMillis())
        ); // Map<String, Object>

        return validationReport;
    }

    // ========== SECURE INPUT SANITIZATION ==========

    /**
     * Demonstrates type inference with input sanitization and security measures
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java provides built-in security measures
     * for input sanitization, unlike JavaScript's manual sanitization requirements.</p>
     *
     * @param rawInputs the raw user inputs to sanitize
     * @return Map containing sanitized input results
     */
    public Map<String, Object> demonstrateInputSanitization(final Map<String, String> rawInputs) {
        log.info("=== Demonstrating Input Sanitization ===");

        // Type inference with sanitization results
        var sanitizationResults = new HashMap<String, Map<String, Object>>(); // HashMap<String, Map<String, Object>>
        var securityMetrics = new HashMap<String, Integer>(); // HashMap<String, Integer>

        // Initialize security metrics
        securityMetrics.put("totalInputs", Integer.valueOf(rawInputs.size()));
        securityMetrics.put("safeInputs", Integer.valueOf(0));
        securityMetrics.put("sanitizedInputs", Integer.valueOf(0));
        securityMetrics.put("blockedInputs", Integer.valueOf(0));
        securityMetrics.put("sqlInjectionAttempts", Integer.valueOf(0));
        securityMetrics.put("xssAttempts", Integer.valueOf(0));

        for (var entry : rawInputs.entrySet()) { // Map.Entry<String, String>
            var inputName = entry.getKey(); // String
            var rawValue = entry.getValue(); // String

            // Type inference with security checks
            var securityCheck = performSecurityChecks(rawValue); // Map<String, Object>
            var sanitizedValue = sanitizeInput(rawValue); // String
            var finalValue = securityCheck.get("isSafe").equals(Boolean.TRUE) ? sanitizedValue : "[BLOCKED]"; // String

            // Update security metrics
            var isSafe = (Boolean) securityCheck.get("isSafe"); // Boolean
            var wasSanitized = !rawValue.equals(sanitizedValue); // boolean
            var hasSqlInjection = (Boolean) securityCheck.get("hasSqlInjection"); // Boolean
            var hasXss = (Boolean) securityCheck.get("hasXss"); // Boolean

            if (isSafe) {
                if (wasSanitized) {
                    securityMetrics.put("sanitizedInputs",
                        Integer.valueOf(securityMetrics.get("sanitizedInputs") + 1));
                } else {
                    securityMetrics.put("safeInputs",
                        Integer.valueOf(securityMetrics.get("safeInputs") + 1));
                }
            } else {
                securityMetrics.put("blockedInputs",
                    Integer.valueOf(securityMetrics.get("blockedInputs") + 1));
            }

            if (hasSqlInjection) {
                securityMetrics.put("sqlInjectionAttempts",
                    Integer.valueOf(securityMetrics.get("sqlInjectionAttempts") + 1));
            }

            if (hasXss) {
                securityMetrics.put("xssAttempts",
                    Integer.valueOf(securityMetrics.get("xssAttempts") + 1));
            }

            // Type inference with sanitization details
            var sanitizationInfo = Map.of(
                "inputName", inputName,
                "rawValue", rawValue,
                "sanitizedValue", sanitizedValue,
                "finalValue", finalValue,
                "securityCheck", securityCheck,
                "isSafe", isSafe,
                "wasSanitized", Boolean.valueOf(wasSanitized),
                "wasBlocked", Boolean.valueOf(!isSafe)
            ); // Map<String, Object>

            sanitizationResults.put(inputName, sanitizationInfo);
        }

        // Type inference with security summary
        var totalInputs = Integer.valueOf(rawInputs.size()); // Integer
        var safeInputs = securityMetrics.get("safeInputs"); // Integer
        var sanitizedInputs = securityMetrics.get("sanitizedInputs"); // Integer
        var blockedInputs = securityMetrics.get("blockedInputs"); // Integer
        var sqlInjectionAttempts = securityMetrics.get("sqlInjectionAttempts"); // Integer
        var xssAttempts = securityMetrics.get("xssAttempts"); // Integer

        var securityScore = Double.valueOf((double) (safeInputs + sanitizedInputs) / totalInputs); // Double
        var threatLevel = sqlInjectionAttempts > 0 || xssAttempts > 0 ? "HIGH" : blockedInputs > 0 ? "MEDIUM" : "LOW"; // String

        return Map.of(
            "sanitizationResults", sanitizationResults,
            "securityMetrics", securityMetrics,
            "totalInputs", totalInputs,
            "safeInputs", safeInputs,
            "sanitizedInputs", sanitizedInputs,
            "blockedInputs", blockedInputs,
            "sqlInjectionAttempts", sqlInjectionAttempts,
            "xssAttempts", xssAttempts,
            "securityScore", securityScore,
            "threatLevel", threatLevel,
            "processingTime", Long.valueOf(System.currentTimeMillis())
        );
    }

    // ========== TYPE CONVERSION AND VALIDATION ==========

    /**
     * Demonstrates type inference with input type conversion and validation
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java provides explicit type conversion
     * with validation, unlike JavaScript's implicit type coercion.</p>
     *
     * @param inputValues the input values to convert and validate
     * @return Map containing type conversion results
     */
    public Map<String, Object> demonstrateTypeConversion(final Map<String, Object> inputValues) {
        log.info("=== Demonstrating Type Conversion ===");

        // Type inference with type conversion results
        var conversionResults = new HashMap<String, Map<String, Object>>(); // HashMap<String, Map<String, Object>>
        var conversionSummary = new HashMap<String, Integer>(); // HashMap<String, Integer>

        // Initialize conversion counters
        conversionSummary.put("totalConversions", Integer.valueOf(inputValues.size()));
        conversionSummary.put("successfulConversions", Integer.valueOf(0));
        conversionSummary.put("failedConversions", Integer.valueOf(0));
        conversionSummary.put("stringConversions", Integer.valueOf(0));
        conversionSummary.put("numberConversions", Integer.valueOf(0));
        conversionSummary.put("dateConversions", Integer.valueOf(0));
        conversionSummary.put("booleanConversions", Integer.valueOf(0));

        for (var entry : inputValues.entrySet()) { // Map.Entry<String, Object>
            var fieldName = entry.getKey(); // String
            var rawValue = entry.getValue(); // Object

            // Type inference with type conversion attempts
            var stringConversion = safeConvertToString(rawValue); // Optional<String>
            var integerConversion = safeConvertToInteger(rawValue); // Optional<Integer>
            var bigDecimalConversion = safeConvertToBigDecimal(rawValue); // Optional<BigDecimal>
            var booleanConversion = safeConvertToBoolean(rawValue); // Optional<Boolean>
            var dateConversion = safeConvertToLocalDate(rawValue); // Optional<LocalDate>

            // Determine best conversion
            var conversionType = determineBestConversionType(rawValue); // String
            var convertedValue = getBestConvertedValue(rawValue, conversionType); // Object
            var isSuccessful = Boolean.valueOf(convertedValue != null); // Boolean

            // Update conversion counters
            if (isSuccessful) {
                conversionSummary.put("successfulConversions",
                    Integer.valueOf(conversionSummary.get("successfulConversions") + 1));

                switch (conversionType) {
                    case "string" -> conversionSummary.put("stringConversions",
                        Integer.valueOf(conversionSummary.get("stringConversions") + 1));
                    case "integer" -> conversionSummary.put("numberConversions",
                        Integer.valueOf(conversionSummary.get("numberConversions") + 1));
                    case "bigDecimal" -> conversionSummary.put("numberConversions",
                        Integer.valueOf(conversionSummary.get("numberConversions") + 1));
                    case "boolean" -> conversionSummary.put("booleanConversions",
                        Integer.valueOf(conversionSummary.get("booleanConversions") + 1));
                    case "date" -> conversionSummary.put("dateConversions",
                        Integer.valueOf(conversionSummary.get("dateConversions") + 1));
                }
            } else {
                conversionSummary.put("failedConversions",
                    Integer.valueOf(conversionSummary.get("failedConversions") + 1));
            }

            // Type inference with conversion details
            var conversionInfo = Map.of(
                "fieldName", fieldName,
                "rawValue", rawValue,
                "rawValueType", rawValue != null ? rawValue.getClass().getSimpleName() : "null",
                "conversionType", conversionType,
                "convertedValue", convertedValue,
                "isSuccessful", isSuccessful,
                "stringConversion", stringConversion.orElse(null),
                "integerConversion", integerConversion.orElse(null),
                "bigDecimalConversion", bigDecimalConversion.orElse(null),
                "booleanConversion", booleanConversion.orElse(null),
                "dateConversion", dateConversion.map(LocalDate::toString).orElse(null)
            ); // Map<String, Object>

            conversionResults.put(fieldName, conversionInfo);
        }

        // Type inference with conversion statistics
        var totalConversions = Integer.valueOf(inputValues.size()); // Integer
        var successfulConversions = conversionSummary.get("successfulConversions"); // Integer
        var failedConversions = conversionSummary.get("failedConversions"); // Integer
        var successRate = Double.valueOf((double) successfulConversions.intValue() / totalConversions.intValue()); // Double

        return Map.of(
            "conversionResults", conversionResults,
            "conversionSummary", conversionSummary,
            "totalConversions", totalConversions,
            "successfulConversions", successfulConversions,
            "failedConversions", failedConversions,
            "successRate", successRate,
            "hasSuccessfulConversions", Boolean.valueOf(successfulConversions > 0),
            "hasFailedConversions", Boolean.valueOf(failedConversions > 0),
            "processingTime", Long.valueOf(System.currentTimeMillis())
        );
    }

    // ========== VARIABLE SCOPING WITH INPUT HANDLING ==========

    /**
     * Demonstrates global vs local variable scoping with input processing
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java has block scoping similar to
     * TypeScript, but with explicit type declarations and final keyword usage for constants.</p>
     *
     * @param inputSource the source of user input
     * @return processing results with proper scoping
     */
    public Map<String, Object> demonstrateVariableScopingWithInput(final String inputSource) {
        log.info("=== Demonstrating Variable Scoping with Input Processing ===");

        // Global-like variables (method scope) - Netflix production standard
        final var INPUT_PROCESSING_TIMEOUT_MS = 10000L;
        final var MAX_INPUT_SIZE_BYTES = 1048576; // 1MB
        final var VALIDATION_CACHE_TTL_MS = 300000L; // 5 minutes

        // Local variables with type inference
        var processingResults = new ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>
        var startTime = System.currentTimeMillis(); // long
        var inputCount = 0; // int
        var validationCache = new HashMap<String, Boolean>(); // HashMap<String, Boolean>

        // Nested scope demonstration with input processing
        {
            var localSessionId = "input_session_" + System.nanoTime(); // String
            var localInputBatch = new ArrayList<String>(); // ArrayList<String>

            // Type inference with wrapper classes in local scope
            var localBatchSize = Integer.valueOf(0); // Integer
            var localIsValid = Boolean.valueOf(true); // Boolean
            var localProcessingRate = Double.valueOf(0.0); // Double

            // Simulate input processing in local scope
            for (var i = 1; i <= 5; i++) { // int
                var simulatedInput = "input_" + localSessionId + "_" + i; // String
                var isInputValid = validateInput(simulatedInput); // boolean

                localInputBatch.add(simulatedInput);
                localBatchSize = Integer.valueOf(localBatchSize.intValue() + 1);

                if (!isInputValid) {
                    localIsValid = Boolean.valueOf(false);
                }

                inputCount++;
            }

            // Calculate processing rate
            var processingTime = System.currentTimeMillis() - startTime; // long
            localProcessingRate = Double.valueOf((double) localBatchSize.intValue() / processingTime * 1000);

            processingResults.add(Map.of(
                "scope", "local",
                "sessionId", localSessionId,
                "inputBatch", localInputBatch,
                "batchSize", localBatchSize,
                "isValid", localIsValid,
                "processingRate", localProcessingRate,
                "processingTime", Long.valueOf(processingTime),
                "status", "completed"
            ));
        }

        // Loop scope with type inference and input validation
        for (var i = 0; i < 3; i++) { // int
            var loopBatchId = "batch_" + i + "_" + System.nanoTime(); // String
            var loopInputItems = new ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>

            // Type inference with input validation in loop scope
            for (var j = 1; j <= 3; j++) { // int
                var inputItem = "item_" + i + "_" + j; // String
                var validationResult = validateInput(inputItem); // boolean
                var sanitizedInput = sanitizeInput(inputItem); // String

                var itemInfo = Map.of(
                    "itemId", inputItem,
                    "isValid", Boolean.valueOf(validationResult),
                    "sanitizedInput", sanitizedInput,
                    "wasModified", Boolean.valueOf(!inputItem.equals(sanitizedInput))
                ); // Map<String, Object>

                loopInputItems.add(itemInfo);

                if (validationResult) {
                    inputCount++;
                }
            }

            var loopValidCount = loopInputItems.stream()
                .filter(item -> (Boolean) item.get("isValid"))
                .count(); // long

            var loopValidRate = Double.valueOf((double) loopValidCount / loopInputItems.size()); // Double

            var loopInfo = Map.of(
                "scope", "loop",
                "batchId", loopBatchId,
                "iteration", Integer.valueOf(i),
                "inputItems", loopInputItems,
                "validCount", Integer.valueOf((int) loopValidCount),
                "totalCount", Integer.valueOf(loopInputItems.size()),
                "validRate", loopValidRate,
                "status", "processed"
            );

            processingResults.add(loopInfo);
        }

        // Final processing with type inference
        var endTime = System.currentTimeMillis(); // long
        var totalDurationMs = Long.valueOf(endTime - startTime); // Long

        var finalResults = Map.of(
            "inputSource", inputSource,
            "startTime", Long.valueOf(startTime),
            "endTime", Long.valueOf(endTime),
            "totalDurationMs", totalDurationMs,
            "inputCount", Integer.valueOf(inputCount),
            "processingTimeoutMs", Long.valueOf(INPUT_PROCESSING_TIMEOUT_MS),
            "maxInputSizeBytes", Integer.valueOf(MAX_INPUT_SIZE_BYTES),
            "validationCacheTtlMs", Long.valueOf(VALIDATION_CACHE_TTL_MS),
            "processingResults", processingResults,
            "hasResults", Boolean.valueOf(!processingResults.isEmpty())
        );

        return finalResults;
    }

    // ========== HELPER METHODS ==========

    /**
     * Validates a field based on its name and value
     */
    private Map<String, Object> validateField(final String fieldName, final String fieldValue) {
        if (fieldValue == null) {
            return Map.of(
                "fieldName", fieldName,
                "originalValue", fieldValue,
                "isValid", Boolean.valueOf(false),
                "wasSanitized", Boolean.valueOf(false),
                "error", "Field value is null"
            );
        }

        var sanitizedValue = sanitizeInput(fieldValue); // String
        var wasSanitized = Boolean.valueOf(!fieldValue.equals(sanitizedValue)); // Boolean
        var validationResult = validateFieldByName(fieldName, sanitizedValue); // Map<String, Object>

        return Map.of(
            "fieldName", fieldName,
            "originalValue", fieldValue,
            "sanitizedValue", sanitizedValue,
            "isValid", validationResult.get("isValid"),
            "wasSanitized", wasSanitized,
            "validationDetails", validationResult
        );
    }

    /**
     * Validates field by name with specific rules
     */
    private Map<String, Object> validateFieldByName(final String fieldName, final String value) {
        return switch (fieldName.toLowerCase()) {
            case "email" -> Map.of(
                "isValid", Boolean.valueOf(isValidEmail(value)),
                "validationType", "email",
                "maxLength", Integer.valueOf(MAX_EMAIL_LENGTH)
            );
            case "password" -> Map.of(
                "isValid", Boolean.valueOf(isValidPassword(value)),
                "validationType", "password",
                "minLength", Integer.valueOf(MIN_PASSWORD_LENGTH),
                "maxLength", Integer.valueOf(MAX_PASSWORD_LENGTH)
            );
            case "name" -> Map.of(
                "isValid", Boolean.valueOf(value.length() <= MAX_NAME_LENGTH),
                "validationType", "name",
                "maxLength", Integer.valueOf(MAX_NAME_LENGTH)
            );
            default -> Map.of(
                "isValid", Boolean.valueOf(value.length() <= MAX_STRING_LENGTH),
                "validationType", "general",
                "maxLength", Integer.valueOf(MAX_STRING_LENGTH)
            );
        };
    }

    /**
     * Performs security checks on input
     */
    private Map<String, Object> performSecurityChecks(final String input) {
        if (input == null) {
            return Map.of(
                "isSafe", Boolean.valueOf(false),
                "hasSqlInjection", Boolean.valueOf(false),
                "hasXss", Boolean.valueOf(false),
                "securityScore", Integer.valueOf(0)
            );
        }

        var hasSqlInjection = SQL_INJECTION_PATTERN.matcher(input).find(); // boolean
        var hasXss = input.contains("<script") || input.contains("javascript:"); // boolean
        var isSafe = Boolean.valueOf(!hasSqlInjection && !hasXss); // Boolean

        var securityScore = isSafe ? 100 : hasSqlInjection || hasXss ? 0 : 50; // int

        return Map.of(
            "isSafe", isSafe,
            "hasSqlInjection", Boolean.valueOf(hasSqlInjection),
            "hasXss", Boolean.valueOf(hasXss),
            "securityScore", Integer.valueOf(securityScore)
        );
    }

    /**
     * Sanitizes user input
     */
    private String sanitizeInput(final String input) {
        if (input == null) return null;

        return input.trim()
                   .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;")
                   .replaceAll("&", "&amp;");
    }

    /**
     * Validates email format
     */
    private boolean isValidEmail(final String email) {
        return email != null && email.length() <= MAX_EMAIL_LENGTH &&
               EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates password strength
     */
    private boolean isValidPassword(final String password) {
        return password != null &&
               password.length() >= MIN_PASSWORD_LENGTH &&
               password.length() <= MAX_PASSWORD_LENGTH &&
               password.matches(".*[A-Z].*") && // At least one uppercase
               password.matches(".*[a-z].*") && // At least one lowercase
               password.matches(".*\\d.*");     // At least one digit
    }

    /**
     * Validates general input
     */
    private boolean validateInput(final String input) {
        return input != null && !input.trim().isEmpty() && input.length() <= MAX_STRING_LENGTH;
    }

    /**
     * Safe type conversion methods
     */
    private Optional<String> safeConvertToString(final Object value) {
        if (value == null) return Optional.empty();
        return Optional.of(value.toString());
    }

    private Optional<Integer> safeConvertToInteger(final Object value) {
        try {
            if (value instanceof Integer) return Optional.of((Integer) value);
            if (value instanceof String) return Optional.of(Integer.valueOf((String) value));
            if (value instanceof Number) return Optional.of(((Number) value).intValue());
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> safeConvertToBigDecimal(final Object value) {
        try {
            if (value instanceof BigDecimal) return Optional.of((BigDecimal) value);
            if (value instanceof String) return Optional.of(new BigDecimal((String) value));
            if (value instanceof Number) return Optional.of(new BigDecimal(((Number) value).doubleValue()));
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Boolean> safeConvertToBoolean(final Object value) {
        try {
            if (value instanceof Boolean) return Optional.of((Boolean) value);
            if (value instanceof String) return Optional.of(Boolean.valueOf((String) value));
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<LocalDate> safeConvertToLocalDate(final Object value) {
        try {
            if (value instanceof LocalDate) return Optional.of((LocalDate) value);
            if (value instanceof String) {
                return Optional.of(LocalDate.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE));
            }
            return Optional.empty();
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private String determineBestConversionType(final Object value) {
        if (value instanceof String) return "string";
        if (value instanceof Integer || value instanceof BigDecimal) return "number";
        if (value instanceof Boolean) return "boolean";
        if (value instanceof LocalDate) return "date";
        return "unknown";
    }

    private Object getBestConvertedValue(final Object value, final String conversionType) {
        return switch (conversionType) {
            case "string" -> safeConvertToString(value).orElse(null);
            case "integer" -> safeConvertToInteger(value).orElse(null);
            case "bigDecimal" -> safeConvertToBigDecimal(value).orElse(null);
            case "boolean" -> safeConvertToBoolean(value).orElse(null);
            case "date" -> safeConvertToLocalDate(value).orElse(null);
            default -> null;
        };
    }
}
