## Why

需要一个集项目展示、个人博客、AI 助手于一体的个人网站平台，作为个人主页和作品集，同时提供后台管理能力。当前没有统一的个人品牌展示入口，项目、博客、AI 能力分散，无法集中管理和展示。

## What Changes

- 新建 AM Portal 个人展示平台，前后端分离架构
- 后端基于 Java 21 + Spring Boot 3.x + Spring AI + MyBatis Plus + Spring Security
- 前端基于 Vue3 + TypeScript + Element Plus
- 实现首页展示（个人信息、最近项目、最近博客、AI 入口）
- 实现用户系统（注册、登录、审核、三种角色：Guest/User/Admin）
- 实现权限管理（Spring Security + RBAC）
- 实现个人信息管理（管理员可修改首页信息、头像上传至 MinIO）
- 实现项目管理（CRUD、Markdown+HTML 混合展示、图片上传）
- 实现博客系统（CRUD、分类标签、Markdown 实时预览、图片上传）
- 实现文件上传与管理（MinIO 统一存储、支持 jpg/png/gif/webp）
- 实现 AI 助手（DeepSeek + Spring AI、多轮对话、上下文记忆、Prompt 模板）
- 实现 Function Calling（网页搜索、可扩展插件化）
- 实现 Prompt 管理（后台 CRUD、启用/停用、默认 Prompt）
- 实现文件管理后台（MinIO 文件查看、删除、上传、下载、搜索）
- 实现系统配置统一管理（多环境 application-{env}.yml）
- 实现日志记录（登录、AI 调用、文件上传、用户操作）
- 数据存储：MySQL + Redis + MinIO
- 安全：BCrypt 密码加密、JWT 认证、文件上传限制、SQL 注入防护

## Capabilities

### New Capabilities

- `home-page`: 首页展示模块，包括个人信息、最近项目、最近博客、AI 入口，所有信息支持后台修改
- `user-system`: 用户系统，包括注册、登录（用户名/邮箱）、用户审核、三种角色（Guest/User/Admin）
- `auth-rbac`: 权限管理，基于 Spring Security + RBAC 模型的接口权限控制
- `profile-management`: 个人信息管理，管理员修改首页信息、头像上传至 MinIO
- `project-management`: 项目管理，CRUD、Markdown+HTML 混合展示、图片上传、置顶、显示控制
- `blog-system`: 博客系统，CRUD、分类标签、Markdown 实时预览、阅读量统计、发布控制
- `file-storage`: 文件上传与管理，MinIO 统一存储、格式限制、文件管理后台
- `ai-assistant`: AI 助手，DeepSeek + Spring AI、多轮对话、上下文记忆、Prompt 模板、系统 Prompt 动态配置
- `function-calling`: Function Calling，网页搜索、插件化扩展接口
- `prompt-management`: Prompt 管理，后台 CRUD、启用/停用、默认 Prompt 配置
- `system-config`: 系统配置管理，多环境配置、统一管理、禁止硬编码
- `logging`: 日志记录，登录日志、AI 调用日志、文件上传日志、用户操作日志

### Modified Capabilities

## Impact

- 新建完整后端项目，采用多模块 Maven 架构（project-common、project-framework、project-auth、project-user、project-project、project-blog、project-ai、project-storage、project-system、project-admin、project-api、project-web）
- 新建前端项目（Vue3 + TypeScript + Vite）
- 依赖外部服务：MySQL、Redis、MinIO、DeepSeek API
- RESTful API 设计，所有接口提供 Swagger/Knife4j 文档
- DTO/VO 分层，Service 接口化
