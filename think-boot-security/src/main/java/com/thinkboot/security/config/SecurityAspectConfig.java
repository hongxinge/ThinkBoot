package com.thinkboot.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.thinkboot.security.aspect.IdempotentAspect;
import com.thinkboot.security.aspect.DistributedLockAspect;

/**
 * 安全切面自动配置
 */
@Configuration
@ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
@Import({IdempotentAspect.class, DistributedLockAspect.class})
public class SecurityAspectConfig {
}
