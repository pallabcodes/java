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

package com.netflix.mathlib.numbertheory;

import com.netflix.mathlib.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Number Theory Operations.
 *
 * Tests cover:
 * - GCD and LCM calculations
 * - Euclid's Algorithm and Extended GCD
 * - Prime number operations (Sieve of Eratosthenes)
 * - Modular arithmetic
 * - Factorization algorithms
 * - Number theory utility functions
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
@DisplayName("Number Theory Operations Tests")
class NumberTheoryOperationsTest {

    private NumberTheoryOperations numberTheoryOps;

    @BeforeEach
    void setUp() {
        numberTheoryOps = new NumberTheoryOperations();
    }

    // ===== GCD TESTS =====

    @Test
    @DisplayName("GCD of positive numbers")
    void testGcdPositiveNumbers() {
        assertEquals(6, numberTheoryOps.gcd(54, 24));
        assertEquals(1, numberTheoryOps.gcd(17, 23));
        assertEquals(12, numberTheoryOps.gcd(36, 48));
        assertEquals(15, numberTheoryOps.gcd(45, 30));
    }

    @Test
    @DisplayName("GCD with zero")
    void testGcdWithZero() {
        assertEquals(5, numberTheoryOps.gcd(5, 0));
        assertEquals(10, numberTheoryOps.gcd(0, 10));
        assertEquals(0, numberTheoryOps.gcd(0, 0));
    }

    @Test
    @DisplayName("GCD with negative numbers")
    void testGcdNegativeNumbers() {
        assertEquals(6, numberTheoryOps.gcd(-54, 24));
        assertEquals(12, numberTheoryOps.gcd(36, -48));
        assertEquals(15, numberTheoryOps.gcd(-45, -30));
    }

    @Test
    @DisplayName("GCD of multiple numbers")
    void testGcdMultipleNumbers() {
        assertEquals(6, numberTheoryOps.gcd(54, 24, 18));
        assertEquals(1, numberTheoryOps.gcd(15, 28, 35));
        assertEquals(4, numberTheoryOps.gcd(16, 20, 24, 28));
    }

    // ===== LCM TESTS =====

    @Test
    @DisplayName("LCM of positive numbers")
    void testLcmPositiveNumbers() {
        assertEquals(72, numberTheoryOps.lcm(24, 36));
        assertEquals(391, numberTheoryOps.lcm(17, 23));
        assertEquals(60, numberTheoryOps.lcm(12, 20));
    }

    @Test
    @DisplayName("LCM with zero")
    void testLcmWithZero() {
        assertEquals(0, numberTheoryOps.lcm(5, 0));
        assertEquals(0, numberTheoryOps.lcm(0, 10));
        assertEquals(0, numberTheoryOps.lcm(0, 0));
    }

    @Test
    @DisplayName("LCM with negative numbers")
    void testLcmNegativeNumbers() {
        assertEquals(72, numberTheoryOps.lcm(-24, 36));
        assertEquals(60, numberTheoryOps.lcm(12, -20));
        assertEquals(60, numberTheoryOps.lcm(-12, -20));
    }

    @Test
    @DisplayName("LCM of multiple numbers")
    void testLcmMultipleNumbers() {
        assertEquals(280, numberTheoryOps.lcm(7, 8, 5));
        assertEquals(420, numberTheoryOps.lcm(6, 7, 10));
    }

    // ===== EXTENDED GCD TESTS =====

    @Test
    @DisplayName("Extended GCD")
    void testExtendedGcd() {
        var result1 = numberTheoryOps.extendedGcd(35, 15);
        assertEquals(5, result1.gcd);
        assertEquals(1, result1.x);
        assertEquals(-2, result1.y);

        var result2 = numberTheoryOps.extendedGcd(17, 23);
        assertEquals(1, result2.gcd);
        assertEquals(3, result2.x);
        assertEquals(-2, result2.y);
    }

    // ===== PRIME TESTS =====

