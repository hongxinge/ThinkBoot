package com.thinkboot.auth.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(prefix = "think-boot.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SaTokenWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")
                    .notMatch(getExcludes())
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }

    private String[] getExcludes() {
        return new String[]{
                "/login",
                "/logout",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/v3/api-docs.yaml",
                "/swagger-resources/**",
                "/webjars/**",
                "/doc.html",
                "/favicon.ico",
                "/error",
                "/actuator/**"
        };
    }
}