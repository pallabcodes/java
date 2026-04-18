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
import java.math.BigInteger;
import java.math.MathContext;

/**
 * Poisson Distribution implementation.
 *
 * Models the number of events occurring in a fixed interval of time/space
 * when these events occur with a known constant mean rate and independently.
 *
 * Provides methods for calculating:
 * - Probability Mass Function (PMF)
 * - Cumulative Distribution Function (CDF)
 * - Mean, variance, and other moments
 * - Quantiles
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class PoissonDistribution {

    private static final MathContext PRECISION = new MathContext(50, java.math.RoundingMode.HALF_UP);

    private final double lambda;

    /**
     * Constructor for Poisson distribution.
     *
     * @param lambda mean rate of events (must be positive)
     */
    public PoissonDistribution(double lambda) {
        if (lambda <= 0) {
            throw new ValidationException("Mean rate lambda must be positive", "PoissonDistribution");
        }

        this.lambda = lambda;
    }

    /**
     * Calculate probability mass function (PMF) at point k.
     *
     * PMF(k) = (e^(-λ) * λ^k) / k!
     *
     * @param k number of events
     * @return PMF value at k
     */
    public BigDecimal pmf(int k) {
        return pmf(k, lambda);
    }

    /**
     * Calculate probability mass function (PMF) with custom parameters.
     *
     * @param k number of events
     * @param lambda mean rate
     * @return PMF value at k
     */
    public BigDecimal pmf(int k, double lambda) {
        if (k < 0) {
            return BigDecimal.ZERO;
        }

        if (lambda <= 0) {
            throw new ValidationException("Mean rate lambda must be positive", "PoissonDistribution");
        }

        // Calculate e^(-λ)
        BigDecimal expNegLambda = exp(BigDecimal.valueOf(-lambda));

        // Calculate λ^k
        BigDecimal lambdaPowerK = BigDecimal.valueOf(Math.pow(lambda, k));

        // Calculate k!
        BigDecimal factorialK = factorial(k);

        return expNegLambda.multiply(lambdaPowerK, PRECISION).divide(factorialK, PRECISION);
    }

    /**
     * Calculate cumulative distribution function (CDF) at point k.
     *
     * CDF(k) = Σ PMF(i) for i = 0 to k
     *
     * @param k number of events
     * @return CDF value at k
     */
    public BigDecimal cdf(int k) {
        return cdf(k, lambda);
    }

    /**
     * Calculate cumulative distribution function (CDF) with custom parameters.
     *
     * @param k number of events
     * @param lambda mean rate
     * @return CDF value at k
     */
    public BigDecimal cdf(int k, double lambda) {
        if (k < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal cumulative = BigDecimal.ZERO;

        // Sum PMF from 0 to k
        for (int i = 0; i <= k; i++) {
            cumulative = cumulative.add(pmf(i, lambda), PRECISION);
        }

        return cumulative;
    }

    /**
     * Calculate quantile (inverse CDF) for given probability.
     *
     * @param probability probability value (between 0 and 1)
     * @return smallest k such that CDF(k) >= probability
     */
    public int quantile(double probability) {
        return quantile(probability, lambda);
    }

    /**
     * Calculate quantile (inverse CDF) with custom parameters.
     *
     * @param probability probability value (between 0 and 1)
     * @param lambda mean rate
     * @return smallest k such that CDF(k) >= probability
     */
    public int quantile(double probability, double lambda) {
        if (probability < 0 || probability > 1) {
            throw new ValidationException("Probability must be between 0 and 1", "PoissonDistribution");
        }

        if (lambda <= 0) {
            throw new ValidationException("Mean rate lambda must be positive", "PoissonDistribution");
        }

        BigDecimal target = BigDecimal.valueOf(probability);
        BigDecimal cumulative = BigDecimal.ZERO;
        int k = 0;

        // Find smallest k where CDF(k) >= probability
        while (cumulative.compareTo(target) < 0) {
            cumulative = cumulative.add(pmf(k, lambda), PRECISION);
            k++;
        }

        return k - 1; // Subtract 1 because we incremented k after the last addition
    }

    /**
     * Calculate the mean of the Poisson distribution.
     *
     * Mean = λ
     *
     * @return mean value
     */
    public double getMean() {
        return lambda;
    }

    /**
     * Calculate the variance of the Poisson distribution.
     *
     * Variance = λ
     *
     * @return variance value
     */
    public double getVariance() {
        return lambda;
    }

    /**
     * Calculate the standard deviation of the Poisson distribution.
     *
     * Standard Deviation = sqrt(λ)
     *
     * @return standard deviation value
     */
    public double getStandardDeviation() {
        return Math.sqrt(lambda);
    }

    /**
     * Calculate skewness of the Poisson distribution.
     *
     * Skewness = 1/sqrt(λ)
     *
     * @return skewness value
     */
    public double getSkewness() {
        return 1.0 / Math.sqrt(lambda);
    }

    /**
     * Calculate kurtosis of the Poisson distribution.
     *
     * Kurtosis = 1/λ
     *
     * @return kurtosis value
     */
    public double getKurtosis() {
        return 1.0 / lambda;
    }

    /**
     * Get the mean rate parameter.
     *
     * @return lambda value
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Calculate factorial of a number using BigInteger.
     *
     * @param n number to calculate factorial for
     * @return n!
     */
    private BigDecimal factorial(int n) {
        if (n < 0) {
            throw new ValidationException("Factorial is not defined for negative numbers", "PoissonDistribution");
        }

        if (n == 0 || n == 1) {
            return BigDecimal.ONE;
        }

        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }

        return new BigDecimal(result);
    }

    /**
     * Calculate exponential function using Taylor series.
     *
     * @param x exponent value
     * @return e^x
     */
    private BigDecimal exp(BigDecimal x) {
        BigDecimal result = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal factorial = BigDecimal.ONE;

        // Taylor series: e^x = Σ (x^n / n!)
        for (int n = 1; n < 50 && term.abs().compareTo(new BigDecimal("1e-20")) > 0; n++) {
            term = term.multiply(x, PRECISION);
            factorial = factorial.multiply(BigDecimal.valueOf(n), PRECISION);
            result = result.add(term.divide(factorial, PRECISION), PRECISION);
        }

        return result;
    }
}
