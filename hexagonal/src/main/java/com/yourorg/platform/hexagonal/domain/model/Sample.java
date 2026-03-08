package com.yourorg.platform.hexagonal.domain.model;

import java.time.Instant;

public record Sample(SampleId id, String name, SampleStatus status, Instant createdAt) {
    public static Sample create(String name) {
        return new Sample(SampleId.newId(), name, SampleStatus.CREATED, Instant.now());
    }
}
