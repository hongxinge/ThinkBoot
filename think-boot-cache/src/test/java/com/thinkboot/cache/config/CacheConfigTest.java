package com.thinkboot.cache.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;

class CacheConfigTest {

    @Test
    void testCacheConfigExists() {
        CacheConfig config = new CacheConfig();
        assertNotNull(config);
    }

    @Test
    void testCacheManagerBeanMethod() throws NoSuchMethodException {
        CacheConfig config = new CacheConfig();
        assertNotNull(CacheConfig.class.getMethod("cacheManager", org.springframework.data.redis.connection.RedisConnectionFactory.class));
    }
}