package com.thinkboot.security.annotation;

import java.lang.annotation.*;

/**
 * 分布式锁注解
 * 基于 Redis SETNX 实现，适用于集群部署场景
 * 
 * 使用示例：
 * <pre>
 * @DistributedLock(key = "'order:' + #orderId", waitTime = 3, leaseTime = 10)
 * @PostMapping("/order/pay")
 * public R<Void> payOrder(@PathVariable Long orderId) {
 *     return R.ok();
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁的 key（支持 SpEL 表达式）
     * 例如："'order:' + #orderId"
     */
    String key();

    /**
     * 等待时间（秒）
     * 获取锁的最大等待时间，超时则抛出异常
     */
    int waitTime() default 3;

    /**
     * 锁自动释放时间（秒）
     * 防止死锁，超过此时间自动释放
     */
    int leaseTime() default 10;

    /**
     * 提示信息
     */
    String message() default "操作过于频繁，请稍后再试";
}
