package com.thinkboot.auth.annotation;

import java.lang.annotation.*;

/**
 * 忽略认证注解
 * 标注在 Controller 类或方法上，表示不需要登录验证
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreAuth {
    /**
     * 忽略原因说明（可选）
     */
    String reason() default "";
}
