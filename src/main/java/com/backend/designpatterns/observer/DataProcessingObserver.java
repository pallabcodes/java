package com.backend.designpatterns.observer;

import java.util.Map;

/**
 * Netflix Production-Grade Observer Pattern Interface
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Observer pattern interface design
 * - Generic event data handling
 * - Production-grade method signatures
 * - Netflix-style event processing contracts
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
public interface DataProcessingObserver {
    
    /**
     * Handle data processing events
     * 
     * @param eventType the type of event
     * @param eventData the event data
     */
    void onDataProcessingEvent(String eventType, Map<String, Object> eventData);
    
    /**
     * Get observer identifier
     * 
     * @return observer ID
     */
    String getObserverId();
    
    /**
     * Check if observer is active
     * 
     * @return true if observer is active
     */
    boolean isActive();
    
    /**
     * Get observer metadata
     * 
     * @return observer information
     */
    Map<String, Object> getObserverInfo();
}
