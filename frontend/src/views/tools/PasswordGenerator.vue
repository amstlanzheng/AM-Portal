<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="passwords.length > 0">
    <template #panel>
      <div class="pwd-form">
        <div class="form-row">
          <div class="len-head">
            <label class="form-label">密码长度</label>
            <span class="len-value">{{ length }}</span>
          </div>
          <el-slider v-model="length" :min="8" :max="64" :step="1" show-stops :marks="{ 8: '8', 24: '24', 40: '40', 64: '64' }" />
        </div>

        <div class="form-row checks">
          <el-checkbox v-model="options.upper">大写字母 A-Z</el-checkbox>
          <el-checkbox v-model="options.lower">小写字母 a-z</el-checkbox>
          <el-checkbox v-model="options.digit">数字 0-9</el-checkbox>
          <el-checkbox v-model="options.symbol">符号 !@#$%…</el-checkbox>
        </div>

        <div class="form-row">
          <el-checkbox v-model="excludeAmbiguous">排除易混淆字符（I l 1 O 0 o）</el-checkbox>
        </div>

        <div class="form-actions">
          <el-button type="primary" :disabled="!anyTypeSelected" @click="generate(1)">生成密码</el-button>
          <el-button :disabled="!anyTypeSelected" @click="generate(5)">批量生成 5 个</el-button>
        </div>
        <p v-if="!anyTypeSelected" class="form-hint">请至少勾选一种字符类型。</p>
      </div>
    </template>

    <template #result>
      <div v-if="passwords.length" class="pwd-result">
        <div v-for="(pwd, i) in passwords" :key="i" class="pwd-item" :class="{ main: passwords.length === 1 }">
          <code class="pwd-text">{{ pwd }}</code>
          <el-button size="small" text type="primary" @click="copyPwd(pwd, i)">{{ copied && lastCopied === i ? '已复制 ✓' : '复制' }}</el-button>
        </div>

        <!-- 强度条（单条时展示） -->
        <div v-if="passwords.length === 1" class="strength">
          <div class="strength-track">
            <div class="strength-fill" :class="strength.level" :style="{ width: strength.percent + '%' }"></div>
          </div>
          <span class="strength-label" :class="strength.level">{{ strength.text }}</span>
        </div>
        <p class="strength-note">强度按密码长度与字符集种类估算，仅供快速参考。</p>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { useCopy } from '../../composables/useCopy'

const tool = findTool('password')!
const { copied, copy } = useCopy()

const UPPER = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
const LOWER = 'abcdefghijklmnopqrstuvwxyz'
const DIGIT = '0123456789'
const SYMBOL = '!@#$%^&*()-_=+[]{};:,.<>?/~'
const AMBIGUOUS = new Set(['I', 'l', '1', 'O', '0', 'o'])

const length = ref(16)
const options = reactive({ upper: true, lower: true, digit: true, symbol: true })
const excludeAmbiguous = ref(false)

const anyTypeSelected = computed(() => options.upper || options.lower || options.digit || options.symbol)

const passwords = ref<string[]>([])
const lastCopied = ref(-1)

/** 过滤易混淆字符后的字符集 */
function buildSets(): string[] {
  const sets: string[] = []
  const filter = (s: string) => excludeAmbiguous.value ? [...s].filter(c => !AMBIGUOUS.has(c)).join('') : s
  if (options.upper && filter(UPPER)) sets.push(filter(UPPER))
  if (options.lower && filter(LOWER)) sets.push(filter(LOWER))
  if (options.digit && filter(DIGIT)) sets.push(filter(DIGIT))
  if (options.symbol) sets.push(SYMBOL)
  return sets
}

/** 加密级随机索引：Uint32 取模（偏差 < 字符数/2^32，可忽略） */
function randomIndex(max: number): number {
  const buf = new Uint32Array(1)
  crypto.getRandomValues(buf)
  return buf[0] % max
}

function pick(chars: string): string {
  return chars[randomIndex(chars.length)]
}

function generate(count: number) {
  if (!anyTypeSelected.value) return
  const sets = buildSets()
  const all = sets.join('')

  passwords.value = Array.from({ length: count }, () => {
    // 每类至少一个
    const chars: string[] = sets.map(s => pick(s))
    while (chars.length < length.value) chars.push(pick(all))
    // Fisher-Yates 洗牌
    for (let i = chars.length - 1; i > 0; i--) {
      const j = randomIndex(i + 1)
      ;[chars[i], chars[j]] = [chars[j], chars[i]]
    }
    return chars.join('')
  })
  lastCopied.value = -1
}

const strength = computed(() => {
  if (passwords.value.length !== 1) return { level: '', text: '', percent: 0 }
  const pwd = passwords.value[0]
  const classCount = [/[A-Z]/, /[a-z]/, /\d/, /[^A-Za-z0-9]/].filter(r => r.test(pwd)).length
  const score = (pwd.length >= 16 ? 2 : pwd.length >= 12 ? 1 : 0) + classCount
  if (score >= 5) return { level: 's4', text: '极强', percent: 100 }
  if (score >= 4) return { level: 's3', text: '强', percent: 75 }
  if (score >= 2) return { level: 's2', text: '中', percent: 50 }
  return { level: 's1', text: '弱', percent: 25 }
})

async function copyPwd(pwd: string, index: number) {
  lastCopied.value = index
  await copy(pwd)
}
</script>

<style scoped>
.pwd-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.len-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.len-value {
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--brand-600);
  font-variant-numeric: tabular-nums;
}

.checks {
  flex-direction: row;
  flex-wrap: wrap;
  gap: var(--space-2) var(--space-6);
}

.form-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
}

.form-actions {
  display: flex;
  gap: var(--space-3);
}

.form-hint {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

/* ---------- 结果 ---------- */
.pwd-result {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.pwd-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: 10px var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
}

.pwd-item.main {
  padding: var(--space-4) var(--space-5);
  background: var(--brand-50);
  border-color: color-mix(in srgb, var(--brand-600) 30%, transparent);
}

.pwd-text {
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  color: var(--color-text-strong);
  word-break: break-all;
  line-height: 1.6;
}

.pwd-item.main .pwd-text {
  font-size: var(--text-lg);
  font-weight: 600;
}

/* 强度条 */
.strength {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-2);
}

.strength-track {
  flex: 1;
  height: 8px;
  border-radius: var(--radius-pill);
  background: var(--surface-muted);
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: var(--radius-pill);
  transition: width 0.4s ease;
}

.strength-fill.s1 { background: var(--el-color-danger); }
.strength-fill.s2 { background: var(--el-color-warning); }
.strength-fill.s3 { background: var(--el-color-success); }
.strength-fill.s4 { background: linear-gradient(90deg, var(--brand-500), var(--brand-600)); }

.strength-label {
  font-size: var(--text-sm);
  font-weight: 600;
  min-width: 36px;
  text-align: right;
}

.strength-label.s1 { color: var(--el-color-danger); }
.strength-label.s2 { color: var(--el-color-warning); }
.strength-label.s3 { color: var(--el-color-success); }
.strength-label.s4 { color: var(--brand-600); }

.strength-note {
  margin: 6px 0 0;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
  text-align: center;
}
</style>
