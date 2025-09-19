/*
 * Copyright 2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.w3.org/2001/XMLSchema-instance
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.mathlib.statistics.measures;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

/**
 * Descriptive Statistics - Advanced statistical measures and analysis.
 *
 * Provides comprehensive descriptive statistics including:
 * - Central tendency measures (mean, median, mode, geometric mean, harmonic mean)
 * - Dispersion measures (range, variance, standard deviation, quartiles, IQR)
 * - Shape measures (skewness, kurtosis)
 * - Position measures (percentiles, quartiles, deciles)
 * - Concentration measures (Gini coefficient)
 * - Robust statistics (median absolute deviation, trimmed mean)
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class DescriptiveStatistics implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(DescriptiveStatistics.class);
    private static final String OPERATION_NAME = "DescriptiveStatistics";
    private static final String COMPLEXITY = "O(n log n)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;
    private final MathContext DEFAULT_PRECISION = new MathContext(50, java.math.RoundingMode.HALF_UP);

    /**
     * Constructor for Descriptive Statistics.
     */
    public DescriptiveStatistics() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Descriptive Statistics module");
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

    // ===== CENTRAL TENDENCY MEASURES =====

    /**
     * Calculate geometric mean of positive numbers.
     *
     * Geometric Mean = (∏ x_i)^(1/n)
     *
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     *
     * @param data array of positive numbers
     * @return geometric mean
     */
    public BigDecimal geometricMean(double[] data) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs((Object) data);

            if (data.length == 0) {
                throw ValidationException.emptyParameter("data", OPERATION_NAME);
            }

            logger.debug("Calculating geometric mean of {} data points", data.length);

            BigDecimal product = BigDecimal.ONE;

            // Check for non-positive values
            for (double value : data) {
                if (value <= 0) {
                    throw new ValidationException("Geometric mean requires all positive values", OPERATION_NAME);
                }
                product = product.multiply(BigDecimal.valueOf(value), DEFAULT_PRECISION);
            }

            // Calculate nth root
            BigDecimal n = BigDecimal.valueOf(data.length);
            BigDecimal geometricMean = nthRoot(product, n);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Geometric mean result: {}", geometricMean);

            return geometricMean;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating geometric mean: {}", e.getMessage());
            throw new ValidationException("Failed to calculate geometric mean: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Calculate harmonic mean of positive numbers.
     *
     * Harmonic Mean = n / Σ(1/x_i)
     *
     * @param data array of positive numbers
     * @return harmonic mean
     */
    public BigDecimal harmonicMean(double[] data) {
        validateInputs((Object) data);

        if (data.length == 0) {
            throw ValidationException.emptyParameter("data", OPERATION_NAME);
        }

        BigDecimal sumReciprocals = BigDecimal.ZERO;

        for (double value : data) {
            if (value <= 0) {
                throw new ValidationException("Harmonic mean requires all positive values", OPERATION_NAME);
            }
            sumReciprocals = sumReciprocals.add(BigDecimal.ONE.divide(BigDecimal.valueOf(value), DEFAULT_PRECISION), DEFAULT_PRECISION);
        }

        BigDecimal n = BigDecimal.valueOf(data.length);
        return n.divide(sumReciprocals, DEFAULT_PRECISION);
    }

    /**
     * Calculate trimmed mean (mean after removing outliers).
     *
     * @param data array of numbers
     * @param trimPercentage percentage of data to trim from each end (0-50)
     * @return trimmed mean
     */
    public BigDecimal trimmedMean(double[] data, double trimPercentage) {
        validateInputs((Object) data);

        if (trimPercentage < 0 || trimPercentage >= 50) {
            throw new ValidationException("Trim percentage must be between 0 and 50", OPERATION_NAME);
        }

        if (data.length == 0) {
            throw ValidationException.emptyParameter("data", OPERATION_NAME);
        }

        double[] sorted = Arrays.copyOf(data, data.length);
        Arrays.sort(sorted);

        int trimCount = (int) Math.round(data.length * trimPercentage / 100.0);
        int start = trimCount;
        int end = data.length - trimCount;

        if (start >= end) {
            throw new ValidationException("Trim percentage too high for dataset size", OPERATION_NAME);
        }

        double sum = 0;
        for (int i = start; i < end; i++) {
            sum += sorted[i];
        }

        return BigDecimal.valueOf(sum / (end - start));
    }

    // ===== DISPERSION MEASURES =====

    /**
     * Calculate range of a dataset.
     *
     * Range = max - min
     *
     * @param data array of numbers
     * @return range value
     */
    public BigDecimal range(double[] data) {
        validateInputs((Object) data);

        if (data.length == 0) {
            throw ValidationException.emptyParameter("data", OPERATION_NAME);
        }

        double min = Arrays.stream(data).min().orElse(0);
        double max = Arrays.stream(data).max().orElse(0);

        return BigDecimal.valueOf(max - min);
    }

    /**
     * Calculate interquartile range (IQR).
     *
     * IQR = Q3 - Q1
     *
     * @param data array of numbers
     * @return IQR value
     */
    public BigDecimal interquartileRange(double[] data) {
        validateInputs((Object) data);

        if (data.length < 4) {
            throw new ValidationException("Need at least 4 data points for IQR", OPERATION_NAME);
        }

        double[] sorted = Arrays.copyOf(data, data.length);
        Arrays.sort(sorted);

        BigDecimal q1 = percentile(sorted, 25);
        BigDecimal q3 = percentile(sorted, 75);

        return q3.subtract(q1, DEFAULT_PRECISION);
    }

    /**
     * Calculate median absolute deviation (MAD).
     *
     * MAD = median(|x_i - median(X)|)
     *
     * @param data array of numbers
     * @return MAD value
     */
    public BigDecimal medianAbsoluteDeviation(double[] data) {
        validateInputs((Object) data);

        if (data.length == 0) {
            throw ValidationException.emptyParameter("data", OPERATION_NAME);
        }

        BigDecimal median = BigDecimal.valueOf(median(data));

        double[] absoluteDeviations = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            absoluteDeviations[i] = Math.abs(data[i] - median.doubleValue());
        }

        return BigDecimal.valueOf(median(absoluteDeviations));
    }

    // ===== POSITION MEASURES =====

    /**
     * Calculate percentile of a dataset.
     *
     * @param data array of numbers
     * @param percentile percentile value (0-100)
     * @return percentile value
     */
    public BigDecimal percentile(double[] data, double percentile) {
        validateInputs((Object) data);

        if (percentile < 0 || percentile > 100) {
            throw new ValidationException("Percentile must be between 0 and 100", OPERATION_NAME);
        }

        if (data.length == 0) {
            throw ValidationException.emptyParameter("data", OPERATION_NAME);
        }

        double[] sorted = Arrays.copyOf(data, data.length);
        Arrays.sort(sorted);

        if (percentile == 0) {
            return BigDecimal.valueOf(sorted[0]);
        }

        if (percentile == 100) {
            return BigDecimal.valueOf(sorted[sorted.length - 1]);
        }

        double index = (percentile / 100.0) * (sorted.length - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);

        if (lower == upper) {
            return BigDecimal.valueOf(sorted[lower]);
        }

        // Linear interpolation
        double fraction = index - lower;
        double interpolated = sorted[lower] + fraction * (sorted[upper] - sorted[lower]);
        return BigDecimal.valueOf(interpolated);
    }

    /**
     * Calculate quartiles of a dataset.
     *
     * @param data array of numbers
     * @return array containing [Q1, Q2, Q3]
     */
    public BigDecimal[] quartiles(double[] data) {
        validateInputs((Object) data);

        if (data.length < 4) {
            throw new ValidationException("Need at least 4 data points for quartiles", OPERATION_NAME);
        }

        BigDecimal q1 = percentile(data, 25);
        BigDecimal q2 = percentile(data, 50);
        BigDecimal q3 = percentile(data, 75);

        return new BigDecimal[]{q1, q2, q3};
    }

    /**
     * Calculate deciles of a dataset.
     *
     * @param data array of numbers
     * @return array containing deciles (D1 through D9)
     */
    public BigDecimal[] deciles(double[] data) {
        validateInputs((Object) data);

        if (data.length < 10) {
            throw new ValidationException("Need at least 10 data points for deciles", OPERATION_NAME);
        }

        BigDecimal[] deciles = new BigDecimal[9];
        for (int i = 0; i < 9; i++) {
            deciles[i] = percentile(data, (i + 1) * 10.0);
        }

        return deciles;
    }

    // ===== SHAPE MEASURES =====

    /**
     * Calculate skewness of a dataset.
     *
     * Skewness measures asymmetry of the distribution.
     * - Negative: left-skewed (long left tail)
     * - Positive: right-skewed (long right tail)
     * - Zero: symmetric
     *
     * @param data array of numbers
     * @return skewness value
     */
    public BigDecimal skewness(double[] data) {
        validateInputs((Object) data);

        if (data.length < 3) {
            throw new ValidationException("Need at least 3 data points for skewness", OPERATION_NAME);
        }

        BigDecimal mean = BigDecimal.valueOf(mean(data));
        BigDecimal stdDev = BigDecimal.valueOf(standardDeviation(data, false));

        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // No variation
        }

        BigDecimal sumCubedDeviations = BigDecimal.ZERO;

        for (double value : data) {
            BigDecimal deviation = BigDecimal.valueOf(value).subtract(mean, DEFAULT_PRECISION);
            BigDecimal cubedDeviation = deviation.multiply(deviation, DEFAULT_PRECISION)
                                                .multiply(deviation, DEFAULT_PRECISION);
            sumCubedDeviations = sumCubedDeviations.add(cubedDeviation, DEFAULT_PRECISION);
        }

        BigDecimal n = BigDecimal.valueOf(data.length);
        BigDecimal moment3 = sumCubedDeviations.divide(n, DEFAULT_PRECISION);
        BigDecimal stdDevCubed = stdDev.multiply(stdDev, DEFAULT_PRECISION)
                                      .multiply(stdDev, DEFAULT_PRECISION);

        return moment3.divide(stdDevCubed, DEFAULT_PRECISION);
    }

    /**
     * Calculate kurtosis of a dataset.
     *
     * Kurtosis measures "tailedness" of the distribution.
     * - Negative: lighter tails (platykurtic)
     * - Positive: heavier tails (leptokurtic)
     * - Zero: normal tails (mesokurtic)
     *
     * @param data array of numbers
     * @return kurtosis value
     */
    public BigDecimal kurtosis(double[] data) {
        validateInputs((Object) data);

        if (data.length < 4) {
            throw new ValidationException("Need at least 4 data points for kurtosis", OPERATION_NAME);
        }

        BigDecimal mean = BigDecimal.valueOf(mean(data));
        BigDecimal stdDev = BigDecimal.valueOf(standardDeviation(data, false));

        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // No variation
        }

        BigDecimal sumFourthDeviations = BigDecimal.ZERO;

        for (double value : data) {
            BigDecimal deviation = BigDecimal.valueOf(value).subtract(mean, DEFAULT_PRECISION);
            BigDecimal fourthDeviation = deviation.multiply(deviation, DEFAULT_PRECISION)
                                                 .multiply(deviation, DEFAULT_PRECISION)
                                                 .multiply(deviation, DEFAULT_PRECISION);
            sumFourthDeviations = sumFourthDeviations.add(fourthDeviation, DEFAULT_PRECISION);
        }

        BigDecimal n = BigDecimal.valueOf(data.length);
        BigDecimal moment4 = sumFourthDeviations.divide(n, DEFAULT_PRECISION);
        BigDecimal stdDevFourth = stdDev.multiply(stdDev, DEFAULT_PRECISION)
                                       .multiply(stdDev, DEFAULT_PRECISION)
                                       .multiply(stdDev, DEFAULT_PRECISION);

        BigDecimal excessKurtosis = moment4.divide(stdDevFourth, DEFAULT_PRECISION)
                                         .subtract(BigDecimal.valueOf(3), DEFAULT_PRECISION);

        return excessKurtosis;
    }

    // ===== CONCENTRATION MEASURES =====

    /**
     * Calculate Gini coefficient (measure of inequality).
     *
     * Gini coefficient ranges from 0 (perfect equality) to 1 (perfect inequality).
     *
     * @param data array of numbers (representing income, wealth, etc.)
     * @return Gini coefficient
     */
    public BigDecimal giniCoefficient(double[] data) {
        validateInputs((Object) data);

        if (data.length < 2) {
            throw new ValidationException("Need at least 2 data points for Gini coefficient", OPERATION_NAME);
        }

        double[] sorted = Arrays.copyOf(data, data.length);
        Arrays.sort(sorted);

        double sumAbsoluteDifferences = 0;
        double sumValues = 0;

        for (int i = 0; i < sorted.length; i++) {
            sumValues += sorted[i];
            for (int j = 0; j < sorted.length; j++) {
                sumAbsoluteDifferences += Math.abs(sorted[i] - sorted[j]);
            }
        }

        if (sumValues == 0) {
            return BigDecimal.ZERO; // All values are zero
        }

        double n = sorted.length;
        double gini = sumAbsoluteDifferences / (2 * n * n * sumValues / n);

        return BigDecimal.valueOf(gini);
    }

    // ===== UTILITY METHODS =====

    /**
     * Calculate nth root using Newton's method.
     */
    private BigDecimal nthRoot(BigDecimal value, BigDecimal n) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Cannot calculate nth root of negative number", OPERATION_NAME);
        }

        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Initial approximation
        BigDecimal x = BigDecimal.valueOf(Math.pow(value.doubleValue(), 1.0 / n.doubleValue()));

        // Newton's method: x_{n+1} = x_n - f(x_n)/f'(x_n)
        // For f(x) = x^n - value, f'(x) = n*x^(n-1)
        for (int i = 0; i < 50; i++) {
            BigDecimal xPowerN = power(x, n);
            BigDecimal derivative = n.multiply(power(x, n.subtract(BigDecimal.ONE, DEFAULT_PRECISION)), DEFAULT_PRECISION);
            BigDecimal numerator = xPowerN.subtract(value, DEFAULT_PRECISION);
            BigDecimal adjustment = numerator.divide(derivative, DEFAULT_PRECISION);
            x = x.subtract(adjustment, DEFAULT_PRECISION);
        }

        return x;
    }

    /**
     * Calculate power using BigDecimal.
     */
    private BigDecimal power(BigDecimal base, BigDecimal exponent) {
        // For integer exponents, use efficient algorithm
        if (exponent.stripTrailingZeros().scale() <= 0) {
            return power(base, exponent.intValue());
        }

        // For non-integer exponents, use approximation
        return BigDecimal.valueOf(Math.pow(base.doubleValue(), exponent.doubleValue()));
    }

    /**
     * Calculate integer power.
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
     * Calculate mean (helper method).
     */
    private double mean(double[] data) {
        double sum = 0;
        for (double value : data) {
            sum += value;
        }
        return sum / data.length;
    }

    /**
     * Calculate median (helper method).
     */
    private double median(double[] data) {
        double[] sorted = Arrays.copyOf(data, data.length);
        Arrays.sort(sorted);

        int n = sorted.length;
        if (n % 2 == 0) {
            return (sorted[n/2 - 1] + sorted[n/2]) / 2.0;
        } else {
            return sorted[n/2];
        }
    }

    /**
     * Calculate standard deviation (helper method).
     */
    private double standardDeviation(double[] data, boolean isPopulation) {
        double mean = mean(data);
        double sumSquaredDiffs = 0;

        for (double value : data) {
            double diff = value - mean;
            sumSquaredDiffs += diff * diff;
        }

        int divisor = isPopulation ? data.length : data.length - 1;
        return Math.sqrt(sumSquaredDiffs / divisor);
    }

    /**
     * Get current memory usage in bytes.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
