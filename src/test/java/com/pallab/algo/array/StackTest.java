package com.pallab.algo.array;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StackTest {
    @Test
    void stackOperationsShouldWork() {
        Stack stack = new Stack();
        stack.push(1);
        assertEquals(1, stack.size());
        assertEquals(1, stack.pop());
        assertEquals(0, stack.size());
    }
}