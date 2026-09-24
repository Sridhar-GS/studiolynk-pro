package com.studiolynk.service.storage;

import com.studiolynk.exception.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

/**
 * AWS S3 Implementation of StorageService.
 */
public class S3StorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageServiceImpl.class);

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    public S3StorageServiceImpl(S3Client s3Client, String bucketName, String region) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.region = region;
    }

    @Override
    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
            String url = getFileUrl(key);
            log.info("Successfully uploaded file to AWS S3: bucket={}, key={}", bucketName, key);
            return url;
        } catch (Exception e) {
            log.error("Failed to upload file to AWS S3: bucket={}, key={}", bucketName, key, e);
            throw new InternalServerException("Failed to upload file to AWS S3: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Successfully deleted file from AWS S3: bucket={}, key={}", bucketName, key);
        } catch (Exception e) {
            log.warn("Failed to delete file from AWS S3 (may not exist): key={}", key, e);
        }
    }

    @Override
    public String getFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
}
