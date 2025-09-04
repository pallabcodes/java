package com.algorithmpractice.constants;

/**
 * Constants used throughout the algorithm implementations.
 * 
 * <p>This class provides centralized constants to avoid magic numbers
 * and improve code maintainability. All constants are public static final
 * and follow Java naming conventions.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class AlgorithmConstants {

    private AlgorithmConstants() {
        // Utility class - prevent instantiation
    }

    // ============================================================================
    // ARRAY CONSTANTS
    // ============================================================================

    /** Default initial capacity for dynamic arrays. */
    public static final int DEFAULT_ARRAY_CAPACITY = 10;

    /** Growth factor for dynamic array resizing. */
    public static final double ARRAY_GROWTH_FACTOR = 2.0;

    /** Maximum array size to prevent integer overflow. */
    public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    // ============================================================================
    // SORTING CONSTANTS
    // ============================================================================

    /** Threshold for switching to insertion sort in hybrid algorithms. */
    public static final int INSERTION_SORT_THRESHOLD = 10;

    /** Threshold for switching to merge sort in hybrid algorithms. */
    public static final int MERGE_SORT_THRESHOLD = 7;

    /** Maximum recursion depth for quicksort to prevent stack overflow. */
    public static final int MAX_QUICKSORT_DEPTH = 100;

    // ============================================================================
    // SEARCHING CONSTANTS
    // ============================================================================

    /** Value returned when element is not found in search algorithms. */
    public static final int ELEMENT_NOT_FOUND = -1;

    /** Minimum array size for binary search to be efficient. */
    public static final int BINARY_SEARCH_MIN_SIZE = 3;

    // ============================================================================
    // TREE CONSTANTS
    // ============================================================================

    /** Maximum height difference for AVL tree balancing. */
    public static final int AVL_MAX_HEIGHT_DIFF = 1;

    /** Red-black tree color constants. */
    public static final boolean RED = true;
    public static final boolean BLACK = false;

    /** Default B-tree order. */
    public static final int DEFAULT_BTREE_ORDER = 4;

    // ============================================================================
    // GRAPH CONSTANTS
    // ============================================================================

    /** Default initial capacity for graph adjacency lists. */
    public static final int DEFAULT_GRAPH_CAPACITY = 16;

    /** Maximum number of vertices for dense graph representation. */
    public static final int DENSE_GRAPH_THRESHOLD = 1000;

    /** Infinity value for graph algorithms (using Integer.MAX_VALUE / 2 to prevent overflow). */
    public static final int GRAPH_INFINITY = Integer.MAX_VALUE / 2;

    // ============================================================================
    // STRING CONSTANTS
    // ============================================================================

    /** Default initial capacity for string builders. */
    public static final int DEFAULT_STRING_BUILDER_CAPACITY = 16;

    /** ASCII character set size. */
    public static final int ASCII_SIZE = 128;

    /** Extended ASCII character set size. */
    public static final int EXTENDED_ASCII_SIZE = 256;

    // ============================================================================
    // MATHEMATICAL CONSTANTS
    // ============================================================================

    /** Mathematical constant PI (approximation). */
    public static final double PI = Math.PI;

    /** Mathematical constant E (approximation). */
    public static final double E = Math.E;

    /** Golden ratio constant. */
    public static final double GOLDEN_RATIO = 1.618033988749895;

    /** Square root of 2. */
    public static final double SQRT_2 = 1.4142135623730951;

    // ============================================================================
    // PERFORMANCE CONSTANTS
    // ============================================================================

    /** Time threshold for performance warnings (in milliseconds). */
    public static final long PERFORMANCE_WARNING_THRESHOLD = 1000;

    /** Memory threshold for performance warnings (in bytes). */
    public static final long MEMORY_WARNING_THRESHOLD = 100 * 1024 * 1024; // 100MB

    // ============================================================================
    // ERROR MESSAGES
    // ============================================================================

    /** Error message for null array input. */
    public static final String ERROR_NULL_ARRAY = "Array cannot be null";

    /** Error message for empty array input. */
    public static final String ERROR_EMPTY_ARRAY = "Array cannot be empty";

    /** Error message for invalid index. */
    public static final String ERROR_INVALID_INDEX = "Index is out of bounds";

    /** Error message for unsorted array. */
    public static final String ERROR_UNSORTED_ARRAY = "Array must be sorted for this operation";

    // ============================================================================
    // VALIDATION CONSTANTS
    // ============================================================================

    /** Minimum valid array length for most operations. */
    public static final int MIN_ARRAY_LENGTH = 1;

    /** Maximum valid array length for safe operations. */
    public static final int MAX_SAFE_ARRAY_LENGTH = 1_000_000;

    /** Minimum valid integer value for most algorithms. */
    public static final int MIN_SAFE_INTEGER = -1_000_000;

    /** Maximum valid integer value for most algorithms. */
    public static final int MAX_SAFE_INTEGER = 1_000_000;
}
