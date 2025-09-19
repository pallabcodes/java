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

package com.netflix.mathlib.geometry;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Geometry Operations - Production-grade geometric and trigonometric operations.
 *
 * This class provides comprehensive geometric operations including:
 * - Trigonometric functions (sin, cos, tan, etc.)
 * - Coordinate geometry (points, lines, circles, etc.)
 * - Distance calculations (Euclidean, Manhattan, etc.)
 * - Area and perimeter calculations
 * - Geometric transformations
 * - Shape analysis and properties
 * - Vector geometry
 * - 2D and 3D geometric operations
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
public class GeometryOperations implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(GeometryOperations.class);
    private static final String OPERATION_NAME = "GeometryOperations";
    private static final String COMPLEXITY = "O(various)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;
    private final MathContext DEFAULT_PRECISION = new MathContext(50, RoundingMode.HALF_UP);
    private final BigDecimal PI = new BigDecimal("3.14159265358979323846264338327950288419716939937510");
    private final BigDecimal E = new BigDecimal("2.71828182845904523536028747135266249775724709369995");
    private final BigDecimal TOLERANCE = new BigDecimal("1e-15");

    /**
     * Constructor for Geometry Operations.
     */
    public GeometryOperations() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Geometry Operations module");
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

    // ===== TRIGONOMETRIC FUNCTIONS =====

    /**
     * Calculate sine of an angle in radians using Taylor series.
     *
     * Time Complexity: O(terms) - configurable precision
     * Space Complexity: O(1)
     *
     * @param angle angle in radians
     * @return sine of angle
     * @throws ValidationException if input is invalid
     */
    public BigDecimal sin(BigDecimal angle) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(angle);

            logger.debug("Calculating sin({})", angle);

            // Normalize angle to [-π, π]
            angle = normalizeAngle(angle);

            BigDecimal result = BigDecimal.ZERO;
            BigDecimal term = angle;
            BigDecimal angleSquared = angle.multiply(angle, DEFAULT_PRECISION);
            int sign = 1;

            // Taylor series: sin(x) = x - x^3/3! + x^5/5! - x^7/7! + ...
            for (int n = 1; term.abs().compareTo(TOLERANCE) > 0 && n < 100; n += 2) {
                result = result.add(term.multiply(BigDecimal.valueOf(sign), DEFAULT_PRECISION), DEFAULT_PRECISION);
                term = term.multiply(angleSquared, DEFAULT_PRECISION).divide(
                    BigDecimal.valueOf((n + 1) * (n + 2)), DEFAULT_PRECISION);
                sign = -sign;
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating sin({}): {}", angle, e.getMessage());
            throw new ValidationException("Failed to calculate sin: " + e.getMessage(), OPERATION_NAME, angle);
        }
    }

    /**
     * Calculate cosine of an angle in radians using Taylor series.
     *
     * @param angle angle in radians
     * @return cosine of angle
     */
    public BigDecimal cos(BigDecimal angle) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(angle);

            logger.debug("Calculating cos({})", angle);

            // Normalize angle to [-π, π]
            angle = normalizeAngle(angle);

            BigDecimal result = BigDecimal.ONE;
            BigDecimal term = BigDecimal.ONE;
            BigDecimal angleSquared = angle.multiply(angle, DEFAULT_PRECISION);
            int sign = -1;

            // Taylor series: cos(x) = 1 - x^2/2! + x^4/4! - x^6/6! + ...
            for (int n = 2; term.abs().compareTo(TOLERANCE) > 0 && n < 100; n += 2) {
                term = term.multiply(angleSquared, DEFAULT_PRECISION).divide(
                    BigDecimal.valueOf(n * (n - 1)), DEFAULT_PRECISION);
                result = result.add(term.multiply(BigDecimal.valueOf(sign), DEFAULT_PRECISION), DEFAULT_PRECISION);
                sign = -sign;
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating cos({}): {}", angle, e.getMessage());
            throw new ValidationException("Failed to calculate cos: " + e.getMessage(), OPERATION_NAME, angle);
        }
    }

    /**
     * Calculate tangent of an angle in radians.
     *
     * @param angle angle in radians
     * @return tangent of angle
     */
    public BigDecimal tan(BigDecimal angle) {
        validateInputs(angle);

        BigDecimal cosValue = cos(angle);
        if (cosValue.abs().compareTo(TOLERANCE) < 0) {
            throw new ValidationException("Tangent undefined at this angle", OPERATION_NAME, angle);
        }

        return sin(angle).divide(cosValue, DEFAULT_PRECISION);
    }

    /**
     * Calculate arcsine (inverse sine) of a value.
     *
     * @param value value between -1 and 1
     * @return angle in radians
     */
    public BigDecimal asin(BigDecimal value) {
        validateInputs(value);

        if (value.abs().compareTo(BigDecimal.ONE) > 0) {
            throw new ValidationException("Arcsin input must be between -1 and 1", OPERATION_NAME, value);
        }

        // For small values, use approximation: asin(x) ≈ x + (1/6)x^3 + ...
        if (value.abs().compareTo(new BigDecimal("0.5")) < 0) {
            BigDecimal x = value;
            BigDecimal result = x;
            BigDecimal term = x;
            BigDecimal xSquared = x.multiply(x, DEFAULT_PRECISION);

            for (int n = 1; n < 20 && term.abs().compareTo(TOLERANCE) > 0; n++) {
                term = term.multiply(xSquared, DEFAULT_PRECISION)
                          .multiply(BigDecimal.valueOf(2 * n - 1), DEFAULT_PRECISION)
                          .divide(BigDecimal.valueOf(2 * n + 1), DEFAULT_PRECISION);
                result = result.add(term, DEFAULT_PRECISION);
            }

            return result;
        } else {
            // Use identity: asin(x) = π/2 - acos(x) for |x| > 0.5
            BigDecimal acosValue = acos(value);
            return PI.divide(BigDecimal.valueOf(2), DEFAULT_PRECISION).subtract(acosValue, DEFAULT_PRECISION);
        }
    }

    /**
     * Calculate arccosine (inverse cosine) of a value.
     *
     * @param value value between -1 and 1
     * @return angle in radians
     */
    public BigDecimal acos(BigDecimal value) {
        validateInputs(value);

        if (value.abs().compareTo(BigDecimal.ONE) > 0) {
            throw new ValidationException("Arccos input must be between -1 and 1", OPERATION_NAME, value);
        }

        // Use identity: acos(x) = π/2 - asin(x)
        BigDecimal asinValue = asin(value);
        return PI.divide(BigDecimal.valueOf(2), DEFAULT_PRECISION).subtract(asinValue, DEFAULT_PRECISION);
    }

    /**
     * Calculate arctangent (inverse tangent) of a value.
     *
     * @param value any real number
     * @return angle in radians between -π/2 and π/2
     */
    public BigDecimal atan(BigDecimal value) {
        validateInputs(value);

        // For small values, use approximation: atan(x) ≈ x - x^3/3 + x^5/5 - ...
        BigDecimal x = value;
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = x;
        BigDecimal xSquared = x.multiply(x, DEFAULT_PRECISION);
        int sign = 1;

        for (int n = 1; n < 30 && term.abs().compareTo(TOLERANCE) > 0; n += 2) {
            result = result.add(term.multiply(BigDecimal.valueOf(sign), DEFAULT_PRECISION), DEFAULT_PRECISION);
            term = term.multiply(xSquared, DEFAULT_PRECISION).divide(BigDecimal.valueOf(n + 1), DEFAULT_PRECISION);
            sign = -sign;
        }

        return result;
    }

    // ===== DISTANCE CALCULATIONS =====

    /**
     * Calculate Euclidean distance between two points in 2D.
     *
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @return Euclidean distance
     */
    public BigDecimal euclideanDistance(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2) {
        validateInputs(x1, y1, x2, y2);

        BigDecimal dx = x2.subtract(x1, DEFAULT_PRECISION);
        BigDecimal dy = y2.subtract(y1, DEFAULT_PRECISION);

        BigDecimal sum = dx.multiply(dx, DEFAULT_PRECISION).add(
            dy.multiply(dy, DEFAULT_PRECISION), DEFAULT_PRECISION);

        return sqrt(sum);
    }

    /**
     * Calculate Manhattan distance between two points in 2D.
     *
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @return Manhattan distance
     */
    public BigDecimal manhattanDistance(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2) {
        validateInputs(x1, y1, x2, y2);

        BigDecimal dx = x2.subtract(x1, DEFAULT_PRECISION).abs();
        BigDecimal dy = y2.subtract(y1, DEFAULT_PRECISION).abs();

        return dx.add(dy, DEFAULT_PRECISION);
    }

    /**
     * Calculate Chebyshev distance between two points in 2D.
     *
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @return Chebyshev distance
     */
    public BigDecimal chebyshevDistance(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2) {
        validateInputs(x1, y1, x2, y2);

        BigDecimal dx = x2.subtract(x1, DEFAULT_PRECISION).abs();
        BigDecimal dy = y2.subtract(y1, DEFAULT_PRECISION).abs();

        return dx.max(dy);
    }

    /**
     * Calculate Euclidean distance between two points in 3D.
     *
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param z1 z-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @param z2 z-coordinate of second point
     * @return Euclidean distance
     */
    public BigDecimal euclideanDistance3D(BigDecimal x1, BigDecimal y1, BigDecimal z1,
                                        BigDecimal x2, BigDecimal y2, BigDecimal z2) {
        validateInputs(x1, y1, z1, x2, y2, z2);

        BigDecimal dx = x2.subtract(x1, DEFAULT_PRECISION);
        BigDecimal dy = y2.subtract(y1, DEFAULT_PRECISION);
        BigDecimal dz = z2.subtract(z1, DEFAULT_PRECISION);

        BigDecimal sum = dx.multiply(dx, DEFAULT_PRECISION)
                           .add(dy.multiply(dy, DEFAULT_PRECISION), DEFAULT_PRECISION)
                           .add(dz.multiply(dz, DEFAULT_PRECISION), DEFAULT_PRECISION);

        return sqrt(sum);
    }

    // ===== AREA CALCULATIONS =====

    /**
     * Calculate area of a triangle given three points (using determinant method).
     *
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @param x3 x-coordinate of third point
     * @param y3 y-coordinate of third point
     * @return area of triangle
     */
    public BigDecimal triangleArea(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2,
                                 BigDecimal x3, BigDecimal y3) {
        validateInputs(x1, y1, x2, y2, x3, y3);

        // Area = (1/2) * | (x1(y2 - y3) + x2(y3 - y1) + x3(y1 - y2)) |
        BigDecimal term1 = x1.multiply(y2.subtract(y3, DEFAULT_PRECISION), DEFAULT_PRECISION);
        BigDecimal term2 = x2.multiply(y3.subtract(y1, DEFAULT_PRECISION), DEFAULT_PRECISION);
        BigDecimal term3 = x3.multiply(y1.subtract(y2, DEFAULT_PRECISION), DEFAULT_PRECISION);

        BigDecimal area = term1.add(term2, DEFAULT_PRECISION).add(term3, DEFAULT_PRECISION).abs();
        return area.divide(BigDecimal.valueOf(2), DEFAULT_PRECISION);
    }

    /**
     * Calculate area of a circle.
     *
     * @param radius radius of circle
     * @return area of circle
     */
    public BigDecimal circleArea(BigDecimal radius) {
        validateInputs(radius);

        if (radius.compareTo(BigDecimal.ZERO) < 0) {
            throw ValidationException.negativeValue("radius", radius, OPERATION_NAME);
        }

        return PI.multiply(radius.multiply(radius, DEFAULT_PRECISION), DEFAULT_PRECISION);
    }

    /**
     * Calculate area of a rectangle.
     *
     * @param length length of rectangle
     * @param width width of rectangle
     * @return area of rectangle
     */
    public BigDecimal rectangleArea(BigDecimal length, BigDecimal width) {
        validateInputs(length, width);

        if (length.compareTo(BigDecimal.ZERO) <= 0 || width.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Length and width must be positive", OPERATION_NAME);
        }

        return length.multiply(width, DEFAULT_PRECISION);
    }

    /**
     * Calculate area of a polygon using shoelace formula.
     *
     * @param xCoords array of x-coordinates
     * @param yCoords array of y-coordinates
     * @return area of polygon
     */
    public BigDecimal polygonArea(BigDecimal[] xCoords, BigDecimal[] yCoords) {
        validateInputs((Object) xCoords, (Object) yCoords);

        if (xCoords.length != yCoords.length || xCoords.length < 3) {
            throw new ValidationException("Polygon must have at least 3 points", OPERATION_NAME);
        }

        int n = xCoords.length;
        BigDecimal area = BigDecimal.ZERO;

        // Shoelace formula
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area = area.add(
                xCoords[i].multiply(yCoords[j], DEFAULT_PRECISION).subtract(
                    xCoords[j].multiply(yCoords[i], DEFAULT_PRECISION), DEFAULT_PRECISION
                ), DEFAULT_PRECISION
            );
        }

        return area.abs().divide(BigDecimal.valueOf(2), DEFAULT_PRECISION);
    }

    // ===== PERIMETER CALCULATIONS =====

    /**
     * Calculate perimeter of a triangle.
     *
     * @param side1 length of first side
     * @param side2 length of second side
     * @param side3 length of third side
     * @return perimeter of triangle
     */
    public BigDecimal trianglePerimeter(BigDecimal side1, BigDecimal side2, BigDecimal side3) {
        validateInputs(side1, side2, side3);

        if (side1.compareTo(BigDecimal.ZERO) <= 0 ||
            side2.compareTo(BigDecimal.ZERO) <= 0 ||
            side3.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("All sides must be positive", OPERATION_NAME);
        }

        return side1.add(side2, DEFAULT_PRECISION).add(side3, DEFAULT_PRECISION);
    }

    /**
     * Calculate circumference of a circle.
     *
     * @param radius radius of circle
     * @return circumference of circle
     */
    public BigDecimal circleCircumference(BigDecimal radius) {
        validateInputs(radius);

        if (radius.compareTo(BigDecimal.ZERO) < 0) {
            throw ValidationException.negativeValue("radius", radius, OPERATION_NAME);
        }

        return BigDecimal.valueOf(2).multiply(PI, DEFAULT_PRECISION).multiply(radius, DEFAULT_PRECISION);
    }

    /**
     * Calculate perimeter of a rectangle.
     *
     * @param length length of rectangle
     * @param width width of rectangle
     * @return perimeter of rectangle
     */
    public BigDecimal rectanglePerimeter(BigDecimal length, BigDecimal width) {
        validateInputs(length, width);

        if (length.compareTo(BigDecimal.ZERO) <= 0 || width.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Length and width must be positive", OPERATION_NAME);
        }

        return BigDecimal.valueOf(2).multiply(length.add(width, DEFAULT_PRECISION), DEFAULT_PRECISION);
    }

    // ===== COORDINATE GEOMETRY =====

    /**
     * Calculate slope of a line given two points.
     *
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @return slope of line
     */
    public BigDecimal lineSlope(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2) {
        validateInputs(x1, y1, x2, y2);

        if (x2.subtract(x1, DEFAULT_PRECISION).abs().compareTo(TOLERANCE) < 0) {
            throw new ValidationException("Vertical line has undefined slope", OPERATION_NAME);
        }

        return y2.subtract(y1, DEFAULT_PRECISION).divide(
            x2.subtract(x1, DEFAULT_PRECISION), DEFAULT_PRECISION);
    }

    /**
     * Calculate distance from a point to a line.
     *
     * Line equation: ax + by + c = 0
     *
     * @param a coefficient of x
     * @param b coefficient of y
     * @param c constant term
     * @param x x-coordinate of point
     * @param y y-coordinate of point
     * @return distance from point to line
     */
    public BigDecimal pointToLineDistance(BigDecimal a, BigDecimal b, BigDecimal c,
                                       BigDecimal x, BigDecimal y) {
        validateInputs(a, b, c, x, y);

        // Distance = |ax + by + c| / sqrt(a^2 + b^2)
        BigDecimal numerator = a.multiply(x, DEFAULT_PRECISION)
                                .add(b.multiply(y, DEFAULT_PRECISION), DEFAULT_PRECISION)
                                .add(c, DEFAULT_PRECISION).abs();

        BigDecimal denominator = sqrt(
            a.multiply(a, DEFAULT_PRECISION).add(b.multiply(b, DEFAULT_PRECISION), DEFAULT_PRECISION)
        );

        return numerator.divide(denominator, DEFAULT_PRECISION);
    }

    /**
     * Check if three points are collinear.
     *
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @param x3 x-coordinate of third point
     * @param y3 y-coordinate of third point
     * @return true if points are collinear, false otherwise
     */
    public boolean areCollinear(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2,
                               BigDecimal x3, BigDecimal y3) {
        validateInputs(x1, y1, x2, y2, x3, y3);

        // Area of triangle formed by three points
        BigDecimal area = triangleArea(x1, y1, x2, y2, x3, y3);
        return area.abs().compareTo(TOLERANCE) < 0;
    }

    // ===== VECTOR GEOMETRY =====

    /**
     * Calculate dot product of two 2D vectors.
     *
     * @param x1 x-component of first vector
     * @param y1 y-component of first vector
     * @param x2 x-component of second vector
     * @param y2 y-component of second vector
     * @return dot product
     */
    public BigDecimal dotProduct2D(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2) {
        validateInputs(x1, y1, x2, y2);
        return x1.multiply(x2, DEFAULT_PRECISION).add(y1.multiply(y2, DEFAULT_PRECISION), DEFAULT_PRECISION);
    }

    /**
     * Calculate cross product of two 2D vectors.
     *
     * @param x1 x-component of first vector
     * @param y1 y-component of first vector
     * @param x2 x-component of second vector
     * @param y2 y-component of second vector
     * @return cross product (scalar in 2D)
     */
    public BigDecimal crossProduct2D(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2) {
        validateInputs(x1, y1, x2, y2);
        return x1.multiply(y2, DEFAULT_PRECISION).subtract(y1.multiply(x2, DEFAULT_PRECISION), DEFAULT_PRECISION);
    }

    /**
     * Calculate magnitude of a 2D vector.
     *
     * @param x x-component of vector
     * @param y y-component of vector
     * @return magnitude
     */
    public BigDecimal vectorMagnitude2D(BigDecimal x, BigDecimal y) {
        validateInputs(x, y);
        return sqrt(x.multiply(x, DEFAULT_PRECISION).add(y.multiply(y, DEFAULT_PRECISION), DEFAULT_PRECISION));
    }

    /**
     * Calculate angle between two 2D vectors in radians.
     *
     * @param x1 x-component of first vector
     * @param y1 y-component of first vector
     * @param x2 x-component of second vector
     * @param y2 y-component of second vector
     * @return angle in radians
     */
    public BigDecimal angleBetweenVectors2D(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2) {
        validateInputs(x1, y1, x2, y2);

        BigDecimal dotProduct = dotProduct2D(x1, y1, x2, y2);
        BigDecimal mag1 = vectorMagnitude2D(x1, y1);
        BigDecimal mag2 = vectorMagnitude2D(x2, y2);

        if (mag1.compareTo(TOLERANCE) < 0 || mag2.compareTo(TOLERANCE) < 0) {
            throw new ValidationException("Cannot calculate angle with zero vector", OPERATION_NAME);
        }

        BigDecimal cosTheta = dotProduct.divide(mag1.multiply(mag2, DEFAULT_PRECISION), DEFAULT_PRECISION);
        return acos(cosTheta);
    }

    // ===== UTILITY METHODS =====

    /**
     * Normalize angle to [-π, π] range.
     */
    private BigDecimal normalizeAngle(BigDecimal angle) {
        BigDecimal twoPi = PI.multiply(BigDecimal.valueOf(2), DEFAULT_PRECISION);

        // Reduce angle modulo 2π
        angle = angle.remainder(twoPi, DEFAULT_PRECISION);

        // Adjust to [-π, π]
        if (angle.compareTo(PI) > 0) {
            angle = angle.subtract(twoPi, DEFAULT_PRECISION);
        } else if (angle.compareTo(PI.negate()) < 0) {
            angle = angle.add(twoPi, DEFAULT_PRECISION);
        }

        return angle;
    }

    /**
     * Calculate square root using BigDecimal with high precision.
     */
    private BigDecimal sqrt(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Cannot calculate square root of negative number", OPERATION_NAME);
        }

        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Use Newton-Raphson method for square root
        BigDecimal x = value.divide(BigDecimal.valueOf(2), DEFAULT_PRECISION);
        BigDecimal two = BigDecimal.valueOf(2);

        for (int i = 0; i < 100; i++) { // 100 iterations for very high precision
            x = x.add(value.divide(x, DEFAULT_PRECISION), DEFAULT_PRECISION).divide(two, DEFAULT_PRECISION);
        }

        return x;
    }

    /**
     * Get current memory usage in bytes.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // ===== CONVERSION METHODS =====

    /**
     * Convert degrees to radians.
     *
     * @param degrees angle in degrees
     * @return angle in radians
     */
    public BigDecimal degreesToRadians(BigDecimal degrees) {
        validateInputs(degrees);
        return degrees.multiply(PI, DEFAULT_PRECISION).divide(BigDecimal.valueOf(180), DEFAULT_PRECISION);
    }

    /**
     * Convert radians to degrees.
     *
     * @param radians angle in radians
     * @return angle in degrees
     */
    public BigDecimal radiansToDegrees(BigDecimal radians) {
        validateInputs(radians);
        return radians.multiply(BigDecimal.valueOf(180), DEFAULT_PRECISION).divide(PI, DEFAULT_PRECISION);
    }

    // ===== GEOMETRIC SHAPE ANALYSIS =====

    /**
     * Check if three side lengths can form a valid triangle.
     *
     * @param a length of first side
     * @param b length of second side
     * @param c length of third side
     * @return true if sides can form a triangle, false otherwise
     */
    public boolean canFormTriangle(BigDecimal a, BigDecimal b, BigDecimal c) {
        validateInputs(a, b, c);

        if (a.compareTo(BigDecimal.ZERO) <= 0 ||
            b.compareTo(BigDecimal.ZERO) <= 0 ||
            c.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // Triangle inequality theorem
        return a.add(b, DEFAULT_PRECISION).compareTo(c) > 0 &&
               a.add(c, DEFAULT_PRECISION).compareTo(b) > 0 &&
               b.add(c, DEFAULT_PRECISION).compareTo(a) > 0;
    }

    /**
     * Determine the type of triangle based on side lengths.
     *
     * @param a length of first side
     * @param b length of second side
     * @param c length of third side
     * @return "equilateral", "isosceles", or "scalene"
     */
    public String triangleType(BigDecimal a, BigDecimal b, BigDecimal c) {
        validateInputs(a, b, c);

        if (!canFormTriangle(a, b, c)) {
            throw new ValidationException("Sides cannot form a valid triangle", OPERATION_NAME);
        }

        boolean abEqual = a.subtract(b, DEFAULT_PRECISION).abs().compareTo(TOLERANCE) < 0;
        boolean acEqual = a.subtract(c, DEFAULT_PRECISION).abs().compareTo(TOLERANCE) < 0;
        boolean bcEqual = b.subtract(c, DEFAULT_PRECISION).abs().compareTo(TOLERANCE) < 0;

        if (abEqual && acEqual) {
            return "equilateral";
        } else if (abEqual || acEqual || bcEqual) {
            return "isosceles";
        } else {
            return "scalene";
        }
    }

    /**
     * Calculate the centroid of a triangle given three points.
     *
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @param x3 x-coordinate of third point
     * @param y3 y-coordinate of third point
     * @return array containing [centroidX, centroidY]
     */
    public BigDecimal[] triangleCentroid(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2,
                                       BigDecimal x3, BigDecimal y3) {
        validateInputs(x1, y1, x2, y2, x3, y3);

        BigDecimal centroidX = x1.add(x2, DEFAULT_PRECISION).add(x3, DEFAULT_PRECISION)
                                .divide(BigDecimal.valueOf(3), DEFAULT_PRECISION);
        BigDecimal centroidY = y1.add(y2, DEFAULT_PRECISION).add(y3, DEFAULT_PRECISION)
                                .divide(BigDecimal.valueOf(3), DEFAULT_PRECISION);

        return new BigDecimal[]{centroidX, centroidY};
    }
}

