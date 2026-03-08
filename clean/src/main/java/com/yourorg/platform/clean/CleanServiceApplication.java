package com.yourorg.platform.clean;

import com.yourorg.platform.clean.framework.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class CleanServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CleanServiceApplication.class, args);
    }
}
