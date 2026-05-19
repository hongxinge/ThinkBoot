-- ThinkBoot 示例数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4

CREATE DATABASE IF NOT EXISTS `thinkboot` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `thinkboot`;

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据 (密码: 123456, BCrypt 加密)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `phone`, `status`) VALUES
(1, 'admin', '$2a$10$mMLcvMNGxI8oLypbvvlZT.GWgQCFsH.rPyoVkeJUqmDL40buK28Xa', '管理员', 'admin@thinkboot.com', '13800138000', 1),
(2, 'user', '$2a$10$mMLcvMNGxI8oLypbvvlZT.GWgQCFsH.rPyoVkeJUqmDL40buK28Xa', '普通用户', 'user@thinkboot.com', '13800138001', 1);

-- 示例业务表
DROP TABLE IF EXISTS `demo_article`;
CREATE TABLE `demo_article` (
    `id` BIGINT NOT NULL COMMENT '文章ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT COMMENT '内容',
    `author_id` BIGINT DEFAULT NULL COMMENT '作者ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0-草稿,1-发布)',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';
