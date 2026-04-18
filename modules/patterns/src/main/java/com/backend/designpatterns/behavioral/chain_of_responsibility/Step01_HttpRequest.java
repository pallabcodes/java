package com.backend.designpatterns.behavioral.chain_of_responsibility;

/**
 * Step 1: THE PAYLOAD (Context)
 * 
 * Modern Java Record holding the data that needs to be validated and processed.
 */
public record Step01_HttpRequest(
    String ipAddress,
    String endpoint,
    String bearerToken,
    String body
) {}
