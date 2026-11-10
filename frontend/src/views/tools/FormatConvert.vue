<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="resultReady">
    <template #panel>
      <el-radio-group v-model="mode" class="mode-switch">
        <el-radio-button value="image">图片格式</el-radio-button>
        <el-radio-button value="json2csv">JSON → CSV</el-radio-button>
        <el-radio-button value="csv2json">CSV → JSON</el-radio-button>
      </el-radio-group>

      <!-- 图片格式转换 -->
      <div v-if="mode === 'image'" class="image-pane">
        <div
          class="drop-zone" :class="{ dragging }"
          @click="fileInput?.click()"
          @dragover.prevent="dragging = true"
          @dragleave.prevent="dragging = false"
          @drop.prevent="onDrop"
        >
          <el-icon :size="28"><Picture /></el-icon>
          <p class="drop-text">点击选择或拖拽图片</p>
          <input ref="fileInput" type="file" accept="image/*" hidden @change="onPick" />
        </div>
        <div v-if="imgFile" class="image-convert-bar">
          <img class="preview" :src="imgUrl!" alt="预览" />
          <div class="convert-controls">
            <p class="file-line">{{ imgFile.name }}（{{ formatSize(imgFile.size) }}）</p>
            <div class="target-row">
              <span class="target-label">目标格式</span>
              <el-radio-group v-model="targetFormat" size="small">
                <el-radio-button value="image/png">PNG</el-radio-button>
                <el-radio-button value="image/jpeg">JPG</el-radio-button>
                <el-radio-button value="image/webp" :disabled="!webpSupported">WebP{{ webpSupported ? '' : '（不支持）' }}</el-radio-button>
              </el-radio-group>
            </div>
            <p v-if="!webpSupported" class="webp-note">当前浏览器不支持 WebP 编码，已回退 PNG/JPG。</p>
            <el-button type="primary" size="small" @click="convertImage">转换</el-button>
          </div>
        </div>
      </div>

      <!-- 文本转换 -->
      <div v-else class="text-pane">
        <p class="pane-hint">{{ mode === 'json2csv' ? '粘贴 JSON 数组（元素需为对象）' : '粘贴 CSV 文本（首行为表头）' }}</p>
        <textarea v-model="textInput" class="convert-input" spellcheck="false"
          :placeholder="mode === 'json2csv' ? '[{&quot;name&quot;:&quot;Alice&quot;,&quot;age&quot;:30},{&quot;name&quot;:&quot;Bob&quot;,&quot;age&quot;:25}]' : 'name,age\nAlice,30\nBob,25'"></textarea>
        <el-button type="primary" :disabled="!textInput.trim()" @click="convertText">转换</el-button>
      </div>

      <p v-if="convertError" class="convert-error">✕ {{ convertError }}</p>
    </template>

    <template #result>
      <!-- 图片结果 -->
      <div v-if="mode === 'image' && convertedUrl" class="image-result">
        <p class="result-flag">✓ 已转换为 {{ targetFormatLabel }}（{{ formatSize(convertedSize) }}）</p>
        <img class="converted-img" :src="convertedUrl" alt="转换结果" />
        <el-button type="primary" @click="downloadImage">下载</el-button>
      </div>

      <!-- 文本结果 -->
      <div v-else-if="textOutput !== null" class="text-result">
        <div class="output-head">
          <span class="result-flag">✓ 转换成功（{{ outputLineCount }} 行）</span>
          <div class="head-actions">
            <el-button size="small" @click="copy(textOutput)">{{ copied ? '已复制 ✓' : '复制' }}</el-button>
            <el-button size="small" @click="downloadText">下载 .{{ mode === 'json2csv' ? 'csv' : 'json' }}</el-button>
          </div>
        </div>
        <pre class="text-output">{{ textOutput }}</pre>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { useCopy } from '../../composables/useCopy'

const tool = findTool('format-convert')!
const { copied, copy } = useCopy()

