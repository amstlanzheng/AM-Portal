# 工具页面技术设计

## Context

- 前端：Vue 3 + TS + Element Plus + Vite，主题系统（`--brand-*` CSS 变量）已支持用户级换肤，页面须全部走语义令牌
- 后端：Spring Boot 多模块，公开接口统一挂 `GET /api/home/**`（SecurityConfig 已 permitAll，无需改动）
- 现有依赖已覆盖本次所需：`markdown-it`（MD 转公众号）、`highlight.js`（JSON 高亮可选）、Canvas（图片压缩/格式转换）、`crypto.getRandomValues`（密码生成）
- 需求文档：`doc/v1.03/工具页面.md`（设计原则：零后端、隐私优先、三段式、结果可带走）

## Goals / Non-Goals

**Goals:**
- 12 个工具页 + 2 个导航页，全部纯前端本地计算（MBTI 取题除外）
- MBTI 题库与结果类型入库，后台可增删改
- 样式 100% 走主题令牌，换肤即时联动
- 不新增任何 npm / Maven 依赖

**Non-Goals:**
- 不做分享图片卡片（统一用「复制成绩文本」替代，P2 再升级）
- 不做 zip 打包下载（图片压缩多选时逐张下载）
- 不做排行榜/成绩持久化（趣味工具结果仅本地展示）
- 不做编码转换、CSV 高级方言（格式转换 P2）

## Decisions

### D1. 路由与文件组织
- 静态路由：`/tools`、`/fun` 两个导航页 + `/tools/:name`、`/fun/:name` 12 个工具页（每个工具独立组件，便于维护与懒加载）
- 文件：`views/tools/ToolsIndex.vue`、`views/tools/FunIndex.vue`、`views/tools/<tool>.vue`（kebab-case）
- 复用 FrontLayout，主导航加「工具」「趣味」两项
- 工具元数据（名称/介绍/图标/路由名）集中定义在 `views/tools/toolMeta.ts`，导航页卡片与路由标题共用，避免两处维护

### D2. 统一三段式骨架
新建 `components/tools/ToolPageLayout.vue`：props 为 `title / intro / usage`（介绍区）+ 两个插槽（操作区 `#panel`、结果区 `#result`），底部固定渲染隐私小字与免责声明。所有 12 个工具页复用，保证结构一致。

### D3. 结果可带走（复制/下载统一封装）
新建 `composables/useCopy.ts`：封装「已复制 ✓ 2 秒还原」交互（文档要求的统一反馈）。下载用原生 `URL.createObjectURL + a[download]`。趣味工具的"分享卡片"统一降级为复制成绩文本。

### D4. 各工具实现要点（零依赖）
| 工具 | 方案 |
|---|---|
| 热量计算 | Mifflin-St Jeor 公式直算；输入非法禁用按钮 |
| 图片压缩 | `Canvas.drawImage + toBlob(quality)`；jpg/png/webp；多选逐张处理显示进度 |
| 格式转换 | 图片 Canvas 互转；JSON↔CSV 手写递归（转义逗号/引号/换行） |
| JSON 格式化 | `JSON.parse/stringify`；捕获异常展示行列位置（解析 error message 提取 position） |
| MD 转公众号 | `markdown-it` 渲染 → 遍历 DOM 注入内联样式（2-3 套主题预设为 JS 对象）→ `ClipboardItem` 写富文本 |
| 密码生成 | `crypto.getRandomValues` 拒绝采样；保证每勾选类至少 1 字符；强度按长度+字符集估档 |
| 颜色选择器 | 原生 `input[type=color]` + 手写 HEX/RGB/HSL 互转；配色用 HSL 色轮偏移（互补/邻近/明暗变体） |
| 时间转换 | 秒/毫秒按位数自动识别；`Intl.DateTimeFormat` 出多时区格式；相对时间手写 |
| 打字测试 | 内置固定语料（英/中/代码各 3 段）；逐字符对比 + 错误高亮；WPM/CPM/准确率 |
| 反应力 | 随机延迟 1-4s 防预判；提前点击判犯规；5 次取平均 + 评级文案 |
| 点击计数器 | 点击累加 + 倒计时；CPS 计算；移动端 touchstart |
| MBTI 测试 | 见 D5 |

