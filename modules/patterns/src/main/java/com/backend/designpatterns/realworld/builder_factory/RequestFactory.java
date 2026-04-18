package com.backend.designpatterns.realworld.builder_factory;

// Factory: Encapsulates the complexity of building common request types
public class RequestFactory {

    public static NetworkRequest createGetRequest(String url) {
        return new NetworkRequestBuilder()
                .url(url)
                .method(RequestMethod.GET)
                .header("Content-Type", "application/json")
                .build();
    }

    public static NetworkRequest createPostRequest(String url, String jsonBody) {
        return new NetworkRequestBuilder()
                .url(url)
                .method(RequestMethod.POST)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer token_123") // Default auth
                .body(jsonBody)
                .build();
    }
}
