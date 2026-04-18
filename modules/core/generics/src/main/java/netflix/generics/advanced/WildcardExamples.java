package netflix.generics.advanced;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import netflix.generics.basic.GenericBox;

/**
 * Netflix Production-Grade Wildcard Examples
 * 
 * This class demonstrates advanced wildcard concepts including:
 * - Unbounded wildcards (?)
 * - Upper-bounded wildcards (? extends T)
 * - Lower-bounded wildcards (? super T)
 * - Wildcard capture and helper methods
 * - PECS principle (Producer Extends, Consumer Super)
 * - Wildcard restrictions and limitations
 * 
 * @author Netflix Java Generics Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class WildcardExamples {

    /**
     * Demonstrates unbounded wildcards
     * 
     * Unbounded wildcards are useful when you don't care about the type
     * and only need to call methods that don't depend on the type parameter.
     * 
     * @param list the list to process
     */
    public static void processUnboundedWildcard(List<?> list) {
        log.info("Processing unbounded wildcard list with {} elements", list.size());
        
        // Can only call methods that don't depend on the type parameter
        for (Object item : list) {
            log.debug("Processing item: {}", item);
        }
        
        // Cannot add elements (except null)
        // list.add("test"); // Compile error!
        list.add(null); // This is allowed
        
        // Can call size(), isEmpty(), clear(), etc.
        log.debug("List size: {}, isEmpty: {}", list.size(), list.isEmpty());
    }

    /**
     * Demonstrates upper-bounded wildcards
     * 
     * Upper-bounded wildcards allow you to read from the collection
     * but restrict what you can add to it.
     * 
     * @param numbers the list of numbers to process
     */
    public static void processUpperBoundedWildcard(List<? extends Number> numbers) {
        log.info("Processing upper-bounded wildcard list with {} elements", numbers.size());
        
        // Can read from the collection
        for (Number number : numbers) {
            log.debug("Processing number: {} (type: {})", number, number.getClass().getSimpleName());
        }
        
        // Cannot add elements (except null)
        // numbers.add(42); // Compile error!
        // numbers.add(3.14); // Compile error!
        numbers.add(null); // This is allowed
        
        // Can call methods on the elements
        double sum = numbers.stream()
                .filter(n -> n != null)
                .mapToDouble(Number::doubleValue)
                .sum();
        
        log.info("Sum of numbers: {}", sum);
    }

    /**
     * Demonstrates lower-bounded wildcards
     * 
     * Lower-bounded wildcards allow you to add elements to the collection
     * but restrict what you can read from it.
     * 
     * @param numbers the list to add numbers to
     */
    public static void processLowerBoundedWildcard(List<? super Integer> numbers) {
        log.info("Processing lower-bounded wildcard list with {} elements", numbers.size());
        
        // Can add elements
        numbers.add(42);
        numbers.add(100);
        numbers.add(200);
        
        // Cannot read specific types (except Object)
        for (Object item : numbers) {
            log.debug("Processing item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Can call methods that don't depend on the type parameter
        log.debug("List size: {}, isEmpty: {}", numbers.size(), numbers.isEmpty());
    }

    /**
     * Demonstrates the PECS principle (Producer Extends, Consumer Super)
     * 
     * This method shows how to properly use wildcards when:
     * - Reading from a collection (Producer Extends)
     * - Writing to a collection (Consumer Super)
     * 
     * @param source the source list to read from
     * @param destination the destination list to write to
     */
    public static <T> void copyWithPECS(List<? extends T> source, List<? super T> destination) {
        log.info("Copying {} elements using PECS principle", source.size());
        
        // Producer Extends: can read from source
        for (T item : source) {
            log.debug("Reading item: {}", item);
            // Consumer Super: can write to destination
            destination.add(item);
        }
        
        log.info("Copied {} elements to destination", destination.size());
    }

    /**
     * Demonstrates wildcard capture
     * 
     * Wildcard capture allows you to work with wildcard types
     * by capturing them in a generic method.
     * 
     * @param list the list to process
     */
    public static void processWildcardCapture(List<?> list) {
        log.info("Processing wildcard capture with {} elements", list.size());
        
        // Use a helper method to capture the wildcard
        processWildcardCaptureHelper(list);
    }

    /**
     * Helper method for wildcard capture
     * 
     * @param list the list to process
     * @param <T> the captured type
     */
    private static <T> void processWildcardCaptureHelper(List<T> list) {
        log.debug("Processing captured wildcard type: {}", 
                 list.isEmpty() ? "unknown" : list.get(0).getClass().getSimpleName());
        
        // Now we can work with the specific type T
        for (T item : list) {
            log.debug("Processing captured item: {} (type: {})", item, item.getClass().getSimpleName());
        }
    }

    /**
     * Demonstrates wildcard restrictions
     * 
     * This method shows what you can and cannot do with wildcards.
     * 
     * @param list the list to demonstrate restrictions on
     */
    public static void demonstrateWildcardRestrictions(List<?> list) {
        log.info("Demonstrating wildcard restrictions with {} elements", list.size());
        
        // Allowed operations
        log.debug("Size: {}", list.size());
        log.debug("Is empty: {}", list.isEmpty());
        list.clear();
        list.add(null);
        
        // Not allowed operations (commented out to avoid compile errors)
        // list.add("test"); // Compile error!
        // list.add(42); // Compile error!
        // T item = list.get(0); // Compile error!
        
        // Workaround: use wildcard capture
        processWildcardCaptureHelper(list);
    }

    /**
     * Demonstrates wildcard with generic methods
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> void processWithGenericMethod(List<? extends T> list) {
        log.info("Processing with generic method, list size: {}", list.size());
        
        // Can read from the list
        for (T item : list) {
            log.debug("Processing item: {}", item);
        }
        
        // Can call generic methods
        List<T> result = new ArrayList<>();
        copyWithPECS(list, result);
        
        log.info("Processed {} items", result.size());
    }

    /**
     * Demonstrates wildcard with multiple bounds
     * 
     * @param list the list to process
     * @param <T> the type parameter with multiple bounds
     */
    public static <T extends Number & Comparable<Number>> void processMultipleBounds(List<T> list) {
       log.info("Processing multiple bounds wildcard with {} elements", list.size());
        
        // Can read from the list and call methods from both bounds
        for (Number number : list) {
            log.debug("Processing number: {} (type: {})", number, number.getClass().getSimpleName());
            
            // Can call Number methods
            double value = number.doubleValue();
            log.debug("Double value: {}", value);
            
            // Can call Comparable methods if the type implements it
            if (list.size() > 1 && number instanceof Comparable) {
                Number other = list.get(0);
                @SuppressWarnings("unchecked")
                Comparable<Number> comparable = (Comparable<Number>) number;
                if (comparable.compareTo(other) > 0) {
                    log.debug("{} is greater than {}", number, other);
                }
            }
        }
    }

    /**
     * Demonstrates wildcard with arrays
     * 
     * @param array the array to process
     */
    public static void processWildcardArray(Number[] array) {
        log.info("Processing wildcard array with {} elements", array.length);
        
        // Can read from the array
        for (Number number : array) {
            log.debug("Processing number: {} (type: {})", number, number.getClass().getSimpleName());
        }
        
        // Can modify elements (unlike collections)
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                array[i] = array[i].doubleValue() * 2;
            }
        }
        
        log.info("Processed wildcard array");
    }

    /**
     * Demonstrates wildcard with collections and iterators
     * 
     * @param collection the collection to process
     */
    public static void processWildcardCollection(Collection<? extends Number> collection) {
        log.info("Processing wildcard collection with {} elements", collection.size());
        
        // Can use iterator
        Iterator<? extends Number> iterator = collection.iterator();
        while (iterator.hasNext()) {
            Number number = iterator.next();
            log.debug("Processing number: {} (type: {})", number, number.getClass().getSimpleName());
        }
        
        // Can use enhanced for loop
        for (Number number : collection) {
            log.debug("Processing number: {} (type: {})", number, number.getClass().getSimpleName());
        }
        
        // Can use stream
        double sum = collection.stream()
                .filter(n -> n != null)
                .mapToDouble(Number::doubleValue)
                .sum();
        
        log.info("Sum of numbers: {}", sum);
    }

    /**
     * Demonstrates wildcard with generic classes
     * 
     * @param box the generic box to process
     */
    public static void processWildcardGenericBox(GenericBox<? extends Number> box) {
        log.info("Processing wildcard generic box");
        
        // Can read from the box
        Number content = box.getContent();
        if (content != null) {
            log.debug("Box content: {} (type: {})", content, content.getClass().getSimpleName());
        }
        
        // Cannot set content (except null)
        // box.setContent(42); // Compile error!
        box.setContent(null); // This is allowed
        
        // Can call other methods
        log.debug("Box is empty: {}", box.isEmpty());
    }

    /**
     * Demonstrates wildcard with generic methods
     * 
     * @param list the list to process
     * @param <T> the type parameter
     */
    public static <T> void processWildcardGenericMethod(List<? extends T> list) {
        log.info("Processing wildcard generic method with {} elements", list.size());
        
        // Can read from the list
        for (T item : list) {
            log.debug("Processing item: {} (type: {})", item, item.getClass().getSimpleName());
        }
        
        // Can call other generic methods
        List<T> result = new ArrayList<>();
        copyWithPECS(list, result);
        
        log.info("Processed {} items", result.size());
    }
}
