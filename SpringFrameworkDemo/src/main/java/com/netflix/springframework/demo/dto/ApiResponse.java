package com.netflix.springframework.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ApiResponse - Generic API response wrapper
 * 
 * This class demonstrates:
 * 1. Generic response wrapper pattern
 * 2. Consistent API response structure
 * 3. JSON serialization with custom field names
 * 4. Generic type handling
 * 
 * For C/C++ engineers:
 * - This is like a response wrapper struct in C++
 * - Generic types are similar to templates in C++
 * - @JsonProperty is like field mapping for JSON
 * - Consistent response structure helps with API clients
 * 
 * @param <T> The type of data being returned
 * @author Netflix SDE-2 Team
 */
public class ApiResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    /**
     * Default constructor
     * 
     * Required for JSON deserialization
     * Similar to default constructor in C++
     */
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
        System.out.println("ApiResponse default constructor called");
    }
    
    /**
     * Parameterized constructor
     * 
     * @param success Whether the operation was successful
     * @param message Response message
     * @param data Response data
     */
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        System.out.println("ApiResponse parameterized constructor called: " + success);
    }
    
    /**
     * Static factory method for success response
     * 
     * @param message Success message
     * @param data Response data
     * @param <T> Type of data
     * @return Success ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    /**
     * Static factory method for success response with data only
     * 
     * @param data Response data
     * @param <T> Type of data
     * @return Success ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data);
    }
    
    /**
     * Static factory method for error response
     * 
     * @param message Error message
     * @param <T> Type of data
     * @return Error ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
    
    /**
     * Static factory method for error response with data
     * 
     * @param message Error message
     * @param data Error data
     * @param <T> Type of data
     * @return Error ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
    
    // Getters and Setters
    // In C++, these would be like getter/setter methods or public member access
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Check if response is successful
     * 
     * @return true if success is true
     */
    public boolean isSuccessful() {
        return success;
    }
    
    /**
     * Check if response has data
     * 
     * @return true if data is not null
     */
    public boolean hasData() {
        return data != null;
    }
    
    /**
     * toString method for debugging and logging
     * 
     * Similar to operator<< in C++ or toString methods
     */
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
    
    /**
     * equals method for object comparison
     * 
     * Similar to operator== in C++
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ApiResponse<?> that = (ApiResponse<?>) obj;
        return success == that.success &&
               timestamp == that.timestamp &&
               (message != null ? message.equals(that.message) : that.message == null) &&
               (data != null ? data.equals(that.data) : that.data == null);
    }
    
    /**
     * hashCode method for hash-based collections
     * 
     * Similar to hash functions in C++
     */
    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
