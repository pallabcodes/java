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

package com.netflix.mathlib.recursion;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Recursion Patterns - Production-grade recursive algorithms and patterns.
 *
 * This class provides comprehensive recursive algorithms including:
 * - Divide and Conquer algorithms
 * - Backtracking algorithms
 * - Recursive tree traversal
 * - Combinatorial recursion
 * - Recursive dynamic programming
 * - Memoization techniques
 * - Tail recursion optimization
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
public class RecursionPatterns implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(RecursionPatterns.class);
    private static final String OPERATION_NAME = "RecursionPatterns";
    private static final String COMPLEXITY = "O(various)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;

    /**
     * Constructor for Recursion Patterns.
     */
    public RecursionPatterns() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Recursion Patterns module");
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

    // ===== DIVIDE AND CONQUER ALGORITHMS =====

    /**
     * Binary search using recursion.
     *
     * Time Complexity: O(log n)
     * Space Complexity: O(log n) due to recursion stack
     *
     * @param arr sorted array to search
     * @param target target value
     * @param left left boundary
     * @param right right boundary
     * @return index of target if found, -1 otherwise
     */
    public int binarySearch(int[] arr, int target, int left, int right) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(arr, target, left, right);

            if (left > right) {
                return -1;
            }

            int mid = left + (right - left) / 2;

            if (arr[mid] == target) {
                long executionTime = System.nanoTime() - startTime;
                long memoryUsed = getCurrentMemoryUsage() - startMemory;
                metrics.recordSuccess(executionTime, memoryUsed);
                return mid;
            } else if (arr[mid] > target) {
                return binarySearch(arr, target, left, mid - 1);
            } else {
                return binarySearch(arr, target, mid + 1, right);
            }

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error in binary search: {}", e.getMessage());
            throw new ValidationException("Failed to perform binary search: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Merge sort using recursion.
     *
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     *
     * @param arr array to sort
     * @return sorted array
     */
    public int[] mergeSort(int[] arr) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs((Object) arr);

            if (arr.length <= 1) {
                long executionTime = System.nanoTime() - startTime;
                long memoryUsed = getCurrentMemoryUsage() - startMemory;
                metrics.recordSuccess(executionTime, memoryUsed);
                return arr.clone();
            }

            int mid = arr.length / 2;
            int[] left = Arrays.copyOfRange(arr, 0, mid);
            int[] right = Arrays.copyOfRange(arr, mid, arr.length);

            left = mergeSort(left);
            right = mergeSort(right);

            int[] result = merge(left, right);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;
            metrics.recordSuccess(executionTime, memoryUsed);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error in merge sort: {}", e.getMessage());
            throw new ValidationException("Failed to perform merge sort: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Helper method for merge sort.
     */
    private int[] merge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];
        int i = 0, j = 0, k = 0;

        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) {
                result[k++] = left[i++];
            } else {
                result[k++] = right[j++];
            }
        }

        while (i < left.length) {
            result[k++] = left[i++];
        }

        while (j < right.length) {
            result[k++] = right[j++];
        }

        return result;
    }

    // ===== BACKTRACKING ALGORITHMS =====

    /**
     * Solve N-Queens problem using backtracking.
     *
     * Time Complexity: O(N!) in worst case
     * Space Complexity: O(N^2) for board
     *
     * @param n board size and number of queens
     * @return list of all solutions (each solution is a list of queen positions)
     */
    public List<List<Integer>> solveNQueens(int n) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n);

            if (n < 0) {
                throw new ValidationException("Board size must be non-negative", OPERATION_NAME);
            }

            logger.debug("Solving N-Queens for board size {}", n);

            List<List<Integer>> solutions = new ArrayList<>();
            int[] board = new int[n]; // board[i] = column position of queen in row i
            Arrays.fill(board, -1);

            solveNQueensUtil(board, 0, n, solutions);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;
            metrics.recordSuccess(executionTime, memoryUsed);

            logger.debug("Found {} solutions for N-Queens", solutions.size());
            return solutions;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error solving N-Queens: {}", e.getMessage());
            throw new ValidationException("Failed to solve N-Queens: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Helper method for N-Queens backtracking.
     */
    private void solveNQueensUtil(int[] board, int row, int n, List<List<Integer>> solutions) {
        if (row == n) {
            // Found a solution
            List<Integer> solution = new ArrayList<>();
            for (int col : board) {
                solution.add(col);
            }
            solutions.add(solution);
            return;
        }

        for (int col = 0; col < n; col++) {
            if (isSafe(board, row, col, n)) {
                board[row] = col;
                solveNQueensUtil(board, row + 1, n, solutions);
                board[row] = -1; // Backtrack
            }
        }
    }

    /**
     * Check if queen placement is safe.
     */
    private boolean isSafe(int[] board, int row, int col, int n) {
        // Check same column
        for (int i = 0; i < row; i++) {
            if (board[i] == col) {
                return false;
            }
        }

        // Check upper left diagonal
        for (int i = row - 1, j = col - 1; i >= 0 && j >= 0; i--, j--) {
            if (board[i] == j) {
                return false;
            }
        }

        // Check upper right diagonal
        for (int i = row - 1, j = col + 1; i >= 0 && j < n; i--, j++) {
            if (board[i] == j) {
                return false;
            }
        }

        return true;
    }

    /**
     * Generate all permutations using backtracking.
     *
     * Time Complexity: O(n!)
     * Space Complexity: O(n) for recursion stack
     *
     * @param arr array to generate permutations for
     * @return list of all permutations
     */
    public List<List<Integer>> generatePermutations(int[] arr) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs((Object) arr);

            logger.debug("Generating permutations for array of length {}", arr.length);

            List<List<Integer>> result = new ArrayList<>();
            List<Integer> current = new ArrayList<>();
            boolean[] used = new boolean[arr.length];

            generatePermutationsUtil(arr, current, used, result);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;
            metrics.recordSuccess(executionTime, memoryUsed);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error generating permutations: {}", e.getMessage());
            throw new ValidationException("Failed to generate permutations: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Helper method for permutation generation.
     */
    private void generatePermutationsUtil(int[] arr, List<Integer> current, boolean[] used, List<List<Integer>> result) {
        if (current.size() == arr.length) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = 0; i < arr.length; i++) {
            if (!used[i]) {
                used[i] = true;
                current.add(arr[i]);
                generatePermutationsUtil(arr, current, used, result);
                current.remove(current.size() - 1);
                used[i] = false;
            }
        }
    }

    /**
     * Solve subset sum problem using backtracking.
     *
     * @param arr array of integers
     * @param target target sum
     * @return list of all subsets that sum to target
     */
    public List<List<Integer>> subsetSumBacktracking(int[] arr, int target) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(arr, target);

            logger.debug("Finding subsets that sum to {} in array of length {}", target, arr.length);

            List<List<Integer>> result = new ArrayList<>();
            List<Integer> current = new ArrayList<>();

            subsetSumUtil(arr, target, 0, current, result);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;
            metrics.recordSuccess(executionTime, memoryUsed);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error in subset sum backtracking: {}", e.getMessage());
            throw new ValidationException("Failed to find subset sum: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Helper method for subset sum backtracking.
     */
    private void subsetSumUtil(int[] arr, int target, int index, List<Integer> current, List<List<Integer>> result) {
        if (target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        if (target < 0 || index >= arr.length) {
            return;
        }

        // Include current element
        current.add(arr[index]);
        subsetSumUtil(arr, target - arr[index], index + 1, current, result);
        current.remove(current.size() - 1);

        // Exclude current element
        subsetSumUtil(arr, target, index + 1, current, result);
    }

    // ===== RECURSIVE TREE TRAVERSAL =====

    /**
     * Calculate height of binary tree recursively.
     *
     * @param root root node of binary tree
     * @return height of tree
     */
    public int treeHeight(TreeNode root) {
        if (root == null) {
            return 0;
        }

        int leftHeight = treeHeight(root.left);
        int rightHeight = treeHeight(root.right);

        return Math.max(leftHeight, rightHeight) + 1;
    }

    /**
     * Count nodes in binary tree recursively.
     *
     * @param root root node of binary tree
     * @return number of nodes
     */
    public int countNodes(TreeNode root) {
        if (root == null) {
            return 0;
        }

        return 1 + countNodes(root.left) + countNodes(root.right);
    }

    /**
     * Check if binary tree is balanced recursively.
     *
     * @param root root node of binary tree
     * @return true if balanced, false otherwise
     */
    public boolean isBalanced(TreeNode root) {
        return checkBalance(root) != -1;
    }

    /**
     * Helper method for balance checking.
     */
    private int checkBalance(TreeNode node) {
        if (node == null) {
            return 0;
        }

        int leftHeight = checkBalance(node.left);
        if (leftHeight == -1) {
            return -1;
        }

        int rightHeight = checkBalance(node.right);
        if (rightHeight == -1) {
            return -1;
        }

        if (Math.abs(leftHeight - rightHeight) > 1) {
            return -1;
        }

        return Math.max(leftHeight, rightHeight) + 1;
    }

    // ===== COMBINATORIAL RECURSION =====

    /**
     * Generate all subsets using recursion.
     *
     * Time Complexity: O(2^n)
     * Space Complexity: O(n) for recursion stack
     *
     * @param arr array to generate subsets from
     * @return list of all subsets
     */
    public List<List<Integer>> generateSubsets(int[] arr) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs((Object) arr);

            logger.debug("Generating subsets for array of length {}", arr.length);

            List<List<Integer>> result = new ArrayList<>();
            List<Integer> current = new ArrayList<>();

            generateSubsetsUtil(arr, 0, current, result);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;
            metrics.recordSuccess(executionTime, memoryUsed);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error generating subsets: {}", e.getMessage());
            throw new ValidationException("Failed to generate subsets: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Helper method for subset generation.
     */
    private void generateSubsetsUtil(int[] arr, int index, List<Integer> current, List<List<Integer>> result) {
        if (index == arr.length) {
            result.add(new ArrayList<>(current));
            return;
        }

        // Include current element
        current.add(arr[index]);
        generateSubsetsUtil(arr, index + 1, current, result);
        current.remove(current.size() - 1);

        // Exclude current element
        generateSubsetsUtil(arr, index + 1, current, result);
    }

    /**
     * Solve Tower of Hanoi using recursion.
     *
     * @param n number of disks
     * @param from source peg
     * @param to destination peg
     * @param aux auxiliary peg
     * @return list of moves
     */
    public List<String> towerOfHanoi(int n, char from, char to, char aux) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n, from, to, aux);

            if (n < 0) {
                throw new ValidationException("Number of disks must be non-negative", OPERATION_NAME);
            }

            logger.debug("Solving Tower of Hanoi for {} disks", n);

            List<String> moves = new ArrayList<>();
            towerOfHanoiUtil(n, from, to, aux, moves);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;
            metrics.recordSuccess(executionTime, memoryUsed);

            return moves;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error solving Tower of Hanoi: {}", e.getMessage());
            throw new ValidationException("Failed to solve Tower of Hanoi: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Helper method for Tower of Hanoi.
     */
    private void towerOfHanoiUtil(int n, char from, char to, char aux, List<String> moves) {
        if (n == 1) {
            moves.add("Move disk 1 from " + from + " to " + to);
            return;
        }

        towerOfHanoiUtil(n - 1, from, aux, to, moves);
        moves.add("Move disk " + n + " from " + from + " to " + to);
        towerOfHanoiUtil(n - 1, aux, to, from, moves);
    }

    // ===== MEMOIZATION TECHNIQUES =====

    /**
     * Calculate Fibonacci number with memoization.
     *
     * Time Complexity: O(n)
     * Space Complexity: O(n) for memoization cache
     *
     * @param n term number
     * @return nth Fibonacci number
     */
    public long fibonacciMemoized(int n) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n);

            if (n < 0) {
                throw new ValidationException("Term number must be non-negative", OPERATION_NAME);
            }

            logger.debug("Calculating Fibonacci({}) with memoization", n);

            Map<Integer, Long> memo = new HashMap<>();
            long result = fibonacciMemoUtil(n, memo);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;
            metrics.recordSuccess(executionTime, memoryUsed);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating Fibonacci with memoization: {}", e.getMessage());
            throw new ValidationException("Failed to calculate Fibonacci: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Helper method for memoized Fibonacci.
     */
    private long fibonacciMemoUtil(int n, Map<Integer, Long> memo) {
        if (n <= 1) {
            return n;
        }

        if (memo.containsKey(n)) {
            return memo.get(n);
        }

        long result = fibonacciMemoUtil(n - 1, memo) + fibonacciMemoUtil(n - 2, memo);
        memo.put(n, result);
        return result;
    }

    // ===== TAIL RECURSION OPTIMIZATION =====

    /**
     * Calculate factorial using tail recursion.
     *
     * @param n number to calculate factorial for
     * @return n!
     */
    public long factorialTailRecursive(int n) {
        validateInputs(n);

        if (n < 0) {
            throw new ValidationException("Factorial is not defined for negative numbers", OPERATION_NAME);
        }

        return factorialTailUtil(n, 1);
    }

    /**
     * Helper method for tail recursive factorial.
     */
    private long factorialTailUtil(int n, long accumulator) {
        if (n <= 1) {
            return accumulator;
        }

        return factorialTailUtil(n - 1, n * accumulator);
    }

    // ===== RECURSIVE DYNAMIC PROGRAMMING =====

    /**
     * Solve coin change problem using recursive memoization.
     *
     * @param coins array of coin denominations
     * @param amount target amount
     * @return minimum number of coins needed
     */
    public int coinChangeRecursive(int[] coins, int amount) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(coins, amount);

            if (amount < 0) {
                return -1;
            }

            logger.debug("Solving coin change recursively for amount {} with {} coins", amount, coins.length);

            Map<Integer, Integer> memo = new HashMap<>();
            int result = coinChangeRecursiveUtil(coins, amount, memo);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;
            metrics.recordSuccess(executionTime, memoryUsed);

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error in recursive coin change: {}", e.getMessage());
            throw new ValidationException("Failed to solve coin change: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Helper method for recursive coin change.
     */
    private int coinChangeRecursiveUtil(int[] coins, int amount, Map<Integer, Integer> memo) {
        if (amount == 0) {
            return 0;
        }

        if (amount < 0) {
            return -1;
        }

        if (memo.containsKey(amount)) {
            return memo.get(amount);
        }

        int minCoins = Integer.MAX_VALUE;

        for (int coin : coins) {
            int result = coinChangeRecursiveUtil(coins, amount - coin, memo);
            if (result >= 0) {
                minCoins = Math.min(minCoins, result + 1);
            }
        }

        int finalResult = minCoins == Integer.MAX_VALUE ? -1 : minCoins;
        memo.put(amount, finalResult);
        return finalResult;
    }

    // ===== UTILITY METHODS =====

    /**
     * Get current memory usage in bytes.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // ===== DATA STRUCTURES =====

    /**
     * Simple binary tree node for demonstration.
     */
    public static class TreeNode {
        public int val;
        public TreeNode left;
        public TreeNode right;

        public TreeNode(int val) {
            this.val = val;
        }

        public TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }
}
