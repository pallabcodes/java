package com.algorithmpractice.solid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade BigDecimal Utilities
 *
 * <p>This class provides comprehensive BigDecimal operations for financial calculations with Netflix production standards.
 * It demonstrates advanced Java BigDecimal patterns, precision handling, and production-grade practices
 * expected at Netflix for SDE-2 Senior Backend Engineers.</p>
 *
 * <p><strong>Key Features for Cross-Language Developers (TypeScript/Node.js background):</strong></p>
 * <ul>
 *   <li><strong>Precision Control:</strong> Unlike JavaScript's floating-point numbers, BigDecimal provides exact decimal arithmetic</li>
 *   <li><strong>Financial Calculations:</strong> Built-in support for currency calculations with proper rounding</li>
 *   <li><strong>Type Safety:</strong> Compile-time type checking prevents floating-point errors</li>
 *   <li><strong>Performance:</strong> Optimized operations with caching and efficient algorithms</li>
 *   <li><strong>Thread Safety:</strong> Immutable operations suitable for concurrent environments</li>
 * </ul>
 *
 * <p><strong>Netflix Production Standards:</strong></p>
 * <ul>
 *   <li>Banker's rounding (HALF_EVEN) for financial calculations</li>
 *   <li>Configurable precision with MathContext</li>
 *   <li>Comprehensive error handling for overflow/underflow</li>
 *   <li>Type inference patterns using 'var' keyword</li>
 *   <li>Final keyword usage for immutability</li>
 *   <li>Wrapper class integration for null safety</li>
 *   <li>Performance optimization with caching</li>
 *   <li>Thread-safe operations for high-throughput systems</li>
 * </ul>
 *
 * @author Netflix Backend Engineering Team
 * @version 2.0.0
 * @since 2024
 */
@Slf4j
@Component
public class NetflixBigDecimalUtils {

    // ========== GLOBAL CONSTANTS (Netflix Production Standards) ==========

    /**
     * Global constants with final keyword - Netflix production standard
     */
    private static final MathContext FINANCIAL_PRECISION = new MathContext(10, RoundingMode.HALF_EVEN);
    private static final MathContext HIGH_PRECISION = new MathContext(20, RoundingMode.HALF_EVEN);
    private static final MathContext LOW_PRECISION = new MathContext(5, RoundingMode.HALF_EVEN);
    private static final Integer DEFAULT_SCALE = 2;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    // ========== THREAD-SAFE CACHING (Netflix Production Standard) ==========

    /**
     * Thread-safe cache for frequently used BigDecimal values - Netflix production standard
     */
    private static final Map<String, BigDecimal> DECIMAL_CACHE = new ConcurrentHashMap<>();

    static {
        // Pre-populate common financial values
        DECIMAL_CACHE.put("0", ZERO);
        DECIMAL_CACHE.put("1", ONE);
        DECIMAL_CACHE.put("100", ONE_HUNDRED);
        DECIMAL_CACHE.put("0.01", new BigDecimal("0.01"));
        DECIMAL_CACHE.put("0.10", new BigDecimal("0.10"));
        DECIMAL_CACHE.put("0.25", new BigDecimal("0.25"));
        DECIMAL_CACHE.put("0.50", new BigDecimal("0.50"));
    }

    // ========== TYPE INFERENCE WITH BIGDECIMAL ==========

