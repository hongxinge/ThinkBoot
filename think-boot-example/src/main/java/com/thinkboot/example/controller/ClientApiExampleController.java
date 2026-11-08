package com.thinkboot.example.controller;

import com.thinkboot.example.entity.User;
import com.thinkboot.example.service.UserService;
import com.thinkboot.web.result.PageResult;
import com.thinkboot.web.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端 API 示例 Controller：展示统一响应与分页用法。
 * 登录统一走 /api/auth/login（校验用户名密码），本类不提供任何免密签发 Token 的入口。
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "C端 API 示例", description = "展示面向客户端的 API 设计最佳实践")
public class ClientApiExampleController {

    @Autowired
    private UserService userService;

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
