package netflix.generics.advanced;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import netflix.generics.basic.GenericBox;

/**
 * Netflix Production-Grade Bounds Examples
 * 
 * This class demonstrates advanced bounds concepts including:
 * - Upper bounds (extends)
 * - Lower bounds (super)
 * - Multiple bounds
 * - Recursive bounds
 * - Bounded wildcards
 * - Bounds with generic methods
 * - Bounds with generic classes
 * 
 * @author Netflix Java Generics Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class BoundsExamples {

    /**
     * Demonstrates upper bounds with generic methods
     * 
     * Upper bounds restrict the type parameter to be a subtype of the specified type.
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number
     */
    public static <T extends Number> void processUpperBound(List<T> list) {
        log.info("Processing upper bound list with {} elements", list.size());
        
        // Can call methods from the upper bound
        for (T item : list) {
            log.debug("Processing number: {} (type: {})", item, item.getClass().getSimpleName());
            
            // Can call Number methods
            double value = item.doubleValue();
            int intValue = item.intValue();
            log.debug("Double value: {}, Int value: {}", value, intValue);
        }
        
        // Can perform arithmetic operations
        double sum = list.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Number::doubleValue)
                .sum();
        
        log.info("Sum of numbers: {}", sum);
    }

    /**
     * Demonstrates multiple upper bounds
     * 
     * Multiple bounds allow you to restrict the type parameter to implement
     * multiple interfaces or extend multiple classes.
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number and Comparable
     */
    public static <T extends Number & Comparable<T>> void processMultipleUpperBounds(List<T> list) {
        log.info("Processing multiple upper bounds list with {} elements", list.size());
        
        // Can call methods from all bounds
        for (T item : list) {
            log.debug("Processing number: {} (type: {})", item, item.getClass().getSimpleName());
            
            // Can call Number methods
            double value = item.doubleValue();
            log.debug("Double value: {}", value);
            
            // Can call Comparable methods
            if (list.size() > 1) {
                T other = list.get(0);
                int comparison = item.compareTo(other);
                log.debug("Comparison result: {}", comparison);
            }
        }
        
        // Can sort the list
        list.sort(Comparator.naturalOrder());
        log.info("Sorted list: {}", list);
    }

    /**
     * Demonstrates recursive bounds
     * 
     * Recursive bounds allow you to create self-referencing type parameters.
     * 
     * @param list the list to process
     * @param <T> the type parameter that extends Comparable<T>
     */
    public static <T extends Comparable<T>> void processRecursiveBound(List<T> list) {
        log.info("Processing recursive bound list with {} elements", list.size());
        
        // Can call compareTo on elements
        for (T item : list) {
            log.debug("Processing comparable item: {}", item);
            
            if (list.size() > 1) {
                T other = list.get(0);
                int comparison = item.compareTo(other);
                log.debug("Comparison result: {}", comparison);
            }
        }
        
        // Can sort the list
        list.sort(Comparator.naturalOrder());
        log.info("Sorted list: {}", list);
    }

    /**
     * Demonstrates bounds with generic classes
     * 
     * @param box the generic box to process
     * @param <T> the type parameter bounded by Number
     */
    public static <T extends Number> void processBoundedGenericBox(GenericBox<T> box) {
        log.info("Processing bounded generic box");
        
        // Can read from the box
        T content = box.getContent();
        if (content != null) {
            log.debug("Box content: {} (type: {})", content, content.getClass().getSimpleName());
            
            // Can call Number methods
            double value = content.doubleValue();
            log.debug("Double value: {}", value);
        }
        
        // Can call other methods
        log.debug("Box is empty: {}", box.isEmpty());
    }

    /**
     * Demonstrates bounds with generic methods and wildcards
     * 
     * @param list the list to process
     */
    public static void processBoundedWildcard(List<? extends Number> list) {
        log.info("Processing bounded wildcard list with {} elements", list.size());
        
        // Can read from the list
        for (Number number : list) {
            log.debug("Processing number: {} (type: {})", number, number.getClass().getSimpleName());
            
            // Can call Number methods
            double value = number.doubleValue();
            log.debug("Double value: {}", value);
        }
        
        // Cannot add elements (except null)
        // list.add(42); // Compile error!
        list.add(null); // This is allowed
        
        // Can call other methods
        log.debug("List size: {}, isEmpty: {}", list.size(), list.isEmpty());
    }

    /**
     * Demonstrates lower bounds with bounded wildcards
     * 
     * Lower bounds restrict the type to be a supertype of the specified type.
     * 
     * @param list the list to add numbers to
     */
    public static void processLowerBound(List<? super Integer> list) {
        log.info("Processing lower bound list with {} elements", list.size());
        
        // Can add elements
        list.add(Integer.valueOf(42));
        list.add(Integer.valueOf(100));
        list.add(Integer.valueOf(200));
        
        // Cannot read specific types (except Object)
        for (Object item : list) {
            log.debug("Processing item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Can call methods that don't depend on the type parameter
        log.debug("List size: {}, isEmpty: {}", list.size(), list.isEmpty());
    }

    /**
     * Demonstrates bounds with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number
     */
    public static <T extends Number> T findMax(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        
        log.info("Finding maximum in list with {} elements", list.size());
        
        T max = list.get(0);
        for (T item : list) {
            if (item != null && item.doubleValue() > max.doubleValue()) {
                max = item;
            }
        }
        
        log.debug("Maximum value: {}", max);
        return max;
    }

    /**
     * Demonstrates bounds with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number and Comparable
     */
    public static <T extends Number & Comparable<T>> T findMaxComparable(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        
        log.info("Finding maximum comparable in list with {} elements", list.size());
        
        T max = list.get(0);
        for (T item : list) {
            if (item != null && item.compareTo(max) > 0) {
                max = item;
            }
        }
        
        log.debug("Maximum comparable value: {}", max);
        return max;
    }

    /**
     * Demonstrates bounds with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number
     */
    public static <T extends Number> double calculateSum(List<T> list) {
        if (list == null || list.isEmpty()) {
            return 0.0;
        }
        
        log.info("Calculating sum for list with {} elements", list.size());
        
        double sum = list.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Number::doubleValue)
                .sum();
        
        log.debug("Sum: {}", sum);
        return sum;
    }

    /**
     * Demonstrates bounds with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number
     */
    public static <T extends Number> double calculateAverage(List<T> list) {
        if (list == null || list.isEmpty()) {
            return 0.0;
        }
        
        log.info("Calculating average for list with {} elements", list.size());
        
        double sum = calculateSum(list);
        double average = sum / list.size();
        
        log.debug("Average: {}", average);
        return average;
    }

    /**
     * Demonstrates bounds with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number
     */
    public static <T extends Number> List<T> filterPositive(List<T> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        
        log.info("Filtering positive numbers from list with {} elements", list.size());
        
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (item != null && item.doubleValue() > 0) {
                result.add(item);
            }
        }
        
        log.debug("Filtered {} positive numbers", result.size());
        return result;
    }

    /**
     * Demonstrates bounds with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number
     */
    public static <T extends Number> List<T> filterNegative(List<T> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        
        log.info("Filtering negative numbers from list with {} elements", list.size());
        
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (item != null && item.doubleValue() < 0) {
                result.add(item);
            }
        }
        
        log.debug("Filtered {} negative numbers", result.size());
        return result;
    }

    /**
     * Demonstrates bounds with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number
     */
    public static <T extends Number> List<T> filterZero(List<T> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        
        log.info("Filtering zero numbers from list with {} elements", list.size());
        
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (item != null && item.doubleValue() == 0) {
                result.add(item);
            }
        }
        
        log.debug("Filtered {} zero numbers", result.size());
        return result;
    }

    /**
     * Demonstrates bounds with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number
     */
    public static <T extends Number> List<T> filterRange(List<T> list, double min, double max) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        
        log.info("Filtering numbers in range [{}, {}] from list with {} elements", min, max, list.size());
        
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (item != null) {
                double value = item.doubleValue();
                if (value >= min && value <= max) {
                    result.add(item);
                }
            }
        }
        
        log.debug("Filtered {} numbers in range", result.size());
        return result;
    }

    /**
     * Demonstrates bounds with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter bounded by Number
     */
    public static <T extends Number> List<T> multiplyBy(List<T> list, double factor) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        
        log.info("Multiplying numbers by {} in list with {} elements", factor, list.size());
        
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (item != null) {
                double value = item.doubleValue() * factor;
                // Note: This is a simplified example. In practice, you'd need to handle
                // the conversion back to the original type T, which can be complex.
                log.debug("Original: {}, Multiplied: {}", item, value);
            }
        }
        
        log.debug("Processed {} numbers", result.size());
        return result;
    }
}
