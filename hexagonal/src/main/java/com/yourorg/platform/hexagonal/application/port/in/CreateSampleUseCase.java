package com.yourorg.platform.hexagonal.application.port.in;

import com.yourorg.platform.hexagonal.domain.model.SampleId;

public interface CreateSampleUseCase {
    SampleId create(CreateSampleCommand command);
}
