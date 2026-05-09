package com.thinkboot.core.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommonConstantsTest {

    @Test
    void testCharacterEncoding() {
        assertEquals("UTF-8", CommonConstants.UTF8);
    }

    @Test
    void testStatusMessages() {
        assertEquals("success", CommonConstants.SUCCESS);
        assertEquals("fail", CommonConstants.FAIL);
    }

    @Test
    void testStatusCodes() {
        assertEquals(200, CommonConstants.SUCCESS_CODE);
        assertEquals(500, CommonConstants.FAIL_CODE);
        assertEquals(401, CommonConstants.UNAUTHORIZED_CODE);
        assertEquals(403, CommonConstants.FORBIDDEN_CODE);
    }

    @Test
    void testTokenConstants() {
        assertEquals("Authorization", CommonConstants.TOKEN_HEADER);
        assertEquals("Bearer ", CommonConstants.TOKEN_PREFIX);
    }

    @Test
    void testAuditFieldConstants() {
        assertEquals("createdBy", CommonConstants.CREATED_BY);
        assertEquals("updatedBy", CommonConstants.UPDATED_BY);
        assertEquals("createdTime", CommonConstants.CREATED_TIME);
        assertEquals("updatedTime", CommonConstants.UPDATED_TIME);
    }

    @Test
    void testClassCannotBeInstantiated() throws Exception {
        var constructor = CommonConstants.class.getDeclaredConstructor();
        assertFalse(constructor.canAccess(null));
    }
}
