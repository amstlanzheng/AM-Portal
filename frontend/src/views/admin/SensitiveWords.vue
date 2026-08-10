<template>
  <div>
    <div style="display:flex;justify-content:space-between;margin-bottom:20px">
      <h2>敏感词管理</h2>
      <div>
        <el-button type="primary" @click="showAddDialog">新增敏感词</el-button>
        <el-button @click="showBatchDialog">批量导入</el-button>
      </div>
    </div>

    <div style="margin-bottom:16px;display:flex;gap:12px">
      <el-input v-model="searchWord" placeholder="搜索敏感词" style="width:200px" clearable @clear="loadWords" @keyup.enter="loadWords" />
      <el-select v-model="filterCategory" placeholder="分类筛选" clearable style="width:150px" @change="loadWords">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="filterEnabled" placeholder="状态筛选" clearable style="width:120px" @change="loadWords">
        <el-option label="启用" :value="true" />
        <el-option label="禁用" :value="false" />
      </el-select>
      <el-button type="primary" @click="loadWords">查询</el-button>
    </div>

    <el-table :data="words" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="word" label="敏感词" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="isEnabled" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isEnabled ? 'success' : 'danger'">{{ row.isEnabled ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <div class="op-buttons">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button :type="row.isEnabled ? 'warning' : 'success'" size="small" @click="handleToggleEnabled(row)">{{ row.isEnabled ? '禁用' : '启用' }}</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="prev, pager, next" @current-change="loadWords" style="margin-top:16px" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingWord ? '编辑敏感词' : '新增敏感词'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="敏感词"><el-input v-model="form.word" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="选择或输入分类" filterable allow-create style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.isEnabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="batchDialogVisible" title="批量导入敏感词" width="500px">
      <el-form label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="batchCategory" placeholder="选择或输入分类" filterable allow-create style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="敏感词">
          <el-input v-model="batchText" type="textarea" :rows="8" placeholder="每行一个敏感词" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchAdd">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listSensitiveWords, addSensitiveWord, updateSensitiveWord, deleteSensitiveWord, setSensitiveWordEnabled, batchAddSensitiveWords } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const words = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const searchWord = ref('')
const filterCategory = ref('')
const filterEnabled = ref<boolean | ''>('')
const categories = ref<string[]>(['违法', '政治', '色情', '暴力', '广告', '其他'])

const dialogVisible = ref(false)
const editingWord = ref<any>(null)
const form = reactive({ word: '', category: '', isEnabled: true })

const batchDialogVisible = ref(false)
const batchCategory = ref('')
const batchText = ref('')

async function loadWords() {
  const params: any = { pageNum: page.value, pageSize: 20 }
  if (filterCategory.value) params.category = filterCategory.value
  if (filterEnabled.value !== '') params.enabled = filterEnabled.value
  const res: any = await listSensitiveWords(params)
  words.value = res.data?.records || []
  total.value = res.data?.total || 0
  // 提取已有分类
  const cats = new Set(categories.value)
  words.value.forEach((w: any) => { if (w.category) cats.add(w.category) })
  categories.value = Array.from(cats)
}

function showAddDialog() {
  editingWord.value = null
  form.word = ''; form.category = ''; form.isEnabled = true
  dialogVisible.value = true
}

function showEditDialog(row: any) {
  editingWord.value = row
  form.word = row.word; form.category = row.category; form.isEnabled = row.isEnabled
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.word.trim()) { ElMessage.warning('请输入敏感词'); return }
  if (editingWord.value) {
    await updateSensitiveWord({ id: editingWord.value.id, word: form.word.trim(), category: form.category || null, isEnabled: form.isEnabled })
    ElMessage.success('修改成功')
  } else {
    await addSensitiveWord({ word: form.word.trim(), category: form.category || null, isEnabled: form.isEnabled })
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  loadWords()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该敏感词？', '提示', { type: 'warning' })
  await deleteSensitiveWord(id)
  ElMessage.success('删除成功')
  loadWords()
}

async function handleToggleEnabled(row: any) {
  await setSensitiveWordEnabled(row.id, !row.isEnabled)
  ElMessage.success(row.isEnabled ? '已禁用' : '已启用')
  loadWords()
}

function showBatchDialog() {
  batchCategory.value = ''; batchText.value = ''
  batchDialogVisible.value = true
}

async function handleBatchAdd() {
  const lines = batchText.value.split('\n').map(l => l.trim()).filter(l => l)
  if (!lines.length) { ElMessage.warning('请输入敏感词'); return }
  const wordsList = lines.map(w => ({ word: w, category: batchCategory.value || null, isEnabled: true }))
  await batchAddSensitiveWords(wordsList)
  ElMessage.success(`已导入 ${lines.length} 个敏感词`)
  batchDialogVisible.value = false
  loadWords()
}

onMounted(loadWords)
</script>

<style scoped>
.op-buttons { display: flex; gap: 8px; flex-wrap: nowrap; }
</style>
