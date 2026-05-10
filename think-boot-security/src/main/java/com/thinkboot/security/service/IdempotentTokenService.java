package com.thinkboot.security.service;

import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 幂等性 Token 服务
 * 用于生成和验证接口幂等性 Token
 * 
 * 使用场景：
 * 前端先调用 get token 接口获取 Token，然后在提交请求时在请求头中携带该 Token
 */
@Service
@ConditionalOnBean(RedisTemplate.class)
public class IdempotentTokenService {

    private static final String IDEMPOTENT_PREFIX = "thinkboot:idempotent:";

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取幂等性 Token
     * @param expireSeconds Token 有效期（秒）
     * @return 幂等性 Token
     */
    public String getToken(int expireSeconds) {
        if (redisTemplate == null) {
            return null;
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