    /**
     * Demonstrates type inference with BigDecimal operations
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java's BigDecimal provides precise decimal
     * arithmetic unlike JavaScript's floating-point numbers that can cause rounding errors.</p>
     *
     * @return Map containing comprehensive BigDecimal type inference examples
     */
    public Map<String, Object> demonstrateTypeInferenceWithBigDecimal() {
        log.info("=== Demonstrating Type Inference with BigDecimal ===");

        // Type inference with BigDecimal creation
        var price = new BigDecimal("19.99"); // BigDecimal
        var quantity = new BigDecimal("5"); // BigDecimal
        var taxRate = new BigDecimal("0.08"); // BigDecimal
        var discountPercent = new BigDecimal("10.00"); // BigDecimal

        // Type inference with BigDecimal operations
        var subtotal = price.multiply(quantity); // BigDecimal
        var discountAmount = subtotal.multiply(discountPercent.divide(ONE_HUNDRED)); // BigDecimal
        var taxableAmount = subtotal.subtract(discountAmount); // BigDecimal
        var taxAmount = taxableAmount.multiply(taxRate); // BigDecimal
        var total = taxableAmount.add(taxAmount); // BigDecimal

        // Type inference with rounding operations
        var roundedTotal = total.setScale(DEFAULT_SCALE, RoundingMode.HALF_EVEN); // BigDecimal
        var roundedTax = taxAmount.setScale(DEFAULT_SCALE, RoundingMode.HALF_EVEN); // BigDecimal

        // Type inference with comparison operations
        var isTotalPositive = Boolean.valueOf(total.compareTo(ZERO) > 0); // Boolean
        var isDiscountApplied = Boolean.valueOf(discountAmount.compareTo(ZERO) > 0); // Boolean
        var hasTax = Boolean.valueOf(taxAmount.compareTo(ZERO) > 0); // Boolean

        // Type inference with wrapper classes
        var operationCount = Integer.valueOf(5); // Integer
        var precisionUsed = Integer.valueOf(FINANCIAL_PRECISION.getPrecision()); // Integer
        var calculationTime = Long.valueOf(System.nanoTime()); // Long

        // Complex type inference with financial calculations
        var financialSummary = Map.of(
            "subtotal", subtotal,
            "discountAmount", discountAmount,
            "taxableAmount", taxableAmount,
            "taxAmount", taxAmount,
            "total", total,
            "roundedTotal", roundedTotal,
            "roundedTax", roundedTax,
            "isTotalPositive", isTotalPositive,
            "isDiscountApplied", isDiscountApplied,
            "hasTax", hasTax,
            "operationCount", operationCount,
            "precisionUsed", precisionUsed,
            "calculationTime", calculationTime
        ); // Map<String, Object>

        return financialSummary;
    }

    // ========== FINANCIAL CALCULATIONS ==========

    /**
     * Demonstrates type inference with financial calculation patterns
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java's BigDecimal ensures accurate
     * financial calculations, preventing the rounding errors common in JavaScript floating-point arithmetic.</p>
     *
     * @param financialData the financial data to process
     * @return Map containing financial calculation results
     */
    public Map<String, Object> demonstrateFinancialCalculations(final Map<String, Object> financialData) {
        log.info("=== Demonstrating Financial Calculations ===");

        // Type inference with safe BigDecimal conversion
        var principalAmount = safeToBigDecimal(financialData.get("principal"), ZERO); // BigDecimal
        var interestRate = safeToBigDecimal(financialData.get("interestRate"), ZERO); // BigDecimal
        var loanTermYears = safeToBigDecimal(financialData.get("loanTermYears"), ONE); // BigDecimal
        var paymentFrequency = safeToBigDecimal(financialData.get("paymentFrequency"), new BigDecimal("12")); // BigDecimal

        // Type inference with loan calculations
        var monthlyInterestRate = interestRate.divide(ONE_HUNDRED).divide(paymentFrequency); // BigDecimal
        var numberOfPayments = loanTermYears.multiply(paymentFrequency); // BigDecimal

        // Monthly payment calculation: PMT = P * (r(1+r)^n) / ((1+r)^n - 1)
        var monthlyPayment = calculateMonthlyPayment(principalAmount, monthlyInterestRate, numberOfPayments.intValue()); // BigDecimal

        // Type inference with additional financial metrics
        var totalPayments = monthlyPayment.multiply(numberOfPayments); // BigDecimal
        var totalInterest = totalPayments.subtract(principalAmount); // BigDecimal
        var annualPercentageRate = calculateAPR(interestRate, numberOfPayments.intValue()); // BigDecimal

        // Type inference with wrapper classes for metrics
        var calculationCount = Integer.valueOf(4); // Integer
        var isValidCalculation = Boolean.valueOf(monthlyPayment.compareTo(ZERO) > 0); // Boolean
        var hasInterest = Boolean.valueOf(totalInterest.compareTo(ZERO) > 0); // Boolean

        var financialResults = Map.of(
            "principalAmount", principalAmount,
            "interestRate", interestRate,
            "loanTermYears", loanTermYears,
            "monthlyInterestRate", monthlyInterestRate,
            "numberOfPayments", numberOfPayments,
            "monthlyPayment", monthlyPayment,
            "totalPayments", totalPayments,
            "totalInterest", totalInterest,
            "annualPercentageRate", annualPercentageRate,
            "calculationCount", calculationCount,
            "isValidCalculation", isValidCalculation,
            "hasInterest", hasInterest
        ); // Map<String, Object>

        return financialResults;
    }

    // ========== PRECISION AND ROUNDING ==========

