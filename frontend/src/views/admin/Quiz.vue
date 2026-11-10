<template>
  <div class="admin-page">
    <div class="page-head">
      <h2 class="admin-page-title">MBTI 测试管理</h2>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="题目管理" name="questions">
        <div class="tab-toolbar">
          <el-button type="primary" @click="openQuestionDialog()">新增题目</el-button>
        </div>
        <el-table v-loading="qLoading" :data="questions" border stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="dimension" label="维度" width="90" />
          <el-table-column prop="questionText" label="题干" min-width="260" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.isEnabled ? 'success' : 'info'">{{ row.isEnabled ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <div class="op-buttons">
                <el-button size="small" @click="openQuestionDialog(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDeleteQuestion(row.id)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="结果类型管理" name="resultTypes">
        <div class="tab-toolbar">
          <el-button type="primary" @click="openResultDialog()">新增结果类型</el-button>
        </div>
        <el-table v-loading="rLoading" :data="resultTypes" border stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="code" label="代码" width="90" />
          <el-table-column prop="name" label="名称" width="150" />
          <el-table-column prop="description" label="描述" min-width="260" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <div class="op-buttons">
                <el-button size="small" @click="openResultDialog(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDeleteResult(row.id)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="qDialogVisible" :title="editingQuestionId ? '编辑题目' : '新增题目'" width="720px" :close-on-click-modal="false">
      <el-form :model="qForm" label-width="72px">
        <el-form-item label="维度">
          <el-select v-model="qForm.dimension" placeholder="选择主要考察维度" style="width: 220px">
            <el-option v-for="d in DIMENSIONS" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="题干">
          <el-input
            v-model="qForm.questionText"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="例如：聚会进行到一半，你更可能是哪种状态？"
          />
        </el-form-item>
        <el-form-item label="选项">
          <div class="options-editor">
            <div v-for="(opt, i) in qForm.optionRows" :key="i" class="option-row">
              <span class="opt-index">{{ String.fromCharCode(65 + i) }}</span>
              <div class="opt-text">
                <el-input v-model="opt.text" maxlength="200" placeholder="选项文案" />
              </div>
              <div class="score-list">
                <div v-for="(s, j) in opt.scores" :key="j" class="score-pair">
                  <el-select v-model="s.letter" placeholder="字母" style="width: 88px">
                    <el-option v-for="l in LETTERS" :key="l" :label="l" :value="l" />
                  </el-select>
                  <el-input-number v-model="s.value" :step="1" style="width: 116px" />
                  <el-button type="danger" text size="small" @click="opt.scores.splice(j, 1)">删除</el-button>
                </div>
                <el-button text type="primary" size="small" @click="opt.scores.push({ letter: '', value: 1 })">+ 加分值</el-button>
              </div>
              <el-button type="danger" size="small" :disabled="qForm.optionRows.length <= 2" @click="removeOptionRow(i)">删选项</el-button>
            </div>
            <el-button size="small" @click="addOptionRow">+ 加选项</el-button>
            <div class="options-hint">每个选项至少填写一个「字母 + 分值」；分值可为负数（弱倾向），同一选项可对多个字母计分。</div>
          </div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="qForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="qForm.isEnabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="qDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveQuestion">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rDialogVisible" :title="editingResultId ? '编辑结果类型' : '新增结果类型'" width="560px" :close-on-click-modal="false">
      <el-form :model="rForm" label-width="72px">
        <el-form-item label="代码">
          <el-input v-model="rForm.code" maxlength="10" placeholder="例如：NT" style="width: 200px" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="rForm.name" maxlength="50" placeholder="例如：策略家" style="width: 260px" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="rForm.description"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="该结果类型的性格描述，展示在答题结果页"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="rForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveResult">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  listQuizQuestions, addQuizQuestion, updateQuizQuestion, deleteQuizQuestion,
  listQuizResultTypes, addQuizResultType, updateQuizResultType, deleteQuizResultType
} from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

interface ScorePair { letter: string; value: number | null }
interface OptionRow { text: string; scores: ScorePair[] }

const LETTERS = ['E', 'I', 'N', 'S', 'T', 'F', 'J', 'P']
const DIMENSIONS = [
  { value: 'E', label: 'E · 外向' },
  { value: 'I', label: 'I · 内向' },
  { value: 'N', label: 'N · 直觉' },
  { value: 'S', label: 'S · 实感' },
  { value: 'T', label: 'T · 思考' },
  { value: 'F', label: 'F · 情感' },
  { value: 'J', label: 'J · 判断' },
  { value: 'P', label: 'P · 知觉' }
]

const activeTab = ref('questions')
const questions = ref<any[]>([])
const qLoading = ref(false)
const resultTypes = ref<any[]>([])
const rLoading = ref(false)

const qDialogVisible = ref(false)
const editingQuestionId = ref<number | null>(null)
const qForm = reactive<{ dimension: string; questionText: string; optionRows: OptionRow[]; sortOrder: number; isEnabled: boolean }>({
  dimension: '', questionText: '', optionRows: [], sortOrder: 0, isEnabled: true
})

const rDialogVisible = ref(false)
const editingResultId = ref<number | null>(null)
const rForm = reactive({ code: '', name: '', description: '', sortOrder: 0 })

