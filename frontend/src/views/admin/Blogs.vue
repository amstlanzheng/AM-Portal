<template>
  <div>
    <div style="display:flex;justify-content:space-between;margin-bottom:20px">
      <h2>博客管理</h2>
      <el-button type="primary" @click="showDialog()">新增博客</el-button>
    </div>
    <el-table :data="blogs" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column prop="isPublished" label="发布" width="70">
        <template #default="{ row }"><el-tag :type="row.isPublished ? 'success' : 'warning'" size="small">{{ row.isPublished ? '已发布' : '草稿' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="reviewStatus" label="审核" width="90">
        <template #default="{ row }">
          <el-tag :type="reviewTagType(row.reviewStatus)" size="small">{{ reviewLabel(row.reviewStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读" width="60" />
      <el-table-column label="操作" width="400">
        <template #default="{ row }">
          <div class="op-buttons">
            <el-button size="small" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" type="info" @click="handleDetect(row)">敏感词</el-button>
            <el-button v-if="row.reviewStatus !== 'APPROVED'" type="success" size="small" @click="handleReview(row.id, 'APPROVED')">通过</el-button>
            <el-button v-if="row.reviewStatus !== 'REJECTED'" type="danger" size="small" @click="handleReject(row)">驳回</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadBlogs" style="margin-top:16px" />

    <!-- 新增/编辑弹窗 -->
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

    <!-- 敏感词检测结果弹窗 -->
    <el-dialog v-model="detectDialogVisible" title="敏感词检测结果" width="650px">
      <div v-if="detectedWords.length === 0" style="text-align:center;padding:20px;color:#67c23a">
        <el-icon style="font-size:48px"><Check /></el-icon>
        <p style="margin-top:10px;font-size:16px">未检测到敏感词</p>
      </div>
      <div v-else>
        <p style="margin-bottom:12px;color:#e6a23c">检测到 {{ detectedWords.length }} 个敏感词：</p>
        <div style="margin-bottom:16px">
          <el-tag v-for="w in detectedWords" :key="w" type="danger" style="margin:0 6px 6px 0">{{ w }}</el-tag>
        </div>
        <el-divider />
        <p style="margin-bottom:8px;font-weight:bold">文章内容（敏感词已高亮）：</p>
        <div class="highlight-content" v-html="highlightedContent"></div>
      </div>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="450px">
      <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="请输入驳回原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listAdminBlogs, addBlog, updateBlog, deleteBlog, reviewBlog, detectSensitiveWords } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'

const blogs = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const editingBlog = ref<any>(null)
const form = reactive({ title: '', summary: '', categoryId: null as number | null, markdownContent: '', htmlContent: '', isPublished: false })

const detectDialogVisible = ref(false)
const detectedWords = ref<string[]>([])
const highlightedContent = ref('')

const rejectDialogVisible = ref(false)
const rejectBlogId = ref(0)
const rejectRemark = ref('')

function reviewTagType(status: string) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}
function reviewLabel(status: string) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '待审核'
}

async function loadBlogs() {
  const res: any = await listAdminBlogs({ pageNum: page.value, pageSize: 10 })
  blogs.value = res.data?.records || []
  total.value = res.data?.total || 0
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

async function handleReview(id: number, status: string) {
  await reviewBlog(id, { reviewStatus: status })
  ElMessage.success('审核通过')
  loadBlogs()
}

function handleReject(row: any) {
  rejectBlogId.value = row.id
  rejectRemark.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  await reviewBlog(rejectBlogId.value, { reviewStatus: 'REJECTED', reviewRemark: rejectRemark.value })
  rejectDialogVisible.value = false
  ElMessage.success('已驳回')
  loadBlogs()
}

async function handleDetect(row: any) {
  const text = [row.title, row.summary, row.markdownContent, row.htmlContent].filter(Boolean).join(' ')
  const res: any = await detectSensitiveWords(text)
  detectedWords.value = res.data || []
  if (detectedWords.value.length > 0) {
    highlightedContent.value = highlightText(text, detectedWords.value)
  } else {
    highlightedContent.value = ''
  }
  detectDialogVisible.value = true
}

function highlightText(text: string, words: string[]): string {
  let result = text.replace(/</g, '&lt;').replace(/>/g, '&gt;')
  for (const w of words) {
    const escaped = w.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    const regex = new RegExp(escaped, 'gi')
    result = result.replace(regex, '<span style="background:#f56c6c;color:#fff;padding:0 4px;border-radius:3px">$&</span>')
  }
  return result.replace(/\n/g, '<br/>')
}

onMounted(loadBlogs)
</script>

<style scoped>
.op-buttons { display: flex; gap: 8px; flex-wrap: nowrap; }
.highlight-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.8;
  word-break: break-all;
}
</style>
