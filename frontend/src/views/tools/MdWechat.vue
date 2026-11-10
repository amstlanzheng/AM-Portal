<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="true">
    <template #panel>
      <div class="md-toolbar">
        <div class="toolbar-group">
          <span class="toolbar-label">主题</span>
          <el-radio-group v-model="theme" size="small">
            <el-radio-button v-for="t in THEMES" :key="t.key" :value="t.key">{{ t.name }}</el-radio-button>
          </el-radio-group>
        </div>
        <div class="toolbar-group">
          <span class="toolbar-label">字号</span>
          <el-radio-group v-model="fontSize" size="small">
            <el-radio-button :value="14">14</el-radio-button>
            <el-radio-button :value="15">15</el-radio-button>
            <el-radio-button :value="16">16</el-radio-button>
          </el-radio-group>
        </div>
        <div class="toolbar-group">
          <span class="toolbar-label">行距</span>
          <el-radio-group v-model="lineHeight" size="small">
            <el-radio-button :value="1.6">1.6</el-radio-button>
            <el-radio-button :value="1.75">1.75</el-radio-button>
            <el-radio-button :value="2">2.0</el-radio-button>
          </el-radio-group>
        </div>
        <el-button type="primary" size="small" @click="copyRich">{{ copied ? '已复制 ✓' : '复制到公众号' }}</el-button>
      </div>

      <p v-if="hasExternalImages" class="img-warning">
        ⚠ 检测到外链图片：公众号可能屏蔽外部图床，粘贴后请检查图片是否显示，必要时手动上传替换。
      </p>

      <div class="md-workspace">
        <textarea
          v-model="source" class="md-input" spellcheck="false"
          placeholder="# 在这里写 Markdown&#10;&#10;支持标题、加粗、列表、引用、代码块、链接、图片、表格等常用语法"
        ></textarea>
        <div class="md-preview-wrap">
          <p class="preview-label">预览（已带内联样式）</p>
          <div class="md-preview" v-html="styledHtml"></div>
        </div>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import MarkdownIt from 'markdown-it'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { useCopy } from '../../composables/useCopy'

const tool = findTool('md-wechat')!
const { copied, copy } = useCopy()

/** 独立实例：公众号只认内联样式，避免复用博客渲染器的 hljs class 污染 */
const md = new MarkdownIt({ html: true, linkify: true, breaks: true })

interface WechatTheme {
  key: string
  name: string
  heading: string
  text: string
  accent: string
  quoteBg: string
  quoteBorder: string
}

const THEMES: WechatTheme[] = [
  { key: 'blue', name: '清雅蓝', heading: '#2B5FAD', text: '#3F3F3F', accent: '#2B5FAD', quoteBg: '#F0F5FB', quoteBorder: '#8FB4E3' },
  { key: 'orange', name: '温暖橙', heading: '#D9480F', text: '#3D3833', accent: '#E8590C', quoteBg: '#FDF4EE', quoteBorder: '#FFB380' },
  { key: 'green', name: '沉稳绿', heading: '#2B8A3E', text: '#37352F', accent: '#2F9E44', quoteBg: '#EFF7F0', quoteBorder: '#8CE0A0' }
]

const source = ref(DEFAULT_SOURCE)
const theme = ref('blue')
const fontSize = ref(15)
const lineHeight = ref(1.75)

