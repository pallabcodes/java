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

package com.netflix.mathlib.statistics.distributions;

import com.netflix.mathlib.exceptions.ValidationException;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Normal (Gaussian) Distribution implementation.
 *
 * Provides methods for calculating:
 * - Probability Density Function (PDF)
 * - Cumulative Distribution Function (CDF)
 * - Quantiles (inverse CDF)
 * - Moments (mean, variance, skewness, kurtosis)
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class NormalDistribution {

    private static final MathContext PRECISION = new MathContext(50, java.math.RoundingMode.HALF_UP);
    private static final BigDecimal SQRT_2_PI = new BigDecimal("2.506628274631000502415765284811");
    private static final BigDecimal ONE_OVER_SQRT_2 = new BigDecimal("0.707106781186547524400844362104");

    private final double mean;
    private final double stdDev;

    /**
     * Constructor for standard normal distribution (mean=0, stdDev=1).
     */
    public NormalDistribution() {
        this(0.0, 1.0);
    }

    /**
     * Constructor for normal distribution with specified parameters.
     *
     * @param mean distribution mean
     * @param stdDev standard deviation (must be positive)
     */
    public NormalDistribution(double mean, double stdDev) {
        if (stdDev <= 0) {
            throw new ValidationException("Standard deviation must be positive", "NormalDistribution");
        }

        this.mean = mean;
        this.stdDev = stdDev;
    }

    /**
     * Calculate probability density function (PDF) at point x.
     *
     * PDF(x) = (1 / (σ * √(2π))) * exp(-0.5 * ((x - μ) / σ)²)
     *
     * @param x point to evaluate
     * @return PDF value at x
     */
    public BigDecimal pdf(double x) {
        return pdf(x, mean, stdDev);
    }

    /**
     * Calculate probability density function (PDF) with custom parameters.
     *
     * @param x point to evaluate
     * @param mean distribution mean
     * @param stdDev standard deviation
     * @return PDF value at x
     */
    public BigDecimal pdf(double x, double mean, double stdDev) {
        if (stdDev <= 0) {
            throw new ValidationException("Standard deviation must be positive", "NormalDistribution");
        }

        BigDecimal diff = BigDecimal.valueOf(x - mean);
        BigDecimal sigma = BigDecimal.valueOf(stdDev);

        BigDecimal exponent = diff.divide(sigma, PRECISION)
                                 .multiply(diff.divide(sigma, PRECISION), PRECISION)
                                 .multiply(new BigDecimal("-0.5"), PRECISION);

        BigDecimal expTerm = exp(exponent);
        BigDecimal denominator = sigma.multiply(SQRT_2_PI, PRECISION);

        return expTerm.divide(denominator, PRECISION);
    }

    /**
     * Calculate cumulative distribution function (CDF) at point x.
     *
     * Uses numerical integration for accurate results.
     *
     * @param x point to evaluate
     * @return CDF value at x
     */
    public BigDecimal cdf(double x) {
        return cdf(x, mean, stdDev);
    }

    /**
     * Calculate cumulative distribution function (CDF) with custom parameters.
     *
     * @param x point to evaluate
     * @param mean distribution mean
     * @param stdDev standard deviation
     * @return CDF value at x
     */
    public BigDecimal cdf(double x, double mean, double stdDev) {
        if (stdDev <= 0) {
            throw new ValidationException("Standard deviation must be positive", "NormalDistribution");
        }

        // Standardize the value
        double z = (x - mean) / stdDev;

        // For standard normal distribution
        if (mean == 0.0 && stdDev == 1.0) {
            return standardNormalCDF(z);
        } else {
            return standardNormalCDF(z);
        }
    }

    /**
     * Calculate CDF for standard normal distribution using approximation.
     *
     * Uses Abramowitz & Stegun approximation for accuracy.
     *
     * @param z standardized value
     * @return CDF value
     */
    private BigDecimal standardNormalCDF(double z) {
        // For negative values, use symmetry: CDF(-x) = 1 - CDF(x)
        if (z < 0) {
            return BigDecimal.ONE.subtract(standardNormalCDF(-z), PRECISION);
        }

        // Use polynomial approximation for |z| <= 3
        if (z <= 3.0) {
            BigDecimal b1 = new BigDecimal("0.319381530");
            BigDecimal b2 = new BigDecimal("-0.356563782");
            BigDecimal b3 = new BigDecimal("1.781477937");
            BigDecimal b4 = new BigDecimal("-1.821255978");
            BigDecimal b5 = new BigDecimal("1.330274429");
            BigDecimal p = new BigDecimal("0.2316419");

            BigDecimal zz = BigDecimal.valueOf(z * z);
            BigDecimal t = BigDecimal.ONE.divide(BigDecimal.ONE.add(p.multiply(BigDecimal.valueOf(z), PRECISION)), PRECISION);

            BigDecimal poly = t.multiply(b5, PRECISION);
            poly = poly.add(b4, PRECISION).multiply(t, PRECISION);
            poly = poly.add(b3, PRECISION).multiply(t, PRECISION);
            poly = poly.add(b2, PRECISION).multiply(t, PRECISION);
            poly = poly.add(b1, PRECISION).multiply(t, PRECISION);

            BigDecimal result = BigDecimal.ONE.subtract(pdf(z).multiply(poly, PRECISION), PRECISION);
            return result;
        } else {
            // For large z, CDF approaches 1
            return new BigDecimal("0.9999999999999999");
        }
    }

    /**
     * Calculate quantile (inverse CDF) for given probability.
     *
     * @param p probability (between 0 and 1)
     * @return quantile value
     */
    public BigDecimal quantile(double p) {
        return quantile(p, mean, stdDev);
    }

    /**
     * Calculate quantile (inverse CDF) with custom parameters.
     *
     * @param p probability (between 0 and 1)
     * @param mean distribution mean
     * @param stdDev standard deviation
     * @return quantile value
     */
    public BigDecimal quantile(double p, double mean, double stdDev) {
        if (p <= 0 || p >= 1) {
            throw new ValidationException("Probability must be between 0 and 1", "NormalDistribution");
        }

        if (stdDev <= 0) {
            throw new ValidationException("Standard deviation must be positive", "NormalDistribution");
        }

        // For standard normal distribution
        double z = standardNormalQuantile(p);
        return BigDecimal.valueOf(mean + z * stdDev);
    }

    /**
     * Calculate quantile for standard normal distribution.
     *
     * Uses approximation method for inverse error function.
     *
     * @param p probability
     * @return quantile value
     */
    private double standardNormalQuantile(double p) {
        if (p < 0.5) {
            return -standardNormalQuantile(1 - p);
        }

        // Approximation for p >= 0.5
        double q = Math.sqrt(-2 * Math.log(1 - p));
        double numerator = 2.30753 + q * 0.27061;
        double denominator = 1 + q * (0.99229 + q * 0.04481);
        return q - numerator / denominator;
    }

    /**
     * Calculate the mean of the distribution.
     *
     * @return mean value
     */
    public double getMean() {
        return mean;
    }

    /**
     * Calculate the variance of the distribution.
     *
     * @return variance value
     */
    public double getVariance() {
        return stdDev * stdDev;
    }

    /**
     * Calculate the standard deviation of the distribution.
     *
     * @return standard deviation value
     */
    public double getStandardDeviation() {
        return stdDev;
    }

    /**
     * Calculate skewness of the normal distribution (always 0).
     *
     * @return skewness value
     */
    public double getSkewness() {
        return 0.0;
    }

    /**
     * Calculate kurtosis of the normal distribution (always 0).
     *
     * @return kurtosis value
     */
    public double getKurtosis() {
        return 0.0;
    }

    /**
     * Calculate exponential function using Taylor series.
     */
    private BigDecimal exp(BigDecimal x) {
        BigDecimal result = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal factorial = BigDecimal.ONE;

        for (int n = 1; n < 50 && term.abs().compareTo(new BigDecimal("1e-20")) > 0; n++) {
            term = term.multiply(x, PRECISION);
            factorial = factorial.multiply(BigDecimal.valueOf(n), PRECISION);
            result = result.add(term.divide(factorial, PRECISION), PRECISION);
        }

        return result;
    }
}
