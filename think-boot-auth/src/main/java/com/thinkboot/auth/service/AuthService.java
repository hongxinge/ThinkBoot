package com.thinkboot.auth.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.thinkboot.auth.domain.LoginUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "think-boot.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AuthService {

    public String login(Long userId, String username) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        saLogin(userId);
        SaTokenInfo tokenInfo = saGetTokenInfo();

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUsername(username);
        saGetSession().set("loginUser", loginUser);

        return tokenInfo.getTokenValue();
    }

    public void logout() {
        saLogout();
    }

    public LoginUser getLoginUser() {
        if (!saIsLogin()) {
            return null;
        }
        return (LoginUser) saGetSession().get("loginUser");
    }

    public Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    public String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    public boolean isLogin() {
        return saIsLogin();
    }

    public String getToken() {
        if (!saIsLogin()) {
            return null;
        }
        return saGetTokenValue();
    }

    public String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    protected void saLogin(Long userId) {
        StpUtil.login(userId);
    }

    protected SaTokenInfo saGetTokenInfo() {
        return StpUtil.getTokenInfo();
    }

    protected SaSession saGetSession() {
        return StpUtil.getSession();
    }

    protected boolean saIsLogin() {
        return StpUtil.isLogin();
    }

    protected String saGetTokenValue() {
        return StpUtil.getTokenValue();
    }

    protected void saLogout() {
        StpUtil.logout();
    }
}
