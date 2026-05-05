package com.wms.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.common.Log;
import com.wms.entity.SysLog;
import com.wms.service.ISysLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志AOP切面类
 */
@Aspect
@Component
public class LogAspect {

    @Autowired
    private ISysLogService sysLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(com.wms.common.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long beginTime = System.currentTimeMillis();
        
        // 执行方法
        Object result = joinPoint.proceed();
        
        // 计算执行耗时
        long executionTime = System.currentTimeMillis() - beginTime;
        
        // 保存日志
        saveLog(joinPoint, executionTime);
        
        return result;
    }

    /**
     * 异步保存日志
     */
    private void saveLog(ProceedingJoinPoint joinPoint, long executionTime) {
        try {
            // 获取方法签名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 获取@Log注解
            Log logAnnotation = method.getAnnotation(Log.class);
            if (logAnnotation == null) {
                return;
            }
            
            // 创建日志对象
            SysLog sysLog = new SysLog();
            sysLog.setOperation(logAnnotation.value());
            sysLog.setMethod(method.getDeclaringClass().getName() + "." + method.getName());
            sysLog.setExecutionTime(executionTime);
            sysLog.setCreateTime(LocalDateTime.now());
            
            // 获取请求参数
            try {
                String params = objectMapper.writeValueAsString(joinPoint.getArgs());
                sysLog.setParams(params);
            } catch (Exception e) {
                sysLog.setParams(Arrays.toString(joinPoint.getArgs()));
            }
            
            // 获取IP地址和用户名
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    sysLog.setIp(getIpAddress(request));
                    
                    // 从session中获取用户名（根据实际项目调整）
                    Object username = request.getSession().getAttribute("username");
                    if (username != null) {
                        sysLog.setUsername(username.toString());
                    } else {
                        sysLog.setUsername("anonymous");
                    }
                }
            } catch (Exception e) {
                sysLog.setIp("unknown");
                sysLog.setUsername("unknown");
            }
            
            // 异步保存日志
            new Thread(() -> {
                try {
                    sysLogService.saveLog(sysLog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}