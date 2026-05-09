package com.thinkboot.web.aspect;

import com.thinkboot.web.annotation.OperationLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Pointcut("@annotation(com.thinkboot.web.annotation.OperationLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        
        String className = point.getTarget().getClass().getName();
        String methodName = method.getName();
        String description = annotation.description().isEmpty() ? method.getName() : annotation.description();
        String title = annotation.title();
        String businessType = annotation.businessType().name();
        Object[] args = point.getArgs();
        
        log.info("[OperationLog] Start - Title: {}, Description: {}, Type: {}, Class: {}, Method: {}, Args: {}",
                title, description, businessType, className, methodName, Arrays.toString(args));

        Object result = null;
        try {
            result = point.proceed();
            long costTime = System.currentTimeMillis() - startTime;
            log.info("[OperationLog] Success - Title: {}, Description: {}, Cost: {}ms",
                    title, description, costTime);
            return result;
        } catch (Throwable e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("[OperationLog] Error - Title: {}, Description: {}, Error: {}, Cost: {}ms",
                    title, description, e.getMessage(), costTime, e);
            throw e;
        }
    }
}