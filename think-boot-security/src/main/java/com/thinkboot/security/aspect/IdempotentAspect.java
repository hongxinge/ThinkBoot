package com.thinkboot.security.aspect;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkboot.core.constant.CommonConstants;
import com.thinkboot.core.exception.BusinessException;
import com.thinkboot.security.annotation.Idempotent;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性切面。
 * 自动模式使用参数 JSON 摘要 + 用户维度生成 key；Token 模式使用原子 delete 消费令牌，
 * 避免 hasKey/delete 非原子导致的并发穿透。
 */
@Aspect
@Component
@ConditionalOnClass(RedisTemplate.class)
public class IdempotentAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotentAspect.class);

    private static final String IDEMPOTENT_PREFIX = "thinkboot:idempotent:";
    private static final String IDEMPOTENT_TOKEN_HEADER = "X-Idempotent-Token";
    private static final ObjectMapper ARG_MAPPER = new ObjectMapper();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint point, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("Cannot perform idempotency check outside of web request context");
        }
        HttpServletRequest request = attributes.getRequest();
        if (idempotent.time() <= 0) {
            throw new BusinessException("Idempotent.time must be positive");
        }

        String idempotentKey;
        if (idempotent.useToken()) {
            String token = request.getHeader(IDEMPOTENT_TOKEN_HEADER);
            if (token == null || token.isEmpty()) {
                throw new BusinessException("Missing idempotent token, please request token first");
            }
            idempotentKey = IDEMPOTENT_PREFIX + token;
            // 原子消费：只有一个并发请求能删除成功，杜绝 hasKey + delete 的竞态窗口
            Boolean consumed = redisTemplate.delete(idempotentKey);
            if (!Boolean.TRUE.equals(consumed)) {
                log.warn("Idempotent token already consumed or expired: {}", idempotentKey);
                throw new BusinessException(idempotent.message());
            }
        } else {
            idempotentKey = buildKeyFromRequest(point, request);
            Boolean setSuccess = redisTemplate.opsForValue()
                    .setIfAbsent(idempotentKey, "1", idempotent.time(), TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(setSuccess)) {
                log.warn("Duplicate request detected for key: {}", idempotentKey);
                throw new BusinessException(idempotent.message());
            }
        }

        try {
            return point.proceed();
        } catch (Throwable e) {
            if (!idempotent.useToken()) {
                // 业务失败时释放标记，允许客户端立即重试
                redisTemplate.delete(idempotentKey);
            }
            throw e;
        }
    }

    private String buildKeyFromRequest(ProceedingJoinPoint point, HttpServletRequest request) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            String httpMethod = request.getMethod();
            String uri = request.getRequestURI();
            String args = serializeArgs(point.getArgs());
            String userScope = resolveUserScope(request);
            String input = httpMethod + ":" + uri + ":" + userScope + ":" + args;

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return IDEMPOTENT_PREFIX + sb.toString();
        } catch (Exception e) {
            log.warn("Failed to build stable idempotency key, falling back to random key", e);
            return IDEMPOTENT_PREFIX + IdUtil.fastSimpleUUID();
        }
    }

    /** 用 JSON 摘要代替对象 toString，保证相同业务参数得到稳定 key。 */
    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        try {
            return ARG_MAPPER.writeValueAsString(args);
        } catch (Exception e) {
            return Arrays.toString(args);
        }
    }

    /** 隔离不同调用方，避免跨用户相同参数被误判为重复提交。 */
    private String resolveUserScope(HttpServletRequest request) {
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
