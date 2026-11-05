## ADDED Requirements

### Requirement: 设计令牌作为唯一样式来源

系统 SHALL 在全局样式层定义一套 CSS 变量作为全站视觉的唯一来源，覆盖背景、分割线、文字、强调色、语义色、圆角、阴影、间距、字号、行高、容器宽度与动效参数。页面与组件 MUST 引用这些变量，不得在组件内散落硬编码的色值与圆角。

#### Scenario: 令牌集中定义
- **WHEN** 检查全局样式文件
- **THEN** 存在 `:root` 下的令牌定义，且至少包含 `--bg-base`、`--line`、`--text-primary`、`--text-secondary`、`--text-tertiary`、`--accent`、`--r-card`、`--r-control`、`--shadow-card`、`--shadow-hover`、`--container`、`--nav-h`、`--dur`、`--ease`

#### Scenario: 组件遵循令牌
- **WHEN** 检查任一前台页面组件的 scoped 样式
- **THEN** 颜色、圆角、阴影取值来自 CSS 变量，不出现与令牌冲突的硬编码十六进制色值

### Requirement: 浅色主题与单一强调色

系统 SHALL 采用唯一浅色主题，禁止暗色背景与跟随系统暗色。全站 MUST 只使用一个强调色（`#2563EB`），且强调色视觉面积不超过整页 10%，仅用于主按钮、链接悬停、当前选中态与强调短线。系统 MUST NOT 使用渐变背景、玻璃拟态、霓虹发光、彩色大面积填充与多层阴影。

#### Scenario: 无暗色模式
- **WHEN** 在系统设置为暗色模式的浏览器中打开站点
- **THEN** 页面仍为白色/浅灰底色，文字与元素配色不变

#### Scenario: 无装饰性渐变与发光
- **WHEN** 检索样式文件中与页面背景、卡片背景相关的规则
- **THEN** 不存在 `linear-gradient`、`radial-gradient`、`backdrop-filter` 与 `box-shadow` 彩色发光用法

### Requirement: Element Plus 主题覆盖完整

系统 SHALL 覆盖 Element Plus 主题变量，包括 `--el-color-primary` 及 `light-3/light-5/light-7/light-8/light-9`、`dark-2`、`--el-border-radius-base`、`--el-border-color`、`--el-text-color-*`、`--el-box-shadow-light`。覆盖后站点 MUST 不存在 Element Plus 默认蓝 `#409eff` 的可见残留。

#### Scenario: 默认蓝无残留
- **WHEN** 检索前端源码与构建产物中的颜色定义
- **THEN** 不存在 `#409eff` 作为可见样式值（历史文档与注释除外）

#### Scenario: 深浅档同步
- **WHEN** 查看任意 `el-button` 的 hover 与 disabled 态
- **THEN** 呈现的是新强调色的深浅档，不出现默认蓝描边或默认蓝浅底

### Requirement: 前台导航规范

前台顶部导航 SHALL 高度 56px、白色背景、底部 1px 分割线（`#E8EBF0`）、无投影。导航项 MUST 为 14px 次色文字，悬停变为主文字色，当前项以 2px 强调色短线标识，MUST NOT 使用背景色块表示选中。

#### Scenario: 当前项标识
- **WHEN** 用户处于 `/projects` 页面
- **THEN** 「项目」导航项下方显示 2px 强调色短线，且该项文字色为 `#111827`

#### Scenario: 导航无投影
- **WHEN** 页面滚动后观察顶部导航
- **THEN** 导航与内容之间只有 1px 分割线，无阴影浮层效果

### Requirement: 卡片视觉与悬停行为

内容卡片 SHALL 为白底、1px 描边 `#E8EBF0`、阴影 `0 1px 2px rgba(16,24,40,.04)`、圆角 10px。悬停时 MUST 描边变为 `#D3D9E3`、阴影提升为 `0 4px 12px rgba(16,24,40,.06)`，并在 200ms 内上移 2px。卡片 MUST 只有一层阴影，禁止彩色边框与色块分区。

#### Scenario: 悬停反馈
- **WHEN** 鼠标移入任一项目或博客卡片
- **THEN** 卡片在约 200ms 内上移 2px、描边加深、阴影加深，移出后复原

#### Scenario: 单一阴影层
- **WHEN** 检查卡片阴影声明
- **THEN** 每条阴影规则只包含一层（非逗号分隔的多层阴影）

### Requirement: 列表卡片尺寸一致

首页「最近项目」「最近博客」与列表页卡片 SHALL 采用统一结构：封面容器固定 16:9、标题单行省略、摘要固定两行高度、元信息固定高度。同一栅格行内的卡片 MUST 等高。

#### Scenario: 卡片等高
- **WHEN** 首页同时存在项目卡片与博客卡片
- **THEN** 同一行内所有卡片的高度与宽度一致，封面、标题、摘要、时间四段高度差为 0

#### Scenario: 无封面占位
- **WHEN** 某条记录未设置封面
- **THEN** 显示与封面同尺寸的「暂无封面」占位块，卡片布局不塌陷、不拉伸

### Requirement: 表格与后台信息密度

后台表格 SHALL 去除斑马纹，表头背景 `#F7F8FA`，行高不低于 48px，仅保留横向分割线。后台管理页面 MUST 沿用前台同一套令牌与 Element Plus 覆盖，不得出现独立配色。

