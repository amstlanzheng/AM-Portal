<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="state === 'finished'">
    <template #panel>
      <div class="typing-toolbar">
        <el-radio-group v-model="lang" size="small" :disabled="state !== 'idle'">
          <el-radio-button value="en">英文</el-radio-button>
          <el-radio-button value="zh">中文</el-radio-button>
          <el-radio-button value="code">代码</el-radio-button>
        </el-radio-group>
        <el-radio-group v-model="duration" size="small" :disabled="state !== 'idle'">
          <el-radio-button :value="30">30s</el-radio-button>
          <el-radio-button :value="60">60s</el-radio-button>
          <el-radio-button :value="120">120s</el-radio-button>
        </el-radio-group>
        <div class="typing-meta">
          <span v-if="state === 'running'" class="time-left">{{ timeLeft }}s</span>
          <el-button v-if="state === 'idle'" type="primary" size="small" @click="start">开始</el-button>
          <el-button v-if="state === 'running'" size="small" @click="finish">提前结束</el-button>
          <el-button v-if="state === 'finished'" size="small" @click="reset">再来一次</el-button>
        </div>
      </div>

      <!-- 语料展示 + 隐藏输入 -->
      <div v-if="state !== 'idle'" class="typing-stage" @click="focusInput">
        <p class="passage" ref="passageEl">
          <span
            v-for="(ch, i) in passageChars" :key="i"
            class="ch" :class="{
              correct: i < typed.length && typed[i] === ch,
              wrong: i < typed.length && typed[i] !== ch,
              current: i === typed.length
            }"
          >{{ ch }}</span>
        </p>
        <input
          ref="inputEl" v-model="typed" class="typing-input"
          autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
          @input="onInput" @keydown.tab.prevent
        />
        <p class="input-hint">点击文字区域聚焦输入框，照打上方内容（中文请用输入法上屏）</p>
      </div>
    </template>

    <template #result>
      <div v-if="state === 'finished'" class="typing-result">
        <div class="metric-cards">
          <div class="metric-card">
            <span class="metric-value">{{ stats.wpm }}</span>
            <span class="metric-label">WPM（词/分）</span>
          </div>
          <div class="metric-card">
            <span class="metric-value">{{ stats.cpm }}</span>
            <span class="metric-label">CPM（字符/分）</span>
          </div>
          <div class="metric-card">
            <span class="metric-value">{{ stats.accuracy }}%</span>
            <span class="metric-label">准确率</span>
          </div>
        </div>
        <div class="result-actions">
          <el-button type="primary" @click="copyResult">{{ copied ? '已复制 ✓' : '复制成绩' }}</el-button>
          <el-button @click="reset">再来一次</el-button>
        </div>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { useCopy } from '../../composables/useCopy'

const tool = findTool('typing')!
const { copied, copy } = useCopy()

/* ---------- 语料 ---------- */
const CORPUS: Record<'en' | 'zh' | 'code', string[]> = {
  en: [
    'The quick brown fox jumps over the lazy dog while the sun sets behind the distant mountains, painting the sky in shades of orange and purple.',
    'Programming is not about typing fast; it is about thinking clearly. A well designed system saves hours of debugging and makes everyone happier.',
    'Simplicity is the ultimate sophistication. Keep your code readable, your functions small, and your dependencies few.'
  ],
  zh: [
    '好的代码像好的文章一样，读起来应该流畅自然，让人一眼就能明白它在做什么，而不需要反复琢磨。',
    '把复杂的问题拆解成简单的小问题，逐个击破，是解决大多数难题的通用思路，写代码也不例外。',
    '工具的意义在于节省时间，而节省下来的时间应该用来思考更重要的事情，而不是制造更多的忙碌。'
  ],
  code: [
    'const sum = arr.reduce((acc, cur) => acc + cur, 0);',
    'function debounce(fn, delay) { let timer; return (...args) => { clearTimeout(timer); timer = setTimeout(() => fn(...args), delay); }; }',
    'if (!user?.profile?.name) { throw new Error("missing name"); }'
  ]
}

