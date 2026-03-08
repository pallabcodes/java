package com.yourorg.platform.clean.usecase.port;

import java.time.Instant;
import java.util.UUID;

public record OutboxMessage(UUID id, UUID aggregateId, String aggregateType, String eventType, String payload, Instant occurredAt) {}
