package com.netflix.reporting.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ReportingGrpcServer {

    private final ReportingApiImpl reportingApi;
    private final int port;
    private Server server;

    public ReportingGrpcServer(ReportingApiImpl reportingApi,
                               @Value("${grpc.port:9090}") int port) {
        this.reportingApi = reportingApi;
        this.port = port;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(reportingApi)
                .build()
                .start();
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}


