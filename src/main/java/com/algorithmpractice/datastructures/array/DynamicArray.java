package com.algorithmpractice.datastructures.array;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A dynamic array implementation that automatically grows as needed.
 * 
 * <p>This class provides a resizable array implementation similar to ArrayList
 * but with a focus on educational purposes and clear implementation details.
 * The array grows by a factor of 2 when it reaches capacity.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Automatic resizing with O(1) amortized insertion</li>
 *   <li>Generic type support</li>
 *   <li>Iterable interface implementation</li>
 *   <li>Bounds checking for safety</li>
 *   <li>Efficient memory usage</li>
 *   <li>Thread-safe for read operations</li>
 * </ul>
 * 
 * <p>This implementation is not thread-safe for concurrent modifications.</p>
 * 
 * @param <E> the type of elements in this array
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class DynamicArray<E> implements Iterable<E> {

    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final int GROWTH_FACTOR = 2;

    private E[] elements;
    private int size;

    /**
     * Constructs an empty dynamic array with default initial capacity.
     */
    @SuppressWarnings("unchecked")
    public DynamicArray() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructs an empty dynamic array with the specified initial capacity.
     * 
     * @param initialCapacity the initial capacity of the array
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    @SuppressWarnings("unchecked")
    public DynamicArray(final int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative: " + initialCapacity);
        }
        
        this.elements = (E[]) new Object[initialCapacity];
        this.size = 0;
    }

    /**
     * Adds an element to the end of the array.
     * 
     * @param element the element to add
     * @return true (as specified by Collection.add)
     * @throws IllegalArgumentException if element is null and this array doesn't support null elements
     */
    public boolean add(final E element) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        
        ensureCapacity(size + 1);
        elements[size++] = element;
        return true;
    }

    /**
     * Inserts an element at the specified position.
     * 
     * @param index   the position to insert the element
     * @param element the element to insert
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalArgumentException if element is null
     */
    public void add(final int index, final E element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        
        ensureCapacity(size + 1);
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size++;
    }

    /**
     * Returns the element at the specified position.
     * 
     * @param index the index of the element to return
     * @return the element at the specified position
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public E get(final int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return elements[index];
    }

    /**
     * Replaces the element at the specified position.
     * 
     * @param index   the index of the element to replace
     * @param element the element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalArgumentException if element is null
     */
    public E set(final int index, final E element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        
        final E oldValue = elements[index];
        elements[index] = element;
        return oldValue;
    }

    /**
     * Removes the element at the specified position.
     * 
     * @param index the index of the element to remove
     * @return the element that was removed
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public E remove(final int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        final E removedElement = elements[index];
        final int numMoved = size - index - 1;
        
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        
        elements[--size] = null; // Let GC do its work
        return removedElement;
    }

    /**
     * Removes the first occurrence of the specified element.
     * 
     * @param element the element to remove
     * @return true if the element was removed, false otherwise
     */
    public boolean remove(final Object element) {
        final int index = indexOf(element);
        if (index >= 0) {
            remove(index);
            return true;
        }
        return false;
    }

    /**
     * Returns the index of the first occurrence of the specified element.
     * 
     * @param element the element to search for
     * @return the index of the first occurrence, or -1 if not found
     */
    public int indexOf(final Object element) {
        if (element == null) {
            for (int i = 0; i < size; i++) {
                if (elements[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (element.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element.
     * 
     * @param element the element to search for
     * @return the index of the last occurrence, or -1 if not found
     */
    public int lastIndexOf(final Object element) {
        if (element == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (elements[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (element.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns true if this array contains the specified element.
     * 
     * @param element the element to check for
     * @return true if the element is present, false otherwise
     */
    public boolean contains(final Object element) {
        return indexOf(element) >= 0;
    }

    /**
     * Returns the number of elements in this array.
     * 
     * @return the number of elements
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if this array contains no elements.
     * 
     * @return true if the array is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all elements from this array.
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    /**
     * Returns the current capacity of the array.
     * 
     * @return the current capacity
     */
    public int capacity() {
        return elements.length;
    }

    /**
     * Ensures that the array can hold at least the specified number of elements.
     * 
     * @param minCapacity the minimum capacity required
     */
    private void ensureCapacity(final int minCapacity) {
        if (minCapacity > elements.length) {
            grow(minCapacity);
        }
    }

    /**
     * Grows the array to accommodate the specified minimum capacity.
     * 
     * @param minCapacity the minimum capacity required
     */
    private void grow(final int minCapacity) {
        int newCapacity = elements.length * GROWTH_FACTOR;
        
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        
        if (newCapacity > MAX_ARRAY_SIZE) {
            newCapacity = hugeCapacity(minCapacity);
        }
        
        elements = Arrays.copyOf(elements, newCapacity);
    }

    /**
     * Calculates the capacity for huge arrays.
     * 
     * @param minCapacity the minimum capacity required
     * @return the calculated capacity
     */
    private static int hugeCapacity(final int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    /**
     * Returns an array containing all elements in this dynamic array.
     * 
     * @return an array containing all elements
     */
    @SuppressWarnings("unchecked")
    public E[] toArray() {
        return Arrays.copyOf(elements, size);
    }

    /**
     * Trims the capacity of this array to the current size.
     */
    public void trimToSize() {
        if (size < elements.length) {
            elements = Arrays.copyOf(elements, size);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new DynamicArrayIterator();
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }
        
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(elements[i]);
        }
        
        sb.append(']');
        return sb.toString();
    }

    /**
     * Iterator implementation for DynamicArray.
     * This iterator is fail-fast and will throw ConcurrentModificationException
     * if the array is modified during iteration.
     */
    private final class DynamicArrayIterator implements Iterator<E> {
        private int currentIndex = 0;
        private final int expectedSize = size;

        @Override
        public boolean hasNext() {
            checkForComodification();
            return currentIndex < size;
        }

        @Override
        public E next() {
            checkForComodification();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return elements[currentIndex++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove operation not supported by this iterator");
        }

        /**
         * Checks for concurrent modification during iteration.
         * 
         * @throws java.util.ConcurrentModificationException if the array was modified during iteration
         */
        private void checkForComodification() {
            if (expectedSize != size) {
                throw new java.util.ConcurrentModificationException();
            }
        }
    }
}