### D5. MBTI 变体测试（唯一有后端的部分）
**表设计**（两表，迁移脚本幂等）：
```sql
quiz_question(
  id BIGINT PK AUTO,
  dimension VARCHAR(10) COMMENT '主维度字母(E/I/N/S/T/F/J/P)，用于后台分组展示',
  question_text VARCHAR(500) NOT NULL,
  options JSON NOT NULL COMMENT '[{"text":"...","scores":{"N":1,"T":2}}]',
  sort_order INT DEFAULT 0,
  is_enabled TINYINT(1) DEFAULT 1,
  deleted INT DEFAULT 0,
  create_time DATETIME
)
quiz_result_type(
  id BIGINT PK AUTO,
  code VARCHAR(10) NOT NULL UNIQUE COMMENT '结果代号，两字母组合如 NT/NF/ST/SF/SP/SJ/NP/NJ',
  name VARCHAR(50) NOT NULL COMMENT '自定义命名',
  description VARCHAR(1000) COMMENT '类型描述(约100字)',
  sort_order INT, deleted, create_time
)
```
**计分规则**：8 个字母维度独立累计（选项 scores 可同时给多字母加权），取总分最高的 2 个字母组成 code，匹配 `quiz_result_type`；同分时按 E>N>T>J 字母表权重破平。**计分纯前端完成**（题目与分值随接口下发）。

**接口**（全部复用 `/api/home/**` permitAll + `/api/admin/**` 既有规则，SecurityConfig 零改动）：
- `GET /api/home/quiz/questions` → 启用题目按 sort_order（不含答案语义，公开安全）
- `GET /api/home/quiz/result-types` → 全部启用结果类型
- `GET /api/admin/quiz/questions`（分页）/ `POST /api/admin/quiz/questions` / `PUT /api/admin/quiz/questions` / `DELETE /api/admin/quiz/questions/{id}`
- `GET /api/admin/quiz/result-types` / `POST` / `PUT` / `DELETE /{id}`

**初始数据**：10 道题（工作方式/沟通方式/决策方式等场景）+ 8 种结果类型（NT/NF/ST/SF/SP/SJ/NP/NJ）。

**后台页面**：`views/admin/Quiz.vue`，两个 Tab（题目管理 / 结果类型管理），题目编辑用动态选项行（文本 + 字母分值键值对），复用既有 admin 页面风格。

### D6. 主题兼容
所有颜色只用 `var(--brand-*)`、`var(--color-text-*)`、`var(--bg-*)`、`var(--border-*)` 等现有语义令牌；趣味工具的"变绿"等状态色用语义色变量。验收标准同 UI 改版：切主题全站联动零残留。

## Risks / Trade-offs

- [Canvas 压缩 webp 输出兼容性] → 特性检测 `toBlob('image/webp')`，不支持时回退 jpg 并提示
- [MD 转公众号富文本复制兼容性] → `ClipboardItem` 不支持时回退"复制 HTML 源码"并提示手动粘贴
- [打字测试移动端输入法体验] → MVP 桌面为主，移动端可用但体验一般（P2 优化）
- [题库 JSON 无 schema 校验，管理员可能存错] → 后台表单按结构化字段编辑（选项行 + 分值键值对），提交时序列化，杜绝手写 JSON
- [批量新增代码量] → 12 个工具页一次交付，按 tasks 分批构建验证，每批 `vue-tsc` + build 通过再继续

## Migration Plan

1. 执行 `migration-quiz.sql`（建两张表 + 初始数据，幂等；prod 亦可启动自动执行）
2. 部署新前后端镜像
3. 回滚：还原上一版镜像即可，两表对旧版本无感知

## Open Questions

（无阻塞项——分享图片卡片与 zip 打包已明确列入 Non-Goals，如需升级另行提案）
