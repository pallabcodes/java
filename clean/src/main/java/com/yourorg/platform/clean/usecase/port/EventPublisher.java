package com.yourorg.platform.clean.usecase.port;

import com.yourorg.platform.clean.domain.event.SampleCreatedEvent;

public interface EventPublisher {
    void publish(SampleCreatedEvent event);
}
