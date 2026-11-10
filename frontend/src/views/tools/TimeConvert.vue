<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="resultReady">
    <template #panel>
      <div class="time-form">
        <el-radio-group v-model="mode">
          <el-radio-button value="ts">时间戳 → 日期</el-radio-button>
          <el-radio-button value="date">日期 → 时间戳</el-radio-button>
        </el-radio-group>

        <div v-if="mode === 'ts'" class="input-row">
          <el-input
            v-model="tsInput" placeholder="输入 10 位（秒）或 13 位（毫秒）时间戳，如 1762656000"
            clearable spellcheck="false"
            @keydown.enter="convert"
          />
          <el-button @click="fillNow">现在</el-button>
        </div>
        <p v-if="mode === 'ts' && tsError" class="input-error">{{ tsError }}</p>

        <div v-else class="input-row">
          <el-date-picker
            v-model="dateInput" type="datetime" placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm:ss" style="width: 100%"
          />
          <el-button @click="fillNow">现在</el-button>
        </div>

        <div class="input-row">
          <span class="zone-label">时区</span>
          <el-select v-model="timezone" style="flex: 1">
            <el-option label="本地时区" value="local" />
            <el-option label="UTC" value="UTC" />
            <el-option label="Asia / Shanghai" value="Asia/Shanghai" />
            <el-option label="Asia / Tokyo" value="Asia/Tokyo" />
            <el-option label="Europe / London" value="Europe/London" />
            <el-option label="America / New_York" value="America/New_York" />
          </el-select>
        </div>

        <el-button type="primary" :disabled="!canConvert" @click="convert">转换</el-button>
      </div>
    </template>

    <template #result>
      <div v-if="rows.length" class="time-result">
        <div v-for="row in rows" :key="row.label" class="time-row">
          <span class="row-label">{{ row.label }}</span>
          <code class="row-value">{{ row.value }}</code>
          <el-button size="small" text type="primary" @click="copyValue(row.value, row.label)">{{ copied && lastCopied === row.label ? '已复制 ✓' : '复制' }}</el-button>
        </div>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { useCopy } from '../../composables/useCopy'

const tool = findTool('time-convert')!
const { copied, copy } = useCopy()
const lastCopied = ref('')

const mode = ref<'ts' | 'date'>('ts')
const tsInput = ref('')
const dateInput = ref('')
const timezone = ref('local')
const tsError = ref('')

const rows = ref<{ label: string; value: string }[]>([])

const canConvert = computed(() =>
  mode.value === 'ts' ? !!tsInput.value.trim() : !!dateInput.value
)
const resultReady = computed(() => rows.value.length > 0 || !!tsError.value)

function fillNow() {
  if (mode.value === 'ts') {
    tsInput.value = String(Math.floor(Date.now() / 1000))
    tsError.value = ''
  } else {
    dateInput.value = new Date()
  }
}

/** 解析时间戳输入：10 位秒 / 13 位毫秒，其他位数报错 */
function parseTimestamp(text: string): number | null {
  const t = text.trim()
  if (!/^\d+$/.test(t)) {
    tsError.value = '时间戳应为纯数字'
    return null
  }
  if (t.length === 10) return parseInt(t) * 1000
  if (t.length === 13) return parseInt(t)
  tsError.value = `无法识别的位数（${t.length} 位）：请输入 10 位秒级或 13 位毫秒级时间戳`
  return null
}

function convert() {
  rows.value = []
  let ms: number
  if (mode.value === 'ts') {
    tsError.value = ''
    const parsed = parseTimestamp(tsInput.value)
    if (parsed === null) return
    ms = parsed
  } else {
    const d = dateInput.value instanceof Date ? dateInput.value : new Date(dateInput.value)
    if (isNaN(d.getTime())) { tsError.value = '日期无效'; return }
    tsError.value = ''
    ms = d.getTime()
  }

  const date = new Date(ms)
  rows.value = [
    { label: '日期时间', value: formatDate(date) },
    { label: '时间戳（秒）', value: String(Math.floor(ms / 1000)) },
    { label: '时间戳（毫秒）', value: String(ms) },
    { label: 'ISO 8601', value: date.toISOString() },
    { label: 'UTC', value: date.toUTCString() },
    { label: '相对时间', value: relativeTime(ms) },
    { label: '星期', value: `星期${'日一二三四五六'[date.getDay()]}` }
  ]
}

/** 按所选时区格式化为 yyyy-MM-dd HH:mm:ss */
function formatDate(date: Date): string {
  const opts: Intl.DateTimeFormatOptions = {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
  }
  if (timezone.value !== 'local') opts.timeZone = timezone.value
  // zh-CN 输出 2026/11/09 12:00:00，统一替换为连字符
  return new Intl.DateTimeFormat('zh-CN', opts).format(date).replace(/\//g, '-')
}

/** 相对时间：x 年/月/天/小时/分钟/秒 前|后 */
function relativeTime(ms: number): string {
  const diff = ms - Date.now()
  const abs = Math.abs(diff)
  const units: [string, number][] = [
    ['年', 31536000000], ['个月', 2592000000], ['天', 86400000],
    ['小时', 3600000], ['分钟', 60000], ['秒', 1000]
  ]
  let text = '刚刚'
  for (const [name, unit] of units) {
    if (abs >= unit) { text = `${Math.floor(abs / unit)} ${name}${diff >= 0 ? '后' : '前'}`; break }
  }
  return text
}

async function copyValue(text: string, key: string) {
  lastCopied.value = key
  await copy(text)
}
</script>

<style scoped>
.time-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.input-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.input-row .el-button {
  flex-shrink: 0;
}

.zone-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
  flex-shrink: 0;
}

.input-error {
  margin: -6px 0 0;
  font-size: var(--text-xs);
  color: var(--el-color-danger);
}

/* ---------- 结果 ---------- */
.time-result {
  display: flex;
  flex-direction: column;
}

.time-row {
  display: grid;
  grid-template-columns: 130px 1fr auto;
  align-items: center;
  gap: var(--space-3);
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
}

.time-row:last-child {
  border-bottom: none;
}

.row-label {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.row-value {
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  color: var(--color-text-strong);
  word-break: break-all;
}

@media (max-width: 768px) {
  .time-row {
    grid-template-columns: 104px 1fr auto;
  }
}
</style>
