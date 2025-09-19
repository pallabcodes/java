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

package com.netflix.mathlib.graph.datastructures;

/**
 * Disjoint Set (Union-Find) data structure with path compression and union by rank.
 *
 * Used for efficiently tracking connected components in graphs and implementing
 * Kruskal's Minimum Spanning Tree algorithm.
 *
 * Time Complexity:
 * - Find: O(α(n)) amortized (nearly O(1))
 * - Union: O(α(n)) amortized (nearly O(1))
 * where α(n) is the inverse Ackermann function
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class DisjointSet {

    private final int[] parent;
    private final int[] rank;
    private final int size;

    /**
     * Constructor for DisjointSet.
     *
     * @param size number of elements
     */
    public DisjointSet(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        this.size = size;
        this.parent = new int[size];
        this.rank = new int[size];

        // Initialize each element as its own parent
        for (int i = 0; i < size; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    /**
     * Find the representative (root) of the set containing element x.
     * Uses path compression for optimization.
     *
     * @param x element to find
     * @return root of the set containing x
     */
    public int find(int x) {
        validateElement(x);

        if (parent[x] != x) {
            // Path compression: make parent[x] point directly to root
            parent[x] = find(parent[x]);
        }

        return parent[x];
    }

    /**
     * Union two sets containing elements x and y.
     * Uses union by rank for optimization.
     *
     * @param x first element
     * @param y second element
     */
    public void union(int x, int y) {
        validateElement(x);
        validateElement(y);

        int rootX = find(x);
        int rootY = find(y);

        if (rootX != rootY) {
            // Union by rank: attach smaller rank tree under root of higher rank tree
            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else {
                // Same rank: attach y's tree under x's tree and increment rank
                parent[rootY] = rootX;
                rank[rootX]++;
            }
        }
    }

    /**
     * Check if two elements are in the same set.
     *
     * @param x first element
     * @param y second element
     * @return true if in same set, false otherwise
     */
    public boolean connected(int x, int y) {
        validateElement(x);
        validateElement(y);
        return find(x) == find(y);
    }

    /**
     * Get the number of elements in this disjoint set.
     *
     * @return number of elements
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the number of distinct sets (connected components).
     *
     * @return number of sets
     */
    public int getNumberOfSets() {
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (parent[i] == i) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get all elements in the same set as the given element.
     *
     * @param x element to query
     * @return array of elements in the same set
     */
    public int[] getSet(int x) {
        validateElement(x);

        int root = find(x);
        int count = 0;

        // Count elements in set
        for (int i = 0; i < size; i++) {
            if (find(i) == root) {
                count++;
            }
        }

        // Collect elements
        int[] set = new int[count];
        int index = 0;
        for (int i = 0; i < size; i++) {
            if (find(i) == root) {
                set[index++] = i;
            }
        }

        return set;
    }

    /**
     * Reset the disjoint set to initial state.
     */
    public void reset() {
        for (int i = 0; i < size; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    /**
     * Validate element index.
     */
    private void validateElement(int x) {
        if (x < 0 || x >= size) {
            throw new IllegalArgumentException("Invalid element: " + x);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DisjointSet (").append(size).append(" elements, ")
          .append(getNumberOfSets()).append(" sets)\n");

        sb.append("Parent: ");
        for (int i = 0; i < size; i++) {
            sb.append(parent[i]);
            if (i < size - 1) sb.append(", ");
        }
        sb.append("\n");

        sb.append("Rank:   ");
        for (int i = 0; i < size; i++) {
            sb.append(rank[i]);
            if (i < size - 1) sb.append(", ");
        }

        return sb.toString();
    }
}
