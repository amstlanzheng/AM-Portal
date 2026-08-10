## MODIFIED Requirements

### Requirement: JWT 认证
系统 SHALL 使用 JWT 进行无状态认证，Access Token 短期有效，Refresh Token 长期有效。用户密码被修改或重置后，系统 MUST 使该用户在 Redis 中的 Token 失效，强制重新登录。

#### Scenario: 请求携带有效 Token
- **WHEN** 请求携带有效的 Access Token
- **THEN** 系统解析 Token，获取用户信息，放行请求

#### Scenario: Access Token 过期
- **WHEN** Access Token 过期但 Refresh Token 有效
- **THEN** 系统允许通过 Refresh Token 获取新的 Access Token

#### Scenario: Token 无效或伪造
- **WHEN** 请求携带无效或伪造的 Token
- **THEN** 系统返回 401 Unauthorized

#### Scenario: 密码修改后 Token 失效
- **WHEN** 用户修改自己密码或被管理员修改/重置密码后
- **THEN** 系统删除该用户在 Redis 中的 access/refresh Token，该用户后续请求因 Token 不在 Redis 中而返回 401，需重新登录
