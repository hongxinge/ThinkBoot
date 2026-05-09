package com.thinkboot.storage.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageResultTest {

    @Test
    void testStorageResultDefaultConstructor() {
        StorageResult result = new StorageResult();
        assertNull(result.getUrl());
        assertNull(result.getKey());
        assertNull(result.getBucket());
    }

    @Test
    void testStorageResultParameterizedConstructor() {
        StorageResult result = new StorageResult(
            "https://example.com/file.txt",
            "file.txt",
            "my-bucket"
        );

        assertEquals("https://example.com/file.txt", result.getUrl());
        assertEquals("file.txt", result.getKey());
        assertEquals("my-bucket", result.getBucket());
    }

    @Test
    void testStorageResultSetters() {
        StorageResult result = new StorageResult();
        result.setUrl("https://storage.example.com/image.png");
        result.setKey("images/image.png");
        result.setBucket("images-bucket");

        assertEquals("https://storage.example.com/image.png", result.getUrl());
        assertEquals("images/image.png", result.getKey());
        assertEquals("images-bucket", result.getBucket());
    }

    @Test
    void testStorageResultWithNullValues() {
        StorageResult result = new StorageResult(null, null, null);
        assertNull(result.getUrl());
        assertNull(result.getKey());
        assertNull(result.getBucket());
    }

    @Test
    void testStorageResultEqualsAndHashCode() {
        StorageResult result1 = new StorageResult("url1", "key1", "bucket1");
        StorageResult result2 = new StorageResult("url1", "key1", "bucket1");

        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void testStorageResultToString() {
        StorageResult result = new StorageResult("https://example.com/file", "file.txt", "bucket");
        String toString = result.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("https://example.com/file"));
        assertTrue(toString.contains("file.txt"));
        assertTrue(toString.contains("bucket"));
    }
}
