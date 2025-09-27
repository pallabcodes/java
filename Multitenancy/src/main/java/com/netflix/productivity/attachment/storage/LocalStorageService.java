package com.netflix.productivity.attachment.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@ConditionalOnProperty(prefix = "attachments.storage", name = "provider", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    @Value("${attachments.storage.baseDir:/tmp/attachments}")
    private String baseDir;

    @Override
    public InputStream openStream(String storageKey) {
        try {
            Path path = Path.of(baseDir, storageKey);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("File not found");
            }
            return new FileInputStream(path.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open file", e);
        }
    }

    @Override
    public void save(String storageKey, InputStream data) {
        try {
            Path path = Path.of(baseDir, storageKey);
            Files.createDirectories(path.getParent());
            try (FileOutputStream out = new FileOutputStream(path.toFile())) {
                data.transferTo(out);
                out.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save file", e);
        }
    }
}