    /**
     * Demonstrates type inference with precision and rounding operations
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java provides explicit control over
     * precision and rounding, unlike JavaScript's implicit floating-point behavior.</p>
     *
     * @param decimalValues the decimal values to process
     * @return Map containing precision and rounding results
     */
    public Map<String, Object> demonstratePrecisionAndRounding(final List<BigDecimal> decimalValues) {
        log.info("=== Demonstrating Precision and Rounding ===");

        // Type inference with different precision contexts
        var highPrecisionResults = new ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>
        var financialPrecisionResults = new ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>
        var lowPrecisionResults = new ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>

        for (var value : decimalValues) { // BigDecimal
            // Type inference with high precision operations
            var highPrecisionSqrt = value.sqrt(HIGH_PRECISION); // BigDecimal
            var highPrecisionResult = Map.of(
                "original", value,
                "operation", "sqrt",
                "result", highPrecisionSqrt,
                "precision", Integer.valueOf(HIGH_PRECISION.getPrecision()),
                "scale", Integer.valueOf(highPrecisionSqrt.scale())
            ); // Map<String, Object>

            highPrecisionResults.add(highPrecisionResult);

            // Type inference with financial precision operations
            var financialRounded = value.setScale(DEFAULT_SCALE, RoundingMode.HALF_EVEN); // BigDecimal
            var financialResult = Map.of(
                "original", value,
                "operation", "round",
                "result", financialRounded,
                "precision", Integer.valueOf(FINANCIAL_PRECISION.getPrecision()),
                "roundingMode", RoundingMode.HALF_EVEN.toString()
            ); // Map<String, Object>

            financialPrecisionResults.add(financialResult);

            // Type inference with low precision operations
            var lowPrecisionValue = value.round(LOW_PRECISION); // BigDecimal
            var lowPrecisionResult = Map.of(
                "original", value,
                "operation", "round_low",
                "result", lowPrecisionValue,
                "precision", Integer.valueOf(LOW_PRECISION.getPrecision()),
                "scale", Integer.valueOf(lowPrecisionValue.scale())
            ); // Map<String, Object>

            lowPrecisionResults.add(lowPrecisionResult);
        }

        // Type inference with precision comparison
        var precisionComparison = Map.of(
            "highPrecision", highPrecisionResults,
            "financialPrecision", financialPrecisionResults,
            "lowPrecision", lowPrecisionResults
        ); // Map<String, List<Map<String, Object>>>

        // Type inference with summary statistics
        var totalValues = Integer.valueOf(decimalValues.size()); // Integer
        var hasHighPrecisionResults = Boolean.valueOf(!highPrecisionResults.isEmpty()); // Boolean
        var hasFinancialResults = Boolean.valueOf(!financialPrecisionResults.isEmpty()); // Boolean
        var hasLowPrecisionResults = Boolean.valueOf(!lowPrecisionResults.isEmpty()); // Boolean

        return Map.of(
            "precisionComparison", precisionComparison,
            "totalValues", totalValues,
            "hasHighPrecisionResults", hasHighPrecisionResults,
            "hasFinancialResults", hasFinancialResults,
            "hasLowPrecisionResults", hasLowPrecisionResults,
            "defaultScale", Integer.valueOf(DEFAULT_SCALE),
            "financialPrecision", Integer.valueOf(FINANCIAL_PRECISION.getPrecision())
        );
    }

    // ========== CURRENCY CONVERSIONS ==========

    /**
     * Demonstrates type inference with currency conversion operations
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java's BigDecimal ensures accurate
     * currency conversions with proper precision, unlike JavaScript's floating-point limitations.</p>
     *
     * @param baseAmount the base amount to convert
     * @param exchangeRates the exchange rates to use
     * @return Map containing currency conversion results
     */
    public Map<String, Object> demonstrateCurrencyConversions(final BigDecimal baseAmount, final Map<String, BigDecimal> exchangeRates) {
        log.info("=== Demonstrating Currency Conversions ===");

        // Type inference with currency conversion calculations
        var conversionResults = new HashMap<String, Map<String, Object>>(); // HashMap<String, Map<String, Object>>

        for (var entry : exchangeRates.entrySet()) { // Map.Entry<String, BigDecimal>
            var targetCurrency = entry.getKey(); // String
            var exchangeRate = entry.getValue(); // BigDecimal

            // Type inference with conversion calculations
            var convertedAmount = baseAmount.multiply(exchangeRate); // BigDecimal
            var roundedAmount = convertedAmount.setScale(DEFAULT_SCALE, RoundingMode.HALF_EVEN); // BigDecimal
            var conversionFee = calculateConversionFee(baseAmount, exchangeRate); // BigDecimal
            var finalAmount = roundedAmount.subtract(conversionFee); // BigDecimal

            // Type inference with conversion metrics
            var conversionInfo = Map.of(
                "baseAmount", baseAmount,
                "targetCurrency", targetCurrency,
                "exchangeRate", exchangeRate,
                "convertedAmount", convertedAmount,
                "roundedAmount", roundedAmount,
                "conversionFee", conversionFee,
                "finalAmount", finalAmount,
                "isProfitable", Boolean.valueOf(finalAmount.compareTo(ZERO) > 0)
            ); // Map<String, Object>

            conversionResults.put(targetCurrency, conversionInfo);
        }

        // Type inference with conversion summary
        var totalCurrencies = Integer.valueOf(conversionResults.size()); // Integer
        var successfulConversions = conversionResults.values().stream()
            .filter(result -> (Boolean) result.get("isProfitable"))
            .count(); // long

        var successRate = Double.valueOf((double) successfulConversions / totalCurrencies.intValue()); // Double
        var totalConvertedValue = conversionResults.values().stream()
            .map(result -> (BigDecimal) result.get("finalAmount"))
            .reduce(ZERO, BigDecimal::add); // BigDecimal

        return Map.of(
            "conversionResults", conversionResults,
            "totalCurrencies", totalCurrencies,
            "successfulConversions", Integer.valueOf((int) successfulConversions),
            "successRate", successRate,
            "totalConvertedValue", totalConvertedValue,
            "baseAmount", baseAmount,
            "hasConversions", Boolean.valueOf(!conversionResults.isEmpty())
        );
    }

