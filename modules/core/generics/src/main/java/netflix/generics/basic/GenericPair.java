package netflix.generics.basic;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Netflix Production-Grade Generic Pair Class
 * 
 * This class demonstrates multiple type parameters and generic methods
 * with comprehensive type safety and utility methods.
 * 
 * @param <T> the type of the first element
 * @param <U> the type of the second element
 * @author Netflix Java Generics Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Data
public class GenericPair<T, U> {
    
    private T first;
    private U second;
    private final String id;
    private final long timestamp;
    
    /**
     * Constructs a new GenericPair with the specified elements
     * 
     * @param first the first element
     * @param second the second element
     */
    public GenericPair(T first, U second) {
        this.first = first;
        this.second = second;
        this.id = generateId();
        this.timestamp = System.currentTimeMillis();
        log.debug("Created GenericPair with types: {} and {}", 
                 first != null ? first.getClass().getSimpleName() : "null",
                 second != null ? second.getClass().getSimpleName() : "null");
    }
    
    /**
     * Constructs an empty GenericPair
     */
    public GenericPair() {
        this.first = null;
        this.second = null;
        this.id = generateId();
        this.timestamp = System.currentTimeMillis();
        log.debug("Created empty GenericPair");
    }
    
    /**
     * Sets the first element
     * 
     * @param first the new first element
     */
    public void setFirst(T first) {
        log.debug("Setting first element of type: {}", 
                 first != null ? first.getClass().getSimpleName() : "null");
        this.first = first;
    }
    
    /**
     * Sets the second element
     * 
     * @param second the new second element
     */
    public void setSecond(U second) {
        log.debug("Setting second element of type: {}", 
                 second != null ? second.getClass().getSimpleName() : "null");
        this.second = second;
    }
    
    /**
     * Gets the first element
     * 
     * @return the first element
     */
    public T getFirst() {
        log.debug("Getting first element of type: {}", 
                 first != null ? first.getClass().getSimpleName() : "null");
        return first;
    }
    
    /**
     * Gets the second element
     * 
     * @return the second element
     */
    public U getSecond() {
        log.debug("Getting second element of type: {}", 
                 second != null ? second.getClass().getSimpleName() : "null");
        return second;
    }
    
    /**
     * Checks if both elements are present
     * 
     * @return true if both elements are not null, false otherwise
     */
    public boolean isComplete() {
        return first != null && second != null;
    }
    
    /**
     * Checks if the pair is empty
     * 
     * @return true if both elements are null, false otherwise
     */
    public boolean isEmpty() {
        return first == null && second == null;
    }
    
    /**
     * Clears both elements
     */
    public void clear() {
        log.debug("Clearing both elements from GenericPair");
        this.first = null;
        this.second = null;
    }
    
    /**
     * Swaps the elements of this pair
     */
    public void swap() {
        log.debug("Swapping elements in GenericPair");
        T temp = this.first;
        this.first = (T) this.second; // This is a type-unsafe operation for demonstration
        this.second = (U) temp;
    }
    
    /**
     * Creates a copy of this pair
     * 
     * @return a new GenericPair with the same elements
     */
    public GenericPair<T, U> copy() {
        log.debug("Creating copy of GenericPair");
        return new GenericPair<>(this.first, this.second);
    }
    
    /**
     * Generic method to create a pair from two elements
     * 
     * @param first the first element
     * @param second the second element
     * @param <T> the type of the first element
     * @param <U> the type of the second element
     * @return a new GenericPair containing the elements
     */
    public static <T, U> GenericPair<T, U> of(T first, U second) {
        log.debug("Creating GenericPair from elements of types: {} and {}", 
                 first != null ? first.getClass().getSimpleName() : "null",
                 second != null ? second.getClass().getSimpleName() : "null");
        return new GenericPair<>(first, second);
    }
    
    /**
     * Generic method to create an empty pair
     * 
     * @param <T> the type of the first element
     * @param <U> the type of the second element
     * @return a new empty GenericPair
     */
    public static <T, U> GenericPair<T, U> empty() {
        log.debug("Creating empty GenericPair");
        return new GenericPair<>();
    }
    
