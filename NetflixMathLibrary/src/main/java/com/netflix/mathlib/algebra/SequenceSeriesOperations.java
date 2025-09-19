/*
 * Copyright 2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.w3.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.mathlib.algebra;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Sequence and Series Operations - Production-grade implementations of sequence and series operations.
 *
 * This class provides comprehensive operations for:
 * - Arithmetic Progressions (AP)
 * - Geometric Progressions (GP)
 * - Harmonic Progressions
 * - Arithmetic-Geometric Progressions
 * - Sum calculations for various series
 * - Sequence analysis and properties
 * - Convergence testing
 * - Partial sums and infinite series
 *
 * All implementations are optimized for performance and production use with:
 * - Comprehensive input validation
 * - Performance monitoring and metrics
 * - High-precision arithmetic using BigDecimal
 * - Extensive error handling
 * - Detailed logging and debugging support
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class SequenceSeriesOperations implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(SequenceSeriesOperations.class);
    private static final String OPERATION_NAME = "SequenceSeriesOperations";
    private static final String COMPLEXITY = "O(various)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;
    private final MathContext DEFAULT_PRECISION = new MathContext(50, RoundingMode.HALF_UP);

    /**
     * Constructor for Sequence and Series Operations.
     */
    public SequenceSeriesOperations() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Sequence and Series Operations module");
    }

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public String getComplexity() {
        return COMPLEXITY;
    }

    @Override
    public OperationMetrics getMetrics() {
        return metrics;
    }

    @Override
    public void validateInputs(Object... inputs) {
        if (inputs == null || inputs.length == 0) {
            throw ValidationException.nullParameter("inputs", OPERATION_NAME);
        }

        for (Object input : inputs) {
            if (input == null) {
                throw ValidationException.nullParameter("input", OPERATION_NAME);
            }
        }
    }

    @Override
    public boolean isThreadSafe() {
        return THREAD_SAFE;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    // ===== ARITHMETIC PROGRESSION (AP) =====

    /**
     * Calculate nth term of an Arithmetic Progression.
     *
     * Formula: a_n = a + (n-1) * d
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     *
     * @param firstTerm first term (a)
     * @param commonDifference common difference (d)
     * @param termNumber term number (n)
     * @return nth term of the AP
     * @throws ValidationException if inputs are invalid
     */
    public BigDecimal arithmeticProgressionTerm(BigDecimal firstTerm, BigDecimal commonDifference, int termNumber) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(firstTerm, commonDifference, termNumber);

            if (termNumber < 1) {
                throw ValidationException.invalidRange("termNumber", termNumber, 1, Integer.MAX_VALUE, OPERATION_NAME);
            }

            logger.debug("Calculating AP term: a={}, d={}, n={}", firstTerm, commonDifference, termNumber);

            // a_n = a + (n-1) * d
            BigDecimal term = firstTerm.add(
                commonDifference.multiply(BigDecimal.valueOf(termNumber - 1), DEFAULT_PRECISION),
                DEFAULT_PRECISION
            );

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("AP term result: {}", term);

            return term;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating AP term: {}", e.getMessage());
            throw new ValidationException("Failed to calculate AP term: " + e.getMessage(),
                                        OPERATION_NAME, firstTerm, commonDifference, termNumber);
        }
    }

    /**
     * Calculate sum of first n terms of an Arithmetic Progression.
     *
     * Formula: S_n = n/2 * (2a + (n-1)d) or S_n = n/2 * (first + last)
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     *
     * @param firstTerm first term (a)
     * @param commonDifference common difference (d)
     * @param numberOfTerms number of terms (n)
     * @return sum of first n terms
     * @throws ValidationException if inputs are invalid
     */
    public BigDecimal arithmeticProgressionSum(BigDecimal firstTerm, BigDecimal commonDifference, int numberOfTerms) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(firstTerm, commonDifference, numberOfTerms);

            if (numberOfTerms < 1) {
                throw ValidationException.invalidRange("numberOfTerms", numberOfTerms, 1, Integer.MAX_VALUE, OPERATION_NAME);
            }

            logger.debug("Calculating AP sum: a={}, d={}, n={}", firstTerm, commonDifference, numberOfTerms);

            // S_n = n/2 * (2a + (n-1)d)
            BigDecimal n = BigDecimal.valueOf(numberOfTerms);
            BigDecimal twoA = firstTerm.multiply(BigDecimal.valueOf(2), DEFAULT_PRECISION);
            BigDecimal nMinus1D = commonDifference.multiply(BigDecimal.valueOf(numberOfTerms - 1), DEFAULT_PRECISION);
            BigDecimal numerator = twoA.add(nMinus1D, DEFAULT_PRECISION);
            BigDecimal sum = numerator.multiply(n, DEFAULT_PRECISION).divide(BigDecimal.valueOf(2), DEFAULT_PRECISION);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("AP sum result: {}", sum);

            return sum;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating AP sum: {}", e.getMessage());
            throw new ValidationException("Failed to calculate AP sum: " + e.getMessage(),
                                        OPERATION_NAME, firstTerm, commonDifference, numberOfTerms);
        }
    }

    /**
     * Generate Arithmetic Progression sequence.
     *
     * @param firstTerm first term
     * @param commonDifference common difference
     * @param numberOfTerms number of terms to generate
     * @return list of AP terms
     */
    public List<BigDecimal> generateArithmeticProgression(BigDecimal firstTerm, BigDecimal commonDifference, int numberOfTerms) {
        validateInputs(firstTerm, commonDifference, numberOfTerms);

        if (numberOfTerms < 1) {
            throw ValidationException.invalidRange("numberOfTerms", numberOfTerms, 1, Integer.MAX_VALUE, OPERATION_NAME);
        }

        List<BigDecimal> sequence = new ArrayList<>();
        BigDecimal currentTerm = firstTerm;

        for (int i = 0; i < numberOfTerms; i++) {
            sequence.add(currentTerm);
            currentTerm = currentTerm.add(commonDifference, DEFAULT_PRECISION);
        }

        return sequence;
    }

    // ===== GEOMETRIC PROGRESSION (GP) =====

    /**
     * Calculate nth term of a Geometric Progression.
     *
     * Formula: a_n = a * r^(n-1)
     *
     * Time Complexity: O(log n) for exponentiation
     * Space Complexity: O(1)
     *
     * @param firstTerm first term (a)
     * @param commonRatio common ratio (r)
     * @param termNumber term number (n)
     * @return nth term of the GP
     * @throws ValidationException if inputs are invalid
     */
    public BigDecimal geometricProgressionTerm(BigDecimal firstTerm, BigDecimal commonRatio, int termNumber) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(firstTerm, commonRatio, termNumber);

            if (termNumber < 1) {
                throw ValidationException.invalidRange("termNumber", termNumber, 1, Integer.MAX_VALUE, OPERATION_NAME);
            }

            logger.debug("Calculating GP term: a={}, r={}, n={}", firstTerm, commonRatio, termNumber);

            // a_n = a * r^(n-1)
            BigDecimal exponent = BigDecimal.valueOf(termNumber - 1);
            BigDecimal power = power(commonRatio, exponent);
            BigDecimal term = firstTerm.multiply(power, DEFAULT_PRECISION);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("GP term result: {}", term);

            return term;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating GP term: {}", e.getMessage());
            throw new ValidationException("Failed to calculate GP term: " + e.getMessage(),
                                        OPERATION_NAME, firstTerm, commonRatio, termNumber);
        }
    }

    /**
     * Calculate sum of first n terms of a Geometric Progression.
     *
     * Formula: S_n = a * (r^n - 1) / (r - 1) for r ≠ 1
     * Formula: S_n = n * a for r = 1
     *
     * Time Complexity: O(log n) for exponentiation
     * Space Complexity: O(1)
     *
     * @param firstTerm first term (a)
     * @param commonRatio common ratio (r)
     * @param numberOfTerms number of terms (n)
     * @return sum of first n terms
     * @throws ValidationException if inputs are invalid
     */
    public BigDecimal geometricProgressionSum(BigDecimal firstTerm, BigDecimal commonRatio, int numberOfTerms) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(firstTerm, commonRatio, numberOfTerms);

            if (numberOfTerms < 1) {
                throw ValidationException.invalidRange("numberOfTerms", numberOfTerms, 1, Integer.MAX_VALUE, OPERATION_NAME);
            }

            logger.debug("Calculating GP sum: a={}, r={}, n={}", firstTerm, commonRatio, numberOfTerms);

            BigDecimal sum;

            // Special case: r = 1
            if (commonRatio.compareTo(BigDecimal.ONE) == 0) {
                sum = firstTerm.multiply(BigDecimal.valueOf(numberOfTerms), DEFAULT_PRECISION);
            } else {
                // S_n = a * (r^n - 1) / (r - 1)
                BigDecimal rPowerN = power(commonRatio, BigDecimal.valueOf(numberOfTerms));
                BigDecimal numerator = rPowerN.subtract(BigDecimal.ONE, DEFAULT_PRECISION);
                BigDecimal denominator = commonRatio.subtract(BigDecimal.ONE, DEFAULT_PRECISION);
                BigDecimal fraction = numerator.divide(denominator, DEFAULT_PRECISION);
                sum = firstTerm.multiply(fraction, DEFAULT_PRECISION);
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("GP sum result: {}", sum);

            return sum;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating GP sum: {}", e.getMessage());
            throw new ValidationException("Failed to calculate GP sum: " + e.getMessage(),
                                        OPERATION_NAME, firstTerm, commonRatio, numberOfTerms);
        }
    }

    /**
     * Calculate sum to infinity of a Geometric Progression (if convergent).
     *
     * Formula: S_∞ = a / (1 - r) for |r| < 1
     *
     * @param firstTerm first term (a)
     * @param commonRatio common ratio (r)
     * @return infinite sum
     * @throws ValidationException if series diverges or inputs are invalid
     */
    public BigDecimal geometricProgressionInfiniteSum(BigDecimal firstTerm, BigDecimal commonRatio) {
        validateInputs(firstTerm, commonRatio);

        // Check for convergence: |r| < 1
        if (commonRatio.abs().compareTo(BigDecimal.ONE) >= 0) {
            throw new ValidationException("Geometric series diverges: |r| >= 1", OPERATION_NAME, commonRatio);
        }

        // S_∞ = a / (1 - r)
        BigDecimal denominator = BigDecimal.ONE.subtract(commonRatio, DEFAULT_PRECISION);
        return firstTerm.divide(denominator, DEFAULT_PRECISION);
    }

    /**
     * Generate Geometric Progression sequence.
     *
     * @param firstTerm first term
     * @param commonRatio common ratio
     * @param numberOfTerms number of terms to generate
     * @return list of GP terms
     */
    public List<BigDecimal> generateGeometricProgression(BigDecimal firstTerm, BigDecimal commonRatio, int numberOfTerms) {
        validateInputs(firstTerm, commonRatio, numberOfTerms);

        if (numberOfTerms < 1) {
            throw ValidationException.invalidRange("numberOfTerms", numberOfTerms, 1, Integer.MAX_VALUE, OPERATION_NAME);
        }

        List<BigDecimal> sequence = new ArrayList<>();
        BigDecimal currentTerm = firstTerm;

        for (int i = 0; i < numberOfTerms; i++) {
            sequence.add(currentTerm);
            currentTerm = currentTerm.multiply(commonRatio, DEFAULT_PRECISION);
        }

        return sequence;
    }

    // ===== HARMONIC PROGRESSION =====

    /**
     * Calculate nth term of a Harmonic Progression.
     *
     * Formula: a_n = a / (a + (n-1) * d)
     *
     * @param firstTerm first term (a)
     * @param commonDifference common difference (d)
     * @param termNumber term number (n)
     * @return nth term of the HP
     */
    public BigDecimal harmonicProgressionTerm(BigDecimal firstTerm, BigDecimal commonDifference, int termNumber) {
        validateInputs(firstTerm, commonDifference, termNumber);

        if (termNumber < 1) {
            throw ValidationException.invalidRange("termNumber", termNumber, 1, Integer.MAX_VALUE, OPERATION_NAME);
        }

        // a_n = a / (a + (n-1) * d)
        BigDecimal numerator = firstTerm;
        BigDecimal denominator = firstTerm.add(
            commonDifference.multiply(BigDecimal.valueOf(termNumber - 1), DEFAULT_PRECISION),
            DEFAULT_PRECISION
        );

        return numerator.divide(denominator, DEFAULT_PRECISION);
    }

    /**
     * Calculate sum of first n terms of a Harmonic Progression.
     *
     * This is more complex and involves harmonic series calculations.
     *
     * @param firstTerm first term (a)
     * @param commonDifference common difference (d)
     * @param numberOfTerms number of terms (n)
     * @return sum of first n terms
     */
    public BigDecimal harmonicProgressionSum(BigDecimal firstTerm, BigDecimal commonDifference, int numberOfTerms) {
        validateInputs(firstTerm, commonDifference, numberOfTerms);

        if (numberOfTerms < 1) {
            throw ValidationException.invalidRange("numberOfTerms", numberOfTerms, 1, Integer.MAX_VALUE, OPERATION_NAME);
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 1; i <= numberOfTerms; i++) {
            sum = sum.add(harmonicProgressionTerm(firstTerm, commonDifference, i), DEFAULT_PRECISION);
        }

        return sum;
    }

    // ===== ARITHMETIC-GEOMETRIC PROGRESSION =====

    /**
     * Calculate nth term of an Arithmetic-Geometric Progression.
     *
     * Formula: a_n = a * r^(n-1) + (n-1) * d
     *
     * @param firstTerm first term (a)
     * @param commonRatio common ratio (r)
     * @param commonDifference common difference (d)
     * @param termNumber term number (n)
     * @return nth term of the AGP
     */
    public BigDecimal arithmeticGeometricProgressionTerm(BigDecimal firstTerm, BigDecimal commonRatio,
                                                       BigDecimal commonDifference, int termNumber) {
        validateInputs(firstTerm, commonRatio, commonDifference, termNumber);

        if (termNumber < 1) {
            throw ValidationException.invalidRange("termNumber", termNumber, 1, Integer.MAX_VALUE, OPERATION_NAME);
        }

        // a_n = a * r^(n-1) + (n-1) * d
        BigDecimal geometricPart = geometricProgressionTerm(firstTerm, commonRatio, termNumber);
        BigDecimal arithmeticPart = commonDifference.multiply(BigDecimal.valueOf(termNumber - 1), DEFAULT_PRECISION);

        return geometricPart.add(arithmeticPart, DEFAULT_PRECISION);
    }

    // ===== INFINITE SERIES =====

    /**
     * Calculate sum of infinite geometric series (if convergent).
     *
     * @param firstTerm first term
     * @param commonRatio common ratio
     * @return infinite sum
     */
    public BigDecimal infiniteGeometricSeries(BigDecimal firstTerm, BigDecimal commonRatio) {
        return geometricProgressionInfiniteSum(firstTerm, commonRatio);
    }

    /**
     * Test convergence of a geometric series.
     *
     * @param commonRatio common ratio
     * @return true if series converges, false otherwise
     */
    public boolean isGeometricSeriesConvergent(BigDecimal commonRatio) {
        validateInputs(commonRatio);
        return commonRatio.abs().compareTo(BigDecimal.ONE) < 0;
    }

    /**
     * Calculate partial sum of harmonic series.
     *
     * Formula: H_n = Σ(1/k) from k=1 to n
     *
     * @param n number of terms
     * @return partial sum of harmonic series
     */
    public BigDecimal harmonicSeriesSum(int n) {
        validateInputs(n);

        if (n < 1) {
            throw ValidationException.invalidRange("n", n, 1, Integer.MAX_VALUE, OPERATION_NAME);
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int k = 1; k <= n; k++) {
            sum = sum.add(BigDecimal.ONE.divide(BigDecimal.valueOf(k), DEFAULT_PRECISION), DEFAULT_PRECISION);
        }

        return sum;
    }

    // ===== UTILITY METHODS =====

    /**
     * Fast exponentiation using BigDecimal.
     */
    private BigDecimal power(BigDecimal base, BigDecimal exponent) {
        // For integer exponents, use efficient algorithm
        if (exponent.stripTrailingZeros().scale() <= 0) {
            return power(base, exponent.intValue());
        }

        // For non-integer exponents, use BigDecimal pow
        try {
            return base.pow(exponent.intValue(), DEFAULT_PRECISION);
        } catch (Exception e) {
            // Fallback for very large exponents
            return BigDecimal.valueOf(Math.pow(base.doubleValue(), exponent.doubleValue()));
        }
    }

    /**
     * Fast integer exponentiation.
     */
    private BigDecimal power(BigDecimal base, int exponent) {
        if (exponent == 0) return BigDecimal.ONE;
        if (exponent == 1) return base;

        BigDecimal result = BigDecimal.ONE;
        BigDecimal current = base;

        while (exponent > 0) {
            if ((exponent & 1) == 1) {
                result = result.multiply(current, DEFAULT_PRECISION);
            }
            current = current.multiply(current, DEFAULT_PRECISION);
            exponent >>= 1;
        }

        return result;
    }

    /**
     * Get current memory usage in bytes.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // ===== SEQUENCE ANALYSIS =====

    /**
     * Check if a sequence is arithmetic.
     *
     * @param sequence list of numbers
     * @return common difference if arithmetic, null otherwise
     */
    public BigDecimal isArithmeticSequence(List<BigDecimal> sequence) {
        validateInputs(sequence);

        if (sequence.size() < 3) {
            return null; // Need at least 3 terms to determine
        }

        BigDecimal diff1 = sequence.get(1).subtract(sequence.get(0), DEFAULT_PRECISION);
        BigDecimal diff2 = sequence.get(2).subtract(sequence.get(1), DEFAULT_PRECISION);

        if (diff1.compareTo(diff2) == 0) {
            // Check remaining terms
            for (int i = 3; i < sequence.size(); i++) {
                BigDecimal currentDiff = sequence.get(i).subtract(sequence.get(i-1), DEFAULT_PRECISION);
                if (currentDiff.compareTo(diff1) != 0) {
                    return null;
                }
            }
            return diff1;
        }

        return null;
    }

    /**
     * Check if a sequence is geometric.
     *
     * @param sequence list of numbers
     * @return common ratio if geometric, null otherwise
     */
    public BigDecimal isGeometricSequence(List<BigDecimal> sequence) {
        validateInputs(sequence);

        if (sequence.size() < 3) {
            return null; // Need at least 3 terms to determine
        }

        // Avoid division by zero
        if (sequence.get(0).compareTo(BigDecimal.ZERO) == 0 ||
            sequence.get(1).compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal ratio1 = sequence.get(1).divide(sequence.get(0), DEFAULT_PRECISION);
        BigDecimal ratio2 = sequence.get(2).divide(sequence.get(1), DEFAULT_PRECISION);

        if (ratio1.compareTo(ratio2) == 0) {
            // Check remaining terms
            for (int i = 3; i < sequence.size(); i++) {
                if (sequence.get(i-1).compareTo(BigDecimal.ZERO) == 0) {
                    return null;
                }
                BigDecimal currentRatio = sequence.get(i).divide(sequence.get(i-1), DEFAULT_PRECISION);
                if (currentRatio.compareTo(ratio1) != 0) {
                    return null;
                }
            }
            return ratio1;
        }

        return null;
    }

    /**
     * Calculate common difference of an arithmetic sequence.
     *
     * @param sequence arithmetic sequence
     * @return common difference
     */
    public BigDecimal getCommonDifference(List<BigDecimal> sequence) {
        BigDecimal commonDiff = isArithmeticSequence(sequence);
        if (commonDiff == null) {
            throw new ValidationException("Sequence is not arithmetic", OPERATION_NAME);
        }
        return commonDiff;
    }

    /**
     * Calculate common ratio of a geometric sequence.
     *
     * @param sequence geometric sequence
     * @return common ratio
     */
    public BigDecimal getCommonRatio(List<BigDecimal> sequence) {
        BigDecimal commonRatio = isGeometricSequence(sequence);
        if (commonRatio == null) {
            throw new ValidationException("Sequence is not geometric", OPERATION_NAME);
        }
        return commonRatio;
    }
}
