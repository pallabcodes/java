package com.netflix.streaming.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Idempotency filter for handling idempotency keys in API requests.
 * 
 * Ensures exactly-once semantics for mutating operations (POST, PUT, PATCH).
 * Uses Idempotency-Key header to identify duplicate requests.
 */
@Component
@Order(2) // After rate limiting, before authentication
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String IDEMPOTENCY_REPLAY_HEADER = "Idempotency-Replay";

    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public IdempotencyFilter(IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Only apply to mutating operations
        if (!isMutatingOperation(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        
        // If no idempotency key, proceed normally
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate idempotency key format (UUID or custom format)
        if (!isValidIdempotencyKey(idempotencyKey)) {
            sendErrorResponse(response, HttpStatus.BAD_REQUEST, 
                "Invalid idempotency key format. Must be a valid UUID or alphanumeric string.");
            return;
        }

        // Check if this request was already processed
        if (idempotencyService.isProcessed(idempotencyKey)) {
            // Return stored response if available
            String storedResponse = idempotencyService.getStoredResponse(idempotencyKey);
            if (storedResponse != null) {
                logger.debug("Returning stored response for idempotency key: {}", idempotencyKey);
                response.setStatus(HttpStatus.OK.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader(IDEMPOTENCY_REPLAY_HEADER, "true");
                response.getWriter().write(storedResponse);
                return;
            }

            // Key exists but no stored response - conflict
            logger.warn("Idempotency key already processed but no stored response: {}", idempotencyKey);
            sendErrorResponse(response, HttpStatus.CONFLICT,
                "Request with this idempotency key was already processed.");
            return;
        }

        // Claim the idempotency key
        boolean claimed = idempotencyService.claim(idempotencyKey, Duration.ofHours(24));
        if (!claimed) {
            // Race condition: another thread claimed it
            sendErrorResponse(response, HttpStatus.CONFLICT,
                "Request with this idempotency key is being processed.");
            return;
        }

        // Wrap request and response to capture response body
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // Process the request
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            // Store successful responses (2xx status codes)
            if (wrappedResponse.getStatus() >= 200 && wrappedResponse.getStatus() < 300) {
                byte[] responseBody = wrappedResponse.getContentAsByteArray();
                if (responseBody.length > 0) {
                    String responseBodyString = new String(responseBody, StandardCharsets.UTF_8);
                    idempotencyService.storeResponse(idempotencyKey, responseBodyString, Duration.ofHours(24));
                }
            } else {
                // For non-success responses, release the key to allow retry
                idempotencyService.release(idempotencyKey);
            }

            // Copy response to actual response
            wrappedResponse.copyBodyToResponse();

        } catch (Exception e) {
            // On error, release the key to allow retry
            idempotencyService.release(idempotencyKey);
            throw e;
        }
    }

    /**
     * Check if the request is a mutating operation.
     */
    private boolean isMutatingOperation(HttpServletRequest request) {
        String method = request.getMethod();
        return HttpMethod.POST.matches(method) ||
               HttpMethod.PUT.matches(method) ||
               HttpMethod.PATCH.matches(method) ||
               HttpMethod.DELETE.matches(method);
    }

    /**
     * Validate idempotency key format.
     */
    private boolean isValidIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        
        // Allow UUID format or alphanumeric with dashes/underscores
        // Length between 1 and 255 characters
        return key.length() >= 1 && 
               key.length() <= 255 && 
               key.matches("^[a-zA-Z0-9\\-_]+$");
    }

    /**
     * Send error response.
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) 
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        String errorJson = String.format(
            "{\"error\":\"%s\",\"message\":\"%s\",\"status\":%d}",
            status.getReasonPhrase(), message, status.value()
        );
        
        response.getWriter().write(errorJson);
    }
}

