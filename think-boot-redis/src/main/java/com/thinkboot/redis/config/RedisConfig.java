package com.thinkboot.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置：默认启用，并排在 Spring Boot 原生 RedisAutoConfiguration 之前，
 * 确保同名 redisTemplate 由框架提供（String key + JSON value），消除 bean 注册顺序竞争。
 * 连接参数仍由 spring.data.redis.* 原生配置提供；开发者定义同名 bean 时本类会退让。
 */
@AutoConfiguration(before = RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "think-boot.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 序列化 Key
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 序列化 Value
        // 注意：allowIfSubType 为类名前缀匹配（startsWith 语义），
        // 必须以 "." 结尾，写成通配符形式（如 "com.thinkboot.**"）无法匹配任何类
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                // 声明基类型为 Object 时放行，保证任意业务实体可正常序列化
                .allowIfBaseType(Object.class)
                .allowIfSubType("com.thinkboot.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
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
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
