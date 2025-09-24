package com.netflix.productivity.api;

public final class ErrorCodes {
    private ErrorCodes() {}

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String RATE_LIMITED = "RATE_LIMITED";

    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_401_001";
    public static final String AUTH_REQUIRED = "AUTH_401_000";
    public static final String AUTH_FORBIDDEN = "AUTH_403_000";
}


