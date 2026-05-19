package com.thinkboot.web.aspect;

import com.thinkboot.core.constant.CommonConstants;
import com.thinkboot.core.exception.BusinessException;
import com.thinkboot.web.annotation.RateLimit;
import com.thinkboot.web.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;

@Slf4j
@Aspect
@Component
@ConditionalOnBean(RedisTemplate.class)
public class RateLimitAspect {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    private static final String RATE_LIMIT_LUA_SCRIPT =
            "local key = KEYS[1]\n" +
            "local limit = tonumber(ARGV[1])\n" +
            "local window = tonumber(ARGV[2])\n" +
            "local current = redis.call('INCR', key)\n" +
            "if current == 1 then\n" +
            "    redis.call('EXPIRE', key, window)\n" +
            "end\n" +
            "if current > limit then\n" +
            "    return 0\n" +
            "else\n" +
            "    return 1\n" +
            "end";

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(RATE_LIMIT_LUA_SCRIPT, Long.class);

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        if (redisTemplate == null) {
            log.warn("RateLimit annotation used but Redis is not configured, skipping rate limit check");
            return point.proceed();
        }

        String key = buildKey(rateLimit);
        int limit = rateLimit.count();
        int window = rateLimit.time();

        Long result = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(limit),
                String.valueOf(window)
        );

        if (result != null && result == 0) {
            throw new BusinessException(rateLimit.message());
        }

        return point.proceed();
    }

    private String buildKey(RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder(RATE_LIMIT_KEY_PREFIX);

        if (!rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key()).append(":");
        } else {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                keyBuilder.append(request.getRequestURI()).append(":");
            }
        }

        switch (rateLimit.limitType()) {
            case IP:
                ServletRequestAttributes ipAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (ipAttributes != null) {
                    keyBuilder.append(ServletUtils.getClientIp(ipAttributes.getRequest()));
                }
                break;
            case USER:
                keyBuilder.append("user:").append(getCurrentUserId());
                break;
            default:
                keyBuilder.append("default");
        }

        return keyBuilder.toString();
    }

    private String getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                Object userId = attributes.getRequest().getAttribute("loginUserId");
                if (userId != null) {
                    return userId.toString();
                }
            }
        } catch (Exception ignored) {
        }
        return "anonymous";
    }
}
