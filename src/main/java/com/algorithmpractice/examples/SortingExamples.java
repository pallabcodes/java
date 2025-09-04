package com.algorithmpractice.examples;

import com.algorithmpractice.algorithms.sorting.QuickSort;
import com.algorithmpractice.utils.ArrayUtils;

import java.util.Arrays;

/**
 * Examples demonstrating the usage of sorting algorithms.
 * 
 * <p>This class provides practical examples of how to use various
 * sorting algorithms from the library. It includes common use cases
 * and demonstrates best practices for algorithm usage.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class SortingExamples {

    private SortingExamples() {
        // Utility class - prevent instantiation
    }

    /**
     * Demonstrates basic QuickSort usage with different array types.
     */
    public static void demonstrateQuickSort() {
        System.out.println("🔄 QuickSort Examples");
        System.out.println("=====================");

        // Example 1: Basic integer array
        int[] numbers = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("Original array: " + Arrays.toString(numbers));
        
        int[] sortedNumbers = QuickSort.sortCopy(numbers);
        System.out.println("After QuickSort: " + Arrays.toString(sortedNumbers));
        System.out.println("Array is sorted: " + ArrayUtils.isSorted(sortedNumbers));
        System.out.println();

        // Example 2: Already sorted array
        int[] sortedArray = {1, 2, 3, 4, 5};
        System.out.println("Already sorted array: " + Arrays.toString(sortedArray));
        
        int[] result = QuickSort.sortCopy(sortedArray);
        System.out.println("After QuickSort: " + Arrays.toString(result));
        System.out.println("Array is sorted: " + ArrayUtils.isSorted(result));
        System.out.println();

        // Example 3: Reverse sorted array
        int[] reverseArray = {5, 4, 3, 2, 1};
        System.out.println("Reverse sorted array: " + Arrays.toString(reverseArray));
        
        int[] sortedReverse = QuickSort.sortCopy(reverseArray);
        System.out.println("After QuickSort: " + Arrays.toString(sortedReverse));
        System.out.println("Array is sorted: " + ArrayUtils.isSorted(sortedReverse));
        System.out.println();

        // Example 4: Array with duplicates
        int[] duplicateArray = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        System.out.println("Array with duplicates: " + Arrays.toString(duplicateArray));
        
        int[] sortedDuplicates = QuickSort.sortCopy(duplicateArray);
        System.out.println("After QuickSort: " + Arrays.toString(sortedDuplicates));
        System.out.println("Array is sorted: " + ArrayUtils.isSorted(sortedDuplicates));
        System.out.println();
    }

    /**
     * Demonstrates performance comparison between different array sizes.
     */
    public static void demonstratePerformance() {
        System.out.println("⚡ Performance Examples");
        System.out.println("======================");

        // Small array
        int[] smallArray = generateRandomArray(100);
        long startTime = System.nanoTime();
        QuickSort.sortCopy(smallArray);
        long endTime = System.nanoTime();
        System.out.printf("Small array (100 elements): %.2f ms%n", 
                         (endTime - startTime) / 1_000_000.0);

        // Medium array
        int[] mediumArray = generateRandomArray(10_000);
        startTime = System.nanoTime();
        QuickSort.sortCopy(mediumArray);
        endTime = System.nanoTime();
        System.out.printf("Medium array (10,000 elements): %.2f ms%n", 
                         (endTime - startTime) / 1_000_000.0);

        // Large array
        int[] largeArray = generateRandomArray(100_000);
        startTime = System.nanoTime();
        QuickSort.sortCopy(largeArray);
        endTime = System.nanoTime();
        System.out.printf("Large array (100,000 elements): %.2f ms%n", 
                         (endTime - startTime) / 1_000_000.0);
        System.out.println();
    }

    /**
     * Demonstrates edge cases and error handling.
     */
    public static void demonstrateEdgeCases() {
        System.out.println("⚠️  Edge Cases Examples");
        System.out.println("=======================");

        try {
            // Empty array
            int[] emptyArray = {};
            System.out.println("Empty array: " + Arrays.toString(emptyArray));
            int[] sortedEmpty = QuickSort.sortCopy(emptyArray);
            System.out.println("After QuickSort: " + Arrays.toString(sortedEmpty));
            System.out.println();

            // Single element array
            int[] singleArray = {42};
            System.out.println("Single element array: " + Arrays.toString(singleArray));
            int[] sortedSingle = QuickSort.sortCopy(singleArray);
            System.out.println("After QuickSort: " + Arrays.toString(sortedSingle));
            System.out.println();

            // Two element array
            int[] twoArray = {99, 1};
            System.out.println("Two element array: " + Arrays.toString(twoArray));
            int[] sortedTwo = QuickSort.sortCopy(twoArray);
            System.out.println("After QuickSort: " + Arrays.toString(sortedTwo));
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error handling edge case: " + e.getMessage());
        }
    }

    /**
     * Demonstrates in-place sorting vs. copy sorting.
     */
    public static void demonstrateInPlaceVsCopy() {
        System.out.println("🔄 In-Place vs Copy Sorting");
        System.out.println("============================");

        int[] original = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("Original array: " + Arrays.toString(original));

        // In-place sorting (modifies original)
        int[] inPlaceArray = original.clone();
        QuickSort.sort(inPlaceArray);
        System.out.println("In-place result: " + Arrays.toString(inPlaceArray));
        System.out.println("Original modified: " + Arrays.toString(original));

        // Copy sorting (preserves original)
        int[] copyArray = QuickSort.sortCopy(original);
        System.out.println("Copy result: " + Arrays.toString(copyArray));
        System.out.println("Original preserved: " + Arrays.toString(original));
        System.out.println();
    }

    /**
     * Main method to run all examples.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(final String[] args) {
        System.out.println("🚀 Algorithm Practice - Sorting Examples");
        System.out.println("=========================================");
        System.out.println();

        demonstrateQuickSort();
        demonstratePerformance();
        demonstrateEdgeCases();
        demonstrateInPlaceVsCopy();

        System.out.println("✅ All examples completed successfully!");
    }

    /**
     * Generates a random array of the specified size.
     * 
     * @param size the size of the array to generate
     * @return a random integer array
     */
    private static int[] generateRandomArray(final int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = (int) (Math.random() * 1000);
        }
        return array;
    }
}
