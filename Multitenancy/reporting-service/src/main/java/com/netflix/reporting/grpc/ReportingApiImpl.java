package com.netflix.reporting.grpc;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class ReportingApiImpl extends ReportingApiGrpc.ReportingApiImplBase {

    @Override
    public void health(HealthRequest request, StreamObserver<HealthResponse> responseObserver) {
        HealthResponse resp = HealthResponse.newBuilder().setStatus("ok").build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}


