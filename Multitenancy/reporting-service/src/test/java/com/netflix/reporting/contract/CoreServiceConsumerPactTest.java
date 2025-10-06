package com.netflix.reporting.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import com.netflix.reporting.client.CoreServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "core-service")
public class CoreServiceConsumerPactTest {

    @au.com.dius.pact.consumer.junit5.Pact(consumer = "reporting-service")
    public RequestResponsePact usersContract(PactDslWithProvider builder) {
        return builder
                .given("users exist for tenant")
                .uponReceiving("get users")
                .path("/api/internal/users")
                .method("GET")
                .matchHeader("X-Tenant-ID", ".+", "tenant-1")
                .willRespondWith()
                .status(200)
                .headers(java.util.Map.of("Content-Type", "application/json"))
                .body("[]")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "usersContract")
    void verifyUsersContract(MockServer mockServer) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.refresh();
        CoreServiceClient client = new FeignClientBuilder(ctx)
                .forType(CoreServiceClient.class, "core-service")
                .url(mockServer.getUrl())
                .build();

        List<?> users = client.getUsers("tenant-1");
        assertThat(users).isNotNull();
    }
}


