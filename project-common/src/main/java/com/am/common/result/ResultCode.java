package com.am.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // 认证相关 1xxx
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    TOKEN_EXPIRED(1401, "Token 已过期"),
    TOKEN_INVALID(1402, "Token 无效"),
    LOGIN_FAILED(1403, "用户名或密码错误"),
    ACCOUNT_PENDING(1404, "账号待审核，请等待管理员审核"),
    ACCOUNT_DISABLED(1405, "账号已被禁用"),
    USERNAME_EXISTS(1406, "用户名已存在"),

    // 参数校验 2xxx
    PARAM_ERROR(2001, "参数校验失败"),
    FILE_TYPE_ERROR(2002, "不支持的文件格式"),
    FILE_SIZE_ERROR(2003, "文件大小超过限制"),

    // 数据相关 3xxx
    DATA_NOT_FOUND(3001, "数据不存在"),
    DATA_EXISTS(3002, "数据已存在"),

    // 业务相关 4xxx
    AI_NOT_AVAILABLE(4001, "AI 助手暂未开放"),
    PROMPT_DEFAULT_CONFLICT(4002, "已存在默认 Prompt"),

    // 密码相关 5xxx
    OLD_PASSWORD_ERROR(5001, "旧密码错误"),
    PASSWORD_TOO_SHORT(5002, "密码长度不能少于6位"),
    PASSWORD_SAME(5003, "新密码不能与旧密码相同");

    private final int code;
    private final String message;
}
