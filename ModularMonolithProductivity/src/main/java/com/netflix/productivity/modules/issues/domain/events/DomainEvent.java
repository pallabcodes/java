package com.netflix.productivity.modules.issues.domain.events;

import java.time.LocalDateTime;

/**
 * Domain Event Interface - Domain-Driven Design
 *
 * Represents something that happened in the domain that is of interest to other parts of the system.
 * Domain events are immutable and represent past occurrences.
 *
 * Characteristics of Domain Events:
 * - Immutable (no setters)
 * - Represent past occurrences
 * - Named in past tense
 * - Contain all data needed to understand the event
 * - May trigger side effects in other aggregates or bounded contexts
 */
public interface DomainEvent {

    /**
     * Get the timestamp when this event occurred
     */
    LocalDateTime getOccurredOn();

    /**
     * Get a human-readable description of the event
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * Get the aggregate ID this event relates to
     */
    default String getAggregateId() {
        return null; // Override in implementations if needed
    }

    /**
     * Get the event type name
     */
    default String getEventType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Check if this event should trigger cross-aggregate communication
     */
    default boolean shouldTriggerExternalSystems() {
        return false; // Override in implementations if needed
    }

    /**
     * Get event metadata for auditing/logging
     */
    default java.util.Map<String, Object> getMetadata() {
        return java.util.Map.of(
            "eventType", getEventType(),
            "occurredOn", getOccurredOn(),
            "description", getDescription()
        );
    }
}
