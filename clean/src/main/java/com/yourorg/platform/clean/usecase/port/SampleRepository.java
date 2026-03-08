package com.yourorg.platform.clean.usecase.port;

import com.yourorg.platform.clean.domain.model.Sample;
import com.yourorg.platform.clean.domain.model.SampleId;
import java.util.Optional;

public interface SampleRepository {
    Sample save(Sample sample);
    Optional<Sample> findById(SampleId id);
}
