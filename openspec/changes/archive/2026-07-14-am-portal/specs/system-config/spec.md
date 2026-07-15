## ADDED Requirements

### Requirement: 多环境配置管理
系统 SHALL 采用 Spring Profile 多环境配置，包括 application.yml（公共）、application-local.yml、application-dev.yml、application-prod.yml，所有配置禁止硬编码。

#### Scenario: 本地开发环境
- **WHEN** 应用以 local profile 启动
- **THEN** 加载 application-local.yml 配置，连接本地 MySQL、Redis、MinIO

#### Scenario: 生产环境
- **WHEN** 应用以 prod profile 启动
- **THEN** 加载 application-prod.yml 配置，连接生产环境服务

### Requirement: 配置项统一管理
系统 SHALL 将 MySQL、Redis、MinIO、DeepSeek、Spring AI、JWT、文件大小限制、上传目录等配置统一在配置文件中管理。

#### Scenario: 修改配置
- **WHEN** 修改配置文件中的某项配置
- **THEN** 重启后新配置生效

### Requirement: 系统配置动态管理
系统 SHALL 支持通过数据库 sys_config 表动态管理系统配置（如首页信息、AI 开放状态等），修改后无需重启。

#### Scenario: 动态修改系统配置
- **WHEN** 管理员通过后台接口修改系统配置
- **THEN** 配置立即生效，Redis 缓存同步刷新

#### Scenario: 读取系统配置
- **WHEN** 系统需要获取某项配置
- **THEN** 优先从 Redis 缓存读取，缓存不存在时从数据库加载并缓存
