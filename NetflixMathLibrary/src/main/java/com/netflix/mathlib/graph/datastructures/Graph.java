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
 * Graph data structure for unweighted graphs.
 *
 * Supports both directed and undirected graphs with adjacency list representation.
 * Provides methods for adding/removing vertices and edges, and querying graph properties.
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class Graph {

    private final int vertices;
    private final List<List<Integer>> adjacencyList;
    private final boolean directed;
    private int edges;

    /**
     * Constructor for Graph.
     *
     * @param vertices number of vertices
     * @param directed true for directed graph, false for undirected
     */
    public Graph(int vertices, boolean directed) {
        if (vertices <= 0) {
            throw new IllegalArgumentException("Number of vertices must be positive");
        }

        this.vertices = vertices;
        this.directed = directed;
        this.edges = 0;
        this.adjacencyList = new ArrayList<>(vertices);

        for (int i = 0; i < vertices; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    /**
     * Add an edge between two vertices.
     *
     * @param from source vertex
     * @param to destination vertex
     */
    public void addEdge(int from, int to) {
        validateVertex(from);
        validateVertex(to);

        adjacencyList.get(from).add(to);
        edges++;

        if (!directed) {
            adjacencyList.get(to).add(from);
        }
    }

    /**
     * Remove an edge between two vertices.
     *
     * @param from source vertex
     * @param to destination vertex
     */
    public void removeEdge(int from, int to) {
        validateVertex(from);
        validateVertex(to);

        adjacencyList.get(from).remove(Integer.valueOf(to));
        edges--;

        if (!directed) {
            adjacencyList.get(to).remove(Integer.valueOf(from));
        }
    }

    /**
     * Check if edge exists between two vertices.
     *
     * @param from source vertex
     * @param to destination vertex
     * @return true if edge exists, false otherwise
     */
    public boolean hasEdge(int from, int to) {
        validateVertex(from);
        validateVertex(to);
        return adjacencyList.get(from).contains(to);
    }

    /**
     * Get adjacency list for a vertex.
     *
     * @param vertex vertex to query
     * @return list of adjacent vertices
     */
    public List<Integer> getAdjacencyList(int vertex) {
        validateVertex(vertex);
        return new ArrayList<>(adjacencyList.get(vertex));
    }

    /**
     * Get degree of a vertex.
     *
     * @param vertex vertex to query
     * @return degree of vertex
     */
    public int getDegree(int vertex) {
        validateVertex(vertex);
        return adjacencyList.get(vertex).size();
    }

    /**
     * Get in-degree of a vertex (for directed graphs).
     *
     * @param vertex vertex to query
     * @return in-degree of vertex
     */
    public int getInDegree(int vertex) {
        validateVertex(vertex);

        if (!directed) {
            return getDegree(vertex);
        }

        int inDegree = 0;
        for (int i = 0; i < vertices; i++) {
            if (adjacencyList.get(i).contains(vertex)) {
                inDegree++;
            }
        }
        return inDegree;
    }

    /**
     * Get out-degree of a vertex (for directed graphs).
     *
     * @param vertex vertex to query
     * @return out-degree of vertex
     */
    public int getOutDegree(int vertex) {
        validateVertex(vertex);
        return getDegree(vertex);
    }

    /**
     * Get number of vertices in graph.
     *
     * @return number of vertices
     */
    public int getVertices() {
        return vertices;
    }

    /**
     * Get number of edges in graph.
     *
     * @return number of edges
     */
    public int getEdges() {
        return edges;
    }

    /**
     * Check if graph is directed.
     *
     * @return true if directed, false if undirected
     */
    public boolean isDirected() {
        return directed;
    }

    /**
     * Check if graph is empty.
     *
     * @return true if no edges, false otherwise
     */
    public boolean isEmpty() {
        return edges == 0;
    }

    /**
     * Get all edges in the graph.
     *
     * @return list of all edges
     */
    public List<Edge> getAllEdges() {
        List<Edge> allEdges = new ArrayList<>();

        if (directed) {
            for (int i = 0; i < vertices; i++) {
                for (int j : adjacencyList.get(i)) {
                    allEdges.add(new Edge(i, j));
                }
            }
        } else {
            // For undirected graphs, avoid duplicate edges
            boolean[][] visited = new boolean[vertices][vertices];

            for (int i = 0; i < vertices; i++) {
                for (int j : adjacencyList.get(i)) {
                    if (!visited[i][j] && !visited[j][i]) {
                        allEdges.add(new Edge(i, j));
                        visited[i][j] = true;
                        visited[j][i] = true;
                    }
                }
            }
        }

        return allEdges;
    }

    /**
     * Create a copy of this graph.
     *
     * @return copy of the graph
     */
    public Graph copy() {
        Graph copy = new Graph(vertices, directed);

        for (int i = 0; i < vertices; i++) {
            for (int neighbor : adjacencyList.get(i)) {
                copy.addEdge(i, neighbor);
            }
        }

        return copy;
    }

    /**
     * Get graph representation as adjacency matrix.
     *
     * @return adjacency matrix
     */
    public boolean[][] getAdjacencyMatrix() {
        boolean[][] matrix = new boolean[vertices][vertices];

        for (int i = 0; i < vertices; i++) {
            for (int j : adjacencyList.get(i)) {
                matrix[i][j] = true;
            }
        }

        return matrix;
    }

    /**
     * Validate vertex index.
     */
    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= vertices) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph (").append(directed ? "directed" : "undirected")
          .append(", ").append(vertices).append(" vertices, ")
          .append(edges).append(" edges)\n");

        for (int i = 0; i < vertices; i++) {
            sb.append(i).append(": ");
            List<Integer> neighbors = adjacencyList.get(i);
            if (neighbors.isEmpty()) {
                sb.append("[]");
            } else {
                sb.append(neighbors);
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
