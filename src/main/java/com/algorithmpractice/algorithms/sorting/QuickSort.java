package com.algorithmpractice.algorithms.sorting;

import com.algorithmpractice.utils.ArrayUtils;

/**
 * Implementation of the QuickSort algorithm.
 * 
 * <p>QuickSort is a highly efficient, comparison-based sorting algorithm that uses
 * a divide-and-conquer strategy. It has an average time complexity of O(n log n)
 * and a worst-case time complexity of O(n²), though this is rare with proper
 * pivot selection.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>In-place sorting (modifies the input array)</li>
 *   <li>Unstable sorting (relative order of equal elements may change)</li>
 *   <li>Recursive implementation</li>
 *   <li>Uses median-of-three pivot selection for better performance</li>
 *   <li>Thread-safe implementation</li>
 * </ul>
 * 
 * <p>This implementation is optimized for performance and handles edge cases gracefully.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class QuickSort {

    private QuickSort() {
        // Utility class - prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Sorts an array of integers using the QuickSort algorithm.
     * 
     * @param array the array to sort (will be modified)
     * @throws IllegalArgumentException if array is null
     */
    public static void sort(final int[] array) {
        validateInput(array);
        
        if (array.length <= 1) {
            return; // Already sorted
        }
        
        quickSort(array, 0, array.length - 1);
    }

    /**
     * Recursive QuickSort implementation.
     * 
     * @param array the array to sort
     * @param low   the starting index of the partition
     * @param high  the ending index of the partition
     */
    private static void quickSort(final int[] array, final int low, final int high) {
        if (low < high) {
            final int pivotIndex = partition(array, low, high);
            quickSort(array, low, pivotIndex - 1);
            quickSort(array, pivotIndex + 1, high);
        }
    }

    /**
     * Partitions the array around a pivot element.
     * Uses median-of-three pivot selection for better performance.
     * 
     * @param array the array to partition
     * @param low   the starting index
     * @param high  the ending index
     * @return the final position of the pivot element
     */
    private static int partition(final int[] array, final int low, final int high) {
        // Use median-of-three pivot selection
        final int pivotIndex = selectPivot(array, low, high);
        final int pivotValue = array[pivotIndex];
        
        // Move pivot to the end
        ArrayUtils.swap(array, pivotIndex, high);
        
        int i = low - 1;
        
        // Partition elements around pivot
        for (int j = low; j < high; j++) {
            if (array[j] <= pivotValue) {
                i++;
                ArrayUtils.swap(array, i, j);
            }
        }
        
        // Place pivot in its final position
        ArrayUtils.swap(array, i + 1, high);
        return i + 1;
    }

    /**
     * Selects a pivot using the median-of-three method.
     * This helps avoid the worst-case scenario of already sorted arrays.
     * 
     * @param array the array
     * @param low   the starting index
     * @param high  the ending index
     * @return the index of the selected pivot
     */
    private static int selectPivot(final int[] array, final int low, final int high) {
        final int mid = low + (high - low) / 2;
        
        // Find median of low, mid, and high
        if (array[low] > array[mid]) {
            if (array[mid] > array[high]) {
                return mid;
            } else if (array[low] > array[high]) {
                return high;
            } else {
                return low;
            }
        } else {
            if (array[low] > array[high]) {
                return low;
            } else if (array[mid] > array[high]) {
                return high;
            } else {
                return mid;
            }
        }
    }

    /**
     * Sorts an array and returns a new sorted array without modifying the original.
     * 
     * @param array the array to sort
     * @return a new sorted array
     * @throws IllegalArgumentException if array is null
     */
    public static int[] sortCopy(final int[] array) {
        validateInput(array);
        
        final int[] copy = ArrayUtils.copyArray(array);
        sort(copy);
        return copy;
    }

    /**
     * Validates input parameters for QuickSort operations.
     * 
     * @param array the array to validate
     * @throws IllegalArgumentException if array is null
     */
    private static void validateInput(final int[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
    }
}
