package com.thinkboot.security.aspect;

import com.thinkboot.core.exception.BusinessException;
import com.thinkboot.security.annotation.Idempotent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.SourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IdempotentAspectTest {

    private IdempotentAspect aspect;
    private MockHttpServletRequest request;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        aspect = new IdempotentAspect();
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), any(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        setRedisTemplateField(aspect, redisTemplate);
        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/submit");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    @DisplayName("Idempotent annotation default values should be time=3, useToken=false")
    void annotationDefaultValues() throws Exception {
        Idempotent annotation = getAnnotation(IdempotentAnnotationHolder.class);

        assertNotNull(annotation);
        assertEquals(3, annotation.time());
        assertEquals("请勿重复提交", annotation.message());
        assertFalse(annotation.useToken());
    }

    @Test
    @DisplayName("Custom annotation values should override defaults")
    void annotationCustomValues() throws Exception {
        Idempotent annotation = getAnnotation(CustomIdempotentAnnotationHolder.class);

        assertNotNull(annotation);
        assertEquals(5, annotation.time());
        assertEquals("请勿重复提交订单", annotation.message());
        assertFalse(annotation.useToken());
    }

    @Test
    @DisplayName("buildKeyFromRequest should produce consistent key for same request")
    void buildKeyConsistentForSameRequest() throws Exception {
        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        joinPoint.args = new Object[]{"arg1", "arg2"};

        String key1 = invokeBuildKey(joinPoint, request);
        String key2 = invokeBuildKey(joinPoint, request);

        assertNotNull(key1);
        assertEquals(key1, key2);
        assertTrue(key1.startsWith("thinkboot:idempotent:"));
    }

    @Test
    @DisplayName("buildKeyFromRequest should produce different keys for different URIs")
    void buildKeyDifferentForDifferentUris() throws Exception {
        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        joinPoint.args = new Object[]{"arg1"};

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setMethod("POST");
        request2.setRequestURI("/api/different");

        String key1 = invokeBuildKey(joinPoint, request);
        String key2 = invokeBuildKey(joinPoint, request2);

        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("buildKeyFromRequest should produce different keys for different HTTP methods")
    void buildKeyDifferentForDifferentMethods() throws Exception {
        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        joinPoint.args = new Object[]{"arg1"};

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setMethod("GET");
        request2.setRequestURI("/api/submit");

        String key1 = invokeBuildKey(joinPoint, request);
        String key2 = invokeBuildKey(joinPoint, request2);

        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("buildKeyFromRequest should produce different keys for different arguments")
    void buildKeyDifferentForDifferentArgs() throws Exception {
        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();

        joinPoint.args = new Object[]{"arg1", "arg2"};
        String key1 = invokeBuildKey(joinPoint, request);

        joinPoint.args = new Object[]{"arg3", "arg4"};
        String key2 = invokeBuildKey(joinPoint, request);

        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("buildKeyFromRequest should handle null args gracefully")
    void buildKeyWithNullArgs() throws Exception {
        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        joinPoint.args = null;

        String key = invokeBuildKey(joinPoint, request);

        assertNotNull(key);
        assertTrue(key.startsWith("thinkboot:idempotent:"));
    }

    @Test
    @DisplayName("buildKeyFromRequest should handle empty args gracefully")
    void buildKeyWithEmptyArgs() throws Exception {
        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        joinPoint.args = new Object[0];

        String key = invokeBuildKey(joinPoint, request);

        assertNotNull(key);
        assertTrue(key.startsWith("thinkboot:idempotent:"));
    }

    @Test
    @DisplayName("buildKeyFromRequest should include HTTP method and URI in key generation")
    void buildKeyIncludesMethodAndUri() throws Exception {
        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        joinPoint.args = new Object[]{};

        MockHttpServletRequest getReq = new MockHttpServletRequest();
        getReq.setMethod("GET");
        getReq.setRequestURI("/api/users/123");

        String key1 = invokeBuildKey(joinPoint, getReq);

        MockHttpServletRequest postReq = new MockHttpServletRequest();
        postReq.setMethod("POST");
        postReq.setRequestURI("/api/users");

        String key2 = invokeBuildKey(joinPoint, postReq);

        assertNotEquals(key1, key2);
        assertTrue(key1.startsWith("thinkboot:idempotent:"));
        assertTrue(key2.startsWith("thinkboot:idempotent:"));
    }

    @Test
    @DisplayName("Idempotent prefix should be 'thinkboot:idempotent:'")
    void idempotentPrefix() throws Exception {
        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        joinPoint.args = new Object[]{};

        String key = invokeBuildKey(joinPoint, request);

        assertTrue(key.startsWith("thinkboot:idempotent:"));
        String hash = key.substring("thinkboot:idempotent:".length());
        assertEquals(32, hash.length());
    }

    @Test
    @DisplayName("around should release idempotent key when business fails (non-token mode)")
    void aroundReleasesKeyWhenBusinessFails() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), any(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        setRedisTemplateField(aspect, redisTemplate);

        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        joinPoint.proceedException = new IllegalStateException("business error");

        assertThrows(IllegalStateException.class,
                () -> aspect.around(joinPoint, getAnnotation(IdempotentAnnotationHolder.class)));

        // 修复前：业务异常后 key 不释放，需等待 time 秒过期才能重试
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("around should keep idempotent key when business succeeds (non-token mode)")
    void aroundKeepsKeyWhenBusinessSucceeds() throws Throwable {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), any(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        setRedisTemplateField(aspect, redisTemplate);

        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        joinPoint.proceedResult = "test-result";

        Object result = aspect.around(joinPoint, getAnnotation(IdempotentAnnotationHolder.class));

        assertEquals("test-result", result);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("Token 模式应原子消费令牌并放行一次")
    @SuppressWarnings("unchecked")
    void tokenModeConsumesTokenAtomically() throws Throwable {
        request.addHeader("X-Idempotent-Token", "T-1");
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        setRedisTemplateField(aspect, redisTemplate);

        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();
        joinPoint.proceedResult = "ok";

        Object result = aspect.around(joinPoint, getAnnotation(IdempotentWithTokenHolder.class));

        assertEquals("ok", result);
        assertEquals(1, joinPoint.proceedCount);
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("Token 已被消费时应拒绝重复提交")
    @SuppressWarnings("unchecked")
    void tokenModeRejectsConsumedToken() throws Throwable {
        request.addHeader("X-Idempotent-Token", "T-1");
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        when(redisTemplate.delete(anyString())).thenReturn(false);
        setRedisTemplateField(aspect, redisTemplate);

        TestProceedingJoinPoint joinPoint = new TestProceedingJoinPoint();

        assertThrows(BusinessException.class,
                () -> aspect.around(joinPoint, getAnnotation(IdempotentWithTokenHolder.class)));
        assertEquals(0, joinPoint.proceedCount);
    }

    @SuppressWarnings("unchecked")
    private void setRedisTemplateField(IdempotentAspect target, Object value) {
        try {
            java.lang.reflect.Field field = IdempotentAspect.class.getDeclaredField("redisTemplate");
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set redisTemplate field", e);
        }
    }

    private String invokeBuildKey(ProceedingJoinPoint point, MockHttpServletRequest req) throws Exception {
        java.lang.reflect.Method method = IdempotentAspect.class
                .getDeclaredMethod("buildKeyFromRequest", ProceedingJoinPoint.class, jakarta.servlet.http.HttpServletRequest.class);
        method.setAccessible(true);
        return (String) method.invoke(aspect, point, req);
    }

    private static interface IdempotentAnnotationHolder {
        @Idempotent
        Object dummy();
    }

    private static interface CustomIdempotentAnnotationHolder {
        @Idempotent(time = 5, message = "请勿重复提交订单")
        Object dummy();
    }

    private static interface IdempotentWithTokenHolder {
        @Idempotent(useToken = true)
        Object dummy();
    }

    private Idempotent getAnnotation(Class<?> holderClass) throws Exception {
        return holderClass.getMethod("dummy").getAnnotation(Idempotent.class);
    }

    static class TestProceedingJoinPoint implements ProceedingJoinPoint {
        Object proceedResult;
        Throwable proceedException;
        int proceedCount = 0;
        Object[] args;

        @Override
        public Object proceed() throws Throwable {
            proceedCount++;
            if (proceedException != null) {
                throw proceedException;
            }
            return proceedResult;
        }

        @Override
        public Object proceed(Object[] arguments) throws Throwable {
            proceedCount++;
            return proceedResult;
        }

        @Override public Object getThis() { return null; }
        @Override public Object[] getArgs() { return args; }
        @Override public SourceLocation getSourceLocation() { return null; }
        @Override public String toShortString() { return null; }
        @Override public String toLongString() { return null; }
        @Override public org.aspectj.lang.Signature getSignature() { return null; }
        @Override public Object getTarget() { return null; }
        @Override public void set$AroundClosure(org.aspectj.runtime.internal.AroundClosure arc) {}
        @Override public JoinPoint.StaticPart getStaticPart() { return null; }
        @Override public String getKind() { return METHOD_EXECUTION; }
    }
}
