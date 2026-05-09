package com.thinkboot.example.controller;

import com.thinkboot.auth.domain.LoginUser;
import com.thinkboot.auth.service.AuthService;
import com.thinkboot.example.entity.User;
import com.thinkboot.example.service.UserService;
import com.thinkboot.web.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "认证管理", description = "登录、登出等认证相关接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名和密码登录系统")
    @ApiResponse(responseCode = "200", description = "登录成功，返回 token")
    public R<Map<String, String>> login(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password) {
        User user = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getPassword, authService.encryptPassword(password))
                .one();
        
        if (user == null) {
            return R.fail("用户名或密码错误");
        }
        
        String token = authService.login(user.getId(), user.getUsername());
        
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId().toString());
        result.put("username", user.getUsername());
        
        return R.ok(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出当前登录状态")
    @ApiResponse(responseCode = "200", description = "登出成功")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @GetMapping("/user/info")
    @Operation(summary = "获取当前用户信息")
    public R<LoginUser> getUserInfo() {
        return R.ok(authService.getLoginUser());
    }
}