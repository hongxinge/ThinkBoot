package com.thinkboot.auth.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginUserTest {

    @Test
    void testLoginUserDefaultValues() {
        LoginUser loginUser = new LoginUser();
        assertNull(loginUser.getUserId());
        assertNull(loginUser.getUsername());
        assertNull(loginUser.getNickname());
        assertNotNull(loginUser.getPermissions());
        assertTrue(loginUser.getPermissions().isEmpty());
        assertNotNull(loginUser.getRoles());
        assertTrue(loginUser.getRoles().isEmpty());
    }

    @Test
    void testLoginUserSettersAndGetters() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setUsername("testuser");
        loginUser.setNickname("测试用户");

        assertEquals(1L, loginUser.getUserId());
        assertEquals("testuser", loginUser.getUsername());
        assertEquals("测试用户", loginUser.getNickname());
    }

    @Test
    void testLoginUserWithPermissions() {
        LoginUser loginUser = new LoginUser();
        Set<String> permissions = Set.of("user:view", "user:edit");
        loginUser.setPermissions(permissions);

        assertNotNull(loginUser.getPermissions());
        assertEquals(2, loginUser.getPermissions().size());
        assertTrue(loginUser.getPermissions().contains("user:view"));
    }

    @Test
    void testLoginUserWithRoles() {
        LoginUser loginUser = new LoginUser();
        Set<String> roles = Set.of("admin", "user");
        loginUser.setRoles(roles);

        assertNotNull(loginUser.getRoles());
        assertEquals(2, loginUser.getRoles().size());
        assertTrue(loginUser.getRoles().contains("admin"));
    }

    @Test
    void testLoginUserFullConstruction() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(100L);
        loginUser.setUsername("admin");
        loginUser.setNickname("管理员");
        loginUser.setPermissions(Set.of("system:manage"));
        loginUser.setRoles(Set.of("admin"));

        assertEquals(100L, loginUser.getUserId());
        assertEquals("admin", loginUser.getUsername());
        assertEquals("管理员", loginUser.getNickname());
        assertTrue(loginUser.getPermissions().contains("system:manage"));
        assertTrue(loginUser.getRoles().contains("admin"));
    }

    @Test
    void testLoginUserImplementsSerializable() {
        LoginUser loginUser = new LoginUser();
        assertInstanceOf(java.io.Serializable.class, loginUser);
    }

    @Test
    void testLoginUserEqualsAndHashCode() {
        LoginUser user1 = new LoginUser();
        user1.setUserId(1L);
        user1.setUsername("test");

        LoginUser user2 = new LoginUser();
        user2.setUserId(1L);
        user2.setUsername("test");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testLoginUserToString() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setUsername("testuser");

        String toString = loginUser.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("testuser"));
    }
}
