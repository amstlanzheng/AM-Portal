<template>
  <div>
    <h2 style="margin-bottom:20px">用户管理</h2>
    <el-table :data="users" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ENABLED' ? 'success' : row.status === 'PENDING' ? 'warning' : 'danger'">{{ row.status === 'ENABLED' ? '启用' : row.status === 'PENDING' ? '待审核' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isAdmin" label="管理员" width="80">
        <template #default="{ row }"><el-tag :type="row.isAdmin ? 'danger' : 'info'">{{ row.isAdmin ? '是' : '否' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="440">
        <template #default="{ row }">
          <div class="op-buttons">
            <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="handleApprove(row.id)">通过</el-button>
            <el-button v-if="row.status !== 'DISABLED'" type="danger" size="small" @click="handleDisable(row.id)">禁用</el-button>
            <el-button v-if="row.status === 'DISABLED'" type="success" size="small" @click="handleApprove(row.id)">启用</el-button>
            <el-button size="small" @click="handleSetAdmin(row.id, !row.isAdmin)">{{ row.isAdmin ? '取消管理员' : '设为管理员' }}</el-button>
            <el-button type="warning" size="small" @click="openChangePwdDialog(row)">修改密码</el-button>
            <el-button type="info" size="small" @click="handleResetPassword(row)">重置密码</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadUsers" style="margin-top:16px" />

    <el-dialog v-model="changePwdDialogVisible" title="修改用户密码" width="420px">
      <el-form :model="changePwdForm" :rules="changePwdRules" ref="changePwdFormRef" label-width="100px">
        <el-form-item label="用户">
          <span>{{ changePwdForm.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="changePwdForm.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changePwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdminChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listUsers, approveUser, disableUser, setAdmin, adminChangePassword, adminResetPassword } from '../../api'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

const users = ref<any[]>([])
const page = ref(1)
const total = ref(0)

const changePwdDialogVisible = ref(false)
const changePwdFormRef = ref<FormInstance>()
const changePwdForm = reactive({ userId: 0, username: '', newPassword: '' })
const changePwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

async function loadUsers() {
  const res: any = await listUsers({ pageNum: page.value, pageSize: 10 })
  users.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handleApprove(id: number) { await approveUser(id); ElMessage.success('操作成功'); loadUsers() }
async function handleDisable(id: number) { await ElMessageBox.confirm('确定禁用该用户？'); await disableUser(id); ElMessage.success('操作成功'); loadUsers() }
async function handleSetAdmin(id: number, isAdmin: boolean) { await setAdmin(id, isAdmin); ElMessage.success('操作成功'); loadUsers() }

function openChangePwdDialog(row: any) {
  changePwdForm.userId = row.id
  changePwdForm.username = row.username
  changePwdForm.newPassword = ''
  changePwdDialogVisible.value = true
}

async function handleAdminChangePassword() {
  await changePwdFormRef.value?.validate()
  await adminChangePassword(changePwdForm.userId, { newPassword: changePwdForm.newPassword })
  changePwdDialogVisible.value = false
  ElMessage.success('已修改，该用户需重新登录')
}

async function handleResetPassword(row: any) {
  await ElMessageBox.confirm(`确定将用户「${row.username}」的密码重置为初始密码 33550336？`, '重置密码', { type: 'warning' })
  await adminResetPassword(row.id)
  ElMessage.success('已重置为初始密码 33550336')
  loadUsers()
}

onMounted(loadUsers)
</script>

<style scoped>
.op-buttons { display: flex; gap: 8px; flex-wrap: nowrap; }
</style>
