package com.thinkboot.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    private String description;

    private String businessType;

    private String className;

    private String methodName;

    private String requestMethod;

    private String requestUrl;

    private String operator;

    private String operatorIp;

    private String operatorLocation;

    private String requestParams;

    private String responseResult;

    private Integer status;

    private String errorMsg;

    private Long costTime;

    private LocalDateTime createdTime;
}