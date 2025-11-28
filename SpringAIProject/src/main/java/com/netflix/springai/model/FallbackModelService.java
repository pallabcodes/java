package com.netflix.springai.model;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FallbackModelService {

    private static final Logger logger = LoggerFactory.getLogger(FallbackModelService.class);

    private final ModelVersionManager modelVersionManager;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final CacheManager cacheManager;
    private final ModelPerformanceRepository modelPerformanceRepository;

    // Fallback chains for different model types
    private final Map<String, List<String>> fallbackChains = Map.of(
        "CHAT", List.of("gpt-4", "gpt-3.5-turbo", "claude-3", "claude-2"),
        "EMBEDDING", List.of("text-embedding-ada-002", "text-embedding-3-small"),
        "COMPLETION", List.of("gpt-4", "gpt-3.5-turbo", "claude-3"),
        "MODERATION", List.of("text-moderation-stable", "text-moderation-latest")
    );

    // Circuit breakers for each model
    private final Map<String, CircuitBreaker> modelCircuitBreakers = new HashMap<>();

    @Autowired
    public FallbackModelService(
            ModelVersionManager modelVersionManager,
            CircuitBreakerRegistry circuitBreakerRegistry,
            CacheManager cacheManager,
            ModelPerformanceRepository modelPerformanceRepository) {
        this.modelVersionManager = modelVersionManager;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.cacheManager = cacheManager;
        this.modelPerformanceRepository = modelPerformanceRepository;

        initializeCircuitBreakers();
    }

    public <T, R> CompletableFuture<ModelResponse<R>> executeWithFallback(
            String modelType,
            T input,
            Function<T, CompletableFuture<ModelResponse<R>>> primaryExecutor,
            Function<T, CompletableFuture<ModelResponse<R>>> fallbackExecutor) {

        return executeWithFallbackChain(modelType, input, List.of(primaryExecutor, fallbackExecutor));
    }

    public <T, R> CompletableFuture<ModelResponse<R>> executeWithFallbackChain(
            String modelType,
            T input,
            List<Function<T, CompletableFuture<ModelResponse<R>>>> executors) {

        if (executors.isEmpty()) {
            return CompletableFuture.completedFuture(
                ModelResponse.error("No executors provided for model type: " + modelType)
            );
        }

        CompletableFuture<ModelResponse<R>> result = new CompletableFuture<>();

        executeWithFallbackRecursive(modelType, input, executors, 0, result, null);

        return result;
    }

    private <T, R> void executeWithFallbackRecursive(
            String modelType,
            T input,
            List<Function<T, CompletableFuture<ModelResponse<R>>>> executors,
            int currentIndex,
            CompletableFuture<ModelResponse<R>> result,
            Exception lastException) {

        if (currentIndex >= executors.size()) {
            // All executors failed
            String errorMessage = "All fallback models failed for " + modelType;
            if (lastException != null) {
                errorMessage += ": " + lastException.getMessage();
            }

            result.complete(ModelResponse.error(errorMessage));
            return;
        }

        Function<T, CompletableFuture<ModelResponse<R>>> currentExecutor = executors.get(currentIndex);
        String modelName = getModelNameFromIndex(modelType, currentIndex);

        // Check circuit breaker
        CircuitBreaker circuitBreaker = modelCircuitBreakers.get(modelName);
        if (circuitBreaker != null && circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            logger.warn("Circuit breaker OPEN for model: {}, skipping to fallback", modelName);
            executeWithFallbackRecursive(modelType, input, executors, currentIndex + 1, result, lastException);
            return;
        }

        try {
            long startTime = System.nanoTime();

            currentExecutor.apply(input)
                .thenAccept(response -> {
                    long endTime = System.nanoTime();
                    long responseTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

                    if (response.isSuccess()) {
                        logger.info("Model execution successful: {} ({}ms)", modelName, responseTime);

                        // Record success metrics
                        recordModelSuccess(modelName, responseTime);

                        // Close circuit breaker if it was half-open
                        if (circuitBreaker != null) {
                            circuitBreaker.onSuccess(responseTime);
                        }

                        result.complete(response);
                    } else {
                        logger.warn("Model execution failed: {} ({}ms) - {}", modelName, responseTime, response.getErrorMessage());

                        // Record failure metrics
                        recordModelFailure(modelName, response.getErrorMessage());

                        // Open circuit breaker
                        if (circuitBreaker != null) {
                            circuitBreaker.onError(responseTime, response.getError());
                        }

                        // Try next fallback
                        executeWithFallbackRecursive(modelType, input, executors, currentIndex + 1, result,
                            new RuntimeException(response.getErrorMessage()));
                    }
                })
                .exceptionally(throwable -> {
                    long endTime = System.nanoTime();
                    long responseTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

                    logger.error("Model execution exception: {} ({}ms)", modelName, throwable.getMessage());

                    // Record failure metrics
                    recordModelFailure(modelName, throwable.getMessage());

                    // Open circuit breaker
                    if (circuitBreaker != null) {
                        circuitBreaker.onError(responseTime, throwable);
                    }

                    // Try next fallback
                    executeWithFallbackRecursive(modelType, input, executors, currentIndex + 1, result, throwable);

                    return null;
                });

        } catch (Exception e) {
            logger.error("Unexpected error executing model: {}", modelName, e);

            // Record failure and try fallback immediately
            recordModelFailure(modelName, e.getMessage());

            if (circuitBreaker != null) {
                circuitBreaker.onError(0, e);
            }

            executeWithFallbackRecursive(modelType, input, executors, currentIndex + 1, result, e);
        }
    }

    @Cacheable(value = "fallbackChains", key = "#modelType")
    public List<String> getFallbackChain(String modelType) {
        return fallbackChains.getOrDefault(modelType, List.of());
    }

    public AIModelVersion getBestAvailableModel(String modelType) {
        // Try to get active model first
        Optional<AIModelVersion> activeModel = modelVersionManager.getActiveModelVersion(modelType);
        if (activeModel.isPresent()) {
            // Check if model is healthy (circuit breaker not open)
            String modelName = activeModel.get().getVersion();
            CircuitBreaker circuitBreaker = modelCircuitBreakers.get(modelName);

            if (circuitBreaker == null || circuitBreaker.getState() != CircuitBreaker.State.OPEN) {
                return activeModel.get();
            }
        }

        // Fallback to chain
        List<String> chain = getFallbackChain(modelType);
        for (String modelVersion : chain) {
            try {
                Optional<AIModelVersion> model = modelVersionManager.getActiveModelVersion(modelType);
                if (model.isPresent()) {
                    CircuitBreaker circuitBreaker = modelCircuitBreakers.get(modelVersion);
                    if (circuitBreaker == null || circuitBreaker.getState() != CircuitBreaker.State.OPEN) {
                        return model.get();
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to check model: {}", modelVersion, e);
            }
        }

        throw new RuntimeException("No available models found for type: " + modelType);
    }

    public Map<String, CircuitBreaker.State> getCircuitBreakerStates() {
        return modelCircuitBreakers.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getState()
            ));
    }

    public Map<String, Object> getFallbackHealthStatus() {
        Map<String, Object> status = new HashMap<>();

        // Circuit breaker states
        status.put("circuitBreakers", getCircuitBreakerStates());

        // Model health status
        Map<String, Object> modelHealth = new HashMap<>();
        for (String modelType : fallbackChains.keySet()) {
            modelHealth.put(modelType, modelVersionManager.getModelHealthStatus(modelType));
        }
        status.put("modelHealth", modelHealth);

        // Fallback chain status
        Map<String, List<String>> chainStatus = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : fallbackChains.entrySet()) {
            List<String> availableModels = entry.getValue().stream()
                .filter(modelName -> {
                    CircuitBreaker cb = modelCircuitBreakers.get(modelName);
                    return cb == null || cb.getState() != CircuitBreaker.State.OPEN;
                })
                .collect(Collectors.toList());
            chainStatus.put(entry.getKey(), availableModels);
        }
        status.put("availableFallbacks", chainStatus);

        return status;
    }

    public void resetCircuitBreaker(String modelName) {
        CircuitBreaker circuitBreaker = modelCircuitBreakers.get(modelName);
        if (circuitBreaker != null) {
            circuitBreaker.reset();
            logger.info("Reset circuit breaker for model: {}", modelName);
        }
    }

    public void updateFallbackChain(String modelType, List<String> newChain) {
        // In a real implementation, this would be persisted to database/cache
        logger.info("Updated fallback chain for {}: {}", modelType, newChain);

        // Clear cache to force reload
        if (cacheManager.getCache("fallbackChains") != null) {
            cacheManager.getCache("fallbackChains").clear();
        }
    }

    private void initializeCircuitBreakers() {
        // Create circuit breakers for all models in fallback chains
        for (List<String> chain : fallbackChains.values()) {
            for (String modelName : chain) {
                if (!modelCircuitBreakers.containsKey(modelName)) {
                    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(modelName);
                    modelCircuitBreakers.put(modelName, circuitBreaker);
                }
            }
        }

        logger.info("Initialized circuit breakers for {} models", modelCircuitBreakers.size());
    }

    private void recordModelSuccess(String modelName, long responseTime) {
        // Record success metrics (could be sent to monitoring system)
        logger.debug("Model success: {} ({}ms)", modelName, responseTime);
    }

    private void recordModelFailure(String modelName, String errorMessage) {
        // Record failure metrics (could be sent to monitoring system)
        logger.warn("Model failure: {} - {}", modelName, errorMessage);
    }

    private String getModelNameFromIndex(String modelType, int index) {
        List<String> chain = getFallbackChain(modelType);
        return if (index < chain.size()) chain.get(index) else "unknown_model_" + index;
    }

    // Utility method to create a model executor function
    public static <T, R> Function<T, CompletableFuture<ModelResponse<R>>> createModelExecutor(
            String modelName,
            java.util.function.Function<T, R> modelFunction) {

        return input -> CompletableFuture.supplyAsync(() -> {
            try {
                R result = modelFunction.apply(input);
                return ModelResponse.success(result);
            } catch (Exception e) {
                return ModelResponse.error(e.getMessage());
            }
        });
    }
}

// Response wrapper class
class ModelResponse<T> {
    private final boolean success;
    private final T data;
    private final String errorMessage;
    private final Exception error;

    private ModelResponse(boolean success, T data, String errorMessage, Exception error) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.error = error;
    }

    public static <T> ModelResponse<T> success(T data) {
        return new ModelResponse<>(true, data, null, null);
    }

    public static <T> ModelResponse<T> error(String message) {
        return new ModelResponse<>(false, null, message, null);
    }

    public static <T> ModelResponse<T> error(Exception error) {
        return new ModelResponse<>(false, null, error.getMessage(), error);
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getErrorMessage() { return errorMessage; }
    public Exception getError() { return error; }
}
