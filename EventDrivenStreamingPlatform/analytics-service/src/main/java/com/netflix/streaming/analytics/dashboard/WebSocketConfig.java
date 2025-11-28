package com.netflix.streaming.analytics.dashboard;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket Configuration for Real-Time Dashboards.
 *
 * Configures WebSocket endpoints for streaming analytics data.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DashboardWebSocketHandler dashboardHandler;
    private final ContentWebSocketHandler contentHandler;

    public WebSocketConfig(DashboardWebSocketHandler dashboardHandler,
                          ContentWebSocketHandler contentHandler) {
        this.dashboardHandler = dashboardHandler;
        this.contentHandler = contentHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Dashboard endpoint for real-time metrics
        registry.addHandler(dashboardHandler, "/ws/dashboard")
                .setAllowedOrigins("*") // Configure appropriately for production
                .withSockJS(); // Fallback for browsers without WebSocket support

        // Content-specific analytics endpoint
        registry.addHandler(contentHandler, "/ws/content/{contentId}")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}