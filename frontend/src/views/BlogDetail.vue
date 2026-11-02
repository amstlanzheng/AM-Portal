<template>
  <div class="page-container detail">
    <button class="back-link" @click="$router.back()">
      <el-icon><ArrowLeft /></el-icon> 返回
    </button>

    <article v-if="blog">
      <header class="article-head">
        <p class="eyebrow">Blog</p>
        <h1 class="article-title">{{ blog.title }}</h1>
        <div class="article-meta">
          <span>{{ blog.publishTime }}</span>
          <span class="dot">·</span>
          <span>{{ blog.viewCount }} 次阅读</span>
        </div>
      </header>

      <div class="article surface">
        <div v-if="blog.htmlContent" v-html="blog.htmlContent" class="markdown-body" />
        <div v-else-if="blog.markdownContent" v-html="renderedMarkdown" class="markdown-body" />
        <p v-else class="faint">（本文暂无内容）</p>
      </div>
    </article>
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
.detail {
  max-width: 860px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px 6px 10px;
  margin-bottom: var(--space-5);
  border: none;
  border-radius: var(--radius-pill);
  background: transparent;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}
.back-link:hover {
  background: var(--surface);
  color: var(--brand-600);
  box-shadow: var(--shadow-xs);
}

.article-head {
  margin-bottom: var(--space-6);
}

.article-title {
  margin-top: 6px;
  font-size: var(--text-3xl);
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: var(--space-3);
  font-size: var(--text-sm);
  color: var(--color-text-faint);
}

.dot {
  color: var(--ink-300);
}

.article {
  padding: var(--space-8) var(--space-10);
}

@media (max-width: 640px) {
  .article-title {
    font-size: var(--text-2xl);
  }
  .article {
    padding: var(--space-5);
  }
}
</style>
