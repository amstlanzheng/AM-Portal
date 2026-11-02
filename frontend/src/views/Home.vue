<template>
  <div class="page-container home">
    <!-- Hero -->
    <section class="hero surface">
      <div class="hero-glow" aria-hidden="true"></div>
      <div class="hero-body">
        <el-avatar :size="88" :src="siteInfo.site_avatar" class="hero-avatar">
          {{ (siteInfo.site_title || 'AM').slice(0, 1).toUpperCase() }}
        </el-avatar>
        <div class="hero-text">
          <p class="eyebrow">Personal Portal</p>
          <h1 class="hero-title">{{ siteInfo.site_title || 'AM Portal' }}</h1>
          <p class="hero-subtitle">{{ siteInfo.site_subtitle || '个人展示平台' }}</p>
          <p class="hero-desc">{{ siteInfo.site_description || '一个集项目展示、个人博客、AI 助手于一体的个人网站' }}</p>
          <div class="hero-links">
            <a v-if="siteInfo.site_github" :href="siteInfo.site_github" target="_blank" class="link-chip">
              <el-icon><Link /></el-icon> GitHub
            </a>
            <a v-if="siteInfo.site_gitee" :href="siteInfo.site_gitee" target="_blank" class="link-chip">
              <el-icon><Link /></el-icon> Gitee
            </a>
            <a v-if="siteInfo.site_email" :href="'mailto:' + siteInfo.site_email" class="link-chip">
              <el-icon><Message /></el-icon> {{ siteInfo.site_email }}
            </a>
          </div>
        </div>
      </div>
    </section>

    <div class="home-grid">
      <div class="home-main">
        <!-- 最近项目 -->
        <section class="block">
          <div class="section-head">
            <h2 class="section-title">最近项目</h2>
            <router-link class="section-more" to="/projects">全部项目 →</router-link>
          </div>
          <div v-loading="projectsLoading" element-loading-text="加载中..." class="loading-box">
            <div v-if="!projectsLoading && !recentProjects.length" class="empty-state surface">
              <span>暂无项目</span>
            </div>
            <div v-else class="grid grid-3">
              <article
                v-for="project in recentProjects"
                :key="project.id"
                class="item-card surface card-hover"
                @click="$router.push('/projects/' + project.id)"
              >
                <div class="cover">
                  <el-image v-if="project.cover" :src="project.cover" fit="cover" />
                  <div v-else class="cover-placeholder">{{ (project.name || '?').slice(0, 1) }}</div>
                </div>
                <div class="item-body">
                  <h3 class="item-title">{{ project.name }}</h3>
                  <p class="item-desc">{{ project.summary }}</p>
                </div>
              </article>
            </div>
          </div>
        </section>

        <!-- 最近博客 -->
        <section class="block">
          <div class="section-head">
            <h2 class="section-title">最近博客</h2>
            <router-link class="section-more" to="/blogs">全部博客 →</router-link>
          </div>
          <div v-loading="blogsLoading" element-loading-text="加载中..." class="loading-box">
            <div v-if="!blogsLoading && !recentBlogs.length" class="empty-state surface">
              <span>暂无博客</span>
            </div>
            <div v-else class="blog-list">
              <article
                v-for="blog in recentBlogs"
                :key="blog.id"
                class="blog-row surface card-hover"
                @click="$router.push('/blogs/' + blog.id)"
              >
                <div class="blog-row-main">
                  <h3 class="item-title">{{ blog.title }}</h3>
                  <p class="item-desc">{{ blog.summary }}</p>
                </div>
                <div class="blog-row-meta">
                  <span class="faint">{{ blog.publishTime }}</span>
                  <el-icon class="arrow"><ArrowRight /></el-icon>
                </div>
              </article>
            </div>
          </div>
        </section>
      </div>

      <!-- 侧栏 -->
      <aside class="home-side">
        <section class="surface side-card">
          <h3 class="side-title">技术栈</h3>
          <div v-if="techList.length" class="chip-list">
            <span v-for="tech in techList" :key="tech" class="chip">{{ tech }}</span>
          </div>
          <p v-else class="faint">尚未配置</p>
        </section>

        <section class="surface side-card">
          <h3 class="side-title">快速导航</h3>
          <div class="side-nav">
            <router-link to="/projects" class="side-link">
              <span>项目展示</span>
              <el-icon><ArrowRight /></el-icon>
            </router-link>
            <router-link to="/blogs" class="side-link">
              <span>个人博客</span>
              <el-icon><ArrowRight /></el-icon>
            </router-link>
            <router-link to="/profile" class="side-link">
              <span>个人资料</span>
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getSiteInfo, getHomeRecentProjects, getHomeRecentBlogs } from '../api'

