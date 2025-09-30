package com.netflix.productivity.smoke;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthSmokeTest {

    @LocalServerPort
    private int port;

    @Test
    void healthEndpointResponds() {
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> resp = rt.getForEntity("http://localhost:" + port + "/actuator/health", String.class);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody() != null && resp.getBody().contains("UP"));
    }
}
