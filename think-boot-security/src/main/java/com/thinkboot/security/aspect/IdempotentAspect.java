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
                throw new BusinessException("Missing idempotent token, please request token first");
            }
            idempotentKey = IDEMPOTENT_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(idempotentKey);
            if (Boolean.FALSE.equals(exists)) {
                log.warn("Idempotent token validation failed for key: {}", idempotentKey);
                throw new BusinessException(idempotent.message());
            }
        } else {
            idempotentKey = buildKeyFromRequest(point, request);
            Boolean setSuccess = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", idempotent.time(), TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(setSuccess)) {
                log.warn("Duplicate request detected for key: {}", idempotentKey);
                throw new BusinessException(idempotent.message());
            }
        }

        try {
            return point.proceed();
        } finally {
            if (idempotent.useToken()) {
                redisTemplate.delete(idempotentKey);
            }
        }
    }

    private String buildKeyFromRequest(ProceedingJoinPoint point, HttpServletRequest request) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            String httpMethod = request.getMethod();
            String uri = request.getRequestURI();
            String args = (point.getArgs() != null) ? Arrays.toString(point.getArgs()) : "";
            String input = httpMethod + ":" + uri + args;

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
