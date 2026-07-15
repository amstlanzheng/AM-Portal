## ADDED Requirements

### Requirement: AI 聊天对话
系统 SHALL 在首页提供聊天窗口，通过 Spring AI 调用 DeepSeek API 实现对话功能。

#### Scenario: 用户发送消息
- **WHEN** 用户在聊天窗口输入消息并发送
- **THEN** 系统调用 DeepSeek API，流式返回 AI 回复

#### Scenario: AI 助手开放控制
- **WHEN** 管理员配置 AI 助手为不开放
- **THEN** 游客无法使用 AI 助手，仅登录用户可用

### Requirement: 多轮对话与上下文记忆
系统 SHALL 支持多轮对话，通过 Redis 存储对话上下文，保持会话连贯性。

#### Scenario: 多轮对话
- **WHEN** 用户连续发送多条消息
- **THEN** AI 回复时考虑之前的对话内容

#### Scenario: 上下文长度限制
- **WHEN** 对话轮数超过最大限制
- **THEN** 系统截断最早的对话内容，保留最近的上下文

#### Scenario: 新建对话
- **WHEN** 用户点击新建对话
- **THEN** 系统清除当前对话上下文，开始新的会话

### Requirement: 系统 Prompt 动态配置
系统 SHALL 支持管理员在后台修改 AI 系统提示词，修改后无需重启服务即可生效。

#### Scenario: 管理员修改系统 Prompt
- **WHEN** 管理员在后台修改系统提示词
- **THEN** 新的对话使用更新后的系统提示词

#### Scenario: 默认系统 Prompt
- **WHEN** 管理员未配置自定义系统 Prompt
- **THEN** 系统使用默认系统提示词

### Requirement: Prompt 模板
系统 SHALL 支持 Spring AI Prompt Template，允许管理员定义带变量的 Prompt 模板。

#### Scenario: 使用 Prompt 模板
- **WHEN** 管理员配置了带变量的 Prompt 模板
- **THEN** AI 对话时自动填充变量生成最终 Prompt

### Requirement: AI 调用日志
系统 SHALL 记录每次 AI 调用的日志，包括用户、输入内容、输出内容、调用时间、耗时。

#### Scenario: 记录 AI 调用日志
- **WHEN** AI 接口被调用
- **THEN** 系统自动记录调用日志到数据库
