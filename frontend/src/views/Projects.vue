<template>
  <div class="page-container">
    <h1 style="margin-bottom:20px">项目列表</h1>
    <el-row :gutter="20">
      <el-col :span="8" v-for="project in projects" :key="project.id">
        <el-card shadow="hover" class="card-hover" style="margin-bottom:20px;cursor:pointer" @click="$router.push('/projects/' + project.id)">
          <el-image :src="project.cover" style="width:100%;height:180px" fit="cover" />
          <h3 style="margin:12px 0 6px">{{ project.name }}</h3>
          <p style="color:#999;font-size:13px">{{ project.summary }}</p>
          <div style="margin-top:8px">
            <el-tag v-for="tag in (project.tags||'').split(',').filter(Boolean)" :key="tag" size="small" style="margin-right:4px">{{ tag.trim() }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-pagination v-model:current-page="page" :page-size="9" :total="total" layout="prev, pager, next" @current-change="loadProjects" style="margin-top:20px;text-align:center" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listProjects } from '../api'

const projects = ref<any[]>([])
const page = ref(1)
const total = ref(0)

async function loadProjects() {
  const res: any = await listProjects({ pageNum: page.value, pageSize: 9 })
  projects.value = res.data?.records || []
  total.value = res.data?.total || 0
}

onMounted(loadProjects)
</script>
