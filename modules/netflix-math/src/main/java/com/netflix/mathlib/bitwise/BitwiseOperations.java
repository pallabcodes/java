/*
 * Copyright 2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.w3.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.mathlib.bitwise;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Bitwise Operations - Production-grade bit manipulation and number system operations.
 *
 * This class provides comprehensive bitwise operations including:
 * - Basic bitwise operations (AND, OR, XOR, NOT, shifts)
 * - Bit manipulation algorithms
 * - Number system conversions (binary, decimal, hexadecimal, octal)
 * - Bit counting and analysis
 * - Gray code operations
 * - Bitwise algorithms for optimization
 * - Hamming distance calculations
 * - Bit pattern recognition
 *
 * All implementations are optimized for performance and production use with:
 * - Comprehensive input validation
 * - Performance monitoring and metrics
 * - Memory-efficient algorithms
 * - Extensive error handling
 * - Detailed logging and debugging support
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class BitwiseOperations implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(BitwiseOperations.class);
    private static final String OPERATION_NAME = "BitwiseOperations";
    private static final String COMPLEXITY = "O(various)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;

    // Bit manipulation constants
    private static final int BITS_PER_BYTE = 8;
    private static final int BITS_PER_INT = 32;
    private static final int BITS_PER_LONG = 64;

    // Hexadecimal character lookup
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7',
                                             '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Constructor for Bitwise Operations.
     */
    public BitwiseOperations() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Bitwise Operations module");
    }

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public String getComplexity() {
        return COMPLEXITY;
    }

    @Override
    public OperationMetrics getMetrics() {
        return metrics;
    }

    @Override
    public void validateInputs(Object... inputs) {
        if (inputs == null || inputs.length == 0) {
            throw ValidationException.nullParameter("inputs", OPERATION_NAME);
        }

        for (Object input : inputs) {
            if (input == null) {
                throw ValidationException.nullParameter("input", OPERATION_NAME);
            }
        }
    }

    @Override
    public boolean isThreadSafe() {
        return THREAD_SAFE;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    // ===== BASIC BITWISE OPERATIONS =====

    /**
     * Perform bitwise AND operation.
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     *
     * @param a first operand
     * @param b second operand
     * @return a & b
     */
    public long bitwiseAnd(long a, long b) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(a, b);
            long result = a & b;

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Bitwise AND: {} & {} = {}", a, b, result);

            return result;
        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error performing bitwise AND: {}", e.getMessage());
            throw new ValidationException("Failed to perform bitwise AND: " + e.getMessage(), OPERATION_NAME, a, b);
        }
    }

    /**
     * Perform bitwise OR operation.
     *
     * @param a first operand
     * @param b second operand
     * @return a | b
     */
    public long bitwiseOr(long a, long b) {
        validateInputs(a, b);
        return a | b;
    }

    /**
     * Perform bitwise XOR operation.
     *
     * @param a first operand
     * @param b second operand
     * @return a ^ b
     */
    public long bitwiseXor(long a, long b) {
        validateInputs(a, b);
        return a ^ b;
    }

    /**
     * Perform bitwise NOT operation.
     *
     * @param a operand
     * @return ~a
     */
    public long bitwiseNot(long a) {
        validateInputs(a);
        return ~a;
    }

    /**
     * Perform left shift operation.
     *
     * @param a operand
     * @param positions number of positions to shift
     * @return a << positions
     */
    public long leftShift(long a, int positions) {
        validateInputs(a, positions);

        if (positions < 0 || positions >= BITS_PER_LONG) {
            throw new ValidationException("Invalid shift positions", OPERATION_NAME, positions);
        }

        return a << positions;
    }

    /**
     * Perform right shift operation (arithmetic shift).
     *
     * @param a operand
     * @param positions number of positions to shift
     * @return a >> positions
     */
    public long rightShift(long a, int positions) {
        validateInputs(a, positions);

        if (positions < 0 || positions >= BITS_PER_LONG) {
            throw new ValidationException("Invalid shift positions", OPERATION_NAME, positions);
        }

        return a >> positions;
    }

    /**
     * Perform unsigned right shift operation.
     *
     * @param a operand
     * @param positions number of positions to shift
     * @return a >>> positions
     */
    public long unsignedRightShift(long a, int positions) {
        validateInputs(a, positions);

        if (positions < 0 || positions >= BITS_PER_LONG) {
            throw new ValidationException("Invalid shift positions", OPERATION_NAME, positions);
        }

        return a >>> positions;
    }

    // ===== BIT ANALYSIS =====

    /**
     * Count the number of set bits (Hamming weight) in a number.
     *
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     *
     * @param n number to analyze
     * @return number of set bits
     */
    public int countSetBits(long n) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(n);

            int count = 0;
            long num = n;

            // Brian Kernighan's algorithm
            while (num != 0) {
                num &= (num - 1);
                count++;
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Set bits in {}: {}", n, count);

            return count;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error counting set bits: {}", e.getMessage());
            throw new ValidationException("Failed to count set bits: " + e.getMessage(), OPERATION_NAME, n);
        }
    }

    /**
     * Count total number of bits in a number.
     *
     * @param n number to analyze
     * @return total number of bits
     */
    public int countTotalBits(long n) {
        validateInputs(n);
        return BITS_PER_LONG - Long.numberOfLeadingZeros(n);
    }

    /**
     * Get the position of the least significant set bit.
     *
     * @param n number to analyze
     * @return position of least significant set bit (0-based), -1 if no set bits
     */
    public int leastSignificantBit(long n) {
        validateInputs(n);

        if (n == 0) {
            return -1;
        }

        return Long.numberOfTrailingZeros(n);
    }

    /**
     * Get the position of the most significant set bit.
     *
     * @param n number to analyze
     * @return position of most significant set bit (0-based), -1 if no set bits
     */
    public int mostSignificantBit(long n) {
        validateInputs(n);

        if (n == 0) {
            return -1;
        }

        return BITS_PER_LONG - 1 - Long.numberOfLeadingZeros(n);
    }

    /**
     * Check if a number is a power of 2.
     *
     * @param n number to check
     * @return true if power of 2, false otherwise
     */
    public boolean isPowerOfTwo(long n) {
        validateInputs(n);
        return n > 0 && (n & (n - 1)) == 0;
    }

    /**
     * Get the next power of 2 greater than or equal to a number.
     *
     * @param n input number
     * @return next power of 2
     */
    public long nextPowerOfTwo(long n) {
        validateInputs(n);

        if (n <= 0) {
            return 1;
        }

        if (isPowerOfTwo(n)) {
            return n;
        }

        long result = n;
        result--;
        result |= result >> 1;
        result |= result >> 2;
        result |= result >> 4;
        result |= result >> 8;
        result |= result >> 16;
        result |= result >> 32;
        result++;

        return result;
    }

    // ===== NUMBER SYSTEM CONVERSIONS =====

    /**
     * Convert decimal to binary string.
     *
     * @param n decimal number
     * @return binary string representation
     */
    public String decimalToBinary(long n) {
        validateInputs(n);

        if (n == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        boolean negative = n < 0;
        long num = Math.abs(n);

        while (num > 0) {
            sb.insert(0, num & 1);
            num >>= 1;
        }

        if (negative) {
            sb.insert(0, '-');
        }

        return sb.toString();
    }

    /**
     * Convert binary string to decimal.
     *
     * @param binary binary string
     * @return decimal representation
     */
    public long binaryToDecimal(String binary) {
        validateInputs(binary);

        if (binary == null || binary.isEmpty()) {
            throw ValidationException.emptyParameter("binary", OPERATION_NAME);
        }

        boolean negative = binary.charAt(0) == '-';
        String binaryStr = negative ? binary.substring(1) : binary;

        long result = 0;
        for (int i = 0; i < binaryStr.length(); i++) {
            char c = binaryStr.charAt(i);
            if (c != '0' && c != '1') {
                throw new ValidationException("Invalid binary digit: " + c, OPERATION_NAME, binary);
            }
            result = (result << 1) | (c - '0');
        }

        return negative ? -result : result;
    }

    /**
     * Convert decimal to hexadecimal string.
     *
     * @param n decimal number
     * @return hexadecimal string representation
     */
    public String decimalToHexadecimal(long n) {
        validateInputs(n);

        if (n == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        boolean negative = n < 0;
        long num = Math.abs(n);

        while (num > 0) {
            sb.insert(0, HEX_CHARS[(int) (num & 0xF)]);
            num >>= 4;
        }

        if (negative) {
            sb.insert(0, '-');
        }

        return sb.toString();
    }

    /**
     * Convert hexadecimal string to decimal.
     *
     * @param hex hexadecimal string
     * @return decimal representation
     */
    public long hexadecimalToDecimal(String hex) {
        validateInputs(hex);

        if (hex == null || hex.isEmpty()) {
            throw ValidationException.emptyParameter("hex", OPERATION_NAME);
        }

        boolean negative = hex.charAt(0) == '-';
        String hexStr = negative ? hex.substring(1) : hex;

        long result = 0;
        for (int i = 0; i < hexStr.length(); i++) {
            char c = Character.toUpperCase(hexStr.charAt(i));
            int value;

            if (Character.isDigit(c)) {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = 10 + (c - 'A');
            } else {
                throw new ValidationException("Invalid hexadecimal digit: " + c, OPERATION_NAME, hex);
            }

            result = (result << 4) | value;
        }

        return negative ? -result : result;
    }

    /**
     * Convert decimal to octal string.
     *
     * @param n decimal number
     * @return octal string representation
     */
    public String decimalToOctal(long n) {
        validateInputs(n);

        if (n == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        boolean negative = n < 0;
        long num = Math.abs(n);

        while (num > 0) {
            sb.insert(0, num & 7);
            num >>= 3;
        }

        if (negative) {
            sb.insert(0, '-');
        }

        return sb.toString();
    }

    /**
     * Convert octal string to decimal.
     *
     * @param octal octal string
     * @return decimal representation
     */
    public long octalToDecimal(String octal) {
        validateInputs(octal);

        if (octal == null || octal.isEmpty()) {
            throw ValidationException.emptyParameter("octal", OPERATION_NAME);
        }

        boolean negative = octal.charAt(0) == '-';
        String octalStr = negative ? octal.substring(1) : octal;

        long result = 0;
        for (int i = 0; i < octalStr.length(); i++) {
            char c = octalStr.charAt(i);
            if (c < '0' || c > '7') {
                throw new ValidationException("Invalid octal digit: " + c, OPERATION_NAME, octal);
            }
            result = (result << 3) | (c - '0');
        }

        return negative ? -result : result;
    }

    // ===== GRAY CODE OPERATIONS =====

    /**
     * Convert binary number to Gray code.
     *
     * @param n binary number
     * @return Gray code representation
     */
    public long binaryToGray(long n) {
        validateInputs(n);
        return n ^ (n >> 1);
    }

    /**
     * Convert Gray code to binary number.
     *
     * @param gray Gray code number
     * @return binary representation
     */
    public long grayToBinary(long gray) {
        validateInputs(gray);

        long binary = gray;
        while ((gray >>= 1) != 0) {
            binary ^= gray;
        }

        return binary;
    }

    /**
     * Generate Gray code sequence of given length.
     *
     * @param n number of bits
     * @return list of Gray code numbers
     */
    public List<Long> generateGrayCode(int n) {
        validateInputs(n);

        if (n < 0 || n > 31) {
            throw new ValidationException("Gray code length must be between 0 and 31", OPERATION_NAME, n);
        }

        List<Long> grayCodes = new ArrayList<>();
        int total = 1 << n; // 2^n

        for (int i = 0; i < total; i++) {
            grayCodes.add(binaryToGray(i));
        }

        return grayCodes;
    }

    // ===== HAMMING DISTANCE =====

    /**
     * Calculate Hamming distance between two numbers.
     *
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     *
     * @param a first number
     * @param b second number
     * @return Hamming distance
     */
    public int hammingDistance(long a, long b) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(a, b);

            long xor = a ^ b;
            int distance = countSetBits(xor);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Hamming distance between {} and {}: {}", a, b, distance);

            return distance;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating Hamming distance: {}", e.getMessage());
            throw new ValidationException("Failed to calculate Hamming distance: " + e.getMessage(), OPERATION_NAME, a, b);
        }
    }

    /**
     * Calculate Hamming weight (population count).
     *
     * @param n number to analyze
     * @return number of 1s in binary representation
     */
    public int hammingWeight(long n) {
        validateInputs(n);
        return countSetBits(n);
    }

    // ===== BIT MANIPULATION ALGORITHMS =====

    /**
     * Reverse bits in a number.
     *
     * @param n number to reverse
     * @return number with bits reversed
     */
    public long reverseBits(long n) {
        validateInputs(n);

        long result = 0;
        for (int i = 0; i < BITS_PER_LONG; i++) {
            result = (result << 1) | (n & 1);
            n >>= 1;
        }

        return result;
    }

    /**
     * Swap two bits at given positions.
     *
     * @param n number
     * @param pos1 first position (0-based)
     * @param pos2 second position (0-based)
     * @return number with bits swapped
     */
    public long swapBits(long n, int pos1, int pos2) {
        validateInputs(n, pos1, pos2);

        if (pos1 < 0 || pos1 >= BITS_PER_LONG || pos2 < 0 || pos2 >= BITS_PER_LONG) {
            throw new ValidationException("Invalid bit positions", OPERATION_NAME, pos1, pos2);
        }

        // Get bits at positions
        long bit1 = (n >> pos1) & 1;
        long bit2 = (n >> pos2) & 1;

        // If bits are different, swap them
        if (bit1 != bit2) {
            long mask = (1L << pos1) | (1L << pos2);
            n ^= mask;
        }

        return n;
    }

    /**
     * Set a bit at given position.
     *
     * @param n number
     * @param position bit position (0-based)
     * @return number with bit set
     */
    public long setBit(long n, int position) {
        validateInputs(n, position);

        if (position < 0 || position >= BITS_PER_LONG) {
            throw new ValidationException("Invalid bit position", OPERATION_NAME, position);
        }

        return n | (1L << position);
    }

    /**
     * Clear a bit at given position.
     *
     * @param n number
     * @param position bit position (0-based)
     * @return number with bit cleared
     */
    public long clearBit(long n, int position) {
        validateInputs(n, position);

        if (position < 0 || position >= BITS_PER_LONG) {
            throw new ValidationException("Invalid bit position", OPERATION_NAME, position);
        }

        return n & ~(1L << position);
    }

    /**
     * Toggle a bit at given position.
     *
     * @param n number
     * @param position bit position (0-based)
     * @return number with bit toggled
     */
    public long toggleBit(long n, int position) {
        validateInputs(n, position);

        if (position < 0 || position >= BITS_PER_LONG) {
            throw new ValidationException("Invalid bit position", OPERATION_NAME, position);
        }

        return n ^ (1L << position);
    }

    /**
     * Check if a bit is set at given position.
     *
     * @param n number
     * @param position bit position (0-based)
     * @return true if bit is set, false otherwise
     */
    public boolean isBitSet(long n, int position) {
        validateInputs(n, position);

        if (position < 0 || position >= BITS_PER_LONG) {
            throw new ValidationException("Invalid bit position", OPERATION_NAME, position);
        }

        return ((n >> position) & 1) == 1;
    }

    // ===== BIT PATTERN ANALYSIS =====

    /**
     * Find all set bit positions in a number.
     *
     * @param n number to analyze
     * @return list of set bit positions (0-based)
     */
    public List<Integer> getSetBitPositions(long n) {
        validateInputs(n);

        List<Integer> positions = new ArrayList<>();
        int position = 0;
        long num = n;

        while (num != 0) {
            if ((num & 1) == 1) {
                positions.add(position);
            }
            num >>= 1;
            position++;
        }

        return positions;
    }

    /**
     * Check if number has alternating bits (101010... or 010101...).
     *
     * @param n number to check
     * @return true if alternating bits, false otherwise
     */
    public boolean hasAlternatingBits(long n) {
        validateInputs(n);

        long xor = n ^ (n >> 1);
        return (xor & (xor + 1)) == 0;
    }

    /**
     * Count number of bits that need to be flipped to convert a to b.
     *
     * @param a first number
     * @param b second number
     * @return number of bits to flip
     */
    public int bitsToFlip(long a, long b) {
        validateInputs(a, b);
        return hammingDistance(a, b);
    }

    /**
     * Check if number is a palindrome in binary representation.
     *
     * @param n number to check
     * @return true if binary palindrome, false otherwise
     */
    public boolean isBinaryPalindrome(long n) {
        validateInputs(n);

        if (n < 0) {
            return false; // Negative numbers can't be palindromes in binary
        }

        String binary = decimalToBinary(n);
        int length = binary.length();

        for (int i = 0; i < length / 2; i++) {
            if (binary.charAt(i) != binary.charAt(length - 1 - i)) {
                return false;
            }
        }

        return true;
    }

    // ===== UTILITY METHODS =====

    /**
     * Get current memory usage in bytes.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Get binary representation as formatted string with bit positions.
     *
     * @param n number
     * @return formatted binary string
     */
    public String getBinaryWithPositions(long n) {
        validateInputs(n);

        StringBuilder sb = new StringBuilder();
        sb.append("Binary: ").append(decimalToBinary(n)).append("\n");
        sb.append("Positions: ");

        for (int i = BITS_PER_LONG - 1; i >= 0; i--) {
            if (isBitSet(n, i)) {
                sb.append(i).append(" ");
            }
        }

        return sb.toString().trim();
    }

    /**
     * Calculate parity of a number (even or odd number of set bits).
     *
     * @param n number to check
     * @return true for even parity, false for odd parity
     */
    public boolean evenParity(long n) {
        validateInputs(n);
        return countSetBits(n) % 2 == 0;
    }

    /**
     * Find the largest power of 2 that divides the number.
     *
     * @param n number
     * @return largest power of 2 divisor
     */
    public long largestPowerOfTwoDivisor(long n) {
        validateInputs(n);

        if (n == 0) {
            return 0;
        }

        // This is equivalent to n & -n
        return n & -n;
    }

    /**
     * Check if two numbers have the same parity.
     *
     * @param a first number
     * @param b second number
     * @return true if same parity, false otherwise
     */
    public boolean sameParity(long a, long b) {
        validateInputs(a, b);
        return evenParity(a) == evenParity(b);
    }
}
