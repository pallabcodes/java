package com.backend.functional;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Step 06: Currying & Partial Application
 * 
 * L5 Principles:
 * 1. Currying: Converting a function that takes multiple arguments into a chain of functions.
 * 2. Partial Application: Fixing some arguments of a function to create a more specialized one.
 * 3. Configuration Injection: Injecting context/config early and passing around data-only functions.
 */
public class Step06_ConfigInjection {

    public record ProjectConfig(String projectId, String region) {}

    public static void main(String[] args) {
        System.out.println("=== Step 06: Currying & Partial Application ===");

        // A generic cloud storage function taking config and resource id
        BiFunction<ProjectConfig, String, String> genericCloudCall = (config, resourceId) -> 
            String.format("Fetching %s from %s in region %s", resourceId, config.projectId(), config.region());

        // 1. Partial Application manually
        ProjectConfig myProject = new ProjectConfig("google-internal-777", "us-central1");
        Function<String, String> specializedFetch = resourceId -> genericCloudCall.apply(myProject, resourceId);

        System.out.println("Result (Partial): " + specializedFetch.apply("gmeet-recording-001"));

        // 2. Proper Currying
        Function<ProjectConfig, Function<String, String>> curriedCloudCall = 
            config -> resourceId -> String.format("CLOUD_REST_API: %s/%s/%s", config.region(), config.projectId(), resourceId);

        // Pre-configure the function
        Function<String, String> regionXFetcher = curriedCloudCall.apply(myProject);

        // Now used later in the code by someone who doesn't know about the config
        System.out.println("Result (Curried): " + regionXFetcher.apply("user-profile-data"));
    }
}
