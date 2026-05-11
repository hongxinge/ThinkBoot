package com.thinkboot.storage.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "think-boot.storage.tencent")
@ConditionalOnProperty(prefix = "think-boot.storage", name = "type", havingValue = "tencent")
public class TencentCosConfig {

    private String secretId;
    private String secretKey;
    private String region;
    private String bucketName = "default";

    @Bean(destroyMethod = "shutdown")
    public COSClient cosClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig config = new ClientConfig(new Region(region));
        return new COSClient(cred, config);
    }

    @PostConstruct
    public void validate() {
        if (secretId == null || secretId.isEmpty()) {
            throw new IllegalStateException("Tencent COS secretId must be configured");
        }
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("Tencent COS secretKey must be configured");
        }
        if (region == null || region.isEmpty()) {
            throw new IllegalStateException("Tencent COS region must be configured");
        }
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