const hasExternalImages = computed(() => /!\[[^\]]*\]\(\s*https?:\/\//.test(source.value))

/** 渲染 + 内联样式包装（公众号只认内联样式） */
const styledHtml = computed(() => {
  const raw = md.render(source.value)
  if (!raw.trim()) return '<p style="color:#999;font-size:14px;">预览将显示在这里</p>'
  const t = THEMES.find(x => x.key === theme.value)!
  const fs = fontSize.value
  const lh = lineHeight.value

  const doc = new DOMParser().parseFromString(`<div id="r">${raw}</div>`, 'text/html')
  const root = doc.getElementById('r')!

  const set = (el: Element, style: Partial<CSSStyleDeclaration>) => {
    const s = Object.entries(style).map(([k, v]) => `${k.replace(/[A-Z]/g, m => '-' + m.toLowerCase())}:${v}`).join(';')
    el.setAttribute('style', s)
  }

  root.querySelectorAll('h1,h2,h3,h4,h5,h6').forEach(el => {
    const tag = el.tagName.toLowerCase()
    const sizes: Record<string, number> = { h1: fs + 6, h2: fs + 4, h3: fs + 2, h4: fs + 1, h5: fs, h6: fs }
    set(el, {
      margin: tag === 'h1' ? '24px 0 16px' : '20px 0 12px',
      fontSize: `${sizes[tag]}px`, fontWeight: '700', color: t.heading, lineHeight: `${lh}`
    })
  })

  root.querySelectorAll('p').forEach(el => set(el, {
    margin: '0 0 16px', fontSize: `${fs}px`, lineHeight: `${lh}`, color: t.text, wordBreak: 'break-word'
  }))

  root.querySelectorAll('li').forEach(el => set(el, {
    margin: '0 0 8px', fontSize: `${fs}px`, lineHeight: `${lh}`, color: t.text
  }))
  root.querySelectorAll('ul,ol').forEach(el => set(el, { padding: '0 0 0 22px', margin: '0 0 16px' }))

  root.querySelectorAll('blockquote').forEach(el => {
    set(el, {
      margin: '0 0 16px', padding: '10px 15px',
      borderLeft: `3px solid ${t.quoteBorder}`, background: t.quoteBg,
      fontSize: `${fs}px`, lineHeight: `${lh}`, color: t.text
    })
  })

  root.querySelectorAll('pre').forEach(el => set(el, {
    margin: '0 0 16px', padding: '14px', background: '#2D2D2D',
    borderRadius: '6px', overflowX: 'auto', lineHeight: '1.6'
  }))
  root.querySelectorAll('pre code').forEach(el => set(el, {
    fontFamily: 'Consolas,Menlo,monospace', fontSize: `${fs - 2}px`, color: '#F8F8F2', whiteSpace: 'pre'
  }))
  root.querySelectorAll(':not(pre) > code').forEach(el => set(el, {
    fontFamily: 'Consolas,Menlo,monospace', fontSize: `${fs - 1}px`,
    background: t.quoteBg, color: t.accent, padding: '2px 6px', borderRadius: '4px'
  }))

  root.querySelectorAll('strong').forEach(el => set(el, { fontWeight: '700', color: t.heading }))
  root.querySelectorAll('em').forEach(el => set(el, { fontStyle: 'italic', color: t.text }))
  root.querySelectorAll('a').forEach(el => set(el, { color: t.accent, textDecoration: 'none', borderBottom: `1px solid ${t.quoteBorder}` }))

  root.querySelectorAll('img').forEach(el => set(el, { maxWidth: '100%', borderRadius: '6px', display: 'block', margin: '8px auto' }))
  root.querySelectorAll('hr').forEach(el => set(el, { border: 'none', borderTop: '1px solid #DDDDDD', margin: '20px 0' }))

  root.querySelectorAll('table').forEach(el => set(el, { borderCollapse: 'collapse', margin: '0 0 16px', width: '100%', fontSize: `${fs - 1}px` }))
  root.querySelectorAll('th,td').forEach(el => set(el, {
    border: '1px solid #DDDDDD', padding: '8px 12px', textAlign: 'left', lineHeight: '1.5'
  }))
  root.querySelectorAll('th').forEach(el => set(el, { background: t.quoteBg, color: t.heading, fontWeight: '700' }))

  return root.innerHTML
})

/** 复制富文本：ClipboardItem 写入 text/html，失败回退复制 HTML 源码 */
async function copyRich() {
  const html = styledHtml.value
  try {
    const item = new ClipboardItem({
      'text/html': new Blob([html], { type: 'text/html' }),
      'text/plain': new Blob([source.value], { type: 'text/plain' })
    })
    await navigator.clipboard.write([item])
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    await copy(html)
  }
}

const DEFAULT_SOURCE = `# 欢迎使用 Markdown 转公众号

在**左侧**编辑 Markdown，右侧实时预览带内联样式的排版效果，点击「复制到公众号」后直接粘贴到公众号编辑器。

## 常用语法示例

支持 *斜体*、**加粗**、[链接](https://example.com)、\`行内代码\`：

> 引用一段话：简约不简单。

1. 有序列表
2. 无序列表也行

\`\`\`js
console.log('代码块会转成深色底样式')
\`\`\`
`
</script>

<style scoped>
.md-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.toolbar-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
}

.md-toolbar .el-button {
  margin-left: auto;
}

.img-warning {
  margin: 0 0 var(--space-4);
  padding: 10px var(--space-4);
  border-radius: var(--radius-lg);
  border-left: 3px solid var(--el-color-warning);
  background: color-mix(in srgb, var(--el-color-warning) 8%, var(--surface));
  font-size: var(--text-sm);
  color: var(--el-color-warning);
}

.md-workspace {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
  align-items: stretch;
}

.md-input {
  width: 100%;
  min-height: 420px;
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  line-height: 1.7;
  color: var(--color-text-strong);
  resize: vertical;
  outline: none;
}

.md-input:focus {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--brand-500) 15%, transparent);
}

.md-preview-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-label {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

.md-preview {
  flex: 1;
  min-height: 420px;
  padding: var(--space-4) var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface);
  overflow: auto;
  max-width: 100%;
}

:deep(.md-preview img) { max-width: 100%; }

@media (max-width: 768px) {
  .md-workspace {
    grid-template-columns: 1fr;
  }
}
</style>
