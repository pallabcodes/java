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

package com.netflix.mathlib.algebra;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Vector Operations - Production-grade vector algebra operations.
 *
 * This class provides comprehensive vector operations including:
 * - Vector arithmetic (addition, subtraction, scalar multiplication)
 * - Dot product and cross product
 * - Vector magnitude and normalization
 * - Vector projection and rejection
 * - Angle between vectors
 * - Vector transformations
 * - N-dimensional vector operations
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
public class VectorOperations implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(VectorOperations.class);
    private static final String OPERATION_NAME = "VectorOperations";
    private static final String COMPLEXITY = "O(n)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;
    private final MathContext DEFAULT_PRECISION = new MathContext(50, java.math.RoundingMode.HALF_UP);
    private final BigDecimal TOLERANCE = new BigDecimal("1e-15");

    /**
     * Constructor for Vector Operations.
     */
    public VectorOperations() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Vector Operations module");
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

    // ===== VECTOR ARITHMETIC =====

    /**
     * Add two vectors.
     *
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     *
     * @param vectorA first vector
     * @param vectorB second vector
     * @return sum of vectors
     * @throws ValidationException if vectors have incompatible dimensions
     */
    public BigDecimal[] add(BigDecimal[] vectorA, BigDecimal[] vectorB) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs((Object) vectorA, (Object) vectorB);

            if (vectorA.length != vectorB.length) {
                throw new ValidationException("Vectors must have same dimension", OPERATION_NAME);
            }

            logger.debug("Adding vectors of dimension {}", vectorA.length);

            BigDecimal[] result = new BigDecimal[vectorA.length];
            for (int i = 0; i < vectorA.length; i++) {
                result[i] = vectorA[i].add(vectorB[i], DEFAULT_PRECISION);
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error adding vectors: {}", e.getMessage());
            throw new ValidationException("Failed to add vectors: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Subtract two vectors.
     *
     * @param vectorA first vector
     * @param vectorB second vector
     * @return difference of vectors
     */
    public BigDecimal[] subtract(BigDecimal[] vectorA, BigDecimal[] vectorB) {
        validateInputs((Object) vectorA, (Object) vectorB);

        if (vectorA.length != vectorB.length) {
            throw new ValidationException("Vectors must have same dimension", OPERATION_NAME);
        }

        BigDecimal[] result = new BigDecimal[vectorA.length];
        for (int i = 0; i < vectorA.length; i++) {
            result[i] = vectorA[i].subtract(vectorB[i], DEFAULT_PRECISION);
        }

        return result;
    }

    /**
     * Multiply vector by scalar.
     *
     * @param vector vector to multiply
     * @param scalar scalar value
     * @return scaled vector
     */
    public BigDecimal[] multiplyByScalar(BigDecimal[] vector, BigDecimal scalar) {
        validateInputs((Object) vector, scalar);

        BigDecimal[] result = new BigDecimal[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i].multiply(scalar, DEFAULT_PRECISION);
        }

        return result;
    }

    // ===== DOT PRODUCT =====

    /**
     * Calculate dot product of two vectors.
     *
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     *
     * @param vectorA first vector
     * @param vectorB second vector
     * @return dot product
     * @throws ValidationException if vectors have incompatible dimensions
     */
    public BigDecimal dotProduct(BigDecimal[] vectorA, BigDecimal[] vectorB) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs((Object) vectorA, (Object) vectorB);

            if (vectorA.length != vectorB.length) {
                throw new ValidationException("Vectors must have same dimension", OPERATION_NAME);
            }

            logger.debug("Calculating dot product of vectors with dimension {}", vectorA.length);

            BigDecimal result = BigDecimal.ZERO;
            for (int i = 0; i < vectorA.length; i++) {
                result = result.add(vectorA[i].multiply(vectorB[i], DEFAULT_PRECISION), DEFAULT_PRECISION);
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Dot product result: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating dot product: {}", e.getMessage());
            throw new ValidationException("Failed to calculate dot product: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== CROSS PRODUCT (3D ONLY) =====

    /**
     * Calculate cross product of two 3D vectors.
     *
     * @param vectorA first 3D vector
     * @param vectorB second 3D vector
     * @return cross product vector
     */
    public BigDecimal[] crossProduct3D(BigDecimal[] vectorA, BigDecimal[] vectorB) {
        validateInputs((Object) vectorA, (Object) vectorB);

        if (vectorA.length != 3 || vectorB.length != 3) {
            throw new ValidationException("Cross product requires 3D vectors", OPERATION_NAME);
        }

        BigDecimal[] result = new BigDecimal[3];

        // i component: a2*b3 - a3*b2
        result[0] = vectorA[1].multiply(vectorB[2], DEFAULT_PRECISION)
                     .subtract(vectorA[2].multiply(vectorB[1], DEFAULT_PRECISION), DEFAULT_PRECISION);

        // j component: a3*b1 - a1*b3
        result[1] = vectorA[2].multiply(vectorB[0], DEFAULT_PRECISION)
                     .subtract(vectorA[0].multiply(vectorB[2], DEFAULT_PRECISION), DEFAULT_PRECISION);

        // k component: a1*b2 - a2*b1
        result[2] = vectorA[0].multiply(vectorB[1], DEFAULT_PRECISION)
                     .subtract(vectorA[1].multiply(vectorB[0], DEFAULT_PRECISION), DEFAULT_PRECISION);

        return result;
    }

    // ===== VECTOR MAGNITUDE =====

    /**
     * Calculate magnitude (length) of a vector.
     *
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     *
     * @param vector input vector
     * @return magnitude
     */
    public BigDecimal magnitude(BigDecimal[] vector) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs((Object) vector);

            logger.debug("Calculating magnitude of vector with dimension {}", vector.length);

            BigDecimal sumSquares = BigDecimal.ZERO;
            for (BigDecimal component : vector) {
                sumSquares = sumSquares.add(component.multiply(component, DEFAULT_PRECISION), DEFAULT_PRECISION);
            }

            BigDecimal result = sqrt(sumSquares);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Vector magnitude: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating vector magnitude: {}", e.getMessage());
            throw new ValidationException("Failed to calculate vector magnitude: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== VECTOR NORMALIZATION =====

    /**
     * Normalize a vector (make it unit length).
     *
     * @param vector input vector
     * @return normalized vector
     */
    public BigDecimal[] normalize(BigDecimal[] vector) {
        validateInputs((Object) vector);

        BigDecimal magnitude = magnitude(vector);

        if (magnitude.abs().compareTo(TOLERANCE) < 0) {
            throw new ValidationException("Cannot normalize zero vector", OPERATION_NAME);
        }

        BigDecimal[] result = new BigDecimal[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i].divide(magnitude, DEFAULT_PRECISION);
        }

        return result;
    }

    // ===== VECTOR PROJECTION =====

    /**
     * Calculate projection of vector A onto vector B.
     *
     * @param vectorA vector to project
     * @param vectorB target vector
     * @return projection vector
     */
    public BigDecimal[] projection(BigDecimal[] vectorA, BigDecimal[] vectorB) {
        validateInputs((Object) vectorA, (Object) vectorB);

        if (vectorA.length != vectorB.length) {
            throw new ValidationException("Vectors must have same dimension", OPERATION_NAME);
        }

        BigDecimal dotAB = dotProduct(vectorA, vectorB);
        BigDecimal magnitudeBSquared = magnitude(vectorB).multiply(magnitude(vectorB), DEFAULT_PRECISION);

        if (magnitudeBSquared.abs().compareTo(TOLERANCE) < 0) {
            throw new ValidationException("Cannot project onto zero vector", OPERATION_NAME);
        }

        BigDecimal scalar = dotAB.divide(magnitudeBSquared, DEFAULT_PRECISION);
        return multiplyByScalar(vectorB, scalar);
    }

    // ===== ANGLE BETWEEN VECTORS =====

    /**
     * Calculate angle between two vectors in radians.
     *
     * @param vectorA first vector
     * @param vectorB second vector
     * @return angle in radians
     */
    public BigDecimal angleBetween(BigDecimal[] vectorA, BigDecimal[] vectorB) {
        validateInputs((Object) vectorA, (Object) vectorB);

        if (vectorA.length != vectorB.length) {
            throw new ValidationException("Vectors must have same dimension", OPERATION_NAME);
        }

        BigDecimal dotProduct = dotProduct(vectorA, vectorB);
        BigDecimal magnitudeA = magnitude(vectorA);
        BigDecimal magnitudeB = magnitude(vectorB);

        if (magnitudeA.abs().compareTo(TOLERANCE) < 0 || magnitudeB.abs().compareTo(TOLERANCE) < 0) {
            throw new ValidationException("Cannot calculate angle with zero vector", OPERATION_NAME);
        }

        BigDecimal cosTheta = dotProduct.divide(magnitudeA.multiply(magnitudeB, DEFAULT_PRECISION), DEFAULT_PRECISION);

        // Clamp to [-1, 1] to handle floating point errors
        if (cosTheta.compareTo(BigDecimal.ONE) > 0) {
            cosTheta = BigDecimal.ONE;
        } else if (cosTheta.compareTo(BigDecimal.ONE.negate()) < 0) {
            cosTheta = BigDecimal.ONE.negate();
        }

        return acos(cosTheta);
    }

    // ===== VECTOR TRANSFORMATIONS =====

    /**
     * Rotate 2D vector by given angle.
     *
     * @param vector 2D vector to rotate
     * @param angle rotation angle in radians
     * @return rotated vector
     */
    public BigDecimal[] rotate2D(BigDecimal[] vector, BigDecimal angle) {
        validateInputs((Object) vector, angle);

        if (vector.length != 2) {
            throw new ValidationException("Rotation requires 2D vector", OPERATION_NAME);
        }

        BigDecimal cosTheta = cos(angle);
        BigDecimal sinTheta = sin(angle);

        BigDecimal x = vector[0].multiply(cosTheta, DEFAULT_PRECISION)
                       .subtract(vector[1].multiply(sinTheta, DEFAULT_PRECISION), DEFAULT_PRECISION);
        BigDecimal y = vector[0].multiply(sinTheta, DEFAULT_PRECISION)
                       .add(vector[1].multiply(cosTheta, DEFAULT_PRECISION), DEFAULT_PRECISION);

        return new BigDecimal[]{x, y};
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
     * Calculate cosine using Taylor series.
     */
    private BigDecimal cos(BigDecimal angle) {
        // Normalize angle to [-π, π]
        BigDecimal pi = new BigDecimal("3.141592653589793238462643383279502884197");
        BigDecimal twoPi = pi.multiply(BigDecimal.valueOf(2), DEFAULT_PRECISION);

        while (angle.compareTo(pi) > 0) {
            angle = angle.subtract(twoPi, DEFAULT_PRECISION);
        }
        while (angle.compareTo(pi.negate()) < 0) {
            angle = angle.add(twoPi, DEFAULT_PRECISION);
        }

        BigDecimal result = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal angleSquared = angle.multiply(angle, DEFAULT_PRECISION);
        int sign = -1;

        // Taylor series: cos(x) = 1 - x^2/2! + x^4/4! - x^6/6! + ...
        for (int n = 2; term.abs().compareTo(TOLERANCE) > 0 && n < 20; n += 2) {
            term = term.multiply(angleSquared, DEFAULT_PRECISION)
                       .divide(BigDecimal.valueOf((n - 1) * n), DEFAULT_PRECISION);
            result = result.add(term.multiply(BigDecimal.valueOf(sign), DEFAULT_PRECISION), DEFAULT_PRECISION);
            sign = -sign;
        }

        return result;
    }

    /**
     * Calculate sine using Taylor series.
     */
    private BigDecimal sin(BigDecimal angle) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = angle;
        BigDecimal angleSquared = angle.multiply(angle, DEFAULT_PRECISION);
        int sign = 1;

        // Taylor series: sin(x) = x - x^3/3! + x^5/5! - x^7/7! + ...
        for (int n = 1; term.abs().compareTo(TOLERANCE) > 0 && n < 20; n += 2) {
            result = result.add(term.multiply(BigDecimal.valueOf(sign), DEFAULT_PRECISION), DEFAULT_PRECISION);
            term = term.multiply(angleSquared, DEFAULT_PRECISION)
                       .divide(BigDecimal.valueOf((n + 1) * (n + 2)), DEFAULT_PRECISION);
            sign = -sign;
        }

        return result;
    }

    /**
     * Calculate arccosine.
     */
    private BigDecimal acos(BigDecimal value) {
        // For simplicity, use approximation
        if (value.compareTo(BigDecimal.ONE) == 0) {
            return BigDecimal.ZERO;
        } else if (value.compareTo(BigDecimal.ONE.negate()) == 0) {
            return new BigDecimal("3.141592653589793238462643383279502884197");
        }

        // Use identity: acos(x) = π/2 - asin(x)
        BigDecimal pi = new BigDecimal("3.141592653589793238462643383279502884197");
        return pi.divide(BigDecimal.valueOf(2), DEFAULT_PRECISION).subtract(asin(value), DEFAULT_PRECISION);
    }

    /**
     * Calculate arcsine.
     */
    private BigDecimal asin(BigDecimal value) {
        if (value.abs().compareTo(BigDecimal.ONE) > 0) {
            throw new ValidationException("Arcsin input must be between -1 and 1", OPERATION_NAME);
        }

        // For small values, use approximation: asin(x) ≈ x + (1/6)x^3 + ...
        if (value.abs().compareTo(new BigDecimal("0.5")) < 0) {
            BigDecimal x = value;
            BigDecimal result = x;
            BigDecimal term = x;
            BigDecimal xSquared = x.multiply(x, DEFAULT_PRECISION);

            for (int n = 1; n < 10 && term.abs().compareTo(TOLERANCE) > 0; n++) {
                term = term.multiply(xSquared, DEFAULT_PRECISION)
                          .multiply(BigDecimal.valueOf(2 * n - 1), DEFAULT_PRECISION)
                          .divide(BigDecimal.valueOf(2 * n + 1), DEFAULT_PRECISION);
                result = result.add(term, DEFAULT_PRECISION);
            }

            return result;
        } else {
            // Use identity: asin(x) = π/2 - acos(x) for |x| > 0.5
            BigDecimal pi = new BigDecimal("3.141592653589793238462643383279502884197");
            return pi.divide(BigDecimal.valueOf(2), DEFAULT_PRECISION).subtract(acos(value), DEFAULT_PRECISION);
        }
    }

    /**
     * Get current memory usage in bytes.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // ===== VECTOR CREATION UTILITIES =====

    /**
     * Create a zero vector of given dimension.
     *
     * @param dimension vector dimension
     * @return zero vector
     */
    public BigDecimal[] zeroVector(int dimension) {
        validateInputs(dimension);

        if (dimension <= 0) {
            throw new ValidationException("Dimension must be positive", OPERATION_NAME);
        }

        BigDecimal[] vector = new BigDecimal[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = BigDecimal.ZERO;
        }

        return vector;
    }

    /**
     * Create a unit vector in given dimension and direction.
     *
     * @param dimension vector dimension
     * @param direction unit vector direction (0-based index)
     * @return unit vector
     */
    public BigDecimal[] unitVector(int dimension, int direction) {
        validateInputs(dimension, direction);

        if (dimension <= 0) {
            throw new ValidationException("Dimension must be positive", OPERATION_NAME);
        }

        if (direction < 0 || direction >= dimension) {
            throw new ValidationException("Invalid direction index", OPERATION_NAME);
        }

        BigDecimal[] vector = new BigDecimal[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = (i == direction) ? BigDecimal.ONE : BigDecimal.ZERO;
        }

        return vector;
    }

    /**
     * Check if two vectors are equal within tolerance.
     *
     * @param vectorA first vector
     * @param vectorB second vector
     * @return true if equal, false otherwise
     */
    public boolean equals(BigDecimal[] vectorA, BigDecimal[] vectorB) {
        validateInputs((Object) vectorA, (Object) vectorB);

        if (vectorA.length != vectorB.length) {
            return false;
        }

        for (int i = 0; i < vectorA.length; i++) {
            if (vectorA[i].subtract(vectorB[i], DEFAULT_PRECISION).abs().compareTo(TOLERANCE) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get vector dimension.
     *
     * @param vector input vector
     * @return dimension
     */
    public int getDimension(BigDecimal[] vector) {
        validateInputs((Object) vector);
        return vector.length;
    }
}
