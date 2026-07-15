<template>
  <div>
    <h2 style="margin-bottom:20px">文件管理</h2>
    <div style="margin-bottom:16px;display:flex;gap:12px;align-items:center">
      <el-upload :show-file-list="false" :before-upload="handleUpload" accept="image/*"><el-button type="primary">上传文件</el-button></el-upload>
      <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除{{ selectedIds.length ? `(${selectedIds.length})` : '' }}</el-button>
      <el-input v-model="keyword" placeholder="搜索文件名" style="width:200px" @keyup.enter="loadFiles" />
      <el-select v-model="directory" placeholder="目录" clearable style="width:150px" @change="loadFiles">
        <el-option label="头像" value="avatar" /><el-option label="项目" value="project" /><el-option label="博客" value="blog" /><el-option label="其他" value="other" />
      </el-select>
    </div>
    <el-table :data="files" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="originalName" label="文件名" show-overflow-tooltip />
      <el-table-column prop="directory" label="目录" width="100" />
      <el-table-column prop="fileSize" label="大小" width="100"><template #default="{ row }">{{ (row.fileSize / 1024).toFixed(1) }} KB</template></el-table-column>
      <el-table-column prop="fileType" label="类型" width="80" />
      <el-table-column prop="uploadTime" label="上传时间" width="170" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <div class="op-buttons">
            <el-button size="small" @click="handlePreview(row)">查看</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadFiles" style="margin-top:16px" />

    <el-dialog v-model="previewVisible" title="文件预览" width="700px">
      <div class="preview-box">
        <img v-if="isImage(previewFile)" :src="previewFile?.url" alt="预览" />
        <div v-else class="no-preview">
          <p>该文件类型无法预览</p>
          <el-button type="primary" @click="openUrl(previewFile?.url)">在新窗口打开</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listFiles, uploadFile, deleteFile, batchDeleteFiles } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const files = ref<any[]>([])
const selectedIds = ref<number[]>([])
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const directory = ref('')
const previewVisible = ref(false)
const previewFile = ref<any>(null)

const IMAGE_EXTS = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']

function isImage(file: any): boolean {
  if (!file) return false
  const ext = (file.fileType || '').toLowerCase()
  if (IMAGE_EXTS.includes(ext)) return true
  if (ext.startsWith('image/')) return true
  const name = (file.originalName || '').toLowerCase()
  return IMAGE_EXTS.some(e => name.endsWith('.' + e))
}

function openUrl(url?: string) {
  if (url) window.open(url, '_blank')
}

async function loadFiles() {
  const res: any = await listFiles({ pageNum: page.value, pageSize: 10, keyword: keyword.value, directory: directory.value })
  files.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handleUpload(file: File) {
  await uploadFile(file, directory.value || 'other')
  ElMessage.success('上传成功')
  loadFiles()
  return false
}

function handlePreview(row: any) {
  previewFile.value = row
  previewVisible.value = true
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除？')
  await deleteFile(id)
  ElMessage.success('删除成功')
  loadFiles()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个文件？`, '批量删除', { type: 'warning' })
  await batchDeleteFiles(selectedIds.value)
  ElMessage.success('批量删除成功')
  selectedIds.value = []
  loadFiles()
}

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}

onMounted(loadFiles)
</script>

<style scoped>
.op-buttons { display: flex; gap: 8px; flex-wrap: nowrap; }
.preview-box { text-align: center; }
.preview-box img { max-width: 100%; max-height: 500px; }
.no-preview { padding: 40px; color: #999; }
</style>
