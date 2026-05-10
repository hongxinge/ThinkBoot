package com.thinkboot.security.annotation;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class DistributedLockTest {

    @Test
    void testDefaultValues() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("defaultTest");
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);
        
        assertNotNull(annotation);
        assertEquals("defaultKey", annotation.key());
        assertEquals(3, annotation.waitTime());
        assertEquals(10, annotation.leaseTime());
        assertEquals("操作过于频繁，请稍后再试", annotation.message());
    }

    @Test
    void testCustomValues() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("customTest");
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);
        
        assertNotNull(annotation);
        assertEquals("'custom:key:' + #id", annotation.key());
        assertEquals(5, annotation.waitTime());
        assertEquals(30, annotation.leaseTime());
        assertEquals("Custom lock message", annotation.message());
    }

    static class TestController {
        @DistributedLock(key = "defaultKey")
        public void defaultTest() {}

        @DistributedLock(key = "'custom:key:' + #id", waitTime = 5, leaseTime = 30, message = "Custom lock message")
        public void customTest() {}
    }
}