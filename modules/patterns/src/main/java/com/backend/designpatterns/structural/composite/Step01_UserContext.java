package com.backend.designpatterns.structural.composite;

/**
 * Step 1: EVALUATION CONTEXT
 */
public record Step01_UserContext(
    String username,
    String role,
    String department,
    int currentHour
) {}
