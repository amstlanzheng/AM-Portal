<template>
  <div>
    <div style="display:flex;justify-content:space-between;margin-bottom:20px">
      <h2>Prompt 管理</h2>
      <el-button type="primary" @click="showDialog()">新增 Prompt</el-button>
    </div>
    <el-table :data="prompts" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="isDefault" label="默认" width="80"><template #default="{ row }"><el-tag :type="row.isDefault ? 'danger' : 'info'">{{ row.isDefault ? '是' : '否' }}</el-tag></template></el-table-column>
      <el-table-column prop="isEnabled" label="启用" width="80"><template #default="{ row }"><el-tag :type="row.isEnabled ? 'success' : 'info'">{{ row.isEnabled ? '是' : '否' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="340">
        <template #default="{ row }">
          <div class="op-buttons">
            <el-button size="small" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.isEnabled ? 'warning' : 'success'" @click="handleToggleEnabled(row)">{{ row.isEnabled ? '停用' : '启用' }}</el-button>
            <el-button size="small" :type="row.isDefault ? 'info' : 'danger'" @click="handleSetDefault(row.id)" :disabled="row.isDefault">{{ row.isDefault ? '已默认' : '设为默认' }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editingPrompt ? '编辑 Prompt' : '新增 Prompt'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
        <el-form-item label="默认"><el-switch v-model="form.isDefault" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.isEnabled" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listPrompts, addPrompt, updatePrompt, deletePrompt, setPromptEnabled, setPromptDefault } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const prompts = ref<any[]>([])
const dialogVisible = ref(false)
const editingPrompt = ref<any>(null)
const form = reactive({ name: '', content: '', isDefault: false, isEnabled: true })

async function loadPrompts() {
  const res: any = await listPrompts({ pageNum: 1, pageSize: 100 })
  prompts.value = res.data?.records || []
}

function showDialog(prompt?: any) {
  editingPrompt.value = prompt || null
  if (prompt) { Object.assign(form, prompt) } else { form.name = ''; form.content = ''; form.isDefault = false; form.isEnabled = true }
  dialogVisible.value = true
}

async function handleSave() {
  if (editingPrompt.value) { await updatePrompt({ ...form, id: editingPrompt.value.id }) } else { await addPrompt(form) }
  ElMessage.success('保存成功'); dialogVisible.value = false; loadPrompts()
}

async function handleDelete(id: number) { await ElMessageBox.confirm('确定删除？'); await deletePrompt(id); ElMessage.success('删除成功'); loadPrompts() }
async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个 Prompt？`, '批量删除', { type: 'warning' })
  await batchDeletePrompts(selectedIds.value)
  ElMessage.success('批量删除成功'); selectedIds.value = []; loadPrompts()
}
function handleSelectionChange(rows: any[]) { selectedIds.value = rows.map(r => r.id) }
async function handleToggleEnabled(row: any) { await setPromptEnabled(row.id, !row.isEnabled); loadPrompts() }
async function handleSetDefault(id: number) { await setPromptDefault(id); ElMessage.success('设置成功'); loadPrompts() }

onMounted(loadPrompts)
</script>

<style scoped>
.op-buttons { display: flex; gap: 8px; flex-wrap: nowrap; }
</style>
