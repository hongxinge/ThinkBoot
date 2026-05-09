package com.thinkboot.storage.domain;

import lombok.Data;

import java.io.InputStream;

@Data
public class StorageResult {

    private String url;

    private String key;

    private String bucket;

    public StorageResult() {
    }

    public StorageResult(String url, String key, String bucket) {
        this.url = url;
        this.key = key;
        this.bucket = bucket;
    }
}