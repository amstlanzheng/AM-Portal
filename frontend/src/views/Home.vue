<template>
  <div class="page-container home">
    <!-- Hero -->
    <section
      ref="heroRef"
      class="hero surface"
      @mousemove="onHeroMove"
      @mouseleave="onHeroLeave"
    >
      <div class="hero-glow" :style="glowStyle" aria-hidden="true"></div>
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

          <div class="hero-stats">
            <div class="stat">
              <span class="stat-num">{{ shownProjects }}</span>
              <span class="stat-label">项目</span>
            </div>
            <span class="stat-sep"></span>
            <div class="stat">
              <span class="stat-num">{{ shownBlogs }}</span>
              <span class="stat-label">博客</span>
            </div>
            <span class="stat-sep"></span>
            <div class="stat">
              <span class="stat-num">{{ shownTech }}</span>
              <span class="stat-label">技术栈</span>
            </div>
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
                v-for="(project, idx) in recentProjects"
                :key="project.id"
                v-reveal="idx * 70"
                class="item-card surface card-hover"
                @click="$router.push('/projects/' + project.id)"
              >
                <div class="cover">
                  <el-image v-if="project.cover" :src="project.cover" fit="cover" lazy />
                  <div v-else class="cover-placeholder">{{ (project.name || '?').slice(0, 1) }}</div>
                </div>
                <div class="item-body">
                  <h3 class="item-title">{{ project.name }}</h3>
                  <p class="item-desc">{{ project.summary }}</p>
                  <p class="card-time">{{ fmtDate(project.publishTime || project.createTime) }}</p>
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
                v-for="(blog, idx) in recentBlogs"
                :key="blog.id"
                v-reveal="idx * 60"
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
        <!-- 时钟 -->
        <section v-reveal class="surface side-card now-card">
          <div class="now-top">
            <span class="clock-time">{{ clockTime }}</span>
            <span class="now-greet">{{ greeting }}</span>
          </div>
          <p class="clock-date">{{ clockDate }}</p>
        </section>

        <!-- 小贴士 -->
        <section v-reveal="80" class="surface side-card tip-card">
          <div class="tip-head">
            <h3 class="side-title">小贴士</h3>
            <button class="tip-refresh" :class="{ spinning: tipLoading }" title="换一条" @click="loadTip">
              <el-icon><Refresh /></el-icon>
            </button>
          </div>
          <transition name="tip-fade" mode="out-in">
            <p :key="tip" class="tip-text">{{ tip || '加载中…' }}</p>
          </transition>
        </section>

        <!-- 技术栈 -->
        <section v-reveal="140" class="surface side-card">
          <h3 class="side-title">技术栈</h3>
          <div v-if="techList.length" class="chip-list">
            <span v-for="tech in techList" :key="tech" class="chip">{{ tech }}</span>
          </div>
          <p v-else class="faint">尚未配置</p>
        </section>

        <!-- 快速导航 -->
        <section v-reveal="200" class="surface side-card">
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
import { ref, computed, reactive, onMounted, onBeforeUnmount } from 'vue'
import { getSiteInfo, getHomeRecentProjects, getHomeRecentBlogs, getRandomTip, listProjects, listBlogs } from '../api'

const siteInfo = ref<any>({})
const recentProjects = ref<any[]>([])
const recentBlogs = ref<any[]>([])
const projectsLoading = ref(false)
const blogsLoading = ref(false)

/* ---------------- 实时时钟 ---------------- */
const now = ref(new Date())
let clockTimer: number | undefined

const pad = (n: number) => String(n).padStart(2, '0')

const clockTime = computed(() => {
  const d = now.value
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
})

