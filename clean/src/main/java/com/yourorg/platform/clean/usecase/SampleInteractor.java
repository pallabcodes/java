package com.yourorg.platform.clean.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourorg.platform.clean.domain.event.SampleCreatedEvent;
import com.yourorg.platform.clean.domain.model.Sample;
import com.yourorg.platform.clean.domain.model.SampleId;
import com.yourorg.platform.clean.usecase.port.EventPublisher;
import com.yourorg.platform.clean.usecase.port.OutboxMessage;
import com.yourorg.platform.clean.usecase.port.OutboxPort;
import com.yourorg.platform.clean.usecase.port.SampleRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SampleInteractor implements CreateSampleUseCase, GetSampleUseCase {
    private final SampleRepository sampleRepository;
    private final EventPublisher eventPublisher;
    private final OutboxPort outboxPort;
    private final ObjectMapper objectMapper;

    public SampleInteractor(
            SampleRepository sampleRepository,
            EventPublisher eventPublisher,
            OutboxPort outboxPort,
            ObjectMapper objectMapper
    ) {
        this.sampleRepository = sampleRepository;
        this.eventPublisher = eventPublisher;
        this.outboxPort = outboxPort;
        this.objectMapper = objectMapper;
    }

    @Override
    public SampleId create(CreateSampleCommand command) {
        Sample sample = Sample.create(command.name());
        sampleRepository.save(sample);

        SampleCreatedEvent event = SampleCreatedEvent.of(sample.id(), sample.name());
        eventPublisher.publish(event);

        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxPort.save(new OutboxMessage(
                    UUID.randomUUID(),
                    sample.id().value(),
                    "Sample",
                    "SampleCreated",
                    payload,
                    event.occurredAt()
            ));
        } catch (Exception ignored) {
            // Swallow to keep core flow simple; production should handle retries and alerting.
        }

        return sample.id();
    }

    @Override
    public Optional<Sample> getById(SampleId id) {
        return sampleRepository.findById(id);
    }
}
