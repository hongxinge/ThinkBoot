package com.thinkboot.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thinkboot.example.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}