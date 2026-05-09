package com.thinkboot.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "think-boot.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SaTokenConfigure {
}