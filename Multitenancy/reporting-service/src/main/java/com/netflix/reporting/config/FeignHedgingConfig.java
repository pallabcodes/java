package com.netflix.reporting.config;

import feign.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class FeignHedgingConfig {

    @Value("${feign.hedge.delay.ms:100}")
    private long hedgeDelayMs;

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService feignHedgeExecutor() {
        return Executors.newScheduledThreadPool(4);
    }

    @Bean
    public Client feignClient(Client defaultClient, ScheduledExecutorService feignHedgeExecutor) {
        return new HedgingFeignClient(defaultClient, feignHedgeExecutor, hedgeDelayMs);
    }
}


