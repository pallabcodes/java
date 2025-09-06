package netflix.generics.basic;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Netflix Production-Grade Generic Box Class
 * 
 * This class demonstrates basic generics concepts including:
 * - Type parameters and generic classes
 * - Type safety and compile-time checking
 * - Generic methods and constructors
 * - Multiple type parameters
 * - Bounded type parameters
 * 
 * @param <T> the type of elements stored in this box
 * @author Netflix Java Generics Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Data
public class GenericBox<T> {
    
    private T content;
    private final String id;
    private final long timestamp;
    
    /**
     * Constructs a new GenericBox with the specified content
     * 
     * @param content the content to store in this box
     */
    public GenericBox(T content) {
        this.content = content;
        this.id = generateId();
        this.timestamp = System.currentTimeMillis();
        log.debug("Created GenericBox with content type: {}", 
                 content != null ? content.getClass().getSimpleName() : "null");
    }
    
    /**
     * Constructs an empty GenericBox
     */
    public GenericBox() {
        this.content = null;
        this.id = generateId();
        this.timestamp = System.currentTimeMillis();
        log.debug("Created empty GenericBox");
    }
    
    /**
     * Sets the content of this box
     * 
     * @param content the new content
     */
    public void setContent(T content) {
        log.debug("Setting content of type: {}", 
                 content != null ? content.getClass().getSimpleName() : "null");
        this.content = content;
    }
    
    /**
     * Gets the content of this box
     * 
     * @return the content
     */
    public T getContent() {
        log.debug("Getting content of type: {}", 
                 content != null ? content.getClass().getSimpleName() : "null");
        return content;
    }
    
    /**
     * Checks if this box is empty
     * 
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return content == null;
    }
    
    /**
     * Clears the content of this box
     */
    public void clear() {
        log.debug("Clearing content from GenericBox");
        this.content = null;
    }
    
    /**
     * Creates a copy of this box with the same content
     * 
     * @return a new GenericBox with the same content
     */
    public GenericBox<T> copy() {
        log.debug("Creating copy of GenericBox");
        return new GenericBox<>(this.content);
    }
    
    /**
     * Swaps the content of this box with another box
     * 
     * @param other the other box to swap with
     */
    public void swap(GenericBox<T> other) {
        if (other == null) {
            throw new IllegalArgumentException("Other box cannot be null");
        }
        
        log.debug("Swapping content between GenericBoxes");
        T temp = this.content;
        this.content = other.content;
        other.content = temp;
    }
    
    /**
     * Generic method to create a box from any type
     * 
     * @param content the content to wrap
     * @param <U> the type of the content
     * @return a new GenericBox containing the content
     */
    public static <U> GenericBox<U> of(U content) {
        log.debug("Creating GenericBox from content of type: {}", 
                 content != null ? content.getClass().getSimpleName() : "null");
        return new GenericBox<>(content);
    }
    
    /**
     * Generic method to create an empty box of any type
     * 
     * @param <U> the type of the box
     * @return a new empty GenericBox
     */
    public static <U> GenericBox<U> empty() {
        log.debug("Creating empty GenericBox");
        return new GenericBox<>();
    }
    
    /**
     * Generic method to create a box from a supplier
     * 
     * @param supplier the supplier to get content from
     * @param <U> the type of the content
     * @return a new GenericBox containing the supplied content
     */
    public static <U> GenericBox<U> from(java.util.function.Supplier<U> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }
        
        log.debug("Creating GenericBox from supplier");
        return new GenericBox<>(supplier.get());
    }
    
    /**
     * Generic method to transform the content of this box
     * 
     * @param transformer the function to transform the content
     * @param <U> the type of the transformed content
     * @return a new GenericBox with the transformed content
     */
    public <U> GenericBox<U> map(java.util.function.Function<T, U> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("Transformer cannot be null");
        }
        
        log.debug("Transforming content of GenericBox");
        U transformedContent = this.content != null ? transformer.apply(this.content) : null;
        return new GenericBox<>(transformedContent);
    }
    
    /**
     * Generic method to filter the content of this box
     * 
     * @param predicate the predicate to test the content
     * @return a new GenericBox with the content if it passes the predicate, empty otherwise
     */
    public GenericBox<T> filter(java.util.function.Predicate<T> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("Predicate cannot be null");
        }
        
        log.debug("Filtering content of GenericBox");
        if (this.content != null && predicate.test(this.content)) {
            return new GenericBox<>(this.content);
        } else {
            return new GenericBox<>();
        }
    }
    
    /**
     * Generic method to perform an action on the content
     * 
     * @param action the action to perform
     */
    public void ifPresent(java.util.function.Consumer<T> action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        
        if (this.content != null) {
            log.debug("Performing action on content of GenericBox");
            action.accept(this.content);
        }
    }
    
    /**
     * Generic method to get the content or a default value
     * 
     * @param defaultValue the default value to return if content is null
     * @return the content or the default value
     */
    public T orElse(T defaultValue) {
        log.debug("Getting content or default value from GenericBox");
        return this.content != null ? this.content : defaultValue;
    }
    
    /**
     * Generic method to get the content or throw an exception
     * 
     * @param exceptionSupplier the supplier for the exception to throw
     * @param <X> the type of the exception
     * @return the content
     * @throws X if content is null
     */
    public <X extends Throwable> T orElseThrow(java.util.function.Supplier<X> exceptionSupplier) throws X {
        if (exceptionSupplier == null) {
            throw new IllegalArgumentException("Exception supplier cannot be null");
        }
        
        if (this.content != null) {
            log.debug("Getting content from GenericBox");
            return this.content;
        } else {
            log.debug("Throwing exception from GenericBox");
            throw exceptionSupplier.get();
        }
    }
    
    /**
     * Generates a unique ID for this box
     * 
     * @return a unique ID
     */
    private String generateId() {
        return "box-" + System.nanoTime() + "-" + Thread.currentThread().getId();
    }
    
    @Override
    public String toString() {
        return String.format("GenericBox{id='%s', content=%s, timestamp=%d}", 
                           id, content, timestamp);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GenericBox<?> other = (GenericBox<?>) obj;
        return id != null ? id.equals(other.id) : other.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
