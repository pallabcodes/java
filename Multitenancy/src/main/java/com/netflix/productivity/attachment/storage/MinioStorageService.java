package com.netflix.productivity.attachment.storage;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "attachments.storage", name = "provider", havingValue = "s3")
public class MinioStorageService implements StorageService {

    @Value("${attachments.storage.s3.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${attachments.storage.s3.accessKey:minioadmin}")
    private String accessKey;

    @Value("${attachments.storage.s3.secretKey:minioadmin}")
    private String secretKey;

    @Value("${attachments.storage.s3.bucket:attachments}")
    private String bucket;

    private final MeterRegistry meterRegistry;

    private Object client() {
        try {
            Class<?> clientClazz = Class.forName("io.minio.MinioClient");
            Method builderMethod = clientClazz.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            Method endpointMethod = builder.getClass().getMethod("endpoint", String.class);
            Method credentialsMethod = builder.getClass().getMethod("credentials", String.class, String.class);
            Method buildMethod = builder.getClass().getMethod("build");
            endpointMethod.invoke(builder, endpoint);
            credentialsMethod.invoke(builder, accessKey, secretKey);
            return buildMethod.invoke(builder);
        } catch (Exception e) {
            throw new IllegalStateException("MinIO client not available. Add io.minio:minio dependency.", e);
        }
    }

    @Override
    public InputStream openStream(String storageKey) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Object c = client();
            Class<?> getArgsClazz = Class.forName("io.minio.GetObjectArgs");
            Method builderMethod = getArgsClazz.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            Method bucketMethod = builder.getClass().getMethod("bucket", String.class);
            Method objectMethod = builder.getClass().getMethod("object", String.class);
            Method buildMethod = builder.getClass().getMethod("build");
            bucketMethod.invoke(builder, bucket);
            objectMethod.invoke(builder, storageKey);
            Object args = buildMethod.invoke(builder);
            Method getObject = c.getClass().getMethod("getObject", getArgsClazz);
            Object stream = getObject.invoke(c, args);
            return (InputStream) stream;
        } catch (ClassCastException e) {
            throw new IllegalStateException("MinIO getObject did not return InputStream", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch object from MinIO", e);
        } finally {
            sample.stop(Timer.builder("storage.openStream").tag("provider","s3").register(meterRegistry));
        }
    }

    @Override
    public void save(String storageKey, InputStream data) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            byte[] bytes = data.readAllBytes();
            Object c = client();
            Class<?> putArgsClazz = Class.forName("io.minio.PutObjectArgs");
            Method builderMethod = putArgsClazz.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            Method bucketMethod = builder.getClass().getMethod("bucket", String.class);
            Method objectMethod = builder.getClass().getMethod("object", String.class);
            Method streamMethod = builder.getClass().getMethod("stream", InputStream.class, long.class, long.class);
            Method contentTypeMethod;
            try { contentTypeMethod = builder.getClass().getMethod("contentType", String.class); } catch (NoSuchMethodException ignore) { contentTypeMethod = null; }
            Method buildMethod = builder.getClass().getMethod("build");
            bucketMethod.invoke(builder, bucket);
            objectMethod.invoke(builder, storageKey);
            streamMethod.invoke(builder, new ByteArrayInputStream(bytes), (long) bytes.length, -1L);
            if (contentTypeMethod != null) contentTypeMethod.invoke(builder, "application/octet-stream");
            Object args = buildMethod.invoke(builder);
            Method putObject = c.getClass().getMethod("putObject", putArgsClazz);
            putObject.invoke(c, args);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to put object to MinIO", e);
        } finally {
            sample.stop(Timer.builder("storage.save").tag("provider","s3").register(meterRegistry));
        }
    }
}

