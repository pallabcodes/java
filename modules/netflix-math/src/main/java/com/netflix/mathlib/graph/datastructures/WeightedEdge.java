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
 * Weighted Edge data structure for weighted graphs.
 *
 * Represents a connection between two vertices with an associated weight/cost.
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class WeightedEdge {

    public final int from;
    public final int to;
    public final double weight;

    /**
     * Constructor for WeightedEdge.
     *
     * @param from source vertex
     * @param to destination vertex
     * @param weight edge weight/cost
     */
    public WeightedEdge(int from, int to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        WeightedEdge edge = (WeightedEdge) obj;
        return from == edge.from && to == edge.to &&
               Double.compare(edge.weight, weight) == 0;
    }

    @Override
    public int hashCode() {
        int result = 31 * from + to;
        long temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.format("(%.2f) %d -> %d", weight, from, to);
    }
}
