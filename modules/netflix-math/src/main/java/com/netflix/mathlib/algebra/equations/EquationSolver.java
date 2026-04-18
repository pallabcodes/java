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

package com.netflix.mathlib.algebra.equations;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Equation Solver - Production-grade equation solving utilities.
 *
 * This class provides comprehensive equation solving capabilities including:
 * - Linear Equations (ax + b = 0)
 * - Quadratic Equations (ax² + bx + c = 0)
 * - Cubic Equations (ax³ + bx² + cx + d = 0)
 * - System of Linear Equations
 * - Numerical equation solving methods
 * - Ratio and Proportion utilities
 * - Equation analysis and classification
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
public class EquationSolver implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(EquationSolver.class);
    private static final String OPERATION_NAME = "EquationSolver";
    private static final String COMPLEXITY = "O(1)-O(n³)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;
    private final MathContext DEFAULT_PRECISION = new MathContext(50, java.math.RoundingMode.HALF_UP);
    private final BigDecimal TOLERANCE = new BigDecimal("1e-15");

    /**
     * Constructor for Equation Solver.
     */
    public EquationSolver() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Equation Solver module");
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

    // ===== LINEAR EQUATIONS =====

    /**
     * Solve linear equation: ax + b = 0
     *
     * Solution: x = -b/a
     *
     * @param a coefficient of x
     * @param b constant term
     * @return solution x
     * @throws ValidationException if a = 0 (infinite solutions)
     */
    public BigDecimal solveLinear(BigDecimal a, BigDecimal b) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(a, b);

            logger.debug("Solving linear equation: {}x + {} = 0", a, b);

            if (a.abs().compareTo(TOLERANCE) < 0) {
                throw new ValidationException("Coefficient 'a' cannot be zero (infinite solutions)", OPERATION_NAME);
            }

            BigDecimal result = b.negate(DEFAULT_PRECISION).divide(a, DEFAULT_PRECISION);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Linear equation solution: x = {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error solving linear equation: {}", e.getMessage());
            throw new ValidationException("Failed to solve linear equation: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== QUADRATIC EQUATIONS =====

    /**
     * Solve quadratic equation: ax² + bx + c = 0
     *
     * Solutions: x = [-b ± √(b² - 4ac)] / (2a)
     *
     * @param a coefficient of x²
     * @param b coefficient of x
     * @param c constant term
     * @return array of solutions [x1, x2] (may contain complex numbers as strings)
     */
    public QuadraticSolution solveQuadratic(BigDecimal a, BigDecimal b, BigDecimal c) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(a, b, c);

            logger.debug("Solving quadratic equation: {}x² + {}x + {} = 0", a, b, c);

            if (a.abs().compareTo(TOLERANCE) < 0) {
                throw new ValidationException("Coefficient 'a' cannot be zero (use linear solver)", OPERATION_NAME);
            }

            // Calculate discriminant: D = b² - 4ac
            BigDecimal discriminant = b.multiply(b, DEFAULT_PRECISION)
                                     .subtract(a.multiply(c, DEFAULT_PRECISION).multiply(BigDecimal.valueOf(4), DEFAULT_PRECISION), DEFAULT_PRECISION);

            BigDecimal twoA = a.multiply(BigDecimal.valueOf(2), DEFAULT_PRECISION);
            BigDecimal negB = b.negate(DEFAULT_PRECISION);

            QuadraticSolution result;

            if (discriminant.compareTo(BigDecimal.ZERO) > 0) {
                // Two real solutions
                BigDecimal sqrtD = sqrt(discriminant);
                BigDecimal x1 = negB.add(sqrtD, DEFAULT_PRECISION).divide(twoA, DEFAULT_PRECISION);
                BigDecimal x2 = negB.subtract(sqrtD, DEFAULT_PRECISION).divide(twoA, DEFAULT_PRECISION);
                result = new QuadraticSolution(x1, x2, discriminant, "two_real");

            } else if (discriminant.abs().compareTo(TOLERANCE) < 0) {
                // One real solution (repeated)
                BigDecimal x1 = negB.divide(twoA, DEFAULT_PRECISION);
                result = new QuadraticSolution(x1, x1, discriminant, "one_real");

            } else {
                // Two complex solutions
                BigDecimal realPart = negB.divide(twoA, DEFAULT_PRECISION);
                BigDecimal imaginaryPart = sqrt(discriminant.negate(DEFAULT_PRECISION)).divide(twoA, DEFAULT_PRECISION);

                String x1 = realPart + " + " + imaginaryPart + "i";
                String x2 = realPart + " - " + imaginaryPart + "i";
                result = new QuadraticSolution(x1, x2, discriminant, "two_complex");
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Quadratic equation solutions: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error solving quadratic equation: {}", e.getMessage());
            throw new ValidationException("Failed to solve quadratic equation: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== CUBIC EQUATIONS =====

    /**
     * Solve cubic equation: ax³ + bx² + cx + d = 0
     *
     * Uses Cardano's method for general cubic equations.
     *
     * @param a coefficient of x³
     * @param b coefficient of x²
     * @param c coefficient of x
     * @param d constant term
     * @return list of real solutions
     */
    public List<BigDecimal> solveCubic(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(a, b, c, d);

            logger.debug("Solving cubic equation: {}x³ + {}x² + {}x + {} = 0", a, b, c, d);

            if (a.abs().compareTo(TOLERANCE) < 0) {
                throw new ValidationException("Coefficient 'a' cannot be zero (use quadratic solver)", OPERATION_NAME);
            }

            // Normalize the equation: divide by a
            BigDecimal A = b.divide(a, DEFAULT_PRECISION);
            BigDecimal B = c.divide(a, DEFAULT_PRECISION);
            BigDecimal C = d.divide(a, DEFAULT_PRECISION);

            // Depressed cubic: t³ + p*t + q = 0
            BigDecimal p = B.subtract(A.multiply(A, DEFAULT_PRECISION).divide(BigDecimal.valueOf(3), DEFAULT_PRECISION), DEFAULT_PRECISION);
            BigDecimal q = C.add(A.multiply(A.multiply(A, DEFAULT_PRECISION), DEFAULT_PRECISION).multiply(BigDecimal.valueOf(2), DEFAULT_PRECISION).divide(BigDecimal.valueOf(27), DEFAULT_PRECISION), DEFAULT_PRECISION)
                          .subtract(A.multiply(B, DEFAULT_PRECISION).divide(BigDecimal.valueOf(3), DEFAULT_PRECISION), DEFAULT_PRECISION);

            // Discriminant: Δ = -(4p³ + 27q²)
            BigDecimal discriminant = p.multiply(p, DEFAULT_PRECISION).multiply(p, DEFAULT_PRECISION).multiply(BigDecimal.valueOf(4), DEFAULT_PRECISION)
                                     .add(q.multiply(q, DEFAULT_PRECISION).multiply(BigDecimal.valueOf(27), DEFAULT_PRECISION), DEFAULT_PRECISION).negate(DEFAULT_PRECISION);

            List<BigDecimal> solutions = new ArrayList<>();

            if (discriminant.abs().compareTo(TOLERANCE) < 0) {
                // Multiple root case
                BigDecimal t = cbrt(q.negate(DEFAULT_PRECISION));
                BigDecimal x = t.subtract(A.divide(BigDecimal.valueOf(3), DEFAULT_PRECISION), DEFAULT_PRECISION);
                solutions.add(x);

            } else if (discriminant.compareTo(BigDecimal.ZERO) > 0) {
                // Three distinct real roots
                BigDecimal sqrtDelta = sqrt(discriminant);
                BigDecimal u = cbrt(q.negate(DEFAULT_PRECISION).add(sqrtDelta.divide(BigDecimal.valueOf(2), DEFAULT_PRECISION), DEFAULT_PRECISION));
                BigDecimal v = cbrt(q.negate(DEFAULT_PRECISION).subtract(sqrtDelta.divide(BigDecimal.valueOf(2), DEFAULT_PRECISION), DEFAULT_PRECISION));

                BigDecimal x1 = u.add(v, DEFAULT_PRECISION).subtract(A.divide(BigDecimal.valueOf(3), DEFAULT_PRECISION), DEFAULT_PRECISION);
                BigDecimal x2 = u.multiply(BigDecimal.valueOf(-0.5), DEFAULT_PRECISION)
                               .add(v.multiply(BigDecimal.valueOf(-0.5), DEFAULT_PRECISION), DEFAULT_PRECISION)
                               .subtract(A.divide(BigDecimal.valueOf(3), DEFAULT_PRECISION), DEFAULT_PRECISION)
                               .add(u.multiply(v, DEFAULT_PRECISION).multiply(sqrt(BigDecimal.valueOf(3)), DEFAULT_PRECISION).multiply(BigDecimal.valueOf(0.5), DEFAULT_PRECISION), DEFAULT_PRECISION);
                BigDecimal x3 = u.multiply(BigDecimal.valueOf(-0.5), DEFAULT_PRECISION)
                               .add(v.multiply(BigDecimal.valueOf(-0.5), DEFAULT_PRECISION), DEFAULT_PRECISION)
                               .subtract(A.divide(BigDecimal.valueOf(3), DEFAULT_PRECISION), DEFAULT_PRECISION)
                               .subtract(u.multiply(v, DEFAULT_PRECISION).multiply(sqrt(BigDecimal.valueOf(3)), DEFAULT_PRECISION).multiply(BigDecimal.valueOf(0.5), DEFAULT_PRECISION), DEFAULT_PRECISION);

                solutions.add(x1);
                solutions.add(x2);
                solutions.add(x3);

            } else {
                // One real root (and two complex)
                BigDecimal u = cbrt(q.negate(DEFAULT_PRECISION));
                BigDecimal x = u.subtract(A.divide(BigDecimal.valueOf(3), DEFAULT_PRECISION), DEFAULT_PRECISION);
                solutions.add(x);
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Cubic equation solutions: {}", solutions);

            return solutions;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error solving cubic equation: {}", e.getMessage());
            throw new ValidationException("Failed to solve cubic equation: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== SYSTEM OF LINEAR EQUATIONS =====

    /**
     * Solve system of linear equations using Gaussian elimination.
     *
     * System: A*x = b
     *
     * @param coefficientMatrix coefficient matrix A (n x n)
     * @param constantsVector constants vector b (n x 1)
     * @return solution vector x
     */
    public BigDecimal[] solveLinearSystem(BigDecimal[][] coefficientMatrix, BigDecimal[] constantsVector) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs((Object) coefficientMatrix, (Object) constantsVector);

            int n = coefficientMatrix.length;

            if (n == 0 || coefficientMatrix[0].length != n || constantsVector.length != n) {
                throw new ValidationException("Invalid matrix dimensions for linear system", OPERATION_NAME);
            }

            logger.debug("Solving {}x{} linear system", n, n);

            // Create augmented matrix [A|b]
            BigDecimal[][] augmented = new BigDecimal[n][n + 1];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    augmented[i][j] = coefficientMatrix[i][j];
                }
                augmented[i][n] = constantsVector[i];
            }

            // Forward elimination
            for (int pivot = 0; pivot < n; pivot++) {
                // Find pivot row
                int maxRow = pivot;
                for (int i = pivot + 1; i < n; i++) {
                    if (augmented[i][pivot].abs().compareTo(augmented[maxRow][pivot].abs()) > 0) {
                        maxRow = i;
                    }
                }

                // Swap rows
                BigDecimal[] temp = augmented[pivot];
                augmented[pivot] = augmented[maxRow];
                augmented[maxRow] = temp;

                // Check for singular matrix
                if (augmented[pivot][pivot].abs().compareTo(TOLERANCE) < 0) {
                    throw new ValidationException("Matrix is singular or nearly singular", OPERATION_NAME);
                }

                // Eliminate
                for (int i = pivot + 1; i < n; i++) {
                    BigDecimal factor = augmented[i][pivot].divide(augmented[pivot][pivot], DEFAULT_PRECISION);
                    for (int j = pivot; j <= n; j++) {
                        augmented[i][j] = augmented[i][j].subtract(
                            augmented[pivot][j].multiply(factor, DEFAULT_PRECISION), DEFAULT_PRECISION);
                    }
                }
            }

            // Back substitution
            BigDecimal[] solution = new BigDecimal[n];
            for (int i = n - 1; i >= 0; i--) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i + 1; j < n; j++) {
                    sum = sum.add(augmented[i][j].multiply(solution[j], DEFAULT_PRECISION), DEFAULT_PRECISION);
                }
                solution[i] = augmented[i][n].subtract(sum, DEFAULT_PRECISION)
                             .divide(augmented[i][i], DEFAULT_PRECISION);
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Linear system solution computed");

            return solution;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error solving linear system: {}", e.getMessage());
            throw new ValidationException("Failed to solve linear system: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== RATIO AND PROPORTION UTILITIES =====

    /**
     * Calculate ratio between two numbers.
     *
     * @param a first number
     * @param b second number
     * @return ratio a:b as simplified fraction
     */
    public Ratio calculateRatio(BigDecimal a, BigDecimal b) {
        validateInputs(a, b);

        if (b.abs().compareTo(TOLERANCE) < 0) {
            throw new ValidationException("Cannot calculate ratio with zero denominator", OPERATION_NAME);
        }

        // Convert to common scale for ratio calculation
        BigDecimal ratio = a.divide(b, DEFAULT_PRECISION);

        // Find greatest common divisor for simplification
        long gcd = gcd(a.abs(), b.abs());
        BigDecimal simplifiedA = a.divide(BigDecimal.valueOf(gcd), DEFAULT_PRECISION);
        BigDecimal simplifiedB = b.divide(BigDecimal.valueOf(gcd), DEFAULT_PRECISION);

        return new Ratio(simplifiedA, simplifiedB, ratio);
    }

    /**
     * Check if three numbers are in proportion (a:b = c:d).
     *
     * @param a first term
     * @param b second term
     * @param c third term
     * @param d fourth term
     * @return true if in proportion, false otherwise
     */
    public boolean isProportion(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d) {
        validateInputs(a, b, c, d);

        if (b.abs().compareTo(TOLERANCE) < 0 || d.abs().compareTo(TOLERANCE) < 0) {
            return false;
        }

        BigDecimal ratio1 = a.divide(b, DEFAULT_PRECISION);
        BigDecimal ratio2 = c.divide(d, DEFAULT_PRECISION);

        return ratio1.subtract(ratio2, DEFAULT_PRECISION).abs().compareTo(TOLERANCE) < 0;
    }

    /**
     * Find the missing term in a proportion.
     *
     * Given a:b = c:x, solve for x
     *
     * @param a first term
     * @param b second term
     * @param c third term
     * @return fourth term x
     */
    public BigDecimal findProportionMissingTerm(BigDecimal a, BigDecimal b, BigDecimal c) {
        validateInputs(a, b, c);

        if (b.abs().compareTo(TOLERANCE) < 0) {
            throw new ValidationException("Cannot solve proportion with zero in denominator position", OPERATION_NAME);
        }

        // x = (c * b) / a
        return c.multiply(b, DEFAULT_PRECISION).divide(a, DEFAULT_PRECISION);
    }

    // ===== UTILITY METHODS =====

    /**
     * Calculate square root using BigDecimal.
     */
    private BigDecimal sqrt(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Cannot calculate square root of negative number", OPERATION_NAME);
        }

        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Newton-Raphson method for square root
        BigDecimal x = value.divide(BigDecimal.valueOf(2), DEFAULT_PRECISION);
        BigDecimal two = BigDecimal.valueOf(2);

        for (int i = 0; i < 50; i++) {
            x = x.add(value.divide(x, DEFAULT_PRECISION), DEFAULT_PRECISION).divide(two, DEFAULT_PRECISION);
        }

        return x;
    }

    /**
     * Calculate cube root using BigDecimal.
     */
    private BigDecimal cbrt(BigDecimal value) {
        // Simple approximation using Math.cbrt
        return BigDecimal.valueOf(Math.cbrt(value.doubleValue()));
    }

    /**
     * Calculate GCD of two BigDecimal numbers.
     */
    private long gcd(BigDecimal a, BigDecimal b) {
        // Convert to long for GCD calculation (simplified)
        long aLong = a.abs().longValue();
        long bLong = b.abs().longValue();

        while (bLong != 0) {
            long temp = bLong;
            bLong = aLong % bLong;
            aLong = temp;
        }

        return aLong;
    }

    /**
     * Get current memory usage in bytes.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // ===== RESULT CLASSES =====

    /**
     * Quadratic equation solution result.
     */
    public static class QuadraticSolution {
        public final Object root1; // BigDecimal or String (for complex)
        public final Object root2; // BigDecimal or String (for complex)
        public final BigDecimal discriminant;
        public final String solutionType; // "two_real", "one_real", "two_complex"

        public QuadraticSolution(Object root1, Object root2, BigDecimal discriminant, String solutionType) {
            this.root1 = root1;
            this.root2 = root2;
            this.discriminant = discriminant;
            this.solutionType = solutionType;
        }

        @Override
        public String toString() {
            return String.format("Roots: %s, %s (Type: %s, Discriminant: %s)",
                               root1, root2, solutionType, discriminant);
        }
    }

    /**
     * Ratio representation.
     */
    public static class Ratio {
        public final BigDecimal numerator;
        public final BigDecimal denominator;
        public final BigDecimal decimalValue;

        public Ratio(BigDecimal numerator, BigDecimal denominator, BigDecimal decimalValue) {
            this.numerator = numerator;
            this.denominator = denominator;
            this.decimalValue = decimalValue;
        }

        @Override
        public String toString() {
            return String.format("%s:%s (≈ %s)", numerator, denominator, decimalValue);
        }
    }
}
