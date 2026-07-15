<template>
  <div class="page-container">
    <el-card v-if="blog">
      <el-page-header @back="$router.back()" />
      <h1 style="margin:20px 0 10px">{{ blog.title }}</h1>
      <div style="color:#999;font-size:13px;margin-bottom:20px">{{ blog.publishTime }} · {{ blog.viewCount }} 次阅读</div>
      <div v-if="blog.htmlContent" v-html="blog.htmlContent" class="markdown-body" />
      <div v-else-if="blog.markdownContent" class="markdown-body">{{ blog.markdownContent }}</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getBlog } from '../api'

const route = useRoute()
const blog = ref<any>(null)

onMounted(async () => {
  const res: any = await getBlog(Number(route.params.id))
  blog.value = res.data
})
</script>

<style scoped>
.markdown-body { line-height: 1.8; font-size: 16px; }
</style>
