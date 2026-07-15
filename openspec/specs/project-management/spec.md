## ADDED Requirements

### Requirement: 项目 CRUD
系统 SHALL 支持管理员新增、修改、删除项目，支持游客和用户浏览项目列表和详情。

#### Scenario: 管理员新增项目
- **WHEN** 管理员提交项目信息（名称、Logo、封面、简介、Markdown 介绍、HTML 内容、截图、标签、Github 链接、Gitee 链接、在线体验地址、下载地址）
- **THEN** 系统创建项目记录，设置发布时间

#### Scenario: 管理员修改项目
- **WHEN** 管理员修改项目信息
- **THEN** 系统更新项目记录，更新时间自动刷新

#### Scenario: 管理员删除项目
- **WHEN** 管理员删除项目
- **THEN** 系统删除项目记录及关联数据

#### Scenario: 游客浏览项目列表
- **WHEN** 游客访问项目列表页
- **THEN** 系统返回所有"显示"状态的项目，置顶项目优先，按发布时间倒序

### Requirement: 项目详情展示
系统 SHALL 支持点击项目进入详情页，展示项目介绍、截图、技术栈、链接、Markdown 内容、HTML 内容。

#### Scenario: 查看项目详情
- **WHEN** 用户点击项目卡片
- **THEN** 系统展示项目完整信息，包括 Markdown 和 HTML 混合内容、项目截图画廊、所有链接

#### Scenario: 项目图片浏览
- **WHEN** 用户在项目详情页点击截图
- **THEN** 系统支持图片放大浏览

### Requirement: 项目字段模型
项目实体 SHALL 包含：项目名称、项目 Logo、项目封面、项目简介、Markdown 介绍、HTML 内容、项目截图、项目标签、发布时间、更新时间、Github 链接、Gitee 链接、在线体验地址、下载地址、是否置顶、是否显示。

#### Scenario: 项目数据完整性
- **WHEN** 创建或更新项目
- **THEN** 项目名称必填，发布时间自动设置，更新时间自动刷新

### Requirement: 项目图片上传
系统 SHALL 支持项目相关图片（Logo、封面、截图）上传至 MinIO。

#### Scenario: 上传项目图片
- **WHEN** 管理员上传项目图片
- **THEN** 系统将图片存储至 MinIO，返回访问 URL

### Requirement: 项目置顶和显示控制
系统 SHALL 支持项目置顶和显示/隐藏控制。

#### Scenario: 置顶项目
- **WHEN** 管理员设置项目为置顶
- **THEN** 该项目在列表中优先展示

#### Scenario: 隐藏项目
- **WHEN** 管理员设置项目为不显示
- **THEN** 游客和用户无法在列表中看到该项目
