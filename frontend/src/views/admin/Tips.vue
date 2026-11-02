<template>
  <div class="admin-page">
    <div class="page-head">
      <h2 class="admin-page-title">小贴士管理</h2>
      <el-button type="primary" @click="openDialog">新增小贴士</el-button>
    </div>

    <el-table v-loading="loading" :data="tips" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="content" label="内容" min-width="320" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="190" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <div class="op-buttons">
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增小贴士" width="520px">
      <el-form :model="form" label-width="64px">
        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="例如：提交之前先看一眼 git diff"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listTips, addTip, deleteTip } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const tips = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ content: '' })

async function loadTips() {
  loading.value = true
  try {
    const res: any = await listTips()
    tips.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog() {
  form.content = ''
  dialogVisible.value = true
}

async function handleAdd() {
  if (!form.content.trim()) {
    ElMessage.warning('请输入贴士内容')
    return
  }
  await addTip(form.content.trim())
  ElMessage.success('添加成功')
  dialogVisible.value = false
  loadTips()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除这条小贴士？', '提示', { type: 'warning' })
  await deleteTip(id)
  ElMessage.success('删除成功')
  loadTips()
}

onMounted(loadTips)
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

.op-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
}
</style>
