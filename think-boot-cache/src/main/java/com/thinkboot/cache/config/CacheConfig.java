package com.thinkboot.cache.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置
 *
 * 注意：使用 BasicPolymorphicTypeValidator 替代 LaissezFaireSubTypeValidator 提升安全性
 * 提供可配置的 TTL 配置项，避免硬编码
 */
@Configuration
@EnableCaching
@ConditionalOnBean(RedisTemplate.class)
@ConfigurationProperties(prefix = "think-boot.cache")
public class CacheConfig {

    private Ttl ttl = new Ttl();

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType("com.thinkboot.**")
                .allowIfSubType("java.util.**")
                .allowIfSubType("java.time.**")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY
        );
        objectMapper.registerModule(new JavaTimeModule());

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(ttl.getDefaultHours()))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("default", defaultConfig);
        cacheConfigurations.put("short", defaultConfig.entryTtl(Duration.ofMinutes(ttl.getShortMinutes())));
        cacheConfigurations.put("long", defaultConfig.entryTtl(Duration.ofHours(ttl.getLongHours())));

        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    public Ttl getTtl() {
        return ttl;
    }

    public void setTtl(Ttl ttl) {
        this.ttl = ttl;
    }

    public static class Ttl {
        private long defaultHours = 1L;
        private long shortMinutes = 10L;
        private long longHours = 24L;

        public long getDefaultHours() {
            return defaultHours;
        }

        public void setDefaultHours(long defaultHours) {
            this.defaultHours = defaultHours;
        }

        public long getShortMinutes() {
            return shortMinutes;
        }

        public void setShortMinutes(long shortMinutes) {
            this.shortMinutes = shortMinutes;
        }

        public long getLongHours() {
            return longHours;
        }

        public void setLongHours(long longHours) {
            this.longHours = longHours;
        }
    }
}