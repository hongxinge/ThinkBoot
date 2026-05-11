package com.thinkboot.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thinkboot.web.domain.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}