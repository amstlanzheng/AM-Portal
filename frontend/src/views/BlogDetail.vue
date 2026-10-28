<template>
  <div class="page-container">
    <el-card v-if="blog">
      <el-page-header @back="$router.back()" />
      <h1 style="margin:20px 0 10px">{{ blog.title }}</h1>
      <div style="color:#999;font-size:13px;margin-bottom:20px">{{ blog.publishTime }} · {{ blog.viewCount }} 次阅读</div>
      <div v-if="blog.htmlContent" v-html="blog.htmlContent" class="markdown-body" />
      <div v-else-if="blog.markdownContent" v-html="renderedMarkdown" class="markdown-body" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getBlog } from '../api'
import { renderMarkdown } from '../utils/markdown'

const route = useRoute()
const blog = ref<any>(null)

const renderedMarkdown = computed(() => renderMarkdown(blog.value?.markdownContent))

onMounted(async () => {
  const res: any = await getBlog(Number(route.params.id))
  blog.value = res.data
})
</script>

<style scoped>
/* 排版样式见 src/style.css 中的 .markdown-body（v-html 内容需全局样式） */
</style>
