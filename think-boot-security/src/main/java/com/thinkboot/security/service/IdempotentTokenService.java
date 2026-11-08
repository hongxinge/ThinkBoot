package com.thinkboot.security.service;

import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 幂等性 Token 服务：生成一次性令牌，由 {@code IdempotentAspect} 原子消费。
 * 使用场景：前端先调用取 Token 接口，提交时在 X-Idempotent-Token 头中携带。
 */
@Service
@ConditionalOnClass(RedisTemplate.class)
public class IdempotentTokenService {

    private static final String IDEMPOTENT_PREFIX = "thinkboot:idempotent:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取幂等性 Token
     * @param expireSeconds Token 有效期（秒）
     * @return 幂等性 Token
     */
    public String getToken(int expireSeconds) {
        if (expireSeconds <= 0) {
            throw new IllegalArgumentException("expireSeconds must be positive");
        }
        String token = IdUtil.fastSimpleUUID();
        String key = IDEMPOTENT_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", expireSeconds, TimeUnit.SECONDS);
        return token;
    }

    /**
     * 获取幂等性 Token（默认 60 秒有效期）
     * @return 幂等性 Token
     */
    public String getToken() {
        return getToken(60);
    }
}
