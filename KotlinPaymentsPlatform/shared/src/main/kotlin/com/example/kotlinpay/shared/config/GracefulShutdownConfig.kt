package com.example.kotlinpay.shared.config

import org.slf4j.LoggerFactory
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextClosedEvent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Graceful shutdown configuration for production deployments.
 * 
 * Ensures:
 * - In-flight requests complete
 * - Connections drain properly
 * - Resources are released cleanly
 */
@Configuration
class GracefulShutdownConfig : ApplicationListener<ContextClosedEvent> {

    private val logger = LoggerFactory.getLogger(GracefulShutdownConfig::class.java)
    private val shutdownTimeoutSeconds = 30

    @Bean
    fun gracefulShutdown(): GracefulShutdown {
        return GracefulShutdown()
    }

    @Bean
    fun webServerFactory(gracefulShutdown: GracefulShutdown): ConfigurableServletWebServerFactory {
        val factory = TomcatServletWebServerFactory()
        factory.addConnectorCustomizers(gracefulShutdown)
        return factory
    }

    override fun onApplicationEvent(event: ContextClosedEvent) {
        logger.info("Application context closing, initiating graceful shutdown")
        
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        executor.submit {
            try {
                logger.info("Waiting {} seconds for graceful shutdown", shutdownTimeoutSeconds)
                Thread.sleep(shutdownTimeoutSeconds * 1000L)
                logger.info("Graceful shutdown completed")
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                logger.warn("Graceful shutdown interrupted")
            }
        }
        
        executor.shutdown()
        try {
            if (!executor.awaitTermination(shutdownTimeoutSeconds.toLong(), TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }

    /**
     * Graceful shutdown connector customizer
     */
    class GracefulShutdown : org.apache.catalina.connector.ConnectorCustomizer,
        org.springframework.context.ApplicationListener<org.springframework.context.event.ContextClosedEvent> {

        @Volatile
        private var connector: org.apache.catalina.connector.Connector? = null

        override fun customize(connector: org.apache.catalina.connector.Connector) {
            this.connector = connector
        }

        override fun onApplicationEvent(event: org.springframework.context.event.ContextClosedEvent) {
            connector?.let {
                it.pause()
                val executor = it.protocolHandler.executor
                if (executor is ExecutorService) {
                    try {
                        executor.shutdown()
                        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                            logger.warn("Executor did not terminate within timeout, forcing shutdown")
                            executor.shutdownNow()
                        }
                    } catch (e: InterruptedException) {
                        executor.shutdownNow()
                        Thread.currentThread().interrupt()
                    }
                }
            }
        }

        companion object {
            private val logger = LoggerFactory.getLogger(GracefulShutdown::class.java)
        }
    }
}

