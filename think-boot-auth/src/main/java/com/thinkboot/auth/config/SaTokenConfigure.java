package com.thinkboot.auth.config;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import com.thinkboot.auth.annotation.IgnoreAuth;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 配置类
 *
 * 1. 重写注解策略：使 @IgnoreAuth 与 @SaIgnore 等效
 * 2. 支持注解合并：通过 AnnotatedElementUtils 获取合并注解
 * 3. Sa-Token 通过 application.yml 中的 sa-token.* 配置项进行配置
 *
 * 参考文档：https://sa-token.cc
 */
@Component
@ConditionalOnClass(SaInterceptor.class)
@ConditionalOnProperty(prefix = "think-boot.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SaTokenConfigure {

    @PostConstruct
    public void rewriteSaStrategy() {
        // 重写 Sa-Token 的注解处理器，增加注解合并功能，并使 @IgnoreAuth 与 @SaIgnore 等效
        SaAnnotationStrategy.instance.getAnnotation = (element, annotationClass) -> {
            // 如果是 @SaIgnore，先检查原生注解，再检查 @IgnoreAuth
            if (annotationClass == SaIgnore.class) {
                SaIgnore saIgnore = element.getAnnotation(SaIgnore.class);
                if (saIgnore != null) {
                    return saIgnore;
                }
                // 兼容 @IgnoreAuth 注解（已有企业用户在使用）
                IgnoreAuth ignoreAuth = element.getAnnotation(IgnoreAuth.class);
                if (ignoreAuth != null) {
                    // 返回一个代理的 @SaIgnore 注解实例
                    return (SaIgnore) java.lang.reflect.Proxy.newProxyInstance(
                            SaIgnore.class.getClassLoader(),
                            new Class<?>[]{SaIgnore.class},
                            (proxy, method, args) -> {
                                if ("value".equals(method.getName())) {
                                    return ignoreAuth.reason();
                                }
                                return null;
                            }
                    );
                }
            }
            // 默认使用 Spring 的注解合并功能
            return AnnotatedElementUtils.getMergedAnnotation(element, annotationClass);
        };
    }
}
