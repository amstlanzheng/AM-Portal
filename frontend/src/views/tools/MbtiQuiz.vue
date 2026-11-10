<template>
  <ToolPageLayout
    :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon"
    :result-ready="phase === 'result'" extra-note="本测试为娱乐向，非心理诊断。"
  >
    <template #panel>
      <!-- 加载中 -->
      <div v-if="phase === 'loading'" class="quiz-state">
        <el-icon class="is-loading" :size="28"><Loading /></el-icon>
        <p>正在加载题目…</p>
      </div>

      <!-- 加载失败 -->
      <div v-else-if="phase === 'error'" class="quiz-state error">
        <p>题目加载失败：{{ errorMsg }}</p>
        <el-button @click="load">重试</el-button>
      </div>

      <!-- 答题中 -->
      <div v-else-if="phase === 'quiz'" class="quiz-stage">
        <div class="quiz-progress">
          <el-progress :percentage="progressPercent" :stroke-width="10" :show-text="false" />
          <span class="progress-text">{{ currentIndex + 1 }} / {{ questions.length }}</span>
        </div>

        <transition name="q-fade" mode="out-in">
          <div :key="currentQuestion.id" class="quiz-card">
            <p class="quiz-question">{{ currentQuestion.questionText }}</p>
            <button
              v-for="(opt, i) in currentOptions" :key="i"
              class="quiz-option" :class="{ selected: answers[currentIndex] === i }"
              @click="selectOption(i)"
            >
              <span class="opt-letter">{{ 'ABCD'[i] }}</span>
              <span class="opt-text">{{ opt.text }}</span>
            </button>
          </div>
        </transition>

        <div class="quiz-nav">
          <el-button :disabled="currentIndex === 0" @click="currentIndex--">上一题</el-button>
          <el-button v-if="currentIndex < questions.length - 1" type="primary" :disabled="answers[currentIndex] === undefined" @click="currentIndex++">下一题</el-button>
          <el-button v-else type="primary" :disabled="answers[currentIndex] === undefined" @click="finish">查看结果</el-button>
        </div>
      </div>

      <!-- 结果 -->
      <div v-else-if="phase === 'result'" class="result-summary">
        <p class="result-code">{{ resultCode }}</p>
        <p class="result-name">{{ resultType?.name || '未知类型' }}</p>
        <el-button type="primary" @click="restart">再测一次</el-button>
      </div>
    </template>

    <template #result>
      <div v-if="phase === 'result' && resultType" class="mbti-result">
        <p class="result-desc">{{ resultType.description }}</p>

        <h3 class="section-title">你的字母倾向</h3>
        <div class="letter-bars">
          <div v-for="l in sortedLetters" :key="l" class="letter-row">
            <span class="letter-name">{{ l }}</span>
            <div class="letter-track">
              <div class="letter-fill" :style="{ width: letterPercent(l) + '%' }"></div>
            </div>
            <span class="letter-score">{{ letterScores[l] || 0 }}</span>
          </div>
        </div>

        <div class="result-actions">
          <el-button type="primary" @click="copyResult">{{ copied ? '已复制 ✓' : '复制成绩' }}</el-button>
          <el-button @click="restart">再测一次</el-button>
        </div>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { useCopy } from '../../composables/useCopy'
import { getQuizQuestions, getQuizResultTypes } from '../../api'

const tool = findTool('mbti')!
const { copied, copy } = useCopy()

interface QuizOption { text: string; scores: Record<string, number> }
interface QuizQuestion { id: number; questionText: string; options: string }
interface QuizResultType { code: string; name: string; description: string }

const phase = ref<'loading' | 'error' | 'quiz' | 'result'>('loading')
const errorMsg = ref('')
const questions = ref<QuizQuestion[]>([])
const resultTypes = ref<QuizResultType[]>([])
const currentIndex = ref(0)
const answers = ref<Record<number, number>>({})

const LETTERS = ['E', 'I', 'N', 'S', 'T', 'F', 'J', 'P']
/** 同分破平权重：E > N > T > J > I > S > F > P */
const TIE_PRIORITY: Record<string, number> = { E: 8, N: 7, T: 6, J: 5, I: 4, S: 3, F: 2, P: 1 }

async function load() {
  phase.value = 'loading'
  errorMsg.value = ''
  try {
    const [q, t]: any = await Promise.all([getQuizQuestions(), getQuizResultTypes()])
    questions.value = (q.data || []).map((x: any) => ({ ...x }))
    resultTypes.value = t.data || []
    if (!questions.value.length) {
      phase.value = 'error'
      errorMsg.value = '题库为空，请联系管理员在后台添加题目'
      return
    }
    phase.value = 'quiz'
  } catch (e: any) {
    phase.value = 'error'
    errorMsg.value = e?.message || '网络异常'
  }
}

const currentQuestion = computed(() => questions.value[currentIndex.value])

const currentOptions = computed<QuizOption[]>(() => {
  try { return JSON.parse(currentQuestion.value?.options || '[]') } catch { return [] }
})

const progressPercent = computed(() =>
  Math.round((Object.keys(answers.value).length / questions.value.length) * 100)
)

