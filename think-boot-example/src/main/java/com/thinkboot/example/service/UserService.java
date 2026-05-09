package com.thinkboot.example.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thinkboot.example.entity.User;
import com.thinkboot.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
}