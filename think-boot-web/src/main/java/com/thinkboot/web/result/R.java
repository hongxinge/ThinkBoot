package com.thinkboot.web.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thinkboot.core.constant.CommonConstants;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;

    private String msg;

    private T data;

    private long timestamp;

    private String traceId;

    public R() {
        this.timestamp = System.currentTimeMillis();
        this.traceId = MDC.get("traceId");
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> result = new R<>();
        result.setCode(CommonConstants.SUCCESS_CODE);
        result.setMsg(CommonConstants.SUCCESS);
        result.setData(data);
        return result;
    }

    public static <T> R<T> fail() {
        return fail("操作失败");
    }

    public static <T> R<T> fail(String msg) {
        R<T> result = new R<>();
        result.setCode(CommonConstants.FAIL_CODE);
        result.setMsg(msg);
        return result;
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> result = new R<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> R<T> unauthorized() {
        return fail(CommonConstants.UNAUTHORIZED_CODE, "未认证，请先登录");
    }

    public static <T> R<T> forbidden() {
        return fail(CommonConstants.FORBIDDEN_CODE, "没有权限");
    }
}