<template>
  <div class="page-container detail">
    <button class="back-link" @click="$router.back()">
      <el-icon><ArrowLeft /></el-icon> 返回
    </button>

    <article v-if="project">
      <header class="detail-head">
        <p class="eyebrow">Project</p>
        <h1 class="detail-title">{{ project.name }}</h1>
        <p v-if="project.summary" class="detail-desc">{{ project.summary }}</p>

        <div v-if="tagList.length" class="chip-list detail-tags">
          <span v-for="tag in tagList" :key="tag" class="chip">{{ tag }}</span>
        </div>

        <div class="detail-actions">
          <button v-if="hasUrl(project.githubUrl)" class="act act-solid" @click="openUrl(project.githubUrl)">
            <el-icon><Link /></el-icon> GitHub
          </button>
          <button v-if="hasUrl(project.giteeUrl)" class="act act-outline" @click="openUrl(project.giteeUrl)">
            <el-icon><Link /></el-icon> Gitee
          </button>
          <button v-if="hasUrl(project.demoUrl)" class="act act-outline" @click="openUrl(project.demoUrl)">
            <el-icon><Monitor /></el-icon> 在线体验
          </button>
          <button v-if="hasUrl(project.downloadUrl)" class="act act-outline" @click="openUrl(project.downloadUrl)">
            <el-icon><Download /></el-icon> 下载
          </button>
        </div>
      </header>

      <div v-if="project.cover" class="cover detail-cover">
        <el-image :src="project.cover" fit="cover" />
      </div>

      <div v-if="project.htmlContent || project.markdownContent" class="article surface">
        <div v-if="project.htmlContent" v-html="project.htmlContent" class="markdown-body" />
        <div v-else-if="project.markdownContent" v-html="renderedMarkdown" class="markdown-body" />
      </div>
    </article>
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

const tagList = computed(() =>
  (project.value?.tags || '')
    .split(',')
    .map((t: string) => t.trim())
    .filter(Boolean)
)

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
.detail {
  max-width: 900px;
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

.detail-head {
  margin-bottom: var(--space-6);
}

.detail-title {
  margin-top: 6px;
  font-size: var(--text-3xl);
  font-weight: 700;
  letter-spacing: -0.02em;
}

.detail-desc {
  margin-top: var(--space-3);
  font-size: var(--text-lg);
  color: var(--color-text-muted);
  max-width: 68ch;
}

.detail-tags {
  margin-top: var(--space-4);
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-top: var(--space-5);
}

.act {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 38px;
  padding: 0 18px;
  border-radius: var(--radius-pill);
  font-size: var(--text-sm);
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.act-solid {
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  box-shadow: 0 10px 20px -12px rgba(37, 99, 235, 0.8);
}
.act-solid:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 24px -14px rgba(37, 99, 235, 0.9);
}

.act-outline {
  color: var(--color-text);
  background: var(--surface);
  border-color: var(--color-border);
}
.act-outline:hover {
  color: var(--brand-600);
  border-color: var(--brand-100);
  background: var(--brand-50);
}

.detail-cover {
  aspect-ratio: 16 / 8;
  margin-bottom: var(--space-6);
  border: 1px solid var(--color-border);
}

.article {
  padding: var(--space-8);
}

@media (max-width: 640px) {
  .detail-title {
    font-size: var(--text-2xl);
  }
  .article {
    padding: var(--space-5);
  }
}
</style>
