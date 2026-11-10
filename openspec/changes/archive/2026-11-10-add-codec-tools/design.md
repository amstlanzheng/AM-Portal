# add-codec-tools Design

## Context

工具页第一批（add-tool-pages）已交付 12 个工具并沉淀了公共设施：`ToolPageLayout.vue`（三段式骨架 + 隐私/免责文案 + 空态）、`toolMeta.ts`（卡片元数据）、`useCopy.ts`（复制反馈）、工具页「纯本地计算、不发网络请求、只用主题语义令牌」的约束。本变更在其上补充 6 个编解码/哈希工具，技术形态与既有工具完全一致，核心新增物只有一个纯函数工具库 `utils/codec.ts`。

## Goals / Non-Goals

**Goals:**
- 6 个工具页（Base64 / URL / HTML 实体 / 哈希 / UUID / Unicode）按统一骨架落地，注册路由与导航卡片
- 零新增 npm 依赖、零后端改动、零网络请求
- 所有转换逻辑收敛到可独立测试的纯函数层

**Non-Goals:**
- 不做 Base64 文件（图片/PDF）编解码、不做 HMAC、不做密钥派生（bcrypt/scrypt）——后续批次再说
- 不改动既有 12 个工具的任何行为
- 不引入 it-tools 的其余类别（JSON Diff、正则、二维码等留给下一批）

## Decisions

### D1: 哈希——Web Crypto + 内嵌 MD5，拒绝 crypto-js
SHA-1/256/384/512 用浏览器原生 `crypto.subtle.digest`（异步、C 实现、零字节依赖）。MD5 不在 Web Crypto 内，采用公共域纯 JS 实现内嵌进 `utils/codec.ts`（约 120 行），并用公开测试向量验证（`"abc"` → `900150983cd24fb0d6963f7d28e17f72`）。

*替代方案*：引入 `crypto-js`（约 70KB）或 `js-md5`——仅为 MD5 引入依赖违背零依赖目标，拒绝。

### D2: Base64 UTF-8 安全用 TextEncoder 方案
`btoa/atob` 仅支持 Latin-1，直接编码中文会抛 `InvalidCharacterError`。采用 `TextEncoder` → 字节数组 → 二进制字符串 → `btoa` 的编码路径，解码反向用 `TextDecoder`。非法 Base64 输入捕获异常并在结果区显示错误提示（不崩溃）。

*替代方案*：`encodeURIComponent` 转义老派做法——可读性差且百分号序列更长，拒绝。

### D3: 纯函数层与页面层分离
全部转换/生成逻辑放 `frontend/src/utils/codec.ts` 纯函数（base64Encode/base64Decode、urlEncode/urlDecode、htmlEscape/htmlUnescape、md5/sha*、unicodeEscape/unicodeUnescape、genUuids），页面组件只做输入绑定、调用与结果渲染。HTML 实体解码用 `textarea.innerHTML` 技巧（textarea 不渲染不执行脚本，无 XSS 面）。

### D4: 哈希即时计算 + 竞态守卫
哈希页输入即算（input 事件 + 150ms 防抖）。`crypto.subtle.digest` 是异步的，连续输入可能乱序返回，用单调递增序号守卫：仅当回调序号等于最新请求序号时才更新结果。

### D5: 非安全上下文的 Web Crypto 降级
`crypto.subtle` 仅在安全上下文（https/localhost）可用。工具页加载时探测，不可用时哈希区显示「当前环境不支持 Web Crypto（需 HTTPS 或 localhost 访问）」而非白屏报错。其余 5 个工具不依赖 Web Crypto（UUID 用 `crypto.getRandomValues` 自行拼装 v4，非安全上下文 `crypto.getRandomValues` 仍可用）。

*替代方案*：UUID 直接用 `crypto.randomUUID()`——该 API 同样要求安全上下文，自行拼装更稳。

### D6: 复用既有模式，零新范式
路由懒加载 + `meta.title` 取自 toolMeta、导航卡片数据源于 toolMeta、样式仅用主题语义令牌、复制反馈走 useCopy、「🔒 本地计算」隐私文案由 ToolPageLayout 自带——全部沿用 add-tool-pages 已验证的模式，不引入任何新结构。

## Risks / Trade-offs

- [内嵌 MD5 实现存在手写错误风险] → 以 RFC 1321 公开测试向量（空串、"abc"、长串）在验证阶段逐一比对
- [SHA 异步计算在极端慢设备上有感知延迟] → 150ms 防抖 + 序号守卫，单次计算本身 <10ms，可接受
- [6 个页面结构高度相似，存在复制粘贴式重复] → 有意为之：与既有 12 个工具保持同构，不提前抽象「双向编解码」公共组件；若下一批再增同类工具届时再抽
- [导航页卡片从 8 增至 14，移动端首屏变长] → 卡片列表本就滚动浏览，网格布局不变，可接受

## Migration Plan

纯前端增量：构建发布 `frontend/dist` 即可，无数据库迁移、无后端发布、无配置变更。回滚 = 回退 dist。

## Open Questions

无。
