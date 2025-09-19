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
 * Edge data structure for unweighted graphs.
 *
 * Represents a connection between two vertices in a graph.
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class Edge {

    public final int from;
    public final int to;

    /**
     * Constructor for Edge.
     *
     * @param from source vertex
     * @param to destination vertex
     */
    public Edge(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Edge edge = (Edge) obj;
        return from == edge.from && to == edge.to;
    }

    @Override
    public int hashCode() {
        return 31 * from + to;
    }

    @Override
    public String toString() {
        return "(" + from + " -> " + to + ")";
    }
}
