package com.thinkboot.storage.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.thinkboot.storage.config.TencentCosConfig;
import com.thinkboot.storage.domain.StorageResult;
import com.thinkboot.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Service
@ConditionalOnProperty(prefix = "think-boot.storage", name = "type", havingValue = "tencent")
public class TencentCosServiceImpl implements StorageService {

    @Autowired
    private TencentCosConfig config;

    @Autowired
    private COSClient cosClient;

    @Override
    public StorageResult upload(String bucket, String key, InputStream inputStream, String contentType) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream must not be null");
        }
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalArgumentException("Bucket must not be null or empty");
        }
        validateKey(key);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, inputStream, metadata);
        cosClient.putObject(putObjectRequest);

        String url = "https://" + bucket + ".cos." + config.getRegion() + ".myqcloud.com/" + key;
        return new StorageResult(url, key, bucket);
    }

    @Override
    public StorageResult upload(String key, InputStream inputStream, String contentType) {
        return upload(config.getBucketName(), key, inputStream, contentType);
    }

    @Override
    public boolean delete(String bucket, String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalArgumentException("Bucket must not be null or empty");
        }
        cosClient.deleteObject(bucket, key);
        return true;
    }

    @Override
    public boolean delete(String key) {
        return delete(config.getBucketName(), key);
    }

    @Override
    public InputStream download(String bucket, String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalArgumentException("Bucket must not be null or empty");
        }
        COSObject cosObject = cosClient.getObject(bucket, key);
        if (cosObject == null) {
            throw new RuntimeException("Object not found: " + key);
        }
        return cosObject.getObjectContent();
    }

    @Override
    public InputStream download(String key) {
        return download(config.getBucketName(), key);
    }

    @Override
    public String getPresignedUrl(String bucket, String key, int expiresInSeconds) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalArgumentException("Bucket must not be null or empty");
        }
        Date expiration = new Date(System.currentTimeMillis() + expiresInSeconds * 1000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key);
        request.setExpiration(expiration);
        URL url = cosClient.generatePresignedUrl(request);
        return url.toString();
    }

    @Override
    public String getPresignedUrl(String key, int expiresInSeconds) {
        return getPresignedUrl(config.getBucketName(), key, expiresInSeconds);
    }

    @Override
    public String getServiceName() {
        return "Tencent COS";
    }

    private void validateKey(String key) {
        if (key.contains("..") || key.startsWith("/") || key.contains("\\")) {
            throw new IllegalArgumentException("Invalid key: path traversal is not allowed");
        }
    }
}
