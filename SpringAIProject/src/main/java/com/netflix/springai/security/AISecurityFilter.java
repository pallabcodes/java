package com.netflix.springai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AISecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AISecurityFilter.class);

    private final PromptInjectionDetector promptInjectionDetector;
    private final ModelPoisoningDetector modelPoisoningDetector;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;

    // Security metrics
    private final Counter securityViolations;
    private final Counter blockedRequests;
    private final Counter suspiciousRequests;
    private final Counter safeRequests;

    // User tracking for behavioral analysis
    private final Map<String, UserSecurityProfile> userProfiles = new ConcurrentHashMap<>();

    @Autowired
    public AISecurityFilter(
            PromptInjectionDetector promptInjectionDetector,
            ModelPoisoningDetector modelPoisoningDetector,
            MeterRegistry meterRegistry,
            ObjectMapper objectMapper) {
        this.promptInjectionDetector = promptInjectionDetector;
        this.modelPoisoningDetector = modelPoisoningDetector;
        this.meterRegistry = meterRegistry;
        this.objectMapper = objectMapper;

        // Initialize security metrics
        this.securityViolations = Counter.builder("ai.security.violations")
                .description("Total number of AI security violations detected")
                .register(meterRegistry);

        this.blockedRequests = Counter.builder("ai.security.requests.blocked")
                .description("Total number of requests blocked due to security violations")
                .register(meterRegistry);

        this.suspiciousRequests = Counter.builder("ai.security.requests.suspicious")
                .description("Total number of suspicious requests flagged for review")
                .register(meterRegistry);

        this.safeRequests = Counter.builder("ai.security.requests.safe")
                .description("Total number of requests that passed security checks")
                .register(meterRegistry);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.nanoTime();
        String requestId = generateRequestId();
        String userId = extractUserId(request);
        String clientIp = getClientIpAddress(request);

        try {
            // Extract request data for analysis
            AIRequestData requestData = extractRequestData(request);

            if (requestData == null) {
                // Not an AI request, continue normally
                filterChain.doFilter(request, response);
                return;
            }

            // Build security context
            Map<String, Object> securityContext = buildSecurityContext(request, userId, clientIp);

            // Perform security analysis
            SecurityAnalysisResult analysisResult = performSecurityAnalysis(requestData, securityContext, userId);

            // Handle security violations
            SecurityResponseAction action = determineResponseAction(analysisResult);

            // Update user profile
            updateUserProfile(userId, analysisResult, action);

            // Execute response action
            if (action == SecurityResponseAction.BLOCK) {
                handleBlockedRequest(response, analysisResult, requestId);
                blockedRequests.increment();
                return;
            } else if (action == SecurityResponseAction.FLAG) {
                // Add security headers and continue
                addSecurityHeaders(response, analysisResult);
                suspiciousRequests.increment();
            } else {
                safeRequests.increment();
            }

            // Add security metadata to request for downstream processing
            request.setAttribute("AI_SECURITY_ANALYSIS", analysisResult);
            request.setAttribute("AI_SECURITY_ACTION", action);

            // Continue with request processing
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Error in AI security filter for request: {}", requestId, e);
            securityViolations.increment();

            // On error, block the request for security
            handleErrorResponse(response, "Security analysis failed", requestId);
            return;
        }

        long processingTime = System.nanoTime() - startTime;
        logger.debug("AI security analysis completed in {}ns for request: {}", processingTime, requestId);
    }

    private AIRequestData extractRequestData(HttpServletRequest request) {
        try {
            // Check if this is an AI-related endpoint
            String requestURI = request.getRequestURI();
            if (!isAIEndpoint(requestURI)) {
                return null;
            }

            String modelType = extractModelType(requestURI);
            String input = extractRequestBody(request);
            Map<String, Object> metadata = extractMetadata(request);

            return new AIRequestData(modelType, input, metadata);

        } catch (Exception e) {
            logger.warn("Failed to extract AI request data", e);
            return null;
        }
    }

    private SecurityAnalysisResult performSecurityAnalysis(AIRequestData requestData,
                                                         Map<String, Object> securityContext,
                                                         String userId) {

        // Prompt injection analysis
        PromptInjectionDetector.DetectionResult injectionResult =
            promptInjectionDetector.analyzePrompt(requestData.getInput(), userId, securityContext);

        // Model poisoning analysis (if we have output, but for requests we'll analyze input patterns)
        ModelPoisoningDetector.PoisoningAnalysis poisoningResult =
            modelPoisoningDetector.analyzeRequest(
                requestData.getModelType(),
                requestData.getInput(),
                null, // No output yet for incoming requests
                requestData.getMetadata(),
                userId
            );

        // Combine results
        double overallRiskScore = Math.max(injectionResult.getRiskScore(),
                                         poisoningResult.getRiskScore() * 0.8); // Weight poisoning slightly less

        SecurityThreatLevel overallThreatLevel = determineOverallThreatLevel(injectionResult, poisoningResult);

        Map<String, Object> analysisDetails = new HashMap<>();
        analysisDetails.put("promptInjection", Map.of(
            "threatLevel", injectionResult.getThreatLevel().name(),
            "riskScore", injectionResult.getRiskScore(),
            "patternsDetected", injectionResult.getDetectedPatterns().size()
        ));
        analysisDetails.put("modelPoisoning", Map.of(
            "threatLevel", poisoningResult.getThreatLevel().name(),
            "riskScore", poisoningResult.getRiskScore(),
            "anomaliesDetected", poisoningResult.getAnomalies().getAnomalies().size()
        ));

        return new SecurityAnalysisResult(
            overallThreatLevel,
            overallRiskScore,
            analysisDetails,
            LocalDateTime.now()
        );
    }

    private SecurityResponseAction determineResponseAction(SecurityAnalysisResult analysis) {
        return switch (analysis.getThreatLevel()) {
            case CRITICAL -> SecurityResponseAction.BLOCK;
            case HIGH -> SecurityResponseAction.BLOCK;
            case MEDIUM -> SecurityResponseAction.FLAG;
            case LOW -> SecurityResponseAction.FLAG;
            case SAFE -> SecurityResponseAction.ALLOW;
        };
    }

    private void handleBlockedRequest(HttpServletResponse response,
                                    SecurityAnalysisResult analysis,
                                    String requestId) throws IOException {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "SECURITY_VIOLATION");
        errorResponse.put("message", "Request blocked due to security policy violation");
        errorResponse.put("requestId", requestId);
        errorResponse.put("threatLevel", analysis.getThreatLevel().name());
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        // Don't expose detailed analysis in production
        if (logger.isDebugEnabled()) {
            errorResponse.put("analysis", analysis.getDetails());
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

        logger.warn("Request blocked due to security violation: requestId={}, threatLevel={}",
                   requestId, analysis.getThreatLevel());
    }

    private void handleErrorResponse(HttpServletResponse response, String message, String requestId) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "SECURITY_ERROR");
        errorResponse.put("message", message);
        errorResponse.put("requestId", requestId);
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private void addSecurityHeaders(HttpServletResponse response, SecurityAnalysisResult analysis) {
        response.setHeader("X-AI-Security-Threat-Level", analysis.getThreatLevel().name());
        response.setHeader("X-AI-Security-Risk-Score", String.valueOf(analysis.getRiskScore()));
        response.setHeader("X-AI-Security-Flagged", "true");

        // Additional security headers
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
    }

    private void updateUserProfile(String userId, SecurityAnalysisResult analysis, SecurityResponseAction action) {
        UserSecurityProfile profile = userProfiles.computeIfAbsent(userId, k -> new UserSecurityProfile(userId));

        profile.recordSecurityEvent(analysis, action);

        // Check for patterns that might indicate an attack campaign
        if (profile.hasSuspiciousPattern()) {
            logger.warn("Suspicious pattern detected for user: {}", userId);
            // Could trigger additional security measures
        }
    }

    private boolean isAIEndpoint(String requestURI) {
        return requestURI.contains("/api/chat") ||
               requestURI.contains("/api/completion") ||
               requestURI.contains("/api/embedding") ||
               requestURI.contains("/api/moderation");
    }

    private String extractModelType(String requestURI) {
        if (requestURI.contains("/chat")) return "CHAT";
        if (requestURI.contains("/completion")) return "COMPLETION";
        if (requestURI.contains("/embedding")) return "EMBEDDING";
        if (requestURI.contains("/moderation")) return "MODERATION";
        return "UNKNOWN";
    }

    private String extractRequestBody(HttpServletRequest request) {
        try {
            return request.getReader().lines().reduce("", (a, b) -> a + b);
        } catch (Exception e) {
            logger.warn("Failed to extract request body", e);
            return "";
        }
    }

    private Map<String, Object> extractMetadata(HttpServletRequest request) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_agent", request.getHeader("User-Agent"));
        metadata.put("content_length", request.getContentLength());
        metadata.put("content_type", request.getContentType());
        metadata.put("request_method", request.getMethod());
        metadata.put("request_uri", request.getRequestURI());

        // Add timing information
        metadata.put("request_start_time", System.currentTimeMillis());

        return metadata;
    }

    private Map<String, Object> buildSecurityContext(HttpServletRequest request, String userId, String clientIp) {
        Map<String, Object> context = new HashMap<>();

        // User history
        UserSecurityProfile profile = userProfiles.get(userId);
        if (profile != null) {
            context.put("previous_attempts", profile.getRecentViolationCount());
            context.put("avg_risk_score", profile.getAverageRiskScore());
        }

        // Request context
        context.put("client_ip", clientIp);
        context.put("user_agent", request.getHeader("User-Agent"));
        context.put("time_since_last_request", calculateTimeSinceLastRequest(userId));

        // System context
        context.put("current_hour", LocalDateTime.now().getHour());
        context.put("is_business_hours", isBusinessHours());

        return context;
    }

    private String extractUserId(HttpServletRequest request) {
        // Extract user ID from JWT token or session
        // This would depend on your authentication mechanism
        return request.getHeader("X-User-ID") != null ?
               request.getHeader("X-User-ID") : "anonymous";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private SecurityThreatLevel determineOverallThreatLevel(
            PromptInjectionDetector.DetectionResult injectionResult,
            ModelPoisoningDetector.PoisoningAnalysis poisoningResult) {

        // Use the highest threat level from both analyses
        SecurityThreatLevel injectionLevel = convertInjectionThreatLevel(injectionResult.getThreatLevel());
        SecurityThreatLevel poisoningLevel = convertPoisoningThreatLevel(poisoningResult.getThreatLevel());

        return injectionLevel.ordinal() > poisoningLevel.ordinal() ? injectionLevel : poisoningLevel;
    }

    private SecurityThreatLevel convertInjectionThreatLevel(PromptInjectionDetector.ThreatLevel level) {
        return switch (level) {
            case CRITICAL -> SecurityThreatLevel.CRITICAL;
            case HIGH -> SecurityThreatLevel.HIGH;
            case MEDIUM -> SecurityThreatLevel.MEDIUM;
            case LOW -> SecurityThreatLevel.LOW;
            case SAFE -> SecurityThreatLevel.SAFE;
        };
    }

    private SecurityThreatLevel convertPoisoningThreatLevel(ModelPoisoningDetector.PoisoningThreatLevel level) {
        return switch (level) {
            case CRITICAL -> SecurityThreatLevel.CRITICAL;
            case HIGH -> SecurityThreatLevel.HIGH;
            case MEDIUM -> SecurityThreatLevel.MEDIUM;
            case LOW -> SecurityThreatLevel.LOW;
            case SAFE -> SecurityThreatLevel.SAFE;
        };
    }

    private long calculateTimeSinceLastRequest(String userId) {
        UserSecurityProfile profile = userProfiles.get(userId);
        return profile != null ? profile.getTimeSinceLastRequest() : Long.MAX_VALUE;
    }

    private boolean isBusinessHours() {
        int hour = LocalDateTime.now().getHour();
        return hour >= 9 && hour <= 17; // 9 AM to 5 PM
    }

    private String generateRequestId() {
        return "req_" + System.nanoTime() + "_" + Thread.currentThread().getId();
    }

    // Enums and data classes
    public enum SecurityThreatLevel {
        SAFE, LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum SecurityResponseAction {
        ALLOW, FLAG, BLOCK
    }

    public static class AIRequestData {
        private final String modelType;
        private final String input;
        private final Map<String, Object> metadata;

        public AIRequestData(String modelType, String input, Map<String, Object> metadata) {
            this.modelType = modelType;
            this.input = input;
            this.metadata = metadata;
        }

        public String getModelType() { return modelType; }
        public String getInput() { return input; }
        public Map<String, Object> getMetadata() { return metadata; }
    }

    public static class SecurityAnalysisResult {
        private final SecurityThreatLevel threatLevel;
        private final double riskScore;
        private final Map<String, Object> details;
        private final LocalDateTime analyzedAt;

        public SecurityAnalysisResult(SecurityThreatLevel threatLevel, double riskScore,
                                    Map<String, Object> details, LocalDateTime analyzedAt) {
            this.threatLevel = threatLevel;
            this.riskScore = riskScore;
            this.details = details;
            this.analyzedAt = analyzedAt;
        }

        public SecurityThreatLevel getThreatLevel() { return threatLevel; }
        public double getRiskScore() { return riskScore; }
        public Map<String, Object> getDetails() { return details; }
        public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    }

    private static class UserSecurityProfile {
        private final String userId;
        private int violationCount = 0;
        private double totalRiskScore = 0.0;
        private int eventCount = 0;
        private long lastRequestTime = 0;
        private final List<SecurityEvent> recentEvents = new java.util.ArrayList<>();

        public UserSecurityProfile(String userId) {
            this.userId = userId;
        }

        public synchronized void recordSecurityEvent(SecurityAnalysisResult analysis, SecurityResponseAction action) {
            SecurityEvent event = new SecurityEvent(analysis, action, System.currentTimeMillis());
            recentEvents.add(event);

            // Keep only last 10 events
            if (recentEvents.size() > 10) {
                recentEvents.remove(0);
            }

            if (action == SecurityResponseAction.BLOCK || analysis.getThreatLevel().ordinal() >= SecurityThreatLevel.MEDIUM.ordinal()) {
                violationCount++;
            }

            totalRiskScore += analysis.getRiskScore();
            eventCount++;
            lastRequestTime = System.currentTimeMillis();
        }

        public synchronized int getRecentViolationCount() {
            long oneHourAgo = System.currentTimeMillis() - 3600000;
            return (int) recentEvents.stream()
                .filter(event -> event.timestamp > oneHourAgo)
                .filter(event -> event.action == SecurityResponseAction.BLOCK ||
                               event.analysis.getThreatLevel().ordinal() >= SecurityThreatLevel.MEDIUM.ordinal())
                .count();
        }

        public synchronized double getAverageRiskScore() {
            return eventCount > 0 ? totalRiskScore / eventCount : 0.0;
        }

        public synchronized long getTimeSinceLastRequest() {
            return System.currentTimeMillis() - lastRequestTime;
        }

        public synchronized boolean hasSuspiciousPattern() {
            // Check for patterns indicating attack campaigns
            long recentBlocks = getRecentViolationCount();
            return recentBlocks >= 3 || getAverageRiskScore() > 60.0;
        }

        private static class SecurityEvent {
            final SecurityAnalysisResult analysis;
            final SecurityResponseAction action;
            final long timestamp;

            SecurityEvent(SecurityAnalysisResult analysis, SecurityResponseAction action, long timestamp) {
                this.analysis = analysis;
                this.action = action;
                this.timestamp = timestamp;
            }
        }
    }
}
