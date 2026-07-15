## ADDED Requirements

### Requirement: 首页信息管理
系统 SHALL 支持管理员在后台修改首页展示的所有信息，包括网站标题、副标题、个人介绍、联系方式、Github、Gitee、邮箱、QQ、微信二维码。

#### Scenario: 管理员修改首页信息
- **WHEN** 管理员在后台修改网站标题为"AM Portal"
- **THEN** 首页展示更新后的标题

#### Scenario: 管理员修改联系方式
- **WHEN** 管理员更新 Github 地址和邮箱
- **THEN** 首页联系方式展示最新信息

### Requirement: 头像管理
系统 SHALL 支持管理员上传和更换首页头像，头像存储至 MinIO。

#### Scenario: 管理员上传头像
- **WHEN** 管理员上传新头像图片
- **THEN** 系统将图片上传至 MinIO，更新系统配置中的头像 URL

#### Scenario: 头像格式校验
- **WHEN** 管理员上传非图片格式文件作为头像
- **THEN** 系统返回错误提示，拒绝上传

### Requirement: 系统配置持久化
系统 SHALL 将首页信息存储在数据库 sys_config 表中，支持通过后台接口动态修改，修改后自动刷新 Redis 缓存。

#### Scenario: 配置修改后缓存刷新
- **WHEN** 管理员修改系统配置
- **THEN** 数据库更新，Redis 缓存自动失效并重新加载
