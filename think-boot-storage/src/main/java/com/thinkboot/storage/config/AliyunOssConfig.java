package com.thinkboot.storage.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 存储配置
 *
 * 注意：此配置使用 think-boot.storage.aliyun 前缀，是框架统一存储抽象层的一部分
 * 框架提供统一的存储接口，屏蔽不同存储服务商的配置差异
 */
@Configuration
@ConfigurationProperties(prefix = "think-boot.storage.aliyun")
@ConditionalOnProperty(prefix = "think-boot.storage", name = "type", havingValue = "aliyun")
public class AliyunOssConfig {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName = "default";

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    @PostConstruct
    public void validate() {
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalStateException("Aliyun OSS endpoint must be configured");
        }
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            throw new IllegalStateException("Aliyun OSS accessKeyId must be configured");
        }
        if (accessKeySecret == null || accessKeySecret.isEmpty()) {
            throw new IllegalStateException("Aliyun OSS accessKeySecret must be configured");
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
