package com.yourorg.platform.clean.framework.seed;

import com.yourorg.platform.clean.domain.model.Sample;
import com.yourorg.platform.clean.usecase.port.SampleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("seed")
public class SampleSeeder implements CommandLineRunner {
    private final SampleRepository repository;

    public SampleSeeder(SampleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        repository.save(Sample.create("seed-sample"));
    }
}
