<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="resultReady">
    <template #panel>
      <textarea
        v-model="input"
        class="hash-input"
        placeholder="输入任意文本，五种摘要随输入即时计算"
        spellcheck="false"
      ></textarea>
    </template>

    <template #result>
      <div v-if="!cryptoOk" class="hash-warn">
        当前环境不支持 Web Crypto（需 HTTPS 或 localhost 访问），SHA 系列摘要不可用，MD5 不受影响。
      </div>
      <ul class="hash-list">
        <li v-for="row in rows" :key="row.algo" class="hash-row">
          <span class="hash-algo">{{ row.algo }}</span>
          <span class="hash-value" :class="{ placeholder: !row.value }">{{ row.value || row.pendingText }}</span>
          <el-button
            size="small"
            text
            type="primary"
            :disabled="!row.value"
            @click="copyRow(row.algo, row.value)"
          >{{ copiedAlgo === row.algo ? '已复制 ✓' : '复制' }}</el-button>
        </li>
      </ul>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { md5, sha, webCryptoAvailable, type ShaAlgorithm } from '../../utils/codec'

interface HashRow {
  algo: string
  value: string
  pendingText: string
}

const tool = findTool('hash')!
const input = ref('')
const cryptoOk = ref(true)
const rows = ref<HashRow[]>([
  { algo: 'MD5', value: '', pendingText: '输入文本后自动计算' },
  { algo: 'SHA-1', value: '', pendingText: '输入文本后自动计算' },
  { algo: 'SHA-256', value: '', pendingText: '输入文本后自动计算' },
  { algo: 'SHA-384', value: '', pendingText: '输入文本后自动计算' },
  { algo: 'SHA-512', value: '', pendingText: '输入文本后自动计算' }
])

const resultReady = computed(() => input.value !== '' || rows.value.some(r => r.value !== ''))

/* 多键复制反馈（本页五行各自独立状态，不走共享 useCopy） */
const copiedAlgo = ref('')
let copyTimer = 0
async function copyRow(algo: string, text: string) {
  try {
    await navigator.clipboard.writeText(text)
    copiedAlgo.value = algo
    if (copyTimer) clearTimeout(copyTimer)
    copyTimer = window.setTimeout(() => { copiedAlgo.value = '' }, 2000)
  } catch { /* 剪贴板不可用时静默 */ }
}

/* 输入防抖 + 竞态守卫（SHA 为异步，仅采纳最后一次输入的结果） */
let debounceTimer = 0
let requestSeq = 0

watch(input, (text) => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = window.setTimeout(() => { void compute(text) }, 150)
})

async function compute(text: string) {
  const seq = ++requestSeq
  if (!text) {
    rows.value = rows.value.map(r => ({ ...r, value: '', pendingText: '输入文本后自动计算' }))
    return
  }
  const md5Value = md5(text)
  const shaValues: Record<ShaAlgorithm, string> = { 'SHA-1': '', 'SHA-256': '', 'SHA-384': '', 'SHA-512': '' }
  if (cryptoOk.value) {
    const settled = await Promise.all(
      (['SHA-1', 'SHA-256', 'SHA-384', 'SHA-512'] as ShaAlgorithm[]).map(async (algo) => {
        try {
          return [algo, await sha(algo, text)] as const
        } catch {
          return [algo, '计算失败'] as const
        }
      })
    )
    for (const [algo, value] of settled) shaValues[algo] = value
  }
  if (seq !== requestSeq) return // 已有更新输入，丢弃本次结果
  rows.value = rows.value.map(r => ({
    ...r,
    value: r.algo === 'MD5' ? md5Value : shaValues[r.algo as ShaAlgorithm] || (cryptoOk.value ? '' : '不可用'),
    pendingText: ''
  }))
}

onMounted(() => {
  cryptoOk.value = webCryptoAvailable()
  if (!cryptoOk.value) {
    rows.value = rows.value.map(r => r.algo === 'MD5' ? r : { ...r, pendingText: '不可用' })
  }
})
</script>

<style scoped>
.hash-input {
  width: 100%;
  min-height: 120px;
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

.hash-input:focus {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--brand-500) 15%, transparent);
}

.hash-warn {
  margin-bottom: var(--space-3);
  padding: 10px var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid color-mix(in srgb, var(--el-color-warning) 40%, transparent);
  background: color-mix(in srgb, var(--el-color-warning) 8%, var(--surface));
  font-size: var(--text-sm);
  color: var(--el-color-warning);
}

.hash-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.hash-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 10px var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
}

.hash-algo {
  flex-shrink: 0;
  width: 72px;
  font-size: var(--text-xs);
  font-weight: 700;
  color: var(--brand-600);
}

.hash-value {
  flex: 1;
  min-width: 0;
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  color: var(--color-text-strong);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hash-value.placeholder {
  color: var(--color-text-faint);
  font-family: inherit;
}

@media (max-width: 768px) {
  .hash-algo {
    width: 58px;
  }
  .hash-value {
    font-size: var(--text-xs);
  }
}
</style>
