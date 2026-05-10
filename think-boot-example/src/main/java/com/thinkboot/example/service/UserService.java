package com.thinkboot.example.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thinkboot.example.entity.User;
import com.thinkboot.example.mapper.UserMapper;
import com.thinkboot.security.annotation.DistributedLock;
import com.thinkboot.security.annotation.Idempotent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Cacheable(value = "user", key = "#id")
    @Transactional(rollbackFor = Exception.class)
    public User getByIdWithCache(Long id) {
        return getById(id);
    }

    @Idempotent(time = 5, message = "订单提交过于频繁，请稍后再试")
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        save(user);
        return user;
    }

    @DistributedLock(key = "'user:lock:' + #id", leaseTime = 5)
    @Transactional(rollbackFor = Exception.class)
    public User updateUserStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        updateById(user);
        return user;
    }
}