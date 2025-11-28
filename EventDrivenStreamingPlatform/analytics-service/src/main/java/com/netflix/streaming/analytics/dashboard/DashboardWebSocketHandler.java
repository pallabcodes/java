package com.netflix.streaming.analytics.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket Handler for Dashboard Real-Time Updates.
 *
 * Handles dashboard client connections and manages subscriptions.
 */
@Component
public class DashboardWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(DashboardWebSocketHandler.class);

    private final RealTimeDashboardService dashboardService;
    private final ObjectMapper objectMapper;

    public DashboardWebSocketHandler(RealTimeDashboardService dashboardService,
                                    ObjectMapper objectMapper) {
        this.dashboardService = dashboardService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("Dashboard WebSocket connection established: {}", session.getId());
        dashboardService.addDashboardSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Dashboard WebSocket connection closed: {} with status: {}", session.getId(), status);
        dashboardService.removeDashboardSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            JsonNode jsonNode = objectMapper.readTree(payload);

            String messageType = jsonNode.get("type").asText();

            switch (messageType) {
                case "subscribe_content":
                    handleContentSubscription(session, jsonNode);
                    break;
                case "unsubscribe_content":
                    handleContentUnsubscription(session, jsonNode);
                    break;
                case "ping":
                    handlePing(session);
                    break;
                default:
                    logger.warn("Unknown message type: {} from session: {}", messageType, session.getId());
            }

        } catch (Exception e) {
            logger.error("Error handling dashboard message from session: {}", session.getId(), e);
            sendErrorMessage(session, "Invalid message format");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("Dashboard WebSocket transport error for session: {}", session.getId(), exception);
        dashboardService.removeDashboardSession(session);
    }

    private void handleContentSubscription(WebSocketSession session, JsonNode message) {
        String contentId = message.get("contentId").asText();
        dashboardService.subscribeToContent(session, contentId);

        sendAckMessage(session, "subscribed", contentId);
        logger.debug("Dashboard client {} subscribed to content {}", session.getId(), contentId);
    }

    private void handleContentUnsubscription(WebSocketSession session, JsonNode message) {
        String contentId = message.get("contentId").asText();
        dashboardService.unsubscribeFromContent(session, contentId);

        sendAckMessage(session, "unsubscribed", contentId);
        logger.debug("Dashboard client {} unsubscribed from content {}", session.getId(), contentId);
    }

    private void handlePing(WebSocketSession session) {
        sendPongMessage(session);
    }

    private void sendAckMessage(WebSocketSession session, String action, String contentId) {
        try {
            String ackMessage = objectMapper.writeValueAsString(java.util.Map.of(
                "type", "ack",
                "action", action,
                "contentId", contentId,
                "timestamp", java.time.Instant.now()
            ));
            session.sendMessage(new TextMessage(ackMessage));
        } catch (Exception e) {
            logger.error("Failed to send ACK message to session: {}", session.getId(), e);
        }
    }

    private void sendPongMessage(WebSocketSession session) {
        try {
            String pongMessage = objectMapper.writeValueAsString(java.util.Map.of(
                "type", "pong",
                "timestamp", java.time.Instant.now()
            ));
            session.sendMessage(new TextMessage(pongMessage));
        } catch (Exception e) {
            logger.error("Failed to send PONG message to session: {}", session.getId(), e);
        }
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            String errorResponse = objectMapper.writeValueAsString(java.util.Map.of(
                "type", "error",
                "message", errorMessage,
                "timestamp", java.time.Instant.now()
            ));
            session.sendMessage(new TextMessage(errorResponse));
        } catch (Exception e) {
            logger.error("Failed to send error message to session: {}", session.getId(), e);
        }
    }
}