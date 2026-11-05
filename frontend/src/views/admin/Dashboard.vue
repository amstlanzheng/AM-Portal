<template>
  <div>
    <h2 class="admin-page-title" style="margin-bottom:20px">仪表盘</h2>
    <el-row :gutter="20">
      <el-col :span="8"><el-card shadow="hover"><el-statistic title="用户数" :value="stats.userCount" /></el-card></el-col>
      <el-col :span="8"><el-card shadow="hover"><el-statistic title="项目数" :value="stats.projectCount" /></el-card></el-col>
      <el-col :span="8"><el-card shadow="hover"><el-statistic title="博客数" :value="stats.blogCount" /></el-card></el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboard } from '../../api'

const stats = ref({ userCount: 0, projectCount: 0, blogCount: 0 })

onMounted(async () => {
  try { const res: any = await getDashboard(); stats.value = res.data } catch { /* ignore */ }
})
</script>

<style scoped>
/* 统计数据用等宽字体，弱化标签 */
.admin-page-title + .el-row :deep(.el-statistic__head) {
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
}
.admin-page-title + .el-row :deep(.el-statistic__number) {
  font-family: var(--font-mono);
  font-size: var(--fs-2xl);
  font-weight: 600;
  color: var(--text-primary);
}
</style>
