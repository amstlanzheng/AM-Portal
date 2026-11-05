## Context

上一变更（minimal-tech-ui-redesign）建立了浅色极简风：品牌色阶变量（`--brand-700/600/500/100/50`）在 style.css 的 `:root` 中静态定义为蓝色系，语义层（`--color-accent` 等）与 Element Plus 层（`--el-color-primary` 及衍生）引用它。全站页面 scoped 样式统一使用语义令牌。

现有基础设施：
- `user` 表 / `User` 实体（MyBatis-Plus，无 theme 字段）
- `sys_config` 表 + `SysConfigService`（Redis 缓存，`getHomePageInfo()` 有 homeKeys 白名单，`/api/home/site-info` 公开返回）
- `UserController` 已有 `/me`、`/me/profile`、`/me/avatar` 模式可循
- 前端 `App.vue` + `stores/user`（Pinia）管理登录态；`Profile.vue` 个人中心；`admin/Config.vue` 系统配置（表单绑定 `site_*` 键）
- 迁移脚本模式：`db/migration-*.sql` 幂等设计，`application-prod.yml` 的 `sql.init` 启动自动执行（continue-on-error: true）

## Goals / Non-Goals

**Goals:**

- 默认主题改为青绿色（teal），未登录、未设置偏好的用户看到站点默认配色
- 管理员在后台「系统配置」设置站点默认配色（存 sys_config，Redis 缓存联动刷新）
- 登录用户在「个人中心」选择自己的配色（存 user 表，按用户区分），优先于站点默认
- 6 个预设主题（色板），点击即换、即时生效、无页面刷新
- Element Plus 组件主色及衍生色（light-3/5/7/8/9、dark-2）自动跟随

**Non-Goals:**

- 深色模式（dark theme）
- 自定义色值 / color picker（仅预设色板）
- 主题跟随操作系统
- 登录页按访客偏好显示（登录页始终用站点默认色）

## Decisions

### D1. 注入点：覆盖 `--brand-*` 色阶变量（5 个）

`applyTheme()` 在 `document.documentElement.style` 上覆盖 `--brand-700/600/500/100/50`。语义层与 EP 层的引用关系保持不变，改动面最小、天然全站联动。

替代方案（否决）：为每个主题写完整的一套 CSS class（`.theme-teal` 等）——需要复制全部令牌定义，维护成本高；直接改 EP 的 `--el-color-primary` 一个变量——衍生色与品牌浅色（`--color-accent-weak`）无法联动。

### D2. 衍生色改为 `color-mix()` 派生

style.css 中 EP 衍生色改为（与 EP 官方混白公式一致，已用现有蓝色静态值验证）：

```css
--el-color-primary-light-3: color-mix(in srgb, var(--brand-600) 70%, #FFF);
--el-color-primary-light-5: color-mix(in srgb, var(--brand-600) 50%, #FFF);
--el-color-primary-light-7: color-mix(in srgb, var(--brand-600) 30%, #FFF);
--el-color-primary-light-8: color-mix(in srgb, var(--brand-600) 20%, #FFF);
--el-color-primary-light-9: var(--brand-50);
--el-color-primary-dark-2: var(--brand-700);
```

`--shadow-accent` 同理改为 `color-mix(in srgb, var(--brand-600) 45%, transparent)`。

替代方案（否决）：JS 端计算衍生色——需要色彩算法库或手写 hex 混合，复杂且不可靠。

### D3. 色板定义（6 个预设，Tailwind 色阶基准）

key 集合即后端校验白名单。琥珀 600 加深一档保证按钮白字对比度 ≥3:1。

| key | 名称 | 700 | 600(主) | 500 | 100 | 50 |
|---|---|---|---|---|---|---|
| `teal` | 青绿（默认） | #0F766E | #0D9488 | #14B8A6 | #CCFBF1 | #F0FDFA |
| `blue` | 蔚蓝 | #1D4ED8 | #2563EB | #3B82F6 | #DBEAFE | #EFF6FF |
| `violet` | 紫罗兰 | #6D28D9 | #7C3AED | #8B5CF6 | #EDE9FE | #F5F3FF |
| `rose` | 玫红 | #BE123C | #E11D48 | #F43F5E | #FFE4E6 | #FFF1F2 |
| `amber` | 琥珀 | #92400E | #B45309 | #D97706 | #FEF3C7 | #FFFBEB |
| `slate` | 石墨 | #334155 | #475569 | #64748B | #E2E8F0 | #F8FAFC |

