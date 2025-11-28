package com.netflix.springai.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class PromptInjectionDetector {

    private static final Logger logger = LoggerFactory.getLogger(PromptInjectionDetector.class);

    // Common prompt injection patterns
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
        // Direct instruction overrides
        Pattern.compile("(?i)\\b(ignore|forget|disregard)\\s+(all\\s+)?previous\\s+instructions\\b"),
        Pattern.compile("(?i)\\b(override|change|modify)\\s+(these|the|all)\\s+instructions\\b"),
        Pattern.compile("(?i)\\bfrom\\s+now\\s+on\\b.*\\b(you\\s+are|act\\s+as)\\b"),
        Pattern.compile("(?i)\\b(your\\s+)?new\\s+(role|persona|identity|instructions?)\\b"),

        // System prompt manipulation
        Pattern.compile("(?i)\\b(system|developer)\\s+prompt\\b"),
        Pattern.compile("(?i)\\b(backend|internal)\\s+(instruction|command|directive)s?\\b"),
        Pattern.compile("(?i)\\b(hidden|secret|confidential)\\s+(prompt|instruction)s?\\b"),

        // Role-playing attacks
        Pattern.compile("(?i)\\bpretend\\s+(to\\s+be|you\\s+are|acting\\s+as)\\b"),
        Pattern.compile("(?i)\\byou\\s+are\\s+now\\s+\\w+\\s*,\\s*forgotten\\s+\\w+\\b"),
        Pattern.compile("(?i)\\blet'?s\\s+play\\s+a\\s+game\\b"),

        // Jailbreak attempts
        Pattern.compile("(?i)\\b(DAN|Developer\\s+Mode|Uncensored|Unfiltered)\\b"),
        Pattern.compile("(?i)\\b(jailbreak|break\\s+out\\s+of|escape)\\s+(jail|restrictions?|rules?)\\b"),
        Pattern.compile("(?i)\\b(override|circumvent|bypass)\\s+(safety|security|filter)s?\\b"),

        // Encoding tricks
        Pattern.compile("(?i)\\b(base64|rot13|hex|binary)\\s+(encoded?|decoded?)\\b"),
        Pattern.compile("(?i)\\b(ignore|skip)\\s+(any\\s+)?filter(s|ing)?\\b"),
        Pattern.compile("(?i)\\b(reverse|invert|flip)\\s+(the\\s+)?instruction\\w*\\b"),

        // Separator manipulation
        Pattern.compile("(?i)\\b\\[\\s*(system|assistant|user)\\s*\\]\\s*:"),
        Pattern.compile("(?i)\\b###\\s*(end|start)\\s+(of|system)\\s+prompt\\b"),
        Pattern.compile("(?i)\\b---\\s*(system|user|assistant)\\s+---\\b"),

        // Meta-comment attacks
        Pattern.compile("(?i)\\b(reminder|note|important)\\s*:.*\\b(ignore|forget|override)\\b"),
        Pattern.compile("(?i)\\b(do\\s+not|never)\\s+(follow|obey)\\s+(this|these)\\s+(instruction|rule)s?\\b"),
        Pattern.compile("(?i)\\b(exception|special\\s+case)\\s+(to|for)\\s+(the|these)\\s+rule\\w*\\b")
    );

    // Contextual risk indicators
    private static final Set<String> HIGH_RISK_KEYWORDS = Set.of(
        "ignore", "forget", "override", "system", "developer", "internal", "hidden",
        "pretend", "jailbreak", "bypass", "uncensored", "dan", "developer mode",
        "base64", "encoded", "filter", "separator", "meta", "instruction"
    );

    // Safe prompt templates and allowed patterns
    private static final List<Pattern> ALLOWED_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)\\b(please|could\\s+you|can\\s+you)\\b.*\\?"),
        Pattern.compile("(?i)\\b(help|assist|support)\\s+(me|us|with)\\b"),
        Pattern.compile("(?i)\\b(explain|describe|tell\\s+me)\\s+(about|how)\\b"),
        Pattern.compile("(?i)\\b(what|how|why|when|where|who)\\s+(is|are|do|does|did|was|were)\\b")
    );

    private final Map<String, InjectionAttempt> recentAttempts = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, InjectionAttempt> eldest) {
            return size() > 1000; // Keep last 1000 attempts
        }
    };

    public DetectionResult analyzePrompt(String prompt, String userId, Map<String, Object> context) {
        long startTime = System.nanoTime();

        // Basic sanitization
        String sanitizedPrompt = sanitizeInput(prompt);

        // Multi-layer analysis
        List<String> detectedPatterns = detectInjectionPatterns(sanitizedPrompt);
        double riskScore = calculateRiskScore(sanitizedPrompt, detectedPatterns, context);
        ThreatLevel threatLevel = determineThreatLevel(riskScore, detectedPatterns.size());

        // Contextual analysis
        boolean isContextualRisk = analyzeContext(context);
        List<String> recommendations = generateRecommendations(threatLevel, detectedPatterns);

        DetectionResult result = new DetectionResult(
            threatLevel,
            riskScore,
            detectedPatterns,
            isContextualRisk,
            recommendations,
            System.nanoTime() - startTime
        );

        // Log injection attempts
        if (threatLevel != ThreatLevel.SAFE) {
            logInjectionAttempt(prompt, userId, result);
        }

        return result;
    }

    public SanitizedPrompt sanitizePrompt(String prompt, DetectionResult detectionResult) {
        if (detectionResult.getThreatLevel() == ThreatLevel.SAFE) {
            return new SanitizedPrompt(prompt, false, Collections.emptyList());
        }

        // Apply sanitization based on threat level
        String sanitized = applySanitization(prompt, detectionResult);

        List<String> appliedFilters = new ArrayList<>();
        if (detectionResult.getThreatLevel() == ThreatLevel.HIGH ||
            detectionResult.getThreatLevel() == ThreatLevel.CRITICAL) {
            appliedFilters.add("CONTENT_BLOCKED");
        } else {
            appliedFilters.add("PATTERN_REMOVAL");
            appliedFilters.add("SAFE_REPLACEMENT");
        }

        return new SanitizedPrompt(sanitized, true, appliedFilters);
    }

    private String sanitizeInput(String input) {
        if (input == null) return "";

        // Remove excessive whitespace and normalize
        return input.trim()
            .replaceAll("\\s+", " ")
            .replaceAll("[\\x00-\\x1F\\x7F-\\x9F]", "") // Remove control characters
            .substring(0, Math.min(input.length(), 10000)); // Limit length
    }

    private List<String> detectInjectionPatterns(String prompt) {
        List<String> detected = new ArrayList<>();

        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(prompt).find()) {
                detected.add(pattern.pattern());
            }
        }

        return detected.stream().distinct().collect(Collectors.toList());
    }

    private double calculateRiskScore(String prompt, List<String> detectedPatterns, Map<String, Object> context) {
        double baseScore = 0.0;

        // Pattern-based scoring
        baseScore += detectedPatterns.size() * 25.0; // Each pattern adds 25 points

        // Keyword density scoring
        long highRiskKeywordCount = HIGH_RISK_KEYWORDS.stream()
            .mapToLong(keyword -> countOccurrences(prompt.toLowerCase(), keyword))
            .sum();
        baseScore += highRiskKeywordCount * 10.0;

        // Length-based scoring (very long prompts might be hiding attacks)
        if (prompt.length() > 2000) {
            baseScore += 15.0;
        } else if (prompt.length() > 1000) {
            baseScore += 5.0;
        }

        // Contextual scoring
        if (context.containsKey("previous_attempts") &&
            (Integer) context.getOrDefault("previous_attempts", 0) > 3) {
            baseScore += 30.0; // User has attempted injections before
        }

        // Time-based scoring (rapid successive requests)
        if (context.containsKey("time_since_last_request") &&
            (Long) context.getOrDefault("time_since_last_request", 1000L) < 100) {
            baseScore += 20.0; // Potential automated attack
        }

        // Cap at 100
        return Math.min(baseScore, 100.0);
    }

    private ThreatLevel determineThreatLevel(double riskScore, int patternCount) {
        if (riskScore >= 80.0 || patternCount >= 3) {
            return ThreatLevel.CRITICAL;
        } else if (riskScore >= 60.0 || patternCount >= 2) {
            return ThreatLevel.HIGH;
        } else if (riskScore >= 40.0 || patternCount >= 1) {
            return ThreatLevel.MEDIUM;
        } else if (riskScore >= 20.0) {
            return ThreatLevel.LOW;
        } else {
            return ThreatLevel.SAFE;
        }
    }

    private boolean analyzeContext(Map<String, Object> context) {
        // Check for suspicious patterns in request context
        Integer requestCount = (Integer) context.getOrDefault("hourly_request_count", 0);
        if (requestCount > 100) return true; // High request volume

        Long avgResponseTime = (Long) context.getOrDefault("avg_response_time", 0L);
        if (avgResponseTime > 30000) return true; // Slow responses might indicate complex attacks

        String userAgent = (String) context.getOrDefault("user_agent", "");
        if (userAgent.contains("bot") || userAgent.contains("crawler")) return true;

        return false;
    }

    private List<String> generateRecommendations(ThreatLevel threatLevel, List<String> detectedPatterns) {
        List<String> recommendations = new ArrayList<>();

        switch (threatLevel) {
            case CRITICAL:
                recommendations.add("BLOCK_REQUEST: Critical threat detected");
                recommendations.add("ALERT_SECURITY_TEAM: Immediate investigation required");
                recommendations.add("RATE_LIMIT_USER: Temporarily restrict user access");
                break;
            case HIGH:
                recommendations.add("SANITIZE_PROMPT: Remove detected injection patterns");
                recommendations.add("LOG_FOR_REVIEW: Store for security analysis");
                recommendations.add("MONITOR_USER: Increase monitoring for this user");
                break;
            case MEDIUM:
                recommendations.add("SANITIZE_PROMPT: Clean potentially harmful content");
                recommendations.add("ADD_DISCLAIMER: Include safety notice in response");
                break;
            case LOW:
                recommendations.add("MONITOR_REQUEST: Log for pattern analysis");
                break;
            case SAFE:
                // No recommendations needed
                break;
        }

        if (!detectedPatterns.isEmpty()) {
            recommendations.add("PATTERN_ANALYSIS: Review patterns " + detectedPatterns.size() + " detected patterns");
        }

        return recommendations;
    }

    private String applySanitization(String prompt, DetectionResult detectionResult) {
        if (detectionResult.getThreatLevel() == ThreatLevel.CRITICAL ||
            detectionResult.getThreatLevel() == ThreatLevel.HIGH) {
            // For high-risk prompts, return a safe response instead
            return "I'm sorry, but I cannot process this request as it appears to contain potentially harmful content. " +
                   "Please rephrase your request and try again.";
        }

        // For medium/low risk, apply pattern removal
        String sanitized = prompt;

        // Remove detected patterns
        for (String patternStr : detectionResult.getDetectedPatterns()) {
            try {
                Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                sanitized = pattern.matcher(sanitized).replaceAll("[CONTENT_FILTERED]");
            } catch (Exception e) {
                logger.warn("Failed to sanitize pattern: {}", patternStr, e);
            }
        }

        // Additional safety measures
        sanitized = sanitized.replaceAll("(?i)\\b(system|developer)\\b", "[FILTERED]");
        sanitized = sanitized.replaceAll("(?i)\\b(ignore|forget|override)\\b", "[FILTERED]");

        return sanitized;
    }

    private void logInjectionAttempt(String originalPrompt, String userId, DetectionResult result) {
        String attemptId = UUID.randomUUID().toString();
        InjectionAttempt attempt = new InjectionAttempt(
            attemptId,
            userId,
            originalPrompt.substring(0, Math.min(originalPrompt.length(), 500)), // Truncate for logging
            result.getThreatLevel(),
            result.getRiskScore(),
            result.getDetectedPatterns(),
            System.currentTimeMillis()
        );

        recentAttempts.put(attemptId, attempt);

        logger.warn("PROMPT_INJECTION_DETECTED: User={}, ThreatLevel={}, RiskScore={}, Patterns={}",
                   userId, result.getThreatLevel(), result.getRiskScore(), result.getDetectedPatterns().size());
    }

    private long countOccurrences(String text, String keyword) {
        return Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE)
            .matcher(text)
            .results()
            .count();
    }

    // Getter for monitoring
    public Map<String, InjectionAttempt> getRecentAttempts() {
        return new LinkedHashMap<>(recentAttempts);
    }

    public Map<String, Integer> getInjectionStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_attempts", recentAttempts.size());

        Map<ThreatLevel, Long> threatDistribution = recentAttempts.values().stream()
            .collect(Collectors.groupingBy(InjectionAttempt::getThreatLevel, Collectors.counting()));

        for (ThreatLevel level : ThreatLevel.values()) {
            stats.put(level.name().toLowerCase() + "_threats",
                     threatDistribution.getOrDefault(level, 0L).intValue());
        }

        return stats;
    }

    // Enums and data classes
    public enum ThreatLevel {
        SAFE, LOW, MEDIUM, HIGH, CRITICAL
    }

    public static class DetectionResult {
        private final ThreatLevel threatLevel;
        private final double riskScore;
        private final List<String> detectedPatterns;
        private final boolean contextualRisk;
        private final List<String> recommendations;
        private final long processingTimeNs;

        public DetectionResult(ThreatLevel threatLevel, double riskScore, List<String> detectedPatterns,
                             boolean contextualRisk, List<String> recommendations, long processingTimeNs) {
            this.threatLevel = threatLevel;
            this.riskScore = riskScore;
            this.detectedPatterns = detectedPatterns;
            this.contextualRisk = contextualRisk;
            this.recommendations = recommendations;
            this.processingTimeNs = processingTimeNs;
        }

        // Getters
        public ThreatLevel getThreatLevel() { return threatLevel; }
        public double getRiskScore() { return riskScore; }
        public List<String> getDetectedPatterns() { return detectedPatterns; }
        public boolean isContextualRisk() { return contextualRisk; }
        public List<String> getRecommendations() { return recommendations; }
        public long getProcessingTimeNs() { return processingTimeNs; }
    }

    public static class SanitizedPrompt {
        private final String content;
        private final boolean wasModified;
        private final List<String> appliedFilters;

        public SanitizedPrompt(String content, boolean wasModified, List<String> appliedFilters) {
            this.content = content;
            this.wasModified = wasModified;
            this.appliedFilters = appliedFilters;
        }

        // Getters
        public String getContent() { return content; }
        public boolean wasModified() { return wasModified; }
        public List<String> getAppliedFilters() { return appliedFilters; }
    }

    private static class InjectionAttempt {
        private final String id;
        private final String userId;
        private final String promptSnippet;
        private final ThreatLevel threatLevel;
        private final double riskScore;
        private final List<String> detectedPatterns;
        private final long timestamp;

        public InjectionAttempt(String id, String userId, String promptSnippet, ThreatLevel threatLevel,
                              double riskScore, List<String> detectedPatterns, long timestamp) {
            this.id = id;
            this.userId = userId;
            this.promptSnippet = promptSnippet;
            this.threatLevel = threatLevel;
            this.riskScore = riskScore;
            this.detectedPatterns = detectedPatterns;
            this.timestamp = timestamp;
        }

        // Getters
        public String getId() { return id; }
        public String getUserId() { return userId; }
        public String getPromptSnippet() { return promptSnippet; }
        public ThreatLevel getThreatLevel() { return threatLevel; }
        public double getRiskScore() { return riskScore; }
        public List<String> getDetectedPatterns() { return detectedPatterns; }
        public long getTimestamp() { return timestamp; }
    }
}
