# ============================================
# 前端多阶段构建：Node 构建 + Nginx 运行
# 构建上下文为 frontend 目录：
#   docker build -f frontend/Dockerfile -t am-portal-frontend frontend/
# ============================================

# ---------- 构建阶段 ----------
FROM node:20-alpine AS builder

WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------- 运行阶段 ----------
FROM nginx:1.27-alpine

# 自定义 Nginx 配置：SPA 路由 + /api 反向代理 + SSE 支持
COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget -qO- http://localhost/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
