package com.thinkboot.example.controller;

import com.thinkboot.example.entity.User;
import com.thinkboot.example.service.UserService;
import com.thinkboot.security.annotation.DistributedLock;
import com.thinkboot.security.annotation.Idempotent;
import com.thinkboot.security.service.IdempotentTokenService;
import com.thinkboot.web.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/demo")
@Tag(name = "企业级特性示例", description = "演示安全、缓存、分布式锁等企业级功能")
public class EnterpriseDemoController {

    @Autowired
    private UserService userService;

    @Autowired
    private IdempotentTokenService idempotentTokenService;

    @GetMapping("/token")
    @Operation(summary = "获取幂等性 Token")
    public R<String> getIdempotentToken() {
        String token = idempotentTokenService.getToken();
        return R.ok(token);
    }

    @PostMapping("/submit")
    @Idempotent(time = 5, message = "请勿重复提交，5秒内只能提交一次")
    @Operation(summary = "幂等性提交示例")
    public R<String> idempotentSubmit(@RequestBody Map<String, Object> data) {
        return R.ok("提交成功：" + data);
    }

    @PostMapping("/submit-with-token")
    @Idempotent(useToken = true, message = "Token已失效，请重新获取")
    @Operation(summary = "幂等性 Token 模式示例")
    public R<String> idempotentSubmitWithToken(
            @RequestHeader("X-Idempotent-Token") String token,
            @RequestBody Map<String, Object> data) {
        return R.ok("提交成功：" + data);
    }

    @PostMapping("/lock")
    @DistributedLock(key = "'inventory:' + #data.productId", waitTime = 2, leaseTime = 10)
    @Operation(summary = "分布式锁示例 - 库存扣减")
    public R<String> deductInventory(@RequestBody Map<String, Object> data) {
        String productId = data.get("productId").toString();
        int quantity = Integer.parseInt(data.get("quantity").toString());
        return R.ok("库存扣减成功：商品ID=" + productId + ", 数量=" + quantity);
    }

    @GetMapping("/cache/{id}")
    @Cacheable(value = "user", key = "#id")
    @Operation(summary = "Spring Cache 示例 - 缓存用户信息")
    public R<User> getCachedUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return R.ok(user);
    }

    @PutMapping("/cache/{id}")
    @CachePut(value = "user", key = "#id")
    @Operation(summary = "Spring Cache 示例 - 更新用户缓存")
    public R<User> updateCachedUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateById(user);
        return R.ok(user);
    }

    @DeleteMapping("/cache/{id}")
    @CacheEvict(value = "user", key = "#id")
    @Operation(summary = "Spring Cache 示例 - 清除用户缓存")
    public R<Void> evictCachedUser(@PathVariable Long id) {
        return R.ok();
    }
}