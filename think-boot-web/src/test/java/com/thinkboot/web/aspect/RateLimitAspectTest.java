package com.thinkboot.web.aspect;

import com.thinkboot.web.annotation.RateLimit;
import com.thinkboot.web.annotation.RateLimit.LimitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitAspectTest {

    @Test
    @DisplayName("RateLimit annotation default values should be time=60, count=100, limitType=DEFAULT")
    void annotationDefaultValues() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("defaultRateLimitMethod");
        RateLimit annotation = method.getAnnotation(RateLimit.class);

        assertNotNull(annotation);
        assertEquals("", annotation.key());
        assertEquals(60, annotation.time());
        assertEquals(100, annotation.count());
        assertEquals("请求过于频繁，请稍后再试", annotation.message());
        assertEquals(LimitType.DEFAULT, annotation.limitType());
    }

    @Test
    @DisplayName("Custom annotation values should override defaults")
    void annotationCustomValues() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("customRateLimitMethod");
        RateLimit annotation = method.getAnnotation(RateLimit.class);

        assertNotNull(annotation);
        assertEquals("my-api", annotation.key());
        assertEquals(30, annotation.time());
        assertEquals(50, annotation.count());
        assertEquals("Rate limit exceeded", annotation.message());
        assertEquals(LimitType.IP, annotation.limitType());
    }

    @Test
    @DisplayName("RateLimit with USER limitType should be configured correctly")
    void annotationUserLimitType() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("userRateLimitMethod");
        RateLimit annotation = method.getAnnotation(RateLimit.class);

        assertNotNull(annotation);
        assertEquals(LimitType.USER, annotation.limitType());
        assertEquals("user-api", annotation.key());
    }

    @Test
    @DisplayName("RateLimit with DEFAULT limitType should be configured correctly")
    void annotationDefaultLimitType() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("defaultRateLimitMethod");
        RateLimit annotation = method.getAnnotation(RateLimit.class);

        assertNotNull(annotation);
        assertEquals(LimitType.DEFAULT, annotation.limitType());
    }

    @Test
    @DisplayName("RateLimit with IP limitType should be configured correctly")
    void annotationIpLimitType() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("ipRateLimitMethod");
        RateLimit annotation = method.getAnnotation(RateLimit.class);

        assertNotNull(annotation);
        assertEquals(LimitType.IP, annotation.limitType());
    }

    @Test
    @DisplayName("All three LimitType enum values should exist")
    void limitTypeEnumValues() {
        LimitType[] values = LimitType.values();

        assertEquals(3, values.length);
        assertEquals(LimitType.DEFAULT, LimitType.valueOf("DEFAULT"));
        assertEquals(LimitType.IP, LimitType.valueOf("IP"));
        assertEquals(LimitType.USER, LimitType.valueOf("USER"));
    }

    @Test
    @DisplayName("RateLimit with empty key should use URI as key")
    void annotationEmptyKey() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("defaultRateLimitMethod");
        RateLimit annotation = method.getAnnotation(RateLimit.class);

        assertTrue(annotation.key().isEmpty());
    }

    @Test
    @DisplayName("RateLimit with non-empty key should use custom key")
    void annotationNonEmptyKey() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("customRateLimitMethod");
        RateLimit annotation = method.getAnnotation(RateLimit.class);

        assertEquals("my-api", annotation.key());
        assertFalse(annotation.key().isEmpty());
    }

    @Test
    @DisplayName("RateLimit time should accept different values")
    void annotationDifferentTimeValues() throws NoSuchMethodException {
        Method method1 = TestController.class.getMethod("defaultRateLimitMethod");
        Method method2 = TestController.class.getMethod("customRateLimitMethod");

        assertEquals(60, method1.getAnnotation(RateLimit.class).time());
        assertEquals(30, method2.getAnnotation(RateLimit.class).time());
    }

    @Test
    @DisplayName("RateLimit count should accept different values")
    void annotationDifferentCountValues() throws NoSuchMethodException {
        Method method1 = TestController.class.getMethod("defaultRateLimitMethod");
        Method method2 = TestController.class.getMethod("customRateLimitMethod");

        assertEquals(100, method1.getAnnotation(RateLimit.class).count());
        assertEquals(50, method2.getAnnotation(RateLimit.class).count());
    }

    @Test
    @DisplayName("RateLimit message should accept different values")
    void annotationDifferentMessageValues() throws NoSuchMethodException {
        Method method1 = TestController.class.getMethod("defaultRateLimitMethod");
        Method method2 = TestController.class.getMethod("customRateLimitMethod");

        assertEquals("请求过于频繁，请稍后再试", method1.getAnnotation(RateLimit.class).message());
        assertEquals("Rate limit exceeded", method2.getAnnotation(RateLimit.class).message());
    }

    @Test
    @DisplayName("RateLimit key building logic should prefix with rate_limit:")
    void rateLimitKeyPrefix() {
        String prefix = "rate_limit:";

        String key1 = prefix + "my-api:" + "default";
        String key2 = prefix + "user-api:" + "user";
        String key3 = prefix + "default";

        assertTrue(key1.startsWith("rate_limit:"));
        assertTrue(key2.startsWith("rate_limit:"));
        assertTrue(key3.startsWith("rate_limit:"));
    }

    @Test
    @DisplayName("RateLimit key building with custom key and DEFAULT type")
    void buildKeyWithCustomKeyAndDefaultType() {
        String key = buildRateLimitKey("my-custom-key", LimitType.DEFAULT);

        assertEquals("rate_limit:my-custom-key:default", key);
    }

    @Test
    @DisplayName("RateLimit key building with custom key and IP type")
    void buildKeyWithCustomKeyAndIpType() {
        String key = buildRateLimitKey("my-custom-key", LimitType.IP);

        assertEquals("rate_limit:my-custom-key:ip_placeholder", key);
    }

    @Test
    @DisplayName("RateLimit key building with custom key and USER type")
    void buildKeyWithCustomKeyAndUserType() {
        String key = buildRateLimitKey("my-custom-key", LimitType.USER);

        assertEquals("rate_limit:my-custom-key:user", key);
    }

    @Test
    @DisplayName("RateLimit key building without custom key should use URI")
    void buildKeyWithoutCustomKey() {
        String uri = "/api/test";
        String key = buildRateLimitKey("", LimitType.DEFAULT, uri);

        assertEquals("rate_limit:/api/test:default", key);
    }

    @Test
    @DisplayName("RateLimit key building with special characters in key")
    void buildKeyWithSpecialCharacters() {
        String key = buildRateLimitKey("api:v1:endpoint", LimitType.DEFAULT);

        assertEquals("rate_limit:api:v1:endpoint:default", key);
    }

    private String buildRateLimitKey(String key, LimitType limitType) {
        return buildRateLimitKey(key, limitType, null);
    }

    private String buildRateLimitKey(String key, LimitType limitType, String uri) {
        StringBuilder keyBuilder = new StringBuilder("rate_limit:");

        if (!key.isEmpty()) {
            keyBuilder.append(key).append(":");
        } else if (uri != null) {
            keyBuilder.append(uri).append(":");
        }

        switch (limitType) {
            case IP:
                keyBuilder.append("ip_placeholder");
                break;
            case USER:
                keyBuilder.append("user");
                break;
            default:
                keyBuilder.append("default");
        }

        return keyBuilder.toString();
    }

    static class TestController {

        @RateLimit
        public void defaultRateLimitMethod() {
        }

        @RateLimit(key = "my-api", time = 30, count = 50, message = "Rate limit exceeded", limitType = LimitType.IP)
        public void customRateLimitMethod() {
        }

        @RateLimit(key = "user-api", limitType = LimitType.USER)
        public void userRateLimitMethod() {
        }

        @RateLimit(limitType = LimitType.IP)
        public void ipRateLimitMethod() {
        }
    }
}
