package com.netflix.productivity.attachment.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@ConditionalOnProperty(prefix = "attachments.storage", name = "provider", havingValue = "s3")
public class MinioBucketInitializer {

    @Value("${attachments.storage.s3.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${attachments.storage.s3.accessKey:minioadmin}")
    private String accessKey;

    @Value("${attachments.storage.s3.secretKey:minioadmin}")
    private String secretKey;

    @Value("${attachments.storage.s3.bucket:attachments}")
    private String bucket;

    @PostConstruct
    public void ensureBucket() {
        try {
            Class<?> clientClazz = Class.forName("io.minio.MinioClient");
            Object client = clientClazz.getMethod("builder").invoke(null);
            client = client.getClass().getMethod("endpoint", String.class).invoke(client, endpoint);
            client = client.getClass().getMethod("credentials", String.class, String.class).invoke(client, accessKey, secretKey);
            client = client.getClass().getMethod("build").invoke(client);

            Class<?> existsArgsClazz = Class.forName("io.minio.BucketExistsArgs");
            Object existsArgs = existsArgsClazz.getMethod("builder").invoke(null);
            existsArgs = existsArgs.getClass().getMethod("bucket", String.class).invoke(existsArgs, bucket);
            existsArgs = existsArgs.getClass().getMethod("build").invoke(existsArgs);
            boolean exists = (boolean) client.getClass().getMethod("bucketExists", existsArgsClazz).invoke(client, existsArgs);
            if (!exists) {
                Class<?> makeArgsClazz = Class.forName("io.minio.MakeBucketArgs");
                Object makeArgs = makeArgsClazz.getMethod("builder").invoke(null);
                makeArgs = makeArgs.getClass().getMethod("bucket", String.class).invoke(makeArgs, bucket);
                makeArgs = makeArgs.getClass().getMethod("build").invoke(makeArgs);
                client.getClass().getMethod("makeBucket", makeArgsClazz).invoke(client, makeArgs);
            }
        } catch (ClassNotFoundException e) {
            // MinIO not on classpath, skip
        } catch (Exception e) {
            throw new IllegalStateException("Failed to ensure MinIO bucket", e);
        }
    }
}

