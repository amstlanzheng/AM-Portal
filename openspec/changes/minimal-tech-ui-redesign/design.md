## Context

站点为 Vue 3 + Vite + Element Plus 的 SPA，前台（Home / Projects / ProjectDetail / Blogs / BlogDetail / Login / Register / Profile）与后台（AdminLayout + 8 个管理页）共用同一套 Element Plus 默认皮肤。全局样式只有 `frontend/src/style.css` 一个文件，其中已包含 `.page-container`、`.card-hover`、`.project-cover`、`.cover-placeholder`、`.markdown-body` 等少量自定义规则，其余视觉全部来自 Element Plus 默认值。

约束：

- 只改视觉层，不改业务逻辑、接口契约与数据模型
- 不引入新的 UI 框架；`package.json` 现有依赖中**没有 Tailwind CSS**，提示词中「Vue 3 + Vite + Element Plus + Tailwind CSS」的技术栈假设与现状不符
- Markdown / HTML 正文通过 `v-html` 注入，scoped 样式无法作用于注入节点，必须走全局样式
- 页面中存在大量内联 `style="..."`（如 `margin-bottom:16px`、`color:#999`），改造需兼顾但不要求一次性清理

## Goals / Non-Goals

**Goals:**

- 用一套 CSS 变量令牌取代散落的硬编码值，并让 Element Plus 主题（含 primary 深浅档）与新令牌对齐
- 前台导航、卡片、页脚、列表与详情页形成统一的极简科技风浅色视觉，卡片等高、间距为 8 的倍数
- 后台管理沿用同一套令牌，保持信息密度可用
- 改造完成后可量化验收：无 `#409eff` 残留、无渐变/发光、375px 无横向滚动

**Non-Goals:**

- 不重构组件结构、不抽取通用卡片组件、不改任何接口调用与路由
- 不引入 Tailwind CSS 或其他样式框架
- 不做暗色主题、不做主题切换
- 不重写页面内容（文案、字段展示逻辑保持现状）
- 不改后台各页面的业务表单与校验

## Decisions

### 决策 1：令牌落地在现有 `style.css`，不新增独立 tokens 文件

在 `frontend/src/style.css` 顶部按区块组织：`设计令牌` → `基础重置` → `Element Plus 覆盖` → `公共类` → `Markdown 排版`。

- 理由：全局样式目前只有这一个文件，拆分需改动 `main.ts` 引入顺序，收益低于维护成本；单文件按区块组织同样清晰
- 备选方案：新增 `src/styles/tokens.css` 并 `import`。放弃原因：多一次构建入口变更，且无实际分层收益

### 决策 2：不引入 Tailwind CSS，用令牌 + 少量语义化类替代

提示词假设工程已装 Tailwind，实际没有。若安装 Tailwind v4 需要新增依赖、改 Vite 配置，并可能影响现有 Element Plus 样式优先级。

- 采用：CSS 变量 + 现有语义类（`.page-container`、`.card-hover`、`.project-cover`、`.stat-card` 等）
- 理由：零构建改动、零新增依赖，符合「不引入新 UI 框架」的约束；科技感由 1px 细线、留白、等宽数字营造，不依赖工具类
- 备选方案：安装 Tailwind。放弃原因：与"最小改动"目标冲突，且当前页面数量不多，工具类收益有限

### 决策 3：Element Plus 通过运行时 CSS 变量覆盖，不做 SCSS 主题编译

直接覆盖 `:root` 下的 `--el-color-primary` 及 `light-3/5/7/8/9`、`dark-2`、`--el-border-radius-base`、`--el-border-color(-light)`、`--el-text-color-*`、`--el-box-shadow-light`、`--el-bg-color` 等。

- 理由：`element-plus/dist/index.css` 中的默认值均为 `:root` 变量，同层级后置覆盖即可生效，无需引入 sass 编译链
- 风险控制：按钮 hover/active/disabled、`el-tag`、`el-menu` 选中态依赖深浅档，必须同时覆盖 5 个 light 档与 dark-2
- 备选方案：使用 `@forward 'element-plus/theme-chalk/src/...'` 的 SCSS 主题机制。放弃原因：需新增 sass 依赖与构建配置，改动面远大于收益

### 决策 4：视觉分三层实现，避免样式互相打架

1. **令牌层**：颜色、圆角、阴影、间距、字号、动效
2. **全局覆盖层**：Element Plus 变量 + 组件级微调（`el-card`、`el-button`、`el-table`、`el-tag`、`el-dialog`、`el-input`）
3. **页面 scoped 层**：只写布局与间距（flex/grid/gap/padding），不重复定义颜色与圆角

