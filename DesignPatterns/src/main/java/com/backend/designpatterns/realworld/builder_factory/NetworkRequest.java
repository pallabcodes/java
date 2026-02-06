package com.backend.designpatterns.realworld.builder_factory;

import java.util.Map;

// Product
public class NetworkRequest {
    private final String url;
    private final RequestMethod method;
    private final Map<String, String> headers;
    private final String body;

    // Constructor is package-private to enforce usage of Builder
    NetworkRequest(String url, RequestMethod method, Map<String, String> headers, String body) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public String toString() {
        return "NetworkRequest{" +
                "method=" + method +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
