## 1. 纯函数层（utils/codec.ts）

- [x] 1.1 创建 `frontend/src/utils/codec.ts`：Base64 UTF-8 安全编解码（TextEncoder/TextDecoder 方案，解码异常返回错误信息）
- [x] 1.2 URL 编解码双模式（encodeURIComponent/encodeURI 及对应解码，解码失败返回错误信息）
- [x] 1.3 HTML 实体转义/反转义（先转 &，textarea.innerHTML 解码技巧）
- [x] 1.4 Unicode 编解码（非 ASCII → \uXXXX；非法转义序列返回错误信息）
- [x] 1.5 MD5 内嵌纯 JS 实现 + sha1/sha256/sha384/sha512 Web Crypto 封装（async）+ 已知测试向量自检（"abc" 等）
- [x] 1.6 genUuids（数量 1-50、大小写、连字符选项，crypto.getRandomValues 拼装 v4 格式）

## 2. 六个工具页面

- [x] 2.1 `views/tools/Base64Codec.vue`：双向编解码，空输入禁用，非法输入错误提示，复制
- [x] 2.2 `views/tools/UrlCodec.vue`：双模式切换（组件/整 URL），双向，解码错误提示，复制
- [x] 2.3 `views/tools/HtmlEntity.vue`：双向转义/还原，复制
- [x] 2.4 `views/tools/HashTool.vue`：输入防抖 150ms 即时计算五种摘要 + 竞态守卫 + 每项单独复制 + Web Crypto 不可用降级提示
- [x] 2.5 `views/tools/UuidGenerator.vue`：数量/大小写/连字符选项 + 生成列表 + 单条与全部复制
- [x] 2.6 `views/tools/UnicodeCodec.vue`：双向转换，非法序列错误提示，复制

## 3. 注册与集成

- [x] 3.1 `toolMeta.ts` 新增 6 条元数据（名称/介绍/图标/路由名），`/tools` 导航页自动展示 14 卡片
- [x] 3.2 路由注册 6 个懒加载路由（/tools/base64、url-encode、html-entity、hash、uuid、unicode，meta.title 取 toolMeta）
- [x] 3.3 阶段验证：npm run build 通过；浏览器实测六个工具主路径（含中文编码往返、abc 测试向量、UUID 格式断言、导航卡片跳转）

## 4. 收尾

- [x] 4.1 全量回归：前端 build + grep 检查新增文件无硬编码颜色（仅语义令牌）+ 后端零改动确认（git status 无 project-* 变更）
