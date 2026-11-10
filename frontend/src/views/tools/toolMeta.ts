import type { Component } from 'vue'
import {
  TrendCharts, Picture, Switch, Document, EditPen, Key, Brush, Clock,
  Memo, Lightning, MagicStick, Pointer, Files, Link, Collection, Lock,
  Grid, Tickets
} from '@element-plus/icons-vue'

export interface ToolMeta {
  name: string
  title: string
  desc: string
  icon: Component
  intro: string
  usage: string
}

export const utilityTools: ToolMeta[] = [
  {
    name: 'calorie', title: '健身增肌热量计算器', icon: TrendCharts,
    desc: '估算维持体重和增肌所需的每日热量，并给出三大营养素建议摄入量',
    intro: '输入身高、体重、年龄、性别和活动量，估算维持体重和增肌所需的每日热量，并给出蛋白质、碳水、脂肪的建议摄入量。适合想科学增肌、不想盲目多吃的人。',
    usage: '填写基础信息 → 选择活动强度 → 选择目标 → 点击计算'
  },
  {
    name: 'image-compress', title: '图片压缩', icon: Picture,
    desc: '拖动质量滑块，在本地完成压缩，原图不会离开你的设备',
    intro: '选择图片、拖动质量滑块，在本地完成压缩，原图不会离开你的设备。',
    usage: '上传 → 调质量 → 预览对比 → 下载'
  },
  {
    name: 'format-convert', title: '格式转换', icon: Switch,
    desc: '图片格式互转与 JSON↔CSV 转换，全部在浏览器内完成',
    intro: '支持图片格式互转（PNG/JPG/WebP）、JSON↔CSV 等常用格式转换，全部在浏览器内完成。',
    usage: '选转换类型 → 上传/粘贴 → 转换 → 下载或复制'
  },
  {
    name: 'json-format', title: 'JSON 格式化', icon: Document,
    desc: '一键美化、压缩或校验 JSON 语法错误，并定位出错位置',
    intro: '粘贴 JSON 即可一键美化、压缩或校验语法错误，并定位出错位置。',
    usage: '粘贴 → 选美化/压缩/校验 → 查看结果 → 复制'
  },
  {
    name: 'md-wechat', title: 'Markdown 转公众号排版', icon: EditPen,
    desc: '左边写 Markdown，右边实时预览公众号排版，一键复制富文本',
    intro: '左边写 Markdown，右边实时预览公众号排版效果，一键复制带样式的富文本。',
    usage: '左侧输入 → 选主题 → 复制到公众号 → 粘贴'
  },
  {
    name: 'password', title: '随机密码生成器', icon: Key,
    desc: '自定义长度和字符类型，一键生成高强度随机密码',
    intro: '自定义长度和字符类型，一键生成高强度随机密码，密码只在浏览器里生成。',
    usage: '拖长度 → 勾字符类型 → 生成 → 复制'
  },
  {
    name: 'color-picker', title: '颜色选择器', icon: Brush,
    desc: '选色并查看 HEX/RGB/HSL 多格式色值，生成协调配色',
    intro: '选择或输入颜色，实时查看 HEX、RGB、HSL 等格式，并生成协调配色。',
    usage: '点色块或输入色值 → 查看格式 → 复制或看推荐配色'
  },
  {
    name: 'time-convert', title: '时间转换工具', icon: Clock,
    desc: '时间戳和日期互相转换，支持多时区显示和常用格式输出',
    intro: '时间戳和日期互相转换，支持多时区显示和常用格式输出。',
    usage: '输入时间戳或选日期 → 选时区 → 查看结果 → 复制'
  },
  {
    name: 'base64', title: 'Base64 编解码', icon: Files,
    desc: '文本与 Base64 双向转换，完整支持中文等 UTF-8 内容',
    intro: '文本与 Base64 双向转换，完整支持中文等 UTF-8 字符，非法 Base64 会给出明确错误提示。',
    usage: '粘贴内容 → 选编码/解码 → 复制结果'
  },
  {
    name: 'url-encode', title: 'URL 编解码', icon: Link,
    desc: '组件模式与整 URL 模式两种百分号编解码',
    intro: 'URL 百分号编解码：组件模式转义 & = ? 等保留字符，整 URL 模式保留链接结构，按需选择。',
    usage: '选模式 → 输入内容 → 编码/解码 → 复制'
  },
  {
    name: 'html-entity', title: 'HTML 实体转义', icon: Collection,
    desc: '尖括号、引号、& 与 HTML 实体双向转换',
    intro: 'HTML 特殊字符与实体（&lt; &gt; &amp; &quot; 等）双向转换，发代码片段到网页前的必备步骤。',
    usage: '输入文本 → 选转义/还原 → 复制结果'
  },
  {
    name: 'hash', title: '哈希工具', icon: Lock,
    desc: '输入文本即时计算 MD5、SHA-1、SHA-256 等五种摘要',
    intro: '输入文本即时计算 MD5、SHA-1、SHA-256、SHA-384、SHA-512 五种摘要，随打随算，逐项复制。',
    usage: '输入文本 → 自动出结果 → 逐项复制'
  },
  {
    name: 'uuid', title: 'UUID 生成器', icon: Grid,
    desc: '批量生成 UUID v4，支持大小写与连字符格式',
    intro: '批量生成随机的 UUID v4（最多 50 个），可选大小写与是否带连字符，单个或全部复制。',
    usage: '选数量与格式 → 生成 → 复制'
  },
  {
    name: 'unicode', title: 'Unicode 编解码', icon: Tickets,
    desc: '中文等字符与 \\uXXXX 转义序列双向转换',
    intro: '文本与 \\uXXXX 转义序列双向转换，编码时 ASCII 字符保留原样，常见于配置文件与国际化资源处理。',
    usage: '输入文本 → 选编码/解码 → 复制结果'
  }
]

export const funTools: ToolMeta[] = [
  {
    name: 'typing', title: '打字速度测试', icon: Memo,
    desc: '限时打字测出你的 WPM、CPM 与准确率',
    intro: '60 秒内打完屏幕上的文字，测出打字速度和准确率，看看能不能进排行榜。',
    usage: '开始 → 照打 → 自动结算 → 查看速度 → 挑战好友'
  },
  {
    name: 'reaction', title: '反应力测试', icon: Lightning,
    desc: '屏幕变绿立刻点击，普通人约 250 毫秒，你能更快吗',
    intro: '屏幕变绿时立刻点击，测出你的反应速度。普通人约 250 毫秒，你能更快吗？',
    usage: '开始 → 等变绿 → 点击 → 查看时间 → 测 5 次取平均'
  },
  {
    name: 'mbti', title: 'MBTI 变体测试', icon: MagicStick,
    desc: '10 道轻松小题，测出你的职场人格变体（娱乐向）',
    intro: '10 道轻松小题，测出你的职场人格/社交人格变体。纯属娱乐，别太当真。',
    usage: '逐题选择 → 提交 → 查看结果 → 分享'
  },
  {
    name: 'clicker', title: '点击计数器', icon: Pointer,
    desc: '10 秒内你能点多少下？测测手速',
    intro: '10 秒内你能点多少下？测测手速，看看能不能打破纪录。',
    usage: '开始 → 疯狂点击 → 时间到看次数 → 再来或分享'
  }
]

export function findTool(name: string): ToolMeta | undefined {
  return [...utilityTools, ...funTools].find(t => t.name === name)
}
