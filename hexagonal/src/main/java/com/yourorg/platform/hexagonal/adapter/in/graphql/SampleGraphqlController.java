package com.yourorg.platform.hexagonal.adapter.in.graphql;

import com.yourorg.platform.hexagonal.application.port.in.CreateSampleCommand;
import com.yourorg.platform.hexagonal.application.port.in.CreateSampleUseCase;
import com.yourorg.platform.hexagonal.application.port.in.GetSampleUseCase;
import com.yourorg.platform.hexagonal.domain.model.Sample;
import com.yourorg.platform.hexagonal.domain.model.SampleId;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SampleGraphqlController {
    private final CreateSampleUseCase createSampleUseCase;
    private final GetSampleUseCase getSampleUseCase;

    public SampleGraphqlController(CreateSampleUseCase createSampleUseCase, GetSampleUseCase getSampleUseCase) {
        this.createSampleUseCase = createSampleUseCase;
        this.getSampleUseCase = getSampleUseCase;
    }

    @QueryMapping
    public SampleGraphqlResponse sample(@Argument String id) {
        return getSampleUseCase.getById(new SampleId(UUID.fromString(id)))
                .map(SampleGraphqlController::toResponse)
                .orElse(null);
    }

    @MutationMapping
    public SampleGraphqlResponse createSample(@Argument CreateSampleInput input) {
        SampleId id = createSampleUseCase.create(new CreateSampleCommand(input.name()));
        return getSampleUseCase.getById(id)
                .map(SampleGraphqlController::toResponse)
                .orElse(null);
    }

    private static SampleGraphqlResponse toResponse(Sample sample) {
        return new SampleGraphqlResponse(
                sample.id().value().toString(),
                sample.name(),
                sample.status().name(),
                sample.createdAt().toString()
        );
    }
}
