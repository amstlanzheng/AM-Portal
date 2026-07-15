<template>
  <div>
    <div style="display:flex;justify-content:space-between;margin-bottom:20px">
      <h2>博客管理</h2>
      <el-button type="primary" @click="showDialog()">新增博客</el-button>
    </div>
    <el-table :data="blogs" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="isPublished" label="状态" width="80"><template #default="{ row }"><el-tag :type="row.isPublished ? 'success' : 'warning'">{{ row.isPublished ? '已发布' : '草稿' }}</el-tag></template></el-table-column>
      <el-table-column prop="viewCount" label="阅读量" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editingBlog ? '编辑博客' : '新增博客'" width="700px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.categoryId" placeholder="分类ID" /></el-form-item>
        <el-form-item label="Markdown"><el-input v-model="form.markdownContent" type="textarea" :rows="10" /></el-form-item>
        <el-form-item label="HTML"><el-input v-model="form.htmlContent" type="textarea" :rows="10" /></el-form-item>
        <el-form-item label="发布"><el-switch v-model="form.isPublished" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listBlogs, addBlog, updateBlog, deleteBlog, batchDeleteBlogs } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const blogs = ref<any[]>([])
const selectedIds = ref<number[]>([])
const dialogVisible = ref(false)
const editingBlog = ref<any>(null)
const form = reactive({ title: '', summary: '', categoryId: null as number | null, markdownContent: '', htmlContent: '', isPublished: false })

async function loadBlogs() {
  const res: any = await listBlogs({ pageNum: 1, pageSize: 100 })
  blogs.value = res.data?.records || []
}

function showDialog(blog?: any) {
  editingBlog.value = blog || null
  if (blog) { Object.assign(form, blog) } else { Object.keys(form).forEach(k => (form as any)[k] = k === 'isPublished' ? false : k === 'categoryId' ? null : '') }
  dialogVisible.value = true
}

async function handleSave() {
  if (editingBlog.value) { await updateBlog({ ...form, id: editingBlog.value.id }) } else { await addBlog(form) }
  ElMessage.success('保存成功'); dialogVisible.value = false; loadBlogs()
}

async function handleDelete(id: number) { await ElMessageBox.confirm('确定删除？'); await deleteBlog(id); ElMessage.success('删除成功'); loadBlogs() }

onMounted(loadBlogs)
</script>
