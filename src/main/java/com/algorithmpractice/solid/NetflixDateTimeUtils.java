package com.algorithmpractice.solid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Date/Time Utilities
 *
 * <p>This class provides comprehensive date and time handling capabilities with Netflix production standards.
 * It demonstrates advanced Java type inference patterns, wrapper classes, and production-grade practices
 * expected at Netflix for SDE-2 Senior Backend Engineers.</p>
 *
 * <p><strong>Key Features for Cross-Language Developers (TypeScript/Node.js background):</strong></p>
 * <ul>
 *   <li><strong>Immutable Date Objects:</strong> Unlike JavaScript Date, Java date objects are immutable and thread-safe</li>
 *   <li><strong>Type Safety:</strong> Compile-time type checking prevents invalid date operations</li>
 *   <li><strong>Timezone Handling:</strong> Proper timezone support with UTC as default (Netflix standard)</li>
 *   <li><strong>Performance:</strong> Optimized caching and efficient date parsing/formatting</li>
 *   <li><strong>Null Safety:</strong> Comprehensive Optional usage for null-safe date operations</li>
 * </ul>
 *
 * <p><strong>Netflix Production Standards:</strong></p>
 * <ul>
 *   <li>UTC as default timezone for all date operations</li>
 *   <li>Immutable date objects for thread safety</li>
 *   <li>Comprehensive error handling with proper logging</li>
 *   <li>Type inference patterns using 'var' keyword</li>
 *   <li>Final keyword usage for immutability and performance</li>
 *   <li>Wrapper class integration for null safety</li>
 *   <li>Performance optimization with caching</li>
 * </ul>
 *
 * @author Netflix Backend Engineering Team
 * @version 2.0.0
 * @since 2024
 */
@Slf4j
@Component
public class NetflixDateTimeUtils {

    // ========== GLOBAL CONSTANTS (Netflix Production Standards) ==========

    /**
     * Global constants with final keyword - Netflix production standard
     * These demonstrate proper global variable declaration with immutability
     */
    private static final String NETFLIX_TIMEZONE = "UTC";
    private static final ZoneId NETFLIX_ZONE_ID = ZoneId.of(NETFLIX_TIMEZONE);
    private static final Integer MAX_DATE_CACHE_SIZE = 1000;
    private static final Long DEFAULT_SESSION_TIMEOUT_MINUTES = 30L;
    private static final Integer BUSINESS_HOURS_START = 9;
    private static final Integer BUSINESS_HOURS_END = 17;

    // ========== THREAD-SAFE CACHING (Netflix Production Standard) ==========

    /**
     * Thread-safe cache for date formatters - Netflix production standard
     * This demonstrates proper global collection usage with thread safety
     */
    private static final Map<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

    // ========== TYPE INFERENCE WITH DATES ==========

    /**
     * Demonstrates type inference with date and time operations
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java's LocalDateTime vs JavaScript Date:
     * Java provides separate classes for different date/time concepts, unlike JavaScript's single Date class.</p>
     *
     * @return Map containing comprehensive date/time type inference examples
     */
    public Map<String, Object> demonstrateTypeInferenceWithDates() {
        log.info("=== Demonstrating Type Inference with Dates ===");

        // Type inference with var keyword - Netflix production standard
        var currentDateTime = LocalDateTime.now(NETFLIX_ZONE_ID); // LocalDateTime
        var currentDate = LocalDate.now(NETFLIX_ZONE_ID); // LocalDate
        var currentTime = LocalTime.now(NETFLIX_ZONE_ID); // LocalTime
        var zonedDateTime = ZonedDateTime.now(NETFLIX_ZONE_ID); // ZonedDateTime
        var instant = Instant.now(); // Instant

        // Type inference with date collections
        var dateList = Arrays.asList(
            currentDateTime,
            currentDateTime.plusDays(1),
            currentDateTime.plusDays(2)
        ); // List<LocalDateTime>

        var dateMap = Map.of(
            "created", currentDateTime,
            "modified", currentDateTime.plusMinutes(5),
            "expires", currentDateTime.plusHours(1)
        ); // Map<String, LocalDateTime>

        // Type inference with wrapper classes for date calculations
        var daysBetweenWrapper = Integer.valueOf((int) ChronoUnit.DAYS.between(currentDate, currentDate.plusWeeks(1)));
        var hoursBetweenWrapper = Long.valueOf(ChronoUnit.HOURS.between(currentDateTime, currentDateTime.plusDays(1)));
        var minutesBetweenWrapper = Integer.valueOf((int) ChronoUnit.MINUTES.between(currentDateTime, currentDateTime.plusHours(2)));

        // Type inference with date formatting
        var isoFormatter = getCachedFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME); // DateTimeFormatter
        var formattedDate = currentDateTime.format(isoFormatter); // String

