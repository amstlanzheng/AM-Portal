## Why

当前系统缺少密码管理能力：用户无法自助修改密码，管理员也无法为用户重置密码。用户忘记密码只能通过数据库手动操作，既不安全也不便捷。需要补齐这一基础能力。

## What Changes

- 新增「用户修改自己密码」接口：用户在个人中心通过验证旧密码后设置新密码
- 新增「管理员修改任意用户密码」接口：管理员在后台用户管理中直接设置用户的新密码
- 新增「管理员重置用户密码」接口：管理员一键将用户密码重置为系统初始密码 `33550336`
- 前端个人中心页新增修改密码入口
- 前端后台用户管理页新增「修改密码」和「重置密码」操作按钮
- 密码修改后使当前用户的 Redis Token 失效，强制重新登录（管理员重置/修改他人密码时使目标用户 Token 失效）

## Capabilities

### New Capabilities
- `password-management`: 密码自助修改、管理员改密、管理员重置密码等能力

### Modified Capabilities
- `user-system`: 用户管理后台操作列新增修改密码、重置密码操作
- `auth-rbac`: 密码修改后需要使已有 Token 失效，强制重新登录

## Impact

- **后端**：
  - `project-user`：UserService 新增 changePassword / adminChangePassword / resetPassword 方法
  - `project-user`：UserController 新增 3 个密码管理接口
  - `project-auth`：AuthService 注销 Token 逻辑可复用
  - `project-common`：ResultCode 新增密码相关错误码（旧密码错误、密码强度不足等）
- **前端**：
  - `views/Profile.vue`：新增修改密码弹窗
  - `views/admin/Users.vue`：操作列新增「修改密码」「重置密码」按钮
  - `api/index.ts`：新增 3 个密码管理 API
- **数据库**：无变更
- **依赖**：无新增依赖，复用现有 PasswordEncoder 和 RedisUtil
