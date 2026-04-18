package com.backend.designpatterns.behavioral.observer;

/**
 * Step 1: THE EVENT PAYLOAD (Modern Context)
 * 
 * An immutable record representing a distinct business action in Google Docs.
 */
public record Step01_DocumentEditEvent(
    String documentId,
    String collaboratorEmail,
    String textChange, // e.g., "Added letter 'A'"
    int cursorPosition,
    java.time.Instant timestamp
) {}
