package com.thinkboot.example;

import com.thinkboot.security.aspect.DistributedLockAspect;
import com.thinkboot.security.aspect.IdempotentAspect;
import com.thinkboot.security.service.IdempotentTokenService;
import com.thinkboot.web.aspect.RateLimitAspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 默认配置下的装配契约：Redis 增强链路的四个组件必须真实注册。
 * 该用例同时验证日志配置可用（@SpringBootTest 会加载 logback-spring.xml）。
 */
@SpringBootTest
@ActiveProfiles("test")
class FrameworkContractTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("默认配置下 Redis 增强链路的四个组件必须注册")
    void redisFeatureBeansMustBeRegistered() {
        assertNotNull(context.getBean(IdempotentTokenService.class));
        assertNotNull(context.getBean(IdempotentAspect.class));
        assertNotNull(context.getBean(DistributedLockAspect.class));
        assertNotNull(context.getBean(RateLimitAspect.class));
    }
}
