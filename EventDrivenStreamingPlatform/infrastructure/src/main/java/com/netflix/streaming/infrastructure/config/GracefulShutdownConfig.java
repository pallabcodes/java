package com.netflix.streaming.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Graceful shutdown configuration for production deployments.
 * 
 * Ensures:
 * - In-flight requests complete
 * - Connections drain properly
 * - Resources are released cleanly
 */
@Configuration
public class GracefulShutdownConfig implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdownConfig.class);
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

    @Bean
    public GracefulShutdown gracefulShutdown() {
        return new GracefulShutdown();
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory(
            final GracefulShutdown gracefulShutdown) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(gracefulShutdown);
        return factory;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        logger.info("Application context closing, initiating graceful shutdown");
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                logger.info("Waiting {} seconds for graceful shutdown", SHUTDOWN_TIMEOUT_SECONDS);
                Thread.sleep(SHUTDOWN_TIMEOUT_SECONDS * 1000L);
                logger.info("Graceful shutdown completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Graceful shutdown interrupted");
            }
        });
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Graceful shutdown connector customizer
     */
    public static class GracefulShutdown implements org.apache.catalina.connector.ConnectorCustomizer,
            org.springframework.context.ApplicationListener<org.springframework.context.event.ContextClosedEvent> {

        private volatile org.apache.catalina.connector.Connector connector;

        @Override
        public void customize(org.apache.catalina.connector.Connector connector) {
            this.connector = connector;
        }

        @Override
        public void onApplicationEvent(org.springframework.context.event.ContextClosedEvent event) {
            if (this.connector != null) {
                this.connector.pause();
                org.apache.catalina.Executor executor = this.connector.getProtocolHandler().getExecutor();
                if (executor instanceof java.util.concurrent.ExecutorService) {
                    try {
                        ((java.util.concurrent.ExecutorService) executor).shutdown();
                        if (!((java.util.concurrent.ExecutorService) executor).awaitTermination(
                                SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                            logger.warn("Executor did not terminate within timeout, forcing shutdown");
                            ((java.util.concurrent.ExecutorService) executor).shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        ((java.util.concurrent.ExecutorService) executor).shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}

