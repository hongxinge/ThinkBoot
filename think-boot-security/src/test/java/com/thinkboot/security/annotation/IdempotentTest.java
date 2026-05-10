package com.thinkboot.security.annotation;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class IdempotentTest {

    @Test
    void testDefaultValues() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("defaultTest");
        Idempotent annotation = method.getAnnotation(Idempotent.class);
        
        assertNotNull(annotation);
        assertEquals(3, annotation.time());
        assertEquals("请勿重复提交", annotation.message());
        assertFalse(annotation.useToken());
    }

    @Test
    void testCustomValues() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("customTest");
        Idempotent annotation = method.getAnnotation(Idempotent.class);
        
        assertNotNull(annotation);
        assertEquals(10, annotation.time());
        assertEquals("Custom message", annotation.message());
        assertTrue(annotation.useToken());
    }

    static class TestController {
        @Idempotent
        public void defaultTest() {}

        @Idempotent(time = 10, message = "Custom message", useToken = true)
        public void customTest() {}
    }
}