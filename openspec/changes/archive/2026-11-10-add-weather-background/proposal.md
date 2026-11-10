## Why

前台页面当前为静态视觉风格，缺少氛围感与"鲜活"的信息展示。用户希望模仿 macOS 天气应用 Nimbus 的沉浸式体验：动态背景随实时天气（晴/雨/雪/多云/夜晚）切换界面元素（光晕、雨线、雪粒），并自动调整文字颜色保证可读性，让门户首页"活"起来。本变更仅做天气展示与背景切换，最小范围。

## What Changes

- 新增天气数据获取层：前端直调 Open-Meteo 免费 API（无需 key、不碰后端），浏览器 geolocation 取坐标，拒绝/失败时回退默认城市（北京）
- 新增动态天气背景组件：全屏渐变天空底色（随天气类型 + 昼夜变化配色）+ canvas 粒子/动效层（雨线、雪粒、云影飘移、晴天光晕、夜晚星星）
- 新增文字对比度自适应：根据当前背景明暗自动为前台内容区切换深色/浅色文字主题（CSS 变量驱动，全站生效）
- 新增天气信息角标：前台固定角落展示天气图标 + 温度 + 简述，点击展开手动切换面板
- 新增手动天气切换：用户可脱离实时数据手动体验各天气效果（晴/多云/雨/雪/夜晚），可一键恢复"跟随实时"
- 集成到 FrontLayout：所有前台页面共享（登录/注册/后台不受影响）

## Capabilities

### New Capabilities

- `weather-background`: 实时天气获取（Open-Meteo + 定位回退）、动态背景渲染（五类天气视觉方案 + 昼夜变化）、文字对比度自适应、天气角标展示与手动切换交互

### Modified Capabilities

（无——前台布局集成属实现细节，不改既有能力的需求行为）

## Impact

- **前端新增**：`components/weather/WeatherBackground.vue`（背景渲染）、`composables/useWeather.ts`（数据获取 + 状态管理）、天气类型定义与视觉映射配置
- **前端修改**：`layouts/FrontLayout.vue`（挂载背景层与角标）、`App.vue` 或全局样式（对比度自适应 CSS 变量切换）
- **零后端改动**：Open-Meteo 由浏览器直调（https 免费公开 API），无新依赖、无新接口、无数据库变更
- **兼容性**：prefers-reduced-motion 用户降级为静态渐变；canvas 层 pointer-events: none 不影响交互
