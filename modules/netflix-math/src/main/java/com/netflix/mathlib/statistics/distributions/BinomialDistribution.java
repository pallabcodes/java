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
 * Binomial Distribution implementation.
 *
 * Models the number of successes in a fixed number of independent Bernoulli trials.
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
public class BinomialDistribution {

    private static final MathContext PRECISION = new MathContext(50, java.math.RoundingMode.HALF_UP);

    private final int n;
    private final double p;

    /**
     * Constructor for binomial distribution.
     *
     * @param n number of trials (must be non-negative)
     * @param p success probability (must be between 0 and 1)
     */
    public BinomialDistribution(int n, double p) {
        if (n < 0) {
            throw new ValidationException("Number of trials must be non-negative", "BinomialDistribution");
        }

        if (p < 0 || p > 1) {
            throw new ValidationException("Success probability must be between 0 and 1", "BinomialDistribution");
        }

        this.n = n;
        this.p = p;
    }

    /**
     * Calculate probability mass function (PMF) at point k.
     *
     * PMF(k) = C(n,k) * p^k * (1-p)^(n-k)
     *
     * @param k number of successes
     * @return PMF value at k
     */
    public BigDecimal pmf(int k) {
        return pmf(k, n, p);
    }

    /**
     * Calculate probability mass function (PMF) with custom parameters.
     *
     * @param k number of successes
     * @param n number of trials
     * @param p success probability
     * @return PMF value at k
     */
    public BigDecimal pmf(int k, int n, double p) {
        if (k < 0 || k > n) {
            return BigDecimal.ZERO;
        }

        if (n < 0) {
            throw new ValidationException("Number of trials must be non-negative", "BinomialDistribution");
        }

        if (p < 0 || p > 1) {
            throw new ValidationException("Success probability must be between 0 and 1", "BinomialDistribution");
        }

        // Calculate binomial coefficient C(n,k)
        BigDecimal coefficient = binomialCoefficient(n, k);

        // Calculate p^k * (1-p)^(n-k)
        BigDecimal pTerm = BigDecimal.valueOf(Math.pow(p, k));
        BigDecimal qTerm = BigDecimal.valueOf(Math.pow(1 - p, n - k));

        return coefficient.multiply(pTerm, PRECISION).multiply(qTerm, PRECISION);
    }

    /**
     * Calculate cumulative distribution function (CDF) at point k.
     *
     * CDF(k) = Σ PMF(i) for i = 0 to k
     *
     * @param k number of successes
     * @return CDF value at k
     */
    public BigDecimal cdf(int k) {
        return cdf(k, n, p);
    }

    /**
     * Calculate cumulative distribution function (CDF) with custom parameters.
     *
     * @param k number of successes
     * @param n number of trials
     * @param p success probability
     * @return CDF value at k
     */
    public BigDecimal cdf(int k, int n, double p) {
        if (k < 0) {
            return BigDecimal.ZERO;
        }

        if (k >= n) {
            return BigDecimal.ONE;
        }

        BigDecimal cumulative = BigDecimal.ZERO;

        // Sum PMF from 0 to k
        for (int i = 0; i <= k; i++) {
            cumulative = cumulative.add(pmf(i, n, p), PRECISION);
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
        return quantile(probability, n, p);
    }

    /**
     * Calculate quantile (inverse CDF) with custom parameters.
     *
     * @param probability probability value (between 0 and 1)
     * @param n number of trials
     * @param p success probability
     * @return smallest k such that CDF(k) >= probability
     */
    public int quantile(double probability, int n, double p) {
        if (probability < 0 || probability > 1) {
            throw new ValidationException("Probability must be between 0 and 1", "BinomialDistribution");
        }

        BigDecimal target = BigDecimal.valueOf(probability);
        BigDecimal cumulative = BigDecimal.ZERO;

        for (int k = 0; k <= n; k++) {
            cumulative = cumulative.add(pmf(k, n, p), PRECISION);
            if (cumulative.compareTo(target) >= 0) {
                return k;
            }
        }

        return n;
    }

    /**
     * Calculate the mean of the binomial distribution.
     *
     * Mean = n * p
     *
     * @return mean value
     */
    public double getMean() {
        return n * p;
    }

    /**
     * Calculate the variance of the binomial distribution.
     *
     * Variance = n * p * (1-p)
     *
     * @return variance value
     */
    public double getVariance() {
        return n * p * (1 - p);
    }

    /**
     * Calculate the standard deviation of the binomial distribution.
     *
     * Standard Deviation = sqrt(variance)
     *
     * @return standard deviation value
     */
    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    /**
     * Calculate skewness of the binomial distribution.
     *
     * Skewness = (1 - 2p) / sqrt(n*p*(1-p))
     *
     * @return skewness value
     */
    public double getSkewness() {
        double variance = getVariance();
        if (variance == 0) {
            return 0.0; // No variation
        }
        return (1 - 2 * p) / Math.sqrt(variance);
    }

    /**
     * Calculate kurtosis of the binomial distribution.
     *
     * Kurtosis = (1 - 6p*(1-p)) / (n*p*(1-p))
     *
     * @return kurtosis value
     */
    public double getKurtosis() {
        double mean = getMean();
        double variance = getVariance();

        if (variance == 0) {
            return 0.0; // No variation
        }

        return (1 - 6 * p * (1 - p)) / variance;
    }

    /**
     * Get the number of trials.
     *
     * @return number of trials
     */
    public int getNumberOfTrials() {
        return n;
    }

    /**
     * Get the success probability.
     *
     * @return success probability
     */
    public double getSuccessProbability() {
        return p;
    }

    /**
     * Calculate binomial coefficient C(n, k) = n! / (k! * (n-k)!)
     *
     * Uses multiplicative formula to avoid large intermediate results.
     *
     * @param n total items
     * @param k items to choose
     * @return binomial coefficient
     */
    private BigDecimal binomialCoefficient(int n, int k) {
        if (k < 0 || k > n) {
            return BigDecimal.ZERO;
        }

        if (k == 0 || k == n) {
            return BigDecimal.ONE;
        }

        // Use symmetry: C(n,k) = C(n,n-k)
        if (k > n - k) {
            k = n - k;
        }

        BigDecimal result = BigDecimal.ONE;

        // Calculate using multiplicative formula: C(n,k) = ∏(n-k+i)/i for i=1 to k
        for (int i = 1; i <= k; i++) {
            BigDecimal numerator = BigDecimal.valueOf(n - k + i);
            BigDecimal denominator = BigDecimal.valueOf(i);
            result = result.multiply(numerator, PRECISION).divide(denominator, PRECISION);
        }

        return result;
    }
}