        // Complex type inference with nested date operations
        var dateMetadata = Map.of(
            "currentDateTime", currentDateTime,
            "currentDate", currentDate,
            "currentTime", currentTime,
            "zonedDateTime", zonedDateTime,
            "instant", instant,
            "dateList", dateList,
            "dateMap", dateMap,
            "daysBetween", daysBetweenWrapper,
            "hoursBetween", hoursBetweenWrapper,
            "minutesBetween", minutesBetweenWrapper,
            "formattedDate", formattedDate,
            "isBusinessHours", Boolean.valueOf(isBusinessHours(currentTime))
        ); // Map<String, Object>

        return dateMetadata;
    }

    // ========== DATE PARSING AND FORMATTING ==========

    /**
     * Demonstrates type inference with date parsing and formatting
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java provides compile-time format validation
     * and thread-safe formatters, unlike JavaScript's runtime string formatting.</p>
     *
     * @param dateStrings the date strings to parse
     * @return Map containing parsed and formatted date information
     */
    public Map<String, Object> demonstrateDateParsingAndFormatting(final List<String> dateStrings) {
        log.info("=== Demonstrating Date Parsing and Formatting ===");

        // Type inference with date parsing
        var parsedDates = new ArrayList<LocalDateTime>(); // ArrayList<LocalDateTime>
        var parsingErrors = new ArrayList<String>(); // ArrayList<String>

        // Type inference with multiple formatters
        var formatters = Arrays.asList(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        ); // List<DateTimeFormatter>

        // Type inference with parsing operations
        for (var dateString : dateStrings) { // String
            var parsed = false; // boolean

            for (var formatter : formatters) { // DateTimeFormatter
                try {
                    var parsedDate = LocalDateTime.parse(dateString, formatter); // LocalDateTime
                    parsedDates.add(parsedDate);
                    parsed = true;
                    break;
                } catch (DateTimeParseException e) {
                    // Continue with next formatter
                }
            }

            if (!parsed) {
                parsingErrors.add(dateString);
            }
        }

        // Type inference with formatting operations
        var formattedResults = parsedDates.stream()
            .map(date -> Map.of(
                "original", date.toString(),
                "iso", date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "readable", date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")),
                "compact", date.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            ))
            .collect(Collectors.toList()); // List<Map<String, String>>

        // Type inference with wrapper classes
        var successCount = Integer.valueOf(parsedDates.size()); // Integer
        var errorCount = Integer.valueOf(parsingErrors.size()); // Integer
        var successRate = Double.valueOf((double) successCount.intValue() / dateStrings.size()); // Double

        return Map.of(
            "parsedDates", parsedDates,
            "parsingErrors", parsingErrors,
            "formattedResults", formattedResults,
            "successCount", successCount,
            "errorCount", errorCount,
            "successRate", successRate,
            "hasErrors", Boolean.valueOf(!parsingErrors.isEmpty())
        );
    }

    // ========== DATE CALCULATIONS AND OPERATIONS ==========

    /**
     * Demonstrates type inference with date calculations and business logic
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java's temporal API provides
     * type-safe date arithmetic with compile-time validation, unlike JavaScript's mutable Date objects.</p>
     *
     * @param baseDate the base date for calculations
     * @param operations the operations to perform
     * @return Map containing calculation results
     */
    public Map<String, Object> demonstrateDateCalculations(final LocalDateTime baseDate, final List<String> operations) {
        log.info("=== Demonstrating Date Calculations ===");

        // Type inference with date calculations
        var calculationResults = new HashMap<String, Map<String, Object>>(); // HashMap<String, Map<String, Object>>

        for (var operation : operations) { // String
            var result = switch (operation.toLowerCase()) {
                case "next_business_day" -> {
                    var nextBusinessDay = getNextBusinessDay(baseDate.toLocalDate()); // LocalDate
                    var resultDateTime = nextBusinessDay.atTime(9, 0); // LocalDateTime
                    yield Map.of(
                        "operation", operation,
                        "result", resultDateTime,
                        "daysAdded", Integer.valueOf(1),
                        "isBusinessDay", Boolean.valueOf(true)
                    ); // Map<String, Object>
                }
                case "end_of_month" -> {
                    var endOfMonth = baseDate.with(TemporalAdjusters.lastDayOfMonth()); // LocalDateTime
                    yield Map.of(
                        "operation", operation,
                        "result", endOfMonth,
                        "daysUntil", Integer.valueOf((int) ChronoUnit.DAYS.between(baseDate.toLocalDate(), endOfMonth.toLocalDate())),
                        "isEndOfMonth", Boolean.valueOf(true)
                    );
                }
                case "session_expiry" -> {
                    var sessionExpiry = baseDate.plusMinutes(DEFAULT_SESSION_TIMEOUT_MINUTES); // LocalDateTime
                    var minutesUntilExpiry = Integer.valueOf((int) ChronoUnit.MINUTES.between(baseDate, sessionExpiry));
                    yield Map.of(
                        "operation", operation,
                        "result", sessionExpiry,
                        "minutesUntilExpiry", minutesUntilExpiry,
                        "isExpired", Boolean.valueOf(sessionExpiry.isBefore(LocalDateTime.now(NETFLIX_ZONE_ID)))
                    );
                }
                default -> Map.of(
                    "operation", operation,
                    "result", baseDate,
                    "error", "Unknown operation: " + operation
                );
            };

            calculationResults.put(operation, result);
        }

        // Type inference with summary calculations
        var totalOperations = Integer.valueOf(calculationResults.size()); // Integer
        var successfulOperations = calculationResults.values().stream()
            .filter(result -> !result.containsKey("error"))
            .count(); // long

        var successRate = Double.valueOf((double) successfulOperations / totalOperations.intValue()); // Double

        return Map.of(
            "calculationResults", calculationResults,
            "totalOperations", totalOperations,
            "successfulOperations", Integer.valueOf((int) successfulOperations),
            "successRate", successRate,
            "baseDate", baseDate,
            "hasErrors", Boolean.valueOf(successfulOperations < totalOperations.intValue())
        );
    }

    // ========== TIMEZONE CONVERSIONS ==========

    /**
     * Demonstrates type inference with timezone conversions
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java provides comprehensive timezone
     * support with proper daylight saving time handling, unlike JavaScript's basic timezone support.</p>
     *
     * @param dateTime the date time to convert
     * @param targetTimezones the target timezones
     * @return Map containing timezone conversion results
     */
    public Map<String, Object> demonstrateTimezoneConversions(final LocalDateTime dateTime, final List<String> targetTimezones) {
        log.info("=== Demonstrating Timezone Conversions ===");

        // Type inference with timezone conversions
        var utcZoned = dateTime.atZone(NETFLIX_ZONE_ID); // ZonedDateTime
        var timezoneConversions = new HashMap<String, Map<String, Object>>(); // HashMap<String, Map<String, Object>>

        for (var timezone : targetTimezones) { // String
            try {
                var targetZone = ZoneId.of(timezone); // ZoneId
                var converted = utcZoned.withZoneSameInstant(targetZone); // ZonedDateTime

                var conversionInfo = Map.of(
                    "timezone", timezone,
                    "convertedDateTime", converted.toLocalDateTime(),
                    "offset", converted.getOffset().toString(),
                    "isDST", Boolean.valueOf(converted.getZone().getRules().isDaylightSavings(converted.toInstant())),
                    "hoursDifference", Integer.valueOf((int) ChronoUnit.HOURS.between(utcZoned, converted))
                ); // Map<String, Object>

                timezoneConversions.put(timezone, conversionInfo);
            } catch (Exception e) {
                var errorInfo = Map.of(
                    "timezone", timezone,
                    "error", e.getMessage(),
                    "errorType", e.getClass().getSimpleName()
                );
                timezoneConversions.put(timezone, errorInfo);
            }
        }

        // Type inference with conversion summary
        var successfulConversions = timezoneConversions.values().stream()
            .filter(conversion -> !conversion.containsKey("error"))
            .count(); // long

        var totalConversions = Integer.valueOf(timezoneConversions.size()); // Integer
        var successRate = Double.valueOf((double) successfulConversions / totalConversions.intValue()); // Double

        return Map.of(
            "originalDateTime", dateTime,
            "utcZoned", utcZoned,
            "timezoneConversions", timezoneConversions,
            "successfulConversions", Integer.valueOf((int) successfulConversions),
            "totalConversions", totalConversions,
            "successRate", successRate,
            "hasErrors", Boolean.valueOf(successfulConversions < totalConversions.intValue())
        );
    }

    // ========== VARIABLE SCOPING WITH DATES ==========

    /**
     * Demonstrates global vs local variable scoping with date operations
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java has block scoping similar to
     * TypeScript, but with explicit type declarations and final keyword usage for constants.</p>
     *
     * @param dateOperation the date operation to perform
     * @return processing results with proper scoping
     */
    public Map<String, Object> demonstrateVariableScopingWithDates(final String dateOperation) {
        log.info("=== Demonstrating Variable Scoping with Dates ===");

        // Global-like variables (method scope) - Netflix production standard
        final var AUDIT_RETENTION_DAYS = 2555; // 7 years in days
        final var COMPLIANCE_CHECK_INTERVAL_HOURS = 24;
        final var PERFORMANCE_LOG_INTERVAL_MINUTES = 5;

        // Local variables with type inference
        var processingResults = new ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>
        var startTime = LocalDateTime.now(NETFLIX_ZONE_ID); // LocalDateTime
        var operationCount = 0; // int

        // Nested scope demonstration with dates
        {
            var localStartTime = LocalDateTime.now(NETFLIX_ZONE_ID); // LocalDateTime
            var localOperationId = "date_op_" + System.nanoTime(); // String

            // Type inference with wrapper classes in local scope
            var localBatchSize = Integer.valueOf(100); // Integer
            var localIsActive = Boolean.valueOf(true); // Boolean
            var localProgress = Double.valueOf(0.0); // Double

            processingResults.add(Map.of(
                "scope", "local",
                "operationId", localOperationId,
                "startTime", localStartTime,
                "batchSize", localBatchSize,
                "isActive", localIsActive,
                "progress", localProgress
            ));
        }

        // Loop scope with type inference and date operations
        for (var i = 0; i < 3; i++) { // int
            var loopStartTime = LocalDateTime.now(NETFLIX_ZONE_ID); // LocalDateTime
            var loopBatchId = Integer.valueOf(i); // Integer

            // Type inference with date calculations in loop scope
            var loopExpiryTime = loopStartTime.plusHours(COMPLIANCE_CHECK_INTERVAL_HOURS); // LocalDateTime
            var loopDurationMinutes = Integer.valueOf((int) ChronoUnit.MINUTES.between(loopStartTime, loopExpiryTime));

            var loopResult = Map.of(
                "iteration", loopBatchId,
                "startTime", loopStartTime,
                "expiryTime", loopExpiryTime,
                "durationMinutes", loopDurationMinutes,
                "isExpired", Boolean.valueOf(loopExpiryTime.isBefore(LocalDateTime.now(NETFLIX_ZONE_ID)))
            );

            processingResults.add(loopResult);
            operationCount++;
        }

        // Final processing with type inference
        var endTime = LocalDateTime.now(NETFLIX_ZONE_ID); // LocalDateTime
        var totalDurationMinutes = Integer.valueOf((int) ChronoUnit.MINUTES.between(startTime, endTime));

        var finalResults = Map.of(
            "dateOperation", dateOperation,
            "startTime", startTime,
            "endTime", endTime,
            "totalDurationMinutes", totalDurationMinutes,
            "operationCount", Integer.valueOf(operationCount),
            "auditRetentionDays", Integer.valueOf(AUDIT_RETENTION_DAYS),
            "complianceCheckIntervalHours", Integer.valueOf(COMPLIANCE_CHECK_INTERVAL_HOURS),
            "performanceLogIntervalMinutes", Integer.valueOf(PERFORMANCE_LOG_INTERVAL_MINUTES),
            "processingResults", processingResults,
            "hasResults", Boolean.valueOf(!processingResults.isEmpty())
        );

        return finalResults;
    }

    // ========== HELPER METHODS ==========

    /**
     * Gets the next business day from the given date
     */
    private LocalDate getNextBusinessDay(final LocalDate date) {
        var nextDay = date.plusDays(1);
        while (nextDay.getDayOfWeek() == DayOfWeek.SATURDAY || nextDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    /**
     * Checks if the given time is within business hours
     */
    private boolean isBusinessHours(final LocalTime time) {
        var hour = time.getHour();
        return hour >= BUSINESS_HOURS_START && hour < BUSINESS_HOURS_END;
    }

    /**
     * Gets a cached DateTimeFormatter to avoid recreation overhead
     */
    private DateTimeFormatter getCachedFormatter(final DateTimeFormatter formatter) {
        var pattern = formatter.toString();
        return FORMATTER_CACHE.computeIfAbsent(pattern, k -> formatter);
    }

    /**
     * Gets a cached DateTimeFormatter by pattern
     */
    private DateTimeFormatter getCachedFormatter(final String pattern) {
        return FORMATTER_CACHE.computeIfAbsent(pattern, k -> DateTimeFormatter.ofPattern(pattern));
    }
}