const lang = ref<'en' | 'zh' | 'code'>('en')
const duration = ref(60)

const state = ref<'idle' | 'running' | 'finished'>('idle')
const passage = ref('')
const typed = ref('')
const timeLeft = ref(0)
const inputEl = ref<HTMLInputElement>()
const passageEl = ref<HTMLParagraphElement>()
let startTime = 0
let timer: number | undefined
let correctCount = 0

const passageChars = computed(() => [...passage.value])

function start() {
  const pool = CORPUS[lang.value]
  passage.value = pool[Math.floor(Math.random() * pool.length)]
  typed.value = ''
  correctCount = 0
  timeLeft.value = duration.value
  state.value = 'running'
  startTime = performance.now()
  nextTick(() => inputEl.value?.focus())
  timer = window.setInterval(() => {
    timeLeft.value--
    if (timeLeft.value <= 0) finish()
  }, 1000)
}

function onInput(e: Event) {
  // 中文输入法组词过程中不比对，上屏后的 input 事件才有效
  if ((e as InputEvent).isComposing) return
  if (state.value !== 'running') return
  // 滚动当前字符进入视野
  const cur = passageEl.value?.querySelector('.ch.current')
  cur?.scrollIntoView({ block: 'nearest' })
  if (typed.value.length >= passage.value.length) finish()
}

function finish() {
  if (state.value !== 'running') return
  if (timer) clearInterval(timer)
  state.value = 'finished'
  const elapsedMin = Math.max((performance.now() - startTime) / 60000, 1 / 60)
  const typedChars = [...typed.value]
  correctCount = 0
  const target = [...passage.value]
  for (let i = 0; i < typedChars.length; i++) {
    if (typedChars[i] === target[i]) correctCount++
  }
  stats.value = {
    wpm: Math.round(correctCount / 5 / elapsedMin),
    cpm: Math.round(correctCount / elapsedMin),
    accuracy: typedChars.length ? Math.round(correctCount / typedChars.length * 100) : 0
  }
}

const stats = ref({ wpm: 0, cpm: 0, accuracy: 0 })

function reset() {
  state.value = 'idle'
  typed.value = ''
  passage.value = ''
  stats.value = { wpm: 0, cpm: 0, accuracy: 0 }
}

function focusInput() {
  inputEl.value?.focus()
}

async function copyResult() {
  const s = stats.value
  await copy(`【打字速度测试】${{ en: '英文', zh: '中文', code: '代码' }[lang.value]} · ${duration.value}s\nWPM：${s.wpm}｜CPM：${s.cpm}｜准确率：${s.accuracy}%\n——来自 AM Portal 工具页`)
}
</script>

<style scoped>
.typing-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.typing-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-left: auto;
}

.time-left {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--brand-600);
  font-variant-numeric: tabular-nums;
  min-width: 56px;
  text-align: right;
}

.typing-stage {
  cursor: text;
}

.passage {
  margin: 0;
  max-height: 220px;
  overflow: auto;
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  font-size: var(--text-lg);
  line-height: 2;
  letter-spacing: 0.02em;
  word-break: break-all;
}

.ch {
  white-space: pre;
}

.ch.correct {
  color: var(--el-color-success);
}

.ch.wrong {
  color: var(--el-color-danger);
  background: color-mix(in srgb, var(--el-color-danger) 12%, transparent);
  border-radius: 3px;
}

.ch.current {
  background: var(--brand-100);
  border-radius: 3px;
  box-shadow: inset 0 -2px 0 var(--brand-500);
}

.typing-input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.input-hint {
  margin: 10px 0 0;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
  text-align: center;
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
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--brand-600);
  font-variant-numeric: tabular-nums;
}

.metric-label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
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