    // ========== VARIABLE SCOPING WITH BIGDECIMAL ==========

    /**
     * Demonstrates global vs local variable scoping with BigDecimal operations
     *
     * <p><strong>For TypeScript/Node.js developers:</strong> Java has block scoping similar to
     * TypeScript, but with explicit type declarations and final keyword usage for constants.</p>
     *
     * @param calculationType the type of calculation to perform
     * @return processing results with proper scoping
     */
    public Map<String, Object> demonstrateVariableScopingWithBigDecimal(final String calculationType) {
        log.info("=== Demonstrating Variable Scoping with BigDecimal ===");

        // Global-like variables (method scope) - Netflix production standard
        final var FINANCIAL_CALCULATION_TIMEOUT_MS = 5000L;
        final var MAX_CALCULATION_ITERATIONS = 1000;
        final var PRECISION_THRESHOLD = new BigDecimal("0.0001");

        // Local variables with type inference
        var calculationResults = new ArrayList<Map<String, Object>>(); // ArrayList<Map<String, Object>>
        var startTime = System.currentTimeMillis(); // long
        var calculationCount = 0; // int

        // Nested scope demonstration with BigDecimal
        {
            var localCalculationId = "calc_" + System.nanoTime(); // String
            var localPrecision = FINANCIAL_PRECISION; // MathContext

            // Type inference with wrapper classes in local scope
            var localIterationCount = Integer.valueOf(0); // Integer
            var localIsPrecise = Boolean.valueOf(true); // Boolean
            var localCalculationScore = Double.valueOf(0.0); // Double

            try {
                // Type inference with BigDecimal calculations in local scope
                var localBaseValue = new BigDecimal("100.00"); // BigDecimal
                var localMultiplier = new BigDecimal("1.5"); // BigDecimal
                var localResult = localBaseValue.multiply(localMultiplier, localPrecision); // BigDecimal

                calculationResults.add(Map.of(
                    "scope", "local",
                    "calculationId", localCalculationId,
                    "baseValue", localBaseValue,
                    "multiplier", localMultiplier,
                    "result", localResult,
                    "precision", Integer.valueOf(localPrecision.getPrecision()),
                    "iterationCount", localIterationCount,
                    "isPrecise", localIsPrecise,
                    "calculationScore", localCalculationScore,
                    "status", "success"
                ));

                calculationCount++;
            } catch (Exception e) {
                var errorInfo = Map.of(
                    "scope", "local",
                    "calculationId", localCalculationId,
                    "error", e.getMessage(),
                    "precision", Integer.valueOf(localPrecision.getPrecision()),
                    "iterationCount", localIterationCount,
                    "isPrecise", Boolean.valueOf(false),
                    "calculationScore", Double.valueOf(-1.0),
                    "status", "error"
                );

                calculationResults.add(errorInfo);
            }
        }

        // Loop scope with type inference and BigDecimal operations
        for (var i = 0; i < 3; i++) { // int
            var loopCalculationId = "loop_calc_" + i + "_" + System.nanoTime(); // String
            var loopIterationNumber = Integer.valueOf(i); // Integer

            // Type inference with BigDecimal operations in loop scope
            var loopBaseValue = new BigDecimal(String.valueOf(100 + i * 50)); // BigDecimal
            var loopOperation = switch (i % 3) {
                case 0 -> "multiply";
                case 1 -> "divide";
                default -> "add";
            }; // String

            var loopResult = performBigDecimalOperation(loopBaseValue, new BigDecimal("2.5"), loopOperation); // BigDecimal
            var loopPrecision = Integer.valueOf(loopResult.precision()); // Integer

            var loopInfo = Map.of(
                "scope", "loop",
                "iteration", loopIterationNumber,
                "calculationId", loopCalculationId,
                "baseValue", loopBaseValue,
                "operation", loopOperation,
                "result", loopResult,
                "precision", loopPrecision,
                "status", "success"
            );

            calculationResults.add(loopInfo);
            calculationCount++;
        }

        // Final processing with type inference
        var endTime = System.currentTimeMillis(); // long
        var totalDurationMs = Long.valueOf(endTime - startTime); // Long

        var finalResults = Map.of(
            "calculationType", calculationType,
            "startTime", Long.valueOf(startTime),
            "endTime", Long.valueOf(endTime),
            "totalDurationMs", totalDurationMs,
            "calculationCount", Integer.valueOf(calculationCount),
            "calculationTimeoutMs", Long.valueOf(FINANCIAL_CALCULATION_TIMEOUT_MS),
            "maxIterations", Integer.valueOf(MAX_CALCULATION_ITERATIONS),
            "precisionThreshold", PRECISION_THRESHOLD,
            "calculationResults", calculationResults,
            "hasResults", Boolean.valueOf(!calculationResults.isEmpty())
        );

        return finalResults;
    }

