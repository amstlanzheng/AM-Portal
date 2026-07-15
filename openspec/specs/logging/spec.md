## ADDED Requirements

### Requirement: 登录日志
系统 SHALL 记录用户登录日志，包括用户名、登录时间、登录 IP、登录结果（成功/失败）。

#### Scenario: 记录登录成功日志
- **WHEN** 用户登录成功
- **THEN** 系统记录登录日志，状态为成功

#### Scenario: 记录登录失败日志
- **WHEN** 用户登录失败
- **THEN** 系统记录登录日志，状态为失败，包含失败原因

### Requirement: AI 调用日志
系统 SHALL 记录 AI 接口调用日志，包括调用用户、输入内容、输出内容、调用时间、响应耗时。

#### Scenario: 记录 AI 调用日志
- **WHEN** AI 接口被调用
- **THEN** 系统记录完整的调用日志

### Requirement: 文件上传日志
系统 SHALL 记录文件上传日志，包括上传用户、文件名、文件大小、上传时间、上传结果。

#### Scenario: 记录文件上传日志
- **WHEN** 用户上传文件
- **THEN** 系统记录上传日志，包含文件信息和上传结果

### Requirement: 用户操作日志
系统 SHALL 记录关键用户操作日志，包括操作用户、操作类型、操作内容、操作时间、操作结果。

#### Scenario: 记录管理操作日志
- **WHEN** 管理员执行项目管理、博客管理、用户管理等操作
- **THEN** 系统记录操作日志

### Requirement: 日志存储
系统 SHALL 将日志存储在数据库 operation_log 表中，采用 Spring Boot Logging 框架。

#### Scenario: 日志查询
- **WHEN** 管理员查询操作日志
- **THEN** 系统支持按时间范围、操作类型、用户等条件筛选
