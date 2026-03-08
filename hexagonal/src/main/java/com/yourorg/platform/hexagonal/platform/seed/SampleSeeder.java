package com.yourorg.platform.hexagonal.platform.seed;

import com.yourorg.platform.hexagonal.application.port.out.SampleRepository;
import com.yourorg.platform.hexagonal.domain.model.Sample;
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
