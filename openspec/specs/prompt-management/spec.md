## ADDED Requirements

### Requirement: Prompt CRUD
系统 SHALL 支持管理员新增、修改、删除 Prompt。

#### Scenario: 新增 Prompt
- **WHEN** 管理员提交新 Prompt（名称、内容、是否默认）
- **THEN** 系统创建 Prompt 记录

#### Scenario: 修改 Prompt
- **WHEN** 管理员修改 Prompt 内容
- **THEN** 系统更新 Prompt 记录

#### Scenario: 删除 Prompt
- **WHEN** 管理员删除 Prompt
- **THEN** 系统删除 Prompt 记录

### Requirement: Prompt 启用/停用
系统 SHALL 支持管理员启用或停用 Prompt，停用的 Prompt 不会被 AI 使用。

#### Scenario: 启用 Prompt
- **WHEN** 管理员启用某个 Prompt
- **THEN** 该 Prompt 可被 AI 使用

#### Scenario: 停用 Prompt
- **WHEN** 管理员停用某个 Prompt
- **THEN** 该 Prompt 不会被 AI 使用

### Requirement: 默认 Prompt 配置
系统 SHALL 支持设置默认 Prompt，AI 启动时自动读取默认 Prompt 作为系统提示词。

#### Scenario: 设置默认 Prompt
- **WHEN** 管理员将某个 Prompt 设置为默认
- **THEN** 之前的默认 Prompt 取消默认，新 Prompt 成为默认

#### Scenario: AI 使用默认 Prompt
- **WHEN** AI 对话启动时
- **THEN** 系统自动加载默认 Prompt 作为系统提示词

### Requirement: Prompt 数据模型
Prompt 实体 SHALL 包含：名称、内容、是否默认、是否启用、创建时间、更新时间。

#### Scenario: Prompt 数据完整性
- **WHEN** 创建或更新 Prompt
- **THEN** 名称和内容必填，同一时间只能有一个默认 Prompt
