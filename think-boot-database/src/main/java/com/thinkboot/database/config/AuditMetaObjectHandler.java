package com.thinkboot.database.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "createdBy", this::getCurrentUser, String.class);
        this.strictInsertFill(metaObject, "updatedBy", this::getCurrentUser, String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime::now, LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updatedBy", this::getCurrentUser, String.class);
    }

    protected String getCurrentUser() {
        return "system";
    }
}
