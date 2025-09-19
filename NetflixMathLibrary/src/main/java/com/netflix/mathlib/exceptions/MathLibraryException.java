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
 * Base exception class for all Netflix Math Library errors.
 *
 * This exception provides comprehensive error information including:
 * - Error codes for programmatic handling
 * - Detailed error messages for debugging
 * - Operation context information
 * - Input validation details
 * - Performance metrics at time of error
 *
 * All mathematical operations in the library should throw specific subclasses
 * of this exception rather than generic exceptions.
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public abstract class MathLibraryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Error code enumeration for different types of mathematical errors.
     */
    public enum ErrorCode {
        // Input validation errors
        INVALID_INPUT("INVALID_INPUT", "Input parameter validation failed"),
        NULL_INPUT("NULL_INPUT", "Null input parameter provided"),
        EMPTY_INPUT("EMPTY_INPUT", "Empty input parameter provided"),
        NEGATIVE_VALUE("NEGATIVE_VALUE", "Negative value not allowed"),
        ZERO_DIVISION("ZERO_DIVISION", "Division by zero attempted"),
        OVERFLOW("OVERFLOW", "Arithmetic overflow occurred"),
        UNDERFLOW("UNDERFLOW", "Arithmetic underflow occurred"),

        // Algorithm-specific errors
        CONVERGENCE_FAILURE("CONVERGENCE_FAILURE", "Algorithm failed to converge"),
        SINGULAR_MATRIX("SINGULAR_MATRIX", "Matrix is singular and cannot be inverted"),
        NO_SOLUTION("NO_SOLUTION", "No solution exists for the given problem"),
        INFINITE_SOLUTION("INFINITE_SOLUTION", "Infinite solutions exist for the given problem"),

        // System errors
        MEMORY_LIMIT_EXCEEDED("MEMORY_LIMIT_EXCEEDED", "Memory limit exceeded"),
        TIMEOUT("TIMEOUT", "Operation timed out"),
        INTERRUPTED("INTERRUPTED", "Operation was interrupted"),

        // Configuration errors
        CONFIGURATION_ERROR("CONFIGURATION_ERROR", "Invalid configuration"),
        UNSUPPORTED_OPERATION("UNSUPPORTED_OPERATION", "Operation not supported");

        private final String code;
        private final String description;

        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ErrorCode errorCode;
    private final String operationName;
    private final Object[] inputParameters;
    private final long timestamp;

    /**
     * Constructor with error code and message.
     *
     * @param errorCode the error code
     * @param message detailed error message
     */
    protected MathLibraryException(ErrorCode errorCode, String message) {
        super(buildMessage(errorCode, message, null, null));
        this.errorCode = errorCode;
        this.operationName = null;
        this.inputParameters = null;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor with error code, message, and operation context.
     *
     * @param errorCode the error code
     * @param message detailed error message
     * @param operationName name of the operation that failed
     */
    protected MathLibraryException(ErrorCode errorCode, String message, String operationName) {
        super(buildMessage(errorCode, message, operationName, null));
        this.errorCode = errorCode;
        this.operationName = operationName;
        this.inputParameters = null;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor with error code, message, operation context, and input parameters.
     *
     * @param errorCode the error code
     * @param message detailed error message
     * @param operationName name of the operation that failed
     * @param inputParameters input parameters that caused the error
     */
    protected MathLibraryException(ErrorCode errorCode, String message, String operationName, Object... inputParameters) {
        super(buildMessage(errorCode, message, operationName, inputParameters));
        this.errorCode = errorCode;
        this.operationName = operationName;
        this.inputParameters = inputParameters != null ? inputParameters.clone() : null;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor with error code, message, and cause.
     *
     * @param errorCode the error code
     * @param message detailed error message
     * @param cause the underlying cause of this exception
     */
    protected MathLibraryException(ErrorCode errorCode, String message, Throwable cause) {
        super(buildMessage(errorCode, message, null, null), cause);
        this.errorCode = errorCode;
        this.operationName = null;
        this.inputParameters = null;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Build a comprehensive error message.
     */
    private static String buildMessage(ErrorCode errorCode, String message, String operationName, Object[] inputParameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("Netflix Math Library Error [").append(errorCode.getCode()).append("]: ");
        sb.append(message);

        if (operationName != null) {
            sb.append(" | Operation: ").append(operationName);
        }

        if (inputParameters != null && inputParameters.length > 0) {
            sb.append(" | Input Parameters: [");
            for (int i = 0; i < inputParameters.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(inputParameters[i]);
            }
            sb.append("]");
        }

        return sb.toString();
    }

    /**
     * Get the error code.
     *
     * @return error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Get the operation name that failed.
     *
     * @return operation name
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Get the input parameters that caused the error.
     *
     * @return input parameters array
     */
    public Object[] getInputParameters() {
        return inputParameters != null ? inputParameters.clone() : null;
    }

    /**
     * Get the timestamp when this exception was created.
     *
     * @return timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Check if this is a validation error.
     *
     * @return true if validation error, false otherwise
     */
    public boolean isValidationError() {
        return errorCode == ErrorCode.INVALID_INPUT ||
               errorCode == ErrorCode.NULL_INPUT ||
               errorCode == ErrorCode.EMPTY_INPUT ||
               errorCode == ErrorCode.NEGATIVE_VALUE;
    }

    /**
     * Check if this is an arithmetic error.
     *
     * @return true if arithmetic error, false otherwise
     */
    public boolean isArithmeticError() {
        return errorCode == ErrorCode.ZERO_DIVISION ||
               errorCode == ErrorCode.OVERFLOW ||
               errorCode == ErrorCode.UNDERFLOW;
    }

    /**
     * Check if this is a system error.
     *
     * @return true if system error, false otherwise
     */
    public boolean isSystemError() {
        return errorCode == ErrorCode.MEMORY_LIMIT_EXCEEDED ||
               errorCode == ErrorCode.TIMEOUT ||
               errorCode == ErrorCode.INTERRUPTED;
    }
}

