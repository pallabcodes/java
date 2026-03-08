package com.yourorg.platform.hexagonal.application.port.in;

import com.yourorg.platform.hexagonal.domain.model.Sample;
import com.yourorg.platform.hexagonal.domain.model.SampleId;
import java.util.Optional;

public interface GetSampleUseCase {
    Optional<Sample> getById(SampleId id);
}
