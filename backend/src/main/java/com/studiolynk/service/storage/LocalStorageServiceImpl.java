package com.studiolynk.service.storage;

import com.studiolynk.exception.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Local filesystem implementation of StorageService used for offline/local development and testing.
 */
public class LocalStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageServiceImpl.class);

    private final Path baseStoragePath;
    private final String baseUrlPrefix;

    public LocalStorageServiceImpl(String baseStorageDir, String baseUrlPrefix) {
        this.baseStoragePath = Paths.get(baseStorageDir).toAbsolutePath().normalize();
        this.baseUrlPrefix = baseUrlPrefix.endsWith("/") ? baseUrlPrefix : baseUrlPrefix + "/";

        try {
            Files.createDirectories(this.baseStoragePath);
            log.info("Initialized LocalStorageServiceImpl at: {}", this.baseStoragePath);
        } catch (IOException e) {
            throw new InternalServerException("Could not initialize local storage directory: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            Path targetFile = baseStoragePath.resolve(key).normalize();

            // Prevent path traversal
            if (!targetFile.startsWith(baseStoragePath)) {
                throw new SecurityException("Cannot store file outside current storage directory.");
            }

            Files.createDirectories(targetFile.getParent());
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file locally: {}", targetFile);
            return getFileUrl(key);
        } catch (IOException e) {
            log.error("Failed to store file locally for key={}", key, e);
            throw new InternalServerException("Failed to store file locally: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            Path targetFile = baseStoragePath.resolve(key).normalize();
            if (targetFile.startsWith(baseStoragePath)) {
                Files.deleteIfExists(targetFile);
                log.info("Deleted local file: {}", targetFile);
            }
        } catch (IOException e) {
            log.warn("Failed to delete local file for key={}", key, e);
        }
    }

    @Override
    public String getFileUrl(String key) {
        // Strip leading slash if any
        String cleanKey = key.startsWith("/") ? key.substring(1) : key;
        return baseUrlPrefix + cleanKey;
    }

    public Path getBaseStoragePath() {
        return baseStoragePath;
    }
}
