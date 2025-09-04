package com.backend.designpatterns.observer;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Observer Subject Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Observer pattern subject implementation
 * - Thread-safe observer management
 * - Advanced HashMap operations and iterations
 * - Stream operations for observer processing
 * - Optional usage for null safety
 * - Collection framework best practices
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
@Component
public class DataProcessingSubject {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, List<DataProcessingObserver>> observerRegistry = new ConcurrentHashMap<>();
    private final Map<String, Object> eventHistory = new ConcurrentHashMap<>();
    
    // Production-grade: Observer management
    private final List<DataProcessingObserver> globalObservers = new CopyOnWriteArrayList<>();
    
    /**
     * Register observer for specific event type
     * 
     * @param eventType the event type to observe
     * @param observer the observer to register
     */
    public void registerObserver(String eventType, DataProcessingObserver observer) {
        // Production-grade: Using HashMap with proper iteration
        observerRegistry.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(observer);
        
        // Update event history
        eventHistory.put("observer_registered_" + eventType, System.currentTimeMillis());
    }
    
    /**
     * Register global observer for all events
     * 
     * @param observer the global observer
     */
    public void registerGlobalObserver(DataProcessingObserver observer) {
        globalObservers.add(observer);
        eventHistory.put("global_observer_registered", System.currentTimeMillis());
    }
    
    /**
     * Unregister observer from specific event type
     * 
     * @param eventType the event type
     * @param observer the observer to unregister
     */
    public void unregisterObserver(String eventType, DataProcessingObserver observer) {
        // Production-grade: HashMap iteration with Streams
        Optional.ofNullable(observerRegistry.get(eventType))
                .ifPresent(observers -> observers.remove(observer));
        
        eventHistory.put("observer_unregistered_" + eventType, System.currentTimeMillis());
    }
    
    /**
     * Unregister global observer
     * 
     * @param observer the global observer to unregister
     */
    public void unregisterGlobalObserver(DataProcessingObserver observer) {
        globalObservers.remove(observer);
        eventHistory.put("global_observer_unregistered", System.currentTimeMillis());
    }
    
    /**
     * Notify all observers of an event
     * 
     * @param eventType the event type
     * @param eventData the event data
     */
    public void notifyObservers(String eventType, Map<String, Object> eventData) {
        // Production-grade: Event data preparation with HashMap
        Map<String, Object> enhancedEventData = new HashMap<>(eventData);
        enhancedEventData.put("eventType", eventType);
        enhancedEventData.put("timestamp", System.currentTimeMillis());
        enhancedEventData.put("eventId", UUID.randomUUID().toString());
        
        // Notify specific event observers using HashMap iteration
        Optional.ofNullable(observerRegistry.get(eventType))
                .ifPresent(observers -> {
                    // Method 1: Traditional for-each iteration
                    for (DataProcessingObserver observer : observers) {
                        if (observer.isActive()) {
                            try {
                                observer.onDataProcessingEvent(eventType, enhancedEventData);
                            } catch (Exception e) {
                                // Production-grade: Error handling
                                enhancedEventData.put("error", "Observer notification failed: " + e.getMessage());
                            }
                        }
                    }
                });
        
        // Notify global observers using Stream operations
        globalObservers.stream()
                .filter(DataProcessingObserver::isActive)
                .forEach(observer -> {
                    try {
                        observer.onDataProcessingEvent(eventType, enhancedEventData);
                    } catch (Exception e) {
                        enhancedEventData.put("error", "Global observer notification failed: " + e.getMessage());
                    }
                });
        
        // Update event history
        updateEventHistory(eventType, enhancedEventData);
    }
    
    /**
     * Get all registered observers for an event type
     * 
     * @param eventType the event type
     * @return list of observers
     */
    public List<DataProcessingObserver> getObservers(String eventType) {
        return Optional.ofNullable(observerRegistry.get(eventType))
                .map(ArrayList::new)
                .orElse(new ArrayList<>());
    }
    
    /**
     * Get all global observers
     * 
     * @return list of global observers
     */
    public List<DataProcessingObserver> getGlobalObservers() {
        return new ArrayList<>(globalObservers);
    }
    
    /**
     * Get observer statistics using HashMap operations
     * 
     * @return observer statistics
     */
    public Map<String, Object> getObserverStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Production-grade: HashMap iteration patterns
        stats.put("totalEventTypes", observerRegistry.size());
        stats.put("totalGlobalObservers", globalObservers.size());
        
        // Method 1: Using entrySet iteration
        int totalSpecificObservers = 0;
        for (Map.Entry<String, List<DataProcessingObserver>> entry : observerRegistry.entrySet()) {
            totalSpecificObservers += entry.getValue().size();
        }
        stats.put("totalSpecificObservers", totalSpecificObservers);
        
        // Method 2: Using Streams for aggregation
        Map<String, Integer> observersPerEventType = observerRegistry.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().size()
                ));
        stats.put("observersPerEventType", observersPerEventType);
        
        // Method 3: Using forEach with lambda
        Map<String, Object> eventTypeInfo = new HashMap<>();
        observerRegistry.forEach((eventType, observers) -> {
            Map<String, Object> info = new HashMap<>();
            info.put("observerCount", observers.size());
            info.put("activeObservers", observers.stream()
                    .filter(DataProcessingObserver::isActive)
                    .count());
            eventTypeInfo.put(eventType, info);
        });
        stats.put("eventTypeInfo", eventTypeInfo);
        
        return stats;
    }
    
    /**
     * Update event history using HashMap operations
     * 
     * @param eventType the event type
     * @param eventData the event data
     */
    private void updateEventHistory(String eventType, Map<String, Object> eventData) {
        // Production-grade: Event history management
        String historyKey = "event_" + eventType + "_" + System.currentTimeMillis();
        eventHistory.put(historyKey, eventData);
        
        // Clean up old events (keep last 1000)
        if (eventHistory.size() > 1000) {
            // Using Streams for cleanup
            List<String> keysToRemove = eventHistory.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .limit(eventHistory.size() - 1000)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            keysToRemove.forEach(eventHistory::remove);
        }
    }
    
    /**
     * Get event history using HashMap operations
     * 
     * @return event history
     */
    public Map<String, Object> getEventHistory() {
        return new HashMap<>(eventHistory);
    }
    
    /**
     * Clear all observers and history
     */
    public void clearAll() {
        observerRegistry.clear();
        globalObservers.clear();
        eventHistory.clear();
    }
}
