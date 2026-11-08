package com.thinkboot.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkboot.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    /**
     * 登录口令（BCrypt 哈希）。面向客户端 API 时不得随响应输出，
     * WRITE_ONLY 保留反序列化写入能力（注册/改密等场景仍可接收），
     * 同时不影响 MyBatis-Plus 对数据库字段的读写。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String nickname;

    private String email;

    private String phone;

    private Integer status;
}