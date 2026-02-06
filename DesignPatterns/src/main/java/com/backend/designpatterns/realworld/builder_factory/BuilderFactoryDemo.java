package com.backend.designpatterns.realworld.builder_factory;

public class BuilderFactoryDemo {

    public static void main(String[] args) {
        
        // Use Factory for standard requests
        System.out.println("--- Factory Created Requests ---");
        
        NetworkRequest getReq = RequestFactory.createGetRequest("https://api.example.com/users");
        System.out.println("GET: " + getReq);

        NetworkRequest postReq = RequestFactory.createPostRequest("https://api.example.com/users", "{ \"name\": \"John\" }");
        System.out.println("POST: " + postReq);


        // Use Builder directly for custom scenarios
        System.out.println("\n--- Custom Builder Request ---");
        
        NetworkRequest customReq = new NetworkRequestBuilder()
                .url("https://api.example.com/upload")
                .method(RequestMethod.PUT)
                .header("Content-Type", "application/octet-stream")
                .body("Binary Data")
                .build();
        
        System.out.println("CUSTOM: " + customReq);
    }
}
