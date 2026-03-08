package com.yourorg.platform.clean.usecase;

import com.yourorg.platform.clean.domain.model.Sample;
import com.yourorg.platform.clean.domain.model.SampleId;
import java.util.Optional;

public interface GetSampleUseCase {
    Optional<Sample> getById(SampleId id);
}
