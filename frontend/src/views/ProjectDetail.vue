<template>
  <div class="page-container">
    <el-card v-if="project">
      <el-page-header @back="$router.back()" :title="project.name" />
      <div style="margin-top:20px">
        <el-image v-if="project.cover" :src="project.cover" style="width:100%;max-height:400px" fit="cover" />
        <h1 style="margin:20px 0 10px">{{ project.name }}</h1>
        <p style="color:#666;margin-bottom:16px">{{ project.summary }}</p>
        <div style="margin-bottom:16px">
          <el-tag v-for="tag in (project.tags||'').split(',').filter(Boolean)" :key="tag" style="margin-right:6px">{{ tag.trim() }}</el-tag>
        </div>
        <div v-if="project.htmlContent" v-html="project.htmlContent" class="markdown-body" />
        <div v-else-if="project.markdownContent" class="markdown-body">{{ project.markdownContent }}</div>
        <div style="margin-top:20px;display:flex;gap:12px">
          <el-button v-if="project.githubUrl" type="primary" @click="window.open(project.githubUrl)">GitHub</el-button>
          <el-button v-if="project.giteeUrl" @click="window.open(project.giteeUrl)">Gitee</el-button>
          <el-button v-if="project.demoUrl" type="success" @click="window.open(project.demoUrl)">在线体验</el-button>
          <el-button v-if="project.downloadUrl" @click="window.open(project.downloadUrl)">下载</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getProject } from '../api'

const route = useRoute()
const project = ref<any>(null)

onMounted(async () => {
  const res: any = await getProject(Number(route.params.id))
  project.value = res.data
})
</script>

<style scoped>
.markdown-body { line-height: 1.8; }
</style>
