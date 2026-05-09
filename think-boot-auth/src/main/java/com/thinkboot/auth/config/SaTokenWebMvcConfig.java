package com.thinkboot.auth.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.thinkboot.auth.annotation.IgnoreAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@ConditionalOnProperty(prefix = "think-boot.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SaTokenWebMvcConfig implements WebMvcConfigurer {

    @Value("${think-boot.auth.exclude-paths:}")
    private String[] configExcludePaths;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ThinkBootSaInterceptor(handle -> {
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

    public static class ThinkBootSaInterceptor extends SaInterceptor {

        public ThinkBootSaInterceptor(cn.dev33.satoken.fun.SaParamFunction<Object> auth) {
            super(auth);
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            if (handler instanceof HandlerMethod handlerMethod) {
                if (handlerMethod.hasMethodAnnotation(IgnoreAuth.class)
                        || handlerMethod.getBeanType().isAnnotationPresent(IgnoreAuth.class)) {
                    return true;
                }
            }
            return super.preHandle(request, response, handler);
        }
    }
}