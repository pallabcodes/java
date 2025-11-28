package org.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    @Bean
    public ApplicationStartupListener applicationStartupListener() {
        return new ApplicationStartupListener();
    }

    public static class ApplicationStartupListener {
        private static final Logger startupLogger = LoggerFactory.getLogger("application.startup");

        public void onApplicationEvent() {
            startupLogger.info("ProducerConsumer application started successfully");
            startupLogger.info("Kafka bootstrap servers configured");
            startupLogger.info("Prometheus metrics enabled at /actuator/prometheus");
        }
    }
}