前后端各自持有一份 key 列表（前端 `src/theme.ts` 渲染色板，后端校验入参）；色值仅前端持有（换肤是纯展示关注点）。

### D4. 数据模型：`user.theme` 列 + `sys_config` 的 `site_theme` 键

- `ALTER TABLE user ADD COLUMN theme VARCHAR(20) NULL`——NULL 表示"跟随站点默认"。选列而非独立偏好表：一对一关系、无扩展字段诉求，独立表过度设计。
- `sys_config` 插入 `site_theme = 'teal'` 初始值（幂等 INSERT），并将 `site_theme` 加入 `getHomePageInfo()`/`updateHomePageInfo()` 的 homeKeys 白名单——`/api/home/site-info` 公开接口与后台 `getHomeConfig`/`updateHomeConfig` 自动带出，无需新接口。
- 迁移脚本 `db/migration-user-theme.sql`：ALTER 用 information_schema + PREPARE 幂等写法（MySQL 无 ADD COLUMN IF NOT EXISTS），INSERT 参考 migration-daily-tip.sql 的 WHERE NOT EXISTS 模式；schema.sql 同步补列与初始键。

### D5. API：仅新增一个接口

- `PUT /api/users/me/theme?theme=<key>`（登录用户，UserController，参照 updateAvatar 模式）：校验 theme ∈ 色板 key 白名单，否则 BusinessException；空串/`follow` 语义可清空（置 NULL，回到跟随站点默认）
- `GET /users/me`：User 实体加 `theme` 字段后自动带出
- 站点默认：复用 `/api/home/site-info`（公开）与 `/api/admin/config/home`（管理），零新增

### D6. 前端模块与应用时机

新增 `src/theme.ts`：色板定义 + `applyTheme(key)` + `resolveAndApply()`。应用时机与优先级：

1. `main.ts` 挂载前：同步应用 `localStorage.theme` 缓存（防首屏闪烁，默认 'teal'）
2. `App.vue` onMounted 异步校正：
   - 已登录 → `userStore.user.theme ?? 站点默认` 
   - 未登录 → `getSiteInfo()` 的 `site_theme`
   - 校正后写回 localStorage
3. 登录成功 / Profile 切换主题 → 立即 `applyTheme` + 持久化

### D7. 两处 UI 入口

- `Profile.vue`：新增「界面配色」卡片——色板圆点网格（选中态描边 + 对勾），点击即 applyTheme + PUT 保存；附「跟随站点默认」选项（对应 theme=NULL）
- `admin/Config.vue`：表单新增「站点默认配色」项，同样的色板选择器，走既有 `updateHomeConfig` 保存；说明文案注明"仅影响未设置个人偏好的用户"

## Risks / Trade-offs

- [color-mix 兼容性：2023 年前旧浏览器不识别] → 项目为现代浏览器个人站点，可接受；降级表现为衍生色丢失（主色仍在），不阻塞核心功能
- [首屏闪烁：CSS 静态默认 teal，用户偏好其它色时挂载后才切换] → main.ts 挂载前同步应用 localStorage 缓存，闪烁窗口 < 1 帧
- [scoped 样式中残留硬编码蓝色（#2563EB 系）导致换肤不彻底] → tasks 中全量 grep 验收：`#2563EB|#1D4ED8|#3B82F6|#EFF6FF|#DBEAFE|rgba(37, ?99, ?235` 必须零残留
- [用户表 ALTER 在大表上锁表] → 个人站点 user 表行数极小，风险可忽略；迁移幂等可重复执行
- [amber 等浅主色按钮白字对比度不足] → 色板已将琥珀 600 加深一档；按钮文字对比度按 3:1（大号/加粗文本标准）验收

## Migration Plan

1. 执行/部署：`migration-user-theme.sql` 加入 prod sql.init（启动自动执行，幂等）；dev 环境沿用现有"sql.init 缩进错误待修"的已知问题，用户手动执行或修复缩进
2. 回滚：删除 `user.theme` 列与 `site_theme` 配置键即可；前端 CSS 静态默认 teal 独立生效，不依赖数据库
