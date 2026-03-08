package com.yourorg.platform.hexagonal.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourorg.platform.hexagonal.application.port.in.CreateSampleCommand;
import com.yourorg.platform.hexagonal.application.port.out.EventPublisher;
import com.yourorg.platform.hexagonal.application.port.out.OutboxMessage;
import com.yourorg.platform.hexagonal.application.port.out.OutboxPort;
import com.yourorg.platform.hexagonal.application.port.out.SampleRepository;
import com.yourorg.platform.hexagonal.domain.event.SampleCreatedEvent;
import com.yourorg.platform.hexagonal.domain.model.Sample;
import com.yourorg.platform.hexagonal.domain.model.SampleId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SampleServiceTest {
    @Test
    void createPersistsSample() {
        InMemoryRepo repo = new InMemoryRepo();
        SampleService service = new SampleService(repo, event -> {}, new NoopOutbox(), new ObjectMapper());

        SampleId id = service.create(new CreateSampleCommand("demo"));

        Optional<Sample> loaded = repo.findById(id);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().name()).isEqualTo("demo");
    }

    private static class InMemoryRepo implements SampleRepository {
        private Sample sample;

        @Override
        public Sample save(Sample sample) {
            this.sample = sample;
            return sample;
        }

        @Override
        public Optional<Sample> findById(SampleId id) {
            return Optional.ofNullable(sample);
        }
    }

    private static class NoopOutbox implements OutboxPort {
        @Override
        public void save(OutboxMessage message) {
        }

        @Override
        public List<OutboxMessage> findUnprocessed(int limit) {
            return List.of();
        }

        @Override
        public void markProcessed(List<OutboxMessage> messages) {
        }
    }
}
