package com.algorithmpractice.algorithms.sorting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for QuickSort implementation.
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
@DisplayName("QuickSort Tests")
class QuickSortTest {

    @Nested
    @DisplayName("Basic Sorting Tests")
    class BasicSortingTests {

        @Test
        @DisplayName("Should sort an empty array")
        void shouldSortEmptyArray() {
            // Given
            final int[] array = {};
            
            // When
            QuickSort.sort(array);
            
            // Then
            assertThat(array).isEmpty();
        }

        @Test
        @DisplayName("Should sort a single element array")
        void shouldSortSingleElementArray() {
            // Given
            final int[] array = {42};
            
            // When
            QuickSort.sort(array);
            
            // Then
            assertThat(array).containsExactly(42);
        }

        @Test
        @DisplayName("Should sort a two element array")
        void shouldSortTwoElementArray() {
            // Given
            final int[] array = {5, 2};
            
            // When
            QuickSort.sort(array);
            
            // Then
            assertThat(array).containsExactly(2, 5);
        }

        @Test
        @DisplayName("Should sort already sorted array")
        void shouldSortAlreadySortedArray() {
            // Given
            final int[] array = {1, 2, 3, 4, 5};
            
            // When
            QuickSort.sort(array);
            
            // Then
            assertThat(array).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should sort reverse sorted array")
        void shouldSortReverseSortedArray() {
            // Given
            final int[] array = {5, 4, 3, 2, 1};
            
            // When
            QuickSort.sort(array);
            
            // Then
            assertThat(array).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should sort array with duplicate elements")
        void shouldSortArrayWithDuplicates() {
            // Given
            final int[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
            
            // When
            QuickSort.sort(array);
            
            // Then
            assertThat(array).containsExactly(1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9);
        }

        @Test
        @DisplayName("Should sort array with negative numbers")
        void shouldSortArrayWithNegativeNumbers() {
            // Given
            final int[] array = {-5, 10, -3, 0, 7, -1};
            
            // When
            QuickSort.sort(array);
            
            // Then
            assertThat(array).containsExactly(-5, -3, -1, 0, 7, 10);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should throw exception for null array")
        void shouldThrowExceptionForNullArray() {
            assertThatThrownBy(() -> QuickSort.sort(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Array cannot be null");
        }

        @Test
        @DisplayName("Should handle array with all same elements")
        void shouldHandleArrayWithAllSameElements() {
            // Given
            final int[] array = {7, 7, 7, 7, 7};
            
            // When
            QuickSort.sort(array);
            
            // Then
            assertThat(array).containsExactly(7, 7, 7, 7, 7);
        }

        @Test
        @DisplayName("Should handle large array")
        void shouldHandleLargeArray() {
            // Given
            final int[] array = new int[1000];
            for (int i = 0; i < array.length; i++) {
                array[i] = array.length - i;
            }
            
            // When
            QuickSort.sort(array);
            
            // Then
            assertThat(array).isSorted();
        }
    }

    @Nested
    @DisplayName("Sort Copy Tests")
    class SortCopyTests {

        @Test
        @DisplayName("Should return new sorted array without modifying original")
        void shouldReturnNewSortedArrayWithoutModifyingOriginal() {
            // Given
            final int[] original = {5, 2, 8, 1, 9};
            final int[] originalCopy = Arrays.copyOf(original, original.length);
            
            // When
            final int[] sorted = QuickSort.sortCopy(original);
            
            // Then
            assertThat(original).isEqualTo(originalCopy); // Original unchanged
            assertThat(sorted).isSorted();
            assertThat(sorted).containsExactly(1, 2, 5, 8, 9);
        }

        @Test
        @DisplayName("Should throw exception for null array in sortCopy")
        void shouldThrowExceptionForNullArrayInSortCopy() {
            assertThatThrownBy(() -> QuickSort.sortCopy(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Array cannot be null");
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle worst case scenario efficiently")
        void shouldHandleWorstCaseScenarioEfficiently() {
            // Given - Create array that could trigger worst case for naive pivot selection
            final int[] array = new int[1000];
            for (int i = 0; i < array.length; i++) {
                array[i] = i; // Already sorted - tests median-of-three pivot selection
            }
            
            // When
            final long startTime = System.nanoTime();
            QuickSort.sort(array);
            final long endTime = System.nanoTime();
            
            // Then
            final long duration = endTime - startTime;
            assertThat(array).isSorted();
            assertThat(duration).isLessThan(10_000_000); // Should complete in < 10ms
        }
    }

    @ParameterizedTest
    @MethodSource("provideArraysForSorting")
    @DisplayName("Should sort various arrays correctly")
    void shouldSortVariousArraysCorrectly(final int[] input, final int[] expected) {
        // When
        QuickSort.sort(input);
        
        // Then
        assertThat(input).containsExactly(expected);
    }

    private static Stream<Arguments> provideArraysForSorting() {
        return Stream.of(
                Arguments.of(new int[]{}, new int[]{}),
                Arguments.of(new int[]{1}, new int[]{1}),
                Arguments.of(new int[]{2, 1}, new int[]{1, 2}),
                Arguments.of(new int[]{3, 1, 4, 1, 5}, new int[]{1, 1, 3, 4, 5}),
                Arguments.of(new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}),
                Arguments.of(new int[]{-1, -5, -10, 0, 5, 10}, new int[]{-10, -5, -1, 0, 5, 10})
        );
    }
}
