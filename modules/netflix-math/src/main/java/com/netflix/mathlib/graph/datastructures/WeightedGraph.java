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

import java.util.*;

/**
 * Weighted Graph data structure.
 *
 * Extends basic Graph functionality to support edge weights/costs.
 * Uses adjacency list representation with weighted edges.
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class WeightedGraph extends Graph {

    private final List<List<WeightedEdge>> weightedAdjacencyList;

    /**
     * Constructor for WeightedGraph.
     *
     * @param vertices number of vertices
     * @param directed true for directed graph, false for undirected
     */
    public WeightedGraph(int vertices, boolean directed) {
        super(vertices, directed);
        this.weightedAdjacencyList = new ArrayList<>(vertices);

        for (int i = 0; i < vertices; i++) {
            weightedAdjacencyList.add(new ArrayList<>());
        }
    }

    /**
     * Add a weighted edge between two vertices.
     *
     * @param from source vertex
     * @param to destination vertex
     * @param weight edge weight/cost
     */
    public void addWeightedEdge(int from, int to, double weight) {
        validateVertex(from);
        validateVertex(to);

        if (weight < 0) {
            throw new IllegalArgumentException("Edge weight cannot be negative");
        }

        WeightedEdge edge = new WeightedEdge(from, to, weight);
        weightedAdjacencyList.get(from).add(edge);

        // For undirected graphs, add reverse edge
        if (!isDirected()) {
            WeightedEdge reverseEdge = new WeightedEdge(to, from, weight);
            weightedAdjacencyList.get(to).add(reverseEdge);
        }

        // Also add to parent class for basic graph operations
        addEdge(from, to);
    }

    /**
     * Remove a weighted edge between two vertices.
     *
     * @param from source vertex
     * @param to destination vertex
     */
    public void removeWeightedEdge(int from, int to) {
        validateVertex(from);
        validateVertex(to);

        // Remove from weighted adjacency list
        weightedAdjacencyList.get(from).removeIf(edge -> edge.to == to);

        if (!isDirected()) {
            weightedAdjacencyList.get(to).removeIf(edge -> edge.from == from);
        }

        // Also remove from parent class
        removeEdge(from, to);
    }

    /**
     * Get weighted adjacency list for a vertex.
     *
     * @param vertex vertex to query
     * @return list of weighted edges from the vertex
     */
    public List<WeightedEdge> getWeightedAdjacencyList(int vertex) {
        validateVertex(vertex);
        return new ArrayList<>(weightedAdjacencyList.get(vertex));
    }

    /**
     * Get weight of edge between two vertices.
     *
     * @param from source vertex
     * @param to destination vertex
     * @return edge weight, or Double.POSITIVE_INFINITY if no edge exists
     */
    public double getEdgeWeight(int from, int to) {
        validateVertex(from);
        validateVertex(to);

        for (WeightedEdge edge : weightedAdjacencyList.get(from)) {
            if (edge.to == to) {
                return edge.weight;
            }
        }

        return Double.POSITIVE_INFINITY;
    }

    /**
     * Check if weighted edge exists between two vertices.
     *
     * @param from source vertex
     * @param to destination vertex
     * @return true if edge exists, false otherwise
     */
    public boolean hasWeightedEdge(int from, int to) {
        return getEdgeWeight(from, to) != Double.POSITIVE_INFINITY;
    }

    /**
     * Get all edges in the weighted graph.
     *
     * @return list of all weighted edges
     */
    public List<WeightedEdge> getAllWeightedEdges() {
        List<WeightedEdge> allEdges = new ArrayList<>();

        if (isDirected()) {
            for (List<WeightedEdge> edges : weightedAdjacencyList) {
                allEdges.addAll(edges);
            }
        } else {
            // For undirected graphs, avoid duplicate edges
            Set<String> visited = new HashSet<>();

            for (List<WeightedEdge> edges : weightedAdjacencyList) {
                for (WeightedEdge edge : edges) {
                    String key = Math.min(edge.from, edge.to) + "," + Math.max(edge.from, edge.to);
                    if (!visited.contains(key)) {
                        allEdges.add(edge);
                        visited.add(key);
                    }
                }
            }
        }

        return allEdges;
    }

    /**
     * Get adjacency matrix with weights.
     *
     * @return weight matrix (INF for no edge)
     */
    public double[][] getWeightMatrix() {
        int vertices = getVertices();
        double[][] matrix = new double[vertices][vertices];

        // Initialize with infinity
        for (int i = 0; i < vertices; i++) {
            Arrays.fill(matrix[i], Double.POSITIVE_INFINITY);
            matrix[i][i] = 0; // Self-loops have weight 0
        }

        // Set edge weights
        for (int i = 0; i < vertices; i++) {
            for (WeightedEdge edge : weightedAdjacencyList.get(i)) {
                matrix[i][edge.to] = edge.weight;
            }
        }

        return matrix;
    }

    /**
     * Create a copy of this weighted graph.
     *
     * @return copy of the weighted graph
     */
    public WeightedGraph copy() {
        WeightedGraph copy = new WeightedGraph(getVertices(), isDirected());

        for (int i = 0; i < getVertices(); i++) {
            for (WeightedEdge edge : weightedAdjacencyList.get(i)) {
                copy.addWeightedEdge(edge.from, edge.to, edge.weight);
            }
        }

        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Weighted Graph (").append(isDirected() ? "directed" : "undirected")
          .append(", ").append(getVertices()).append(" vertices, ")
          .append(getEdges()).append(" edges)\n");

        for (int i = 0; i < getVertices(); i++) {
            sb.append(i).append(": ");
            List<WeightedEdge> edges = weightedAdjacencyList.get(i);
            if (edges.isEmpty()) {
                sb.append("[]");
            } else {
                sb.append(edges);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Validate vertex index.
     */
    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= getVertices()) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
    }
}
