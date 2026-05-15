package com.thinkboot.storage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 存储配置
 *
 * 注意：此配置使用 think-boot.storage.minio 前缀，是框架统一存储抽象层的一部分
 * 框架提供统一的存储接口，屏蔽不同存储服务商的配置差异
 */
@Configuration
@ConfigurationProperties(prefix = "think-boot.storage.minio")
@ConditionalOnProperty(prefix = "think-boot.storage", name = "type", havingValue = "minio")
public class MinioConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName = "default";

    @Autowired
    private MinioClient minioClient;

    @Bean(destroyMethod = "close")
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostConstruct
    public void validate() {
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalStateException("MinIO endpoint must be configured");
        }
        if (accessKey == null || accessKey.isEmpty()) {
            throw new IllegalStateException("MinIO accessKey must be configured");
        }
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("MinIO secretKey must be configured");
        }
        try {
            // 复用已创建的 minioClient Bean，避免重复创建实例
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO bucket: " + bucketName, e);
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
