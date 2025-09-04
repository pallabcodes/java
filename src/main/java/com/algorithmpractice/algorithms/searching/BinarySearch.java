package com.algorithmpractice.algorithms.searching;

/**
 * Implementation of the Binary Search algorithm.
 * 
 * <p>Binary Search is an efficient algorithm for finding a target element in a
 * sorted array. It works by repeatedly dividing the search interval in half,
 * making it much faster than linear search for large datasets.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Requires the input array to be sorted</li>
 *   <li>Time complexity: O(log n)</li>
 *   <li>Space complexity: O(1) for iterative, O(log n) for recursive</li>
 *   <li>Returns the index of the target element or NOT_FOUND if not found</li>
 * </ul>
 * 
 * <p>This implementation is thread-safe and handles edge cases gracefully.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class BinarySearch {

    /**
     * Constant indicating that the target element was not found.
     */
    public static final int NOT_FOUND = -1;

    private BinarySearch() {
        // Utility class - prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Searches for a target element in a sorted array using iterative binary search.
     * 
     * @param array  the sorted array to search in
     * @param target the element to find
     * @return the index of the target element, or NOT_FOUND if not found
     * @throws IllegalArgumentException if array is null or empty
     */
    public static int search(final int[] array, final int target) {
        validateInput(array);
        return iterativeSearch(array, target);
    }

    /**
     * Iterative implementation of binary search.
     * 
     * @param array  the sorted array to search in
     * @param target the element to find
     * @return the index of the target element, or NOT_FOUND if not found
     */
    private static int iterativeSearch(final int[] array, final int target) {
        int left = 0;
        int right = array.length - 1;
        
        while (left <= right) {
            final int mid = left + (right - left) / 2;
            
            if (array[mid] == target) {
                return mid; // Target found
            } else if (array[mid] < target) {
                left = mid + 1; // Search right half
            } else {
                right = mid - 1; // Search left half
            }
        }
        
        return NOT_FOUND; // Target not found
    }

    /**
     * Recursive implementation of binary search.
     * 
     * @param array  the sorted array to search in
     * @param target the element to find
     * @return the index of the target element, or NOT_FOUND if not found
     * @throws IllegalArgumentException if array is null or empty
     */
    public static int searchRecursive(final int[] array, final int target) {
        validateInput(array);
        return recursiveSearch(array, target, 0, array.length - 1);
    }

    /**
     * Recursive binary search implementation.
     * 
     * @param array  the sorted array to search in
     * @param target the element to find
     * @param left   the left boundary of the search range
     * @param right  the right boundary of the search range
     * @return the index of the target element, or NOT_FOUND if not found
     */
    private static int recursiveSearch(final int[] array, final int target, final int left, final int right) {
        if (left > right) {
            return NOT_FOUND; // Base case: target not found
        }
        
        final int mid = left + (right - left) / 2;
        
        if (array[mid] == target) {
            return mid; // Target found
        } else if (array[mid] < target) {
            return recursiveSearch(array, target, mid + 1, right); // Search right half
        } else {
            return recursiveSearch(array, target, left, mid - 1); // Search left half
        }
    }

    /**
     * Finds the first occurrence of a target element in a sorted array.
     * Useful when the array may contain duplicate elements.
     * 
     * @param array  the sorted array to search in
     * @param target the element to find
     * @return the index of the first occurrence, or NOT_FOUND if not found
     * @throws IllegalArgumentException if array is null or empty
     */
    public static int findFirstOccurrence(final int[] array, final int target) {
        validateInput(array);
        
        int left = 0;
        int right = array.length - 1;
        int result = NOT_FOUND;
        
        while (left <= right) {
            final int mid = left + (right - left) / 2;
            
            if (array[mid] == target) {
                result = mid;
                right = mid - 1; // Continue searching left for earlier occurrence
            } else if (array[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return result;
    }

    /**
     * Finds the last occurrence of a target element in a sorted array.
     * Useful when the array may contain duplicate elements.
     * 
     * @param array  the sorted array to search in
     * @param target the element to find
     * @return the index of the last occurrence, or NOT_FOUND if not found
     * @throws IllegalArgumentException if array is null or empty
     */
    public static int findLastOccurrence(final int[] array, final int target) {
        validateInput(array);
        
        int left = 0;
        int right = array.length - 1;
        int result = NOT_FOUND;
        
        while (left <= right) {
            final int mid = left + (right - left) / 2;
            
            if (array[mid] == target) {
                result = mid;
                left = mid + 1; // Continue searching right for later occurrence
            } else if (array[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return result;
    }

    /**
     * Finds the insertion point for a target element in a sorted array.
     * Returns the index where the element should be inserted to maintain order.
     * 
     * @param array  the sorted array
     * @param target the element to find insertion point for
     * @return the insertion index
     * @throws IllegalArgumentException if array is null
     */
    public static int findInsertionPoint(final int[] array, final int target) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        
        int left = 0;
        int right = array.length;
        
        while (left < right) {
            final int mid = left + (right - left) / 2;
            
            if (array[mid] < target) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        
        return left;
    }

    /**
     * Validates input parameters for binary search operations.
     * 
     * @param array the array to validate
     * @throws IllegalArgumentException if array is null or empty
     */
    private static void validateInput(final int[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }
    }
}
