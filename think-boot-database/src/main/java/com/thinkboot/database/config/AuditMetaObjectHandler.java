package com.thinkboot.database.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.hasSetter("createdTime")) {
            this.setFieldValByName("createdTime", LocalDateTime.now(), metaObject);
        }
        if (metaObject.hasSetter("updatedTime")) {
            this.setFieldValByName("updatedTime", LocalDateTime.now(), metaObject);
        }
        if (metaObject.hasSetter("createdBy")) {
            this.setFieldValByName("createdBy", getCurrentUser(), metaObject);
        }
        if (metaObject.hasSetter("updatedBy")) {
            this.setFieldValByName("updatedBy", getCurrentUser(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasSetter("updatedTime")) {
            this.setFieldValByName("updatedTime", LocalDateTime.now(), metaObject);
        }
        if (metaObject.hasSetter("updatedBy")) {
            this.setFieldValByName("updatedBy", getCurrentUser(), metaObject);
        }
    }

    protected String getCurrentUser() {
        return "system";
    }
}
