package com.thinkboot.auth.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器配置
 *
 * 注意：所有 Sa-Token 相关配置（token-name、timeout 等）直接使用 sa-token.* 原生配置项
 * 框架仅提供 Sa-Token 没有的增强配置（如 exclude-paths 白名单）
 */
@Configuration
@ConditionalOnClass(SaInterceptor.class)
public class SaTokenWebMvcConfig implements WebMvcConfigurer {

    /**
     * 白名单路径配置（框架增强功能）
     * Sa-Token 原生未提供 YAML 白名单配置，框架封装以方便开发者使用
     *
     * 使用方式：
     * <pre>
     * think-boot:
     *   auth:
     *     exclude-paths:
     *       - /login
     *       - /register
     *       - /api/public/**
     * </pre>
     */
    @Value("${think-boot.auth.exclude-paths:}")
    private String[] configExcludePaths;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 原生拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")
                    .notMatch(getAllExcludes())
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }

    private String[] getAllExcludes() {
        String[] defaultExcludes = {
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

        if (configExcludePaths == null || configExcludePaths.length == 0) {
            return defaultExcludes;
        }

        String[] allExcludes = new String[defaultExcludes.length + configExcludePaths.length];
        System.arraycopy(defaultExcludes, 0, allExcludes, 0, defaultExcludes.length);
        System.arraycopy(configExcludePaths, 0, allExcludes, defaultExcludes.length, configExcludePaths.length);
        return allExcludes;
    }

    /**
     * Sa-Token 全局过滤器：设置安全响应头 + 全局认证异常处理
     * 参考官方示例：https://sa-token.cc
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .setAuth(obj -> {
                })
                .setError(e -> {
                    return SaResult.error(e.getMessage());
                })
                .setBeforeAuth(r -> {
                    // 设置安全响应头
                    SaHolder.getResponse()
                            .setServer("ThinkBoot")
                            .setHeader("X-Frame-Options", "SAMEORIGIN")
                            .setHeader("X-XSS-Protection", "1; mode=block")
                            .setHeader("X-Content-Type-Options", "nosniff")
                            .setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                });
    }
}
