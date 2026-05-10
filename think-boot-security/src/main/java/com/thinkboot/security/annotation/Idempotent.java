package com.thinkboot.security.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等性注解
 * 防止接口重复提交，基于 Redis Token 机制实现
 * 
 * 使用示例：
 * <pre>
 * @Idempotent(time = 5, message = "请勿重复提交")
 * @PostMapping("/submit")
 * public R<Void> submit(@RequestBody Order order) {
 *     return R.ok();
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等性校验时间（秒）
     * 在此时间内相同请求被视为重复提交
     */
    int time() default 3;

    /**
     * 提示信息
     */
    String message() default "请勿重复提交";

    /**
     * 是否使用请求头中的 Token 进行校验
     * 如果为 true，需要客户端先请求获取 Token，然后在提交时携带
     * 如果为 false，则基于 URL + 参数 MD5 作为唯一标识
     */
    boolean useToken() default false;
}
