## 1. 数据层与迁移

- [x] 1.1 创建 `project-web/src/main/resources/db/migration-user-theme.sql`：user 表幂等加 `theme VARCHAR(20) NULL` 列（information_schema + PREPARE）；sys_config 幂等插入 `site_theme = 'teal'` 初始键（WHERE NOT EXISTS 模式）
- [x] 1.2 `schema.sql` 同步：user 表补 theme 列定义、sys_config 初始化补 site_theme 键
- [x] 1.3 `application-prod.yml` 的 sql.init.schema-locations 追加 migration-user-theme.sql
- [x] 1.4 `User` 实体新增 `private String theme;` 字段

## 2. 后端接口

- [x] 2.1 `SysConfigService.getHomePageInfo()` 与 `updateHomePageInfo()` 的 homeKeys 白名单加入 `site_theme`
- [x] 2.2 `UserController` 新增 `PUT /api/users/me/theme`：@RequestParam theme，白名单校验（teal/blue/violet/rose/amber/slate，空值合法=清除偏好），非法值抛 BusinessException；`UserService` 新增 updateTheme（空串置 NULL，LambdaUpdateWrapper 显式 set null）
- [x] 2.3 后端编译验证：`mvn -pl project-web -am compile -DskipTests` 通过

## 3. 前端主题模块

- [x] 3.1 新建 `frontend/src/theme.ts`：6 主题色板定义（design D3 色值表）、`applyTheme(key)`（覆盖 --brand-700/600/500/100/50 + data-theme 标记 + localStorage 缓存）、`THEME_KEYS` 导出
- [x] 3.2 `style.css`：`:root` 品牌色阶默认值改为青绿系；EP 衍生色改 color-mix 派生（D2 公式）；`--shadow-accent` 改 color-mix
- [x] 3.3 全量排查并替换硬编码蓝色：grep `#2563EB|#1D4ED8|#3B82F6|#EFF6FF|#DBEAFE|rgba\(37, ?99, ?235`（含各页面 scoped 样式与 tokens.css 备份外的所有前端源码），替换为 var(--brand-*) 或语义令牌，替换后 grep 零残留
- [x] 3.4 `main.ts` 挂载前同步 applyTheme(localStorage.theme || 'teal')；`App.vue` onMounted 异步校正（登录→user.theme ?? site_theme；未登录→getSiteInfo 的 site_theme）并更新缓存
- [x] 3.5 `api/index.ts` 新增 `updateMyTheme(theme: string)`

## 4. UI 入口

- [x] 4.1 `Profile.vue` 新增「界面配色」卡片：色板圆点网格（选中态描边+对勾）+「跟随站点默认」选项；点击即 applyTheme + PUT 保存，失败回滚并提示
- [x] 4.2 `admin/Config.vue` 新增「站点默认配色」表单项：同款色板选择器，随既有保存流程（updateHomeConfig）提交，附说明「仅影响未设置个人偏好的用户」

## 5. 验证与交付

- [x] 5.1 前端构建验证：`npm run build` 通过（vue-tsc 无类型错误）
- [ ] 5.2 本地起服实测：默认青绿渲染；Profile 切 6 主题逐个验证即时生效与 EP 联动；刷新后主题保持；「跟随站点默认」生效；后台改站点默认后未设置偏好用户可见（前端部分已浏览器实测通过：默认 teal、玫红切换全站联动、color-mix 派生、刷新保持、失败回滚；「跟随站点默认」「后台改站点默认」需后端环境，部署后验证）
- [ ] 5.3 验证非法主题值被拒（直接调 PUT /api/users/me/theme?theme=orange 返回业务错误）（本机无 MySQL/Redis/Docker，需部署后验证）
- [ ] 5.4 多用户隔离验证：甲设偏好、乙不受影响（乙跟随站点默认）（需后端环境，部署后验证）
- [x] 5.5 打包：后端 `mvn -pl project-web -am package -DskipTests`（BUILD SUCCESS，project-web-1.0.0.jar 90.7MB）+ 前端 `npm run build`（通过），给出需手动执行的 SQL 与部署命令
