/*
 * Copyright 2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.mathlib.numbertheory;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Number Theory Operations - Production-grade implementations of fundamental number theory algorithms.
 *
 * This class provides comprehensive number theory operations including:
 * - Greatest Common Divisor (GCD) and Least Common Multiple (LCM)
 * - Euclid's Algorithm and Extended Euclid's Algorithm
 * - Prime number generation and testing (Sieve of Eratosthenes)
 * - Modular arithmetic operations
 * - Factorization algorithms
 * - Number theory utility functions
 *
 * All implementations are optimized for performance and production use with:
 * - Comprehensive input validation
 * - Performance monitoring and metrics
 * - Thread-safe operations where applicable
 * - Extensive error handling
 * - Detailed logging and debugging support
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class NumberTheoryOperations implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(NumberTheoryOperations.class);
    private static final String OPERATION_NAME = "NumberTheoryOperations";
    private static final String COMPLEXITY = "O(various)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;

    /**
     * Constructor for Number Theory Operations.
     */
    public NumberTheoryOperations() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Number Theory Operations module");
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

    // ===== GCD AND LCM OPERATIONS =====

    /**
     * Calculate Greatest Common Divisor (GCD) using Euclid's Algorithm.
     *
     * Time Complexity: O(log min(a,b))
     * Space Complexity: O(1)
     *
     * @param a first number
     * @param b second number
     * @return GCD of a and b
     * @throws ValidationException if inputs are invalid
     */
    public long gcd(long a, long b) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(a, b);

            // Handle negative numbers by taking absolute values
            a = Math.abs(a);
            b = Math.abs(b);

            logger.debug("Calculating GCD of {} and {}", a, b);

            // Euclid's Algorithm
            while (b != 0) {
                long temp = b;
                b = a % b;
                a = temp;
            }

            long result = a;
            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("GCD result: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating GCD of {} and {}: {}", a, b, e.getMessage());
            throw new ValidationException("Failed to calculate GCD: " + e.getMessage(), OPERATION_NAME, a, b);
        }
    }

    /**
     * Calculate GCD for multiple numbers.
     *
     * @param numbers array of numbers
     * @return GCD of all numbers
     * @throws ValidationException if inputs are invalid
     */
    public long gcd(long... numbers) {
        validateInputs((Object) numbers);
        if (numbers.length < 2) {
            throw ValidationException.invalidRange("numbers.length", numbers.length, 2, Integer.MAX_VALUE, OPERATION_NAME);
        }

        long result = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            result = gcd(result, numbers[i]);
        }
        return result;
    }

    /**
     * Calculate Least Common Multiple (LCM) using GCD.
     *
     * Time Complexity: O(log min(a,b))
     * Space Complexity: O(1)
     *
     * @param a first number
     * @param b second number
     * @return LCM of a and b
     * @throws ValidationException if inputs are invalid
     */
    public long lcm(long a, long b) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(a, b);

            // Handle negative numbers by taking absolute values
            a = Math.abs(a);
            b = Math.abs(b);

            logger.debug("Calculating LCM of {} and {}", a, b);

            // LCM(a,b) = |a*b| / GCD(a,b)
            // Check for overflow before multiplication
            if (a == 0 || b == 0) {
                return 0;
            }

            long gcd = gcd(a, b);
            long result = (a / gcd) * b; // This may overflow

            // Check for overflow
            if (result < 0) {
                throw new ArithmeticException("LCM calculation overflow");
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("LCM result: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating LCM of {} and {}: {}", a, b, e.getMessage());
            throw new ValidationException("Failed to calculate LCM: " + e.getMessage(), OPERATION_NAME, a, b);
        }
    }

    /**
     * Calculate LCM for multiple numbers.
     *
     * @param numbers array of numbers
     * @return LCM of all numbers
     * @throws ValidationException if inputs are invalid
     */
    public long lcm(long... numbers) {
        validateInputs((Object) numbers);
        if (numbers.length < 2) {
            throw ValidationException.invalidRange("numbers.length", numbers.length, 2, Integer.MAX_VALUE, OPERATION_NAME);
        }

        long result = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            result = lcm(result, numbers[i]);
        }
        return result;
    }

    // ===== EXTENDED EUCLID'S ALGORITHM =====

    /**
     * Extended Euclid's Algorithm result container.
     */
    public static class ExtendedGcdResult {
        public final long gcd;
        public final long x; // coefficient for first number
        public final long y; // coefficient for second number

        public ExtendedGcdResult(long gcd, long x, long y) {
            this.gcd = gcd;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("GCD: %d, Coefficients: (%d, %d)", gcd, x, y);
        }
    }

    /**
     * Extended Euclid's Algorithm for finding GCD and coefficients.
     *
     * Solves the equation: gcd(a,b) = a*x + b*y
     *
     * Time Complexity: O(log min(a,b))
     * Space Complexity: O(log min(a,b)) due to recursion
     *
     * @param a first number
     * @param b second number
     * @return ExtendedGcdResult containing GCD and coefficients
     * @throws ValidationException if inputs are invalid
     */
    public ExtendedGcdResult extendedGcd(long a, long b) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(a, b);

            logger.debug("Calculating Extended GCD of {} and {}", a, b);

            ExtendedGcdResult result = extendedGcdRecursive(a, b);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Extended GCD result: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating Extended GCD of {} and {}: {}", a, b, e.getMessage());
            throw new ValidationException("Failed to calculate Extended GCD: " + e.getMessage(), OPERATION_NAME, a, b);
        }
    }

    /**
     * Recursive implementation of Extended Euclid's Algorithm.
     */
    private ExtendedGcdResult extendedGcdRecursive(long a, long b) {
        if (b == 0) {
            return new ExtendedGcdResult(a, 1, 0);
        }

        ExtendedGcdResult result = extendedGcdRecursive(b, a % b);
        long x = result.y;
        long y = result.x - (a / b) * result.y;

        return new ExtendedGcdResult(result.gcd, x, y);
    }

    // ===== PRIME NUMBER OPERATIONS =====

    /**
     * Check if a number is prime using trial division.
     *
     * Time Complexity: O(sqrt(n))
     * Space Complexity: O(1)
     *
     * @param n number to check
     * @return true if prime, false otherwise
     * @throws ValidationException if input is invalid
     */
    public boolean isPrime(long n) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n);

            if (n < 2) {
                return false;
            }

            if (n == 2 || n == 3) {
                return true;
            }

            if (n % 2 == 0 || n % 3 == 0) {
                return false;
            }

            logger.debug("Checking if {} is prime", n);

            // Check divisibility by numbers of form 6k±1 up to sqrt(n)
            for (long i = 5; i * i <= n; i += 6) {
                if (n % i == 0 || n % (i + 2) == 0) {
                    return false;
                }
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            return true;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error checking if {} is prime: {}", n, e.getMessage());
            throw new ValidationException("Failed to check prime: " + e.getMessage(), OPERATION_NAME, n);
        }
    }

    /**
     * Sieve of Eratosthenes implementation for finding all primes up to a limit.
     *
     * Time Complexity: O(n log log n)
     * Space Complexity: O(n)
     *
     * @param limit upper limit (inclusive)
     * @return list of prime numbers up to limit
     * @throws ValidationException if input is invalid
     */
    public List<Long> sieveOfEratosthenes(long limit) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(limit);

            if (limit < 2) {
                return new ArrayList<>();
            }

            logger.debug("Generating primes up to {} using Sieve of Eratosthenes", limit);

            BitSet isPrime = new BitSet((int) limit + 1);
            isPrime.set(2, (int) limit + 1); // Assume all are prime initially

            for (long i = 2; i * i <= limit; i++) {
                if (isPrime.get((int) i)) {
                    // Mark multiples of i as composite
                    for (long j = i * i; j <= limit; j += i) {
                        isPrime.clear((int) j);
                    }
                }
            }

            // Collect all prime numbers
            List<Long> primes = new ArrayList<>();
            for (long i = 2; i <= limit; i++) {
                if (isPrime.get((int) i)) {
                    primes.add(i);
                }
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Found {} primes up to {}", primes.size(), limit);

            return primes;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error generating primes up to {}: {}", limit, e.getMessage());
            throw new ValidationException("Failed to generate primes: " + e.getMessage(), OPERATION_NAME, limit);
        }
    }

    /**
     * Optimized Sieve of Eratosthenes with segmented approach for large limits.
     *
     * @param limit upper limit (inclusive)
     * @param segmentSize size of each segment for memory efficiency
     * @return list of prime numbers up to limit
     * @throws ValidationException if inputs are invalid
     */
    public List<Long> sieveOfEratosthenesSegmented(long limit, int segmentSize) {
        validateInputs(limit, segmentSize);

        if (limit < 2) {
            return new ArrayList<>();
        }

        if (segmentSize <= 0) {
            throw ValidationException.negativeValue("segmentSize", segmentSize, OPERATION_NAME);
        }

        logger.debug("Generating primes up to {} using segmented sieve with segment size {}", limit, segmentSize);

        List<Long> primes = new ArrayList<>();

        // Handle small primes first
        if (limit >= 2) primes.add(2L);
        if (limit >= 3) primes.add(3L);

        // Process segments
        for (long low = 6; low <= limit; low += segmentSize) {
            long high = Math.min(low + segmentSize - 1, limit);
            BitSet segment = new BitSet(segmentSize);

            // Mark all numbers in segment as potentially prime
            for (int i = 0; i < segmentSize; i++) {
                segment.set(i);
            }

            // Use previously found primes to mark composites
            for (long prime : primes) {
                if (prime * prime > high) break;

                long start = Math.max(prime * prime, (low + prime - 1) / prime * prime);

                for (long j = start; j <= high; j += prime) {
                    if (j >= low) {
                        segment.clear((int) (j - low));
                    }
                }
            }

            // Collect primes from this segment
            for (int i = 0; i < segmentSize && low + i <= high; i++) {
                if (segment.get(i)) {
                    long num = low + i;
                    if (num % 2 != 0 && num % 3 != 0) { // Skip even numbers and multiples of 3
                        primes.add(num);
                    }
                }
            }
        }

        return primes;
    }

    // ===== MODULAR ARITHMETIC =====

    /**
     * Calculate (base^exponent) mod modulus using fast exponentiation.
     *
     * Time Complexity: O(log exponent)
     * Space Complexity: O(1)
     *
     * @param base base number
     * @param exponent exponent
     * @param modulus modulus
     * @return (base^exponent) mod modulus
     * @throws ValidationException if inputs are invalid
     */
    public long modularExponentiation(long base, long exponent, long modulus) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(base, exponent, modulus);

            if (modulus <= 0) {
                throw ValidationException.negativeValue("modulus", modulus, OPERATION_NAME);
            }

            if (exponent < 0) {
                throw ValidationException.negativeValue("exponent", exponent, OPERATION_NAME);
            }

            logger.debug("Calculating ({}^{}) mod {}", base, exponent, modulus);

            long result = 1;
            base = base % modulus;

            while (exponent > 0) {
                if ((exponent & 1) == 1) { // If exponent is odd
                    result = (result * base) % modulus;
                }
                base = (base * base) % modulus;
                exponent >>= 1; // Divide exponent by 2
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Modular exponentiation result: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating modular exponentiation: {}", e.getMessage());
            throw new ValidationException("Failed to calculate modular exponentiation: " + e.getMessage(),
                                        OPERATION_NAME, base, exponent, modulus);
        }
    }

    /**
     * Calculate modular inverse using Extended Euclid's Algorithm.
     *
     * Time Complexity: O(log modulus)
     * Space Complexity: O(log modulus) due to recursion
     *
     * @param a number to find inverse for
     * @param modulus modulus
     * @return modular inverse of a modulo modulus
     * @throws ValidationException if inputs are invalid or no inverse exists
     */
    public long modularInverse(long a, long modulus) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(a, modulus);

            if (modulus <= 0) {
                throw ValidationException.negativeValue("modulus", modulus, OPERATION_NAME);
            }

            logger.debug("Calculating modular inverse of {} mod {}", a, modulus);

            ExtendedGcdResult result = extendedGcd(a, modulus);

            if (result.gcd != 1) {
                throw new ValidationException(
                    String.format("No modular inverse exists for %d modulo %d (GCD = %d)", a, modulus, result.gcd),
                    OPERATION_NAME, a, modulus
                );
            }

            // Ensure the result is positive
            long inverse = result.x % modulus;
            if (inverse < 0) {
                inverse += modulus;
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Modular inverse result: {}", inverse);

            return inverse;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating modular inverse: {}", e.getMessage());
            throw new ValidationException("Failed to calculate modular inverse: " + e.getMessage(),
                                        OPERATION_NAME, a, modulus);
        }
    }

    // ===== FACTORIZATION =====

    /**
     * Factorize a number into its prime factors.
     *
     * Time Complexity: O(sqrt(n))
     * Space Complexity: O(number of prime factors)
     *
     * @param n number to factorize
     * @return list of prime factors with multiplicity
     * @throws ValidationException if input is invalid
     */
    public List<Long> primeFactors(long n) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n);

            if (n < 2) {
                throw ValidationException.invalidRange("n", n, 2, Long.MAX_VALUE, OPERATION_NAME);
            }

            logger.debug("Factorizing number: {}", n);

            List<Long> factors = new ArrayList<>();

            // Check for factor of 2
            while (n % 2 == 0) {
                factors.add(2L);
                n /= 2;
            }

            // Check for odd factors
            for (long i = 3; i * i <= n; i += 2) {
                while (n % i == 0) {
                    factors.add(i);
                    n /= i;
                }
            }

            // If n is a prime number greater than 2
            if (n > 2) {
                factors.add(n);
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Prime factors of {}: {}", n, factors);

            return factors;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error factorizing {}: {}", n, e.getMessage());
            throw new ValidationException("Failed to factorize: " + e.getMessage(), OPERATION_NAME, n);
        }
    }

    // ===== UTILITY METHODS =====

    /**
     * Get current memory usage in bytes.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Calculate Euler's Totient function φ(n).
     *
     * Time Complexity: O(sqrt(n))
     * Space Complexity: O(1)
     *
     * @param n input number
     * @return Euler's totient function value
     * @throws ValidationException if input is invalid
     */
    public long eulerTotient(long n) {
        validateInputs(n);

        if (n < 1) {
            throw ValidationException.invalidRange("n", n, 1, Long.MAX_VALUE, OPERATION_NAME);
        }

        if (n == 1) {
            return 1;
        }

        long result = n;

        // Check for factor of 2
        if (n % 2 == 0) {
            result -= result / 2;
            while (n % 2 == 0) {
                n /= 2;
            }
        }

        // Check for odd factors
        for (long i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                result -= result / i;
                while (n % i == 0) {
                    n /= i;
                }
            }
        }

        // If n is a prime number greater than 2
        if (n > 2) {
            result -= result / n;
        }

        return result;
    }

    /**
     * Check if two numbers are coprime (GCD = 1).
     *
     * @param a first number
     * @param b second number
     * @return true if coprime, false otherwise
     */
    public boolean areCoprime(long a, long b) {
        return gcd(a, b) == 1;
    }

    /**
     * Find the smallest prime factor of a number.
     *
     * @param n input number
     * @return smallest prime factor
     * @throws ValidationException if input is invalid
     */
    public long smallestPrimeFactor(long n) {
        validateInputs(n);

        if (n < 2) {
            throw ValidationException.invalidRange("n", n, 2, Long.MAX_VALUE, OPERATION_NAME);
        }

        if (n % 2 == 0) {
            return 2;
        }

        for (long i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return i;
            }
        }

        return n; // n is prime
    }
}

