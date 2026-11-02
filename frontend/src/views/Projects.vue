<template>
  <div class="page-container">
    <header class="page-head">
      <div>
        <p class="eyebrow">Projects</p>
        <h1 class="page-title">项目列表</h1>
        <p class="page-sub">我在做、做过以及正在维护的东西</p>
      </div>
      <span class="count">{{ total }} 个项目</span>
    </header>

    <div v-loading="loading" element-loading-text="加载中...">
      <div v-if="!loading && !projects.length" class="empty-state surface">
        <span>暂无项目</span>
      </div>
      <div v-else class="grid grid-3">
        <article
          v-for="project in projects"
          :key="project.id"
          class="project-card surface card-hover"
          @click="$router.push('/projects/' + project.id)"
        >
          <div class="cover">
            <el-image v-if="project.cover" :src="project.cover" fit="cover" />
            <div v-else class="cover-placeholder">{{ (project.name || '?').slice(0, 1) }}</div>
          </div>
          <div class="project-body">
            <h2 class="project-title">{{ project.name }}</h2>
            <p class="project-desc">{{ project.summary }}</p>
            <div v-if="tagList(project).length" class="chip-list">
              <span v-for="tag in tagList(project)" :key="tag" class="chip chip-soft">{{ tag }}</span>
            </div>
          </div>
        </article>
      </div>
    </div>

    <div v-if="total > 9" class="pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="9"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="loadProjects"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listProjects } from '../api'

const projects = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)

function tagList(project: any) {
  return (project?.tags || '')
    .split(',')
    .map((t: string) => t.trim())
    .filter(Boolean)
}

async function loadProjects() {
  loading.value = true
  try {
    const res: any = await listProjects({ pageNum: page.value, pageSize: 9 })
    projects.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadProjects)
</script>

<style scoped>
.count {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  padding: 5px 14px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
  background: var(--surface);
}

.project-card {
  overflow: hidden;
  cursor: pointer;
  padding: 12px;
}

.project-card .cover {
  margin-bottom: var(--space-3);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-size: 30px;
  font-weight: 700;
  color: var(--brand-500);
  opacity: 0.55;
}

.project-body {
  padding: 0 4px 6px;
}

.project-title {
  font-size: var(--text-base);
  font-weight: 650;
  color: var(--color-text-strong);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-desc {
  margin: 6px 0 var(--space-3);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  min-height: calc(1.65em * 2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: var(--space-8);
}
</style>
