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

package com.netflix.mathlib.physics;

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
 * Physics2D - Production-grade 2D physics engine for game development.
 *
 * This class provides essential 2D physics operations for game development including:
 * - Newtonian physics (F = ma, momentum, impulse)
 * - Kinematics (position, velocity, acceleration updates)
 * - Force systems (gravity, friction, custom forces)
 * - Collision response with physics
 * - Trajectory calculations
 * - Rigid body simulation basics
 * - Particle system foundations
 *
 * Essential for 2D games requiring realistic physics:
 * - Platformers (jumping, falling, momentum)
 * - Physics-based puzzles
 * - Racing games (traction, sliding)
 * - Ball games (Pong, Breakout, Billiards)
 * - Simulation games
 *
 * All implementations are optimized for real-time game loops with:
 * - Fixed time step compatibility
 * - Numerical stability
 * - Performance monitoring
 * - High-precision arithmetic using BigDecimal
 * - Comprehensive input validation
 * - Thread-safe operations where applicable
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class Physics2D implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(Physics2D.class);
    private static final String OPERATION_NAME = "Physics2D";
    private static final String COMPLEXITY = "O(1)-O(n)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;
    private final MathContext DEFAULT_PRECISION = new MathContext(50, RoundingMode.HALF_UP);
    private final BigDecimal TOLERANCE = new BigDecimal("1e-15");
    private final BigDecimal ZERO = BigDecimal.ZERO;
    private final BigDecimal ONE = BigDecimal.ONE;
    private final BigDecimal TWO = new BigDecimal("2");

    // Physics constants
    private static final BigDecimal EARTH_GRAVITY = new BigDecimal("9.81"); // m/s²
    private static final BigDecimal DEFAULT_RESTITUTION = new BigDecimal("0.8"); // Bouncy
    private static final BigDecimal DEFAULT_FRICTION = new BigDecimal("0.1"); // Low friction

    /**
     * Constructor for Physics2D.
     */
    public Physics2D() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Physics2D module");
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

    // ===== PHYSICS ENTITIES =====

    /**
     * 2D Physics Entity - Represents an object with physical properties.
     */
    public static class PhysicsEntity {
        public BigDecimal x, y;           // Position
        public BigDecimal vx, vy;         // Velocity
        public BigDecimal ax, ay;         // Acceleration
        public BigDecimal mass;           // Mass
        public BigDecimal restitution;    // Bounciness (0-1)
        public BigDecimal friction;       // Friction coefficient
        public BigDecimal radius;         // For circular objects
        public boolean isStatic;          // Static objects don't move
        public boolean affectedByGravity; // Whether gravity affects this entity

        public PhysicsEntity(BigDecimal x, BigDecimal y, BigDecimal mass) {
            this.x = x;
            this.y = y;
            this.vx = BigDecimal.ZERO;
            this.vy = BigDecimal.ZERO;
            this.ax = BigDecimal.ZERO;
            this.ay = BigDecimal.ZERO;
            this.mass = mass;
            this.restitution = Physics2D.DEFAULT_RESTITUTION;
            this.friction = Physics2D.DEFAULT_FRICTION;
            this.radius = BigDecimal.ONE;
            this.isStatic = false;
            this.affectedByGravity = true;
        }
    }

    /**
     * Force vector for applying forces to entities.
     */
    public static class Force2D {
        public BigDecimal fx, fy;  // Force components
        public BigDecimal duration; // How long to apply (seconds), -1 for continuous

        public Force2D(BigDecimal fx, BigDecimal fy) {
            this.fx = fx;
            this.fy = fy;
            this.duration = new BigDecimal("-1"); // Continuous
        }

        public Force2D(BigDecimal fx, BigDecimal fy, BigDecimal duration) {
            this.fx = fx;
            this.fy = fy;
            this.duration = duration;
        }
    }

    // ===== NEWTONIAN PHYSICS =====

    /**
     * Apply force to a physics entity (F = ma).
     * Essential for game physics: jumping, pushing, explosions, etc.
     *
     * @param entity the physics entity
     * @param force the force to apply
     * @param deltaTime time step in seconds
     */
    public void applyForce(PhysicsEntity entity, Force2D force, BigDecimal deltaTime) {
        validateInputs(entity, force, deltaTime);

        if (entity.isStatic) {
            return; // Static objects don't move
        }

        if (deltaTime.compareTo(ZERO) <= 0) {
            throw new ValidationException("Delta time must be positive", OPERATION_NAME);
        }

        // F = ma => a = F/m
        BigDecimal accelerationX = force.fx.divide(entity.mass, DEFAULT_PRECISION);
        BigDecimal accelerationY = force.fy.divide(entity.mass, DEFAULT_PRECISION);

        // Apply acceleration for the time step
        entity.ax = entity.ax.add(accelerationX);
        entity.ay = entity.ay.add(accelerationY);

        // If force has limited duration, we could track it here
        if (force.duration.compareTo(ZERO) > 0) {
            // Force duration management would go here
            logger.debug("Applied force with duration: {} seconds", force.duration);
        }

        logger.debug("Applied force ({}, {}) to entity at ({}, {})",
                    force.fx, force.fy, entity.x, entity.y);
    }

    /**
     * Apply gravity to an entity.
     * Essential for platformers and realistic falling objects.
     *
     * @param entity the physics entity
     * @param gravityStrength gravity acceleration (default: 9.81 m/s²)
     */
    public void applyGravity(PhysicsEntity entity, BigDecimal gravityStrength) {
        validateInputs(entity, gravityStrength);

        if (!entity.affectedByGravity || entity.isStatic) {
            return;
        }

        if (gravityStrength.compareTo(ZERO) < 0) {
            throw new ValidationException("Gravity strength cannot be negative", OPERATION_NAME);
        }

        // Gravity always points downward (negative Y in most game coordinate systems)
        entity.ay = entity.ay.subtract(gravityStrength);

        logger.debug("Applied gravity {} to entity", gravityStrength);
    }

    /**
     * Apply gravity with default Earth gravity.
     */
    public void applyGravity(PhysicsEntity entity) {
        applyGravity(entity, Physics2D.EARTH_GRAVITY);
    }

    /**
     * Apply friction/damping to reduce velocity over time.
     * Essential for realistic movement and stopping.
     *
     * @param entity the physics entity
     * @param frictionCoefficient friction coefficient (0-1)
     * @param deltaTime time step
     */
    public void applyFriction(PhysicsEntity entity, BigDecimal frictionCoefficient, BigDecimal deltaTime) {
        validateInputs(entity, frictionCoefficient, deltaTime);

        if (entity.isStatic) {
            return;
        }

        if (frictionCoefficient.compareTo(ZERO) < 0 || frictionCoefficient.compareTo(ONE) > 0) {
            throw new ValidationException("Friction coefficient must be between 0 and 1", OPERATION_NAME);
        }

        // Apply friction as opposing force proportional to velocity
        BigDecimal frictionForceX = entity.vx.multiply(frictionCoefficient).negate();
        BigDecimal frictionForceY = entity.vy.multiply(frictionCoefficient).negate();

        // Only apply friction if entity is moving
        if (entity.vx.abs().compareTo(TOLERANCE) > 0) {
            entity.ax = entity.ax.add(frictionForceX.divide(entity.mass, DEFAULT_PRECISION));
        }
        if (entity.vy.abs().compareTo(TOLERANCE) > 0) {
            entity.ay = entity.ay.add(frictionForceY.divide(entity.mass, DEFAULT_PRECISION));
        }

        logger.debug("Applied friction {} to entity", frictionCoefficient);
    }

    // ===== KINEMATICS =====

    /**
     * Update entity kinematics using Euler integration.
     * Essential for game physics loops: position = position + velocity * dt + 0.5 * acceleration * dt²
     *
     * @param entity the physics entity
     * @param deltaTime time step in seconds
     */
    public void updateKinematics(PhysicsEntity entity, BigDecimal deltaTime) {
        validateInputs(entity, deltaTime);

        if (deltaTime.compareTo(ZERO) <= 0) {
            throw new ValidationException("Delta time must be positive", OPERATION_NAME);
        }

        // Euler integration for better stability in games
        // v = v + a * dt
        entity.vx = entity.vx.add(entity.ax.multiply(deltaTime, DEFAULT_PRECISION));
        entity.vy = entity.vy.add(entity.ay.multiply(deltaTime, DEFAULT_PRECISION));

        // x = x + v * dt + 0.5 * a * dt² (more accurate integration)
        BigDecimal halfDeltaTimeSquared = deltaTime.multiply(deltaTime, DEFAULT_PRECISION).divide(TWO, DEFAULT_PRECISION);

        entity.x = entity.x.add(entity.vx.multiply(deltaTime, DEFAULT_PRECISION))
                          .add(entity.ax.multiply(halfDeltaTimeSquared, DEFAULT_PRECISION));

        entity.y = entity.y.add(entity.vy.multiply(deltaTime, DEFAULT_PRECISION))
                          .add(entity.ay.multiply(halfDeltaTimeSquared, DEFAULT_PRECISION));

        // Reset acceleration for next frame (forces need to be re-applied)
        entity.ax = ZERO;
        entity.ay = ZERO;

        logger.debug("Updated kinematics: position ({}, {}), velocity ({}, {})",
                    entity.x, entity.y, entity.vx, entity.vy);
    }

    /**
     * Set entity velocity directly (teleportation, instant speed changes).
     */
    public void setVelocity(PhysicsEntity entity, BigDecimal vx, BigDecimal vy) {
        validateInputs(entity, vx, vy);

        entity.vx = vx;
        entity.vy = vy;

        logger.debug("Set entity velocity to ({}, {})", vx, vy);
    }

    /**
     * Set entity position directly (teleportation).
     */
    public void setPosition(PhysicsEntity entity, BigDecimal x, BigDecimal y) {
        validateInputs(entity, x, y);

        entity.x = x;
        entity.y = y;

        logger.debug("Set entity position to ({}, {})", x, y);
    }

    // ===== MOMENTUM AND IMPULSE =====

    /**
     * Calculate momentum of an entity (p = mv).
     * Essential for collision physics and conservation of momentum.
     *
     * @param entity the physics entity
     * @return momentum vector [px, py]
     */
    public BigDecimal[] calculateMomentum(PhysicsEntity entity) {
        validateInputs(entity);

        BigDecimal px = entity.mass.multiply(entity.vx, DEFAULT_PRECISION);
        BigDecimal py = entity.mass.multiply(entity.vy, DEFAULT_PRECISION);

        return new BigDecimal[]{px, py};
    }

    /**
     * Calculate kinetic energy (KE = 0.5 * m * v²).
     * Useful for energy conservation and damage calculations.
     *
     * @param entity the physics entity
     * @return kinetic energy
     */
    public BigDecimal calculateKineticEnergy(PhysicsEntity entity) {
        validateInputs(entity);

        BigDecimal velocitySquared = entity.vx.multiply(entity.vx, DEFAULT_PRECISION)
                                    .add(entity.vy.multiply(entity.vy, DEFAULT_PRECISION));

        return entity.mass.multiply(velocitySquared, DEFAULT_PRECISION)
                         .divide(TWO, DEFAULT_PRECISION);
    }

    /**
     * Apply impulse to an entity (instant change in momentum).
     * Essential for collision response and explosions.
     *
     * @param entity the physics entity
     * @param impulseX x-component of impulse
     * @param impulseY y-component of impulse
     */
    public void applyImpulse(PhysicsEntity entity, BigDecimal impulseX, BigDecimal impulseY) {
        validateInputs(entity, impulseX, impulseY);

        if (entity.isStatic) {
            return; // Static objects don't respond to impulses
        }

        // J = Δp => Δv = J/m
        BigDecimal deltaVx = impulseX.divide(entity.mass, DEFAULT_PRECISION);
        BigDecimal deltaVy = impulseY.divide(entity.mass, DEFAULT_PRECISION);

        entity.vx = entity.vx.add(deltaVx);
        entity.vy = entity.vy.add(deltaVy);

        logger.debug("Applied impulse ({}, {}) to entity", impulseX, impulseY);
    }

    // ===== COLLISION PHYSICS =====

    /**
     * Handle collision response between two entities.
     * Essential for realistic physics in games (bouncing, momentum transfer).
     *
     * @param entityA first entity
     * @param entityB second entity
     * @param normalX collision normal x-component
     * @param normalY collision normal y-component
     */
    public void collisionResponse(PhysicsEntity entityA, PhysicsEntity entityB,
                                BigDecimal normalX, BigDecimal normalY) {
        validateInputs(entityA, entityB, normalX, normalY);

        // Normalize the collision normal
        BigDecimal normalLength = normalX.multiply(normalX, DEFAULT_PRECISION)
                               .add(normalY.multiply(normalY, DEFAULT_PRECISION))
                               .sqrt(DEFAULT_PRECISION);

        if (normalLength.compareTo(TOLERANCE) < 0) {
            throw new ValidationException("Invalid collision normal", OPERATION_NAME);
        }

        BigDecimal nx = normalX.divide(normalLength, DEFAULT_PRECISION);
        BigDecimal ny = normalY.divide(normalLength, DEFAULT_PRECISION);

        // Calculate relative velocity
        BigDecimal relativeVx = entityA.vx.subtract(entityB.vx);
        BigDecimal relativeVy = entityA.vy.subtract(entityB.vy);

        // Calculate relative velocity along the normal
        BigDecimal velocityAlongNormal = relativeVx.multiply(nx, DEFAULT_PRECISION)
                                     .add(relativeVy.multiply(ny, DEFAULT_PRECISION));

        // Don't resolve if velocities are separating
        if (velocityAlongNormal.compareTo(ZERO) > 0) {
            return;
        }

        // Calculate restitution (bounciness)
        BigDecimal restitution = entityA.restitution.min(entityB.restitution);

        // Calculate impulse scalar
        BigDecimal impulseScalar = velocityAlongNormal.multiply(ONE.add(restitution), DEFAULT_PRECISION)
                               .divide(ONE.divide(entityA.mass, DEFAULT_PRECISION)
                                     .add(ONE.divide(entityB.mass, DEFAULT_PRECISION)), DEFAULT_PRECISION);

        // Apply impulse
        if (!entityA.isStatic) {
            BigDecimal impulseAx = nx.multiply(impulseScalar.divide(entityA.mass, DEFAULT_PRECISION));
            BigDecimal impulseAy = ny.multiply(impulseScalar.divide(entityA.mass, DEFAULT_PRECISION));
            applyImpulse(entityA, impulseAx, impulseAy);
        }

        if (!entityB.isStatic) {
            BigDecimal impulseBx = nx.multiply(impulseScalar.divide(entityB.mass, DEFAULT_PRECISION)).negate();
            BigDecimal impulseBy = ny.multiply(impulseScalar.divide(entityB.mass, DEFAULT_PRECISION)).negate();
            applyImpulse(entityB, impulseBx, impulseBy);
        }

        logger.debug("Resolved collision between entities");
    }

    // ===== TRAJECTORY CALCULATIONS =====

    /**
     * Calculate projectile trajectory.
     * Essential for games with projectiles (cannonballs, arrows, bullets).
     *
     * @param initialVelocity initial velocity magnitude
     * @param launchAngle launch angle in radians
     * @param gravity gravity strength
     * @param time time since launch
     * @return position vector [x, y]
     */
    public BigDecimal[] calculateTrajectory(BigDecimal initialVelocity, BigDecimal launchAngle,
                                          BigDecimal gravity, BigDecimal time) {
        validateInputs(initialVelocity, launchAngle, gravity, time);

        // Parametric equations for projectile motion
        // x = v₀ * cos(θ) * t
        // y = v₀ * sin(θ) * t - 0.5 * g * t²

        BigDecimal cosAngle = cos(launchAngle);
        BigDecimal sinAngle = sin(launchAngle);

        BigDecimal x = initialVelocity.multiply(cosAngle, DEFAULT_PRECISION)
                     .multiply(time, DEFAULT_PRECISION);

        BigDecimal y = initialVelocity.multiply(sinAngle, DEFAULT_PRECISION)
                     .multiply(time, DEFAULT_PRECISION)
                     .subtract(gravity.multiply(time, DEFAULT_PRECISION)
                              .multiply(time, DEFAULT_PRECISION)
                              .divide(TWO, DEFAULT_PRECISION));

        return new BigDecimal[]{x, y};
    }

    /**
     * Calculate maximum range of projectile.
     *
     * @param initialVelocity initial velocity magnitude
     * @param launchAngle launch angle in radians
     * @param gravity gravity strength
     * @return maximum range
     */
    public BigDecimal calculateMaxRange(BigDecimal initialVelocity, BigDecimal launchAngle, BigDecimal gravity) {
        validateInputs(initialVelocity, launchAngle, gravity);

        // R = (v₀² * sin(2θ)) / g
        BigDecimal sinTwoTheta = sin(launchAngle.multiply(TWO, DEFAULT_PRECISION));
        BigDecimal velocitySquared = initialVelocity.multiply(initialVelocity, DEFAULT_PRECISION);

        return velocitySquared.multiply(sinTwoTheta, DEFAULT_PRECISION)
                            .divide(gravity, DEFAULT_PRECISION);
    }

    /**
     * Calculate time of flight for projectile.
     *
     * @param initialVelocity initial velocity magnitude
     * @param launchAngle launch angle in radians
     * @param gravity gravity strength
     * @return time of flight
     */
    public BigDecimal calculateTimeOfFlight(BigDecimal initialVelocity, BigDecimal launchAngle, BigDecimal gravity) {
        validateInputs(initialVelocity, launchAngle, gravity);

        // t = (2 * v₀ * sin(θ)) / g
        BigDecimal sinTheta = sin(launchAngle);

        return initialVelocity.multiply(sinTheta, DEFAULT_PRECISION)
                            .multiply(TWO, DEFAULT_PRECISION)
                            .divide(gravity, DEFAULT_PRECISION);
    }

    // ===== UTILITY FUNCTIONS =====

    /**
     * Calculate distance between two points.
     */
    private BigDecimal distance(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2) {
        BigDecimal dx = x1.subtract(x2, DEFAULT_PRECISION);
        BigDecimal dy = y1.subtract(y2, DEFAULT_PRECISION);
        return dx.multiply(dx, DEFAULT_PRECISION)
                .add(dy.multiply(dy, DEFAULT_PRECISION))
                .sqrt(DEFAULT_PRECISION);
    }

    /**
     * Calculate cosine using Taylor series approximation.
     */
    private BigDecimal cos(BigDecimal angle) {
        // cos(x) ≈ 1 - x²/2! + x⁴/4! - x⁶/6! + ...
        BigDecimal x = angle;
        BigDecimal result = ONE;
        BigDecimal term = ONE;
        BigDecimal xSquared = x.multiply(x, DEFAULT_PRECISION);

        for (int n = 1; n <= 10; n++) {
            term = term.multiply(xSquared, DEFAULT_PRECISION)
                       .divide(new BigDecimal(2 * n - 1), DEFAULT_PRECISION)
                       .divide(new BigDecimal(2 * n), DEFAULT_PRECISION)
                       .negate();
            result = result.add(term);
        }

        return result;
    }

    /**
     * Calculate sine using Taylor series approximation.
     */
    private BigDecimal sin(BigDecimal angle) {
        // sin(x) ≈ x - x³/3! + x⁵/5! - x⁷/7! + ...
        BigDecimal x = angle;
        BigDecimal result = x;
        BigDecimal term = x;
        BigDecimal xSquared = x.multiply(x, DEFAULT_PRECISION);

        for (int n = 1; n <= 10; n++) {
            term = term.multiply(xSquared, DEFAULT_PRECISION)
                       .divide(new BigDecimal(2 * n), DEFAULT_PRECISION)
                       .divide(new BigDecimal(2 * n + 1), DEFAULT_PRECISION)
                       .negate();
            result = result.add(term);
        }

        return result;
    }

    /**
     * Create a physics entity with default properties.
     */
    public PhysicsEntity createEntity(BigDecimal x, BigDecimal y, BigDecimal mass) {
        return new PhysicsEntity(x, y, mass);
    }

    /**
     * Create a static physics entity (immovable object).
     */
    public PhysicsEntity createStaticEntity(BigDecimal x, BigDecimal y, BigDecimal radius) {
        PhysicsEntity entity = new PhysicsEntity(x, y, new BigDecimal("999999")); // Very heavy
        entity.isStatic = true;
        entity.radius = radius;
        entity.affectedByGravity = false;
        return entity;
    }

    /**
     * Create a force vector.
     */
    public Force2D createForce(BigDecimal fx, BigDecimal fy) {
        return new Force2D(fx, fy);
    }

    /**
     * Create a force vector with duration.
     */
    public Force2D createForce(BigDecimal fx, BigDecimal fy, BigDecimal duration) {
        return new Force2D(fx, fy, duration);
    }
}
