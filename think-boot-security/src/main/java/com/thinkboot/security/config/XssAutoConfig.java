package com.thinkboot.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import com.thinkboot.security.filter.XssFilter;

/**
 * XSS 防护自动配置
 * 启用后自动过滤请求参数中的 XSS 攻击代码
 */
@Component
@ConditionalOnProperty(prefix = "think-boot.security.xss", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XssAutoConfig {

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setEnabled(true);
        return registration;
    }
}
