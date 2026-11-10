## ADDED Requirements

### Requirement: 实时天气获取
系统 SHALL 通过浏览器直调 Open-Meteo current API 获取实时天气（温度、WMO weather_code、is_day），坐标优先取浏览器 geolocation，定位拒绝、失败或超时（5 秒）时 SHALL 静默回退默认坐标（北京）继续获取。天气数据 SHALL 每 30 分钟自动刷新一次，且页面从不可见恢复为可见且数据已过期时刷新。天气获取失败 SHALL 不阻塞页面任何其他功能。

#### Scenario: 定位成功获取实时天气
- **WHEN** 用户打开前台页面且浏览器 geolocation 授权成功
- **THEN** 系统以定位坐标请求 Open-Meteo 并在角标展示当前温度与天气简述

#### Scenario: 定位被拒或超时
- **WHEN** geolocation 授权被拒绝、失败或超过 5 秒未响应
- **THEN** 系统静默使用默认坐标（北京）请求天气，页面无错误弹窗，角标正常展示天气

#### Scenario: 天气接口请求失败
- **WHEN** Open-Meteo 请求失败或返回异常
- **THEN** 背景回退到默认场景（日间晴），角标显示"天气不可用"，展开面板可手动重试，页面其他功能不受影响

### Requirement: 动态天气背景渲染
系统 SHALL 在前台布局渲染全屏动态背景，由渐变天空底色层与 canvas 动效层组成，随场景切换视觉元素：晴天=暖色渐变+光晕与漂浮光尘、多云=灰蓝渐变+漂移云团、雨天=青灰渐变+斜向雨线、雪天=冷白渐变+摇摆飘落雪花、夜晚=深蓝渐变+闪烁星星与月晕。场景 SHALL 由实时天气映射（weather_code + is_day）。背景层 SHALL 为 pointer-events: none 且不遮挡页面交互。系统 SHALL 尊重 prefers-reduced-motion：用户开启减少动效时不运行动画，仅呈现静态渐变。

#### Scenario: 五种场景视觉切换
- **WHEN** 天气场景分别为晴、多云、雨、雪、夜
- **THEN** 背景分别呈现对应的渐变底色与动效元素（光晕/云团/雨线/雪花/星星），切换时渐变平滑过渡

#### Scenario: 夜间判定
- **WHEN** Open-Meteo 返回 is_day=0 且天气晴朗
- **THEN** 背景呈现夜晚场景（星空与月晕），而非晴天场景

#### Scenario: 减少动效用户
- **WHEN** 用户系统开启 prefers-reduced-motion
- **THEN** 背景仅显示当前场景静态渐变，无 canvas 动画运行

#### Scenario: 页面切后台暂停动画
- **WHEN** 标签页不可见时
- **THEN** canvas 动画循环暂停，恢复可见后继续，不产生累积跳帧

### Requirement: 文字对比度自适应
系统 SHALL 根据场景明暗自动调整前台落在背景上的文字与表面样式：暗场景（夜晚、雨夜）下顶栏与页脚文字转浅色、背景转深色半透明，内容卡片表面转半透明玻璃效果（backdrop-blur）；亮场景维持现有浅色风格。适配 SHALL 通过 FrontLayout 根节点的 data-wb-theme 属性驱动，不得修改全局 CSS 令牌定义、不得影响后台管理界面与 Element Plus 弹层组件。

#### Scenario: 夜晚场景文字自适应
- **WHEN** 场景为夜晚（暗主题）
- **THEN** 顶栏、页脚文字变为浅色可读，内容卡片呈半透明玻璃效果，正文内容保持可读

#### Scenario: 晴天白天维持现状
- **WHEN** 场景为日间晴天（亮主题）
- **THEN** 前台视觉与既有浅色风格一致，无样式退化

#### Scenario: 后台不受影响
- **WHEN** 用户进入 /admin 后台
- **THEN** 后台界面不渲染天气背景，样式与本变更前完全一致

### Requirement: 天气角标与手动切换
系统 SHALL 在前台固定角落（左下）展示天气角标：天气图标 + 温度 + 天气简述。点击角标 SHALL 展开切换面板，提供六个选项：跟随实时、晴天、多云、雨天、雪天、夜晚。手动选择 SHALL 立即切换背景场景并持久化到 localStorage；选择"跟随实时"SHALL 清除手动选择并立即重新获取实时天气。

#### Scenario: 角标展示实时天气
- **WHEN** 天气数据获取成功
- **THEN** 左下角标展示对应天气图标、温度数值与天气简述

#### Scenario: 手动切换场景
- **WHEN** 用户在面板中选择"雪天"
- **THEN** 背景立即切换为雪天场景，刷新页面后仍保持雪天（localStorage 持久化）

#### Scenario: 恢复跟随实时
- **WHEN** 用户在面板中选择"跟随实时"
- **THEN** 清除手动选择，系统重新获取实时天气并按其渲染背景

### Requirement: 前台集成范围
天气背景 SHALL 仅集成于前台布局（FrontLayout），其下所有子路由页面（首页/项目/博客/工具/趣味/登录/注册/个人资料）共享背景。后台布局（AdminLayout）SHALL 不受任何影响。本能力 SHALL 不修改后端代码、接口与数据库。

#### Scenario: 前台子路由共享背景
- **WHEN** 用户在前台任意子页面间导航
- **THEN** 天气背景持续存在且状态一致（不因路由切换重新初始化或闪烁）

#### Scenario: 零后端改动
- **WHEN** 本变更合入
- **THEN** 后端代码、API 契约、数据库结构均无变化
