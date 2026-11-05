## 1. 设计令牌与全局基础样式

- [x] 1.1 在 `frontend/src/style.css` 顶部新增「设计令牌」区块，定义 `--bg-base`、`--bg-subtle`、`--bg-muted`、`--line`、`--line-strong`、`--line-input`、`--text-primary/secondary/tertiary/disabled`、`--accent`/`--accent-hover`/`--accent-ring`、语义色、圆角、阴影、间距、字号、行高、容器宽度、动效参数
- [x] 1.2 重写基础重置：`body` 背景改 `--bg-base`、正文 14px/1.7、颜色 `--text-primary`、字体栈加入 Inter/思源黑体
- [x] 1.3 更新公共类：`.page-container` 最大宽 1200px、内边距 24px（≤768px 为 16px）；`a` 颜色改 `--accent`
- [x] 1.4 重写 `.card-hover`：位移由 -4px 改 -2px、时长 300ms 改 200ms、阴影用 `--shadow-hover`、缓动用 `--ease`
- [x] 1.5 更新 `.project-cover` 与 `.cover-placeholder` 的圆角与底色为令牌值（`--r-tag`/`--bg-muted`）

## 2. Element Plus 主题覆盖

- [x] 2.1 新增「Element Plus 覆盖」区块，覆盖 `--el-color-primary` 及 `light-3/5/7/8/9`、`dark-2`
- [x] 2.2 覆盖 `--el-border-radius-base`、`--el-border-color`、`--el-border-color-light`、`--el-text-color-primary/regular/secondary`、`--el-box-shadow-light`、`--el-bg-color`
- [x] 2.3 组件级微调：`el-card` 去阴影改 1px 描边 + 圆角 10px + header 底部 1px 分割线
- [x] 2.4 组件级微调：`el-button` 高度 32px、圆角 8px、无阴影；主按钮实心、默认按钮白底描边
- [x] 2.5 组件级微调：`el-input`/`el-select` 高度 32px、描边 `--line-input`、聚焦时强调色描边 + `--accent-ring` 外环
- [x] 2.6 组件级微调：`el-tag` 改浅灰底 + 1px 描边，去除彩色实心样式
- [x] 2.7 组件级微调：`el-table` 去斑马纹、表头 `--bg-muted`、行高 ≥48px、仅横向分割线
- [x] 2.8 组件级微调：`el-dialog` 圆角 12px、去边框、遮罩 `rgba(17,24,39,.32)`

## 3. 前台布局（导航与页脚）

- [x] 3.1 `layouts/FrontLayout.vue` 导航高度 56px、白底、底部 1px 分割线、移除投影
- [x] 3.2 导航项 14px 次色，悬停变主文字色；选中态改为 2px 强调短线（`:deep(.el-menu-item.is-active)::after`），去除背景块与默认底边
- [x] 3.3 页脚配色令牌化：浅底 `--bg-subtle` + 1px 上分割线 + 三级灰文字，保留备案号链接（`https://beian.miit.gov.cn/`）、域名与审核时间
- [x] 3.4 校验内容不足一屏时页脚贴底（`.front-layout` flex column + `.main { flex: 1 }` 保持生效）

## 4. 前台页面

- [x] 4.1 `views/Home.vue`：个人资料卡与「最近项目/最近博客」卡片套用新卡片令牌，标题/摘要/元信息三级灰与等宽字体
- [x] 4.2 `views/Home.vue`：移除内联硬编码色值（`#999`/`#bbb` 等），替换为令牌变量或语义类
- [x] 4.3 `views/Projects.vue`：卡片描边/圆角/阴影/悬停按新规范；封面 16:9 与占位保持等高
- [x] 4.4 `views/ProjectDetail.vue`：标题层级、按钮组、标签与正文区套用令牌；链接按钮去掉阴影
- [x] 4.5 `views/Blogs.vue`：列表卡片按新规范；封面 200×120 容器圆角与占位色令牌化
- [x] 4.6 `views/BlogDetail.vue`：标题与元信息排版套用令牌，正文继承 `.markdown-body`
- [x] 4.7 `views/Login.vue`、`views/Register.vue`：表单容器居中卡片化，输入框与按钮高度 32px、圆角 8px
- [x] 4.8 `views/Profile.vue`：表单与卡片套用令牌，去掉与主题冲突的硬编码色值

## 5. 后台布局与页面

- [x] 5.1 `layouts/AdminLayout.vue`：侧边栏由深色改白底 + 1px 右边框，宽 220px（收起 64px），logo 区文字改主文字色
- [x] 5.2 `layouts/AdminLayout.vue`：移除 `el-menu` 内联属性 `background-color/text-color/active-text-color="#409eff"`，改由 CSS 驱动；选中项浅灰底 + 左侧 2px 强调线
- [x] 5.3 `layouts/AdminLayout.vue`：顶栏去投影改 1px 下分割线，高度 56px
- [x] 5.4 `views/admin/Dashboard.vue`：统计卡片改用统一卡片样式与等宽数字
- [x] 5.5 `views/admin/*` 列表页（Users/Projects/Blogs/Files/Prompts/SensitiveWords）：确认表格、按钮、标签、分页套用统一皮肤，操作列保持不换行
- [x] 5.6 `views/admin/Config.vue`：表单控件与卡片套用统一皮肤

## 6. Markdown / HTML 正文排版

- [x] 6.1 `.markdown-body` 正文改 14px/1.7、颜色 `--text-primary`；标题 600 字重、行高 1.3
- [x] 6.2 标题/h2 底部边框改 1px `--line`；`hr` 改 `--line`
- [x] 6.3 行内 `code` 去除粉色（`#d63384`），改主文字色 + `--bg-muted` 底、圆角 6px
- [x] 6.4 `pre` 背景 `--bg-muted`、圆角 8px、1px 描边；表格 1px `--line` 描边 + 表头 `--bg-muted`
- [x] 6.5 正文链接改 `--accent`，`img` 圆角 8px；`blockquote` 左侧 2px 强调线 + 浅灰底

## 7. 响应式与可访问性

- [x] 7.1 增加 ≤768px 断点：`.page-container` 内边距 16px，首页/列表栅格改单列（`el-col` span 24）
- [x] 7.2 校验 375px 宽下首页、项目/博客列表与详情页无横向滚动
- [x] 7.3 校验文字对比度：正文 ≥4.5:1，弱化文字 ≥3:1，必要时调深浅灰档位

## 8. 验证与交付

- [x] 8.1 运行 `npx vue-tsc --noEmit` 与 `npm run build`，确认退出码为 0
- [x] 8.2 grep 校验源码中无 `#409eff` 可见残留、无 `linear-gradient`/`backdrop-filter`/`radial-gradient`
- [ ] 8.3 本地启动前后端，浏览器实测首页、项目列表/详情、博客列表/详情、登录、后台仪表盘与一个列表页，确认 console 无报错
- [x] 8.4 实测记录：首页同行卡片尺寸一致、导航选中短线正常、页脚贴底、后台表格无斑马纹
- [ ] 8.5 构建前端产物并给出部署命令（`docker compose up -d --build frontend`）