const mode = ref<'image' | 'json2csv' | 'csv2json'>('image')
const convertError = ref('')

const resultReady = computed(() =>
  (mode.value === 'image' && !!convertedUrl.value) ||
  (mode.value !== 'image' && textOutput.value !== null)
)

/* ---------- 图片转换 ---------- */
const fileInput = ref<HTMLInputElement>()
const dragging = ref(false)
const imgFile = ref<File | null>(null)
const imgUrl = ref<string | null>(null)
const targetFormat = ref('image/png')
const convertedUrl = ref<string | null>(null)
const convertedSize = ref(0)

const webpSupported = (() => {
  try { return document.createElement('canvas').toDataURL('image/webp').startsWith('data:image/webp') }
  catch { return false }
})()

const targetFormatLabel = computed(() => ({ 'image/png': 'PNG', 'image/jpeg': 'JPG', 'image/webp': 'WebP' })[targetFormat.value])

function setFile(f: File) {
  if (!f.type.startsWith('image/')) { convertError.value = `「${f.name}」不是图片文件`; return }
  convertError.value = ''
  if (imgUrl.value) URL.revokeObjectURL(imgUrl.value)
  if (convertedUrl.value) { URL.revokeObjectURL(convertedUrl.value); convertedUrl.value = null }
  imgFile.value = f
  imgUrl.value = URL.createObjectURL(f)
}

function onPick(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.[0]) setFile(input.files[0])
  input.value = ''
}

function onDrop(e: DragEvent) {
  dragging.value = false
  if (e.dataTransfer?.files?.[0]) setFile(e.dataTransfer.files[0])
}

function convertImage() {
  if (!imgFile.value || !imgUrl.value) return
  convertError.value = ''
  const img = new Image()
  img.onload = () => {
    const canvas = document.createElement('canvas')
    canvas.width = img.naturalWidth
    canvas.height = img.naturalHeight
    canvas.getContext('2d')!.drawImage(img, 0, 0)
    canvas.toBlob((blob) => {
      if (!blob) { convertError.value = '转换失败，请重试'; return }
      if (convertedUrl.value) URL.revokeObjectURL(convertedUrl.value)
      convertedUrl.value = URL.createObjectURL(blob)
      convertedSize.value = blob.size
    }, targetFormat.value, 0.9)
  }
  img.onerror = () => { convertError.value = '图片加载失败' }
  img.src = imgUrl.value
}

