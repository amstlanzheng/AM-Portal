## 1. 骨架与公共设施

- [x] 1.1 创建 `views/tools/toolMeta.ts`：定义 12 个工具元数据（名称/一句话介绍/图标/路由名），导航页与路由共用
- [x] 1.2 创建 `components/tools/ToolPageLayout.vue`：三段式骨架（介绍/操作插槽/结果插槽）+ 隐私与免责小字 + 空态占位
- [x] 1.3 创建 `composables/useCopy.ts`：「已复制 ✓ 2 秒还原」统一复制反馈
- [x] 1.4 路由：新增 `/tools`、`/fun` 导航页与 12 个工具页路由（懒加载，标题取 toolMeta）；FrontLayout 主导航加「工具」「趣味」
- [x] 1.5 创建 `ToolsIndex.vue` / `FunIndex.vue` 导航页（卡片列表，主题令牌样式）
- [x] 1.6 阶段验证：`npm run build` 通过，浏览器实测导航跳转与主题联动

## 2. 实用工具（上）

- [x] 2.1 健身增肌热量计算器：表单 + Mifflin-St Jeor 计算 + 营养素分配 + 复制结果；非法输入禁用
- [x] 2.2 JSON 格式化：粘贴 + 美化（2/4/Tab）+ 压缩 + 校验错误定位 + 高亮 + 复制
- [x] 2.3 随机密码生成器：长度滑块 + 字符类型勾选 + 排除易混淆 + crypto 随机 + 每类至少一个 + 强度条 + 批量 + 复制
- [x] 2.4 颜色选择器：取色控件 + HEX/RGB/HSL 互转联动 + 明暗变体 + 互补/邻近色 + 点击复制 + 非法回退
- [x] 2.5 时间转换工具：时间戳（秒/毫秒自动识别）/日期选择 + 时区 + 多格式 + UTC + 相对时间 + 复制
- [x] 2.6 阶段验证：build 通过 + 每个工具浏览器实测一遍主路径

## 3. 实用工具（下）

- [x] 3.1 图片压缩：点击/拖拽上传（缩略图+文件名+类型校验+20MB提示）+ 质量滑块 + Canvas 压缩 + 前后对比/压缩率/预览 + 逐张下载 + 进度
- [x] 3.2 格式转换：转换类型下拉 + 图片互转（Canvas，webp 特性检测回退）+ JSON↔CSV（转义/非数组提示）+ 下载/复制
- [x] 3.3 Markdown 转公众号：markdown-it 渲染 + 2-3 套内联样式主题 + 字号/行距 + 实时预览 + ClipboardItem 富文本复制（回退复制 HTML）+ 外链图片提示
- [x] 3.4 阶段验证：build 通过 + 图片压缩/格式转换/MD 转公众号浏览器实测

## 4. 趣味工具

- [x] 4.1 打字速度测试：难度（英/中/代码内置语料）+ 时长选择 + 逐字符对比高亮 + WPM/CPM/准确率结算 + 复制成绩
- [x] 4.2 反应力测试：随机 1-4s 延迟变绿 + 提前点击犯规 + 5 次平均 + 评级 + 复制成绩
- [x] 4.3 点击计数器：时长选择 + 点击累加 + CPS + 评级 + touchstart 支持 + 复制成绩
- [x] 4.4 阶段验证：build 通过 + 三个趣味工具浏览器实测（含移动端模拟）

## 5. MBTI 变体测试（后端 + 前端 + 管理端）

- [x] 5.1 迁移脚本 `migration-quiz.sql`：quiz_question + quiz_result_type 两表（幂等）+ 10 道初始题 + 8 种结果类型；application-prod.yml schema-locations 登记；schema.sql 同步
- [x] 5.2 后端：QuizQuestion/QuizResultType entity + mapper + service（含选项 JSON 序列化）
- [x] 5.3 后端：公开接口 GET /api/home/quiz/questions、GET /api/home/quiz/result-types（启用 + 排序）
- [x] 5.4 后端：管理接口（题目分页 CRUD + 结果类型 CRUD，/api/admin/quiz/**）
- [x] 5.5 前端：`api/index.ts` 新增 quiz 接口；答题页（逐题单选 + 未答完禁用 + 本地计分取最高 2 字母 + 同分 E>N>T>J 破平 + 结果类型匹配 + 兜底文案 + 娱乐向声明 + 复制结果）
- [x] 5.6 管理端：`views/admin/Quiz.vue` 两 Tab（题目：动态选项行编辑文本与维度分值键值对；结果类型：code/命名/描述），路由与侧边栏菜单注册
- [x] 5.7 阶段验证：后端编译 + 前端 build + 浏览器实测答题全流程与管理端增删改

## 6. 收尾

- [x] 6.1 全量回归：前端 build + 后端 package 通过；grep 检查新页面无硬编码颜色（仅语义令牌）
- [x] 6.2 部署验证：执行迁移 SQL（或确认 prod 自动执行）+ docker 更新后自查——导航入口、12 工具主路径、主题联动、MBTI 全流程、后台题库管理
