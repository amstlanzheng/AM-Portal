## ADDED Requirements

### Requirement: Base64 编解码
系统 SHALL 提供 Base64 编解码工具：输入任意 UTF-8 文本（含中文）可编码为 Base64，输入 Base64 可解码还原原文。编码与解码 MUST 支持 UTF-8 多字节字符；非法 Base64 输入 MUST 在结果区显示错误提示而非崩溃或乱码；空输入时操作按钮 MUST 禁用；结果 MUST 提供复制出口。

#### Scenario: 中文文本编码解码往返
- **WHEN** 用户输入「你好，世界」执行编码，再对结果执行解码
- **THEN** 解码输出与原文完全一致「你好，世界」

#### Scenario: 非法 Base64 提示
- **WHEN** 用户输入含非法字符的字符串（如 `abc!@#`）执行解码
- **THEN** 结果区显示错误提示文案，页面不崩溃

#### Scenario: 空输入禁用
- **WHEN** 输入框为空
- **THEN** 编码与解码按钮均处于禁用状态

### Requirement: URL 编解码
系统 SHALL 提供 URL 编解码工具：支持两种编码模式——组件模式（encodeURIComponent，转义 `&` `=` `?` `/` 等保留字符）与整 URL 模式（encodeURI，保留 URL 结构字符）；解码对应 decodeURIComponent/decodeURI。模式切换 MUST 即时生效于下一次操作；解码失败（如孤立的 `%`）MUST 显示错误提示；结果 MUST 提供复制出口。

#### Scenario: 组件模式编码查询参数
- **WHEN** 用户选择组件模式并输入「a=1&b=你好」执行编码
- **THEN** 输出 `a%3D1%26b%3D%E4%BD%A0%E5%A5%BD`

#### Scenario: 整 URL 模式保留结构
- **WHEN** 用户选择整 URL 模式并输入「https://example.com/搜索?q=词」执行编码
- **THEN** 输出中 `https://` 与 `/` `?` `=` 结构字符保留，仅中文被百分号转义

#### Scenario: 解码错误提示
- **WHEN** 用户输入「100%」执行解码
- **THEN** 结果区显示错误提示文案，页面不崩溃

### Requirement: HTML 实体转义
系统 SHALL 提供 HTML 实体转义工具：文本 → 实体（`&` `<` `>` `"` `'` 至少覆盖）与实体 → 文本双向转换。转义 MUST 先处理 `&` 再处理其他字符以避免二次转义；解码 MUST 正确还原数字实体（`&#20320;`）与命名实体；结果 MUST 提供复制出口。

#### Scenario: 转义标签文本
- **WHEN** 用户输入「<div class="a">&</div>」执行转义
- **THEN** 输出 `&lt;div class=&quot;a&quot;&gt;&amp;&lt;/div&gt;`

#### Scenario: 混合实体解码还原
- **WHEN** 用户输入「&lt;b&gt;&#20320;&lt;/b&gt;」执行解码
- **THEN** 输出「<b>你</b>」

### Requirement: 哈希工具
系统 SHALL 提供哈希工具：输入文本后即时输出 MD5、SHA-1、SHA-256、SHA-384、SHA-512 五种摘要（小写十六进制）。SHA 系列 MUST 使用 Web Crypto API；MD5 MUST 使用本地内嵌实现且与公开测试向量一致；输入变化时结果 SHALL 经 150ms 防抖后自动更新；连续输入时 MUST 以最后一次输入的计算结果为准（竞态守卫）；每种摘要 MUST 单独提供复制出口。运行环境不支持 Web Crypto 时 MUST 显示降级提示文案而非报错。

#### Scenario: 已知向量验证
- **WHEN** 用户输入「abc」
- **THEN** MD5 输出 `900150983cd24fb0d6963f7d28e17f72`，SHA-256 输出 `ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad`

#### Scenario: 输入防抖即时更新
- **WHEN** 用户在输入框连续键入「hello world」并停顿
- **THEN** 五种摘要自动更新为「hello world」对应值，无需点击按钮

#### Scenario: 非安全上下文降级
- **WHEN** 页面运行在不支持 Web Crypto 的环境
- **THEN** 哈希结果显示「当前环境不支持 Web Crypto」提示，页面其余功能正常

### Requirement: UUID 生成器
系统 SHALL 提供 UUID v4 生成器：可设置生成数量（1-50）、大写/小写、带/不带连字符；点击生成输出对应数量的随机 UUID 列表；每个 UUID MUST 符合 v4 格式（第 13 位为 4、第 17 位为 8/9/a/b 之一）；单个条目与全部结果 MUST 均可复制；随机源 MUST 使用 `crypto.getRandomValues`。

#### Scenario: 批量生成小写带连字符
- **WHEN** 用户设置数量 5、小写、带连字符并生成
- **THEN** 输出 5 个形如 `xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx`（y∈{8,9,a,b}）的 UUID，且互不相同

#### Scenario: 大写无连字符
- **WHEN** 用户设置大写、不带连字符并生成 1 个
- **THEN** 输出 32 位大写十六进制字符串，第 13 位为 `4`

### Requirement: Unicode 编解码
系统 SHALL 提供 Unicode 编解码工具：文本 ↔ `\uXXXX` 转义序列双向转换。编码时非 ASCII 字符 MUST 转为 `\uXXXX` 形式（ASCII 字符保留原样）；解码 MUST 正确还原转义序列（含代理对外的 BMP 字符）；解码遇非法转义序列 MUST 显示错误提示；结果 MUST 提供复制出口。

#### Scenario: 中文编码为转义序列
- **WHEN** 用户输入「Hello 你好」执行编码
- **THEN** 输出 `Hello \u4f60\u597d`

#### Scenario: 转义序列解码还原
- **WHEN** 用户输入「\u4f60\u597d\u4e16\u754c」执行解码
- **THEN** 输出「你好世界」
