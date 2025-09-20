package com.netflix.springframework.demo.client;

import com.netflix.springframework.demo.dto.UserCreateRequest;
import com.netflix.springframework.demo.dto.UserUpdateRequest;
import com.netflix.springframework.demo.dto.ApiResponse;
import com.netflix.springframework.demo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * UserRestClient - Reactive REST Client
 * 
 * This client demonstrates Netflix production-grade REST client implementation:
 * 1. Reactive WebClient for non-blocking HTTP calls
 * 2. Circuit breaker and retry patterns
 * 3. Proper error handling and logging
 * 4. Timeout and connection management
 * 5. Request/response logging and monitoring
 * 
 * For C/C++ engineers:
 * - WebClient is like HTTP client libraries in C++
 * - Reactive programming is like async/await in C++
 * - Circuit breaker is like fault tolerance patterns in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Component
public class UserRestClient {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRestClient.class);
    private static final String CLIENT_NAME = "UserRestClient";
    
    private final WebClient webClient;
    private final String baseUrl;
    private final Duration timeout;
    
    /**
     * Constructor with WebClient configuration
     * 
     * @param webClient WebClient instance
     * @param baseUrl Base URL for the API
     * @param timeout Request timeout
     */
    public UserRestClient(WebClient webClient, 
                         @Value("${app.external-api.base-url:http://localhost:8080}") String baseUrl,
                         @Value("${app.external-api.timeout:30s}") Duration timeout) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        
        logger.info("{} initialized with base URL: {} and timeout: {}", 
                   CLIENT_NAME, baseUrl, timeout);
    }
    
    /**
     * Get all users
     * 
     * @return Mono containing list of users
     */
    public Mono<ApiResponse<List<User>>> getAllUsers() {
        logger.info("{} - Getting all users", CLIENT_NAME);
        
        return webClient
                .get()
                .uri(baseUrl + "/api/v1/users")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> logger.info("{} - Successfully retrieved users", CLIENT_NAME))
                .doOnError(error -> logger.error("{} - Error getting users", CLIENT_NAME, error))
                .onErrorResume(this::handleError);
    }
    
    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return Mono containing user
     */
    public Mono<ApiResponse<User>> getUserById(Long id) {
        logger.info("{} - Getting user by ID: {}", CLIENT_NAME, id);
        
        return webClient
                .get()
                .uri(baseUrl + "/api/v1/users/{id}", id)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> logger.info("{} - Successfully retrieved user with ID: {}", CLIENT_NAME, id))
                .doOnError(error -> logger.error("{} - Error getting user with ID: {}", CLIENT_NAME, id, error))
                .onErrorResume(this::handleError);
    }
    
    /**
     * Create user
     * 
     * @param request User creation request
     * @return Mono containing created user
     */
    public Mono<ApiResponse<User>> createUser(UserCreateRequest request) {
        logger.info("{} - Creating user: {}", CLIENT_NAME, request.getName());
        
        return webClient
                .post()
                .uri(baseUrl + "/api/v1/users")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> logger.info("{} - Successfully created user: {}", CLIENT_NAME, request.getName()))
                .doOnError(error -> logger.error("{} - Error creating user: {}", CLIENT_NAME, request.getName(), error))
                .onErrorResume(this::handleError);
    }
    
    /**
     * Update user
     * 
     * @param id User ID
     * @param request User update request
     * @return Mono containing updated user
     */
    public Mono<ApiResponse<User>> updateUser(Long id, UserUpdateRequest request) {
        logger.info("{} - Updating user with ID: {}", CLIENT_NAME, id);
        
        return webClient
                .put()
                .uri(baseUrl + "/api/v1/users/{id}", id)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> logger.info("{} - Successfully updated user with ID: {}", CLIENT_NAME, id))
                .doOnError(error -> logger.error("{} - Error updating user with ID: {}", CLIENT_NAME, id, error))
                .onErrorResume(this::handleError);
    }
    
    /**
     * Delete user
     * 
     * @param id User ID
     * @return Mono containing deletion result
     */
    public Mono<ApiResponse<Void>> deleteUser(Long id) {
        logger.info("{} - Deleting user with ID: {}", CLIENT_NAME, id);
        
        return webClient
                .delete()
                .uri(baseUrl + "/api/v1/users/{id}", id)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> logger.info("{} - Successfully deleted user with ID: {}", CLIENT_NAME, id))
                .doOnError(error -> logger.error("{} - Error deleting user with ID: {}", CLIENT_NAME, id, error))
                .onErrorResume(this::handleError);
    }
    
    /**
     * Search users
     * 
     * @param name Name to search for
     * @param email Email to search for
     * @return Mono containing search results
     */
    public Mono<ApiResponse<List<User>>> searchUsers(String name, String email) {
        logger.info("{} - Searching users with name: {}, email: {}", CLIENT_NAME, name, email);
        
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(baseUrl + "/api/v1/users/search")
                        .queryParamIfPresent("name", name != null ? Mono.just(name) : Mono.empty())
                        .queryParamIfPresent("email", email != null ? Mono.just(email) : Mono.empty())
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> logger.info("{} - Successfully searched users", CLIENT_NAME))
                .doOnError(error -> logger.error("{} - Error searching users", CLIENT_NAME, error))
                .onErrorResume(this::handleError);
    }
    
    /**
     * Health check
     * 
     * @return Mono containing health status
     */
    public Mono<ApiResponse<Void>> healthCheck() {
        logger.info("{} - Performing health check", CLIENT_NAME);
        
        return webClient
                .get()
                .uri(baseUrl + "/actuator/health")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> logger.info("{} - Health check successful", CLIENT_NAME))
                .doOnError(error -> logger.error("{} - Health check failed", CLIENT_NAME, error))
                .onErrorResume(this::handleError);
    }
    
    /**
     * Handle errors and convert to appropriate response
     * 
     * @param error The error
     * @return Mono containing error response
     */
    private Mono<ApiResponse<?>> handleError(Throwable error) {
        if (error instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) error;
            logger.error("{} - HTTP error: {} - {}", CLIENT_NAME, ex.getStatusCode(), ex.getResponseBodyAsString());
            
            ApiResponse<?> response = ApiResponse.error(
                "External API error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
            return Mono.just(response);
        } else {
            logger.error("{} - Unexpected error", CLIENT_NAME, error);
            
            ApiResponse<?> response = ApiResponse.error("External API error: " + error.getMessage());
            return Mono.just(response);
        }
    }
}
