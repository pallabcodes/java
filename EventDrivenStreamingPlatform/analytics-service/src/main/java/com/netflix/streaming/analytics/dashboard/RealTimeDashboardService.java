package com.netflix.streaming.analytics.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.streaming.analytics.processor.RealTimeAnalyticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Set;

/**
 * Real-Time Dashboard Service.
 *
 * Manages WebSocket connections and streams real-time analytics to dashboard clients.
 * This demonstrates WebSocket streaming for live dashboards.
 */
@Service
public class RealTimeDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeDashboardService.class);

    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> dashboardSessions = new CopyOnWriteArraySet<>();
    private final ConcurrentHashMap<String, Set<WebSocketSession>> contentSubscriptions = new ConcurrentHashMap<>();

    public RealTimeDashboardService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Handle new dashboard WebSocket connection
     */
    public void addDashboardSession(WebSocketSession session) {
        dashboardSessions.add(session);
        logger.info("Dashboard client connected: {}", session.getId());

        // Send initial metrics
        try {
            session.sendMessage(new TextMessage(createWelcomeMessage()));
        } catch (IOException e) {
            logger.error("Failed to send welcome message to dashboard client", e);
        }
    }

    /**
     * Handle dashboard WebSocket disconnection
     */
    public void removeDashboardSession(WebSocketSession session) {
        dashboardSessions.remove(session);
        // Clean up content subscriptions
        contentSubscriptions.values().forEach(sessions -> sessions.remove(session));
        logger.info("Dashboard client disconnected: {}", session.getId());
    }

    /**
     * Subscribe to specific content analytics
     */
    public void subscribeToContent(WebSocketSession session, String contentId) {
        contentSubscriptions.computeIfAbsent(contentId, k -> new CopyOnWriteArraySet<>()).add(session);
        logger.debug("Dashboard client {} subscribed to content {}", session.getId(), contentId);
    }

    /**
     * Unsubscribe from specific content analytics
     */
    public void unsubscribeFromContent(WebSocketSession session, String contentId) {
        Set<WebSocketSession> sessions = contentSubscriptions.get(contentId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                contentSubscriptions.remove(contentId);
            }
        }
        logger.debug("Dashboard client {} unsubscribed from content {}", session.getId(), contentId);
    }

    /**
     * Broadcast metrics update to all dashboard clients
     */
    public void broadcastMetricsUpdate(RealTimeAnalyticsProcessor.RealTimeMetrics metrics) {
        if (dashboardSessions.isEmpty()) {
            return; // No clients connected
        }

        try {
            String message = objectMapper.writeValueAsString(metrics);
            TextMessage textMessage = new TextMessage(message);

            // Send to all dashboard clients
            for (WebSocketSession session : dashboardSessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        logger.warn("Failed to send metrics to dashboard client {}, removing session", session.getId());
                        dashboardSessions.remove(session);
                    }
                } else {
                    dashboardSessions.remove(session);
                }
            }

            logger.debug("Broadcasted real-time metrics to {} dashboard clients", dashboardSessions.size());

        } catch (Exception e) {
            logger.error("Failed to broadcast metrics update", e);
        }
    }

    /**
     * Send content-specific update to subscribed clients
     */
    public void sendContentUpdate(String contentId, Object contentMetrics) {
        Set<WebSocketSession> subscribers = contentSubscriptions.get(contentId);
        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }

        try {
            String message = objectMapper.writeValueAsString(java.util.Map.of(
                "type", "content_update",
                "contentId", contentId,
                "metrics", contentMetrics,
                "timestamp", java.time.Instant.now()
            ));
            TextMessage textMessage = new TextMessage(message);

            for (WebSocketSession session : subscribers) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        logger.warn("Failed to send content update to client {}", session.getId());
                        subscribers.remove(session);
                    }
                } else {
                    subscribers.remove(session);
                }
            }

            logger.debug("Sent content update for {} to {} subscribers", contentId, subscribers.size());

        } catch (Exception e) {
            logger.error("Failed to send content update for {}", contentId, e);
        }
    }

    /**
     * Get dashboard statistics
     */
    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
            .connectedClients(dashboardSessions.size())
            .contentSubscriptions(contentSubscriptions.size())
            .totalSubscriptions(contentSubscriptions.values().stream()
                .mapToInt(Set::size).sum())
            .build();
    }

    private String createWelcomeMessage() {
        try {
            return objectMapper.writeValueAsString(java.util.Map.of(
                "type", "welcome",
                "message", "Connected to Netflix Streaming Analytics Dashboard",
                "timestamp", java.time.Instant.now(),
                "supportedFeatures", java.util.Arrays.asList(
                    "real_time_metrics",
                    "content_subscriptions",
                    "alerts"
                )
            ));
        } catch (Exception e) {
            return "{\"type\":\"welcome\",\"message\":\"Connected\"}";
        }
    }

    /**
     * Dashboard statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class DashboardStats {
        private int connectedClients;
        private int contentSubscriptions;
        private int totalSubscriptions;
    }
}