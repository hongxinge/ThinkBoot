package com.thinkboot.security.aspect;

import cn.hutool.core.util.IdUtil;
import com.thinkboot.core.exception.BusinessException;
import com.thinkboot.security.annotation.Idempotent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 接口幂等性切面
 * 防止接口重复提交，基于 Redis Token 机制实现
 */
@Aspect
@Component
@ConditionalOnBean(RedisTemplate.class)
public class IdempotentAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotentAspect.class);

    private static final String IDEMPOTENT_PREFIX = "thinkboot:idempotent:";
    private static final String IDEMPOTENT_TOKEN_HEADER = "X-Idempotent-Token";

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint point, Idempotent idempotent) throws Throwable {
        if (redisTemplate == null) {
            log.warn("Idempotent annotation used but Redis is not configured, skipping idempotency check");
            return point.proceed();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("Cannot perform idempotency check outside of web request context");
        }
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader(IDEMPOTENT_TOKEN_HEADER);

        String idempotentKey;
        if (idempotent.useToken()) {
            if (token == null || token.isEmpty()) {
                throw new BusinessException("缺少幂等性 Token，请先请求获取 Token");
            }
            idempotentKey = IDEMPOTENT_PREFIX + token;
            Boolean deleted = redisTemplate.delete(idempotentKey);
            if (Boolean.FALSE.equals(deleted)) {
                log.warn("Idempotent token validation failed for key: {}", idempotentKey);
                throw new BusinessException(idempotent.message());
            }
        } else {
            idempotentKey = buildKeyFromRequest(point);
            Boolean setSuccess = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", idempotent.time(), TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(setSuccess)) {
                log.warn("Duplicate request detected for key: {}", idempotentKey);
                throw new BusinessException(idempotent.message());
            }
        }

        return point.proceed();
    }

    private String buildKeyFromRequest(ProceedingJoinPoint point) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            String method = signature.getMethod().toString();
            String args = Arrays.toString(point.getArgs());
            String input = method + args;

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return IDEMPOTENT_PREFIX + sb.toString();
        } catch (Exception e) {
            return IDEMPOTENT_PREFIX + IdUtil.fastSimpleUUID();
        }
    }
}
