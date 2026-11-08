-- 测试环境 H2 建表脚本（与 init.sql / operation_log.sql 字段保持一致，去掉 MySQL 专有语法）
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    status TINYINT DEFAULT 1,
    created_by VARCHAR(50),
    created_time DATETIME,
    updated_by VARCHAR(50),
    updated_time DATETIME
);

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT NOT NULL PRIMARY KEY,
    title VARCHAR(100),
    description VARCHAR(500),
    business_type VARCHAR(20),
    class_name VARCHAR(200),
    method_name VARCHAR(100),
    request_method VARCHAR(10),
    request_url VARCHAR(500),
    operator VARCHAR(50),
    operator_ip VARCHAR(50),
    operator_location VARCHAR(200),
    request_params VARCHAR(4000),
    response_result VARCHAR(4000),
    status TINYINT DEFAULT 1,
    error_msg VARCHAR(4000),
    cost_time BIGINT DEFAULT 0,
    created_time DATETIME
);

MERGE INTO sys_user (id, username, password, nickname, email, phone, status) KEY(id)
VALUES (1, 'admin', '$2a$10$mMLcvMNGxI8oLypbvvlZT.GWgQCFsH.rPyoVkeJUqmDL40buK28Xa', '管理员', 'admin@thinkboot.com', '13800138000', 1),
       (2, 'user', '$2a$10$mMLcvMNGxI8oLypbvvlZT.GWgQCFsH.rPyoVkeJUqmDL40buK28Xa', '普通用户', 'user@thinkboot.com', '13800138001', 1);
