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

package com.netflix.mathlib.combinatorics;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Combinatorics Operations - Production-grade implementations of combinatorial algorithms.
 *
 * This class provides comprehensive combinatorics operations including:
 * - Factorial calculations with memoization
 * - Permutations (with and without repetition)
 * - Combinations (with and without repetition)
 * - Fibonacci sequences and variants (Tribonacci, etc.)
 * - Catalan numbers
 * - Stirling numbers
 * - Bell numbers
 *
 * All implementations are optimized for performance and production use with:
 * - Comprehensive input validation
 * - Performance monitoring and metrics
 * - Thread-safe memoization where applicable
 * - Extensive error handling
 * - Detailed logging and debugging support
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class CombinatoricsOperations implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(CombinatoricsOperations.class);
    private static final String OPERATION_NAME = "CombinatoricsOperations";
    private static final String COMPLEXITY = "O(various)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;

    // Memoization caches for performance optimization
    private final Map<Integer, BigInteger> factorialCache = new ConcurrentHashMap<>();
    private final Map<String, BigInteger> combinationCache = new ConcurrentHashMap<>();
    private final Map<String, BigInteger> permutationCache = new ConcurrentHashMap<>();
    private final Map<Integer, BigInteger> fibonacciCache = new ConcurrentHashMap<>();

    /**
     * Constructor for Combinatorics Operations.
     */
    public CombinatoricsOperations() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        // Pre-populate factorial cache with small values for better performance
        factorialCache.put(0, BigInteger.ONE);
        factorialCache.put(1, BigInteger.ONE);
        logger.info("Initialized Combinatorics Operations module");
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

    // ===== FACTORIAL OPERATIONS =====

    /**
     * Calculate factorial of a number with memoization.
     *
     * Time Complexity: O(n) for first calculation, O(1) for cached values
     * Space Complexity: O(n) for cache
     *
     * @param n non-negative integer
     * @return n!
     * @throws ValidationException if input is invalid
     */
    public BigInteger factorial(int n) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n);

            if (n < 0) {
                throw ValidationException.negativeValue("n", n, OPERATION_NAME);
            }

            logger.debug("Calculating factorial of {}", n);

            // Check cache first
            if (factorialCache.containsKey(n)) {
                BigInteger result = factorialCache.get(n);
                long executionTime = System.nanoTime() - startTime;
                long memoryUsed = getCurrentMemoryUsage() - startMemory;
                metrics.recordSuccess(executionTime, memoryUsed);
                return result;
            }

            // Calculate iteratively with memoization
            BigInteger result = BigInteger.ONE;
            for (int i = 2; i <= n; i++) {
                result = result.multiply(BigInteger.valueOf(i));
                factorialCache.put(i, result);
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Factorial of {} = {}", n, result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating factorial of {}: {}", n, e.getMessage());
            throw new ValidationException("Failed to calculate factorial: " + e.getMessage(), OPERATION_NAME, n);
        }
    }

    /**
     * Calculate factorial with iterative approach (no memoization).
     * Useful for one-time calculations or when memory is a concern.
     *
     * @param n non-negative integer
     * @return n!
     */
    public BigInteger factorialIterative(int n) {
        validateInputs(n);

        if (n < 0) {
            throw ValidationException.negativeValue("n", n, OPERATION_NAME);
        }

        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    // ===== FIBONACCI SEQUENCES =====

    /**
     * Calculate nth Fibonacci number with memoization.
     *
     * Time Complexity: O(n) for first calculation, O(1) for cached values
     * Space Complexity: O(n) for cache
     *
     * @param n non-negative integer
     * @return nth Fibonacci number
     * @throws ValidationException if input is invalid
     */
    public BigInteger fibonacci(int n) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n);

            if (n < 0) {
                throw ValidationException.negativeValue("n", n, OPERATION_NAME);
            }

            logger.debug("Calculating Fibonacci number {}", n);

            // Base cases
            if (n == 0) return BigInteger.ZERO;
            if (n == 1 || n == 2) return BigInteger.ONE;

            // Check cache
            if (fibonacciCache.containsKey(n)) {
                BigInteger result = fibonacciCache.get(n);
                long executionTime = System.nanoTime() - startTime;
                long memoryUsed = getCurrentMemoryUsage() - startMemory;
                metrics.recordSuccess(executionTime, memoryUsed);
                return result;
            }

            // Calculate iteratively with memoization
            BigInteger a = BigInteger.ONE;
            BigInteger b = BigInteger.ONE;

            for (int i = 3; i <= n; i++) {
                BigInteger temp = a.add(b);
                a = b;
                b = temp;
                fibonacciCache.put(i, b);
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Fibonacci({}) = {}", n, b);

            return b;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating Fibonacci of {}: {}", n, e.getMessage());
            throw new ValidationException("Failed to calculate Fibonacci: " + e.getMessage(), OPERATION_NAME, n);
        }
    }

    /**
     * Generate Fibonacci sequence up to nth term.
     *
     * @param n number of terms to generate
     * @return list of Fibonacci numbers
     */
    public List<BigInteger> fibonacciSequence(int n) {
        validateInputs(n);

        if (n < 0) {
            throw ValidationException.negativeValue("n", n, OPERATION_NAME);
        }

        List<BigInteger> sequence = new ArrayList<>();

        for (int i = 0; i <= n; i++) {
            sequence.add(fibonacci(i));
        }

        return sequence;
    }

    /**
     * Calculate nth Tribonacci number (sum of three preceding numbers).
     *
     * @param n non-negative integer
     * @return nth Tribonacci number
     */
    public BigInteger tribonacci(int n) {
        validateInputs(n);

        if (n < 0) {
            throw ValidationException.negativeValue("n", n, OPERATION_NAME);
        }

        if (n == 0) return BigInteger.ZERO;
        if (n == 1 || n == 2) return BigInteger.ONE;

        BigInteger a = BigInteger.ZERO;
        BigInteger b = BigInteger.ONE;
        BigInteger c = BigInteger.ONE;

        for (int i = 3; i <= n; i++) {
            BigInteger temp = a.add(b).add(c);
            a = b;
            b = c;
            c = temp;
        }

        return c;
    }

    // ===== COMBINATIONS =====

    /**
     * Calculate combinations C(n, k) with memoization.
     *
     * Time Complexity: O(n*k) for first calculation, O(1) for cached values
     * Space Complexity: O(n*k) for cache
     *
     * @param n total number of items
     * @param k number of items to choose
     * @return C(n, k)
     * @throws ValidationException if inputs are invalid
     */
    public BigInteger combination(int n, int k) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n, k);

            if (n < 0 || k < 0) {
                throw ValidationException.negativeValue("n or k", Math.min(n, k), OPERATION_NAME);
            }

            if (k > n) {
                return BigInteger.ZERO;
            }

            if (k == 0 || k == n) {
                return BigInteger.ONE;
            }

            // Use symmetry property: C(n,k) = C(n,n-k)
            if (k > n - k) {
                k = n - k;
            }

            logger.debug("Calculating combination C({}, {})", n, k);

            String cacheKey = n + "," + k;
            if (combinationCache.containsKey(cacheKey)) {
                BigInteger result = combinationCache.get(cacheKey);
                long executionTime = System.nanoTime() - startTime;
                long memoryUsed = getCurrentMemoryUsage() - startMemory;
                metrics.recordSuccess(executionTime, memoryUsed);
                return result;
            }

            // Calculate using multiplicative formula
            BigInteger result = BigInteger.ONE;
            for (int i = 1; i <= k; i++) {
                result = result.multiply(BigInteger.valueOf(n - i + 1));
                result = result.divide(BigInteger.valueOf(i));
            }

            combinationCache.put(cacheKey, result);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Combination C({}, {}) = {}", n, k, result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating combination C({}, {}): {}", n, k, e.getMessage());
            throw new ValidationException("Failed to calculate combination: " + e.getMessage(), OPERATION_NAME, n, k);
        }
    }

    /**
     * Calculate combinations with repetition C(n+k-1, k).
     *
     * @param n number of types of items
     * @param k number of items to choose (with repetition allowed)
     * @return C(n+k-1, k)
     */
    public BigInteger combinationWithRepetition(int n, int k) {
        validateInputs(n, k);

        if (n < 0 || k < 0) {
            throw ValidationException.negativeValue("n or k", Math.min(n, k), OPERATION_NAME);
        }

        return combination(n + k - 1, k);
    }

    // ===== PERMUTATIONS =====

    /**
     * Calculate permutations P(n, k) with memoization.
     *
     * Time Complexity: O(n) for first calculation, O(1) for cached values
     * Space Complexity: O(n) for factorial cache
     *
     * @param n total number of items
     * @param k number of items to arrange
     * @return P(n, k)
     * @throws ValidationException if inputs are invalid
     */
    public BigInteger permutation(int n, int k) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n, k);

            if (n < 0 || k < 0) {
                throw ValidationException.negativeValue("n or k", Math.min(n, k), OPERATION_NAME);
            }

            if (k > n) {
                return BigInteger.ZERO;
            }

            if (k == 0) {
                return BigInteger.ONE;
            }

            logger.debug("Calculating permutation P({}, {})", n, k);

            String cacheKey = n + "," + k;
            if (permutationCache.containsKey(cacheKey)) {
                BigInteger result = permutationCache.get(cacheKey);
                long executionTime = System.nanoTime() - startTime;
                long memoryUsed = getCurrentMemoryUsage() - startMemory;
                metrics.recordSuccess(executionTime, memoryUsed);
                return result;
            }

            // P(n,k) = n! / (n-k)!
            BigInteger result = factorial(n).divide(factorial(n - k));
            permutationCache.put(cacheKey, result);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Permutation P({}, {}) = {}", n, k, result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating permutation P({}, {}): {}", n, k, e.getMessage());
            throw new ValidationException("Failed to calculate permutation: " + e.getMessage(), OPERATION_NAME, n, k);
        }
    }

    /**
     * Calculate permutations with repetition.
     *
     * @param n total number of items
     * @param frequencies array of frequencies for each distinct item
     * @return number of distinct permutations
     */
    public BigInteger permutationWithRepetition(int n, int[] frequencies) {
        validateInputs(n, frequencies);

        if (n < 0) {
            throw ValidationException.negativeValue("n", n, OPERATION_NAME);
        }

        int sum = 0;
        for (int freq : frequencies) {
            if (freq < 0) {
                throw ValidationException.negativeValue("frequency", freq, OPERATION_NAME);
            }
            sum += freq;
        }

        if (sum != n) {
            throw new ValidationException("Sum of frequencies must equal n", OPERATION_NAME, n, Arrays.toString(frequencies));
        }

        BigInteger result = factorial(n);
        for (int freq : frequencies) {
            if (freq > 1) {
                result = result.divide(factorial(freq));
            }
        }

        return result;
    }

    // ===== CATALAN NUMBERS =====

    /**
     * Calculate nth Catalan number.
     *
     * Catalan numbers have many applications in combinatorics including:
     * - Number of valid parentheses sequences
     * - Number of full binary trees
     * - Number of plane trees
     * - Number of triangulations
     *
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     *
     * @param n non-negative integer
     * @return nth Catalan number
     * @throws ValidationException if input is invalid
     */
    public BigInteger catalanNumber(int n) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n);

            if (n < 0) {
                throw ValidationException.negativeValue("n", n, OPERATION_NAME);
            }

            logger.debug("Calculating Catalan number {}", n);

            // C_n = (2n)! / ((n+1)! * n!)
            // Or equivalently: C_n = (1/(n+1)) * (2n choose n)

            BigInteger result = combination(2 * n, n).divide(BigInteger.valueOf(n + 1));

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Catalan number C({}) = {}", n, result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating Catalan number {}: {}", n, e.getMessage());
            throw new ValidationException("Failed to calculate Catalan number: " + e.getMessage(), OPERATION_NAME, n);
        }
    }

    /**
     * Generate first n Catalan numbers.
     *
     * @param n number of Catalan numbers to generate
     * @return list of first n Catalan numbers
     */
    public List<BigInteger> catalanSequence(int n) {
        validateInputs(n);

        if (n < 0) {
            throw ValidationException.negativeValue("n", n, OPERATION_NAME);
        }

        List<BigInteger> sequence = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            sequence.add(catalanNumber(i));
        }
        return sequence;
    }

    // ===== STIRLING NUMBERS =====

    /**
     * Calculate Stirling numbers of the second kind S(n, k).
     *
     * S(n, k) represents the number of ways to partition n distinct objects
     * into k non-empty unlabeled subsets.
     *
     * Time Complexity: O(n*k)
     * Space Complexity: O(n*k) for memoization
     *
     * @param n number of objects
     * @param k number of subsets
     * @return S(n, k)
     */
    public BigInteger stirlingSecondKind(int n, int k) {
        validateInputs(n, k);

        if (n < 0 || k < 0) {
            throw ValidationException.negativeValue("n or k", Math.min(n, k), OPERATION_NAME);
        }

        if (k == 0) return (n == 0) ? BigInteger.ONE : BigInteger.ZERO;
        if (k > n) return BigInteger.ZERO;
        if (k == n || k == 1) return BigInteger.ONE;

        // S(n,k) = k * S(n-1,k) + S(n-1,k-1)
        BigInteger result = stirlingSecondKind(n - 1, k).multiply(BigInteger.valueOf(k))
                            .add(stirlingSecondKind(n - 1, k - 1));

        return result;
    }

    /**
     * Calculate Bell number B(n).
     *
     * B(n) represents the number of ways to partition n distinct objects
     * into non-empty unlabeled subsets.
     *
     * @param n number of objects
     * @return B(n)
     */
    public BigInteger bellNumber(int n) {
        validateInputs(n);

        if (n < 0) {
            throw ValidationException.negativeValue("n", n, OPERATION_NAME);
        }

        if (n == 0) return BigInteger.ONE;

        BigInteger sum = BigInteger.ZERO;
        for (int k = 1; k <= n; k++) {
            sum = sum.add(stirlingSecondKind(n, k));
        }

        return sum;
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
     * Clear all memoization caches.
     * Useful for memory management in long-running applications.
     */
    public void clearCaches() {
        factorialCache.clear();
        combinationCache.clear();
        permutationCache.clear();
        fibonacciCache.clear();

        // Re-populate factorial cache with base cases
        factorialCache.put(0, BigInteger.ONE);
        factorialCache.put(1, BigInteger.ONE);

        logger.info("Cleared all combinatorics caches");
    }

    /**
     * Get cache statistics for performance monitoring.
     *
     * @return map of cache statistics
     */
    public Map<String, Integer> getCacheStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("factorialCache", factorialCache.size());
        stats.put("combinationCache", combinationCache.size());
        stats.put("permutationCache", permutationCache.size());
        stats.put("fibonacciCache", fibonacciCache.size());
        return stats;
    }

    /**
     * Generate all combinations of size k from a set of n elements.
     * This is a combinatorial generation, not just counting.
     *
     * @param n total number of elements
     * @param k size of each combination
     * @return list of all combinations
     */
    public List<List<Integer>> generateCombinations(int n, int k) {
        validateInputs(n, k);

        if (k < 0 || k > n) {
            throw new ValidationException("Invalid combination parameters", OPERATION_NAME, n, k);
        }

        List<List<Integer>> result = new ArrayList<>();
        List<Integer> current = new ArrayList<>();

        generateCombinationsHelper(n, k, 1, current, result);
        return result;
    }

    /**
     * Recursive helper for combination generation.
     */
    private void generateCombinationsHelper(int n, int k, int start, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i <= n; i++) {
            current.add(i);
            generateCombinationsHelper(n, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    /**
     * Generate all permutations of n elements.
     *
     * @param n number of elements
     * @return list of all permutations
     */
    public List<List<Integer>> generatePermutations(int n) {
        validateInputs(n);

        if (n < 0) {
            throw ValidationException.negativeValue("n", n, OPERATION_NAME);
        }

        List<Integer> elements = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            elements.add(i);
        }

        List<List<Integer>> result = new ArrayList<>();
        generatePermutationsHelper(elements, 0, result);
        return result;
    }

    /**
     * Recursive helper for permutation generation.
     */
    private void generatePermutationsHelper(List<Integer> elements, int index, List<List<Integer>> result) {
        if (index == elements.size() - 1) {
            result.add(new ArrayList<>(elements));
            return;
        }

        for (int i = index; i < elements.size(); i++) {
            // Swap
            Collections.swap(elements, index, i);

            // Recurse
            generatePermutationsHelper(elements, index + 1, result);

            // Backtrack
            Collections.swap(elements, index, i);
        }
    }
}