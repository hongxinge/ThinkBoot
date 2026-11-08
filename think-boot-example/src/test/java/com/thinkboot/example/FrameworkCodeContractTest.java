package com.thinkboot.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkboot.example.controller.ClientApiExampleController;
import com.thinkboot.example.entity.User;
import com.thinkboot.security.aspect.DistributedLockAspect;
import com.thinkboot.web.aspect.RateLimitAspect;
import com.thinkboot.web.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 框架代码级契约测试：不依赖 Spring 上下文与外部中间件，
 * 用于把 P0 级缺陷固化为可回归的门禁。
 */
class FrameworkCodeContractTest {

    @Test
    @DisplayName("示例不得提供免密签发 Token 的端点")
    void anonymousTokenEndpointMustNotExist() {
        assertThrows(NoSuchMethodException.class,
                () -> ClientApiExampleController.class.getMethod("login", Long.class, String.class),
                "/api/v1/auth/login 免密端点必须移除");
    }

    @Test
    @DisplayName("分布式锁脚本必须使用 StringRedisTemplate 保证参数为裸字符串")
    void distributedLockScriptMustUseStringRedisTemplate() throws Exception {
        Field field = DistributedLockAspect.class.getDeclaredField("scriptRedisTemplate");
        assertEquals(StringRedisTemplate.class, field.getType());
    }

    @Test
    @DisplayName("限流脚本必须使用 StringRedisTemplate 保证参数为裸字符串")
    void rateLimitScriptMustUseStringRedisTemplate() throws Exception {
        Field field = RateLimitAspect.class.getDeclaredField("scriptRedisTemplate");
        assertEquals(StringRedisTemplate.class, field.getType());
    }

    @Test
    @DisplayName("畸形 JSON 必须有专门的 400 异常处理器")
    void malformedJsonMustBeMappedTo400() throws Exception {
        Method method = GlobalExceptionHandler.class
                .getMethod("handleHttpMessageNotReadable", HttpMessageNotReadableException.class);
        assertNotNull(method);
    }

    @Test
    @DisplayName("用户实体序列化到客户端时不得包含密码哈希")
    void userEntityMustNotExposePassword() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("$2a$10$shouldNeverLeak");
        String json = new ObjectMapper().writeValueAsString(user);
        assertFalse(json.contains("password"),
                "面向客户端的实体不得把 BCrypt 密码哈希输出到响应体");
    }
}
