package com.thinkboot.auth.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 拦截器配置
 *
 * 注意：所有 Sa-Token 相关配置（token-name、timeout 等）直接使用 sa-token.* 原生配置项
 * 框架仅提供 Sa-Token 没有的增强配置（如 exclude-paths 白名单）
 */
@Configuration
@ConditionalOnClass(SaInterceptor.class)
@ConditionalOnProperty(prefix = "think-boot.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "think-boot.auth")
public class SaTokenWebMvcConfig implements WebMvcConfigurer {

    private List<String> excludePaths = new ArrayList<>();

    private volatile String[] cachedExcludes;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")
                    .notMatch(getAllExcludes())
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }

    private String[] getAllExcludes() {
        if (cachedExcludes != null) {
            return cachedExcludes;
        }

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

        List<String> validPaths = getValidConfigPaths();
        if (validPaths.isEmpty()) {
            cachedExcludes = defaultExcludes;
            return defaultExcludes;
        }

        String[] allExcludes = new String[defaultExcludes.length + validPaths.size()];
        System.arraycopy(defaultExcludes, 0, allExcludes, 0, defaultExcludes.length);
        for (int i = 0; i < validPaths.size(); i++) {
            allExcludes[defaultExcludes.length + i] = validPaths.get(i);
        }
        cachedExcludes = allExcludes;
        return allExcludes;
    }

    private List<String> getValidConfigPaths() {
        if (excludePaths == null || excludePaths.isEmpty()) {
            return new ArrayList<>();
        }
        return excludePaths.stream()
                .filter(p -> p != null && !p.trim().isEmpty())
                .toList();
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
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
                    if (e instanceof NotLoginException) {
                        SaHolder.getResponse().setStatus(401);
                        return SaResult.error().setCode(401).setMsg(getNotLoginMessage((NotLoginException) e));
                    }
                    if (e instanceof NotRoleException) {
                        SaHolder.getResponse().setStatus(403);
                        return SaResult.error().setCode(403).setMsg("缺少角色: " + ((NotRoleException) e).getRole());
                    }
                    if (e instanceof NotPermissionException) {
                        SaHolder.getResponse().setStatus(403);
                        return SaResult.error().setCode(403).setMsg("缺少权限: " + ((NotPermissionException) e).getPermission());
                    }
                    return SaResult.error(e.getMessage());
                })
                .setBeforeAuth(r -> {
                    SaHolder.getResponse()
                            .setServer("ThinkBoot")
                            .setHeader("X-Frame-Options", "SAMEORIGIN")
                            .setHeader("X-XSS-Protection", "1; mode=block")
                            .setHeader("X-Content-Type-Options", "nosniff")
                            .setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                });
    }

    private String getNotLoginMessage(NotLoginException e) {
        switch (e.getType()) {
            case NotLoginException.NOT_TOKEN:
                return "未提供认证令牌";
            case NotLoginException.INVALID_TOKEN:
                return "认证令牌无效";
            case NotLoginException.TOKEN_TIMEOUT:
                return "认证令牌已过期";
            case NotLoginException.BE_REPLACED:
                return "账号已在其他设备登录";
            case NotLoginException.KICK_OUT:
                return "账号已被踢下线";
            default:
                return "未登录，请先登录";
        }
    }
}
