package com.thinkboot.example.controller;

import com.thinkboot.auth.annotation.IgnoreAuth;
import com.thinkboot.auth.service.AuthService;
import com.thinkboot.web.annotation.OperationLog;
import com.thinkboot.web.annotation.OperationLog.BusinessType;
import com.thinkboot.web.result.R;

import com.thinkboot.example.entity.User;
import com.thinkboot.example.service.UserService;
import com.thinkboot.web.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * C端 API 示例 Controller
 * 展示 ThinkBoot 框架针对客户端 API 设计的最佳实践
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "C端 API 示例", description = "展示面向客户端的 API 设计最佳实践")
public class ClientApiExampleController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @IgnoreAuth
    @PostMapping("/auth/login")
    @Operation(summary = "用户登录", description = "客户端登录接口，返回 Token")
    public R<Map<String, Object>> login(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "用户名") @RequestParam String username) {
        
        String token = authService.login(userId, username);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", userId);
        data.put("username", username);
        
        return R.ok(data);
    }

    @Operation(summary = "获取用户信息", description = "需要携带 Token 访问")
    @GetMapping("/user/{id}")
    public R<User> getUserInfo(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return R.fail(404, "用户不存在");
        }
        return R.ok(user);
    }

    @Operation(summary = "获取用户列表（分页）", description = "支持分页查询，返回 hasMore 用于无限滚动")
    @GetMapping("/user/list")
    public R<PageResult<User>> getUserList(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page = 
            userService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size));
        
        PageResult<User> pageResult = PageResult.of(page.getRecords(), page.getTotal(), current, size);
        return R.ok(pageResult);
    }
}