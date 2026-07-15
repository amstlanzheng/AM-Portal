## ADDED Requirements

### Requirement: 博客 CRUD
系统 SHALL 支持管理员新增、修改、删除博客文章，支持游客和用户浏览博客列表和阅读内容。

#### Scenario: 管理员新增博客
- **WHEN** 管理员提交博客信息（标题、摘要、封面、分类、标签、Markdown 正文、HTML 正文、是否发布）
- **THEN** 系统创建博客记录，设置发布时间

#### Scenario: 管理员修改博客
- **WHEN** 管理员修改博客内容
- **THEN** 系统更新博客记录

#### Scenario: 管理员删除博客
- **WHEN** 管理员删除博客
- **THEN** 系统删除博客记录及关联数据

#### Scenario: 游客浏览博客列表
- **WHEN** 游客访问博客列表页
- **THEN** 系统返回所有已发布的博客，按发布时间倒序

### Requirement: 博客分类和标签
系统 SHALL 支持博客分类和标签管理，支持按分类和标签筛选。

#### Scenario: 按分类筛选博客
- **WHEN** 用户选择某个分类
- **THEN** 系统返回该分类下的所有已发布博客

#### Scenario: 按标签筛选博客
- **WHEN** 用户选择某个标签
- **THEN** 系统返回包含该标签的所有已发布博客

### Requirement: 博客阅读量统计
系统 SHALL 统计每篇博客的阅读量，每次访问详情页增加一次阅读量。

#### Scenario: 访问博客详情
- **WHEN** 用户访问博客详情页
- **THEN** 系统将该博客阅读量加 1

### Requirement: 博客发布控制
系统 SHALL 支持博客的发布/草稿状态控制，未发布的博客仅管理员可见。

#### Scenario: 保存草稿
- **WHEN** 管理员保存博客但不发布
- **THEN** 博客状态为草稿，游客和用户不可见

#### Scenario: 发布博客
- **WHEN** 管理员将博客设置为发布
- **THEN** 博客对所有用户可见

### Requirement: 博客字段模型
博客实体 SHALL 包含：标题、摘要、封面、分类、标签、Markdown 正文、HTML 正文、阅读量、发布时间、是否发布。

#### Scenario: 博客数据完整性
- **WHEN** 创建或更新博客
- **THEN** 标题必填，Markdown 正文和 HTML 正文至少填写一项

### Requirement: 博客图片上传
系统 SHALL 支持博客内图片上传至 MinIO，支持 Markdown 编辑器中直接插入图片。

#### Scenario: 编辑器中上传图片
- **WHEN** 管理员在 Markdown 编辑器中上传图片
- **THEN** 系统将图片存储至 MinIO，返回图片 URL 并插入编辑器

### Requirement: Markdown 实时预览
系统 SHALL 支持博客 Markdown 内容的实时预览。

#### Scenario: 编辑时实时预览
- **WHEN** 管理员在编辑器中输入 Markdown 内容
- **THEN** 预览区域实时渲染 HTML 效果
