package com.thinkboot.web.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiSignature {
    
    long maxTimeDiff() default 300000;
    
    boolean required() default true;
    
    String message() default "签名验证失败";
}