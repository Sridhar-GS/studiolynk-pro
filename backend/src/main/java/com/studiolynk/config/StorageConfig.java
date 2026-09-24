package com.studiolynk.config;

import com.studiolynk.service.storage.LocalStorageServiceImpl;
import com.studiolynk.service.storage.S3StorageServiceImpl;
import com.studiolynk.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    @Value("${aws.s3.bucket-name:${S3_BUCKET_NAME:}}")
    private String s3BucketName;

    @Value("${aws.region:${AWS_REGION:ap-south-1}}")
    private String awsRegion;

    @Value("${aws.credentials.access-key:${AWS_ACCESS_KEY_ID:}}")
    private String awsAccessKey;

    @Value("${aws.credentials.secret-key:${AWS_SECRET_ACCESS_KEY:}}")
    private String awsSecretKey;

    @Value("${storage.local.dir:${LOCAL_STORAGE_DIR:./uploads}}")
    private String localStorageDir;

    @Value("${storage.local.base-url:${LOCAL_STORAGE_BASE_URL:/api/uploads}}")
    private String localStorageBaseUrl;

    @Bean
    public StorageService storageService() {
        if (s3BucketName != null && !s3BucketName.isBlank()
                && awsAccessKey != null && !awsAccessKey.isBlank()
                && awsSecretKey != null && !awsSecretKey.isBlank()) {

            log.info("Configuring AWS S3 Storage Provider with bucket: '{}', region: '{}'", s3BucketName, awsRegion);
            try {
                S3Client s3Client = S3Client.builder()
                        .region(Region.of(awsRegion))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
                        ))
                        .build();

                return new S3StorageServiceImpl(s3Client, s3BucketName, awsRegion);
            } catch (Exception e) {
                log.warn("Failed to initialize AWS S3 client. Falling back to local storage: {}", e.getMessage());
            }
        }

        log.info("AWS S3 credentials or bucket not configured. Initializing LocalStorageServiceImpl in directory: '{}'", localStorageDir);
        return new LocalStorageServiceImpl(localStorageDir, localStorageBaseUrl);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(localStorageDir).toAbsolutePath().normalize();
        String resourceLocation = "file:" + uploadPath.toString() + "/";
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}