function newOptionRow(): OptionRow {
  return { text: '', scores: [{ letter: '', value: 1 }] }
}

function parseOptions(json: string): OptionRow[] {
  try {
    const arr = JSON.parse(json || '[]')
    if (!Array.isArray(arr)) return []
    return arr.map((o: any) => ({
      text: o?.text || '',
      scores: Object.entries(o?.scores || {}).map(([letter, value]) => ({ letter, value: Number(value) }))
    }))
  } catch {
    return []
  }
}

function serializeOptions(rows: OptionRow[]): string {
  return JSON.stringify(rows.map(r => ({
    text: r.text.trim(),
    scores: Object.fromEntries(r.scores.filter(s => s.letter && s.value !== null).map(s => [s.letter, s.value]))
  })))
}

async function loadQuestions() {
  qLoading.value = true
  try {
    const res: any = await listQuizQuestions()
    questions.value = res.data || []
  } finally {
    qLoading.value = false
  }
}

async function loadResultTypes() {
  rLoading.value = true
  try {
    const res: any = await listQuizResultTypes()
    resultTypes.value = res.data || []
  } finally {
    rLoading.value = false
  }
}

function openQuestionDialog(row?: any) {
  editingQuestionId.value = row?.id ?? null
  qForm.dimension = row?.dimension ?? ''
  qForm.questionText = row?.questionText ?? ''
  const parsed = row ? parseOptions(row.options) : []
  qForm.optionRows = parsed.length >= 2 ? parsed : [newOptionRow(), newOptionRow()]
  qForm.sortOrder = row?.sortOrder ?? 0
  qForm.isEnabled = row?.isEnabled ?? true
  qDialogVisible.value = true
}

function addOptionRow() {
  qForm.optionRows.push(newOptionRow())
}

function removeOptionRow(i: number) {
  qForm.optionRows.splice(i, 1)
}

async function handleSaveQuestion() {
  if (!qForm.dimension) {
    ElMessage.warning('请选择维度')
    return
  }
  if (!qForm.questionText.trim()) {
    ElMessage.warning('请输入题干')
    return
  }
  if (qForm.optionRows.length < 2) {
    ElMessage.warning('至少需要两个选项')
    return
  }
  for (const [i, opt] of qForm.optionRows.entries()) {
    const label = String.fromCharCode(65 + i)
    if (!opt.text.trim()) {
      ElMessage.warning(`选项 ${label} 的文案不能为空`)
      return
    }
    if (!opt.scores.some(s => s.letter && s.value !== null)) {
      ElMessage.warning(`选项 ${label} 至少填写一个字母分值`)
      return
    }
  }
  const payload = {
    id: editingQuestionId.value ?? undefined,
    dimension: qForm.dimension,
    questionText: qForm.questionText.trim(),
    options: serializeOptions(qForm.optionRows),
    sortOrder: qForm.sortOrder,
    isEnabled: qForm.isEnabled
  }
  await (editingQuestionId.value ? updateQuizQuestion(payload) : addQuizQuestion(payload))
  ElMessage.success('保存成功')
  qDialogVisible.value = false
  loadQuestions()
}

async function handleDeleteQuestion(id: number) {
  await ElMessageBox.confirm('确定删除这道题目？', '提示', { type: 'warning' })
  await deleteQuizQuestion(id)
  ElMessage.success('删除成功')
  loadQuestions()
}

function openResultDialog(row?: any) {
  editingResultId.value = row?.id ?? null
  rForm.code = row?.code ?? ''
  rForm.name = row?.name ?? ''
  rForm.description = row?.description ?? ''
  rForm.sortOrder = row?.sortOrder ?? 0
  rDialogVisible.value = true
}

async function handleSaveResult() {
  if (!rForm.code.trim()) {
    ElMessage.warning('请输入结果代码')
    return
  }
  if (!rForm.name.trim()) {
    ElMessage.warning('请输入结果名称')
    return
  }
  const payload = {
    id: editingResultId.value ?? undefined,
    code: rForm.code.trim(),
    name: rForm.name.trim(),
    description: rForm.description.trim(),
    sortOrder: rForm.sortOrder
  }
  await (editingResultId.value ? updateQuizResultType(payload) : addQuizResultType(payload))
  ElMessage.success('保存成功')
  rDialogVisible.value = false
  loadResultTypes()
}

async function handleDeleteResult(id: number) {
  await ElMessageBox.confirm('确定删除这个结果类型？', '提示', { type: 'warning' })
  await deleteQuizResultType(id)
  ElMessage.success('删除成功')
  loadResultTypes()
}

onMounted(() => {
  loadQuestions()
  loadResultTypes()
})
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.admin-page-title {
  font-size: var(--text-lg);
  font-weight: 650;
}

.tab-toolbar {
  margin-bottom: 16px;
}

.op-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
}

.options-editor {
  width: 100%;
}

.option-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
}

.opt-index {
  flex: none;
  width: 22px;
  text-align: center;
  font-weight: 650;
  color: var(--color-text-muted);
  padding-top: 4px;
}

.opt-text {
  flex: 1;
  min-width: 0;
}

.score-list {
  flex: none;
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
}

.score-pair {
  display: flex;
  gap: 6px;
  align-items: center;
}

.options-hint {
  margin-top: 6px;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
  line-height: 1.5;
}
</style>
