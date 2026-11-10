## Context

前台由 `FrontLayout.vue` 承载（首页/项目/博客/工具/趣味/登录/注册/个人资料均为其子路由），布局为 sticky 顶栏 + 内容区 + 页脚，内容多置于 `--surface` 卡片上，页面底色为浅色纯色。项目有成熟 CSS 语义令牌体系（`--brand-*`、`--color-text-*`、`--surface*`），品牌类颜色禁止硬编码。用户希望引入 Nimbus 式天气动态背景：实时天气驱动全屏动态视觉 + 文字对比度自适应，范围最小化（不要天气页面、不要预报、不碰后端）。

既有约束：
- 后端零改动（Open-Meteo 浏览器直调，https 公开免费 API）
- 站点部署在 http 也可能存在（Open-Meteo 支持 http/https，混合内容风险低；若站点为 https 则直调无问题）
- `AiChat` 悬浮窗与回到顶部按钮为 fixed 高 z-index 元素，不受背景层影响

## Goals / Non-Goals

**Goals:**
- 全屏动态天气背景：渐变天空底色 + canvas 动效层，五种场景（晴/多云/雨/雪/夜）+ 昼夜变量
- 实时天气获取：浏览器定位 → Open-Meteo current API（温度 + WMO weather_code + is_day）
- 文字对比度自适应：暗场景（夜/雨夜）下，落在背景上的文字（顶栏/页脚）转浅色、表面转半透明玻璃拟态
- 天气角标：图标 + 温度 + 天气简述；点击展开手动切换面板（跟随实时/晴/云/雨/雪/夜）
- 手动选择 localStorage 持久化，一键恢复自动

**Non-Goals:**
- 不做城市名反解、天气预报、逐小时趋势（仅 current）
- 不做后台管理配置项（不新增 sys_config）
- 不改后端任何代码/接口/数据库
- 不覆盖 Element Plus 弹层体系（dialog/dropdown 等仍用白底）
- 不做天气背景的用户级偏好持久化到服务端

## Decisions

**D1 数据源：Open-Meteo current API 前端直调**
`https://api.open-meteo.com/v1/forecast?latitude=&longitude=&current=temperature_2m,weather_code,is_day&timezone=auto`
- 免费、无 key、CORS 开放，浏览器可直调 → 零后端改动
- 备选：后端代理（多一个接口与外部依赖管理，收益仅"可换源"，当前无此需求）；wttr.in（格式杂）；和风/心知（要 key，违反最小改动）
- WMO weather_code → 场景映射：0+is_day=晴、0+夜=夜、1-3/45/48=多云、51-67/80-82/95-99=雨、71-77/85-86=雪

**D2 定位策略：geolocation 优先，静默回退北京**
`getCurrentPosition` 超时 5s；拒绝/失败/超时 → 使用默认坐标（39.9042, 116.4074）继续请求天气，不打断主流程、不弹错误。不引入 IP 定位三方服务（多一层依赖）。

**D3 状态管理：`composables/useWeather.ts` 模块级单例**
导出响应式状态：`scene`（sunny/cloudy/rainy/snowy/night）、`isDay`、`temperature`、`desc`、`mode`（auto/manual）、`error`、`loading`。模块级单例而非 pinia store（无跨组件持久化诉求，FrontLayout 是唯一消费者族；组件内 composable 足够）。30 分钟定时刷新 + `visibilitychange` 回到可见时若数据超 30 分钟则刷新。

**D4 渲染架构：`WeatherBackground.vue` = 渐变层 + canvas 粒子层**
- 结构：`position: fixed; inset: 0; z-index: 0; pointer-events: none`，FrontLayout 现有子元素（header/main/footer）提 `position: relative; z-index: 1`
- 渐变层：每场景一组 CSS 渐变（div，transition 1.2s crossfade 切换）；夜/雨夜为暗色系
- 粒子层：单 `<canvas>` + rAF 循环，按场景切换粒子系统：晴天=漂浮光尘+顶部光晕、多云=慢速漂移云团（模糊椭圆）、雨=斜向雨线（速度/长度/透明度随机）、雪=正弦摇摆雪花（大小/速度分层）、夜=闪烁星星+月晕
- 工程细节：DPR 缩放、resize 防抖重置、`visibilitychange` 暂停/恢复 rAF、组件卸载清理
- `prefers-reduced-motion: reduce` → 不启动 rAF，仅静态渐变（媒体查询 + JS 双保险）
- 性能预算：粒子数上限（雨 140 / 雪 120 / 星 90 / 云 6 / 尘 40），低配设备掉帧不影响交互（纯装饰层）

**D5 文字自适应：front-layout 根节点绑 `data-wb-theme`（light/dark）**
- useWeather 按 `scene + isDay` 计算 theme：night 必 dark；rainy 夜 dark；其余 light
- FrontLayout 模板绑定 `:data-wb-theme="wbTheme"`，scoped 样式内按 `[data-wb-theme='dark']` 覆写：顶栏/页脚背景转深色半透明 + 文字转浅色；`.site-main` 直传子页面的区域不加覆写（子页面卡片自带 `--surface` 底）
- 内容卡片玻璃拟态：dark 主题下 `--surface` 语义在 front-layout 作用域内覆写为半透明白 + backdrop-blur（scoped + :deep 有限使用），亮色主题保持现状 → 满足"文字颜色适应天气背景"且不破坏全站令牌体系
- 备选：全局换肤（改 root 变量）——影响后台与弹层，越界，否

**D6 角标与手动切换：`WeatherBadge.vue` 固定左下**
- 左下 fixed（避让右下回到顶部/AiChat）；毛玻璃小胶囊：图标 + 温度 + 简述；点击展开 popover 面板：六选项（跟随实时/晴/多云/雨/雪/夜）
- 手动模式写 `localStorage('wb-manual-scene')`；`跟随实时` 清除并立即重新取数
- 定位/请求失败：角标显示"天气不可用"，面板内提供重试；背景回退日间晴场景
- 图标：自绘 SVG 五枚（晴=太阳、云、雨=云+雨线、雪=云+雪晶、夜=月+星），不引新依赖

**D7 集成点：仅改 FrontLayout.vue**
挂 `<WeatherBackground />` 与 `<WeatherBadge />`，绑 data-wb-theme，子元素 z-index 提升。登录/注册是 FrontLayout 子路由（自然共享背景，属预期）；AdminLayout 独立不受影响。

## Risks / Trade-offs

- [Open-Meteo 不可达/被墙] → 静默回退默认场景 + 角标"天气不可用"+重试；不影响站点其他功能（纯装饰增强）
- [geolocation 首次授权弹窗打扰] → 超时 5s 静默回退默认坐标；授权与否都有天气可看
- [暗场景下个别子页面卡片对比度不足] → 卡片玻璃拟态仅 dark 生效且保留白底 ≥72% 不透明度；验收时逐页检查主前台页面
- [canvas 常驻 rAF 的功耗] → 粒子上限收紧 + 页面不可见即暂停 + reduced-motion 降级
- [Open-Meteo 变更 API] → 单点封装在 useWeather 内（fetchWeather 一处），替换成本为 1 个函数
- [手动模式与实时冲突] → 手动优先级高于自动（明确语义：手动=覆盖展示，恢复=回到自动并刷新）

## Migration Plan

纯前端增量，无数据迁移：合入即生效；回滚 = revert 单次提交。部署顺序无要求（前端独立发版）。

## Open Questions

（无——三项关键决策已由用户默认项确定：前端直调 / 前台全站 / 自动+手动）
