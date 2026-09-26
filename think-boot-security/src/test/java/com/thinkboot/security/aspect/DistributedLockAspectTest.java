package com.thinkboot.security.aspect;

import com.thinkboot.core.exception.BusinessException;
import com.thinkboot.security.annotation.DistributedLock;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DistributedLockAspectTest {

    private DistributedLockAspect aspect;
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        aspect = new DistributedLockAspect();
        redisTemplate = mock(RedisTemplate.class);
        setRedisTemplateField(aspect, redisTemplate);
    }

    @Test
    @DisplayName("waitTime=0 should still attempt to acquire lock once and proceed on success")
    void waitTimeZeroAttemptsOnceAndSucceeds() throws Throwable {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(1L);

        TestProceedingJoinPoint joinPoint = newJoinPoint();
        joinPoint.proceedResult = "success";

        Object result = aspect.around(joinPoint, getAnnotation(WaitTimeZeroHolder.class));

        assertEquals("success", result);
        assertEquals(1, joinPoint.proceedCount);
        // 加锁一次 + 解锁一次
        verify(redisTemplate, times(2)).execute(any(RedisScript.class), anyList(), any(Object[].class));
    }

    @Test
    @DisplayName("waitTime=0 should attempt once then fail fast when lock not acquired")
    void waitTimeZeroFailsFastAfterOneAttempt() throws Throwable {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(0L);

        TestProceedingJoinPoint joinPoint = newJoinPoint();

        assertThrows(BusinessException.class,
                () -> aspect.around(joinPoint, getAnnotation(WaitTimeZeroHolder.class)));

        assertEquals(0, joinPoint.proceedCount);
        // 修复前 waitTime=0 时不做任何获取尝试直接抛异常；修复后应恰好尝试一次
        verify(redisTemplate, times(1)).execute(any(RedisScript.class), anyList(), any(Object[].class));
    }

    /**
     * 构造带方法签名的 JoinPoint（parseKey 依赖 MethodSignature 解析 SpEL key）
     */
    private TestProceedingJoinPoint newJoinPoint() throws NoSuchMethodException {
        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(WaitTimeZeroHolder.class.getMethod("dummy"));
        when(signature.getParameterNames()).thenReturn(new String[0]);
        joinPoint.signature = signature;
        return joinPoint;
    }

    private static void setRedisTemplateField(DistributedLockAspect target, Object value) {
        try {
            java.lang.reflect.Field field = DistributedLockAspect.class.getDeclaredField("redisTemplate");
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set redisTemplate field", e);
        }
    }

    private DistributedLock getAnnotation(Class<?> holderClass) throws Exception {
        return holderClass.getMethod("dummy").getAnnotation(DistributedLock.class);
    }

    private interface WaitTimeZeroHolder {
        @DistributedLock(key = "'test'", waitTime = 0, leaseTime = 10)
        Object dummy();
    }

    static class TestProceedingJoinPoint implements ProceedingJoinPoint {
        Object proceedResult;
        int proceedCount = 0;
        MethodSignature signature;

        @Override
        public Object proceed() throws Throwable {
            proceedCount++;
            return proceedResult;
        }

        @Override
        public Object proceed(Object[] arguments) throws Throwable {
            proceedCount++;
            return proceedResult;
        }

        @Override public Object getThis() { return null; }
        @Override public Object[] getArgs() { return new Object[0]; }
        @Override public SourceLocation getSourceLocation() { return null; }
        @Override public String toShortString() { return null; }
        @Override public String toLongString() { return null; }
        @Override public MethodSignature getSignature() { return signature; }
        @Override public Object getTarget() { return null; }
        @Override public void set$AroundClosure(org.aspectj.runtime.internal.AroundClosure arc) {}
        @Override public JoinPoint.StaticPart getStaticPart() { return null; }
        @Override public String getKind() { return METHOD_EXECUTION; }
    }
}
