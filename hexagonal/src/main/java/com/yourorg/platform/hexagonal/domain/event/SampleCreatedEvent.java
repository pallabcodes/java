package com.yourorg.platform.hexagonal.domain.event;

import com.yourorg.platform.hexagonal.domain.model.SampleId;
import java.time.Instant;

public record SampleCreatedEvent(SampleId id, String name, Instant occurredAt) {
    public static SampleCreatedEvent of(SampleId id, String name) {
        return new SampleCreatedEvent(id, name, Instant.now());
    }
}
