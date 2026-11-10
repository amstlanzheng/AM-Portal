<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="resultReady">
    <template #panel>
      <div class="uuid-options">
        <div class="option-item">
          <span class="option-label">数量：{{ count }}</span>
          <el-slider v-model="count" :min="1" :max="50" class="option-slider" />
        </div>
        <div class="option-item">
          <span class="option-label">大写</span>
          <el-switch v-model="uppercase" />
        </div>
        <div class="option-item">
          <span class="option-label">连字符</span>
          <el-switch v-model="hyphens" />
        </div>
        <el-button size="small" type="primary" @click="generate">重新生成</el-button>
      </div>
    </template>

    <template #result>
      <div class="uuid-result">
        <div class="output-head">
          <span class="output-flag">{{ uuids.length }} 个 UUID v4</span>
          <el-button size="small" text type="primary" @click="copyAll">{{ copiedAll ? '已复制 ✓' : '全部复制' }}</el-button>
        </div>
        <ul class="uuid-list">
          <li v-for="(u, i) in uuids" :key="i" class="uuid-row">
            <span class="uuid-value">{{ u }}</span>
            <el-button size="small" text type="primary" @click="copyOne(u, i)">
              {{ copiedIndex === i ? '已复制 ✓' : '复制' }}
            </el-button>
          </li>
        </ul>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { genUuids } from '../../utils/codec'

const tool = findTool('uuid')!

const count = ref(5)
const uppercase = ref(false)
const hyphens = ref(true)
const uuids = ref<string[]>([])

const resultReady = computed(() => uuids.value.length > 0)

function generate() {
  uuids.value = genUuids(count.value, { uppercase: uppercase.value, hyphens: hyphens.value })
}

/* 复制反馈：单条（按行索引）与全部 */
const copiedIndex = ref(-1)
const copiedAll = ref(false)
let timer = 0

async function writeClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    return false
  }
}

async function copyOne(text: string, index: number) {
  if (!(await writeClipboard(text))) return
  copiedIndex.value = index
  copiedAll.value = false
  resetTimer()
}

async function copyAll() {
  if (!(await writeClipboard(uuids.value.join('\n')))) return
  copiedAll.value = true
  copiedIndex.value = -1
  resetTimer()
}

function resetTimer() {
  if (timer) clearTimeout(timer)
  timer = window.setTimeout(() => {
    copiedIndex.value = -1
    copiedAll.value = false
  }, 2000)
}

onMounted(generate)
</script>

<style scoped>
.uuid-options {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-5);
}

.option-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.option-label {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  white-space: nowrap;
}

.option-slider {
  width: 180px;
}

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

.uuid-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  max-height: 420px;
  overflow: auto;
}

.uuid-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: 8px var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
}

.uuid-value {
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  color: var(--color-text-strong);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .option-slider {
    width: 120px;
  }
}
</style>
