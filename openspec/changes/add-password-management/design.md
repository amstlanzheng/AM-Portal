## Context

AM Portal 已有完整的用户系统和 JWT 认证体系（BCrypt + Redis Token 存储），但缺少密码管理能力。当前密码修改只能通过数据库直接操作，既不安全也不便捷。本设计基于现有 UserService / AuthService / RedisUtil 基础设施补齐该能力。

## Goals / Non-Goals

**Goals:**
- 用户可自助修改密码（需验证旧密码）
- 管理员可在后台修改任意用户密码（无需旧密码）
- 管理员可一键重置用户密码为初始密码 `33550336`
- 改密后使受影响用户的 Redis Token 失效，强制重新登录
- 复用现有 PasswordEncoder 和 RedisUtil，不引入新依赖

**Non-Goals:**
- 不实现邮箱验证码找回密码（无邮件服务基础设施）
- 不实现密码强度评分系统，仅做最小长度校验
- 不实现密码修改历史记录
- 不实现首次登录强制改密

## Decisions

### 1. 初始密码硬编码为常量
**选择**：在 `CommonConstant` 中定义 `INITIAL_PASSWORD = "33550336"`
**替代方案**：放 application.yml 配置 → 否决，因为初始密码是业务约定，不需要按环境变化
**理由**：简单直接，管理员重置时业务语义清晰

### 2. 管理员改密无需旧密码
**选择**：管理员接口仅传 `userId` 和 `newPassword`，不校验旧密码
**理由**：管理员角色已通过 RBAC 授权，具备用户管理权限；要求旧密码会让管理员重置场景无法进行

### 3. 改密后 Token 失效策略
**选择**：修改密码后删除该用户在 Redis 中的 access token 和 refresh token
**实现**：复用 RedisUtil 删除 `REDIS_TOKEN_PREFIX + "access:" + userId` 和 `REDIS_TOKEN_PREFIX + "refresh:" + userId`
**替代方案**：JWT 黑名单 → 否决，过度设计，Redis 删除即可让 refresh 失效，access 自然过期（2小时内）
**注意**：管理员改/重置他人密码时，同样使目标用户 Token 失效

### 4. 密码校验规则
**选择**：最小长度 6 位，无复杂度要求
**理由**：与现有注册接口保持一致（当前注册无密码强度校验），避免过度约束

### 5. 接口路径设计
**选择**：
- 用户改密：`PUT /api/users/me/password`
- 管理员改密：`PUT /api/users/admin/{userId}/password`
- 管理员重置：`PUT /api/users/admin/{userId}/password/reset`
**理由**：RESTful 风格，与现有 `/api/users/me/profile`、`/api/users/admin/{userId}/approve` 保持一致

## Risks / Trade-offs

- **[风险] Access Token 短期内仍可用** → 改密后 access token 最长 2 小时内仍能使用（直到过期）。Mitigation：业务可接受，2 小时窗口对个人展示平台风险低；如需即时失效可引入 JWT 黑名单（本次不做）
- **[风险] 管理员误重置密码** → Mitigation：前端重置按钮需二次确认
- **[权衡] 无密码强度校验** → 与现有注册逻辑一致，保持简单