    // ========== HELPER METHODS ==========

    /**
     * Safely converts an object to BigDecimal with fallback
     */
    private BigDecimal safeToBigDecimal(final Object value, final BigDecimal fallback) {
        if (value == null) return fallback;

        try {
            if (value instanceof BigDecimal) return (BigDecimal) value;
            if (value instanceof String) return new BigDecimal((String) value);
            if (value instanceof Number) return new BigDecimal(((Number) value).doubleValue());
            return fallback;
        } catch (Exception e) {
            log.warn("Failed to convert {} to BigDecimal: {}", value, e.getMessage());
            return fallback;
        }
    }

    /**
     * Calculates monthly payment for loan
     */
    private BigDecimal calculateMonthlyPayment(final BigDecimal principal, final BigDecimal monthlyRate, final int numberOfPayments) {
        if (monthlyRate.compareTo(ZERO) == 0) {
            return principal.divide(new BigDecimal(numberOfPayments), FINANCIAL_PRECISION);
        }

        var onePlusRate = ONE.add(monthlyRate); // BigDecimal
        var ratePower = onePlusRate.pow(numberOfPayments, FINANCIAL_PRECISION); // BigDecimal

        var numerator = principal.multiply(monthlyRate).multiply(ratePower); // BigDecimal
        var denominator = ratePower.subtract(ONE); // BigDecimal

        return numerator.divide(denominator, FINANCIAL_PRECISION);
    }

    /**
     * Calculates Annual Percentage Rate (APR)
     */
    private BigDecimal calculateAPR(final BigDecimal interestRate, final int numberOfPayments) {
        var monthlyRate = interestRate.divide(ONE_HUNDRED).divide(new BigDecimal("12"), FINANCIAL_PRECISION);
        var apr = (ONE.add(monthlyRate)).pow(12, FINANCIAL_PRECISION).subtract(ONE).multiply(ONE_HUNDRED);
        return apr.setScale(DEFAULT_SCALE, RoundingMode.HALF_EVEN);
    }

    /**
     * Calculates currency conversion fee
     */
    private BigDecimal calculateConversionFee(final BigDecimal amount, final BigDecimal rate) {
        // Simple fee calculation: 1% of converted amount
        return amount.multiply(rate).multiply(new BigDecimal("0.01")).setScale(DEFAULT_SCALE, RoundingMode.HALF_EVEN);
    }

    /**
     * Performs basic BigDecimal operations
     */
    private BigDecimal performBigDecimalOperation(final BigDecimal a, final BigDecimal b, final String operation) {
        return switch (operation) {
            case "multiply" -> a.multiply(b, FINANCIAL_PRECISION);
            case "divide" -> a.divide(b, FINANCIAL_PRECISION);
            case "add" -> a.add(b);
            case "subtract" -> a.subtract(b);
            default -> a;
        };
    }

    /**
     * Gets cached BigDecimal value
     */
    private BigDecimal getCachedBigDecimal(final String key) {
        return DECIMAL_CACHE.computeIfAbsent(key, k -> new BigDecimal(k));
    }
}
