package com.algorithmpractice.utils;

import java.util.Arrays;

/**
 * Utility class for array operations.
 * Provides common array manipulation and validation methods.
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class ArrayUtils {

    private ArrayUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Checks if an array is sorted in ascending order.
     * 
     * @param array the array to check
     * @return true if the array is sorted, false otherwise
     * @throws IllegalArgumentException if array is null
     */
    public static boolean isSorted(final int[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        
        if (array.length <= 1) {
            return true;
        }
        
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Swaps two elements in an array.
     * 
     * @param array the array containing the elements
     * @param i     the index of the first element
     * @param j     the index of the second element
     * @throws IllegalArgumentException if array is null or indices are invalid
     */
    public static void swap(final int[] array, final int i, final int j) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        
        if (i < 0 || i >= array.length || j < 0 || j >= array.length) {
            throw new IllegalArgumentException("Invalid indices: i=" + i + ", j=" + j);
        }
        
        final int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    /**
     * Creates a copy of an array.
     * 
     * @param array the array to copy
     * @return a new array with the same elements
     * @throws IllegalArgumentException if array is null
     */
    public static int[] copyArray(final int[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        return Arrays.copyOf(array, array.length);
    }

    /**
     * Reverses the elements in an array.
     * 
     * @param array the array to reverse
     * @throws IllegalArgumentException if array is null
     */
    public static void reverse(final int[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        
        for (int i = 0; i < array.length / 2; i++) {
            swap(array, i, array.length - 1 - i);
        }
    }

    /**
     * Finds the maximum value in an array.
     * 
     * @param array the array to search
     * @return the maximum value
     * @throws IllegalArgumentException if array is null or empty
     */
    public static int findMax(final int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array cannot be null or empty");
        }
        
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * Finds the minimum value in an array.
     * 
     * @param array the array to search
     * @return the minimum value
     * @throws IllegalArgumentException if array is null or empty
     */
    public static int findMin(final int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array cannot be null or empty");
        }
        
        int min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    /**
     * Calculates the sum of all elements in an array.
     * 
     * @param array the array to sum
     * @return the sum of all elements
     * @throws IllegalArgumentException if array is null
     */
    public static long sum(final int[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        
        long sum = 0;
        for (final int element : array) {
            sum += element;
        }
        return sum;
    }

    /**
     * Calculates the average of all elements in an array.
     * 
     * @param array the array to average
     * @return the average of all elements
     * @throws IllegalArgumentException if array is null or empty
     */
    public static double average(final int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array cannot be null or empty");
        }
        
        return (double) sum(array) / array.length;
    }
}
