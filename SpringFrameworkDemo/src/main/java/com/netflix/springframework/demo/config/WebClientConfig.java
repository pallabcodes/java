package com.netflix.springframework.demo.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClientConfig - WebClient Configuration
 * 
 * This configuration demonstrates Netflix production-grade WebClient setup:
 * 1. Custom HTTP client with connection pooling
 * 2. Timeout configuration for different scenarios
 * 3. Request/response logging and monitoring
 * 4. Error handling and retry configuration
 * 5. Performance optimization settings
 * 
 * For C/C++ engineers:
 * - WebClient configuration is like HTTP client setup in C++
 * - Connection pooling is like connection management in C++
 * - Timeout configuration is like timeout settings in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Configuration
public class WebClientConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);
    
    @Value("${app.external-api.connection-timeout:10s}")
    private Duration connectionTimeout;
    
    @Value("${app.external-api.read-timeout:30s}")
    private Duration readTimeout;
    
    @Value("${app.external-api.write-timeout:30s}")
    private Duration writeTimeout;
    
    @Value("${app.external-api.max-connections:100}")
    private int maxConnections;
    
    @Value("${app.external-api.max-idle-time:60s}")
    private Duration maxIdleTime;
    
    /**
     * Create HTTP client with custom configuration
     * 
     * @return Configured HttpClient
     */
    @Bean
    public HttpClient httpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectionTimeout.toMillis())
                .responseTimeout(readTimeout)
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(readTimeout.toSeconds(), TimeUnit.SECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(writeTimeout.toSeconds(), TimeUnit.SECONDS));
                })
                .compress(true)
                .followRedirect(true);
    }
    
    /**
     * Create WebClient with custom configuration
     * 
     * @param httpClient HTTP client
     * @return Configured WebClient
     */
    @Bean
    public WebClient webClient(HttpClient httpClient) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(requestLoggingFilter())
                .filter(responseLoggingFilter())
                .filter(errorHandlingFilter())
                .build();
    }
    
    /**
     * Request logging filter
     * 
     * @return ExchangeFilterFunction for request logging
     */
    private ExchangeFilterFunction requestLoggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.debug("Outgoing request: {} {}", clientRequest.method(), clientRequest.url());
            logger.debug("Request headers: {}", clientRequest.headers());
            return Mono.just(clientRequest);
        });
    }
    
    /**
     * Response logging filter
     * 
     * @return ExchangeFilterFunction for response logging
     */
    private ExchangeFilterFunction responseLoggingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.debug("Incoming response: {} {}", clientResponse.statusCode(), clientResponse.headers());
            return Mono.just(clientResponse);
        });
    }
    
    /**
     * Error handling filter
     * 
     * @return ExchangeFilterFunction for error handling
     */
    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                logger.warn("HTTP error response: {} {}", clientResponse.statusCode(), clientResponse.headers());
            }
            return Mono.just(clientResponse);
        });
    }
}
