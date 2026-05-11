package com.thinkboot.web.aspect;

import com.thinkboot.web.annotation.OperationLog;
import com.thinkboot.web.domain.SysOperationLog;
import com.thinkboot.web.service.SysOperationLogService;
import com.thinkboot.web.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired(required = false)
    private SysOperationLogService logService;

    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Pointcut("@annotation(com.thinkboot.web.annotation.OperationLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        START_TIME.set(System.currentTimeMillis());
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        
        String className = point.getTarget().getClass().getName();
        String methodName = method.getName();
        String description = annotation.description().isEmpty() ? method.getName() : annotation.description();
        String title = annotation.title();
        String businessType = annotation.businessType().name();
        
        HttpServletRequest request = ServletUtils.getRequest();
        String requestMethod = request != null ? request.getMethod() : "";
        String requestUrl = request != null ? request.getRequestURI() : "";
        String requestIp = request != null ? ServletUtils.getClientIp(request) : "";
        Object[] args = point.getArgs();
        
        log.info("[OperationLog] Start - Title: {}, Description: {}, Type: {}, Class: {}, Method: {}, Args: {}",
                title, description, businessType, className, methodName, Arrays.toString(args));

        Object result = null;
        try {
            result = point.proceed();
            Long startTime = START_TIME.get();
            long costTime = startTime != null ? System.currentTimeMillis() - startTime : 0;
            log.info("[OperationLog] Success - Title: {}, Description: {}, Cost: {}ms",
                    title, description, costTime);
            
            saveOperationLog(title, description, businessType, className, methodName, 
                    requestMethod, requestUrl, requestIp, args, result, costTime, null, 1);
            
            return result;
        } catch (Throwable e) {
            Long startTime = START_TIME.get();
            long costTime = startTime != null ? System.currentTimeMillis() - startTime : 0;
            log.error("[OperationLog] Error - Title: {}, Description: {}, Error: {}, Cost: {}ms",
                    title, description, e.getMessage(), costTime, e);
            
            saveOperationLog(title, description, businessType, className, methodName, 
                    requestMethod, requestUrl, requestIp, args, null, costTime, e.getMessage(), 0);
            
            throw e;
        } finally {
            START_TIME.remove();
        }
    }

    private void saveOperationLog(String title, String description, String businessType, 
                                   String className, String methodName, String requestMethod,
                                   String requestUrl, String requestIp, Object[] args, 
                                   Object result, Long costTime, String errorMsg, Integer status) {
        if (logService == null) {
            return;
        }
        
        try {
            SysOperationLog operationLog = new SysOperationLog();
            operationLog.setTitle(title);
            operationLog.setDescription(description);
            operationLog.setBusinessType(businessType);
            operationLog.setClassName(className);
            operationLog.setMethodName(methodName);
            operationLog.setRequestMethod(requestMethod);
            operationLog.setRequestUrl(requestUrl);
            operationLog.setOperatorIp(requestIp);
            String requestParams = Arrays.toString(args);
            if (requestParams != null && requestParams.length() > 2000) {
                requestParams = requestParams.substring(0, 2000);
            }
            operationLog.setRequestParams(requestParams);
            operationLog.setCostTime(costTime);
            operationLog.setStatus(status);
            operationLog.setErrorMsg(errorMsg);
            operationLog.setCreatedTime(LocalDateTime.now());
            
            if (result != null) {
                String resultStr = result.toString();
                if (resultStr.length() > 2000) {
                    resultStr = resultStr.substring(0, 2000) + "...";
                }
                operationLog.setResponseResult(resultStr);
            }
            
            logService.saveLogAsync(operationLog);
        } catch (Exception e) {
            log.error("[OperationLog] Failed to save operation log: {}", e.getMessage(), e);
        }
    }
}
