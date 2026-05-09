package com.thinkboot.web.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RTest {

    @Test
    void testOkWithNoData() {
        R<Void> result = R.ok();
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testOkWithData() {
        String data = "test data";
        R<String> result = R.ok(data);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals("test data", result.getData());
    }

    @Test
    void testFail() {
        R<Void> result = R.fail();
        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testFailWithMessage() {
        R<Void> result = R.fail("自定义错误消息");
        assertEquals(500, result.getCode());
        assertEquals("自定义错误消息", result.getMsg());
    }

    @Test
    void testFailWithCodeAndMessage() {
        R<Void> result = R.fail(400, "参数错误");
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMsg());
    }

    @Test
    void testUnauthorized() {
        R<Void> result = R.unauthorized();
        assertEquals(401, result.getCode());
        assertEquals("未认证，请先登录", result.getMsg());
    }

    @Test
    void testForbidden() {
        R<Void> result = R.forbidden();
        assertEquals(403, result.getCode());
        assertEquals("没有权限", result.getMsg());
    }

    @Test
    void testSetCode() {
        R<Void> result = new R<>();
        result.setCode(999);
        assertEquals(999, result.getCode());
    }

    @Test
    void testSetMsg() {
        R<Void> result = new R<>();
        result.setMsg("测试消息");
        assertEquals("测试消息", result.getMsg());
    }

    @Test
    void testSetData() {
        R<String> result = new R<>();
        result.setData("新数据");
        assertEquals("新数据", result.getData());
    }

    @Test
    void testTimestampIsSet() {
        R<Void> result = R.ok();
        assertTrue(result.getTimestamp() > 0);
    }
}