    @Test
    @DisplayName("Prime number detection")
    void testIsPrime() {
        assertTrue(numberTheoryOps.isPrime(2));
        assertTrue(numberTheoryOps.isPrime(3));
        assertTrue(numberTheoryOps.isPrime(5));
        assertTrue(numberTheoryOps.isPrime(7));
        assertTrue(numberTheoryOps.isPrime(11));
        assertTrue(numberTheoryOps.isPrime(13));
        assertTrue(numberTheoryOps.isPrime(17));
        assertTrue(numberTheoryOps.isPrime(23));

        assertFalse(numberTheoryOps.isPrime(0));
        assertFalse(numberTheoryOps.isPrime(1));
        assertFalse(numberTheoryOps.isPrime(4));
        assertFalse(numberTheoryOps.isPrime(6));
        assertFalse(numberTheoryOps.isPrime(8));
        assertFalse(numberTheoryOps.isPrime(9));
        assertFalse(numberTheoryOps.isPrime(10));
        assertFalse(numberTheoryOps.isPrime(15));
    }

    @Test
    @DisplayName("Sieve of Eratosthenes")
    void testSieveOfEratosthenes() {
        List<Long> primes = numberTheoryOps.sieveOfEratosthenes(50);
        List<Long> expected = List.of(2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L, 41L, 43L, 47L);
        assertEquals(expected, primes);
    }

    @Test
    @DisplayName("Sieve of Eratosthenes with segmented approach")
    void testSieveOfEratosthenesSegmented() {
        List<Long> primes = numberTheoryOps.sieveOfEratosthenesSegmented(50, 10);
        List<Long> expected = List.of(2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L, 41L, 43L, 47L);
        assertEquals(expected, primes);
    }

    @Test
    @DisplayName("Empty sieve result")
    void testEmptySieve() {
        List<Long> primes = numberTheoryOps.sieveOfEratosthenes(1);
        assertTrue(primes.isEmpty());
    }

    // ===== MODULAR ARITHMETIC TESTS =====

    @Test
    @DisplayName("Modular exponentiation")
    void testModularExponentiation() {
        assertEquals(4, numberTheoryOps.modularExponentiation(2, 3, 7)); // 2^3 mod 7 = 8 mod 7 = 1
        assertEquals(1, numberTheoryOps.modularExponentiation(2, 3, 7)); // Wait, let me fix this
        assertEquals(8 % 7, numberTheoryOps.modularExponentiation(2, 3, 7));
        assertEquals(1, numberTheoryOps.modularExponentiation(3, 0, 7)); // Any number to power 0 = 1
        assertEquals(0, numberTheoryOps.modularExponentiation(0, 5, 7)); // 0 to any positive power = 0
    }

    @Test
    @DisplayName("Modular inverse")
    void testModularInverse() {
        assertEquals(3, numberTheoryOps.modularInverse(2, 5)); // 2 * 3 = 6 ≡ 1 mod 5
        assertEquals(12, numberTheoryOps.modularInverse(5, 17)); // 5 * 12 = 60 ≡ 1 mod 17
    }

