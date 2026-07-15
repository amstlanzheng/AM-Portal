## 1. 项目初始化与基础架构

- [x] 1.1 创建 Maven 多模块项目结构（project-common、project-framework、project-auth、project-user、project-project、project-blog、project-ai、project-storage、project-system、project-admin、project-api、project-web）
- [x] 1.2 配置父 POM 依赖管理（Java 21、Spring Boot 3.x、MyBatis Plus、Spring AI 等）
- [x] 1.3 创建 project-common 模块：通用工具类、常量、统一响应封装（Result/ResultCode）、全局异常定义
- [x] 1.4 创建 project-framework 模块：Spring Security 配置、Redis 配置、MinIO 配置、CORS 配置、全局异常处理器
- [x] 1.5 配置多环境配置文件（application.yml、application-local.yml、application-dev.yml、application-prod.yml）
- [x] 1.6 配置 Swagger/Knife4j 接口文档
- [x] 1.7 创建 project-web 启动模块，配置 Spring Boot Application 入口

## 2. 数据库设计与基础数据层

- [x] 2.1 设计并创建数据库表：user（用户表）
- [x] 2.2 设计并创建数据库表：project（项目表）
- [x] 2.3 设计并创建数据库表：blog（博客表）、blog_category（分类表）、blog_tag（标签表）、blog_tag_relation（博客标签关联表）
- [x] 2.4 设计并创建数据库表：prompt（Prompt 表）
- [x] 2.5 设计并创建数据库表：sys_config（系统配置表）
- [x] 2.6 设计并创建数据库表：file_info（文件信息表）
- [x] 2.7 设计并创建数据库表：ai_conversation（AI 对话表）、ai_message（AI 消息表）
- [x] 2.8 设计并创建数据库表：operation_log（操作日志表）
- [x] 2.9 配置 MyBatis Plus，创建各表的 Entity、Mapper

## 3. 用户系统与认证授权

- [x] 3.1 实现 project-auth 模块：JWT Token 生成与解析工具类
- [x] 3.2 实现 JWT Access Token + Refresh Token 机制
- [x] 3.3 实现 Spring Security Filter Chain：Token 校验过滤器、权限校验
- [x] 3.4 实现 RBAC 权限模型：Guest/User/Admin 三种角色权限配置
- [x] 3.5 实现用户注册接口：参数校验、BCrypt 密码加密、默认待审核状态
- [x] 3.6 实现用户登录接口：用户名密码校验、状态检查（待审核/禁用）、返回 JWT Token
- [x] 3.7 实现 Token 刷新接口：Refresh Token 换取新 Access Token
- [x] 3.8 实现 project-user 模块：用户 CRUD、用户审核（启用/禁用）、管理员设置

## 4. 个人信息与系统配置管理

- [x] 4.1 实现 project-system 模块：sys_config 表的 CRUD Service
- [x] 4.2 实现系统配置 Redis 缓存：读取时优先缓存、修改时刷新缓存
- [x] 4.3 实现首页信息管理接口：管理员修改网站标题、副标题、个人介绍、联系方式等
- [x] 4.4 实现首页数据查询接口：返回个人信息、最近项目、最近博客
- [x] 4.5 实现用户个人信息修改接口：昵称、邮箱、手机号修改
- [x] 4.6 实现头像上传与更新接口：上传至 MinIO，更新用户头像 URL

## 5. 文件存储服务

- [x] 5.1 实现 project-storage 模块：MinIO 客户端配置与工具类
- [x] 5.2 实现文件上传接口：支持 jpg/png/gif/webp 格式校验、文件大小限制
- [x] 5.3 实现文件按目录管理：avatar/、project/、blog/ 等目录划分
- [x] 5.4 实现 file_info 表 CRUD：文件元信息持久化
- [x] 5.5 实现文件管理后台接口：文件列表、删除、搜索、获取访问地址
- [x] 5.6 实现文件下载接口

## 6. 项目管理

- [x] 6.1 实现 project-project 模块：项目 Entity、Mapper、Service、Controller
- [x] 6.2 实现项目新增接口：包含所有字段（名称、Logo、封面、简介、Markdown、HTML、截图、标签、链接等）
- [x] 6.3 实现项目修改接口：更新项目信息，自动刷新更新时间
- [x] 6.4 实现项目删除接口：删除项目及关联数据
- [x] 6.5 实现项目列表查询接口：支持置顶排序、显示/隐藏过滤、分页
- [x] 6.6 实现项目详情查询接口：返回完整项目信息
- [x] 6.7 实现项目图片上传接口：Logo、封面、截图上传至 MinIO
- [x] 6.8 实现项目置顶和显示控制接口

## 7. 博客系统