- 理由：把颜色收敛到令牌层后，"调一次全站生效"，也便于用 grep 校验合规性

### 决策 5：导航与页脚按 token 重做，结构不变

- `FrontLayout` 导航高度 56px，`el-menu` 去下边框，选中态用 `:deep(.el-menu-item.is-active)` 的 `::after` 画 2px 强调短线，去掉默认的底部条与背景块
- 页脚沿用现有备案信息 DOM（备案号链接、域名、审核时间），仅替换为令牌化配色：浅底 `#FBFCFD` + 1px 上分割线 + 三级灰文字
- `AdminLayout` 侧边栏由现有深色（`#304156`）改为白底 + 1px 右边框，选中项浅灰底 + 左侧 2px 强调线；同时移除 `el-menu` 上的内联属性 `background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff"`（这是 `#409eff` 的实际残留点之一），改为由 CSS 变量驱动

### 决策 6：卡片等高沿用"固定容器 + 固定段落高度"

保持首页/列表页现有结构：封面容器 `aspect-ratio: 16/9`，标题单行 `ellipsis`，摘要 `-webkit-line-clamp: 2` + `min-height`，元信息固定 `min-height`。仅把圆角、描边、阴影、悬停位移替换为令牌值，悬停位移由 -4px 改为 -2px、时长 300ms 改为 200ms。

- 理由：已验证等高方案有效，不做结构性重构

### 决策 7：Markdown 排版在全局 `.markdown-body` 内令牌化

正文 14px / 行高 1.7；`h1`/`h2` 去底部粗边框改为 1px `#E8EBF0`；代码块背景 `#F7F8FA`、圆角 8px；链接使用强调色；表格 1px `#E8EBF0` 描边 + 表头 `#F7F8FA`。同时移除行内 `code` 的粉红色（`#d63384`），改为主文字色 + 浅灰底，避免出现第二种强调色。

### 决策 8：内联样式就地替换，不做大规模重构

页面中形如 `style="color:#999"`、`style="margin-bottom:16px"` 的内联样式，在触及该页面时替换为语义类或令牌值；未触及的保持原样，不为了统一而全量重写。

- 理由：控制改动面，降低回归风险

### 决策 9：验收方式

- 静态：`npx vue-tsc --noEmit`、`npm run build`，并 grep 校验 `#409eff`、`linear-gradient`、`backdrop-filter` 无残留
- 动态：本地起后端 + 前端，浏览器实测首页 / 项目列表 / 项目详情 / 博客列表 / 博客详情 / 登录 / 后台仪表盘与列表页，检查卡片等高、导航选中态、页脚贴底、375px 无横向滚动、控制台无报错

## Risks / Trade-offs

- [Element Plus 深浅档漏覆盖导致按钮 hover 露出默认蓝] → 一次性覆盖 5 个 light 档与 dark-2，构建后 grep `#409eff` 校验
- [后台表格因加大行高与内边距导致一屏信息量下降] → 表格行高固定 48px、操作列保持 `nowrap`，不放大字号
- [全局覆盖 `el-card` 内边距影响现有页面高度与滚动位置] → 逐页浏览器实测，必要时对特定页面用 scoped 覆盖而非全局
- [内联样式与新令牌并存造成局部视觉不一致] → 优先替换首页与列表页的内联色值，其余页面按触及即改
- [提示词假设 Tailwind，实际未安装，可能与用户预期不一致] → 已在 proposal 中显式声明，若用户坚持引入 Tailwind 需另开变更
- [纯样式改动仍需重建前端镜像才能生效] → 部署时执行 `docker compose up -d --build frontend`

## Migration Plan

1. 先在独立分支完成令牌层与 Element Plus 覆盖，构建验证无报错
2. 按「前台布局 → 前台页面 → 后台布局 → 后台页面」顺序改造，每批完成后跑一次类型检查与构建
3. 本地起前后端做浏览器实测，记录卡片尺寸、导航选中态、页脚位置
4. 合并后部署：服务器 `git pull` + `docker compose up -d --build frontend`
5. 回滚策略：本次全部为前端样式文件改动，`git revert` 对应提交并重新构建前端镜像即可；不涉及数据库与接口，无数据回滚需求

## Open Questions

- 强调色最终取 `#2563EB`（钴蓝）还是 `#0D9488`（青）？当前按 `#2563EB` 设计，与现有蓝色系衔接更自然
- AI 悬浮窗（`components/AiChat.vue`）是否也完全套用新令牌，或保留其独立品牌视觉？
- 后台侧边栏是否确认由深色改为浅色（本文按浅色设计）？
