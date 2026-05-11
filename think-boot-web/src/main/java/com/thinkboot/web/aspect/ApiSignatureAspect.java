package com.thinkboot.web.aspect;

import com.thinkboot.core.exception.BusinessException;
import com.thinkboot.web.annotation.ApiSignature;
import com.thinkboot.web.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@ConditionalOnBean(RedisTemplate.class)
public class ApiSignatureAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    public ApiSignatureAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(apiSignature)")
    public Object around(ProceedingJoinPoint point, ApiSignature apiSignature) throws Throwable {
        if (!apiSignature.required()) {
            return point.proceed();
        }

        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            throw new BusinessException("无法获取请求");
        }

        String timestamp = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");
        String signature = request.getHeader("X-Signature");

        if (timestamp == null || nonce == null || signature == null) {
            throw new BusinessException("缺少签名参数");
        }

        long timeDiff = Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp));
        if (timeDiff > apiSignature.maxTimeDiff()) {
            throw new BusinessException("请求已过期");
        }

        String nonceKey = "api:nonce:" + nonce;
        Boolean nonceExists = redisTemplate.hasKey(nonceKey);
        if (Boolean.TRUE.equals(nonceExists)) {
            throw new BusinessException("请求已被使用");
        }

        String calculatedSignature = calculateSignature(request, timestamp, nonce);
        if (!calculatedSignature.equals(signature)) {
            throw new BusinessException("签名验证失败");
        }

        redisTemplate.opsForValue().set(nonceKey, "1", apiSignature.maxTimeDiff(), TimeUnit.MILLISECONDS);

        try {
            return point.proceed();
        } catch (Throwable e) {
            log.error("API signature validation failed", e);
            throw e;
        }
    }

    private String calculateSignature(HttpServletRequest request, String timestamp, String nonce) {
        TreeMap<String, String> params = new TreeMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            params.put(entry.getKey(), String.join(",", entry.getValue()));
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append("timestamp=").append(timestamp).append("&nonce=").append(nonce);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new BusinessException("签名计算失败");
        }
    }
}