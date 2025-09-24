package com.netflix.productivity.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final int status;
    private final String message;
    private final T data;
    private final Object error;
    private final String errorCode;
    private final LocalDateTime timestamp;
    private final String correlationId;
    private final Map<String, Object> metadata;

    private ApiResponse(boolean success, int status, String message, T data, Object error,
                        String errorCode, String correlationId, Map<String, Object> metadata) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
        this.correlationId = correlationId;
        this.metadata = metadata;
        this.errorCode = errorCode;
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(true, status, message, data, null, null, null, null);
    }

    public static <T> ApiResponse<T> success(int status, String message, T data, Map<String, Object> metadata) {
        return new ApiResponse<>(true, status, message, data, null, null, null, metadata);
    }

    public static <T> ApiResponse<T> error(int status, String message, Object error) {
        return new ApiResponse<>(false, status, message, null, error, null, null, null);
    }

    public static <T> ApiResponse<T> error(int status, String message, Object error, String errorCode) {
        return new ApiResponse<>(false, status, message, null, error, errorCode, null, null);
    }

    public static <T> ApiResponse<T> successWith(int status, String message, T data, Map<String, Object> metadata, String correlationId) {
        return new ApiResponse<>(true, status, message, data, null, null, correlationId, metadata);
    }

    public static <T> ApiResponse<T> errorWith(int status, String message, Object error, String errorCode, String correlationId) {
        return new ApiResponse<>(false, status, message, null, error, errorCode, correlationId, null);
    }

    public boolean isSuccess() { return success; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public Object getError() { return error; }
    public String getErrorCode() { return errorCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getCorrelationId() { return correlationId; }
    public Map<String, Object> getMetadata() { return metadata; }
}


