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
├── frontend/             # Vue3 前端项目（含 Dockerfile、nginx.conf）
├── .env.example          # 环境变量示例
├── .env.local            # 环境变量（敏感，不提交）
├── docker-compose.yml    # Docker 部署编排（仅前端 + 后端，中间件使用外部实例）
├── Dockerfile.backend    # 后端镜像构建文件
├── .dockerignore         # Docker 构建忽略清单
└── pom.xml               # Maven 父 POM
```

## 环境要求

**本地开发**：

- JDK 21+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- MinIO

**Docker 部署**：

- Docker 24+ / Docker Compose v2
- 已部署好的外部中间件：MySQL 8.0+、Redis 6.0+、MinIO（容器内不会重复创建中间件）

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

## Docker 部署

### 部署架构

项目通过 `docker-compose.yml` 只编排 **2 个容器**：

| 容器 | 镜像 | 宿主机端口 | 说明 |
|------|------|-----------|------|
| `am-backend` | `am-portal-backend:latest` | `2888` → 8080 | Spring Boot 后端（prod profile） |
| `am-frontend` | `am-portal-frontend:latest` | `2887` → 80 | Nginx 托管前端 + `/api` 反代后端 |

**中间件全部使用外部已部署实例**（MySQL / Redis / MinIO），容器内不会重复创建中间件，连接地址从 `.env.local` 注入。

端口可通过 `.env.local` 中的 `WEB_PORT`（前端）和 `BACKEND_PORT`（后端）覆盖，默认 2887 / 2888。

### 部署前提

1. 服务器已安装 Docker 24+ 与 Docker Compose v2
2. 已部署好外部中间件，且服务器可访问：MySQL、Redis、MinIO
3. 外部 MySQL 已初始化数据库（见「快速开始 → 2. 初始化数据库」）

### 首次部署步骤

**第 1 步：上传代码到服务器**

```bash
# 服务器上拉取代码（或本地打包上传）
git clone <仓库地址> am-portal
cd am-portal
```

**第 2 步：配置环境变量**

```bash
cp .env.example .env.local
vim .env.local
```

按实际中间件地址填写（容器通过以下地址连接外部中间件）：

```env
# ---- 外部 MySQL ----
MYSQL_HOST=<MySQL 服务器 IP>
MYSQL_PORT=3306
MYSQL_DATABASE=am_portal
MYSQL_USER=root
MYSQL_PASSWORD=<密码>

# ---- 外部 Redis ----
REDIS_HOST=<Redis 服务器 IP>
REDIS_PORT=6379
REDIS_PASSWORD=<密码>

# ---- 外部 MinIO ----
MINIO_ENDPOINT=http://<MinIO 地址>:9000
MINIO_ACCESS_KEY=<AccessKey>
MINIO_SECRET_KEY=<SecretKey>
MINIO_BUCKET_NAME=am-portal

# ---- DeepSeek ----
DEEPSEEK_API_KEY=<API Key>

# ---- 端口（可选，默认 2887 / 2888）----
WEB_PORT=2887
BACKEND_PORT=2888
```

> 中间件若部署在同一宿主机上且未设置 `MYSQL_HOST` 等，默认值为 `host.docker.internal`（compose 已通过 `extra_hosts` 支持该解析）。

**第 3 步：构建并启动**

```bash
docker compose up -d --build
```

首次启动时后端需等待健康检查通过（`/actuator/health`），前端会在后端就绪后启动。

**第 4 步：验证**

```bash
# 查看容器状态（两个容器都应为 Up，backend 为 healthy）
docker compose ps

# 查看后端启动日志
docker compose logs -f backend
```

访问地址：

| 地址 | 说明 |
|------|------|
| `http://<服务器IP>:2887` | 前端页面 |
| `http://<服务器IP>:2888` | 后端 API |
| `http://<服务器IP>:2888/doc.html` | 接口文档 |

### 更新服务

代码有更新时（`git pull` 之后），重新构建镜像并重启容器：

```bash
# 拉取最新代码
git pull

# 重新构建并重启全部服务
docker compose up -d --build
```

只更新单个服务：

```bash
docker compose up -d --build backend    # 只更新后端
docker compose up -d --build frontend   # 只更新前端
```

仅修改了 `.env.local` 配置（代码未变）：

```bash
docker compose up -d    # 重新创建容器以加载新环境变量，不会重新构建镜像
```

> 说明：`--build` 会重建镜像；镜像基于 Docker 层缓存，仅变更的层会重新构建。数据保存在外部中间件中，重启容器不会丢失业务数据。

### 常用运维命令

```bash
docker compose ps                 # 查看容器状态
docker compose logs -f backend    # 实时查看后端日志
docker compose logs -f frontend   # 实时查看前端日志
docker compose restart backend    # 重启后端（不重建镜像）
docker compose stop               # 停止所有服务
docker compose down               # 停止并移除容器（数据在外部中间件，不受影响）
```

### 常见问题排查

| 现象 | 排查方式 |
|------|---------|
| `backend` 一直 `health: starting` 或退出 | `docker compose logs backend`，通常是连不上外部 MySQL/Redis/MinIO，检查 `.env.local` 中地址、端口、密码及网络可达性 |
| `frontend` 未启动 | 前端依赖后端健康检查，先解决后端问题 |
| 页面可访问但接口 502 | 检查后端容器是否存活：`docker compose ps` |
| 端口冲突 | 修改 `.env.local` 中 `WEB_PORT` / `BACKEND_PORT` 后 `docker compose up -d` |
| 必填环境变量未设置导致启动失败 | compose 中 `MYSQL_PASSWORD`、`REDIS_PASSWORD`、`MINIO_ACCESS_KEY`、`MINIO_SECRET_KEY` 为必填，缺失会直接报错，补全 `.env.local` 即可 |
