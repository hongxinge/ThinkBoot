package com.thinkboot.auth.service;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.thinkboot.auth.domain.LoginUser;
import com.thinkboot.core.constant.RedisConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "think-boot.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AuthService {

    public String login(Long userId, String username) {
        StpUtil.login(userId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUsername(username);
        StpUtil.getSession().set("loginUser", loginUser);
        
        return tokenInfo.getTokenValue();
    }

    public void logout() {
        StpUtil.logout();
    }

    public LoginUser getLoginUser() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        return (LoginUser) StpUtil.getSession().get("loginUser");
    }

    public Long getUserId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    public String getUsername() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    public boolean isLogin() {
        return StpUtil.isLogin();
    }

    public String getToken() {
        return StpUtil.getTokenValue();
    }

    public String encryptPassword(String password) {
        return SaSecureUtil.sha256(password);
    }
}