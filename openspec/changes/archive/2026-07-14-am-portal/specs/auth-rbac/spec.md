## ADDED Requirements

### Requirement: JWT 认证
系统 SHALL 使用 JWT 进行无状态认证，Access Token 短期有效，Refresh Token 长期有效。

#### Scenario: 请求携带有效 Token
- **WHEN** 请求携带有效的 Access Token
- **THEN** 系统解析 Token，获取用户信息，放行请求

#### Scenario: Access Token 过期
- **WHEN** Access Token 过期但 Refresh Token 有效
- **THEN** 系统允许通过 Refresh Token 获取新的 Access Token

#### Scenario: Token 无效或伪造
- **WHEN** 请求携带无效或伪造的 Token
- **THEN** 系统返回 401 Unauthorized

### Requirement: RBAC 权限控制
系统 SHALL 实现基于角色的访问控制（RBAC），定义三种角色：Guest、User、Admin。

#### Scenario: 游客访问公开接口
- **WHEN** 未登录用户访问首页、项目列表、博客列表等公开接口
- **THEN** 系统正常返回数据

#### Scenario: 普通用户访问需认证接口
- **WHEN** 已登录 User 角色用户访问个人信息修改、AI 对话等接口
- **THEN** 系统正常处理请求

#### Scenario: 普通用户访问管理接口
- **WHEN** User 角色用户访问用户审核、项目管理等 Admin 接口
- **THEN** 系统返回 403 Forbidden

#### Scenario: 管理员访问所有接口
- **WHEN** Admin 角色用户访问任意接口
- **THEN** 系统正常处理请求

### Requirement: 接口权限配置
系统 SHALL 通过 Spring Security 配置接口级别的权限控制，所有管理接口 MUST 要求 Admin 角色。

#### Scenario: 接口权限校验
- **WHEN** 请求到达受保护接口
- **THEN** Spring Security Filter Chain 校验用户角色，无权限则返回 403
