package com.netflix.productivity.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpaPolicyClient {

    @Value("${app.opa.enabled:false}")
    private boolean enabled;

    @Value("${app.opa.base-url:http://localhost:8181}")
    private String baseUrl;

    @Value("${app.opa.policy-path:/v1/data/productivity/allow}")
    private String policyPath;

    private final WebClient.Builder webClientBuilder;

    public boolean allow(Map<String, Object> input) {
        if (!enabled) {
            return true;
        }
        try {
            Map<String, Object> body = Map.of("input", input);
            Map response = webClientBuilder.build()
                    .post()
                    .uri(baseUrl + policyPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            Object result = ((Map) response.get("result")).get("allow");
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("OPA decision error, default deny", e);
            return false;
        }
    }
}

