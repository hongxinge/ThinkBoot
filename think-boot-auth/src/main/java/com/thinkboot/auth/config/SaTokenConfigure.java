package com.thinkboot.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 配置类
 * 
 * Sa-Token 通过 application.yml 中的 sa-token.* 配置项进行配置
 * 如需使用 Redis 存储，请确保已引入 sa-token-redis-jackson 依赖
 * 并配置 spring.data.redis.* 相关参数
 * 
 * 参考文档：https://sa-token.cc
 */
@Configuration
@ConditionalOnProperty(prefix = "think-boot.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SaTokenConfigure {
}