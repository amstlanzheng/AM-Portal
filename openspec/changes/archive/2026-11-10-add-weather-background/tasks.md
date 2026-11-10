## 1. 数据层（useWeather）

- [x] 1.1 创建 `frontend/src/composables/useWeather.ts`：模块级单例状态（scene/isDay/temperature/desc/mode/error/loading）+ WMO weather_code 场景映射（0+is_day=晴、0+夜=夜、1-3/45/48=多云、51-67/80-82/95-99=雨、71-77/85-86=雪）+ desc 中文文案
- [x] 1.2 实现 geolocation 获取（超时 5s，失败/拒绝静默回退北京 39.9042,116.4074）→ Open-Meteo current API 请求（temperature_2m/weather_code/is_day/timezone=auto）→ 状态更新；请求失败置 error 并回退默认场景（日间晴）
- [x] 1.3 实现刷新策略：30 分钟定时刷新 + visibilitychange 恢复可见且数据超 30 分钟时刷新；手动模式（localStorage `wb-manual-scene`）优先于自动，提供 setManualScene/resetToAuto（清除后立即重新取数）；计算并导出 wbTheme（night/雨夜=dark，其余 light）

## 2. 背景渲染（WeatherBackground）

- [x] 2.1 创建 `frontend/src/components/weather/WeatherBackground.vue`：fixed 全屏层（pointer-events:none, z-index:0）+ 五场景渐变底色（div crossfade 1.2s 过渡，夜/雨夜暗色系）+ 组件卸载清理
- [x] 2.2 实现 canvas 粒子引擎：rAF 循环 + DPR 缩放 + resize 防抖 + visibilitychange 暂停/恢复；五套粒子系统（晴=光尘+顶部光晕、云=漂移云团、雨=斜向雨线、雪=正弦摇摆雪花、夜=闪烁星星+月晕），粒子上限（雨140/雪120/星90/云6/尘40）
- [x] 2.3 实现 prefers-reduced-motion 降级（媒体查询 + JS 双保险：不启动 rAF，仅静态渐变）

## 3. 角标与切换面板（WeatherBadge）

- [x] 3.1 创建 `frontend/src/components/weather/WeatherBadge.vue`：左下 fixed 毛玻璃胶囊（自绘 SVG 五枚图标 + 温度 + 简述；error 态显示"天气不可用"）；点击展开面板：跟随实时/晴/云/雨/雪/夜 六选项 + 手动选择高亮 + 重试入口
- [x] 3.2 接通交互：手动选择立即切场景并写 localStorage；"跟随实时"清除并立即重新取数；面板外点击收起

## 4. 布局集成（FrontLayout）

- [x] 4.1 修改 `frontend/src/layouts/FrontLayout.vue`：挂载 WeatherBackground + WeatherBadge；根节点绑 `:data-wb-theme`；header/main/footer 提升 position:relative + z-index:1
- [x] 4.2 实现 dark 主题适配样式（scoped + :deep 有限使用）：顶栏/页脚转深色半透明+浅色文字；内容卡片表面转半透明玻璃（backdrop-blur，白底不透明度 ≥72%）；light 主题零变化；验证 Element Plus 弹层不受影响

## 5. 验证与收尾

- [x] 5.1 阶段验证：`npm run build` 通过 + 浏览器实测（五场景切换过渡、手动切换持久化、恢复实时、reduced-motion 模拟、暗主题下逐页检查前台主页面可读性、路由切换背景不闪烁、/admin 无背景）
- [x] 5.2 全量回归：前端 build + grep 检查新增文件无品牌类硬编码色（渐变天空属功能色场景色，允许；对照 design D5 约束审查）+ 后端零改动确认（git status 无 project-* 变更）
