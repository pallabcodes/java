package com.algorithmpractice.config;

/**
 * Configuration constants for the Algorithm Practice application.
 * 
 * <p>This class centralizes all configuration values to avoid magic numbers
 * and make the application easily configurable.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class AlgorithmConfig {

    // Application version
    public static final String APP_VERSION = "2.0.0";
    
    // Sorting algorithm configuration
    public static final int QUICKSORT_SMALL_ARRAY_THRESHOLD = 10;
    public static final int QUICKSORT_MEDIAN_OF_THREE_THRESHOLD = 40;
    
    // Binary search configuration
    public static final int BINARY_SEARCH_MAX_ITERATIONS = 1000;
    
    // Dynamic array configuration
    public static final int DYNAMIC_ARRAY_DEFAULT_CAPACITY = 10;
    public static final int DYNAMIC_ARRAY_MAX_CAPACITY = Integer.MAX_VALUE - 8;
    public static final int DYNAMIC_ARRAY_GROWTH_FACTOR = 2;
    
    // Demo data configuration
    public static final int[] DEMO_UNSORTED_ARRAY = {64, 34, 25, 12, 22, 11, 90};
    public static final int[] DEMO_SORTED_ARRAY = {11, 12, 22, 25, 34, 64, 90};
    public static final int DEMO_TARGET_VALUE = 25;
    
    // Performance configuration
    public static final int PERFORMANCE_TEST_ARRAY_SIZE = 100_000;
    public static final int PERFORMANCE_TEST_ITERATIONS = 100;
    public static final long PERFORMANCE_TIMEOUT_MS = 30_000; // 30 seconds
    
    // Logging configuration
    public static final String LOG_LEVEL_ROOT = "INFO";
    public static final String LOG_LEVEL_APP = "DEBUG";
    public static final String LOG_FILE_PATH = "logs/algorithm-practice.log";
    public static final int LOG_FILE_MAX_SIZE_MB = 10;
    public static final int LOG_FILE_MAX_HISTORY_DAYS = 30;
    
    // Error handling configuration
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final long RETRY_DELAY_MS = 100;
    
    // Validation configuration
    public static final int MAX_ARRAY_SIZE = 1_000_000;
    public static final int MIN_ARRAY_SIZE = 1;
    
    private AlgorithmConfig() {
        // Utility class - prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
