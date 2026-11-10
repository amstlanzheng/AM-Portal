<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="resultReady">
    <template #panel>
      <div class="json-toolbar">
        <el-radio-group v-model="indent" size="small">
          <el-radio-button value="2">2 空格</el-radio-button>
          <el-radio-button value="4">4 空格</el-radio-button>
          <el-radio-button value="tab">Tab</el-radio-button>
        </el-radio-group>
        <div class="json-actions">
          <el-button size="small" :disabled="!input" @click="run('beautify')">美化</el-button>
          <el-button size="small" :disabled="!input" @click="run('minify')">压缩</el-button>
          <el-button size="small" :disabled="!input" @click="run('validate')">校验</el-button>
        </div>
      </div>
      <textarea
        v-model="input"
        class="json-input"
        placeholder='在此粘贴 JSON，例如 {"name":"AM Portal","tags":["tools","fun"]}'
        spellcheck="false"
      ></textarea>
    </template>

    <template #result>
      <!-- 错误提示 -->
      <div v-if="error" class="json-error">
        <p class="error-title">✕ JSON 语法错误</p>
        <p class="error-detail">{{ error.message }}</p>
        <p v-if="error.position" class="error-position">位置：第 {{ error.line }} 行，第 {{ error.column }} 列（字符偏移 {{ error.position }}）</p>
      </div>

      <!-- 合法结果 -->
      <div v-else-if="output" class="json-output-wrap">
        <div class="output-head">
          <span class="output-flag">{{ mode === 'validate' ? '✓ JSON 语法正确' : mode === 'minify' ? '压缩结果' : '格式化结果' }}</span>
          <el-button size="small" text type="primary" @click="copy(output)">{{ copied ? '已复制 ✓' : '复制' }}</el-button>
        </div>
        <pre class="json-output" v-html="highlighted"></pre>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { useCopy } from '../../composables/useCopy'

const tool = findTool('json-format')!
const { copied, copy } = useCopy()

const input = ref('')
const indent = ref<'2' | '4' | 'tab'>('2')
const mode = ref<'beautify' | 'minify' | 'validate'>('beautify')
const output = ref('')
const error = ref<{ message: string; position?: number; line?: number; column?: number } | null>(null)

const resultReady = computed(() => output.value !== '' || error.value !== null)

function indentStr() {
  return indent.value === 'tab' ? '\t' : ' '.repeat(parseInt(indent.value))
}

function run(m: 'beautify' | 'minify' | 'validate') {
  mode.value = m
  error.value = null
  output.value = ''
  const text = input.value.trim()
  if (!text) return
  try {
    const parsed = JSON.parse(text)
    if (m === 'validate') {
      output.value = text
    } else {
      output.value = m === 'beautify'
        ? JSON.stringify(parsed, null, indentStr())
        : JSON.stringify(parsed)
    }
  } catch (e) {
    const message = e instanceof Error ? e.message : String(e)
    const parsed = parseErrorPosition(message, input.value)
    error.value = { message, ...parsed }
  }
}

/** 从 V8 错误信息中提取 position 并换算行列 */
function parseErrorPosition(message: string, text: string): { position?: number; line?: number; column?: number } {
  const m = message.match(/position\s+(\d+)/i)
  if (!m) return {}
  const position = parseInt(m[1])
  if (position > text.length) return { position }
  const before = text.slice(0, position)
  const line = before.split('\n').length
  const column = position - before.lastIndexOf('\n')
  return { position, line, column }
}

/** 语法高亮：先转义 HTML，再按 token 包 span */
const highlighted = computed(() => {
  const escaped = output.value
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  return escaped.replace(
    /("(?:\\u[a-fA-F0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(?:true|false|null)\b|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)/g,
    (match) => {
      let cls = 'number'
      if (match.startsWith('"')) cls = /:\s*$/.test(match) ? 'key' : 'string'
      else if (match === 'true' || match === 'false') cls = 'boolean'
      else if (match === 'null') cls = 'null'
      return `<span class="tok-${cls}">${match}</span>`
    }
  )
})
</script>

<style scoped>
.json-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.json-actions {
  display: flex;
  gap: var(--space-2);
}

.json-input {
  width: 100%;
  min-height: 220px;
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  line-height: 1.6;
  color: var(--color-text-strong);
  resize: vertical;
  outline: none;
}

.json-input:focus {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--brand-500) 15%, transparent);
}

/* ---------- 错误 ---------- */
.json-error {
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid color-mix(in srgb, var(--el-color-danger) 40%, transparent);
  background: color-mix(in srgb, var(--el-color-danger) 6%, var(--surface));
}

.error-title {
  margin: 0 0 6px;
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--el-color-danger);
}

.error-detail,
.error-position {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--el-color-danger);
  line-height: 1.6;
  word-break: break-all;
}

.error-position {
  margin-top: 4px;
  font-variant-numeric: tabular-nums;
}

/* ---------- 输出 ---------- */
.output-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.output-flag {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--brand-600);
}

.json-output {
  margin: 0;
  max-height: 480px;
  overflow: auto;
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: #1e293b;
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  line-height: 1.65;
  color: #e2e8f0;
  white-space: pre;
  tab-size: 4;
}

.tok-key { color: #7dd3fc; }
.tok-string { color: #86efac; }
.tok-number { color: #fbbf24; }
.tok-boolean { color: #c4b5fd; }
.tok-null { color: #f9a8d4; }
</style>
