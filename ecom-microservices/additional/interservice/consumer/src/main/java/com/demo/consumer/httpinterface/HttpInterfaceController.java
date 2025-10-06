package com.demo.consumer.httpinterface;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/http-interface")
@RequiredArgsConstructor
public class HttpInterfaceController {

    private final ProviderHttpInterface client;

    @GetMapping("/instance")
    public String getInstanceDetails() {
        return client.getInstanceInfo();
    }
}
