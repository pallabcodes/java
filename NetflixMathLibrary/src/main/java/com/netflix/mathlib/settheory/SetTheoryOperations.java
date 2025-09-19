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

package com.netflix.mathlib.settheory;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Set Theory Operations - Production-grade set theory and relation operations.
 *
 * This class provides comprehensive set theory operations including:
 * - Set Operations (union, intersection, difference, symmetric difference)
 * - Power Sets and Subsets
 * - Cartesian Products
 * - Relations (reflexive, symmetric, transitive, equivalence relations)
 * - Functions (injective, surjective, bijective)
 * - Partitions and Equivalence Classes
 * - Set Cardinality and Properties
 * - Lattice Operations
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
public class SetTheoryOperations implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(SetTheoryOperations.class);
    private static final String OPERATION_NAME = "SetTheoryOperations";
    private static final String COMPLEXITY = "O(n*m)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;
    private final MathContext DEFAULT_PRECISION = new MathContext(50, java.math.RoundingMode.HALF_UP);

    /**
     * Constructor for Set Theory Operations.
     */
    public SetTheoryOperations() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized Set Theory Operations module");
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

    // ===== BASIC SET OPERATIONS =====

    /**
     * Calculate union of two sets.
     *
     * A ∪ B = {x | x ∈ A ∨ x ∈ B}
     *
     * Time Complexity: O(n + m)
     * Space Complexity: O(n + m)
     *
     * @param setA first set
     * @param setB second set
     * @return union of sets
     */
    public <T> Set<T> union(Set<T> setA, Set<T> setB) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(setA, setB);

            logger.debug("Calculating union of sets with sizes {} and {}", setA.size(), setB.size());

            Set<T> result = new HashSet<>(setA);
            result.addAll(setB);

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Union result size: {}", result.size());

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating union: {}", e.getMessage());
            throw new ValidationException("Failed to calculate union: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Calculate intersection of two sets.
     *
     * A ∩ B = {x | x ∈ A ∧ x ∈ B}
     *
     * Time Complexity: O(min(n, m))
     * Space Complexity: O(min(n, m))
     *
     * @param setA first set
     * @param setB second set
     * @return intersection of sets
     */
    public <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(setA, setB);

            logger.debug("Calculating intersection of sets with sizes {} and {}", setA.size(), setB.size());

            Set<T> result = new HashSet<>();
            Set<T> smallerSet = setA.size() <= setB.size() ? setA : setB;
            Set<T> largerSet = setA.size() > setB.size() ? setA : setB;

            for (T element : smallerSet) {
                if (largerSet.contains(element)) {
                    result.add(element);
                }
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Intersection result size: {}", result.size());

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating intersection: {}", e.getMessage());
            throw new ValidationException("Failed to calculate intersection: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Calculate set difference (relative complement).
     *
     * A - B = {x | x ∈ A ∧ x ∉ B}
     *
     * @param setA first set
     * @param setB second set
     * @return difference of sets
     */
    public <T> Set<T> difference(Set<T> setA, Set<T> setB) {
        validateInputs(setA, setB);

        Set<T> result = new HashSet<>(setA);
        result.removeAll(setB);

        return result;
    }

    /**
     * Calculate symmetric difference.
     *
     * A △ B = (A - B) ∪ (B - A)
     *
     * @param setA first set
     * @param setB second set
     * @return symmetric difference of sets
     */
    public <T> Set<T> symmetricDifference(Set<T> setA, Set<T> setB) {
        validateInputs(setA, setB);

        Set<T> diffAB = difference(setA, setB);
        Set<T> diffBA = difference(setB, setA);

        return union(diffAB, diffBA);
    }

    // ===== POWER SET AND SUBSETS =====

    /**
     * Calculate power set of a set.
     *
     * P(A) = {B | B ⊆ A}
     *
     * Time Complexity: O(2^n)
     * Space Complexity: O(2^n)
     *
     * @param set input set
     * @return power set
     */
    public <T> Set<Set<T>> powerSet(Set<T> set) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(set);

            if (set.size() > 20) {
                throw new ValidationException("Power set too large for sets with more than 20 elements", OPERATION_NAME);
            }

            logger.debug("Calculating power set of set with {} elements", set.size());

            Set<Set<T>> powerSet = new HashSet<>();
            List<T> elements = new ArrayList<>(set);
            int n = elements.size();

            // Generate all subsets using bit manipulation
            for (int i = 0; i < (1 << n); i++) {
                Set<T> subset = new HashSet<>();
                for (int j = 0; j < n; j++) {
                    if ((i & (1 << j)) != 0) {
                        subset.add(elements.get(j));
                    }
                }
                powerSet.add(subset);
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Power set contains {} subsets", powerSet.size());

            return powerSet;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating power set: {}", e.getMessage());
            throw new ValidationException("Failed to calculate power set: " + e.getMessage(), OPERATION_NAME);
        }
    }

    /**
     * Check if set A is a subset of set B.
     *
     * A ⊆ B ⇔ ∀x(x ∈ A → x ∈ B)
     *
     * @param setA potential subset
     * @param setB potential superset
     * @return true if A ⊆ B, false otherwise
     */
    public <T> boolean isSubset(Set<T> setA, Set<T> setB) {
        validateInputs(setA, setB);
        return setB.containsAll(setA);
    }

    /**
     * Check if set A is a proper subset of set B.
     *
     * A ⊂ B ⇔ A ⊆ B ∧ A ≠ B
     *
     * @param setA potential proper subset
     * @param setB potential superset
     * @return true if A ⊂ B, false otherwise
     */
    public <T> boolean isProperSubset(Set<T> setA, Set<T> setB) {
        validateInputs(setA, setB);
        return setA.size() < setB.size() && isSubset(setA, setB);
    }

    // ===== CARTESIAN PRODUCT =====

    /**
     * Calculate Cartesian product of two sets.
     *
     * A × B = {(a,b) | a ∈ A ∧ b ∈ B}
     *
     * Time Complexity: O(n * m)
     * Space Complexity: O(n * m)
     *
     * @param setA first set
     * @param setB second set
     * @return Cartesian product as set of ordered pairs
     */
    public <T, U> Set<Pair<T, U>> cartesianProduct(Set<T> setA, Set<U> setB) {
        long startTime = System.nanoTime();
        long startMemory = getCurrentMemoryUsage();

        try {
            validateInputs(setA, setB);

            if (setA.size() * setB.size() > 1000000) {
                throw new ValidationException("Cartesian product too large", OPERATION_NAME);
            }

            logger.debug("Calculating Cartesian product of sets with sizes {} and {}", setA.size(), setB.size());

            Set<Pair<T, U>> result = new HashSet<>();
            for (T elementA : setA) {
                for (U elementB : setB) {
                    result.add(new Pair<>(elementA, elementB));
                }
            }

            long executionTime = System.nanoTime() - startTime;
            long memoryUsed = getCurrentMemoryUsage() - startMemory;

            metrics.recordSuccess(executionTime, memoryUsed);
            logger.debug("Cartesian product size: {}", result.size());

            return result;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error calculating Cartesian product: {}", e.getMessage());
            throw new ValidationException("Failed to calculate Cartesian product: " + e.getMessage(), OPERATION_NAME);
        }
    }

    // ===== RELATIONS =====

    /**
     * Check if a relation is reflexive.
     *
     * ∀x ∈ A: (x,x) ∈ R
     *
     * @param domain domain set
     * @param relation relation as set of ordered pairs
     * @return true if reflexive, false otherwise
     */
    public <T> boolean isReflexive(Set<T> domain, Set<Pair<T, T>> relation) {
        validateInputs(domain, relation);

        for (T element : domain) {
            Pair<T, T> reflexivePair = new Pair<>(element, element);
            if (!relation.contains(reflexivePair)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a relation is symmetric.
     *
     * ∀x,y ∈ A: (x,y) ∈ R → (y,x) ∈ R
     *
     * @param relation relation as set of ordered pairs
     * @return true if symmetric, false otherwise
     */
    public <T> boolean isSymmetric(Set<Pair<T, T>> relation) {
        validateInputs(relation);

        for (Pair<T, T> pair : relation) {
            Pair<T, T> symmetricPair = new Pair<>(pair.second, pair.first);
            if (!relation.contains(symmetricPair)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a relation is transitive.
     *
     * ∀x,y,z ∈ A: (x,y) ∈ R ∧ (y,z) ∈ R → (x,z) ∈ R
     *
     * @param relation relation as set of ordered pairs
     * @return true if transitive, false otherwise
     */
    public <T> boolean isTransitive(Set<Pair<T, T>> relation) {
        validateInputs(relation);

        for (Pair<T, T> pairXY : relation) {
            for (Pair<T, T> pairYZ : relation) {
                if (pairXY.second.equals(pairYZ.first)) {
                    Pair<T, T> pairXZ = new Pair<>(pairXY.first, pairYZ.second);
                    if (!relation.contains(pairXZ)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Check if a relation is an equivalence relation.
     *
     * An equivalence relation is reflexive, symmetric, and transitive.
     *
     * @param domain domain set
     * @param relation relation as set of ordered pairs
     * @return true if equivalence relation, false otherwise
     */
    public <T> boolean isEquivalenceRelation(Set<T> domain, Set<Pair<T, T>> relation) {
        return isReflexive(domain, relation) && isSymmetric(relation) && isTransitive(relation);
    }

    /**
     * Calculate equivalence classes for an equivalence relation.
     *
     * @param domain domain set
     * @param relation equivalence relation
     * @return set of equivalence classes
     */
    public <T> Set<Set<T>> equivalenceClasses(Set<T> domain, Set<Pair<T, T>> relation) {
        validateInputs(domain, relation);

        if (!isEquivalenceRelation(domain, relation)) {
            throw new ValidationException("Relation must be an equivalence relation", OPERATION_NAME);
        }

        Set<Set<T>> classes = new HashSet<>();
        Set<T> visited = new HashSet<>();

        for (T element : domain) {
            if (!visited.contains(element)) {
                Set<T> equivalenceClass = new HashSet<>();
                findEquivalenceClass(element, domain, relation, equivalenceClass, visited);
                classes.add(equivalenceClass);
            }
        }

        return classes;
    }

    /**
     * Helper method to find equivalence class.
     */
    private <T> void findEquivalenceClass(T element, Set<T> domain, Set<Pair<T, T>> relation,
                                        Set<T> equivalenceClass, Set<T> visited) {
        if (visited.contains(element)) {
            return;
        }

        visited.add(element);
        equivalenceClass.add(element);

        for (T other : domain) {
            Pair<T, T> pair1 = new Pair<>(element, other);
            Pair<T, T> pair2 = new Pair<>(other, element);

            if (relation.contains(pair1) && relation.contains(pair2)) {
                findEquivalenceClass(other, domain, relation, equivalenceClass, visited);
            }
        }
    }

    // ===== FUNCTIONS =====

    /**
     * Check if a relation is a function.
     *
     * A relation is a function if each element in domain maps to exactly one element in codomain.
     *
     * @param domain domain set
     * @param codomain codomain set
     * @param relation relation as set of ordered pairs
     * @return true if function, false otherwise
     */
    public <T, U> boolean isFunction(Set<T> domain, Set<U> codomain, Set<Pair<T, U>> relation) {
        validateInputs(domain, codomain, relation);

        // Check that every domain element appears exactly once as first element in pairs
        Map<T, Integer> domainCount = new HashMap<>();
        for (Pair<T, U> pair : relation) {
            domainCount.put(pair.first, domainCount.getOrDefault(pair.first, 0) + 1);
        }

        for (T element : domain) {
            if (domainCount.getOrDefault(element, 0) != 1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a function is injective (one-to-one).
     *
     * ∀x₁,x₂ ∈ A: f(x₁) = f(x₂) → x₁ = x₂
     *
     * @param function function as map
     * @return true if injective, false otherwise
     */
    public <T, U> boolean isInjective(Map<T, U> function) {
        validateInputs(function);

        Set<U> seenValues = new HashSet<>();
        for (U value : function.values()) {
            if (!seenValues.add(value)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a function is surjective (onto).
     *
     * ∀y ∈ B: ∃x ∈ A: f(x) = y
     *
     * @param domain domain set
     * @param codomain codomain set
     * @param function function as map
     * @return true if surjective, false otherwise
     */
    public <T, U> boolean isSurjective(Set<T> domain, Set<U> codomain, Map<T, U> function) {
        validateInputs(domain, codomain, function);

        Set<U> image = new HashSet<>(function.values());
        return image.equals(codomain);
    }

    /**
     * Check if a function is bijective.
     *
     * A function is bijective if it is both injective and surjective.
     *
     * @param domain domain set
     * @param codomain codomain set
     * @param function function as map
     * @return true if bijective, false otherwise
     */
    public <T, U> boolean isBijective(Set<T> domain, Set<U> codomain, Map<T, U> function) {
        return isInjective(function) && isSurjective(domain, codomain, function);
    }

    // ===== SET PROPERTIES =====

    /**
     * Calculate cardinality (size) of a set.
     *
     * @param set input set
     * @return cardinality
     */
    public <T> int cardinality(Set<T> set) {
        validateInputs(set);
        return set.size();
    }

    /**
     * Check if a set is empty.
     *
     * @param set input set
     * @return true if empty, false otherwise
     */
    public <T> boolean isEmpty(Set<T> set) {
        validateInputs(set);
        return set.isEmpty();
    }

    /**
     * Check if a set is finite.
     *
     * @param set input set
     * @return true if finite, false otherwise
     */
    public <T> boolean isFinite(Set<T> set) {
        validateInputs(set);
        return true; // All Java sets are finite
    }

    /**
     * Check if two sets are equal.
     *
     * @param setA first set
     * @param setB second set
     * @return true if equal, false otherwise
     */
    public <T> boolean equals(Set<T> setA, Set<T> setB) {
        validateInputs(setA, setB);
        return setA.equals(setB);
    }

    // ===== LATTICE OPERATIONS =====

    /**
     * Calculate the meet (greatest lower bound) of two sets in the subset lattice.
     *
     * @param setA first set
     * @param setB second set
     * @return meet (intersection)
     */
    public <T> Set<T> meet(Set<T> setA, Set<T> setB) {
        return intersection(setA, setB);
    }

    /**
     * Calculate the join (least upper bound) of two sets in the subset lattice.
     *
     * @param setA first set
     * @param setB second set
     * @return join (union)
     */
    public <T> Set<T> join(Set<T> setA, Set<T> setB) {
        return union(setA, setB);
    }

    // ===== UTILITY CLASSES =====

    /**
     * Ordered pair for relations and functions.
     */
    public static class Pair<T, U> {
        public final T first;
        public final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            Pair<?, ?> pair = (Pair<?, ?>) obj;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }
    }

    // ===== UTILITY METHODS =====

    /**
     * Get current memory usage in bytes.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // ===== SET CREATION UTILITIES =====

    /**
     * Create a set from a collection.
     *
     * @param elements elements to include in set
     * @return new set
     */
    @SafeVarargs
    public static <T> Set<T> createSet(T... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    /**
     * Create an empty set.
     *
     * @return empty set
     */
    public static <T> Set<T> emptySet() {
        return new HashSet<>();
    }

    /**
     * Create a singleton set containing one element.
     *
     * @param element the element
     * @return singleton set
     */
    public static <T> Set<T> singletonSet(T element) {
        Set<T> set = new HashSet<>();
        set.add(element);
        return set;
    }
}
