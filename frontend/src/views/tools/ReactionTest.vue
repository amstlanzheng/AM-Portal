<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="state === 'finished'">
    <template #panel>
      <div
        class="reaction-pad" :class="padClass"
        @click="onPadClick"
        @touchstart.prevent="onPadClick"
      >
        <template v-if="state === 'idle'">
          <p class="pad-title">反应力测试</p>
          <p class="pad-sub">点击任意处开始 · 共 5 轮取平均</p>
        </template>
        <template v-else-if="state === 'waiting'">
          <p class="pad-title">等待变绿…</p>
          <p class="pad-sub">别急，提前点击算犯规</p>
        </template>
        <template v-else-if="state === 'ready'">
          <p class="pad-title">现在点！</p>
        </template>
        <template v-else-if="state === 'foul'">
          <p class="pad-title">太急了！</p>
          <p class="pad-sub">提前点击犯规，本轮重来 · 点击继续</p>
        </template>
        <template v-else-if="state === 'recorded'">
          <p class="pad-title">{{ lastMs }} ms</p>
          <p class="pad-sub">第 {{ results.length }}/{{ ROUNDS }} 轮 · 点击继续下一轮</p>
        </template>
        <template v-else>
          <p class="pad-title">{{ grade.label }}</p>
          <p class="pad-sub">平均 {{ avgMs }} ms · 点击重测</p>
        </template>
      </div>

      <div v-if="results.length" class="round-dots">
        <span
          v-for="i in ROUNDS" :key="i"
          class="dot" :class="{ filled: i <= results.length, best: i === bestRound }"
          :title="results[i - 1] ? `第 ${i} 轮：${results[i - 1]} ms` : `第 ${i} 轮`"
        ></span>
      </div>
    </template>

    <template #result>
      <div v-if="state === 'finished'" class="reaction-result">
        <div class="metric-cards">
          <div class="metric-card">
            <span class="metric-value">{{ avgMs }}</span>
            <span class="metric-label">平均反应（ms）</span>
          </div>
          <div class="metric-card">
            <span class="metric-value">{{ bestMs }}</span>
            <span class="metric-label">最佳单次（ms）</span>
          </div>
          <div class="metric-card">
            <span class="metric-value">{{ grade.label }}</span>
            <span class="metric-label">{{ grade.desc }}</span>
          </div>
        </div>
        <p class="rounds-line">各轮成绩：{{ results.join(' / ') }} ms</p>
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

const tool = findTool('reaction')!
const { copied, copy } = useCopy()

const ROUNDS = 5

type State = 'idle' | 'waiting' | 'ready' | 'foul' | 'recorded' | 'finished'
const state = ref<State>('idle')
const results = ref<number[]>([])
const lastMs = ref(0)

let greenAt = 0
let delayTimer: number | undefined

const padClass = computed(() => ({
  idle: state.value === 'idle',
  waiting: state.value === 'waiting',
  ready: state.value === 'ready',
  foul: state.value === 'foul',
  recorded: state.value === 'recorded',
  finished: state.value === 'finished'
}))

const avgMs = computed(() => results.value.length
  ? Math.round(results.value.reduce((a, b) => a + b, 0) / results.value.length) : 0)
const bestMs = computed(() => results.value.length ? Math.min(...results.value) : 0)
const bestRound = computed(() => {
  if (!results.value.length) return 0
  return results.value.indexOf(bestMs.value) + 1
})

const grade = computed(() => {
  const ms = avgMs.value
  if (ms > 0 && ms < 200) return { label: '闪电级 ⚡', desc: '快如闪电，天生的电竞选手' }
  if (ms < 280) return { label: '常人级 🙂', desc: '普通人类的正常水平' }
  return { label: '树懒级 🦥', desc: '慢悠悠也是一种生活态度' }
})

function startRound() {
  state.value = 'waiting'
  // 随机 1-4s 延迟防预判
  const delay = 1000 + Math.random() * 3000
  delayTimer = window.setTimeout(() => {
    state.value = 'ready'
    greenAt = performance.now()
  }, delay)
}

function onPadClick() {
  switch (state.value) {
    case 'idle':
      startRound()
      break
    case 'waiting':
      // 提前点击犯规
      if (delayTimer) clearTimeout(delayTimer)
      state.value = 'foul'
      break
    case 'ready': {
      lastMs.value = Math.round(performance.now() - greenAt)
      results.value.push(lastMs.value)
      state.value = results.value.length >= ROUNDS ? 'finished' : 'recorded'
      break
    }
    case 'foul':
    case 'recorded':
      startRound()
      break
    case 'finished':
      reset()
      startRound()
      break
  }
}

function reset() {
  if (delayTimer) clearTimeout(delayTimer)
  results.value = []
  state.value = 'idle'
}

async function copyResult() {
  await copy(`【反应力测试】平均 ${avgMs.value} ms（最佳 ${bestMs.value} ms）\n各轮：${results.value.join(' / ')} ms\n评级：${grade.value.label}\n——来自 AM Portal 工具页`)
}
</script>

<style scoped>
.reaction-pad {
  min-height: 300px;
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.08s ease;
  border: 1px solid var(--color-border);
}

.reaction-pad.idle,
.reaction-pad.recorded,
.reaction-pad.finished {
  background: var(--surface-muted);
}

.reaction-pad.waiting,
.reaction-pad.foul {
  background: var(--el-color-danger);
  border-color: var(--el-color-danger);
}

.reaction-pad.ready {
  background: var(--el-color-success);
  border-color: var(--el-color-success);
}

.pad-title {
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-text-strong);
}

.waiting .pad-title,
.foul .pad-title { color: #fff; }
.ready .pad-title { color: #fff; text-shadow: 0 1px 3px rgba(0, 0, 0, 0.35); }

.pad-sub {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.waiting .pad-sub,
.foul .pad-sub { color: rgba(255, 255, 255, 0.85); }
.ready .pad-sub { color: #fff; }

.round-dots {
  display: flex;
  justify-content: center;
  gap: var(--space-2);
  margin-top: var(--space-4);
}

.dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--surface-muted);
  border: 1px solid var(--color-border);
}

.dot.filled {
  background: var(--brand-500);
  border-color: var(--brand-600);
}

.dot.best {
  background: var(--el-color-success);
  border-color: var(--el-color-success);
  transform: scale(1.25);
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

.rounds-line {
  margin: var(--space-4) 0 0;
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  font-variant-numeric: tabular-nums;
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
}
</style>