const clockDate = computed(() => {
  const d = now.value
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${'日一二三四五六'[d.getDay()]}`
})

const greeting = computed(() => {
  const h = now.value.getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  if (h < 23) return '晚上好'
  return '夜深了'
})

/* ---------------- 小贴士 ---------------- */
const tip = ref('')
const tipLoading = ref(false)

async function loadTip() {
  if (tipLoading.value) return
  tipLoading.value = true
  try {
    const res: any = await getRandomTip()
    tip.value = res.data?.content || '暂无小贴士，可在后台「小贴士管理」中添加'
  } catch {
    tip.value = '暂无小贴士，可在后台「小贴士管理」中添加'
  } finally {
    tipLoading.value = false
  }
}

/* ---------------- Hero 光晕跟随 ---------------- */
const heroRef = ref<HTMLElement>()
const spot = reactive({ x: 70, y: 10, active: false })

const glowStyle = computed(() => ({
  left: `${spot.x}%`,
  top: `${spot.y}%`,
  opacity: spot.active ? '1' : '0.7'
}))

function onHeroMove(e: MouseEvent) {
  const el = heroRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  spot.x = ((e.clientX - rect.left) / rect.width) * 100
  spot.y = ((e.clientY - rect.top) / rect.height) * 100
  spot.active = true
}

function onHeroLeave() {
  spot.active = false
  spot.x = 70
  spot.y = 10
}

/* ---------------- 统计数字 ---------------- */
const counts = reactive({ projects: 0, blogs: 0 })
const shownProjects = ref(0)
const shownBlogs = ref(0)
const shownTech = ref(0)

const techList = computed(() =>
  (siteInfo.value.site_tech_stack || '')
    .split(',')
    .map((t: string) => t.trim())
    .filter(Boolean)
)

function animateNumber(from: number, to: number, setter: (v: number) => void, duration = 900) {
  const start = performance.now()
  const step = (t: number) => {
    const p = Math.min(1, (t - start) / duration)
    const eased = 1 - Math.pow(1 - p, 3)
    setter(Math.round(from + (to - from) * eased))
    if (p < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

async function loadCounts() {
  try {
    const [p, b]: any[] = await Promise.all([
      listProjects({ pageNum: 1, pageSize: 1 }),
      listBlogs({ pageNum: 1, pageSize: 1 })
    ])
    counts.projects = p.data?.total || 0
    counts.blogs = b.data?.total || 0
    animateNumber(0, counts.projects, v => (shownProjects.value = v))
    animateNumber(0, counts.blogs, v => (shownBlogs.value = v))
  } catch { /* ignore */ }
}

/* ---------------- 基础数据 ---------------- */
function fmtDate(value?: string) {
  if (!value) return ''
  return String(value).slice(0, 10)
}

async function loadSiteInfo() {
  try {
    const res: any = await getSiteInfo()
    siteInfo.value = res.data || {}
    animateNumber(0, techList.value.length, v => (shownTech.value = v), 700)
  } catch { /* ignore */ }
}

async function loadRecentProjects() {
  projectsLoading.value = true
  try {
    const res: any = await getHomeRecentProjects(5)
    recentProjects.value = res.data || []
  } catch { /* ignore */ } finally {
    projectsLoading.value = false
  }
}

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
  loadCounts()
  loadTip()
  clockTimer = window.setInterval(() => (now.value = new Date()), 1000)
})

onBeforeUnmount(() => {
  if (clockTimer) window.clearInterval(clockTimer)
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
  width: 520px;
  height: 520px;
  margin: -260px 0 0 -260px;
  border-radius: 50%;
  background: radial-gradient(closest-side, rgba(37, 99, 235, 0.16), rgba(37, 99, 235, 0.05) 55%, transparent 72%);
  transition: left 0.35s ease-out, top 0.35s ease-out, opacity 0.4s ease;
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

/* 统计数字 */
.hero-stats {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-top: var(--space-5);
  padding-top: var(--space-4);
  border-top: 1px dashed var(--color-border);
}

.stat {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.stat-num {
  font-family: var(--font-mono);
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--brand-600);
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

.stat-sep {
  width: 1px;
  height: 14px;
  background: var(--color-border);
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

.card-time {
  margin-top: var(--space-2);
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-faint);
  font-variant-numeric: tabular-nums;
  min-height: 18px;
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

/* 时钟卡 */
.now-card {
  background:
    linear-gradient(150deg, rgba(37, 99, 235, 0.06), rgba(255, 255, 255, 0) 55%),
    var(--surface);
}

.now-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-2);
}

.clock-time {
  font-family: var(--font-mono);
  font-size: 30px;
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: 0.02em;
  color: var(--color-text-strong);
  font-variant-numeric: tabular-nums;
}

.now-greet {
  font-size: var(--text-xs);
  color: var(--brand-600);
  background: var(--brand-50);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-pill);
  padding: 2px 10px;
  white-space: nowrap;
}

.clock-date {
  margin-top: var(--space-2);
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-faint);
  font-variant-numeric: tabular-nums;
}

/* 小贴士卡 */
.tip-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.tip-head .side-title {
  margin-bottom: 0;
}

.tip-refresh {
  border: none;
  background: transparent;
  color: var(--ink-400);
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  transition: color 0.2s ease, background-color 0.2s ease, transform 0.4s ease;
}
.tip-refresh:hover {
  color: var(--brand-600);
  background: var(--brand-50);
}
.tip-refresh.spinning {
  transform: rotate(360deg);
  color: var(--brand-600);
}

.tip-text {
  position: relative;
  padding-left: var(--space-3);
  border-left: 2px solid var(--brand-100);
  font-size: var(--text-sm);
  line-height: 1.75;
  color: var(--color-text-muted);
}

.tip-fade-enter-active,
.tip-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}
.tip-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.tip-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
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
  .clock-time {
    font-size: 26px;
  }
}
</style>
