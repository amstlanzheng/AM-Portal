<template>
  <div>
    <h2 style="margin-bottom:20px">仪表盘</h2>
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
