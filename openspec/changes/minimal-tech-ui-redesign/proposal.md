## Why

线上站点视觉仍是 Element Plus 出厂默认皮肤：主色 `#409eff`、默认卡片阴影与圆角、默认按钮与标签样式，元素之间缺乏视觉层级，整体观感接近脚手架而非成品。在不改动任何业务逻辑与接口的前提下，需要统一重构前端视觉，形成克制、精确的极简科技风浅色主题。

## What Changes

- 建立统一设计令牌层（CSS 变量）：背景三级、分割线三级、文字三级灰、单一强调色、圆角、阴影、间距、字号、动效时长与缓动
- 覆盖 Element Plus 主题变量：`--el-color-primary` 及其 3/5/7/8/9 深浅档、圆角、边框色、文字色、阴影，确保全站无 `#409eff` 残留
- 重构前台布局骨架：导航 56px 高 + 底部 1px 分割线 + 当前项下方 2px 强调短线（不再使用背景块）；页脚备案信息保留并套用新令牌
- 统一卡片视觉：白底 + 1px 描边 + 单层微阴影 + 圆角 10px；悬停描边加深并上移 2px（200ms）
- 统一排版与间距：字号收敛为 12/13/14/16/20/24/32 六档（实际使用不超过 6 种），正文行高 1.7、标题 1.3，间距严格 8 的倍数
- 统一组件皮肤：按钮 32px 高圆角 8px 无阴影、输入框 32px 高聚焦时强调色描边 + 淡色外环、`el-tag` 改描边样式、表格去斑马纹且仅保留横向分割线、弹窗圆角 12px
- 后台管理页面沿用同一套令牌与组件皮肤，仅调整信息密度
- 明确不引入 Tailwind CSS：当前工程未安装该依赖，且约束要求"不引入新的 UI 框架"；科技感通过 CSS 变量、1px 细线、等宽数字与极淡点阵底纹实现

**BREAKING**：无。仅视觉层变更，不改接口契约、数据模型与业务规则。

## Capabilities

### New Capabilities

- `visual-design-system`：全局设计令牌清单、Element Plus 主题覆盖规则、导航/页脚/卡片/表格/表单/弹窗的统一视觉规范、响应式断点与可访问性（对比度）要求

### Modified Capabilities

无。`home-page`、`project-management`、`blog-system`、`user-system` 等既有能力的业务行为与接口不变，本次仅改变其前端呈现。

## Impact

- **前端样式层**：`frontend/src/style.css`（或新增 tokens 文件并引入 `main.ts`）
- **前台布局与页面**：`layouts/FrontLayout.vue`、`views/Home.vue`、`views/Projects.vue`、`views/ProjectDetail.vue`、`views/Blogs.vue`、`views/BlogDetail.vue`、`views/Login.vue`、`views/Register.vue`、`views/Profile.vue`
- **后台布局与页面**：`layouts/AdminLayout.vue`、`views/admin/*`（Dashboard、Projects、Blogs、Users、Files、Prompts、SensitiveWords、Config）
- **公共组件**：`components/AiChat.vue`
- **不涉及**：后端模块、数据库、接口契约、`package.json` 依赖（不新增 Tailwind 或其他 UI 框架）
- **风险点**：Element Plus 深浅档未同步会出现按钮/标签"蓝边"；`v-html` 注入的 Markdown 内容需全局样式而非 scoped 样式；后台表格信息密度高，需避免过度留白影响可用性
