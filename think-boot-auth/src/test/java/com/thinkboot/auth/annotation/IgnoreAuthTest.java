package com.thinkboot.auth.annotation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class IgnoreAuthTest {

    @IgnoreAuth(reason = "测试方法")
    public void testMethodWithIgnoreAuth() {
    }

    @IgnoreAuth
    public void testMethodWithIgnoreAuthNoReason() {
    }

    @Test
    void testIgnoreAuthAnnotationPresent() throws NoSuchMethodException {
        Method method = IgnoreAuthTest.class.getMethod("testMethodWithIgnoreAuth");
        assertTrue(method.isAnnotationPresent(IgnoreAuth.class));
    }

    @Test
    void testIgnoreAuthWithReason() throws NoSuchMethodException {
        Method method = IgnoreAuthTest.class.getMethod("testMethodWithIgnoreAuth");
        IgnoreAuth annotation = method.getAnnotation(IgnoreAuth.class);
        assertEquals("测试方法", annotation.reason());
    }

    @Test
    void testIgnoreAuthDefaultReason() throws NoSuchMethodException {
        Method method = IgnoreAuthTest.class.getMethod("testMethodWithIgnoreAuthNoReason");
        IgnoreAuth annotation = method.getAnnotation(IgnoreAuth.class);
        assertNotNull(annotation);
        assertEquals("", annotation.reason());
    }

    @Test
    void testIgnoreAuthRetention() {
        Annotation[] annotations = IgnoreAuth.class.getAnnotations();
        assertTrue(annotations.length > 0);
    }

    @Test
    void testIgnoreAuthTargetTypes() {
        Target target = IgnoreAuth.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertEquals(2, target.value().length);
        assertTrue(java.util.Arrays.asList(target.value()).contains(java.lang.annotation.ElementType.METHOD));
        assertTrue(java.util.Arrays.asList(target.value()).contains(java.lang.annotation.ElementType.TYPE));
    }
}
