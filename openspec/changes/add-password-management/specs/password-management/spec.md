## ADDED Requirements

### Requirement: 用户修改自己密码
系统 SHALL 允许已登录用户通过验证旧密码来修改自己的密码，修改成功后当前用户的 Token 失效，需重新登录。

#### Scenario: 修改密码成功
- **WHEN** 用户提交正确的旧密码和新密码（新密码长度 >= 6）
- **THEN** 系统使用 BCrypt 加密新密码并更新数据库，删除该用户在 Redis 中的 access/refresh Token

#### Scenario: 旧密码错误
- **WHEN** 用户提交的旧密码与数据库记录不匹配
- **THEN** 系统返回错误提示"旧密码错误"，密码不更新

#### Scenario: 新密码长度不足
- **WHEN** 用户提交的新密码长度小于 6 位
- **THEN** 系统返回错误提示"密码长度不能少于6位"

#### Scenario: 新旧密码相同
- **WHEN** 用户提交的新密码与旧密码完全相同
- **THEN** 系统返回错误提示"新密码不能与旧密码相同"

### Requirement: 管理员修改用户密码
系统 SHALL 允许管理员直接设置任意用户的新密码（无需旧密码），修改后目标用户的 Token 失效。

#### Scenario: 管理员修改密码成功
- **WHEN** 管理员指定 userId 和新密码（长度 >= 6）
- **THEN** 系统使用 BCrypt 加密新密码并更新数据库，删除目标用户在 Redis 中的 access/refresh Token

#### Scenario: 目标用户不存在
- **WHEN** 管理员指定的 userId 在数据库中不存在
- **THEN** 系统返回错误提示"用户不存在"

#### Scenario: 非管理员调用
- **WHEN** 普通用户调用管理员改密接口
- **THEN** 系统返回 403 Forbidden

### Requirement: 管理员重置用户密码
系统 SHALL 允许管理员将任意用户的密码一键重置为系统初始密码 `33550336`，重置后目标用户的 Token 失效。

#### Scenario: 重置密码成功
- **WHEN** 管理员对指定 userId 执行重置操作
- **THEN** 系统将用户密码更新为 BCrypt 加密的 `33550336`，删除目标用户在 Redis 中的 access/refresh Token

#### Scenario: 目标用户不存在
- **WHEN** 管理员指定的 userId 在数据库中不存在
- **THEN** 系统返回错误提示"用户不存在"

### Requirement: 初始密码常量
系统 SHALL 在常量类中定义初始密码为 `33550336`，管理员重置密码时 MUST 使用该常量。

#### Scenario: 初始密码一致性
- **WHEN** 管理员重置任意用户密码
- **THEN** 重置后的密码均为 `33550336`，用户使用该密码可登录
