package com.thinkboot.storage.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.thinkboot.storage.config.AliyunOssConfig;
import com.thinkboot.storage.domain.StorageResult;
import com.thinkboot.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@ConditionalOnProperty(prefix = "think-boot.storage", name = "type", havingValue = "aliyun")
public class AliyunOssServiceImpl implements StorageService {

    @Autowired
    private AliyunOssConfig config;

    @Autowired
    private OSS ossClient;

    @Override
    public StorageResult upload(String bucket, String key, InputStream inputStream, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        ossClient.putObject(bucket, key, inputStream, metadata);

        String url = "https://" + bucket + "." + config.getEndpoint() + "/" + key;
        return new StorageResult(url, key, bucket);
    }

    @Override
    public StorageResult upload(String key, InputStream inputStream, String contentType) {
        return upload(config.getBucketName(), key, inputStream, contentType);
    }

    @Override
    public boolean delete(String bucket, String key) {
        ossClient.deleteObject(bucket, key);
        return true;
    }

    @Override
    public boolean delete(String key) {
        return delete(config.getBucketName(), key);
    }

    @Override
    public InputStream download(String bucket, String key) {
        return ossClient.getObject(bucket, key).getObjectContent();
    }

    @Override
    public InputStream download(String key) {
        return download(config.getBucketName(), key);
    }

    @Override
    public String getPresignedUrl(String bucket, String key, int expiresInSeconds) {
        java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + expiresInSeconds * 1000L);
        return ossClient.generatePresignedUrl(bucket, key, expiration).toString();
    }

    @Override
    public String getPresignedUrl(String key, int expiresInSeconds) {
        return getPresignedUrl(config.getBucketName(), key, expiresInSeconds);
    }

    @Override
    public String getServiceName() {
        return "Aliyun OSS";
    }
}