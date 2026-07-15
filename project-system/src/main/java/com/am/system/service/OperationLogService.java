package com.am.system.service;

import com.am.system.entity.OperationLog;
import com.am.system.mapper.OperationLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperationLogService extends ServiceImpl<OperationLogMapper, OperationLog> {

    private final HttpServletRequest request;

    public void log(Long userId, String username, String operationType, String content, String result, Long duration) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperationType(operationType);
        log.setOperationContent(content);
        log.setResult(result);
        log.setIp(getClientIp());
        log.setDuration(duration);
        save(log);
    }

    public void logLogin(Long userId, String username, boolean success) {
        log(userId, username, "LOGIN", "用户登录", success ? "SUCCESS" : "FAIL", null);
    }

    public void logAiCall(Long userId, String username, String input, String result, Long duration) {
        log(userId, username, "AI_CALL", input, result, duration);
    }

    public void logFileUpload(Long userId, String username, String fileName, boolean success) {
        log(userId, username, "FILE_UPLOAD", fileName, success ? "SUCCESS" : "FAIL", null);
    }

    public Page<OperationLog> listLogs(int pageNum, int pageSize, String operationType, Long userId) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
