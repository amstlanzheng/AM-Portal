## ADDED Requirements

### Requirement: 文件上传
系统 SHALL 支持图片文件上传至 MinIO，支持的格式为 jpg、png、gif、webp。

#### Scenario: 上传图片成功
- **WHEN** 用户上传符合格式和大小的图片文件
- **THEN** 系统将文件存储至 MinIO，在数据库保存文件信息（URL、文件名、大小、类型），返回访问 URL

#### Scenario: 上传不支持的格式
- **WHEN** 用户上传非 jpg/png/gif/webp 格式的文件
- **THEN** 系统返回错误提示"不支持的文件格式"

#### Scenario: 上传文件超过大小限制
- **WHEN** 用户上传超过配置大小限制的文件
- **THEN** 系统返回错误提示"文件大小超过限制"

### Requirement: 文件管理后台
系统 SHALL 提供文件管理后台，支持查看、删除、上传、下载、搜索 MinIO 中的文件。

#### Scenario: 查看文件列表
- **WHEN** 管理员访问文件管理页面
- **THEN** 系统展示文件列表，包含文件名、大小、类型、上传时间、URL

#### Scenario: 删除文件
- **WHEN** 管理员删除文件
- **THEN** 系统从 MinIO 删除文件，同时删除数据库中的文件记录

#### Scenario: 搜索文件
- **WHEN** 管理员按文件名搜索
- **THEN** 系统返回匹配的文件列表

### Requirement: 文件按目录管理
系统 SHALL 支持按目录管理文件，不同业务模块的文件存储在不同目录下（如 avatar/、project/、blog/）。

#### Scenario: 按目录浏览文件
- **WHEN** 管理员选择某个目录
- **THEN** 系统展示该目录下的文件列表

### Requirement: 文件信息持久化
系统 SHALL 在数据库 file_info 表中保存文件元信息，包括文件 URL、原始文件名、存储文件名、文件大小、文件类型、上传时间、所属目录。

#### Scenario: 文件记录完整性
- **WHEN** 文件上传成功
- **THEN** 数据库中保存完整的文件元信息记录
