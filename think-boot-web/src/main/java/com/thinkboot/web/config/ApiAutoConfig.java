package com.thinkboot.web.config;

import com.thinkboot.web.aspect.ApiSignatureAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class ApiAutoConfig {

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public ApiSignatureAspect apiSignatureAspect(RedisTemplate<String, Object> redisTemplate) {
        return new ApiSignatureAspect(redisTemplate);
    }
}