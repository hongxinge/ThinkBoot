package com.thinkboot.web.exception;

/**
 * 限流异常
 *
 * 触发 @RateLimit 限流时抛出，由全局异常处理器映射为 HTTP 429 状态码，
 * 便于网关、监控与前端按标准语义处理限流响应
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
