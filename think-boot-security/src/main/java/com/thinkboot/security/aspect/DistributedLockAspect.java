package com.thinkboot.security.aspect;

import com.thinkboot.core.exception.BusinessException;
import com.thinkboot.security.annotation.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面
 * 基于 Redis SETNX 实现，适用于集群部署场景
 */
@Aspect
@Component
@ConditionalOnBean(RedisTemplate.class)
public class DistributedLockAspect {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);

    private static final String LOCK_PREFIX = "thinkboot:lock:";

    private static final String LOCK_LUA_SCRIPT =
            "if redis.call('set', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2]) then " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    private static final String UNLOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    private static final DefaultRedisScript<Long> LOCK_SCRIPT = new DefaultRedisScript<>(LOCK_LUA_SCRIPT, Long.class);
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(UNLOCK_LUA_SCRIPT, Long.class);
    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint point, DistributedLock distributedLock) throws Throwable {
        if (redisTemplate == null) {
            log.warn("DistributedLock annotation used but Redis is not configured, skipping lock check");
            return point.proceed();
        }

        String lockKey = LOCK_PREFIX + parseKey(point, distributedLock.key());
        String lockValue = Thread.currentThread().getId() + "-" + System.currentTimeMillis();
        int waitTime = distributedLock.waitTime();
        int leaseTime = distributedLock.leaseTime();

        boolean acquired = false;
        long endTime = System.currentTimeMillis() + waitTime * 1000L;

        while (System.currentTimeMillis() < endTime) {
            Boolean result = tryAcquireLock(lockKey, lockValue, leaseTime);
            if (Boolean.TRUE.equals(result)) {
                acquired = true;
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("Lock acquisition interrupted");
            }
        }

        if (!acquired) {
            throw new BusinessException(distributedLock.message());
        }

        try {
            return point.proceed();
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    private Boolean tryAcquireLock(String lockKey, String lockValue, int leaseTime) {
        Long result = redisTemplate.execute(
                LOCK_SCRIPT,
                Collections.singletonList(lockKey),
                lockValue,
                String.valueOf(leaseTime)
        );
        return result != null && result == 1;
    }

    private void releaseLock(String lockKey, String lockValue) {
        try {
            redisTemplate.execute(
                    UNLOCK_SCRIPT,
                    Collections.singletonList(lockKey),
                    lockValue
            );
        } catch (Exception e) {
            log.error("Failed to release distributed lock: {}", lockKey, e);
        }
    }

    private String parseKey(ProceedingJoinPoint point, String keyExpression) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = signature.getParameterNames();
        Object[] args = point.getArgs();

        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        String result = SPEL_PARSER.parseExpression(keyExpression).getValue(context, String.class);
        if (result == null) {
            throw new BusinessException("Distributed lock key expression evaluated to null: " + keyExpression);
        }
        return result;
    }
}
