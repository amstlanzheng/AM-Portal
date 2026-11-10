<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="result !== null">
    <template #panel>
      <div class="calorie-form">
        <div class="form-row">
          <label class="form-label">性别</label>
          <el-radio-group v-model="form.gender">
            <el-radio-button value="male">男</el-radio-button>
            <el-radio-button value="female">女</el-radio-button>
          </el-radio-group>
        </div>

        <div class="form-grid">
          <div class="form-item">
            <label class="form-label">年龄（岁）</label>
            <el-input-number v-model="form.age" :min="10" :max="100" :step="1" controls-position="right" style="width: 100%" />
          </div>
          <div class="form-item">
            <label class="form-label">身高（cm）</label>
            <el-input-number v-model="form.height" :min="100" :max="250" :step="1" controls-position="right" style="width: 100%" />
          </div>
          <div class="form-item">
            <label class="form-label">体重（kg）</label>
            <el-input-number v-model="form.weight" :min="30" :max="250" :step="0.5" controls-position="right" style="width: 100%" />
          </div>
        </div>

        <div class="form-row">
          <label class="form-label">活动强度</label>
          <el-select v-model="form.activity" style="width: 100%">
            <el-option label="久坐（几乎不运动）" value="1.2" />
            <el-option label="轻度（每周运动 1-3 次）" value="1.375" />
            <el-option label="中度（每周运动 3-5 次）" value="1.55" />
            <el-option label="高强度（每周运动 6-7 次）" value="1.725" />
          </el-select>
        </div>

        <div class="form-row">
          <label class="form-label">目标</label>
          <el-radio-group v-model="form.goal">
            <el-radio-button value="maintain">维持</el-radio-button>
            <el-radio-button value="bulk">增肌</el-radio-button>
            <el-radio-button value="cut">减脂</el-radio-button>
          </el-radio-group>
        </div>

        <div v-if="form.goal === 'bulk'" class="form-row">
          <label class="form-label">增肌幅度</label>
          <el-radio-group v-model="form.surplus">
            <el-radio-button value="200">+200 kcal</el-radio-button>
            <el-radio-button value="300">+300 kcal</el-radio-button>
            <el-radio-button value="500">+500 kcal</el-radio-button>
          </el-radio-group>
        </div>

        <div class="form-actions">
          <el-button type="primary" :disabled="!formValid" @click="calculate">计算</el-button>
          <el-button v-if="result" @click="copyResult">{{ copied ? '已复制 ✓' : '复制结果' }}</el-button>
        </div>
        <p v-if="!formValid" class="form-hint">请填写完整且合理的基础信息后再计算（年龄 10-100、身高 100-250cm、体重 30-250kg）。</p>
      </div>
    </template>

    <template #result>
      <div v-if="result" class="calorie-result">
        <div class="metric-cards">
          <div class="metric-card">
            <span class="metric-value">{{ result.bmr }}</span>
            <span class="metric-label">BMR 基础代谢（kcal/天）</span>
          </div>
          <div class="metric-card">
            <span class="metric-value">{{ result.tdee }}</span>
            <span class="metric-label">TDEE 每日总消耗（kcal/天）</span>
          </div>
          <div class="metric-card accent">
            <span class="metric-value">{{ result.targetCalories }}</span>
            <span class="metric-label">{{ goalLabel }}每日热量（kcal/天）</span>
          </div>
        </div>

        <h3 class="section-title">三大营养素建议</h3>
        <div class="macro-bars">
          <div v-for="m in result.macros" :key="m.name" class="macro-row">
            <span class="macro-name">{{ m.name }}</span>
            <div class="macro-track">
              <div class="macro-fill" :style="{ width: m.ratio + '%' }"></div>
            </div>
            <span class="macro-grams">{{ m.grams }} g</span>
            <span class="macro-ratio">{{ m.ratio }}%</span>
          </div>
        </div>

        <p class="explain">{{ result.explain }}</p>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { useCopy } from '../../composables/useCopy'

const tool = findTool('calorie')!
const { copied, copy } = useCopy()

const form = reactive({
  gender: 'male' as 'male' | 'female',
  age: 25,
  height: 175,
  weight: 65,
  activity: '1.375',
  goal: 'bulk' as 'maintain' | 'bulk' | 'cut',
  surplus: 300
})

const formValid = computed(() =>
  form.age >= 10 && form.age <= 100 &&
  form.height >= 100 && form.height <= 250 &&
  form.weight >= 30 && form.weight <= 250
)

const goalLabel = computed(() => ({ maintain: '维持', bulk: '增肌', cut: '减脂' })[form.goal])

