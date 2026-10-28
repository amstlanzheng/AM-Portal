<template>
  <div class="page-container">
    <el-card v-if="project">
      <el-page-header @back="$router.back()" :title="project.name" />
      <div style="margin-top:20px">
        <el-image v-if="project.cover" :src="project.cover" style="width:100%;max-height:400px" fit="cover" />
        <h1 style="margin:20px 0 10px">{{ project.name }}</h1>
        <div style="margin-bottom:16px;display:flex;gap:12px">
          <el-button v-if="hasUrl(project.githubUrl)" type="primary" @click="openUrl(project.githubUrl)">GitHub</el-button>
          <el-button v-if="hasUrl(project.giteeUrl)" @click="openUrl(project.giteeUrl)">Gitee</el-button>
          <el-button v-if="hasUrl(project.demoUrl)" type="success" @click="openUrl(project.demoUrl)">在线体验</el-button>
          <el-button v-if="hasUrl(project.downloadUrl)" @click="openUrl(project.downloadUrl)">下载</el-button>
        </div>
        <p style="color:#666;margin-bottom:16px">{{ project.summary }}</p>
        <div style="margin-bottom:16px">
          <el-tag v-for="tag in (project.tags||'').split(',').filter(Boolean)" :key="tag" style="margin-right:6px">{{ tag.trim() }}</el-tag>
        </div>
        <div v-if="project.htmlContent" v-html="project.htmlContent" class="markdown-body" />
        <div v-else-if="project.markdownContent" v-html="renderedMarkdown" class="markdown-body" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getProject } from '../api'
import { renderMarkdown } from '../utils/markdown'

const route = useRoute()
const project = ref<any>(null)

const renderedMarkdown = computed(() => renderMarkdown(project.value?.markdownContent))

function hasUrl(url?: string) {
  return !!url && url.trim() !== ''
}

function openUrl(url: string) {
  if (!url) return
  const target = /^https?:\/\//i.test(url) ? url : 'https://' + url
  window.open(target, '_blank')
}

onMounted(async () => {
  const res: any = await getProject(Number(route.params.id))
  project.value = res.data
})
</script>

<style scoped>
/* 排版样式见 src/style.css 中的 .markdown-body（v-html 内容需全局样式） */
</style>
