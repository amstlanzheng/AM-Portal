## MODIFIED Requirements

### Requirement: 导航页展示工具卡片列表
工具导航页 SHALL 以卡片形式列出 14 个实用工具（健身增肌热量计算器、图片压缩、格式转换、JSON 格式化、Markdown 转公众号排版、随机密码生成器、颜色选择器、时间转换工具、Base64 编解码、URL 编解码、HTML 实体转义、哈希工具、UUID 生成器、Unicode 编解码）；趣味导航页 SHALL 列出 4 个趣味工具（打字速度测试、反应力测试、MBTI 变体测试、点击计数器）。每张卡片 MUST 包含工具名称与一句话介绍，点击进入对应工具独立页面。

#### Scenario: 卡片信息完整并可跳转
- **WHEN** 访客在工具导航页点击任一工具卡片
- **THEN** 路由进入该工具独立页面，页面标题显示工具名称

#### Scenario: 新增编解码工具卡片可见
- **WHEN** 访客打开工具导航页 `/tools`
- **THEN** 卡片列表包含 Base64 编解码、URL 编解码、HTML 实体转义、哈希工具、UUID 生成器、Unicode 编解码 6 张新卡片
