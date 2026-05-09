package com.thinkboot.storage.service;

import com.thinkboot.storage.domain.StorageResult;

import java.io.InputStream;

public interface StorageService {

    StorageResult upload(String bucket, String key, InputStream inputStream, String contentType);

    StorageResult upload(String key, InputStream inputStream, String contentType);

    boolean delete(String bucket, String key);

    boolean delete(String key);

    InputStream download(String bucket, String key);

    InputStream download(String key);

    String getPresignedUrl(String bucket, String key, int expiresInSeconds);

    String getPresignedUrl(String key, int expiresInSeconds);

    String getServiceName();
}