package com.yourorg.platform.clean.domain.model;

import java.util.UUID;

public record SampleId(UUID value) {
    public static SampleId newId() {
        return new SampleId(UUID.randomUUID());
    }
}
