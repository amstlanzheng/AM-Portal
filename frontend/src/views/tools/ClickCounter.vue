<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="state === 'finished'">
    <template #panel>
      <div class="clicker-toolbar">
        <span class="toolbar-label">时长</span>
        <el-radio-group v-model="duration" size="small" :disabled="state !== 'idle'">
          <el-radio-button :value="5">5s</el-radio-button>
          <el-radio-button :value="10">10s</el-radio-button>
          <el-radio-button :value="30">30s</el-radio-button>
        </el-radio-group>
        <span class="toolbar-label">最佳纪录：{{ bestRecord !== null ? `${bestRecord} 下` : '—' }}</span>
      </div>

      <div class="clicker-stage">
        <button
          class="click-pad" :class="{ idle: state === 'idle', running: state === 'running' }"
          @click="onClick" @touchstart.prevent="onClick"
        >
          <span class="click-count">{{ clicks }}</span>
          <span class="click-hint">{{ hintText }}</span>
        </button>

        <div v-if="state === 'running'" class="time-bar-wrap">
          <div class="time-bar" :style="{ width: timePercent + '%' }"></div>
        </div>
        <p v-if="state === 'running'" class="time-left">{{ timeLeft.toFixed(1) }}s</p>
      </div>
    </template>

    <template #result>
      <div v-if="state === 'finished'" class="clicker-result">
        <div class="metric-cards">
          <div class="metric-card">
            <span class="metric-value">{{ clicks }}</span>
            <span class="metric-label">总点击（下）</span>
          </div>
          <div class="metric-card">
            <span class="metric-value">{{ cps }}</span>
            <span class="metric-label">CPS（次/秒）</span>
          </div>
          <div class="metric-card">
            <span class="metric-value">{{ grade.label }}</span>
            <span class="metric-label">{{ grade.desc }}</span>
          </div>
        </div>
        <p v-if="isNewRecord" class="new-record">🎉 新纪录！超越了之前的 {{ bestRecord }} 下</p>
        <div class="result-actions">
          <el-button type="primary" @click="copyResult">{{ copied ? '已复制 ✓' : '复制成绩' }}</el-button>
          <el-button @click="reset">再来一次</el-button>
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

const tool = findTool('clicker')!
const { copied, copy } = useCopy()

const duration = ref(10)
const state = ref<'idle' | 'running' | 'finished'>('idle')
const clicks = ref(0)
const timeLeft = ref(0)

const BEST_KEY = 'clicker-best-record'
const bestRecord = ref<number | null>(null)
try {
  const saved = localStorage.getItem(BEST_KEY)
  bestRecord.value = saved !== null && saved !== '' ? parseInt(saved) : null
} catch { bestRecord.value = null }
const isNewRecord = ref(false)

let startTime = 0
let rafId: number | undefined

const timePercent = computed(() => duration.value ? (timeLeft.value / duration.value) * 100 : 0)
const cps = computed(() => (clicks.value / duration.value).toFixed(1))

const hintText = computed(() => ({
  idle: '点击开始',
  running: '疯狂点击！',
  finished: ''
})[state.value])

const grade = computed(() => {
  const c = parseFloat(cps.value)
  if (c >= 8) return { label: '手速之神 🏆', desc: '这不是手指，是缝纫机' }
  if (c >= 6) return { label: '电竞选手 🎮', desc: 'APM 拉满的水平' }
  if (c >= 4) return { label: '手速达人 💪', desc: '快过大多数人' }
  if (c >= 2.5) return { label: '正常人类 🙂', desc: '健康的点击速度' }
  return { label: '悠闲派 🐢', desc: '慢慢来，不着急' }
})

function onClick() {
  if (state.value === 'idle') {
    state.value = 'running'
    clicks.value = 1
    startTime = performance.now()
    isNewRecord.value = false
    tick()
  } else if (state.value === 'running') {
    clicks.value++
  }
}

function tick() {
  const elapsed = (performance.now() - startTime) / 1000
  timeLeft.value = Math.max(0, duration.value - elapsed)
  if (timeLeft.value <= 0) {
    finish()
    return
  }
  rafId = requestAnimationFrame(tick)
}

function finish() {
  if (rafId) cancelAnimationFrame(rafId)
  state.value = 'finished'
  if (bestRecord.value === null || clicks.value > bestRecord.value) {
    isNewRecord.value = bestRecord.value !== null
    bestRecord.value = clicks.value
    try { localStorage.setItem(BEST_KEY, String(clicks.value)) } catch { /* 隐私模式忽略 */ }
  }
}

function reset() {
  state.value = 'idle'
  clicks.value = 0
  timeLeft.value = 0
}

async function copyResult() {
  await copy(`【点击计数器】${duration.value} 秒 ${clicks.value} 下（CPS ${cps.value}）\n评级：${grade.value.label}\n——来自 AM Portal 工具页`)
}
</script>

<style scoped>
.clicker-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.toolbar-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
}

.clicker-toolbar .toolbar-label:last-child {
  margin-left: auto;
  color: var(--color-text-muted);
  font-weight: 500;
}

.clicker-stage {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.click-pad {
  min-height: 260px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  -webkit-user-select: none;
  touch-action: manipulation;
  transition: transform 0.05s ease, background-color 0.15s ease;
}

.click-pad.idle:hover {
  background: var(--brand-50);
  border-color: var(--brand-500);
}

.click-pad.running {
  background: var(--brand-50);
  border-color: var(--brand-600);
}

.click-pad.running:active {
  transform: scale(0.985);
}

.click-count {
  font-size: 72px;
  font-weight: 800;
  line-height: 1;
  color: var(--brand-600);
  font-variant-numeric: tabular-nums;
}

.click-hint {
  font-size: var(--text-base);
  color: var(--color-text-muted);
}

.time-bar-wrap {
  height: 8px;
  border-radius: var(--radius-pill);
  background: var(--surface-muted);
  overflow: hidden;
}

.time-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--brand-500), var(--brand-600));
  border-radius: var(--radius-pill);
}

.time-left {
  margin: 0;
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  font-variant-numeric: tabular-nums;
}

/* ---------- 结果 ---------- */
.metric-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.metric-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: var(--space-5) var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--brand-50);
  border: 1px solid color-mix(in srgb, var(--brand-600) 30%, transparent);
}

.metric-value {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--brand-600);
  font-variant-numeric: tabular-nums;
}

.metric-label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  text-align: center;
}

.new-record {
  margin: var(--space-4) 0 0;
  text-align: center;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--el-color-success);
}

.result-actions {
  display: flex;
  gap: var(--space-3);
  justify-content: center;
  margin-top: var(--space-5);
}

@media (max-width: 768px) {
  .metric-cards {
    grid-template-columns: 1fr;
  }
  .click-count {
    font-size: 56px;
  }
}
</style>
