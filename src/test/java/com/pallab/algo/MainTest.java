package com.pallab.algo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    @DisplayName("Main class should initialize correctly")
    void mainClassShouldInitialize() {
        Main main = new Main();
        assertNotNull(main);
    }

    @Test
    @DisplayName("Scanner input should work correctly")
    void scannerInputShouldWork() {
        Main main = new Main();
        assertTrue(main.usingScanner());
    }
}