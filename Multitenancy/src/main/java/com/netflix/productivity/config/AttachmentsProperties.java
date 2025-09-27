package com.netflix.productivity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "attachments.storage")
public class AttachmentsProperties {
    private String provider = "s3"; // default switched to s3 per request
    private String baseDir = "/tmp/attachments";
    private String s3Endpoint = "http://localhost:9000";
    private String s3AccessKey = "minioadmin";
    private String s3SecretKey = "minioadmin";
    private String s3Bucket = "attachments";
}

