package com.backend.designpatterns.realworld.builder_factory;

import java.util.HashMap;
import java.util.Map;

// Builder
public class NetworkRequestBuilder {
    private String url;
    private RequestMethod method;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public NetworkRequestBuilder url(String url) {
        this.url = url;
        return this;
    }

    public NetworkRequestBuilder method(RequestMethod method) {
        this.method = method;
        return this;
    }

    public NetworkRequestBuilder header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public NetworkRequestBuilder body(String body) {
        this.body = body;
        return this;
    }

    public NetworkRequest build() {
        if (url == null || method == null) {
            throw new IllegalStateException("URL and Method are required");
        }
        return new NetworkRequest(url, method, headers, body);
    }
}
