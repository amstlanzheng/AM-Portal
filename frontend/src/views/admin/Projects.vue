<template>
  <div>
    <div style="display:flex;justify-content:space-between;margin-bottom:20px">
      <h2>项目管理</h2>
      <el-button type="primary" @click="showDialog()">新增项目</el-button>
    </div>
    <el-table :data="projects" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="isTop" label="置顶" width="80"><template #default="{ row }"><el-tag :type="row.isTop ? 'danger' : 'info'">{{ row.isTop ? '是' : '否' }}</el-tag></template></el-table-column>
      <el-table-column prop="isVisible" label="显示" width="80"><template #default="{ row }"><el-tag :type="row.isVisible ? 'success' : 'info'">{{ row.isVisible ? '是' : '否' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="340">
        <template #default="{ row }">
          <div class="op-buttons">
            <el-button size="small" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.isTop ? 'warning' : 'success'" @click="handleToggleTop(row)">{{ row.isTop ? '取消置顶' : '置顶' }}</el-button>
            <el-button size="small" :type="row.isVisible ? 'warning' : 'success'" @click="handleToggleVisible(row)">{{ row.isVisible ? '隐藏' : '显示' }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editingProject ? '编辑项目' : '新增项目'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="封面">
          <div style="display:flex;align-items:center;gap:12px">
            <el-image v-if="form.cover" :src="form.cover" style="width:120px;height:80px;border-radius:4px" fit="cover" />
            <el-upload :show-file-list="false" :before-upload="handleCoverUpload" accept="image/*">
              <el-button>{{ form.cover ? '更换封面' : '上传封面' }}</el-button>
            </el-upload>
            <el-button v-if="form.cover" link type="danger" @click="form.cover = ''">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="简介"><el-input v-model="form.summary" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" placeholder="逗号分隔" /></el-form-item>
        <el-form-item label="Github"><el-input v-model="form.githubUrl" /></el-form-item>
        <el-form-item label="Gitee"><el-input v-model="form.giteeUrl" /></el-form-item>
        <el-form-item label="演示地址"><el-input v-model="form.demoUrl" /></el-form-item>
        <el-form-item label="下载地址"><el-input v-model="form.downloadUrl" /></el-form-item>
        <el-form-item label="Markdown"><el-input v-model="form.markdownContent" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="HTML"><el-input v-model="form.htmlContent" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listProjects, addProject, updateProject, deleteProject, setProjectTop, setProjectVisible, uploadFile } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const projects = ref<any[]>([])
const dialogVisible = ref(false)
const editingProject = ref<any>(null)
const form = reactive({ name: '', cover: '', summary: '', tags: '', githubUrl: '', giteeUrl: '', demoUrl: '', downloadUrl: '', markdownContent: '', htmlContent: '' })

async function loadProjects() {
  const res: any = await listProjects({ pageNum: 1, pageSize: 100 })
  projects.value = res.data?.records || []
}

function showDialog(project?: any) {
  editingProject.value = project || null
  if (project) { Object.assign(form, project) } else { Object.keys(form).forEach(k => (form as any)[k] = '') }
  dialogVisible.value = true
}

async function handleCoverUpload(file: File) {
  const res: any = await uploadFile(file, 'project')
  form.cover = res.data?.url || ''
  ElMessage.success('封面上传成功')
  return false
}

async function handleSave() {
  if (editingProject.value) { await updateProject({ ...form, id: editingProject.value.id }) } else { await addProject(form) }
  ElMessage.success('保存成功'); dialogVisible.value = false; loadProjects()
}

async function handleDelete(id: number) { await ElMessageBox.confirm('确定删除？'); await deleteProject(id); ElMessage.success('删除成功'); loadProjects() }
async function handleToggleTop(row: any) { await setProjectTop(row.id, !row.isTop); loadProjects() }
async function handleToggleVisible(row: any) { await setProjectVisible(row.id, !row.isVisible); loadProjects() }

onMounted(loadProjects)
</script>

<style scoped>
.op-buttons { display: flex; gap: 8px; flex-wrap: nowrap; }
</style>
