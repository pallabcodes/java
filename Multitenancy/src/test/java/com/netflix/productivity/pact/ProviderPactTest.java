package com.netflix.productivity.pact;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;

@Provider("productivity-api")
@PactFolder("pacts")
public class ProviderPactTest {

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", 8080, "/"));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactTests(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("issue exists")
    public void issueExists() {
        // TODO seed a known issue or stub repository for provider state
    }
}