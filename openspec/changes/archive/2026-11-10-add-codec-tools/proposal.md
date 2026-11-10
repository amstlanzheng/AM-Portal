# add-codec-tools

## Why

现有 12 个工具已覆盖计算、图片、排版等场景，但开发日常最高频的「编码解码与哈希」类工具（Base64、URL 编码、哈希、UUID 等）仍然缺失，用户需跳转第三方站点。参考 it-tools 的分类与实践，这一组工具可全部纯前端零依赖实现，与现有「不发网络请求、本地计算」的工具页原则完全一致，是补充工具页性价比最高的第一批。

## What Changes

- 新增 6 个纯前端工具页（复用既有 ToolPageLayout 三段式骨架、toolMeta 元数据、useCopy 复制反馈）：
  1. **Base64 编解码**：UTF-8 安全的编码/解码双向转换，非法 Base64 输入给出错误提示
  2. **URL 编解码**：支持 encodeURIComponent（组件模式）与 encodeURI（整 URL 模式）双模式编码与解码
  3. **HTML 实体转义**：文本 ↔ HTML 实体双向转换（`&amp;` `&lt;` `&gt;` `&quot;` `&#39;` 等常用实体）
  4. **哈希工具**：输入文本即时输出 MD5、SHA-1、SHA-256、SHA-384、SHA-512 五种摘要（SHA 系列走 Web Crypto API，MD5 内嵌纯 JS 实现，零外部依赖）
  5. **UUID 生成器**：批量生成 1-50 个 UUID v4，支持大写/小写、带/不带连字符选项，单个/全部复制
  6. **Unicode 编解码**：文本 ↔ `\uXXXX` 转义序列双向转换（含中文场景）
- `toolMeta.ts` 扩充 6 条工具元数据；`/tools` 导航页卡片列表由 8 个扩为 14 个
- 路由注册 6 个懒加载工具页（`/tools/base64`、`/tools/url-encode`、`/tools/html-entity`、`/tools/hash`、`/tools/uuid`、`/tools/unicode`）
- 全部纯前端实现，无后端改动、无新增 npm 依赖

## Capabilities

### New Capabilities
- `codec-tools`: 编码解码与哈希工具组——Base64/URL/HTML 实体/Unicode 四类双向编解码，MD5/SHA 哈希摘要，UUID v4 批量生成；全部浏览器本地计算、统一复制反馈

### Modified Capabilities
- `tool-navigation`: 工具导航页卡片列表由 8 个实用工具扩充为 14 个（新增 6 个编解码工具卡片）

## Impact

- **前端**：新增 `frontend/src/views/tools/` 下 6 个页面组件、`frontend/src/utils/codec.ts`（编解码与 MD5/SHA 封装）；修改 `toolMeta.ts`、路由 `router/index.ts`
- **后端**：无改动
- **依赖**：无新增 npm 依赖（Web Crypto + 内嵌 MD5 实现）
- **既有能力复用**：ToolPageLayout（三段式骨架与隐私/免责文案）、useCopy（复制反馈）、主题语义令牌体系均直接复用，工具页「纯本地计算、无网络请求」约束延续不变
