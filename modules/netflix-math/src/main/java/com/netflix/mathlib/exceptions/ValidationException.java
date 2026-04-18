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

package com.netflix.mathlib.exceptions;

/**
 * Exception thrown when input validation fails in mathematical operations.
 *
 * This exception is specifically for validation errors such as:
 * - Null or empty inputs
 * - Invalid parameter ranges
 * - Type mismatches
 * - Constraint violations
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class ValidationException extends MathLibraryException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with validation message.
     *
     * @param message detailed validation error message
     */
    public ValidationException(String message) {
        super(ErrorCode.INVALID_INPUT, message);
    }

    /**
     * Constructor with validation message and operation context.
     *
     * @param message detailed validation error message
     * @param operationName name of the operation that failed validation
     */
    public ValidationException(String message, String operationName) {
        super(ErrorCode.INVALID_INPUT, message, operationName);
    }

    /**
     * Constructor with validation message, operation context, and input parameters.
     *
     * @param message detailed validation error message
     * @param operationName name of the operation that failed validation
     * @param inputParameters input parameters that failed validation
     */
    public ValidationException(String message, String operationName, Object... inputParameters) {
        super(ErrorCode.INVALID_INPUT, message, operationName, inputParameters);
    }

    /**
     * Constructor for null input validation error.
     *
     * @param parameterName name of the null parameter
     * @param operationName name of the operation
     */
    public static ValidationException nullParameter(String parameterName, String operationName) {
        return new ValidationException(
            String.format("Parameter '%s' cannot be null", parameterName),
            operationName,
            parameterName
        );
    }

    /**
     * Constructor for empty input validation error.
     *
     * @param parameterName name of the empty parameter
     * @param operationName name of the operation
     */
    public static ValidationException emptyParameter(String parameterName, String operationName) {
        return new ValidationException(
            String.format("Parameter '%s' cannot be empty", parameterName),
            operationName,
            parameterName
        );
    }

    /**
     * Constructor for negative value validation error.
     *
     * @param parameterName name of the parameter with negative value
     * @param value the negative value
     * @param operationName name of the operation
     */
    public static ValidationException negativeValue(String parameterName, Number value, String operationName) {
        return new ValidationException(
            String.format("Parameter '%s' must be non-negative, got: %s", parameterName, value),
            operationName,
            parameterName, value
        );
    }

    /**
     * Constructor for invalid range validation error.
     *
     * @param parameterName name of the parameter
     * @param value the invalid value
     * @param minValue minimum allowed value
     * @param maxValue maximum allowed value
     * @param operationName name of the operation
     */
    public static ValidationException invalidRange(String parameterName, Number value,
                                                  Number minValue, Number maxValue, String operationName) {
        return new ValidationException(
            String.format("Parameter '%s' must be between %s and %s, got: %s",
                         parameterName, minValue, maxValue, value),
            operationName,
            parameterName, value, minValue, maxValue
        );
    }

    /**
     * Constructor for invalid type validation error.
     *
     * @param parameterName name of the parameter
     * @param expectedType expected type
     * @param actualType actual type
     * @param operationName name of the operation
     */
    public static ValidationException invalidType(String parameterName, String expectedType,
                                                 String actualType, String operationName) {
        return new ValidationException(
            String.format("Parameter '%s' must be of type %s, got: %s",
                         parameterName, expectedType, actualType),
            operationName,
            parameterName, expectedType, actualType
        );
    }
}

