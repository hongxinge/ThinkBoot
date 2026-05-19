package com.thinkboot.web.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.thinkboot.web.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@ConditionalOnClass(name = "cn.dev33.satoken.exception.NotLoginException")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SaTokenExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleNotLogin(NotLoginException e) {
        String message;
        switch (e.getType()) {
            case NotLoginException.NOT_TOKEN:
                message = "未提供认证令牌";
                break;
            case NotLoginException.INVALID_TOKEN:
                message = "认证令牌无效";
                break;
            case NotLoginException.TOKEN_TIMEOUT:
                message = "认证令牌已过期";
                break;
            case NotLoginException.BE_REPLACED:
                message = "账号已在其他设备登录";
                break;
            case NotLoginException.KICK_OUT:
                message = "账号已被踢下线";
                break;
            default:
                message = "未登录，请先登录";
                break;
        }
        log.warn("Auth failed: {} - {}", e.getType(), message);
        return R.fail(401, message);
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleNotRole(NotRoleException e) {
        log.warn("Role denied: {}", e.getRole());
        return R.fail(403, "缺少角色: " + e.getRole());
    }

    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleNotPermission(NotPermissionException e) {
        log.warn("Permission denied: {}", e.getPermission());
        return R.fail(403, "缺少权限: " + e.getPermission());
    }
}
