/*
 * Copyright 2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.mathlib.core;

import com.netflix.mathlib.monitoring.OperationMetrics;

/**
 * Base interface for all mathematical operations in Netflix Math Library.
 *
 * This interface defines the contract that all mathematical operations must follow,
 * ensuring consistency, error handling, and performance monitoring across all implementations.
 *
 * Key features:
 * - Standardized error handling with custom exceptions
 * - Performance monitoring and metrics collection
 * - Input validation and sanitization
 * - Thread-safety considerations
 * - Comprehensive logging
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public interface MathOperation {

    /**
     * Get the name of this mathematical operation.
     *
     * @return operation name
     */
    String getOperationName();

    /**
     * Get the complexity class of this operation (e.g., "O(1)", "O(n)", "O(n log n)").
     *
     * @return complexity string
     */
    String getComplexity();

    /**
     * Get performance metrics for this operation.
     *
     * @return operation metrics
     */
    OperationMetrics getMetrics();

    /**
     * Validate input parameters for this operation.
     *
     * @param inputs input parameters to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateInputs(Object... inputs);

    /**
     * Check if this operation is thread-safe.
     *
     * @return true if thread-safe, false otherwise
     */
    boolean isThreadSafe();

    /**
     * Get the version of this operation implementation.
     *
     * @return version string
     */
    String getVersion();
}

