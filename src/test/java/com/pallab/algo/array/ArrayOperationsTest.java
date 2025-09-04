package com.pallab.algo.array;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class ArrayOperationsTest {
    @Test
    @DisplayName("Array reverse should work correctly")
    void reverseArrayShouldWork() {
        int[] input = {1, 2, 3, 4, 5};
        int[] expected = {5, 4, 3, 2, 1};
        assertArrayEquals(expected, ArrayOperations.reverse(input));
    }
}