package netflix.generics.advanced;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Netflix Production-Grade Variance Examples
 * 
 * This class demonstrates variance concepts including:
 * - Covariance (preserves subtyping)
 * - Contravariance (reverses subtyping)
 * - Invariance (no subtyping relationship)
 * - Variance in generic types
 * - Variance in method parameters
 * - Variance in return types
 * - Variance with wildcards
 * 
 * @author Netflix Java Generics Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class VarianceExamples {

    /**
     * Demonstrates covariance with arrays
     * 
     * Arrays in Java are covariant, meaning that if S is a subtype of T,
     * then S[] is a subtype of T[].
     */
    public static void demonstrateArrayCovariance() {
        log.info("Demonstrating array covariance");
        
        // Integer[] is a subtype of Number[]
        Integer[] integers = {1, 2, 3, 4, 5};
        Number[] numbers = integers; // This is allowed (covariance)
        
        // Can read from the array
        for (Number number : numbers) {
            log.debug("Number: {} (type: {})", number, number.getClass().getSimpleName());
        }
        
        // Can modify elements (but this can cause runtime errors)
        try {
            numbers[0] = 3.14; // This compiles but will cause ArrayStoreException at runtime
        } catch (ArrayStoreException e) {
            log.warn("ArrayStoreException caught: {}", e.getMessage());
        }
        
        log.info("Array covariance demonstration completed");
    }

    /**
     * Demonstrates contravariance with generic types
     * 
     * Contravariance reverses the subtyping relationship.
     * If S is a subtype of T, then List<T> is a subtype of List<S>.
     */
    public static void demonstrateGenericContravariance() {
        log.info("Demonstrating generic contravariance");
        
        // List<Number> is a subtype of List<? super Integer>
        List<Number> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2.5);
        numbers.add(3.14f);
        
        // Can pass List<Number> where List<? super Integer> is expected
        processContravariantList(numbers);
        
        log.info("Generic contravariance demonstration completed");
    }

    /**
     * Processes a contravariant list
     * 
     * @param list the list to process
     */
    private static void processContravariantList(List<? super Integer> list) {
        log.debug("Processing contravariant list with {} elements", list.size());
        
        // Can add Integer elements
        list.add(42);
        list.add(100);
        
        // Can only read as Object
        for (Object item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
    }

    /**
     * Demonstrates invariance with generic types
     * 
     * Invariance means there is no subtyping relationship between
     * generic types with different type parameters.
     */
    public static void demonstrateGenericInvariance() {
        log.info("Demonstrating generic invariance");
        
        List<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(2);
        integers.add(3);
        
        List<Number> numbers = new ArrayList<>();
        numbers.add(1.0);
        numbers.add(2.5);
        numbers.add(3.14);
        
        // These assignments are not allowed (invariance)
        // List<Number> numbersFromIntegers = integers; // Compile error!
        // List<Integer> integersFromNumbers = numbers; // Compile error!
        
        // But we can use wildcards to achieve variance
        processCovariantList(integers);
        processContravariantList(numbers);
        
        log.info("Generic invariance demonstration completed");
    }

    /**
     * Processes a covariant list
     * 
     * @param list the list to process
     */
    private static void processCovariantList(List<? extends Number> list) {
        log.debug("Processing covariant list with {} elements", list.size());
        
        // Can read from the list
        for (Number number : list) {
            log.debug("Number: {} (type: {})", number, number.getClass().getSimpleName());
        }
        
        // Cannot add elements (except null)
        // list.add(42); // Compile error!
        list.add(null); // This is allowed
    }

    /**
     * Demonstrates variance in method parameters
     * 
     * @param numbers the list of numbers to process
     */
    public static void demonstrateMethodParameterVariance(List<? extends Number> numbers) {
        log.info("Demonstrating method parameter variance with {} elements", numbers.size());
        
        // Can read from the list
        for (Number number : numbers) {
            log.debug("Number: {} (type: {})", number, number.getClass().getSimpleName());
        }
        
        // Cannot add elements (except null)
        // numbers.add(42); // Compile error!
        numbers.add(null); // This is allowed
    }

    /**
     * Demonstrates variance in return types
     * 
     * @param numbers the list of numbers to process
     * @return a list of numbers
     */
    public static List<? extends Number> demonstrateReturnTypeVariance(List<? extends Number> numbers) {
        log.info("Demonstrating return type variance with {} elements", numbers.size());
        
        // Can read from the list
        for (Number number : numbers) {
            log.debug("Number: {} (type: {})", number, number.getClass().getSimpleName());
        }
        
        // Return the same list (covariant return type)
        return numbers;
    }

    /**
     * Demonstrates variance with generic methods
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> void demonstrateGenericMethodVariance(List<? extends T> list) {
        log.info("Demonstrating generic method variance with {} elements", list.size());
        
        // Can read from the list
        for (T item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Cannot add elements (except null)
        // list.add(item); // Compile error!
        list.add(null); // This is allowed
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> List<? extends T> demonstrateGenericMethodReturnVariance(List<? extends T> list) {
        log.info("Demonstrating generic method return variance with {} elements", list.size());
        
        // Can read from the list
        for (T item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Return the same list (covariant return type)
        return list;
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> void demonstrateGenericMethodContravariance(List<? super T> list) {
        log.info("Demonstrating generic method contravariance with {} elements", list.size());
        
        // Can only read as Object
        for (Object item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Cannot add specific types
        // list.add(item); // Compile error!
        list.add(null); // This is allowed
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> List<? super T> demonstrateGenericMethodReturnContravariance(List<? super T> list) {
        log.info("Demonstrating generic method return contravariance with {} elements", list.size());
        
        // Can only read as Object
        for (Object item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Return the same list (contravariant return type)
        return list;
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> void demonstrateGenericMethodInvariance(List<T> list) {
        log.info("Demonstrating generic method invariance with {} elements", list.size());
        
        // Can read from the list
        for (T item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Can add elements
        // list.add(item); // This would work if we had an item parameter
        list.add(null); // This is allowed
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> List<T> demonstrateGenericMethodReturnInvariance(List<T> list) {
        log.info("Demonstrating generic method return invariance with {} elements", list.size());
        
        // Can read from the list
        for (T item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Return the same list (invariant return type)
        return list;
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> void demonstrateGenericMethodWildcardVariance(List<?> list) {
        log.info("Demonstrating generic method wildcard variance with {} elements", list.size());
        
        // Can only read as Object
        for (Object item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Cannot add elements (except null)
        // list.add(item); // Compile error!
        list.add(null); // This is allowed
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> List<?> demonstrateGenericMethodReturnWildcardVariance(List<?> list) {
        log.info("Demonstrating generic method return wildcard variance with {} elements", list.size());
        
        // Can only read as Object
        for (Object item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Return the same list (wildcard return type)
        return list;
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> void demonstrateGenericMethodBoundedVariance(List<? extends T> list) {
        log.info("Demonstrating generic method bounded variance with {} elements", list.size());
        
        // Can read from the list
        for (T item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Cannot add elements (except null)
        // list.add(item); // Compile error!
        list.add(null); // This is allowed
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> List<? extends T> demonstrateGenericMethodReturnBoundedVariance(List<? extends T> list) {
        log.info("Demonstrating generic method return bounded variance with {} elements", list.size());
        
        // Can read from the list
        for (T item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Return the same list (bounded return type)
        return list;
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> void demonstrateGenericMethodLowerBoundedVariance(List<? super T> list) {
        log.info("Demonstrating generic method lower bounded variance with {} elements", list.size());
        
        // Can only read as Object
        for (Object item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Cannot add specific types
        // list.add(item); // Compile error!
        list.add(null); // This is allowed
    }

    /**
     * Demonstrates variance with generic methods and type inference
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> List<? super T> demonstrateGenericMethodReturnLowerBoundedVariance(List<? super T> list) {
        log.info("Demonstrating generic method return lower bounded variance with {} elements", list.size());
        
        // Can only read as Object
        for (Object item : list) {
            log.debug("Item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Return the same list (lower bounded return type)
        return list;
    }
}
