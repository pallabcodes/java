package com.yourorg.platform.clean.interfaceadapter.in.grpc;

import com.yourorg.platform.clean.domain.model.Sample;
import com.yourorg.platform.clean.domain.model.SampleId;
import com.yourorg.platform.clean.usecase.CreateSampleCommand;
import com.yourorg.platform.clean.usecase.CreateSampleUseCase;
import com.yourorg.platform.clean.usecase.GetSampleUseCase;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class SampleGrpcService extends SampleServiceGrpc.SampleServiceImplBase {
    private final CreateSampleUseCase createSampleUseCase;
    private final GetSampleUseCase getSampleUseCase;

    public SampleGrpcService(CreateSampleUseCase createSampleUseCase, GetSampleUseCase getSampleUseCase) {
        this.createSampleUseCase = createSampleUseCase;
        this.getSampleUseCase = getSampleUseCase;
    }

    @Override
    public void createSample(CreateSampleRequest request, StreamObserver<SampleResponse> responseObserver) {
        SampleId id = createSampleUseCase.create(new CreateSampleCommand(request.getName()));
        Optional<Sample> sample = getSampleUseCase.getById(id);
        responseObserver.onNext(sample.map(SampleGrpcService::toResponse).orElse(SampleResponse.getDefaultInstance()));
        responseObserver.onCompleted();
    }

    @Override
    public void getSample(GetSampleRequest request, StreamObserver<SampleResponse> responseObserver) {
        Optional<Sample> sample = getSampleUseCase.getById(new SampleId(java.util.UUID.fromString(request.getId())));
        responseObserver.onNext(sample.map(SampleGrpcService::toResponse).orElse(SampleResponse.getDefaultInstance()));
        responseObserver.onCompleted();
    }

    private static SampleResponse toResponse(Sample sample) {
        return SampleResponse.newBuilder()
                .setId(sample.id().value().toString())
                .setName(sample.name())
                .setStatus(sample.status().name())
                .setCreatedAt(sample.createdAt().toString())
                .build();
    }
}
