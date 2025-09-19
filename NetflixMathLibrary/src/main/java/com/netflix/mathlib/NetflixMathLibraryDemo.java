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

package com.netflix.mathlib;

import com.netflix.mathlib.core.NetflixMathLibrary;
import com.netflix.mathlib.numbertheory.NumberTheoryOperations;
import com.netflix.mathlib.combinatorics.CombinatoricsOperations;
import com.netflix.mathlib.algebra.SequenceSeriesOperations;
import com.netflix.mathlib.algebra.MatrixOperations;
import com.netflix.mathlib.geometry.GeometryOperations;
import com.netflix.mathlib.bitwise.BitwiseOperations;

import java.math.BigDecimal;
import java.util.List;

/**
 * Netflix Math Library Demonstration
 *
 * This class demonstrates all the mathematical operations available in the
 * Netflix Math Library, covering all topics from the math.md specification.
 *
 * Topics covered:
 * - Number Theory (GCD, LCM, Primes, Sieve of Eratosthenes, Euclid's Algorithm)
 * - Combinatorics (Factorial, Permutations, Combinations, Fibonacci)
 * - Sequences and Series (AP, GP, Arithmetic/Geometric Progressions)
 * - Matrix Algebra (Matrix operations, determinants, inverses)
 * - Geometry (Trigonometry, coordinate geometry, distance calculations)
 * - Bitwise Operations (Bit manipulation, number system conversions)
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class NetflixMathLibraryDemo {

    private static final NetflixMathLibrary mathLib = NetflixMathLibrary.getInstance();

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   Netflix Math Library Demonstration");
        System.out.println("==========================================");
        System.out.println();

        demonstrateNumberTheory();
        demonstrateCombinatorics();
        demonstrateSequencesAndSeries();
        demonstrateMatrixOperations();
        demonstrateGeometry();
        demonstrateBitwiseOperations();

        System.out.println();
        System.out.println("==========================================");
        System.out.println("   Demonstration Complete!");
        System.out.println("==========================================");
    }

    /**
     * Demonstrate Number Theory operations
     */
    private static void demonstrateNumberTheory() {
        System.out.println("🔢 NUMBER THEORY OPERATIONS");
        System.out.println("============================");

        NumberTheoryOperations nt = mathLib.getNumberTheoryOperations();

        // GCD and LCM
        System.out.println("GCD and LCM:");
        System.out.println("  GCD(54, 24) = " + nt.gcd(54, 24));
        System.out.println("  LCM(24, 36) = " + nt.lcm(24, 36));
        System.out.println("  GCD(15, 28, 35) = " + nt.gcd(15, 28, 35));

        // Extended GCD
        var extendedGcd = nt.extendedGcd(35, 15);
        System.out.println("  Extended GCD(35, 15): " + extendedGcd);

        // Prime operations
        System.out.println("\nPrime Operations:");
        System.out.println("  Is 17 prime? " + nt.isPrime(17));
        System.out.println("  Is 21 prime? " + nt.isPrime(21));

        // Sieve of Eratosthenes
        List<Long> primes = nt.sieveOfEratosthenes(30);
        System.out.println("  Primes up to 30: " + primes);

        // Modular arithmetic
        System.out.println("\nModular Arithmetic:");
        System.out.println("  2^10 mod 7 = " + nt.modularExponentiation(2, 10, 7));
        System.out.println("  Modular inverse of 2 mod 5 = " + nt.modularInverse(2, 5));

        // Factorization
        List<Long> factors = nt.primeFactors(60);
        System.out.println("  Prime factors of 60: " + factors);

        // Euler's Totient
        System.out.println("  Euler's Totient φ(10) = " + nt.eulerTotient(10));

        System.out.println();
    }

    /**
     * Demonstrate Combinatorics operations
     */
    private static void demonstrateCombinatorics() {
        System.out.println("🔢 COMBINATORICS OPERATIONS");
        System.out.println("============================");

        CombinatoricsOperations comb = mathLib.getCombinatoricsOperations();

        // Factorial
        System.out.println("Factorial:");
        System.out.println("  5! = " + comb.factorial(5));
        System.out.println("  10! = " + comb.factorial(10));

        // Fibonacci
        System.out.println("\nFibonacci Sequence:");
        System.out.println("  F(10) = " + comb.fibonacci(10));
        System.out.println("  F(15) = " + comb.fibonacci(15));

        // Combinations and Permutations
        System.out.println("\nCombinations and Permutations:");
        System.out.println("  C(5, 2) = " + comb.combination(5, 2));
        System.out.println("  P(5, 2) = " + comb.permutation(5, 2));

        // Catalan numbers
        System.out.println("\nCatalan Numbers:");
        System.out.println("  C_3 = " + comb.catalanNumber(3));
        System.out.println("  C_4 = " + comb.catalanNumber(4));

        // Bell numbers
        System.out.println("\nBell Numbers:");
        System.out.println("  B_3 = " + comb.bellNumber(3));
        System.out.println("  B_4 = " + comb.bellNumber(4));

        System.out.println();
    }

    /**
     * Demonstrate Sequences and Series operations
     */
    private static void demonstrateSequencesAndSeries() {
        System.out.println("🔢 SEQUENCES AND SERIES OPERATIONS");
        System.out.println("===================================");

        SequenceSeriesOperations seq = mathLib.getSequenceSeriesOperations();

        // Arithmetic Progression
        System.out.println("Arithmetic Progression:");
        BigDecimal apTerm = seq.arithmeticProgressionTerm(
            BigDecimal.valueOf(2), BigDecimal.valueOf(3), 5);
        System.out.println("  5th term of AP (a=2, d=3): " + apTerm);

        BigDecimal apSum = seq.arithmeticProgressionSum(
            BigDecimal.valueOf(2), BigDecimal.valueOf(3), 5);
        System.out.println("  Sum of first 5 terms: " + apSum);

        // Geometric Progression
        System.out.println("\nGeometric Progression:");
        BigDecimal gpTerm = seq.geometricProgressionTerm(
            BigDecimal.valueOf(3), BigDecimal.valueOf(2), 4);
        System.out.println("  4th term of GP (a=3, r=2): " + gpTerm);

        BigDecimal gpSum = seq.geometricProgressionSum(
            BigDecimal.valueOf(3), BigDecimal.valueOf(2), 4);
        System.out.println("  Sum of first 4 terms: " + gpSum);

        // Generate sequences
        List<BigDecimal> apSeq = seq.generateArithmeticProgression(
            BigDecimal.valueOf(1), BigDecimal.valueOf(2), 5);
        System.out.println("  AP sequence (5 terms): " + apSeq);

        List<BigDecimal> gpSeq = seq.generateGeometricProgression(
            BigDecimal.valueOf(1), BigDecimal.valueOf(2), 5);
        System.out.println("  GP sequence (5 terms): " + gpSeq);

        // Series analysis
        boolean isAP = seq.isArithmeticSequence(apSeq) != null;
        boolean isGP = seq.isGeometricSequence(gpSeq) != null;
        System.out.println("  Is AP sequence arithmetic? " + isAP);
        System.out.println("  Is GP sequence geometric? " + isGP);

        System.out.println();
    }

    /**
     * Demonstrate Matrix operations
     */
    private static void demonstrateMatrixOperations() {
        System.out.println("🔢 MATRIX OPERATIONS");
        System.out.println("====================");

        MatrixOperations matrixOps = mathLib.getMatrixOperations();

        // Create matrices
        BigDecimal[][] matrixA = {
            {BigDecimal.valueOf(1), BigDecimal.valueOf(2)},
            {BigDecimal.valueOf(3), BigDecimal.valueOf(4)}
        };

        BigDecimal[][] matrixB = {
            {BigDecimal.valueOf(5), BigDecimal.valueOf(6)},
            {BigDecimal.valueOf(7), BigDecimal.valueOf(8)}
        };

        System.out.println("Matrix A:");
        printMatrix(matrixA);
        System.out.println("Matrix B:");
        printMatrix(matrixB);

        // Matrix operations
        BigDecimal[][] sum = matrixOps.add(matrixA, matrixB);
        System.out.println("A + B:");
        printMatrix(sum);

        BigDecimal[][] product = matrixOps.multiply(matrixA, matrixB);
        System.out.println("A * B:");
        printMatrix(product);

        // Matrix properties
        BigDecimal detA = matrixOps.determinant(matrixA);
        System.out.println("Determinant of A: " + detA);

        boolean isSquare = matrixOps.isSquare(matrixA);
        System.out.println("Is A square matrix? " + isSquare);

        // Transpose
        BigDecimal[][] transposeA = matrixOps.transpose(matrixA);
        System.out.println("Transpose of A:");
        printMatrix(transposeA);

        System.out.println();
    }

    /**
     * Demonstrate Geometry operations
     */
    private static void demonstrateGeometry() {
        System.out.println("🔢 GEOMETRY OPERATIONS");
        System.out.println("======================");

        GeometryOperations geom = mathLib.getGeometryOperations();

        // Trigonometric functions
        BigDecimal angle = BigDecimal.valueOf(Math.PI).divide(BigDecimal.valueOf(4), java.math.MathContext.DECIMAL128); // π/4
        System.out.println("Trigonometric Functions (angle = π/4):");
        System.out.println("  sin(π/4) ≈ " + geom.sin(angle));
        System.out.println("  cos(π/4) ≈ " + geom.cos(angle));
        System.out.println("  tan(π/4) ≈ " + geom.tan(angle));

        // Distance calculations
        System.out.println("\nDistance Calculations:");
        BigDecimal distance = geom.euclideanDistance(
            BigDecimal.valueOf(0), BigDecimal.valueOf(0),
            BigDecimal.valueOf(3), BigDecimal.valueOf(4));
        System.out.println("  Euclidean distance (0,0) to (3,4): " + distance);

        BigDecimal manhattan = geom.manhattanDistance(
            BigDecimal.valueOf(0), BigDecimal.valueOf(0),
            BigDecimal.valueOf(3), BigDecimal.valueOf(4));
        System.out.println("  Manhattan distance (0,0) to (3,4): " + manhattan);

        // Area calculations
        BigDecimal circleArea = geom.circleArea(BigDecimal.valueOf(5));
        System.out.println("\nArea Calculations:");
        System.out.println("  Area of circle (r=5): " + circleArea);

        BigDecimal rectArea = geom.rectangleArea(BigDecimal.valueOf(4), BigDecimal.valueOf(6));
        System.out.println("  Area of rectangle (4x6): " + rectArea);

        // Triangle operations
        BigDecimal triangleArea = geom.triangleArea(
            BigDecimal.valueOf(0), BigDecimal.valueOf(0),
            BigDecimal.valueOf(4), BigDecimal.valueOf(0),
            BigDecimal.valueOf(0), BigDecimal.valueOf(3));
        System.out.println("  Area of triangle with vertices (0,0), (4,0), (0,3): " + triangleArea);

        // Vector operations
        BigDecimal dotProduct = geom.dotProduct2D(
            BigDecimal.valueOf(1), BigDecimal.valueOf(2),
            BigDecimal.valueOf(3), BigDecimal.valueOf(4));
        System.out.println("\nVector Operations:");
        System.out.println("  Dot product of (1,2) and (3,4): " + dotProduct);

        BigDecimal angleBetween = geom.angleBetweenVectors2D(
            BigDecimal.valueOf(1), BigDecimal.valueOf(0),
            BigDecimal.valueOf(0), BigDecimal.valueOf(1));
        System.out.println("  Angle between (1,0) and (0,1): " + angleBetween + " radians");

        System.out.println();
    }

    /**
     * Demonstrate Bitwise operations
     */
    private static void demonstrateBitwiseOperations() {
        System.out.println("🔢 BITWISE OPERATIONS");
        System.out.println("=====================");

        BitwiseOperations bitwise = mathLib.getBitwiseOperations();

        // Basic bitwise operations
        long a = 60;  // 0011 1100
        long b = 13;  // 0000 1101

        System.out.println("Bitwise Operations (a=60, b=13):");
        System.out.println("  a & b = " + bitwise.bitwiseAnd(a, b));
        System.out.println("  a | b = " + bitwise.bitwiseOr(a, b));
        System.out.println("  a ^ b = " + bitwise.bitwiseXor(a, b));
        System.out.println("  ~a = " + bitwise.bitwiseNot(a));

        // Bit analysis
        System.out.println("\nBit Analysis:");
        System.out.println("  Set bits in 60: " + bitwise.countSetBits(60));
        System.out.println("  Is 16 a power of 2? " + bitwise.isPowerOfTwo(16));
        System.out.println("  Is 18 a power of 2? " + bitwise.isPowerOfTwo(18));

        // Number system conversions
        System.out.println("\nNumber System Conversions:");
        System.out.println("  42 in binary: " + bitwise.decimalToBinary(42));
        System.out.println("  42 in hexadecimal: " + bitwise.decimalToHexadecimal(42));
        System.out.println("  Binary 101010 to decimal: " + bitwise.binaryToDecimal("101010"));
        System.out.println("  Hex A5 to decimal: " + bitwise.hexadecimalToDecimal("A5"));

        // Bit manipulation
        System.out.println("\nBit Manipulation:");
        long num = 60;
        System.out.println("  Original number: " + num + " (" + bitwise.decimalToBinary(num) + ")");
        System.out.println("  Set bit 0: " + bitwise.setBit(num, 0) + " (" + bitwise.decimalToBinary(bitwise.setBit(num, 0)) + ")");
        System.out.println("  Clear bit 3: " + bitwise.clearBit(num, 3) + " (" + bitwise.decimalToBinary(bitwise.clearBit(num, 3)) + ")");
        System.out.println("  Toggle bit 2: " + bitwise.toggleBit(num, 2) + " (" + bitwise.decimalToBinary(bitwise.toggleBit(num, 2)) + ")");

        // Gray code
        System.out.println("\nGray Code:");
        System.out.println("  Binary 5 to Gray: " + bitwise.binaryToGray(5));
        System.out.println("  Gray 7 to Binary: " + bitwise.grayToBinary(7));

        // Hamming distance
        System.out.println("\nHamming Operations:");
        System.out.println("  Hamming distance between 60 and 13: " + bitwise.hammingDistance(60, 13));
        System.out.println("  Hamming weight of 60: " + bitwise.hammingWeight(60));

        System.out.println();
    }

    /**
     * Utility method to print a matrix
     */
    private static void printMatrix(BigDecimal[][] matrix) {
        for (BigDecimal[] row : matrix) {
            System.out.print("  [");
            for (int i = 0; i < row.length; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(row[i]);
            }
            System.out.println("]");
        }
    }
}
