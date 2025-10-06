package com.netflix.attachments.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    @Value("${attachments.storage.s3.endpoint}")
    private String endpoint;
    @Value("${attachments.storage.s3.accessKey}")
    private String accessKey;
    @Value("${attachments.storage.s3.secretKey}")
    private String secretKey;
    @Value("${attachments.storage.s3.bucket}")
    private String bucket;

    private MinioClient client() {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
    }

    @Override
    public void putObject(String storageKey, InputStream data, long size, String contentType) {
        try {
            client().putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(storageKey)
                    .contentType(contentType)
                    .stream(data, size, -1)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload object", e);
        }
    }

    @Override
    public InputStream getObject(String storageKey) {
        try {
            return client().getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(storageKey)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get object", e);
        }
    }

    @Override
    public void deleteObject(String storageKey) {
        try {
            client().removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(storageKey)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete object", e);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        try (InputStream is = getObject(storageKey)) {
            return is != null;
        } catch (Exception e) {
            return false;
        }
    }
}
