package com.yourorg.platform.hexagonal.adapter.in.rest;

import com.yourorg.platform.hexagonal.application.port.in.CreateSampleCommand;
import com.yourorg.platform.hexagonal.application.port.in.CreateSampleUseCase;
import com.yourorg.platform.hexagonal.application.port.in.GetSampleUseCase;
import com.yourorg.platform.hexagonal.domain.model.Sample;
import com.yourorg.platform.hexagonal.domain.model.SampleId;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/samples")
public class SampleController {
    private final CreateSampleUseCase createSampleUseCase;
    private final GetSampleUseCase getSampleUseCase;

    public SampleController(CreateSampleUseCase createSampleUseCase, GetSampleUseCase getSampleUseCase) {
        this.createSampleUseCase = createSampleUseCase;
        this.getSampleUseCase = getSampleUseCase;
    }

    @PostMapping
    public ResponseEntity<SampleResponse> create(@Valid @RequestBody CreateSampleRequest request) {
        SampleId id = createSampleUseCase.create(new CreateSampleCommand(request.name()));
        Optional<Sample> sample = getSampleUseCase.getById(id);
        return sample.map(SampleController::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SampleResponse> get(@PathVariable UUID id) {
        return getSampleUseCase.getById(new SampleId(id))
                .map(SampleController::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static SampleResponse toResponse(Sample sample) {
        return new SampleResponse(
                sample.id().value().toString(),
                sample.name(),
                sample.status().name(),
                sample.createdAt().toString()
        );
    }
}
