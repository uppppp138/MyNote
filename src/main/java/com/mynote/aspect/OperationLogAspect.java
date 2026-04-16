package com.mynote.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.mynote.annotation.Log;
import com.mynote.domain.po.OperationLog;
import com.mynote.mapper.OperationLogMapper;
import com.mynote.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component//交给spring容器管理
@RequiredArgsConstructor
public class OperationLogAspect {
    private final OperationLogMapper operationLogMapper;
    private final HttpServletRequest request;

    /**
     * 环绕通知：记录操作日志
     */
    @Around("@annotation(com.mynote.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String ip = getIpAddress(request);
        String methodPath = request.getRequestURI();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);
        String operationDesc = logAnnotation != null ? logAnnotation.value() : "";

        String params = getParams(joinPoint.getArgs());

        Long userId = UserContext.getUser();

        Object result = null;
        Integer status = 1;
        String errorMsg = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            saveLogAsync(userId, operationDesc, methodPath, params,
                    JSONUtil.toJsonStr(result), duration, ip, status, errorMsg);
        }


    }
    @Async
    public void saveLogAsync(Long userId, String operation,
                             String method, String params, String result,
                             long duration, String ip, Integer status, String errorMsg) {
        try {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setOperation(operation);
            log.setMethod(method);

            if (StrUtil.isNotBlank(params) && params.length() > 2000) {
                params = params.substring(0, 2000) + "...";
            }
            log.setParams(params);

            if (StrUtil.isNotBlank(result) && result.length() > 2000) {
                result = result.substring(0, 2000) + "...";
            }
            log.setResult(result);

            log.setDurationMs(duration);
            log.setIp(ip);
            log.setStatus(status);
            log.setErrorMsg(errorMsg);
            log.setCreateTime(LocalDateTime.now());

            operationLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }


    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        return JakartaServletUtil.getClientIP(request);
    }

    private String getParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            // ✅ 过滤掉无法序列化的对象
            if (arg == null) {
                continue;
            }
            if (arg instanceof HttpServletRequest ||
                    arg instanceof HttpServletResponse ||
                    arg instanceof MultipartFile) {
                continue;
            }

            try {
                String json = JSONUtil.toJsonStr(arg);
                if (StrUtil.isNotBlank(json)) {
                    json = json.replaceAll("(?<=\"password\":\")(.*?)(?=\")", "******");
                    json = json.replaceAll("(?<=\"oldPassword\":\")(.*?)(?=\")", "******");
                    json = json.replaceAll("(?<=\"newPassword\":\")(.*?)(?=\")", "******");
                    sb.append(json);
                }
            } catch (Exception e) {
                // 序列化失败时跳过该参数
                log.debug("参数序列化失败: {}", e.getMessage());
            }
        }
        String result = sb.toString();
        if (result.length() > 1000) {
            result = result.substring(0, 1000) + ".....";
        }
        return result;
    }


}