const siteInfo = ref<any>({})
const recentProjects = ref<any[]>([])
const recentBlogs = ref<any[]>([])
const projectsLoading = ref(false)
const blogsLoading = ref(false)

const techList = computed(() =>
  (siteInfo.value.site_tech_stack || '')
    .split(',')
    .map((t: string) => t.trim())
    .filter(Boolean)
)

// 站点信息优先加载，保证首屏固定内容立即展示
async function loadSiteInfo() {
  try {
    const res: any = await getSiteInfo()
    siteInfo.value = res.data || {}
  } catch { /* ignore */ }
}

// 最近项目懒加载
async function loadRecentProjects() {
  projectsLoading.value = true
  try {
    const res: any = await getHomeRecentProjects(5)
    recentProjects.value = res.data || []
  } catch { /* ignore */ } finally {
    projectsLoading.value = false
  }
}

// 最近博客懒加载
async function loadRecentBlogs() {
  blogsLoading.value = true
  try {
    const res: any = await getHomeRecentBlogs(5)
    recentBlogs.value = res.data || []
  } catch { /* ignore */ } finally {
    blogsLoading.value = false
  }
}

onMounted(() => {
  loadSiteInfo()
  loadRecentProjects()
  loadRecentBlogs()
})
</script>

<style scoped>
.home-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: var(--space-6);
  margin-top: var(--space-6);
  align-items: start;
}

/* ---------- Hero ---------- */
.hero {
  position: relative;
  overflow: hidden;
  padding: var(--space-8);
}

.hero-glow {
  position: absolute;
  inset: -40% -10% auto auto;
  width: 520px;
  height: 320px;
  background: radial-gradient(closest-side, rgba(37, 99, 235, 0.16), transparent 70%);
  pointer-events: none;
}

.hero-body {
  position: relative;
  display: flex;
  gap: var(--space-6);
  align-items: flex-start;
}

.hero-avatar {
  flex-shrink: 0;
  border: 3px solid #fff;
  box-shadow: 0 10px 24px -14px rgba(15, 23, 42, 0.5);
  font-size: 30px;
  font-weight: 650;
  color: var(--brand-600);
  background: var(--brand-50);
}

.hero-text {
  min-width: 0;
}

.hero-title {
  font-size: var(--text-3xl);
  font-weight: 700;
  margin-top: 6px;
  letter-spacing: -0.02em;
}

.hero-subtitle {
  margin-top: 6px;
  font-size: var(--text-lg);
  color: var(--color-text-muted);
}

.hero-desc {
  margin-top: var(--space-3);
  max-width: 62ch;
  color: var(--color-text);
  font-size: var(--text-base);
}

.hero-links {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-5);
}

.link-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
  background: var(--surface);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  transition: all 0.2s ease;
}
.link-chip:hover {
  color: var(--brand-600);
  border-color: var(--brand-100);
  background: var(--brand-50);
  transform: translateY(-1px);
}

/* ---------- 内容区 ---------- */
.block {
  margin-bottom: var(--space-8);
}

.loading-box {
  min-height: 80px;
}

.item-card {
  overflow: hidden;
  cursor: pointer;
  padding: 12px;
}

.item-card .cover {
  margin-bottom: var(--space-3);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-size: 28px;
  font-weight: 700;
  color: var(--brand-500);
  opacity: 0.55;
}

.item-body {
  padding: 0 4px 4px;
}

.item-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text-strong);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-desc {
  margin-top: 6px;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.blog-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.blog-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  cursor: pointer;
}

.blog-row-main {
  min-width: 0;
}

.blog-row-main .item-desc {
  -webkit-line-clamp: 1;
}

.blog-row-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.arrow {
  color: var(--ink-300);
  transition: transform 0.2s ease, color 0.2s ease;
}
.blog-row:hover .arrow {
  color: var(--brand-600);
  transform: translateX(3px);
}

/* ---------- 侧栏 ---------- */
.home-side {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  position: sticky;
  top: calc(var(--header-h) + var(--space-5));
}

.side-card {
  padding: var(--space-5);
}

.side-title {
  font-size: var(--text-base);
  font-weight: 650;
  margin-bottom: var(--space-3);
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.side-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 10px;
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  transition: all 0.2s ease;
}
.side-link:hover {
  background: var(--brand-50);
  color: var(--brand-600);
}

.side-link .el-icon {
  color: var(--ink-300);
  font-size: 12px;
}

@media (max-width: 980px) {
  .home-grid {
    grid-template-columns: 1fr;
  }
  .home-side {
    position: static;
    order: 2;
  }
}

@media (max-width: 640px) {
  .hero {
    padding: var(--space-5);
  }
  .hero-body {
    flex-direction: column;
    gap: var(--space-4);
  }
  .hero-title {
    font-size: var(--text-2xl);
  }
}
</style>
