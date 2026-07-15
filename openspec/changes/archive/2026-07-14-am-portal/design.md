## Context

AM Portal 是一个全新的个人展示平台项目，从零开始构建。项目需要集成项目展示、博客系统、AI 助手三大核心能力，同时提供完整的后台管理。技术栈已确定：后端 Java 21 + Spring Boot 3.x + Spring AI + MyBatis Plus + Spring Security，前端 Vue3 + TypeScript + Element Plus，数据层 MySQL + Redis + MinIO。

## Goals / Non-Goals

**Goals:**
- 构建前后端分离的个人展示平台，支持项目展示、博客发布、AI 对话
- 实现完整的三级权限体系（Guest/User/Admin）和用户审核流程
- AI 助手集成 DeepSeek，支持多轮对话、Function Calling、动态 Prompt
- 所有文件统一 MinIO 存储，所有缓存统一 Redis，所有配置统一管理
- 模块化 Maven 多模块架构，高内聚低耦合
- RESTful API 设计，提供 Swagger/Knife4j 文档

**Non-Goals:**
- 评论系统、点赞系统、收藏系统（预留接口，V2 实现）
- RSS、Sitemap、SEO 优化（后续规划）
- AI 知识库/RAG/向量数据库（后续规划）
- Docker/K8s 部署（后续规划）
- OAuth/微信/QQ 第三方登录（后续规划）
- 多模型切换（后续规划）
- 多语言支持（后续规划）

## Decisions

### 1. 多模块 Maven 架构

**决策**: 采用多模块 Maven 项目，模块划分如下：
- `project-common`: 通用工具、常量、异常、响应封装
- `project-framework`: Spring Security 配置、Redis 配置、MinIO 配置、全局异常处理
- `project-auth`: 认证授权模块（JWT、登录、注册）
- `project-user`: 用户管理模块（CRUD、审核、个人信息）
- `project-project`: 项目管理模块（CRUD、展示）
- `project-blog`: 博客模块（CRUD、分类标签、展示）
- `project-ai`: AI 助手模块（Spring AI、DeepSeek、Function Calling、Prompt）
- `project-storage`: 文件存储模块（MinIO 上传、管理）
- `project-system`: 系统配置模块（首页信息、系统参数）
- `project-admin`: 后台管理聚合模块
- `project-api`: 前台 API 聚合模块
- `project-web`: 启动模块（Spring Boot Application）

**理由**: 模块化便于团队协作、独立开发、按需扩展。每个业务模块独立，降低耦合。

**备选方案**: 单模块项目 — 简单但难以扩展，不符合高内聚低耦合原则。

### 2. 认证方案：JWT + Spring Security

**决策**: 使用 JWT 无状态认证，Spring Security 做权限控制，RBAC 模型管理接口权限。

**理由**: 前后端分离架构适合无状态 JWT；Spring Security 生态成熟，与 Spring Boot 3.x 深度集成；RBAC 模型灵活，支持三种角色权限控制。

**备选方案**: Sa-Token — 更轻量但生态不如 Spring Security 成熟，作为可选方案保留。

### 3. AI 集成：Spring AI + DeepSeek

**决策**: 通过 Spring AI 框架集成 DeepSeek API，使用 ChatClient 进行对话，支持 Function Calling 和 Prompt Template。

**理由**: Spring AI 是 Spring 官方 AI 框架，与 Spring Boot 3.x 无缝集成；DeepSeek API 兼容 OpenAI 协议，Spring AI 原生支持；Function Calling 支持插件化扩展。

**备选方案**: 直接 HTTP 调用 DeepSeek API — 可行但需要自行管理连接、重试、上下文，Spring AI 已封装这些能力。

### 4. 文件存储：MinIO

**决策**: 所有文件（头像、封面、项目图片、博客图片）统一上传至 MinIO，数据库仅保存文件 URL。

**理由**: MinIO 兼容 S3 API，部署简单，适合私有化场景；统一存储便于管理和后续迁移至 OSS/COS/S3。

**备选方案**: 直接本地文件系统 — 不支持分布式，不利于扩展。

### 5. 缓存策略：Redis

**决策**: Redis 缓存登录 Token、AI 上下文、热门博客/项目、系统配置。

**理由**: Redis 高性能，支持多种数据结构；Token 存储支持过期；AI 上下文需要快速读写；热点数据缓存减轻数据库压力。

### 6. 前端架构：Vue3 + TypeScript + Vite

**决策**: Vue3 Composition API + TypeScript + Vite 构建，Element Plus UI 组件库，Pinia 状态管理。

**理由**: Vue3 性能优于 Vue2，Composition API 逻辑复用更好；TypeScript 类型安全；Vite 开发体验好；Element Plus 组件丰富适合后台管理。

### 7. 数据库设计

**决策**: MySQL 存储所有业务数据，MyBatis Plus 做 ORM，防止 SQL 注入。

**核心表**: user、project、blog、prompt、sys_config、file_info、blog_category、blog_tag、blog_tag_relation、ai_conversation、ai_message、operation_log

### 8. 多环境配置

**决策**: 采用 Spring Profile 多环境配置：application.yml（公共）、application-local.yml、application-dev.yml、application-prod.yml。

**理由**: 统一管理不同环境配置，禁止硬编码，便于部署切换。

## Risks / Trade-offs

- **[DeepSeek API 可用性]** → 设计 AI 模块时抽象模型接口，后续可切换至 OpenAI/Qwen/GLM，降低单一供应商风险
- **[MinIO 单点故障]** → 初期单节点部署，后续可扩展为分布式模式；关键文件做好备份策略
- **[JWT 无状态刷新]** → 实现 Token 刷新机制，短期 Access Token + 长期 Refresh Token
- **[AI 上下文长度限制]** → Redis 存储对话历史，设置最大上下文轮数，超出时截断早期对话
- **[文件上传安全]** → 严格限制文件类型（jpg/png/gif/webp）和大小，防止恶意文件上传
- **[多模块复杂度]** → 模块间依赖通过 project-common 和 project-framework 统一管理，避免循环依赖
