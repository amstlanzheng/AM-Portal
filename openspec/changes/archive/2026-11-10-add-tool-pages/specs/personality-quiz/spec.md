## ADDED Requirements

### Requirement: 题库数据模型
系统 SHALL 提供测试题表（quiz_question：主维度字母、题干、选项 JSON、排序、启用、逻辑删除）与结果类型表（quiz_result_type：两字母结果代号、自定义命名、约 100 字类型描述、排序、逻辑删除）。选项 JSON 结构 MUST 为 `[{"text":"选项文案","scores":{"维度字母":分值}}]`，一个选项可同时给多个维度字母加分。迁移脚本 MUST 幂等并附带 10 道初始题目与 8 种结果类型（NT/NF/ST/SF/SP/SJ/NP/NJ）。

#### Scenario: 迁移脚本可重复执行
- **WHEN** 在已建表的库上再次执行迁移脚本
- **THEN** 不报错、不产生重复数据

#### Scenario: 初始数据可用
- **WHEN** 迁移完成后调用公开取题接口
- **THEN** 返回 10 道启用题目，每题含选项文本与维度分值

### Requirement: 公开取题与结果类型接口
系统 SHALL 提供公开接口 `GET /api/home/quiz/questions`（返回全部启用题目按 sort_order 排序，含选项文本与分值）与 `GET /api/home/quiz/result-types`（返回全部启用结果类型）。接口 MUST 复用 `/api/home/**` 公开访问规则，无需登录。

#### Scenario: 未登录获取题目
- **WHEN** 未登录访客请求 GET /api/home/quiz/questions
- **THEN** 返回启用题目列表，每题含 question_text 与 options 数组

#### Scenario: 未登录获取结果类型
- **WHEN** 未登录访客请求 GET /api/home/quiz/result-types
- **THEN** 返回结果类型列表（code/name/description）

### Requirement: 管理端题库 CRUD
系统 SHALL 在后台提供测试题管理：列表（分页）、新增、修改、删除（逻辑删除），可编辑题干、主维度字母、选项（动态选项行：文本 + 各维度字母分值键值对，无需手写 JSON）、排序与启用状态。

#### Scenario: 新增一道题
- **WHEN** 管理员在后台填写题干、添加两个选项（各配维度分值）并保存
- **THEN** 列表出现新题，前台取题接口随即可见（启用时）

#### Scenario: 修改选项分值
- **WHEN** 管理员将某选项的 N 分值从 1 改为 2 并保存
- **THEN** 前台取题接口返回的该选项 scores 中 N 为 2

#### Scenario: 删除题目
- **WHEN** 管理员删除一道题
- **THEN** 列表不再显示，前台取题接口不再返回该题

### Requirement: 管理端结果类型 CRUD
系统 SHALL 在后台提供结果类型管理：列表、新增、修改、删除，可编辑结果代号（两字母组合）、自定义命名与类型描述。

#### Scenario: 修改结果类型命名
- **WHEN** 管理员将 code 为 NT 的命名改为「策略家」并保存
- **THEN** 前台结果类型接口返回 NT 的 name 为「策略家」

### Requirement: 前端答题与计分流程
前端 SHALL 提供答题页：逐题作答（10 道单选）→ 提交 → 展示结果。计分 MUST 在浏览器本地完成：8 个维度字母独立累计选项分值，取总分最高的 2 个字母组成结果代号；同分时按 E>N>T>J 权重破平；以代号匹配结果类型展示命名与描述，未匹配时 SHALL 显示兜底文案而非报错。页面 MUST 展示「娱乐向，非心理诊断」字样，并提供复制结果出口。

#### Scenario: 答完题得到匹配结果
- **WHEN** 用户完成全部题目且 N、T 两字母累计分最高
- **THEN** 结果页显示 code 为 NT 的结果类型命名与描述，及「娱乐向，非心理诊断」字样

#### Scenario: 结果代号未配置兜底
- **WHEN** 计分得到的代号在结果类型表中不存在
- **THEN** 结果页显示兜底文案，不报错

#### Scenario: 未答完禁止提交
- **WHEN** 存在未作答题目
- **THEN** 提交按钮禁用