#### Scenario: 表格无斑马纹
- **WHEN** 打开任一后台列表页（用户/项目/博客/文件）
- **THEN** 数据行背景统一为白色，仅行间有横向分割线，无交替底色

#### Scenario: 后台与前台配色一致
- **WHEN** 对比后台主按钮与前台主按钮
- **THEN** 两者强调色、圆角、高度完全一致

### Requirement: 表单控件规范

输入框、选择器、日期选择器 SHALL 高度 32px、圆角 8px、描边 `#E1E5EB`；聚焦时 MUST 描边变为强调色并附加 `2px` 淡色外环 `rgba(37,99,235,.12)`。按钮 MUST 高度 32px、圆角 8px、无阴影，主按钮实心强调色，次按钮白底描边。

#### Scenario: 聚焦态
- **WHEN** 用户聚焦任一输入框
- **THEN** 描边变为强调色并出现淡色外环，失焦后恢复

#### Scenario: 按钮无阴影
- **WHEN** 检查任一按钮样式
- **THEN** 按钮无投影，主按钮为实心强调色，次按钮为白底 + 1px 描边

### Requirement: 排版与间距体系统一

系统 SHALL 将字号收敛为 12/13/14/16/20/24/32 档，同屏使用不超过 6 种；正文行高 1.7、标题行高 1.3，标题字重 600、正文字重 400。页面间距 MUST 为 8 的倍数（8/12/16/24/32/48/64），容器最大宽度 1200px，左右内边距 24px。

#### Scenario: 间距合规
- **WHEN** 抽查首页卡片间距、卡片内边距与区块间距
- **THEN** 各值均为 8 的倍数，卡片内边距与卡片间距保持一致

#### Scenario: 三级灰层次
- **WHEN** 观察同一卡片内的标题、描述与时间
- **THEN** 三处文字分别使用主/次/弱三级灰，去除颜色后层级依然清晰

### Requirement: 元信息使用等宽字体

数字、编号、时间、标签等元信息 SHALL 使用等宽字体栈（`ui-monospace`、`JetBrains Mono`、`SFMono-Regular` 等），字号比正文小 1–2px，颜色使用弱灰 `#8A93A3` 或更浅。

#### Scenario: 时间与计数呈现
- **WHEN** 查看卡片发布时间、阅读量、列表 ID
- **THEN** 以等宽字体、`#8A93A3` 系颜色、12–13px 呈现，与正文形成区分

### Requirement: 页脚备案信息展示

前台页脚 SHALL 展示备案号「闽ICP备2022007168号-3」、站点域名与审核时间。备案号 MUST 为可点击链接并指向 `https://beian.miit.gov.cn/`，在新标签页打开且带 `rel="noopener noreferrer"`。页脚 MUST 使用浅底 + 1px 上分割线 + 居中弱化排版，不得使用彩色块。

#### Scenario: 备案号可跳转
- **WHEN** 用户点击页脚备案号
- **THEN** 新标签页打开工信部备案查询系统

#### Scenario: 内容少时页脚贴底
- **WHEN** 页面内容高度小于视口高度
- **THEN** 页脚固定在视口底部，内容区与页脚之间无空白断层

### Requirement: 响应式与移动端适配

系统 SHALL 在视口宽度 ≤768px 时把容器左右内边距收敛为 16px，且 MUST NOT 出现横向滚动。导航、卡片栅格与表格 MUST 在 375px 宽度下正常可用。

#### Scenario: 375px 无横向滚动
- **WHEN** 在 375px 宽视口打开首页、列表页与详情页
- **THEN** 页面无横向滚动条，卡片改为单列，文字不溢出

### Requirement: 动效克制

系统 SHALL 将交互动效时长控制在 150–220ms，缓动使用 ease-out 类曲线，且 MUST 只对 `transform` 与 `opacity` 做动画，禁止弹跳、旋转与超过 220ms 的过渡。

#### Scenario: 动效参数合规
- **WHEN** 检查卡片、按钮、导航的过渡声明
- **THEN** 时长处于 150–220ms，属性仅为 `transform`、`opacity` 或颜色

### Requirement: Markdown 内容排版为全局样式

通过 `v-html` 渲染的项目/博客正文 SHALL 使用全局 `.markdown-body` 样式（非 scoped），并 MUST 与设计令牌一致：正文 14–15px、行高 1.7、代码块浅灰底、表格 1px 描边、链接使用强调色。

#### Scenario: 正文样式生效
- **WHEN** 打开一个使用 Markdown 格式的项目详情页
- **THEN** 标题、段落、代码块与表格均呈现新主题排版，代码块为浅灰底无花哨配色

#### Scenario: HTML 格式正文
- **WHEN** 打开一个使用 HTML 正文的项目详情页
- **THEN** 注入的 HTML 同样继承 `.markdown-body` 排版，不出现默认蓝链接与默认字号

### Requirement: 文本对比度可读

系统 SHALL 保证正文文字与背景对比度不低于 4.5:1，弱化文字（时间、占位、辅助说明）不低于 3:1。MUST NOT 使用浅灰文字承载关键业务信息。

#### Scenario: 弱化文字可读
- **WHEN** 检查卡片时间、空状态提示、页脚辅助信息
- **THEN** 其文字色与白色背景对比度不低于 3:1，关键信息（标题、摘要）不低于 4.5:1