    /**
     * Generic method to create a pair from a supplier
     * 
     * @param firstSupplier the supplier for the first element
     * @param secondSupplier the supplier for the second element
     * @param <T> the type of the first element
     * @param <U> the type of the second element
     * @return a new GenericPair containing the supplied elements
     */
    public static <T, U> GenericPair<T, U> from(java.util.function.Supplier<T> firstSupplier,
                                               java.util.function.Supplier<U> secondSupplier) {
        if (firstSupplier == null || secondSupplier == null) {
            throw new IllegalArgumentException("Suppliers cannot be null");
        }
        
        log.debug("Creating GenericPair from suppliers");
        return new GenericPair<>(firstSupplier.get(), secondSupplier.get());
    }
    
    /**
     * Generic method to transform the first element
     * 
     * @param transformer the function to transform the first element
     * @param <V> the type of the transformed first element
     * @return a new GenericPair with the transformed first element
     */
    public <V> GenericPair<V, U> mapFirst(java.util.function.Function<T, V> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("Transformer cannot be null");
        }
        
        log.debug("Transforming first element of GenericPair");
        V transformedFirst = this.first != null ? transformer.apply(this.first) : null;
        return new GenericPair<>(transformedFirst, this.second);
    }
    
    /**
     * Generic method to transform the second element
     * 
     * @param transformer the function to transform the second element
     * @param <V> the type of the transformed second element
     * @return a new GenericPair with the transformed second element
     */
    public <V> GenericPair<T, V> mapSecond(java.util.function.Function<U, V> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("Transformer cannot be null");
        }
        
        log.debug("Transforming second element of GenericPair");
        V transformedSecond = this.second != null ? transformer.apply(this.second) : null;
        return new GenericPair<>(this.first, transformedSecond);
    }
    
    /**
     * Generic method to transform both elements
     * 
     * @param firstTransformer the function to transform the first element
     * @param secondTransformer the function to transform the second element
     * @param <V> the type of the transformed first element
     * @param <W> the type of the transformed second element
     * @return a new GenericPair with both transformed elements
     */
    public <V, W> GenericPair<V, W> map(java.util.function.Function<T, V> firstTransformer,
                                       java.util.function.Function<U, W> secondTransformer) {
        if (firstTransformer == null || secondTransformer == null) {
            throw new IllegalArgumentException("Transformers cannot be null");
        }
        
        log.debug("Transforming both elements of GenericPair");
        V transformedFirst = this.first != null ? firstTransformer.apply(this.first) : null;
        W transformedSecond = this.second != null ? secondTransformer.apply(this.second) : null;
        return new GenericPair<>(transformedFirst, transformedSecond);
    }
    
    /**
     * Generic method to filter the pair based on both elements
     * 
     * @param predicate the predicate to test both elements
     * @return a new GenericPair with the elements if they pass the predicate, empty otherwise
     */
    public GenericPair<T, U> filter(java.util.function.BiPredicate<T, U> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("Predicate cannot be null");
        }
        
        log.debug("Filtering GenericPair");
        if (this.first != null && this.second != null && predicate.test(this.first, this.second)) {
            return new GenericPair<>(this.first, this.second);
        } else {
            return new GenericPair<>();
        }
    }
    
    /**
     * Generic method to perform an action on both elements
     * 
     * @param action the action to perform on both elements
     */
    public void ifPresent(java.util.function.BiConsumer<T, U> action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        
        if (this.first != null && this.second != null) {
            log.debug("Performing action on both elements of GenericPair");
            action.accept(this.first, this.second);
        }
    }
    
    /**
     * Generic method to get the first element or a default value
     * 
     * @param defaultValue the default value to return if first element is null
     * @return the first element or the default value
     */
    public T getFirstOrElse(T defaultValue) {
        log.debug("Getting first element or default value from GenericPair");
        return this.first != null ? this.first : defaultValue;
    }
    
    /**
     * Generic method to get the second element or a default value
     * 
     * @param defaultValue the default value to return if second element is null
     * @return the second element or the default value
     */
    public U getSecondOrElse(U defaultValue) {
        log.debug("Getting second element or default value from GenericPair");
        return this.second != null ? this.second : defaultValue;
    }
    
    /**
     * Generates a unique ID for this pair
     * 
     * @return a unique ID
     */
    private String generateId() {
        return "pair-" + System.nanoTime() + "-" + Thread.currentThread().getId();
    }
    
    @Override
    public String toString() {
        return String.format("GenericPair{id='%s', first=%s, second=%s, timestamp=%d}", 
                           id, first, second, timestamp);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GenericPair<?, ?> other = (GenericPair<?, ?>) obj;
        return id != null ? id.equals(other.id) : other.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
