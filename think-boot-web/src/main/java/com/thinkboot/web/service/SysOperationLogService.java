package com.thinkboot.web.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thinkboot.web.domain.SysOperationLog;
import com.thinkboot.web.mapper.SysOperationLogMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SysOperationLogService extends ServiceImpl<SysOperationLogMapper, SysOperationLog> {

    @Async
    public void saveLogAsync(SysOperationLog log) {
        save(log);
    }
}