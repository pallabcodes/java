package com.yourorg.platform.hexagonal.domain.model;

import java.util.UUID;

public record SampleId(UUID value) {
    public static SampleId newId() {
        return new SampleId(UUID.randomUUID());
    }
}
