package com.thinkboot.web.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    String title() default "";

    String description() default "";

    BusinessType businessType() default BusinessType.OTHER;

    enum BusinessType {
        INSERT,
        UPDATE,
        DELETE,
        QUERY,
        IMPORT,
        EXPORT,
        LOGIN,
        LOGOUT,
        OTHER
    }
}