function selectOption(i: number) {
  answers.value[currentIndex.value] = i
  // 自动进入下一题（最后一题停留）
  if (currentIndex.value < questions.value.length - 1) {
    setTimeout(() => currentIndex.value++, 260)
  }
}

/* ---------- 计分：8 字母独立累计，取最高 2 个，同分按权重破平 ---------- */
const letterScores = computed<Record<string, number>>(() => {
  const scores: Record<string, number> = {}
  for (let qi = 0; qi < questions.value.length; qi++) {
    const ai = answers.value[qi]
    if (ai === undefined) continue
    let opts: QuizOption[] = []
    try { opts = JSON.parse(questions.value[qi].options) } catch { continue }
    const opt = opts[ai]
    if (!opt?.scores) continue
    for (const [letter, weight] of Object.entries(opt.scores)) {
      scores[letter] = (scores[letter] || 0) + (Number(weight) || 0)
    }
  }
  return scores
})

const sortedLetters = computed(() =>
  [...LETTERS].sort((a, b) =>
    (letterScores.value[b] || 0) - (letterScores.value[a] || 0) ||
    (TIE_PRIORITY[b] || 0) - (TIE_PRIORITY[a] || 0)
  )
)

const resultCode = computed(() => (sortedLetters.value[0] || '') + (sortedLetters.value[1] || ''))

const resultType = computed(() =>
  resultTypes.value.find(t => t.code === resultCode.value)
)

function letterPercent(letter: string): number {
  const max = Math.max(...Object.values(letterScores.value), 1)
  return Math.round(((letterScores.value[letter] || 0) / max) * 100)
}

function finish() {
  phase.value = 'result'
}

function restart() {
  answers.value = {}
  currentIndex.value = 0
  phase.value = 'quiz'
}

async function copyResult() {
  const rt = resultType.value
  const lines = [
    `【MBTI 变体测试】${resultCode}「${rt?.name || '未知类型'}」`,
    rt?.description || '',
    '',
    '字母倾向：',
    ...sortedLetters.value.map(l => `${l}：${letterScores.value[l] || 0} 分`),
    '',
    '——来自 AM Portal 工具页（娱乐向测试）'
  ]
  await copy(lines.filter(x => x !== undefined).join('\n'))
}

onMounted(load)
</script>

<style scoped>
.quiz-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-8) 0;
  color: var(--color-text-muted);
}

.quiz-state.error p {
  margin: 0;
  color: var(--el-color-danger);
}

.quiz-stage {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.quiz-progress {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.quiz-progress :deep(.el-progress) {
  flex: 1;
}

.progress-text {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}

.quiz-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.quiz-question {
  margin: 0 0 var(--space-1);
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-text-strong);
  line-height: 1.6;
}

.quiz-option {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface);
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  font-size: var(--text-base);
  color: var(--color-text);
  transition: border-color 0.15s ease, background-color 0.15s ease, transform 0.15s ease;
}

.quiz-option:hover {
  border-color: var(--brand-500);
  transform: translateX(2px);
}

.quiz-option.selected {
  border-color: var(--brand-600);
  background: var(--brand-50);
  color: var(--brand-600);
  font-weight: 600;
}

.opt-letter {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: var(--text-xs);
  font-weight: 700;
  background: var(--surface-muted);
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.quiz-option.selected .opt-letter {
  background: var(--brand-600);
  color: #fff;
}

.opt-text {
  line-height: 1.5;
}

.quiz-nav {
  display: flex;
  justify-content: space-between;
}

/* ---------- 结果摘要（panel 区） ---------- */
.result-summary {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: var(--space-4) 0;
}

.result-code {
  margin: 0;
  font-size: 56px;
  font-weight: 800;
  letter-spacing: 0.05em;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.result-name {
  margin: 0 0 var(--space-2);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-text-strong);
}

/* ---------- 结果详情 ---------- */
.result-desc {
  margin: 0;
  padding: var(--space-4);
  border-left: 3px solid var(--brand-500);
  background: var(--brand-50);
  border-radius: 0 8px 8px 0;
  font-size: var(--text-base);
  line-height: 1.8;
  color: var(--color-text);
}

.section-title {
  margin: var(--space-5) 0 var(--space-3);
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--color-text-strong);
}

.letter-bars {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.letter-row {
  display: grid;
  grid-template-columns: 28px 1fr 48px;
  align-items: center;
  gap: var(--space-3);
}

.letter-name {
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--brand-600);
  text-align: center;
}

.letter-track {
  height: 10px;
  border-radius: var(--radius-pill);
  background: var(--surface-muted);
  overflow: hidden;
}

.letter-fill {
  height: 100%;
  border-radius: var(--radius-pill);
  background: linear-gradient(90deg, var(--brand-500), var(--brand-600));
  transition: width 0.5s ease;
}

.letter-score {
  text-align: right;
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

/* 题目切换动画 */
.q-fade-enter-active,
.q-fade-leave-active { transition: opacity 0.18s ease, transform 0.18s ease; }
.q-fade-enter-from { opacity: 0; transform: translateX(12px); }
.q-fade-leave-to { opacity: 0; transform: translateX(-12px); }
</style>
