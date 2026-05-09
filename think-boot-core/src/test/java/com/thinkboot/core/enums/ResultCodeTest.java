package com.thinkboot.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultCodeTest {

    @Test
    void testSuccessCode() {
        assertEquals(200, ResultCode.SUCCESS.getCode());
        assertEquals("操作成功", ResultCode.SUCCESS.getMessage());
    }

    @Test
    void testFailCode() {
        assertEquals(500, ResultCode.FAIL.getCode());
        assertEquals("操作失败", ResultCode.FAIL.getMessage());
    }

    @Test
    void testUnauthorizedCode() {
        assertEquals(401, ResultCode.UNAUTHORIZED.getCode());
        assertEquals("未认证，请先登录", ResultCode.UNAUTHORIZED.getMessage());
    }

    @Test
    void testForbiddenCode() {
        assertEquals(403, ResultCode.FORBIDDEN.getCode());
        assertEquals("没有权限", ResultCode.FORBIDDEN.getMessage());
    }

    @Test
    void testNotFoundCode() {
        assertEquals(404, ResultCode.NOT_FOUND.getCode());
        assertEquals("资源不存在", ResultCode.NOT_FOUND.getMessage());
    }

    @Test
    void testParamErrorCode() {
        assertEquals(400, ResultCode.PARAM_ERROR.getCode());
        assertEquals("参数错误", ResultCode.PARAM_ERROR.getMessage());
    }

    @Test
    void testServiceErrorCode() {
        assertEquals(503, ResultCode.SERVICE_ERROR.getCode());
        assertEquals("服务异常", ResultCode.SERVICE_ERROR.getMessage());
    }

    @Test
    void testEnumValueOf() {
        ResultCode code = ResultCode.valueOf("SUCCESS");
        assertEquals(ResultCode.SUCCESS, code);
    }

    @Test
    void testEnumValues() {
        ResultCode[] values = ResultCode.values();
        assertEquals(7, values.length);
    }
}