interface Macro { key: string; name: string; grams: number; ratio: number }

const result = ref<{
  bmr: number; tdee: number; targetCalories: number; macros: Macro[]; explain: string
} | null>(null)

function calculate() {
  if (!formValid.value) return

  const base = 10 * form.weight + 6.25 * form.height - 5 * form.age
  const bmr = Math.round(form.gender === 'male' ? base + 5 : base - 161)
  const tdee = Math.round(bmr * parseFloat(form.activity))
  // 减脂温和缺口 -300；增肌按所选幅度；维持即 TDEE
  const delta = form.goal === 'bulk' ? form.surplus : form.goal === 'cut' ? -300 : 0
  const target = tdee + delta

  // 蛋白质 1.8g/kg，脂肪 25% 热量，其余碳水
  const proteinG = Math.round(1.8 * form.weight)
  const fatG = Math.round(target * 0.25 / 9)
  const carbG = Math.round((target - proteinG * 4 - fatG * 9) / 4)

  const pKcal = proteinG * 4, cKcal = carbG * 4, fKcal = fatG * 9
  const total = Math.max(1, pKcal + cKcal + fKcal)
  const macros: Macro[] = [
    { key: 'protein', name: '蛋白质', grams: proteinG, ratio: Math.round(pKcal / total * 100) },
    { key: 'carb', name: '碳水化合物', grams: carbG, ratio: Math.round(cKcal / total * 100) },
    { key: 'fat', name: '脂肪', grams: fatG, ratio: Math.round(fKcal / total * 100) }
  ]

  const explain = form.goal === 'bulk'
    ? `在 TDEE 基础上增加 ${form.surplus} kcal 热量盈余，配合力量训练，肌肉增长的同时脂肪增长较慢。若连续两周体重无明显变化，可再上调 100-200 kcal。`
    : form.goal === 'cut'
      ? '在 TDEE 基础上制造 300 kcal 温和热量缺口，减脂期间保持高蛋白摄入以最大限度保留肌肉。'
      : '按每日总消耗进食，保持体重稳定。建议每周固定时间空腹称重观察趋势。'

  result.value = { bmr, tdee, targetCalories: target, macros, explain }
}

async function copyResult() {
  if (!result.value) return
  const r = result.value
  const text = [
    `【${tool.title}】`,
    `性别：${form.gender === 'male' ? '男' : '女'}｜年龄：${form.age}｜身高：${form.height}cm｜体重：${form.weight}kg`,
    `BMR：${r.bmr} kcal｜TDEE：${r.tdee} kcal`,
    `${goalLabel.value}每日热量：${r.targetCalories} kcal`,
    ...r.macros.map(m => `${m.name}：${m.grams}g（${m.ratio}%）`)
  ].join('\n')
  await copy(text)
}
</script>

<style scoped>
.calorie-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
}

.form-actions {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-2);
}

.form-hint {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
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
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  border: 1px solid var(--color-border);
}

.metric-card.accent {
  background: var(--brand-50);
  border-color: color-mix(in srgb, var(--brand-600) 30%, transparent);
}

.metric-value {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-text-strong);
}

.metric-card.accent .metric-value {
  color: var(--brand-600);
}

.metric-label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  text-align: center;
}

.section-title {
  margin: var(--space-5) 0 var(--space-3);
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--color-text-strong);
}

.macro-bars {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.macro-row {
  display: grid;
  grid-template-columns: 90px 1fr 80px 48px;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-sm);
}

.macro-name {
  color: var(--color-text);
  font-weight: 500;
}

.macro-track {
  height: 10px;
  border-radius: var(--radius-pill);
  background: var(--surface-muted);
  overflow: hidden;
}

.macro-fill {
  height: 100%;
  border-radius: var(--radius-pill);
  background: linear-gradient(90deg, var(--brand-500), var(--brand-600));
  transition: width 0.4s ease;
}

.macro-grams {
  text-align: right;
  color: var(--color-text-strong);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.macro-ratio {
  text-align: right;
  color: var(--color-text-muted);
  font-variant-numeric: tabular-nums;
}

.explain {
  margin: var(--space-5) 0 0;
  padding: var(--space-4);
  border-left: 3px solid var(--brand-500);
  background: var(--brand-50);
  border-radius: 0 8px 8px 0;
  font-size: var(--text-sm);
  color: var(--color-text);
  line-height: 1.7;
}

@media (max-width: 768px) {
  .form-grid,
  .metric-cards {
    grid-template-columns: 1fr;
  }
  .macro-row {
    grid-template-columns: 72px 1fr 64px 40px;
  }
}
</style>