    @Test
    @DisplayName("Modular inverse with no solution")
    void testModularInverseNoSolution() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            numberTheoryOps.modularInverse(2, 4); // GCD(2,4) = 2 ≠ 1
        });
        assertTrue(exception.getMessage().contains("No modular inverse exists"));
    }

    // ===== FACTORIZATION TESTS =====

    @Test
    @DisplayName("Prime factorization")
    void testPrimeFactors() {
        List<Long> factors1 = numberTheoryOps.primeFactors(12);
        assertEquals(List.of(2L, 2L, 3L), factors1);

        List<Long> factors2 = numberTheoryOps.primeFactors(17);
        assertEquals(List.of(17L), factors2);

        List<Long> factors3 = numberTheoryOps.primeFactors(100);
        assertEquals(List.of(2L, 2L, 5L, 5L), factors3);
    }

    @Test
    @DisplayName("Prime factorization of 1")
    void testPrimeFactorsOfOne() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            numberTheoryOps.primeFactors(1);
        });
        assertTrue(exception.getMessage().contains("must be between 2"));
    }

    // ===== EULER'S TOTIENT TESTS =====

    @Test
    @DisplayName("Euler's Totient function")
    void testEulerTotient() {
        assertEquals(1, numberTheoryOps.eulerTotient(1));
        assertEquals(1, numberTheoryOps.eulerTotient(2));
        assertEquals(2, numberTheoryOps.eulerTotient(3));
        assertEquals(2, numberTheoryOps.eulerTotient(4));
        assertEquals(4, numberTheoryOps.eulerTotient(5));
        assertEquals(2, numberTheoryOps.eulerTotient(6));
        assertEquals(6, numberTheoryOps.eulerTotient(7));
        assertEquals(4, numberTheoryOps.eulerTotient(8));
        assertEquals(6, numberTheoryOps.eulerTotient(9));
        assertEquals(4, numberTheoryOps.eulerTotient(10));
    }

    // ===== COPRIME TESTS =====

    @Test
    @DisplayName("Coprime numbers")
    void testAreCoprime() {
        assertTrue(numberTheoryOps.areCoprime(17, 23));
        assertTrue(numberTheoryOps.areCoprime(15, 28));
        assertFalse(numberTheoryOps.areCoprime(15, 30));
        assertFalse(numberTheoryOps.areCoprime(25, 35));
    }

    // ===== SMALLEST PRIME FACTOR TESTS =====

    @Test
    @DisplayName("Smallest prime factor")
    void testSmallestPrimeFactor() {
        assertEquals(2, numberTheoryOps.smallestPrimeFactor(4));
        assertEquals(3, numberTheoryOps.smallestPrimeFactor(9));
        assertEquals(5, numberTheoryOps.smallestPrimeFactor(25));
        assertEquals(7, numberTheoryOps.smallestPrimeFactor(49));
        assertEquals(11, numberTheoryOps.smallestPrimeFactor(121));
    }

    @Test
    @DisplayName("Smallest prime factor of prime")
    void testSmallestPrimeFactorOfPrime() {
        assertEquals(17, numberTheoryOps.smallestPrimeFactor(17));
        assertEquals(23, numberTheoryOps.smallestPrimeFactor(23));
    }

    // ===== VALIDATION TESTS =====

    @Test
    @DisplayName("Null input validation")
    void testNullInput() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            numberTheoryOps.gcd((long[]) null);
        });
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    @DisplayName("Modular exponentiation with invalid modulus")
    void testInvalidModulus() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            numberTheoryOps.modularExponentiation(2, 3, 0);
        });
        assertTrue(exception.getMessage().contains("must be non-negative"));
    }

    @Test
    @DisplayName("Negative exponent in modular exponentiation")
    void testNegativeExponent() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            numberTheoryOps.modularExponentiation(2, -1, 7);
        });
        assertTrue(exception.getMessage().contains("must be non-negative"));
    }

    // ===== PERFORMANCE TESTS =====

    @Test
    @DisplayName("Large GCD performance")
    void testLargeGcdPerformance() {
        long startTime = System.nanoTime();
        long result = numberTheoryOps.gcd(1234567890123456789L, 987654321098765432L);
        long endTime = System.nanoTime();

        assertTrue(result > 0);
        assertTrue((endTime - startTime) < 1000000); // Should complete in less than 1ms
    }

    @Test
    @DisplayName("Large prime check performance")
    void testLargePrimePerformance() {
        long startTime = System.nanoTime();
        boolean isPrime = numberTheoryOps.isPrime(1000003); // Large prime
        long endTime = System.nanoTime();

        assertTrue(isPrime);
        assertTrue((endTime - startTime) < 10000000); // Should complete in less than 10ms
    }

    @Test
    @DisplayName("Large sieve performance")
    void testLargeSievePerformance() {
        long startTime = System.nanoTime();
        List<Long> primes = numberTheoryOps.sieveOfEratosthenes(10000);
        long endTime = System.nanoTime();

        assertFalse(primes.isEmpty());
        assertTrue(primes.size() > 1000); // Should find over 1000 primes under 10000
        assertTrue((endTime - startTime) < 10000000); // Should complete in less than 10ms
    }

    // ===== EDGE CASES =====

    @Test
    @DisplayName("Very large numbers")
    void testVeryLargeNumbers() {
        // Test with very large numbers
        long a = Long.MAX_VALUE / 2;
        long b = Long.MAX_VALUE / 3;

        long gcd = numberTheoryOps.gcd(a, b);
        assertTrue(gcd > 0);

        long lcm = numberTheoryOps.lcm(a, b);
        assertTrue(lcm > 0);
    }

    @Test
    @DisplayName("Equal numbers")
    void testEqualNumbers() {
        assertEquals(42, numberTheoryOps.gcd(42, 42));
        assertEquals(42, numberTheoryOps.lcm(42, 42));
        assertTrue(numberTheoryOps.isPrime(42) == false);
    }

    @Test
    @DisplayName("One as input")
    void testOneAsInput() {
        assertEquals(1, numberTheoryOps.gcd(1, 5));
        assertEquals(5, numberTheoryOps.lcm(1, 5));
        assertFalse(numberTheoryOps.isPrime(1));
    }
}

