package com.yourorg.platform.clean.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourorg.platform.clean.domain.model.Sample;
import com.yourorg.platform.clean.domain.model.SampleId;
import com.yourorg.platform.clean.usecase.port.OutboxMessage;
import com.yourorg.platform.clean.usecase.port.OutboxPort;
import com.yourorg.platform.clean.usecase.port.SampleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SampleInteractorTest {
    @Test
    void createPersistsSample() {
        InMemoryRepo repo = new InMemoryRepo();
        SampleInteractor interactor = new SampleInteractor(repo, event -> {}, new NoopOutbox(), new ObjectMapper());

        SampleId id = interactor.create(new CreateSampleCommand("demo"));

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
