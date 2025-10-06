package com.demo.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProxyController {

    private final Service2Client client;

    public ProxyController(Service2Client client) {
        this.client = client;
    }

    @GetMapping("/proxy")
    public String proxy(){
        return client.fetchData();
    }
}
