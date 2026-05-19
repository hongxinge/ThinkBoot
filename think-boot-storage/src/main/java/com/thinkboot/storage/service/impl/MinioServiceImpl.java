package com.thinkboot.storage.service.impl;

import com.thinkboot.storage.config.MinioConfig;
import com.thinkboot.storage.domain.StorageResult;
import com.thinkboot.storage.service.StorageService;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(prefix = "think-boot.storage", name = "type", havingValue = "minio")
public class MinioServiceImpl implements StorageService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

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

        try {
            long contentLength = inputStream.available();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(inputStream, contentLength, 10485760)
                            .contentType(contentType)
                            .build()
            );
            inputStream.close();

            String url = minioConfig.getEndpoint() + "/" + bucket + "/" + key;
            return new StorageResult(url, key, bucket);
        } catch (Exception e) {
            throw new RuntimeException("MinIO upload failed: " + key, e);
        }
    }

    @Override
    public StorageResult upload(String key, InputStream inputStream, String contentType) {
        return upload(minioConfig.getBucketName(), key, inputStream, contentType);
    }

    @Override
    public boolean delete(String bucket, String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalArgumentException("Bucket must not be null or empty");
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
            return true;
        } catch (Exception e) {
            throw new RuntimeException("MinIO delete failed: " + key, e);
        }
    }

    @Override
    public boolean delete(String key) {
        return delete(minioConfig.getBucketName(), key);
    }

    @Override
    public InputStream download(String bucket, String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalArgumentException("Bucket must not be null or empty");
        }
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO download failed: " + key, e);
        }
    }

    @Override
    public InputStream download(String key) {
        return download(minioConfig.getBucketName(), key);
    }

    @Override
    public String getPresignedUrl(String bucket, String key, int expiresInSeconds) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalArgumentException("Bucket must not be null or empty");
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(key)
                            .expiry(expiresInSeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO get presigned url failed: " + key, e);
        }
    }

    @Override
    public String getPresignedUrl(String key, int expiresInSeconds) {
        return getPresignedUrl(minioConfig.getBucketName(), key, expiresInSeconds);
    }

    @Override
    public String getServiceName() {
        return "MinIO";
    }

    private void validateKey(String key) {
        if (key.contains("..") || key.startsWith("/") || key.contains("\\")) {
            throw new IllegalArgumentException("Invalid key: path traversal is not allowed");
        }
    }
}
