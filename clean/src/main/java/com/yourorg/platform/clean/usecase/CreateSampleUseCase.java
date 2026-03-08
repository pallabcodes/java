package com.yourorg.platform.clean.usecase;

import com.yourorg.platform.clean.domain.model.SampleId;

public interface CreateSampleUseCase {
    SampleId create(CreateSampleCommand command);
}
