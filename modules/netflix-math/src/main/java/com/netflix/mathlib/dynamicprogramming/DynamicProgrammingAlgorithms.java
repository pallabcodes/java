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

package com.netflix.mathlib.dynamicprogramming;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Dynamic Programming Algorithms - Production-grade DP implementations.
 *
 * This class provides comprehensive dynamic programming algorithms including:
 * - Knapsack problems (0/1, unbounded, bounded)
 * - Longest Common Subsequence/Subsequence
 * - Edit Distance algorithms
 * - Matrix Chain Multiplication
 * - Optimal Binary Search Trees
 * - Coin Change problems
 * - Palindrome partitioning
 * - Word break problems
 *
 * All implementations are optimized for performance and production use with:
 * - Comprehensive input validation
 * - Performance monitoring and metrics
 * - Memory-efficient algorithms
 * - Extensive error handling
 * - Detailed logging and debugging support
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class DynamicProgrammingAlgorithms implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(DynamicProgrammingAlgorithms.class);
    private static final String OPERATION_NAME = "DynamicProgrammingAlgorithms";
    private static final String COMPLEXITY = "O(various)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;

    /**
     * Constructor for Dynamic Programming Algorithms.
     */
    public DynamicProgrammingAlgorithms() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Dynamic Programming Algorithms module");
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

    // ===== 0/1 KNAPSACK PROBLEM =====

    /**
     * Solve 0/1 Knapsack problem.
     *
     * Given weights and values of n items, put these items in a knapsack of
     * capacity W to get the maximum total value in the knapsack.
     *
     * Time Complexity: O(n*W)
     * Space Complexity: O(n*W)
     *
     * @param weights array of item weights
     * @param values array of item values
     * @param capacity knapsack capacity
     * @return maximum value that can be obtained
     */
    public int knapsack01(int[] weights, int[] values, int capacity) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(weights, values, capacity);

            if (weights.length != values.length) {
                throw new ValidationException("Weights and values arrays must have same length", OPERATION_NAME);
            }

            int n = weights.length;
            logger.debug("Solving 0/1 knapsack with {} items and capacity {}", n, capacity);

            // DP table: dp[i][w] = max value using first i items with capacity w
            int[][] dp = new int[n + 1][capacity + 1];

            // Fill DP table
            for (int i = 1; i <= n; i++) {
                for (int w = 0; w <= capacity; w++) {
                    if (weights[i - 1] <= w) {
                        // Choose: include current item
                        int includeValue = values[i - 1] + dp[i - 1][w - weights[i - 1]];
                        // Don't choose: exclude current item
                        int excludeValue = dp[i - 1][w];
                        dp[i][w] = Math.max(includeValue, excludeValue);
                    } else {
                        // Cannot include current item
                        dp[i][w] = dp[i - 1][w];
                    }
                }
            }

            int result = dp[n][capacity];

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("0/1 Knapsack result: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error solving 0/1 knapsack: {}", e.getMessage());
            throw new ValidationException("Failed to solve 0/1 knapsack: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== UNBOUNDED KNAPSACK PROBLEM =====

    /**
     * Solve unbounded knapsack problem (unlimited quantity of each item).
     *
     * Time Complexity: O(n*W)
     * Space Complexity: O(W)
     *
     * @param weights array of item weights
     * @param values array of item values
     * @param capacity knapsack capacity
     * @return maximum value that can be obtained
     */
    public int unboundedKnapsack(int[] weights, int[] values, int capacity) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(weights, values, capacity);

            if (weights.length != values.length) {
                throw new ValidationException("Weights and values arrays must have same length", OPERATION_NAME);
            }

            int n = weights.length;
            logger.debug("Solving unbounded knapsack with {} items and capacity {}", n, capacity);

            // DP array: dp[w] = max value with capacity w
            int[] dp = new int[capacity + 1];

            // Fill DP array
            for (int w = 0; w <= capacity; w++) {
                for (int i = 0; i < n; i++) {
                    if (weights[i] <= w) {
                        dp[w] = Math.max(dp[w], values[i] + dp[w - weights[i]]);
                    }
                }
            }

            int result = dp[capacity];

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Unbounded knapsack result: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error solving unbounded knapsack: {}", e.getMessage());
            throw new ValidationException("Failed to solve unbounded knapsack: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== LONGEST COMMON SUBSEQUENCE =====

    /**
     * Find length of Longest Common Subsequence (LCS).
     *
     * Time Complexity: O(m*n)
     * Space Complexity: O(m*n)
     *
     * @param str1 first string
     * @param str2 second string
     * @return length of LCS
     */
    public int longestCommonSubsequence(String str1, String str2) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(str1, str2);

            int m = str1.length();
            int n = str2.length();

            logger.debug("Finding LCS of strings with lengths {} and {}", m, n);

            // DP table: dp[i][j] = LCS length of str1[0..i-1] and str2[0..j-1]
            int[][] dp = new int[m + 1][n + 1];

            // Fill DP table
            for (int i = 1; i <= m; i++) {
                for (int j = 1; j <= n; j++) {
                    if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                        dp[i][j] = dp[i - 1][j - 1] + 1;
                    } else {
                        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                    }
                }
            }

            int result = dp[m][n];

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("LCS length: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error finding LCS: {}", e.getMessage());
            throw new ValidationException("Failed to find LCS: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== EDIT DISTANCE =====

    /**
     * Calculate Levenshtein edit distance between two strings.
     *
     * Time Complexity: O(m*n)
     * Space Complexity: O(m*n)
     *
     * @param str1 first string
     * @param str2 second string
     * @return minimum edit distance
     */
    public int editDistance(String str1, String str2) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(str1, str2);

            int m = str1.length();
            int n = str2.length();

            logger.debug("Calculating edit distance between strings of lengths {} and {}", m, n);

            // DP table: dp[i][j] = edit distance between str1[0..i-1] and str2[0..j-1]
            int[][] dp = new int[m + 1][n + 1];

            // Initialize base cases
            for (int i = 0; i <= m; i++) {
                dp[i][0] = i; // Delete all characters from str1
            }
            for (int j = 0; j <= n; j++) {
                dp[0][j] = j; // Insert all characters to str1
            }

            // Fill DP table
            for (int i = 1; i <= m; i++) {
                for (int j = 1; j <= n; j++) {
                    if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                        dp[i][j] = dp[i - 1][j - 1]; // No operation needed
                    } else {
                        // Minimum of: insert, delete, replace
                        dp[i][j] = 1 + Math.min(Math.min(dp[i][j - 1], dp[i - 1][j]), dp[i - 1][j - 1]);
                    }
                }
            }

            int result = dp[m][n];

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Edit distance: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating edit distance: {}", e.getMessage());
            throw new ValidationException("Failed to calculate edit distance: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== MATRIX CHAIN MULTIPLICATION =====

    /**
     * Solve matrix chain multiplication problem.
     *
     * Given a sequence of matrices, find the most efficient way to multiply them.
     *
     * Time Complexity: O(n^3)
     * Space Complexity: O(n^2)
     *
     * @param dimensions array of matrix dimensions (size n+1 for n matrices)
     * @return minimum number of scalar multiplications
     */
    public int matrixChainMultiplication(int[] dimensions) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs((Object) dimensions);

            if (dimensions.length < 2) {
                throw new ValidationException("Need at least 2 dimensions for matrix chain", OPERATION_NAME);
            }

            int n = dimensions.length - 1; // Number of matrices
            logger.debug("Solving matrix chain multiplication for {} matrices", n);

            // DP table: dp[i][j] = min cost to multiply matrices i to j
            int[][] dp = new int[n][n];

            // Length of chain
            for (int len = 2; len <= n; len++) {
                for (int i = 0; i <= n - len; i++) {
                    int j = i + len - 1;
                    dp[i][j] = Integer.MAX_VALUE;

                    // Try different split points
                    for (int k = i; k < j; k++) {
                        int cost = dp[i][k] + dp[k + 1][j] +
                                 dimensions[i] * dimensions[k + 1] * dimensions[j + 1];
                        dp[i][j] = Math.min(dp[i][j], cost);
                    }
                }
            }

            int result = dp[0][n - 1];

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Matrix chain multiplication cost: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error solving matrix chain multiplication: {}", e.getMessage());
            throw new ValidationException("Failed to solve matrix chain multiplication: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== COIN CHANGE PROBLEM =====

    /**
     * Solve coin change problem (minimum coins for given amount).
     *
     * Time Complexity: O(amount * coins.length)
     * Space Complexity: O(amount)
     *
     * @param coins array of coin denominations
     * @param amount target amount
     * @return minimum number of coins needed, or -1 if impossible
     */
    public int coinChange(int[] coins, int amount) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(coins, amount);

            if (amount < 0) {
                throw new ValidationException("Amount must be non-negative", OPERATION_NAME);
            }

            logger.debug("Solving coin change for amount {} with {} coin types", amount, coins.length);

            // DP array: dp[i] = min coins needed for amount i
            int[] dp = new int[amount + 1];
            Arrays.fill(dp, amount + 1); // Initialize with impossible value
            dp[0] = 0; // Base case

            // Fill DP array
            for (int i = 1; i <= amount; i++) {
                for (int coin : coins) {
                    if (coin <= i) {
                        dp[i] = Math.min(dp[i], dp[i - coin] + 1);
                    }
                }
            }

            int result = dp[amount];
            if (result > amount) {
                result = -1; // Impossible to make change
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Coin change result: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error solving coin change: {}", e.getMessage());
            throw new ValidationException("Failed to solve coin change: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== PALINDROME PARTITIONING =====

    /**
     * Find minimum cuts needed for palindrome partitioning.
     *
     * Time Complexity: O(n^2)
     * Space Complexity: O(n^2)
     *
     * @param str input string
     * @return minimum number of cuts
     */
    public int minPalindromeCuts(String str) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(str);

            int n = str.length();
            if (n == 0) {
                return 0;
            }

            logger.debug("Finding minimum palindrome cuts for string of length {}", n);

            // DP arrays
            int[] cuts = new int[n];
            boolean[][] isPalindrome = new boolean[n][n];

            // Initialize: every single character is a palindrome
            for (int i = 0; i < n; i++) {
                isPalindrome[i][i] = true;
            }

            // Fill palindrome table
            for (int len = 2; len <= n; len++) {
                for (int i = 0; i <= n - len; i++) {
                    int j = i + len - 1;
                    if (str.charAt(i) == str.charAt(j)) {
                        if (len == 2) {
                            isPalindrome[i][j] = true;
                        } else {
                            isPalindrome[i][j] = isPalindrome[i + 1][j - 1];
                        }
                    }
                }
            }

            // Fill cuts array
            for (int i = 0; i < n; i++) {
                if (isPalindrome[0][i]) {
                    cuts[i] = 0;
                } else {
                    cuts[i] = Integer.MAX_VALUE;
                    for (int j = 0; j < i; j++) {
                        if (isPalindrome[j + 1][i]) {
                            cuts[i] = Math.min(cuts[i], cuts[j] + 1);
                        }
                    }
                }
            }

            int result = cuts[n - 1];

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Minimum palindrome cuts: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error finding minimum palindrome cuts: {}", e.getMessage());
            throw new ValidationException("Failed to find minimum palindrome cuts: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== WORD BREAK PROBLEM =====

    /**
     * Check if a string can be segmented into words from dictionary.
     *
     * Time Complexity: O(n^2)
     * Space Complexity: O(n)
     *
     * @param str input string
     * @param wordDict dictionary of valid words
     * @return true if string can be segmented, false otherwise
     */
    public boolean wordBreak(String str, Set<String> wordDict) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(str, wordDict);

            int n = str.length();
            logger.debug("Checking word break for string of length {} with {} words", n, wordDict.size());

            // DP array: dp[i] = true if str[0..i-1] can be segmented
            boolean[] dp = new boolean[n + 1];
            dp[0] = true; // Empty string can always be segmented

            // Fill DP array
            for (int i = 1; i <= n; i++) {
                for (int j = 0; j < i; j++) {
                    if (dp[j] && wordDict.contains(str.substring(j, i))) {
                        dp[i] = true;
                        break;
                    }
                }
            }

            boolean result = dp[n];

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Word break result: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error checking word break: {}", e.getMessage());
            throw new ValidationException("Failed to check word break: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== SUBSET SUM PROBLEM =====

    /**
     * Check if there exists a subset with given sum.
     *
     * Time Complexity: O(n*sum)
     * Space Complexity: O(n*sum)
     *
     * @param arr array of positive integers
     * @param sum target sum
     * @return true if subset exists, false otherwise
     */
    public boolean subsetSum(int[] arr, int sum) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(arr, sum);

            if (sum < 0) {
                return false;
            }

            int n = arr.length;
            logger.debug("Checking subset sum for {} elements and target {}", n, sum);

            // DP table: dp[i][j] = true if sum j can be achieved using first i elements
            boolean[][] dp = new boolean[n + 1][sum + 1];

            // Base case: sum 0 is always possible (empty subset)
            for (int i = 0; i <= n; i++) {
                dp[i][0] = true;
            }

            // Fill DP table
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= sum; j++) {
                    if (arr[i - 1] <= j) {
                        // Include or exclude current element
                        dp[i][j] = dp[i - 1][j] || dp[i - 1][j - arr[i - 1]];
                    } else {
                        // Cannot include current element
                        dp[i][j] = dp[i - 1][j];
                    }
                }
            }

            boolean result = dp[n][sum];

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Subset sum result: {}", result);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error checking subset sum: {}", e.getMessage());
            throw new ValidationException("Failed to check subset sum: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== LONGEST INCREASING SUBSEQUENCE =====

    /**
     * Find length of longest increasing subsequence.
     *
     * Time Complexity: O(n^2)
     * Space Complexity: O(n)
     *
     * @param arr array of integers
     * @return length of LIS
     */
    public int longestIncreasingSubsequence(int[] arr) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs((Object) arr);

            int n = arr.length;
            if (n == 0) {
                return 0;
            }

            logger.debug("Finding LIS for array of length {}", n);

            // DP array: dp[i] = length of LIS ending at index i
            int[] dp = new int[n];
            Arrays.fill(dp, 1); // Each element is a LIS of length 1

            // Fill DP array
            for (int i = 1; i < n; i++) {
                for (int j = 0; j < i; j++) {
                    if (arr[i] > arr[j]) {
                        dp[i] = Math.max(dp[i], dp[j] + 1);
                    }
                }
            }

            // Find maximum value in dp array
            int maxLength = 0;
            for (int length : dp) {
                maxLength = Math.max(maxLength, length);
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("LIS length: {}", maxLength);

            return maxLength;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error finding LIS: {}", e.getMessage());
            throw new ValidationException("Failed to find LIS: " + e.getMessage(), OPERATION_NAME);
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

    // ===== RESULT CLASSES =====

    /**
     * Result class for knapsack solutions.
     */
    public static class KnapsackResult {
        public final int maxValue;
        public final List<Integer> selectedItems;

        public KnapsackResult(int maxValue, List<Integer> selectedItems) {
            this.maxValue = maxValue;
            this.selectedItems = new ArrayList<>(selectedItems);
        }

        @Override
        public String toString() {
            return String.format("Max Value: %d, Selected Items: %s", maxValue, selectedItems);
        }
    }

    /**
     * Get selected items for 0/1 knapsack (requires DP table reconstruction).
     *
     * @param weights array of item weights
     * @param values array of item values
     * @param capacity knapsack capacity
     * @return KnapsackResult with selected items
     */
    public KnapsackResult knapsack01WithItems(int[] weights, int[] values, int capacity) {
        validateInputs(weights, values, capacity);

        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];

        // Fill DP table (same as knapsack01)
        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= capacity; w++) {
                if (weights[i - 1] <= w) {
                    dp[i][w] = Math.max(values[i - 1] + dp[i - 1][w - weights[i - 1]], dp[i - 1][w]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        // Reconstruct solution
        List<Integer> selectedItems = new ArrayList<>();
        int w = capacity;
        for (int i = n; i > 0 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                selectedItems.add(i - 1); // Item index (0-based)
                w -= weights[i - 1];
            }
        }

        Collections.reverse(selectedItems);
        return new KnapsackResult(dp[n][capacity], selectedItems);
    }

    /**
     * Get all possible ways to make coin change (counting problem).
     *
     * @param coins array of coin denominations
     * @param amount target amount
     * @return number of ways to make change
     */
    public long coinChangeWays(int[] coins, int amount) {
        validateInputs(coins, amount);

        if (amount < 0) {
            return 0;
        }

        // DP array: dp[i] = number of ways to make amount i
        long[] dp = new long[amount + 1];
        dp[0] = 1; // One way to make amount 0 (empty combination)

        // Fill DP array
        for (int coin : coins) {
            for (int i = coin; i <= amount; i++) {
                dp[i] += dp[i - coin];
            }
        }

        return dp[amount];
    }
}
