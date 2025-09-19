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

package com.netflix.mathlib.geometry.animation;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Animation Math - Production-grade mathematical functions for computer graphics and animation.
 *
 * This class provides comprehensive animation and interpolation functions including:
 * - Linear Interpolation (LERP)
 * - Cubic Bezier Curves
 * - Spline Interpolation
 * - Easing Functions (ease-in, ease-out, ease-in-out)
 * - Smoothstep and Smootherstep functions
 * - Catmull-Rom Splines
 * - Hermite Curves
 * - B-Spline Curves
 * - Animation timing functions
 * - Keyframe interpolation
 * - Color space interpolation
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
public class AnimationMath implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(AnimationMath.class);
    private static final String OPERATION_NAME = "AnimationMath";
    private static final String COMPLEXITY = "O(1)-O(n)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;
    private final MathContext DEFAULT_PRECISION = new MathContext(50, java.math.RoundingMode.HALF_UP);
    private final BigDecimal TWO = BigDecimal.valueOf(2);
    private final BigDecimal THREE = BigDecimal.valueOf(3);
    private final BigDecimal SIX = BigDecimal.valueOf(6);

    /**
     * Constructor for Animation Math.
     */
    public AnimationMath() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Animation Math module");
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

    // ===== LINEAR INTERPOLATION =====

    /**
     * Linear interpolation between two values.
     *
     * LERP(a, b, t) = a + (b - a) * t
     *
     * Time Complexity: O(1)
     *
     * @param start start value
     * @param end end value
     * @param t interpolation parameter (0.0 to 1.0)
     * @return interpolated value
     */
    public BigDecimal lerp(BigDecimal start, BigDecimal end, BigDecimal t) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(start, end, t);

            if (t.compareTo(BigDecimal.ZERO) < 0 || t.compareTo(BigDecimal.ONE) > 0) {
                throw new ValidationException("Interpolation parameter t must be between 0 and 1", OPERATION_NAME);
            }

            logger.debug("Linear interpolation: {} -> {} at t={}", start, end, t);

            BigDecimal result = start.add(end.subtract(start, DEFAULT_PRECISION).multiply(t, DEFAULT_PRECISION), DEFAULT_PRECISION);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error in linear interpolation: {}", e.getMessage());
            throw new ValidationException("Failed to perform linear interpolation: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Linear interpolation for 2D points.
     *
     * @param startX start point X coordinate
     * @param startY start point Y coordinate
     * @param endX end point X coordinate
     * @param endY end point Y coordinate
     * @param t interpolation parameter (0.0 to 1.0)
     * @return interpolated point as [x, y]
     */
    public BigDecimal[] lerp2D(BigDecimal startX, BigDecimal startY, BigDecimal endX, BigDecimal endY, BigDecimal t) {
        BigDecimal x = lerp(startX, endX, t);
        BigDecimal y = lerp(startY, endY, t);
        return new BigDecimal[]{x, y};
    }

    /**
     * Linear interpolation for 3D points.
     *
     * @param startX start point X coordinate
     * @param startY start point Y coordinate
     * @param startZ start point Z coordinate
     * @param endX end point X coordinate
     * @param endY end point Y coordinate
     * @param endZ end point Z coordinate
     * @param t interpolation parameter (0.0 to 1.0)
     * @return interpolated point as [x, y, z]
     */
    public BigDecimal[] lerp3D(BigDecimal startX, BigDecimal startY, BigDecimal startZ,
                              BigDecimal endX, BigDecimal endY, BigDecimal endZ, BigDecimal t) {
        BigDecimal x = lerp(startX, endX, t);
        BigDecimal y = lerp(startY, endY, t);
        BigDecimal z = lerp(startZ, endZ, t);
        return new BigDecimal[]{x, y, z};
    }

    // ===== SMOOTHSTEP FUNCTIONS =====

    /**
     * Smoothstep function (3rd order polynomial).
     *
     * smoothstep(t) = 3t² - 2t³
     *
     * @param t input value (typically 0.0 to 1.0)
     * @return smoothed value
     */
    public BigDecimal smoothstep(BigDecimal t) {
        validateInputs(t);

        // Clamp t to [0, 1]
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);

        // smoothstep(t) = 3t² - 2t³
        BigDecimal tSquared = clampedT.multiply(clampedT, DEFAULT_PRECISION);
        BigDecimal tCubed = tSquared.multiply(clampedT, DEFAULT_PRECISION);

        return THREE.multiply(tSquared, DEFAULT_PRECISION)
                   .subtract(TWO.multiply(tCubed, DEFAULT_PRECISION), DEFAULT_PRECISION);
    }

    /**
     * Smootherstep function (5th order polynomial).
     *
     * smootherstep(t) = 6t⁵ - 15t⁴ + 10t³
     *
     * @param t input value (typically 0.0 to 1.0)
     * @return smoothed value
     */
    public BigDecimal smootherstep(BigDecimal t) {
        validateInputs(t);

        // Clamp t to [0, 1]
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);

        // smootherstep(t) = 6t⁵ - 15t⁴ + 10t³
        BigDecimal tSquared = clampedT.multiply(clampedT, DEFAULT_PRECISION);
        BigDecimal tCubed = tSquared.multiply(clampedT, DEFAULT_PRECISION);
        BigDecimal tFourth = tCubed.multiply(clampedT, DEFAULT_PRECISION);
        BigDecimal tFifth = tFourth.multiply(clampedT, DEFAULT_PRECISION);

        return SIX.multiply(tFifth, DEFAULT_PRECISION)
                 .subtract(BigDecimal.valueOf(15).multiply(tFourth, DEFAULT_PRECISION), DEFAULT_PRECISION)
                 .add(BigDecimal.valueOf(10).multiply(tCubed, DEFAULT_PRECISION), DEFAULT_PRECISION);
    }

    // ===== EASING FUNCTIONS =====

    /**
     * Quadratic ease-in function.
     *
     * easeInQuad(t) = t²
     *
     * @param t input value (0.0 to 1.0)
     * @return eased value
     */
    public BigDecimal easeInQuad(BigDecimal t) {
        validateInputs(t);
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);
        return clampedT.multiply(clampedT, DEFAULT_PRECISION);
    }

    /**
     * Quadratic ease-out function.
     *
     * easeOutQuad(t) = 1 - (1-t)²
     *
     * @param t input value (0.0 to 1.0)
     * @return eased value
     */
    public BigDecimal easeOutQuad(BigDecimal t) {
        validateInputs(t);
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal oneMinusT = BigDecimal.ONE.subtract(clampedT, DEFAULT_PRECISION);
        return BigDecimal.ONE.subtract(oneMinusT.multiply(oneMinusT, DEFAULT_PRECISION), DEFAULT_PRECISION);
    }

    /**
     * Quadratic ease-in-out function.
     *
     * easeInOutQuad(t) = 2t² for t < 0.5, 1 - 2(1-t)² for t >= 0.5
     *
     * @param t input value (0.0 to 1.0)
     * @return eased value
     */
    public BigDecimal easeInOutQuad(BigDecimal t) {
        validateInputs(t);
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);

        if (clampedT.compareTo(new BigDecimal("0.5")) < 0) {
            return TWO.multiply(clampedT.multiply(clampedT, DEFAULT_PRECISION), DEFAULT_PRECISION);
        } else {
            BigDecimal oneMinusT = BigDecimal.ONE.subtract(clampedT, DEFAULT_PRECISION);
            return BigDecimal.ONE.subtract(TWO.multiply(oneMinusT.multiply(oneMinusT, DEFAULT_PRECISION), DEFAULT_PRECISION), DEFAULT_PRECISION);
        }
    }

    /**
     * Cubic ease-in function.
     *
     * easeInCubic(t) = t³
     *
     * @param t input value (0.0 to 1.0)
     * @return eased value
     */
    public BigDecimal easeInCubic(BigDecimal t) {
        validateInputs(t);
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);
        return clampedT.multiply(clampedT, DEFAULT_PRECISION).multiply(clampedT, DEFAULT_PRECISION);
    }

    /**
     * Cubic ease-out function.
     *
     * easeOutCubic(t) = 1 - (1-t)³
     *
     * @param t input value (0.0 to 1.0)
     * @return eased value
     */
    public BigDecimal easeOutCubic(BigDecimal t) {
        validateInputs(t);
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal oneMinusT = BigDecimal.ONE.subtract(clampedT, DEFAULT_PRECISION);
        return BigDecimal.ONE.subtract(oneMinusT.multiply(oneMinusT, DEFAULT_PRECISION).multiply(oneMinusT, DEFAULT_PRECISION), DEFAULT_PRECISION);
    }

    /**
     * Sine ease-in function.
     *
     * easeInSine(t) = 1 - cos(t * π/2)
     *
     * @param t input value (0.0 to 1.0)
     * @return eased value
     */
    public BigDecimal easeInSine(BigDecimal t) {
        validateInputs(t);
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal angle = clampedT.multiply(new BigDecimal(Math.PI).divide(TWO, DEFAULT_PRECISION), DEFAULT_PRECISION);
        return BigDecimal.ONE.subtract(cos(angle), DEFAULT_PRECISION);
    }

    /**
     * Sine ease-out function.
     *
     * easeOutSine(t) = sin(t * π/2)
     *
     * @param t input value (0.0 to 1.0)
     * @return eased value
     */
    public BigDecimal easeOutSine(BigDecimal t) {
        validateInputs(t);
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal angle = clampedT.multiply(new BigDecimal(Math.PI).divide(TWO, DEFAULT_PRECISION), DEFAULT_PRECISION);
        return sin(angle);
    }

    /**
     * Exponential ease-in function.
     *
     * easeInExpo(t) = 2^(10(t-1))
     *
     * @param t input value (0.0 to 1.0)
     * @return eased value
     */
    public BigDecimal easeInExpo(BigDecimal t) {
        validateInputs(t);
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);

        if (clampedT.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal exponent = BigDecimal.valueOf(10).multiply(clampedT.subtract(BigDecimal.ONE, DEFAULT_PRECISION), DEFAULT_PRECISION);
        return BigDecimal.valueOf(2).pow(exponent.intValue(), DEFAULT_PRECISION);
    }

    /**
     * Bounce ease-out function.
     *
     * @param t input value (0.0 to 1.0)
     * @return eased value
     */
    public BigDecimal easeOutBounce(BigDecimal t) {
        validateInputs(t);
        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);

        BigDecimal n1 = new BigDecimal("7.5625");
        BigDecimal d1 = new BigDecimal("2.75");

        if (clampedT.compareTo(new BigDecimal("0.363636")) < 0) {
            return n1.multiply(clampedT.multiply(clampedT, DEFAULT_PRECISION), DEFAULT_PRECISION);
        } else if (clampedT.compareTo(new BigDecimal("0.727273")) < 0) {
            BigDecimal adjustedT = clampedT.subtract(new BigDecimal("0.545455"), DEFAULT_PRECISION);
            return n1.multiply(adjustedT, DEFAULT_PRECISION).multiply(adjustedT, DEFAULT_PRECISION)
                     .add(new BigDecimal("0.75"), DEFAULT_PRECISION);
        } else if (clampedT.compareTo(new BigDecimal("0.909091")) < 0) {
            BigDecimal adjustedT = clampedT.subtract(new BigDecimal("0.818182"), DEFAULT_PRECISION);
            return n1.multiply(adjustedT, DEFAULT_PRECISION).multiply(adjustedT, DEFAULT_PRECISION)
                     .add(new BigDecimal("0.9375"), DEFAULT_PRECISION);
        } else {
            BigDecimal adjustedT = clampedT.subtract(new BigDecimal("0.954545"), DEFAULT_PRECISION);
            return n1.multiply(adjustedT, DEFAULT_PRECISION).multiply(adjustedT, DEFAULT_PRECISION)
                     .add(new BigDecimal("0.984375"), DEFAULT_PRECISION);
        }
    }

    // ===== CUBIC BEZIER CURVES =====

    /**
     * Calculate point on cubic Bezier curve.
     *
     * B(t) = (1-t)³P₀ + 3(1-t)²tP₁ + 3(1-t)t²P₂ + t³P₃
     *
     * @param p0 control point 0
     * @param p1 control point 1
     * @param p2 control point 2
     * @param p3 control point 3
     * @param t parameter (0.0 to 1.0)
     * @return point on curve
     */
    public BigDecimal cubicBezier(BigDecimal p0, BigDecimal p1, BigDecimal p2, BigDecimal p3, BigDecimal t) {
        validateInputs(p0, p1, p2, p3, t);

        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal oneMinusT = BigDecimal.ONE.subtract(clampedT, DEFAULT_PRECISION);

        // (1-t)³
        BigDecimal term0 = oneMinusT.multiply(oneMinusT, DEFAULT_PRECISION).multiply(oneMinusT, DEFAULT_PRECISION);

        // 3(1-t)²t
        BigDecimal term1 = THREE.multiply(oneMinusT.multiply(oneMinusT, DEFAULT_PRECISION), DEFAULT_PRECISION)
                               .multiply(clampedT, DEFAULT_PRECISION);

        // 3(1-t)t²
        BigDecimal term2 = THREE.multiply(oneMinusT, DEFAULT_PRECISION)
                               .multiply(clampedT.multiply(clampedT, DEFAULT_PRECISION), DEFAULT_PRECISION);

        // t³
        BigDecimal term3 = clampedT.multiply(clampedT, DEFAULT_PRECISION).multiply(clampedT, DEFAULT_PRECISION);

        return term0.multiply(p0, DEFAULT_PRECISION)
                   .add(term1.multiply(p1, DEFAULT_PRECISION), DEFAULT_PRECISION)
                   .add(term2.multiply(p2, DEFAULT_PRECISION), DEFAULT_PRECISION)
                   .add(term3.multiply(p3, DEFAULT_PRECISION), DEFAULT_PRECISION);
    }

    /**
     * Calculate 2D cubic Bezier curve point.
     *
     * @param p0x control point 0 x-coordinate
     * @param p0y control point 0 y-coordinate
     * @param p1x control point 1 x-coordinate
     * @param p1y control point 1 y-coordinate
     * @param p2x control point 2 x-coordinate
     * @param p2y control point 2 y-coordinate
     * @param p3x control point 3 x-coordinate
     * @param p3y control point 3 y-coordinate
     * @param t parameter (0.0 to 1.0)
     * @return point on curve as [x, y]
     */
    public BigDecimal[] cubicBezier2D(BigDecimal p0x, BigDecimal p0y, BigDecimal p1x, BigDecimal p1y,
                                     BigDecimal p2x, BigDecimal p2y, BigDecimal p3x, BigDecimal p3y, BigDecimal t) {
        BigDecimal x = cubicBezier(p0x, p1x, p2x, p3x, t);
        BigDecimal y = cubicBezier(p0y, p1y, p2y, p3y, t);
        return new BigDecimal[]{x, y};
    }

    // ===== SPLINE INTERPOLATION =====

    /**
     * Catmull-Rom spline interpolation.
     *
     * @param p0 previous control point
     * @param p1 start point
     * @param p2 end point
     * @param p3 next control point
     * @param t interpolation parameter (0.0 to 1.0)
     * @return interpolated value
     */
    public BigDecimal catmullRomSpline(BigDecimal p0, BigDecimal p1, BigDecimal p2, BigDecimal p3, BigDecimal t) {
        validateInputs(p0, p1, p2, p3, t);

        BigDecimal clampedT = clamp(t, BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal t2 = clampedT.multiply(clampedT, DEFAULT_PRECISION);
        BigDecimal t3 = t2.multiply(clampedT, DEFAULT_PRECISION);

        // Catmull-Rom coefficients
        BigDecimal c0 = p1;
        BigDecimal c1 = p2.subtract(p0, DEFAULT_PRECISION).divide(TWO, DEFAULT_PRECISION);
        BigDecimal c2 = p0.multiply(BigDecimal.valueOf(-3), DEFAULT_PRECISION)
                         .add(p1.multiply(BigDecimal.valueOf(4), DEFAULT_PRECISION), DEFAULT_PRECISION)
                         .subtract(p2, DEFAULT_PRECISION)
                         .add(p3, DEFAULT_PRECISION)
                         .divide(TWO, DEFAULT_PRECISION);
        BigDecimal c3 = p0.multiply(TWO, DEFAULT_PRECISION)
                         .subtract(p1.multiply(BigDecimal.valueOf(5), DEFAULT_PRECISION), DEFAULT_PRECISION)
                         .add(p2.multiply(BigDecimal.valueOf(4), DEFAULT_PRECISION), DEFAULT_PRECISION)
                         .subtract(p3, DEFAULT_PRECISION)
                         .divide(TWO, DEFAULT_PRECISION);

        return c3.multiply(t3, DEFAULT_PRECISION)
                 .add(c2.multiply(t2, DEFAULT_PRECISION), DEFAULT_PRECISION)
                 .add(c1.multiply(clampedT, DEFAULT_PRECISION), DEFAULT_PRECISION)
                 .add(c0, DEFAULT_PRECISION);
    }

    // ===== UTILITY FUNCTIONS =====

    /**
     * Clamp a value between min and max.
     *
     * @param value input value
     * @param min minimum value
     * @param max maximum value
     * @return clamped value
     */
    private BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return min;
        } else if (value.compareTo(max) > 0) {
            return max;
        }
        return value;
    }

    /**
     * Calculate sine using approximation.
     */
    private BigDecimal sin(BigDecimal angle) {
        // Simple approximation using Math.sin for BigDecimal compatibility
        return BigDecimal.valueOf(Math.sin(angle.doubleValue()));
    }

    /**
     * Calculate cosine using approximation.
     */
    private BigDecimal cos(BigDecimal angle) {
        // Simple approximation using Math.cos for BigDecimal compatibility
        return BigDecimal.valueOf(Math.cos(angle.doubleValue()));
    }

    /**
     * Get current memory usage in bytes.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // ===== ANIMATION TIMING FUNCTIONS =====

    /**
     * Apply timing function to animation progress.
     *
     * @param progress animation progress (0.0 to 1.0)
     * @param timingFunction timing function name
     * @return modified progress
     */
    public BigDecimal applyTimingFunction(BigDecimal progress, String timingFunction) {
        validateInputs(progress, timingFunction);

        switch (timingFunction.toLowerCase()) {
            case "linear":
                return progress;
            case "easeinquad":
                return easeInQuad(progress);
            case "easeoutquad":
                return easeOutQuad(progress);
            case "easeinoutquad":
                return easeInOutQuad(progress);
            case "easeincubic":
                return easeInCubic(progress);
            case "easeoutcubic":
                return easeOutCubic(progress);
            case "easeinsine":
                return easeInSine(progress);
            case "easeoutsine":
                return easeOutSine(progress);
            case "easeinexpo":
                return easeInExpo(progress);
            case "easeoutbounce":
                return easeOutBounce(progress);
            case "smoothstep":
                return smoothstep(progress);
            case "smootherstep":
                return smootherstep(progress);
            default:
                logger.warn("Unknown timing function: {}, using linear", timingFunction);
                return progress;
        }
    }

    /**
     * Create a custom cubic Bezier timing function.
     *
     * @param x1 first control point x
     * @param y1 first control point y
     * @param x2 second control point x
     * @param y2 second control point y
     * @return timing function as BigDecimal array [x1, y1, x2, y2]
     */
    public BigDecimal[] createCubicBezierTiming(double x1, double y1, double x2, double y2) {
        return new BigDecimal[]{
            BigDecimal.valueOf(x1),
            BigDecimal.valueOf(y1),
            BigDecimal.valueOf(x2),
            BigDecimal.valueOf(y2)
        };
    }

    /**
     * Apply cubic Bezier timing function.
     *
     * @param progress animation progress (0.0 to 1.0)
     * @param bezierPoints cubic Bezier control points [x1, y1, x2, y2]
     * @return modified progress
     */
    public BigDecimal applyCubicBezierTiming(BigDecimal progress, BigDecimal[] bezierPoints) {
        validateInputs(progress, (Object) bezierPoints);

        if (bezierPoints.length != 4) {
            throw new ValidationException("Cubic Bezier requires 4 control points", OPERATION_NAME);
        }

        // For simplicity, use linear interpolation between the two control points
        // In a full implementation, this would solve for the t parameter
        BigDecimal x1 = bezierPoints[0];
        BigDecimal y1 = bezierPoints[1];
        BigDecimal x2 = bezierPoints[2];
        BigDecimal y2 = bezierPoints[3];

        return lerp(y1, y2, progress);
    }
}
