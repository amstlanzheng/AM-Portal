# AM Portal - 个人展示平台

集项目展示、个人博客、AI 助手于一体的个人网站平台。

## 技术栈

### 后端
- Java 21 + Spring Boot 3.x
- Spring Security + JWT 认证
- Spring AI + DeepSeek
- MyBatis Plus + MySQL
- Redis 缓存
- MinIO 文件存储
- Knife4j 接口文档

### 前端
- Vue 3 + TypeScript + Vite
- Element Plus UI
- Pinia 状态管理
- Vue Router
- Axios

## 项目结构

```
am-portal/
├── doc/                  # 项目相关的文档
├── project-common/       # 通用工具、常量、异常、响应封装
├── project-framework/    # 框架配置：Security、Redis、MinIO、全局异常处理
├── project-auth/         # 认证授权：JWT、登录、注册
├── project-user/         # 用户管理：CRUD、审核、个人信息
├── project-project/      # 项目管理：CRUD、置顶、显示控制
├── project-blog/         # 博客系统：CRUD、分类标签、阅读量
├── project-ai/           # AI 助手：DeepSeek、多轮对话、Prompt、Function Calling
├── project-storage/      # 文件存储：MinIO 上传、管理
├── project-system/       # 系统配置、操作日志
├── project-admin/        # 后台管理聚合、仪表盘
├── project-api/          # 前台 API 聚合、首页数据
├── project-web/          # 启动模块、配置文件、SQL 脚本
├── frontend/             # Vue3 前端项目
├── .env.example          # 环境变量示例
├── .env.local            # 环境变量（敏感，不提交）
└── pom.xml               # Maven 父 POM
```

## 环境要求

- JDK 21+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- MinIO

## 快速开始

### 1. 配置环境变量

```bash
# 复制示例文件
cp .env.example .env.local

# 编辑 .env.local，填入实际的数据库、Redis、MinIO、DeepSeek 配置
```

`.env.local` 文件内容：

```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=am_portal
MYSQL_USER=root
MYSQL_PASSWORD=your_password

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET_NAME=am-portal

DEEPSEEK_API_KEY=your_api_key
```

> **重要**：`.env.local` 已在 `.gitignore` 中排除，不会被提交到版本控制。

### 2. 初始化数据库

创建 MySQL 数据库并执行初始化脚本：

```sql
CREATE DATABASE IF NOT EXISTS am_portal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后执行 `project-web/src/main/resources/db/schema.sql` 中的建表和初始数据脚本。

### 3. 启动后端

**方式一：IDE 启动（推荐开发时使用）**

1. 在 IDE 中加载环境变量（从 `.env.local` 文件）
2. 运行启动类 `com.am.AmPortalApplication`
3. 默认 profile 为 `dev`，读取 `application-dev.yml` 配置

**方式二：Maven 命令行启动**

```bash
# 先加载环境变量（Linux/Mac）
source .env.local && export $(cut -d= -f1 .env.local)

# 或 Windows PowerShell
Get-Content .env.local | ForEach-Object { $parts = $_ -split '='; [Environment]::SetEnvironmentVariable($parts[0], $parts[1], 'Process') }

# 启动
mvn spring-boot:run -pl project-web
```

后端启动后访问：
- API 地址：`http://localhost:8080`
- 接口文档：`http://localhost:8080/doc.html`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问：`http://localhost:5173`

前端通过 Vite 代理将 `/api` 请求转发到后端 `http://localhost:8080`。

### 5. 初始管理员账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin  | Chaos33550336%0 | 管理员 |

## 环境配置说明

项目采用 Spring Profile 多环境配置：

| 文件 | 用途 |
|------|------|
| `application.yml` | 公共配置（端口、MyBatis、JWT、Knife4j 等） |
| `application-dev.yml` | 开发环境（从环境变量读取敏感信息） |
| `application-prod.yml` | 生产环境（从环境变量读取，关闭 SQL 日志） |

默认激活 `dev` 环境。所有敏感信息（数据库密码、Redis 密码、MinIO 密钥、API Key）统一通过环境变量注入，**禁止硬编码**。

## 功能模块

| 模块 | 功能 |
|------|------|
| 首页 | 个人信息展示、最近项目、最近博客、AI 入口 |
| 用户系统 | 注册、登录、审核、三种角色（Guest/User/Admin） |
| 权限管理 | Spring Security + JWT + RBAC |
| 项目管理 | CRUD、Markdown+HTML 展示、置顶、显示控制 |
| 博客系统 | CRUD、分类标签、Markdown 预览、阅读量 |
| AI 助手 | DeepSeek 多轮对话、上下文记忆、流式响应 |
| Function Calling | 网页搜索、插件化扩展 |
| Prompt 管理 | CRUD、启用/停用、默认 Prompt |
| 文件管理 | MinIO 统一存储、格式限制、后台管理 |
| 系统配置 | 首页信息、AI 开放状态、动态配置 |
| 操作日志 | 登录、AI 调用、文件上传、用户操作 |

## API 接口

| 路径 | 说明 |
|------|------|
| `GET /api/home` | 首页数据 |
| `POST /api/auth/login` | 登录 |
| `POST /api/auth/register` | 注册 |
| `GET /api/projects` | 项目列表 |
| `GET /api/blogs` | 博客列表 |
| `POST /api/ai/chat` | AI 对话（SSE） |
| `GET /api/admin/*` | 后台管理接口 |

完整接口文档启动后访问 `http://localhost:8080/doc.html`。

## 部署

### 生产环境

1. 设置环境变量（同 `.env.local` 中的变量，指向生产环境服务）
2. 修改 `application.yml` 中 `spring.profiles.active` 为 `prod`
3. 构建后端：`mvn clean package -DskipTests`
4. 构建前端：`cd frontend && npm run build`
5. 将 `frontend/dist` 部署到 Nginx，反向代理 `/api` 到后端

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```
