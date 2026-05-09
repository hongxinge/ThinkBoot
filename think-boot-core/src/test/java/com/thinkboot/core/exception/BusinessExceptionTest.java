package com.thinkboot.core.exception;

import com.thinkboot.core.enums.ResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void testBusinessExceptionWithMessage() {
        BusinessException exception = new BusinessException("自定义错误消息");
        assertEquals("自定义错误消息", exception.getMessage());
        assertEquals(500, exception.getCode());
    }

    @Test
    void testBusinessExceptionWithCodeAndMessage() {
        BusinessException exception = new BusinessException(404, "资源不存在");
        assertEquals("资源不存在", exception.getMessage());
        assertEquals(404, exception.getCode());
    }

    @Test
    void testBusinessExceptionWithResultCode() {
        BusinessException exception = new BusinessException(ResultCode.UNAUTHORIZED);
        assertEquals("未认证，请先登录", exception.getMessage());
        assertEquals(401, exception.getCode());
    }

    @Test
    void testBusinessExceptionWithSuccessResultCode() {
        BusinessException exception = new BusinessException(ResultCode.SUCCESS);
        assertEquals("操作成功", exception.getMessage());
        assertEquals(200, exception.getCode());
    }

    @Test
    void testBusinessExceptionInheritsRuntimeException() {
        BusinessException exception = new BusinessException("测试异常");
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testBusinessExceptionCanBeCaught() {
        try {
            throw new BusinessException("测试抛出异常");
        } catch (BusinessException e) {
            assertEquals("测试抛出异常", e.getMessage());
            assertEquals(500, e.getCode());
        }
    }
}
