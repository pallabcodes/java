package com.yourorg.platform.clean.framework.config;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

@Configuration
public class AwsConfig {
    @Bean
    public SqsClient sqsClient(AppProperties properties) {
        SqsClientBuilder builder = SqsClient.builder().region(Region.of(properties.getAws().getRegion()));
        applyEndpointOverride(builder, properties);
        return builder.build();
    }

    @Bean
    public SnsClient snsClient(AppProperties properties) {
        SnsClientBuilder builder = SnsClient.builder().region(Region.of(properties.getAws().getRegion()));
        applyEndpointOverride(builder, properties);
        return builder.build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient(AppProperties properties) {
        DynamoDbClientBuilder builder = DynamoDbClient.builder().region(Region.of(properties.getAws().getRegion()));
        applyEndpointOverride(builder, properties);
        return builder.build();
    }

    @Bean
    public S3Client s3Client(AppProperties properties) {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(properties.getAws().getRegion()));
        applyEndpointOverride(builder, properties);
        return builder.build();
    }

    private static void applyEndpointOverride(software.amazon.awssdk.awscore.client.builder.AwsClientBuilder<?, ?> builder, AppProperties properties) {
        String endpoint = properties.getAws().getEndpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
    }
}
