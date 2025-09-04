package com.algorithmpractice.exceptions;

/**
 * Custom exception for data structure-related errors.
 * 
 * <p>This exception is thrown when data structure operations fail,
 * such as accessing elements from an empty structure, invalid indices,
 * or unsupported operations. It provides clear error messages for
 * debugging and error handling.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public class DataStructureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new DataStructureException with the specified detail message.
     * 
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     */
    public DataStructureException(final String message) {
        super(message);
    }

    /**
     * Constructs a new DataStructureException with the specified detail message
     * and cause.
     * 
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method)
     */
    public DataStructureException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new DataStructureException with the specified cause and a
     * detail message of (cause==null ? null : cause.toString()).
     * 
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method)
     */
    public DataStructureException(final Throwable cause) {
        super(cause);
    }
}
