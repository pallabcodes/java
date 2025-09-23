package com.example.outbound;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PaymentController {
    private final PaymentClient client;

    public PaymentController(PaymentClient client) {
        this.client = client;
    }

    @GetMapping("/payments/ping")
    public Mono<String> ping() {
        return client.ping();
    }
}


