import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  breaks: true,
  highlight(code: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
          hljs.highlight(code, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
      } catch { /* 忽略高亮异常，回退为转义输出 */ }
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(code) + '</code></pre>'
  }
})

/**
 * 将 Markdown 文本渲染为 HTML
 */
export function renderMarkdown(text?: string): string {
  if (!text) return ''
  return md.render(text)
}
