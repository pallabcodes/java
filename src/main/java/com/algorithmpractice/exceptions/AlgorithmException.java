package com.algorithmpractice.exceptions;

/**
 * Base exception class for all algorithm-related errors in the application.
 * 
 * <p>This exception provides a common base for all algorithm-specific exceptions
 * and includes additional context information for debugging.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public class AlgorithmException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new AlgorithmException with the specified detail message.
     * 
     * @param message the detail message
     */
    public AlgorithmException(final String message) {
        super(message);
    }

    /**
     * Constructs a new AlgorithmException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause   the cause
     */
    public AlgorithmException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AlgorithmException with the specified cause.
     * 
     * @param cause the cause
     */
    public AlgorithmException(final Throwable cause) {
        super(cause);
    }
}
