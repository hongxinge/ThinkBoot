package com.thinkboot.web.aspect;

import com.thinkboot.core.constant.CommonConstants;
import com.thinkboot.web.annotation.RateLimit;
import com.thinkboot.web.exception.RateLimitException;
import com.thinkboot.web.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

/**
 * 接口限流切面。
 * Lua 脚本统一通过 StringRedisTemplate 执行，确保 KEYS/ARGV 为裸字符串，
 * 避免 JSON 序列化器把数字参数包装后导致 Redis 脚本报错。
 */
@Aspect
@Component
@ConditionalOnClass(StringRedisTemplate.class)
public class RateLimitAspect {

    /** 脚本执行专用模板：String 序列化，参数为裸字符串。 */
    @Autowired
    private StringRedisTemplate scriptRedisTemplate;

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

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>(RATE_LIMIT_LUA_SCRIPT, Long.class);

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = buildKey(rateLimit);
        int limit = rateLimit.count();
        int window = rateLimit.time();

        Long result = scriptRedisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(limit),
                String.valueOf(window)
        );

        if (result != null && result == 0) {
            throw new RateLimitException(rateLimit.message());
        }
        return point.proceed();
    }

    private String buildKey(RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder(RATE_LIMIT_KEY_PREFIX);

        if (!rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key()).append(":");
        } else {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                keyBuilder.append(attributes.getRequest().getRequestURI()).append(":");
            }
        }

        switch (rateLimit.limitType()) {
            case IP:
                ServletRequestAttributes ipAttributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
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

    /**
     * 用户维度：优先取业务系统写入的 loginUserId，其次用 token 摘要，
     * 避免所有登录用户共用一个限流桶。不记录原始 token。
     */
    private String getCurrentUserId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "anonymous";
        }
        HttpServletRequest request = attributes.getRequest();
        Object userId = request.getAttribute("loginUserId");
        if (userId != null) {
            return userId.toString();
        }
        String token = request.getHeader(CommonConstants.TOKEN_HEADER);
        if (token != null && !token.isBlank()) {
            return Integer.toHexString(token.hashCode());
        }
        return "anonymous";
    }
}