- [x] 7.1 实现 project-blog 模块：博客 Entity、Mapper、Service、Controller
- [x] 7.2 实现博客新增接口：标题、摘要、封面、分类、标签、Markdown、HTML、发布状态
- [x] 7.3 实现博客修改接口
- [x] 7.4 实现博客删除接口：删除博客及标签关联
- [x] 7.5 实现博客列表查询接口：支持按分类、标签筛选，按发布时间倒序，仅返回已发布博客
- [x] 7.6 实现博客详情查询接口：阅读量自增
- [x] 7.7 实现博客分类和标签管理接口：分类 CRUD、标签 CRUD、关联管理
- [x] 7.8 实现博客发布/草稿状态控制接口
- [x] 7.9 实现博客图片上传接口：编辑器内图片上传至 MinIO

## 8. AI 助手

- [x] 8.1 实现 project-ai 模块：Spring AI 配置、DeepSeek API 集成
- [x] 8.2 实现 AI 聊天接口：流式响应（SSE），调用 DeepSeek ChatClient
- [x] 8.3 实现多轮对话上下文管理：Redis 存储对话历史，设置最大上下文轮数
- [x] 8.4 实现对话管理：新建对话、清除上下文
- [x] 8.5 实现系统 Prompt 动态加载：从数据库读取默认 Prompt，修改后实时生效
- [x] 8.6 实现 Prompt Template 支持：带变量的 Prompt 模板
- [x] 8.7 实现 AI 开放状态控制：根据系统配置决定是否对游客开放
- [x] 8.8 实现 AI 调用日志记录：用户、输入、输出、耗时

## 9. Function Calling

- [x] 9.1 实现 Function Calling 统一接口：Spring AI Function 注册机制
- [x] 9.2 实现网页搜索 Function：调用搜索 API，返回搜索结果
- [x] 9.3 实现 Function 调用日志记录：Function 名称、参数、返回结果、耗时
- [x] 9.4 预留 Function 扩展接口：天气查询、时间查询、项目搜索、博客搜索等

## 10. Prompt 管理

- [x] 10.1 实现 Prompt CRUD 接口：新增、修改、删除 Prompt
- [x] 10.2 实现 Prompt 启用/停用接口
- [x] 10.3 实现默认 Prompt 设置接口：同一时间仅一个默认 Prompt
- [x] 10.4 实现 AI 启动时自动加载默认 Prompt

## 11. 日志系统

- [x] 11.1 实现登录日志记录：用户名、登录时间、IP、结果
- [x] 11.2 实现 AI 调用日志记录：调用用户、输入、输出、耗时
- [x] 11.3 实现文件上传日志记录：上传用户、文件名、大小、结果
- [x] 11.4 实现用户操作日志记录：操作用户、类型、内容、时间、结果
- [x] 11.5 实现日志查询接口：按时间、类型、用户筛选

## 12. 后台管理聚合接口

- [x] 12.1 实现 project-admin 模块：聚合所有后台管理接口
- [x] 12.2 实现后台仪表盘接口：统计数据（用户数、项目数、博客数、AI 调用次数等）

## 13. 前台 API 聚合接口

- [x] 13.1 实现 project-api 模块：聚合所有前台公开接口
- [x] 13.2 实现首页数据聚合接口：个人信息 + 最近项目 + 最近博客

## 14. 前端项目初始化

- [x] 14.1 创建 Vue3 + TypeScript + Vite 前端项目
- [x] 14.2 配置 Element Plus、Pinia、Vue Router、Axios
- [x] 14.3 实现前端路由配置：首页、项目列表、项目详情、博客列表、博客详情、AI 助手
- [x] 14.4 实现前端路由配置：后台管理页面（用户管理、项目管理、博客管理、Prompt 管理、文件管理、系统配置）
- [x] 14.5 实现 Axios 请求封装：统一拦截器、Token 注入、错误处理
- [x] 14.6 实现 Pinia 状态管理：用户状态、Token 管理

## 15. 前端页面开发

- [x] 15.1 实现首页：个人信息展示、最近项目、最近博客、AI 入口
- [x] 15.2 实现登录/注册页面
- [x] 15.3 实现项目列表页和项目详情页
- [x] 15.4 实现博客列表页和博客详情页
- [x] 15.5 实现 AI 聊天窗口组件：流式消息展示、多轮对话
- [x] 15.6 实现后台用户管理页面：用户列表、审核、角色设置
- [x] 15.7 实现后台项目管理页面：项目 CRUD、图片上传、Markdown 编辑
- [x] 15.8 实现后台博客管理页面：博客 CRUD、Markdown 实时预览、图片上传
- [x] 15.9 实现后台 Prompt 管理页面：Prompt CRUD、启用/停用、默认设置
- [x] 15.10 实现后台文件管理页面：文件列表、上传、删除、搜索
- [x] 15.11 实现后台系统配置页面：首页信息修改、AI 开放状态
- [x] 15.12 实现个人信息页面：资料修改、头像上传
