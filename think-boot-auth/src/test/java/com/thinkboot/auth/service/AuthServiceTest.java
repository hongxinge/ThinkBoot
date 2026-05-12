package com.thinkboot.auth.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import com.thinkboot.auth.domain.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private TestableAuthService authService;

    @BeforeEach
    void setUp() {
        authService = new TestableAuthService();
    }

    @Test
    @DisplayName("Login with valid parameters should return token")
    void loginWithValidParameters() {
        SaTokenInfo tokenInfo = new SaTokenInfo();
        tokenInfo.setTokenValue("test-token-123");
        authService.setTokenInfo(tokenInfo);

        String token = authService.login(1L, "testuser");

        assertNotNull(token);
        assertEquals("test-token-123", token);
        assertEquals(1L, authService.getLoginUserId());
        assertEquals("testuser", authService.getLoginUsername());
        assertEquals("loginUser", authService.getSessionKey());
    }

    @Test
    @DisplayName("Login with null userId should throw IllegalArgumentException")
    void loginWithNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(null, "testuser");
        });
    }

    @Test
    @DisplayName("Login with null userId should have correct exception message")
    void loginWithNullUserIdMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(null, "testuser");
        });
        assertEquals("userId cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Logout should call saLogout")
    void logout() {
        authService.logout();

        assertTrue(authService.isLogoutCalled());
    }

    @Test
    @DisplayName("getLoginUser should return null when not logged in")
    void getLoginUserWhenNotLoggedIn() {
        authService.setLogin(false);

        assertNull(authService.getLoginUser());
    }

    @Test
    @DisplayName("getUserId should return null when not logged in")
    void getUserIdWhenNotLoggedIn() {
        authService.setLogin(false);

        assertNull(authService.getUserId());
    }

    @Test
    @DisplayName("getUsername should return null when not logged in")
    void getUsernameWhenNotLoggedIn() {
        authService.setLogin(false);

        assertNull(authService.getUsername());
    }

    @Test
    @DisplayName("getToken should return null when not logged in")
    void getTokenWhenNotLoggedIn() {
        authService.setLogin(false);

        assertNull(authService.getToken());
    }

    @Test
    @DisplayName("isLogin should return false when not logged in")
    void isLoginWhenNotLoggedIn() {
        authService.setLogin(false);

        assertFalse(authService.isLogin());
    }

    @Test
    @DisplayName("encryptPassword should return BCrypt hash different from raw password")
    void encryptPassword() {
        String rawPassword = "mySecretPassword123";

        String hashedPassword = authService.encryptPassword(rawPassword);

        assertNotNull(hashedPassword);
        assertNotEquals(rawPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$"));
    }

    @Test
    @DisplayName("encryptPassword should produce different hashes for same password")
    void encryptPasswordProducesDifferentHashes() {
        String password = "samePassword";

        String hash1 = authService.encryptPassword(password);
        String hash2 = authService.encryptPassword(password);

        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("checkPassword should return true for matching password")
    void checkPasswordWithCorrectPassword() {
        String rawPassword = "correctPassword123";
        String hashedPassword = authService.encryptPassword(rawPassword);

        boolean result = authService.checkPassword(rawPassword, hashedPassword);

        assertTrue(result);
    }

    @Test
    @DisplayName("checkPassword should return false for incorrect password")
    void checkPasswordWithIncorrectPassword() {
        String rawPassword = "correctPassword123";
        String hashedPassword = authService.encryptPassword(rawPassword);

        boolean result = authService.checkPassword("wrongPassword", hashedPassword);

        assertFalse(result);
    }

    @Test
    @DisplayName("encryptPassword and checkPassword roundtrip should work")
    void passwordRoundtrip() {
        String[] testPasswords = {"simple", "With123Numbers", "!@#$%^&*()", "very-long-password-with-many-characters-12345"};

        for (String password : testPasswords) {
            String hashed = authService.encryptPassword(password);
            assertTrue(authService.checkPassword(password, hashed),
                    "Password check should pass for: " + password);
        }
    }

    @Test
    @DisplayName("checkPassword should return false for null raw password")
    void checkPasswordWithNullInputs() {
        assertFalse(authService.checkPassword(null, "hashed"));
    }

    private static class TestableAuthService extends AuthService {
        private Long loginUserId;
        private SaTokenInfo tokenInfo;
        private boolean isLogin = true;
        private boolean logoutCalled = false;
        private final SaSession testSession = new SaSession("test-session");

        public void setTokenInfo(SaTokenInfo tokenInfo) {
            this.tokenInfo = tokenInfo;
        }

        public void setLogin(boolean isLogin) {
            this.isLogin = isLogin;
        }

        @Override
        protected void saLogin(Long userId) {
            loginUserId = userId;
        }

        @Override
        protected SaTokenInfo saGetTokenInfo() {
            return tokenInfo;
        }

        @Override
        protected SaSession saGetSession() {
            return testSession;
        }

        @Override
        protected boolean saIsLogin() {
            return isLogin;
        }

        @Override
        protected String saGetTokenValue() {
            return tokenInfo != null ? tokenInfo.getTokenValue() : null;
        }

        @Override
        protected void saLogout() {
            logoutCalled = true;
        }

        public Long getLoginUserId() {
            return loginUserId;
        }

        public String getLoginUsername() {
            LoginUser user = (LoginUser) testSession.get("loginUser");
            return user != null ? user.getUsername() : null;
        }

        public String getSessionKey() {
            return "loginUser";
        }

        public boolean isLogoutCalled() {
            return logoutCalled;
        }
    }
}
