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
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <div class="op-buttons">
            <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="handleApprove(row.id)">通过</el-button>
            <el-button v-if="row.status !== 'DISABLED'" type="danger" size="small" @click="handleDisable(row.id)">禁用</el-button>
            <el-button v-if="row.status === 'DISABLED'" type="success" size="small" @click="handleApprove(row.id)">启用</el-button>
            <el-button size="small" @click="handleSetAdmin(row.id, !row.isAdmin)">{{ row.isAdmin ? '取消管理员' : '设为管理员' }}</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadUsers" style="margin-top:16px" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listUsers, approveUser, disableUser, setAdmin } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref<any[]>([])
const page = ref(1)
const total = ref(0)

async function loadUsers() {
  const res: any = await listUsers({ pageNum: page.value, pageSize: 10 })
  users.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handleApprove(id: number) { await approveUser(id); ElMessage.success('操作成功'); loadUsers() }
async function handleDisable(id: number) { await ElMessageBox.confirm('确定禁用该用户？'); await disableUser(id); ElMessage.success('操作成功'); loadUsers() }
async function handleSetAdmin(id: number, isAdmin: boolean) { await setAdmin(id, isAdmin); ElMessage.success('操作成功'); loadUsers() }

onMounted(loadUsers)
</script>

<style scoped>
.op-buttons { display: flex; gap: 8px; flex-wrap: nowrap; }
</style>
