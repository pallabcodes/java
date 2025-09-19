/*
 * Copyright 2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.w3.org/2001/XMLSchema-instance
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.mathlib;

import com.netflix.mathlib.core.NetflixMathLibrary;
import com.netflix.mathlib.graph.datastructures.Graph;
import com.netflix.mathlib.graph.datastructures.WeightedGraph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration test for Netflix Math Library.
 *
 * This test demonstrates all mathematical modules working together:
 * - Number Theory operations
 * - Combinatorics algorithms
 * - Sequence and Series operations
 * - Matrix algebra
 * - Graph theory algorithms
 * - Geometry and trigonometry
 * - Bitwise operations
 * - Statistics and probability
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
@DisplayName("Netflix Math Library Integration Tests")
class NetflixMathLibraryIntegrationTest {

    private static NetflixMathLibrary mathLib;

    @BeforeAll
    static void setUp() {
        mathLib = NetflixMathLibrary.getInstance();
        System.out.println("Netflix Math Library Integration Tests");
        System.out.println("=====================================");
    }

    @Test
    @DisplayName("Complete Number Theory Workflow")
    void testNumberTheoryWorkflow() {
        System.out.println("\n1. NUMBER THEORY WORKFLOW");
        System.out.println("=========================");

        var nt = mathLib.getNumberTheoryOperations();

        // GCD and LCM operations
        long gcd = nt.gcd(48, 18);
        long lcm = nt.lcm(12, 18);
        System.out.println("GCD(48, 18) = " + gcd);
        System.out.println("LCM(12, 18) = " + lcm);
        assertEquals(6, gcd);
        assertEquals(36, lcm);

        // Extended GCD
        var extended = nt.extendedGcd(35, 15);
        System.out.println("Extended GCD(35, 15): " + extended);
        assertEquals(5, extended.gcd);

        // Prime operations
        boolean isPrime = nt.isPrime(29);
        var primes = nt.sieveOfEratosthenes(50);
        System.out.println("Is 29 prime? " + isPrime);
        System.out.println("Primes up to 50: " + primes.subList(0, Math.min(10, primes.size())) + "...");
        assertTrue(isPrime);
        assertTrue(primes.contains(29L));

        // Modular arithmetic
        long modExp = nt.modularExponentiation(7, 3, 13);
        long modInv = nt.modularInverse(3, 11);
        System.out.println("7^3 mod 13 = " + modExp);
        System.out.println("Modular inverse of 3 mod 11 = " + modInv);
        assertEquals(5, modExp); // 343 mod 13 = 5
        assertEquals(4, modInv); // 3 * 4 = 12 ≡ 1 mod 11
    }

    @Test
    @DisplayName("Complete Combinatorics Workflow")
    void testCombinatoricsWorkflow() {
        System.out.println("\n2. COMBINATORICS WORKFLOW");
        System.out.println("=========================");

        var comb = mathLib.getCombinatoricsOperations();

        // Factorial
        var fact5 = comb.factorial(5);
        System.out.println("5! = " + fact5);
        assertEquals(BigDecimal.valueOf(120), fact5);

        // Fibonacci
        var fib10 = comb.fibonacci(10);
        var fibSeq = comb.fibonacciSequence(8);
        System.out.println("F(10) = " + fib10);
        System.out.println("Fibonacci sequence (8 terms): " + fibSeq);
        assertEquals(BigDecimal.valueOf(55), fib10);
        assertEquals(8, fibSeq.size());

        // Combinations and permutations
        var comb52 = comb.combination(5, 2);
        var perm52 = comb.permutation(5, 2);
        System.out.println("C(5,2) = " + comb52);
        System.out.println("P(5,2) = " + perm52);
        assertEquals(BigDecimal.valueOf(10), comb52);
        assertEquals(BigDecimal.valueOf(20), perm52);

        // Catalan numbers
        var catalan3 = comb.catalanNumber(3);
        System.out.println("Catalan number C_3 = " + catalan3);
        assertEquals(BigDecimal.valueOf(5), catalan3);
    }

    @Test
    @DisplayName("Complete Sequence and Series Workflow")
    void testSequencesAndSeriesWorkflow() {
        System.out.println("\n3. SEQUENCES AND SERIES WORKFLOW");
        System.out.println("================================");

        var seq = mathLib.getSequenceSeriesOperations();

        // Arithmetic Progression
        var apTerm = seq.arithmeticProgressionTerm(BigDecimal.valueOf(2), BigDecimal.valueOf(3), 5);
        var apSum = seq.arithmeticProgressionSum(BigDecimal.valueOf(2), BigDecimal.valueOf(3), 5);
        var apSeq = seq.generateArithmeticProgression(BigDecimal.valueOf(1), BigDecimal.valueOf(2), 5);
        System.out.println("5th term of AP (a=2, d=3): " + apTerm);
        System.out.println("Sum of first 5 terms: " + apSum);
        System.out.println("AP sequence: " + apSeq);
        assertEquals(BigDecimal.valueOf(14), apTerm); // 2 + 4*3 = 14
        assertEquals(BigDecimal.valueOf(40), apSum);  // (5/2)*(4 + 14) = 40

        // Geometric Progression
        var gpTerm = seq.geometricProgressionTerm(BigDecimal.valueOf(3), BigDecimal.valueOf(2), 4);
        var gpSum = seq.geometricProgressionSum(BigDecimal.valueOf(3), BigDecimal.valueOf(2), 4);
        var gpSeq = seq.generateGeometricProgression(BigDecimal.valueOf(1), BigDecimal.valueOf(2), 5);
        System.out.println("4th term of GP (a=3, r=2): " + gpTerm);
        System.out.println("Sum of first 4 terms: " + gpSum);
        System.out.println("GP sequence: " + gpSeq);
        assertEquals(BigDecimal.valueOf(24), gpTerm); // 3 * 2^3 = 24
        assertTrue(gpSeq.size() == 5);

        // Sequence analysis
        var apCommonDiff = seq.isArithmeticSequence(apSeq);
        var gpCommonRatio = seq.isGeometricSequence(gpSeq);
        System.out.println("AP sequence is arithmetic with common difference: " + apCommonDiff);
        System.out.println("GP sequence is geometric with common ratio: " + gpCommonRatio);
        assertNotNull(apCommonDiff);
        assertNotNull(gpCommonRatio);
    }

    @Test
    @DisplayName("Complete Matrix Operations Workflow")
    void testMatrixOperationsWorkflow() {
        System.out.println("\n4. MATRIX OPERATIONS WORKFLOW");
        System.out.println("=============================");

        var matrixOps = mathLib.getMatrixOperations();

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
        var sum = matrixOps.add(matrixA, matrixB);
        var product = matrixOps.multiply(matrixA, matrixB);
        var transposeA = matrixOps.transpose(matrixA);

        System.out.println("A + B:");
        printMatrix(sum);
        System.out.println("A * B:");
        printMatrix(product);
        System.out.println("Transpose of A:");
        printMatrix(transposeA);

        // Matrix properties
        var detA = matrixOps.determinant(matrixA);
        var isSquare = matrixOps.isSquare(matrixA);
        System.out.println("Determinant of A: " + detA);
        System.out.println("Is A square matrix? " + isSquare);
        assertEquals(BigDecimal.valueOf(-2), detA); // 1*4 - 2*3 = -2
        assertTrue(isSquare);
    }

    @Test
    @DisplayName("Complete Graph Theory Workflow")
    void testGraphTheoryWorkflow() {
        System.out.println("\n5. GRAPH THEORY WORKFLOW");
        System.out.println("========================");

        var graphAlg = mathLib.getGraphAlgorithms();

        // Create an undirected graph
        Graph graph = new Graph(6, false);
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(2, 4);
        graph.addEdge(3, 5);
        graph.addEdge(4, 5);

        System.out.println("Created graph with 6 vertices and 7 edges");

        // BFS traversal
        var bfsResult = graphAlg.breadthFirstSearch(graph, 0);
        System.out.println("BFS from vertex 0:");
        System.out.println("Visited: " + bfsResult.visited);
        System.out.println("Levels: " + bfsResult.levels);

        // DFS traversal
        var dfsResult = graphAlg.depthFirstSearch(graph, 0);
        System.out.println("DFS from vertex 0:");
        System.out.println("Discovery times: " + dfsResult.discoveryTime);
        System.out.println("Finish times: " + dfsResult.finishTime);

        // Graph properties
        int components = graphAlg.countConnectedComponents(graph);
        boolean isConnected = graphAlg.isConnected(graph);
        System.out.println("Number of connected components: " + components);
        System.out.println("Is graph connected? " + isConnected);
        assertEquals(1, components);
        assertTrue(isConnected);

        // Create weighted graph for shortest paths
        WeightedGraph weightedGraph = new WeightedGraph(5, true);
        weightedGraph.addWeightedEdge(0, 1, 4);
        weightedGraph.addWeightedEdge(0, 2, 1);
        weightedGraph.addWeightedEdge(2, 1, 2);
        weightedGraph.addWeightedEdge(1, 3, 1);
        weightedGraph.addWeightedEdge(2, 3, 5);
        weightedGraph.addWeightedEdge(3, 4, 3);

        var dijkstraResult = graphAlg.dijkstra(weightedGraph, 0);
        System.out.println("Shortest path from 0 to 3: " + dijkstraResult.getPath(3));
        assertNotNull(dijkstraResult.getPath(3));
    }

    @Test
    @DisplayName("Complete Geometry Workflow")
    void testGeometryWorkflow() {
        System.out.println("\n6. GEOMETRY WORKFLOW");
        System.out.println("====================");

        var geom = mathLib.getGeometryOperations();

        // Trigonometric functions
        BigDecimal angle = BigDecimal.valueOf(Math.PI).divide(BigDecimal.valueOf(4), geom.DEFAULT_PRECISION);
        var sinValue = geom.sin(angle);
        var cosValue = geom.cos(angle);
        var tanValue = geom.tan(angle);
        System.out.println("sin(π/4) ≈ " + sinValue);
        System.out.println("cos(π/4) ≈ " + cosValue);
        System.out.println("tan(π/4) ≈ " + tanValue);

        // Distance calculations
        var euclideanDist = geom.euclideanDistance(
            BigDecimal.valueOf(0), BigDecimal.valueOf(0),
            BigDecimal.valueOf(3), BigDecimal.valueOf(4));
        var manhattanDist = geom.manhattanDistance(
            BigDecimal.valueOf(0), BigDecimal.valueOf(0),
            BigDecimal.valueOf(3), BigDecimal.valueOf(4));
        System.out.println("Euclidean distance (0,0) to (3,4): " + euclideanDist);
        System.out.println("Manhattan distance (0,0) to (3,4): " + manhattanDist);
        assertEquals(BigDecimal.valueOf(5), euclideanDist); // 3-4-5 triangle
        assertEquals(BigDecimal.valueOf(7), manhattanDist); // |3-0| + |4-0| = 7

        // Area calculations
        var circleArea = geom.circleArea(BigDecimal.valueOf(5));
        var rectArea = geom.rectangleArea(BigDecimal.valueOf(4), BigDecimal.valueOf(6));
        System.out.println("Area of circle (r=5): " + circleArea);
        System.out.println("Area of rectangle (4x6): " + rectArea);

        // Triangle operations
        var triangleArea = geom.triangleArea(
            BigDecimal.valueOf(0), BigDecimal.valueOf(0),
            BigDecimal.valueOf(4), BigDecimal.valueOf(0),
            BigDecimal.valueOf(0), BigDecimal.valueOf(3));
        System.out.println("Area of triangle: " + triangleArea);
        assertEquals(BigDecimal.valueOf(6), triangleArea); // base=4, height=3, area=6
    }

    @Test
    @DisplayName("Complete Bitwise Operations Workflow")
    void testBitwiseOperationsWorkflow() {
        System.out.println("\n7. BITWISE OPERATIONS WORKFLOW");
        System.out.println("==============================");

        var bitwise = mathLib.getBitwiseOperations();

        long a = 60; // 0011 1100
        long b = 13; // 0000 1101

        // Basic bitwise operations
        var andResult = bitwise.bitwiseAnd(a, b);
        var orResult = bitwise.bitwiseOr(a, b);
        var xorResult = bitwise.bitwiseXor(a, b);
        System.out.println("60 & 13 = " + andResult);
        System.out.println("60 | 13 = " + orResult);
        System.out.println("60 ^ 13 = " + xorResult);
        assertEquals(12, andResult);
        assertEquals(61, orResult);
        assertEquals(49, xorResult);

        // Bit analysis
        var setBits = bitwise.countSetBits(60);
        var isPowerOfTwo = bitwise.isPowerOfTwo(16);
        System.out.println("Set bits in 60: " + setBits);
        System.out.println("Is 16 a power of 2? " + isPowerOfTwo);
        assertEquals(4, setBits);
        assertTrue(isPowerOfTwo);

        // Number system conversions
        var binary42 = bitwise.decimalToBinary(42);
        var hex42 = bitwise.decimalToHexadecimal(42);
        var decimalFromBinary = bitwise.binaryToDecimal("101010");
        System.out.println("42 in binary: " + binary42);
        System.out.println("42 in hexadecimal: " + hex42);
        System.out.println("Binary 101010 to decimal: " + decimalFromBinary);
        assertEquals("101010", binary42);
        assertEquals("2A", hex42);
        assertEquals(42, decimalFromBinary);
    }

    @Test
    @DisplayName("Complete Statistics Workflow")
    void testStatisticsWorkflow() {
        System.out.println("\n8. STATISTICS WORKFLOW");
        System.out.println("======================");

        var stats = mathLib.getStatisticsOperations();

        double[] data = {12, 15, 18, 22, 25, 28, 30, 35, 40, 45};

        // Descriptive statistics
        var mean = stats.mean(data);
        var median = stats.median(data);
        var stdDev = stats.standardDeviation(data, false);
        System.out.println("Data: " + java.util.Arrays.toString(data));
        System.out.println("Mean: " + mean);
        System.out.println("Median: " + median);
        System.out.println("Standard Deviation: " + stdDev);

        // Correlation
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2, 4, 6, 8, 10};
        var correlation = stats.correlation(x, y);
        System.out.println("Correlation between [1,2,3,4,5] and [2,4,6,8,10]: " + correlation);
        assertEquals(BigDecimal.valueOf(1), correlation); // Perfect positive correlation

        // Statistical inference
        var confidenceInterval = stats.confidenceInterval(25.0, 5.0, 30, 0.95);
        System.out.println("95% confidence interval: [" + confidenceInterval[0] + ", " + confidenceInterval[1] + "]");

        // Probability distributions
        var normalPDF = stats.normalPDF(0, 0, 1);
        var binomialPMF = stats.binomialPMF(3, 5, 0.5);
        System.out.println("Standard normal PDF at x=0: " + normalPDF);
        System.out.println("Binomial PMF P(X=3) for n=5, p=0.5: " + binomialPMF);
    }

    @Test
    @DisplayName("Cross-Module Integration Test")
    void testCrossModuleIntegration() {
        System.out.println("\n9. CROSS-MODULE INTEGRATION TEST");
        System.out.println("=================================");

        // Demonstrate how different modules work together

        // 1. Use number theory for prime factors, then combinatorics for combinations
        var nt = mathLib.getNumberTheoryOperations();
        var comb = mathLib.getCombinatoricsOperations();

        var factors = nt.primeFactors(120); // 120 = 2^3 * 3 * 5
        System.out.println("Prime factors of 120: " + factors);

        // Number of ways to choose 2 factors from the distinct primes {2,3,5}
        var combinations = comb.combination(3, 2); // C(3,2) = 3
        System.out.println("Number of ways to choose 2 from 3 distinct prime factors: " + combinations);

        // 2. Use geometry for coordinates, then statistics for analysis
        var geom = mathLib.getGeometryOperations();
        var stats = mathLib.getStatisticsOperations();

        // Calculate distances between points
        double[] distances = new double[3];
        distances[0] = geom.euclideanDistance(BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                                           BigDecimal.valueOf(3), BigDecimal.valueOf(4)).doubleValue();
        distances[1] = geom.euclideanDistance(BigDecimal.valueOf(3), BigDecimal.valueOf(4),
                                           BigDecimal.valueOf(6), BigDecimal.valueOf(8)).doubleValue();
        distances[2] = geom.euclideanDistance(BigDecimal.valueOf(6), BigDecimal.valueOf(8),
                                           BigDecimal.valueOf(9), BigDecimal.valueOf(12)).doubleValue();

        var distMean = stats.mean(distances);
        System.out.println("Mean distance between consecutive points: " + distMean);

        // 3. Use matrix operations and graph theory together
        var matrixOps = mathLib.getMatrixOperations();
        var graphAlg = mathLib.getGraphAlgorithms();

        // Create adjacency matrix for a graph
        BigDecimal[][] adjMatrix = {
            {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE},
            {BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE},
            {BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO}
        };

        var matrixTrace = matrixOps.trace(adjMatrix);
        System.out.println("Trace of adjacency matrix: " + matrixTrace);

        // Convert to graph and analyze
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 2);

        var connectedComponents = graphAlg.countConnectedComponents(graph);
        System.out.println("Number of connected components: " + connectedComponents);
    }

    @Test
    @DisplayName("Performance and Scalability Test")
    void testPerformanceAndScalability() {
        System.out.println("\n10. PERFORMANCE AND SCALABILITY TEST");
        System.out.println("====================================");

        var nt = mathLib.getNumberTheoryOperations();
        var comb = mathLib.getCombinatoricsOperations();
        var matrixOps = mathLib.getMatrixOperations();

        // Test performance scaling
        long startTime, endTime;

        // Number theory performance
        startTime = System.nanoTime();
        var largeGcd = nt.gcd(1234567890123456789L, 987654321098765432L);
        endTime = System.nanoTime();
        System.out.println("Large GCD computation time: " + (endTime - startTime) / 1_000_000 + " ms");
        assertTrue(largeGcd > 0);

        // Combinatorics performance
        startTime = System.nanoTime();
        var largeComb = comb.combination(100, 50);
        endTime = System.nanoTime();
        System.out.println("Large combination C(100,50) computation time: " + (endTime - startTime) / 1_000_000 + " ms");
        assertTrue(largeComb.compareTo(BigDecimal.ZERO) > 0);

        // Matrix operations performance
        BigDecimal[][] largeMatrix = matrixOps.zeroMatrix(10, 10);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                largeMatrix[i][j] = BigDecimal.valueOf(i + j);
            }
        }

        startTime = System.nanoTime();
        var largeMatrixTranspose = matrixOps.transpose(largeMatrix);
        endTime = System.nanoTime();
        System.out.println("10x10 matrix transpose time: " + (endTime - startTime) / 1_000 + " μs");
        assertNotNull(largeMatrixTranspose);
    }

    @Test
    @DisplayName("Error Handling and Validation Test")
    void testErrorHandlingAndValidation() {
        System.out.println("\n11. ERROR HANDLING AND VALIDATION TEST");
        System.out.println("======================================");

        var nt = mathLib.getNumberTheoryOperations();
        var comb = mathLib.getCombinatoricsOperations();
        var geom = mathLib.getGeometryOperations();

        // Test validation errors
        assertThrows(IllegalArgumentException.class, () -> nt.gcd(-1, 5));
        assertThrows(IllegalArgumentException.class, () -> comb.factorial(-1));
        assertThrows(IllegalArgumentException.class, () -> geom.sin(null));

        // Test edge cases
        assertEquals(1, nt.gcd(1, 1));
        assertEquals(BigDecimal.ONE, comb.factorial(0));
        assertEquals(BigDecimal.ONE, comb.factorial(1));

        System.out.println("All validation and error handling tests passed!");
    }

    // Helper method to print matrices
    private void printMatrix(BigDecimal[][] matrix) {
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
