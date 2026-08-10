## 1. 后端常量与错误码

- [x] 1.1 在 `CommonConstant` 中新增 `INITIAL_PASSWORD = "33550336"` 常量
- [x] 1.2 在 `ResultCode` 中新增 `OLD_PASSWORD_ERROR`（旧密码错误）、`PASSWORD_TOO_SHORT`（密码长度不足）、`PASSWORD_SAME`（新旧密码相同）错误码

## 2. 后端 UserService 密码管理方法

- [x] 2.1 在 `UserService` 注入 `PasswordEncoder`
- [x] 2.2 实现 `changePassword(Long userId, String oldPassword, String newPassword)` 方法：校验旧密码、校验新密码长度≥6、校验新旧不同、BCrypt 加密更新、删除 Redis Token
- [x] 2.3 实现 `adminChangePassword(Long userId, String newPassword)` 方法：校验用户存在、校验新密码长度≥6、BCrypt 加密更新、删除 Redis Token
- [x] 2.4 实现 `resetPassword(Long userId)` 方法：校验用户存在、使用 `INITIAL_PASSWORD` 常量 BCrypt 加密更新、删除 Redis Token

## 3. 后端 UserController 接口

- [x] 3.1 新增 `PUT /api/users/me/password` 接口：接收 oldPassword、newPassword，调用 changePassword
- [x] 3.2 新增 `PUT /api/users/admin/{userId}/password` 接口：接收 newPassword，需 ADMIN 角色，调用 adminChangePassword
- [x] 3.3 新增 `PUT /api/users/admin/{userId}/password/reset` 接口：需 ADMIN 角色，调用 resetPassword

## 4. 后端权限配置

- [x] 4.1 在 `SecurityConfig` 确认 `/api/users/admin/**` 路径要求 ADMIN 角色（新增规则覆盖该路径）

## 5. 前端 API 定义

- [x] 5.1 在 `api/index.ts` 新增 `changeMyPassword(data)` 调用 `PUT /api/users/me/password`
- [x] 5.2 在 `api/index.ts` 新增 `adminChangePassword(userId, data)` 调用 `PUT /api/users/admin/{userId}/password`
- [x] 5.3 在 `api/index.ts` 新增 `adminResetPassword(userId)` 调用 `PUT /api/users/admin/{userId}/password/reset`

## 6. 前端个人中心修改密码

- [x] 6.1 在 `Profile.vue` 新增"修改密码"按钮和弹窗表单（旧密码、新密码、确认新密码）
- [x] 6.2 表单校验：新密码长度≥6、两次新密码一致
- [x] 6.3 提交调用 changeMyPassword，成功后提示并清除本地 Token 跳转登录页

## 7. 前端后台用户管理密码操作

- [x] 7.1 在 `Users.vue` 操作列新增"修改密码"按钮，点击弹出密码输入框
- [x] 7.2 在 `Users.vue` 操作列新增"重置密码"按钮，点击二次确认后调用 adminResetPassword
- [x] 7.3 修改密码成功后提示"已修改，该用户需重新登录"
- [x] 7.4 重置密码成功后提示"已重置为初始密码 33550336"