function downloadImage() {
  if (!convertedUrl.value || !imgFile.value) return
  const a = document.createElement('a')
  a.href = convertedUrl.value
  a.download = imgFile.value.name.replace(/\.\w+$/, '') + '.' + targetFormatLabel.value.toLowerCase()
  a.click()
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

/* ---------- 文本转换 ---------- */
const textInput = ref('')
const textOutput = ref<string | null>(null)

const outputLineCount = computed(() => textOutput.value ? textOutput.value.split('\n').length : 0)

/** CSV 单元格转义：含逗号/引号/换行时双引号包裹、内部引号翻倍 */
function csvEscape(v: string): string {
  return /[",\n\r]/.test(v) ? `"${v.replace(/"/g, '""')}"` : v
}

function jsonToCsv(text: string): string {
  let data: unknown
  try { data = JSON.parse(text) } catch (e) {
    throw new Error(`JSON 解析失败：${e instanceof Error ? e.message : e}`)
  }
  if (!Array.isArray(data)) throw new Error('JSON 顶层必须是数组（如 [{...}, {...}]），请检查后重试')
  if (data.length === 0) throw new Error('数组为空，没有可转换的数据')
  if (data.some(d => typeof d !== 'object' || d === null || Array.isArray(d))) {
    throw new Error('数组元素必须都是对象（键值对），不支持嵌套数组或纯值')
  }
  const objs = data as Record<string, unknown>[]
  // 表头：保持首元素 key 顺序，再补后续元素新增的 key
  const keys: string[] = []
  for (const o of objs) for (const k of Object.keys(o)) if (!keys.includes(k)) keys.push(k)
  const cell = (v: unknown) => {
    if (v === null || v === undefined) return ''
    if (typeof v === 'object') return JSON.stringify(v)
    return String(v)
  }
  return [
    keys.map(csvEscape).join(','),
    ...objs.map(o => keys.map(k => csvEscape(cell(o[k]))).join(','))
  ].join('\n')
}

/** CSV 解析状态机：正确处理引号内的逗号与换行 */
function parseCsv(text: string): string[][] {
  const rows: string[][] = []
  let row: string[] = []
  let cur = ''
  let inQuotes = false
  for (let i = 0; i < text.length; i++) {
    const c = text[i]
    if (inQuotes) {
      if (c === '"') {
        if (text[i + 1] === '"') { cur += '"'; i++ }
        else inQuotes = false
      } else cur += c
    } else if (c === '"') {
      inQuotes = true
    } else if (c === ',') {
      row.push(cur); cur = ''
    } else if (c === '\n' || c === '\r') {
      if (c === '\r' && text[i + 1] === '\n') i++
      row.push(cur); cur = ''
      if (row.length > 1 || row[0] !== '') rows.push(row)
      row = []
    } else cur += c
  }
  row.push(cur)
  if (row.length > 1 || row[0] !== '') rows.push(row)
  return rows
}

function csvToJson(text: string): string {
  const rows = parseCsv(text.trim())
  if (rows.length < 2) throw new Error('CSV 至少需要表头 + 一行数据')
  const headers = rows[0]
  const convert = (v: string): unknown => {
    if (v === '') return null
    if (v === 'true') return true
    if (v === 'false') return false
    if (/^-?\d+(\.\d+)?([eE][+-]?\d+)?$/.test(v)) return Number(v)
    return v
  }
  return JSON.stringify(
    rows.slice(1).map(r => Object.fromEntries(headers.map((h, i) => [h, convert(r[i] ?? '')]))),
    null, 2
  )
}

function convertText() {
  convertError.value = ''
  textOutput.value = null
  const text = textInput.value.trim()
  if (!text) return
  try {
    textOutput.value = mode.value === 'json2csv' ? jsonToCsv(text) : csvToJson(text)
  } catch (e) {
    convertError.value = e instanceof Error ? e.message : String(e)
  }
}

function downloadText() {
  if (textOutput.value === null) return
  const blob = new Blob([textOutput.value], { type: 'text/plain;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = mode.value === 'json2csv' ? 'converted.csv' : 'converted.json'
  a.click()
  URL.revokeObjectURL(a.href)
}
</script>

<style scoped>
.mode-switch {
  margin-bottom: var(--space-4);
}

.image-pane,
.text-pane {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.drop-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: var(--space-6) var(--space-4);
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.drop-zone:hover,
.drop-zone.dragging {
  border-color: var(--brand-500);
  background: var(--brand-50);
}

.drop-text {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
}

.image-convert-bar {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: var(--space-4);
  align-items: start;
}

.preview {
  width: 100%;
  height: 120px;
  object-fit: contain;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
}

.convert-controls {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  align-items: flex-start;
}

.file-line {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-text-strong);
  word-break: break-all;
}

.target-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.target-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
}

.webp-note {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--el-color-warning);
}

.pane-hint {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

.convert-input {
  width: 100%;
  min-height: 180px;
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

.convert-input:focus {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--brand-500) 15%, transparent);
}

.convert-error {
  margin: var(--space-2) 0 0;
  font-size: var(--text-sm);
  color: var(--el-color-danger);
}

/* ---------- 结果 ---------- */
.image-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
}

.result-flag {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--el-color-success);
}

.converted-img {
  max-width: 100%;
  max-height: 320px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
}

.output-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.head-actions {
  display: flex;
  gap: var(--space-2);
}

.text-output {
  margin: 0;
  max-height: 400px;
  overflow: auto;
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  line-height: 1.6;
  color: var(--color-text-strong);
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
