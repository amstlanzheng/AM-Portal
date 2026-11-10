<template>
  <div class="front-layout" :data-wb-theme="wbTheme">
    <!-- Nimbus 风格动态天气背景（纯装饰层，不拦截交互） -->
    <WeatherBackground />
    <header class="site-header">
      <div class="header-inner">
        <div class="brand" @click="$router.push('/')">
          <span class="brand-mark">A</span>
          <span class="brand-text">{{ siteInfo.site_title || 'AM Portal' }}</span>
        </div>

        <nav class="site-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            {{ item.label }}
          </router-link>
        </nav>

        <div class="header-actions">
          <template v-if="userStore.token">
            <el-dropdown trigger="click">
              <span class="user-chip">
                <el-avatar :size="28" :src="userStore.userInfo?.avatar">{{ userInitial }}</el-avatar>
                <span class="user-name">{{ userStore.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">个人资料</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin')">后台管理</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <button class="btn btn-ghost" @click="$router.push('/login')">登录</button>
            <button class="btn btn-primary" @click="$router.push('/register')">注册</button>
          </template>
        </div>
      </div>
    </header>

    <main class="site-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 回到顶部（带滚动进度环） -->
    <transition name="backtop">
      <button v-if="showBackTop" class="back-top" :title="`回到顶部 ${scrollPercent}%`" @click="scrollToTop">
        <svg class="ring" viewBox="0 0 36 36" aria-hidden="true">
          <circle class="ring-track" cx="18" cy="18" r="16" />
          <circle
            class="ring-bar"
            cx="18"
            cy="18"
            r="16"
            :stroke-dasharray="`${scrollPercent} 100`"
          />
        </svg>
        <el-icon><Top /></el-icon>
      </button>
    </transition>

    <footer class="site-footer">
      <div class="footer-inner">
        <span>© {{ year }} {{ siteInfo.site_title || 'AM Portal' }}</span>
        <span class="dot">·</span>
        <span>Personal Portal &amp; Blog</span>
      </div>
    </footer>

    <!-- AI 聊天窗口 -->
    <AiChat />

    <!-- 天气角标与切换面板 -->
    <WeatherBadge />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getHomePage } from '../api'
import AiChat from '../components/AiChat.vue'
import WeatherBackground from '../components/weather/WeatherBackground.vue'
import WeatherBadge from '../components/weather/WeatherBadge.vue'
import { useWeather } from '../composables/useWeather'

const route = useRoute()
const userStore = useUserStore()
const { wbTheme } = useWeather()
const siteInfo = ref<any>({})
const year = new Date().getFullYear()

const navItems = [
  { path: '/', label: '首页' },
  { path: '/projects', label: '项目' },
  { path: '/blogs', label: '博客' },
  { path: '/tools', label: '工具' },
  { path: '/fun', label: '趣味' }
]

const userInitial = computed(() => (userStore.username || '?').slice(0, 1).toUpperCase())

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

onMounted(async () => {
  try {
    const res: any = await getHomePage()
    siteInfo.value = res.data?.siteInfo || {}
  } catch { /* ignore */ }
  if (userStore.token && !userStore.userInfo) {
    userStore.fetchUserInfo()
  }
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

/* 回到顶部 */
const scrollPercent = ref(0)
const showBackTop = ref(false)
let ticking = false

function onScroll() {
  if (ticking) return
  ticking = true
  requestAnimationFrame(() => {
    const top = window.scrollY || document.documentElement.scrollTop
    const max = Math.max(1, document.documentElement.scrollHeight - window.innerHeight)
    scrollPercent.value = Math.min(100, Math.round((top / max) * 100))
    showBackTop.value = top > 420
    ticking = false
  })
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})

function handleLogout() {
  userStore.logout()
  window.location.href = '/'
}
</script>

<style scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ---------- 顶栏：白底 + 细线 + 毛玻璃 ---------- */
.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: saturate(180%) blur(14px);
  -webkit-backdrop-filter: saturate(180%) blur(14px);
  border-bottom: 1px solid var(--color-border);
  overflow: hidden;
}

/* 顶栏下方一道循环扫光，静态但带科技感 */
.site-header::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, color-mix(in srgb, var(--brand-600) 55%, transparent), transparent);
  background-size: 38% 100%;
  background-repeat: no-repeat;
  animation: header-scan 7s linear infinite;
  pointer-events: none;
}

@keyframes header-scan {
  0% { background-position: -40% 0; }
  100% { background-position: 140% 0; }
}

.header-inner {
  max-width: var(--container);
  height: var(--header-h);
  margin: 0 auto;
  padding: 0 var(--space-6);
  display: flex;
  align-items: center;
  gap: var(--space-8);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}

.brand-mark {
  position: relative;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  box-shadow: var(--shadow-accent);
}

.brand-mark::after {
  content: "";
  position: absolute;
  inset: -3px;
  border-radius: 11px;
  border: 1px solid color-mix(in srgb, var(--brand-600) 35%, transparent);
  animation: mark-pulse 2.6s ease-out infinite;
  pointer-events: none;
}

@keyframes mark-pulse {
  0% { transform: scale(0.96); opacity: 0.85; }
  100% { transform: scale(1.22); opacity: 0; }
}

.brand-text {
  font-size: var(--text-lg);
  font-weight: 650;
  color: var(--color-text-strong);
  letter-spacing: -0.01em;
  white-space: nowrap;
}

.site-nav {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex: 1;
}

.nav-item {
  position: relative;
  padding: 8px 14px;
  border-radius: var(--radius-pill);
  font-size: var(--text-base);
  color: var(--color-text-muted);
  transition: color 0.2s ease, background-color 0.2s ease;
}

.nav-item:hover {
  color: var(--color-text-strong);
  background: var(--surface-muted);
}

.nav-item.active {
  color: var(--brand-600);
  background: var(--brand-50);
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.btn {
  height: 36px;
  padding: 0 18px;
  border-radius: var(--radius-pill);
  font-size: var(--text-sm);
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.btn-ghost {
  background: transparent;
  color: var(--color-text-muted);
  border-color: transparent;
}
.btn-ghost:hover {
  color: var(--color-text-strong);
  background: var(--surface-muted);
}

.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  box-shadow: 0 8px 18px -10px color-mix(in srgb, var(--brand-600) 70%, transparent);
}
.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 22px -12px color-mix(in srgb, var(--brand-600) 80%, transparent);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
  background: var(--surface);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  outline: none;
}
.user-chip:hover {
  border-color: var(--line-strong);
  box-shadow: var(--shadow-sm);
}

.user-name {
  font-size: var(--text-sm);
  color: var(--color-text);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---------- 内容区 ---------- */
.site-main {
  flex: 1;
  position: relative;
  z-index: 1;
}

/* ---------- 页脚 ---------- */
.site-footer {
  position: relative;
  z-index: 1;
  border-top: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.6);
}

/* ---------- 天气暗主题（夜晚/雨夜）：文字与表面自适应 ----------
   白色前景/深色半透明覆层沿用项目功能性配色惯例（同 #fff 白前景）；
   卡片表面经 --surface 作用域覆写实现玻璃拟态（≥72% 白底保证正文可读） */
.front-layout[data-wb-theme='dark'] {
  --surface: rgba(255, 255, 255, 0.78);
  --surface-muted: rgba(255, 255, 255, 0.58);
}

.front-layout[data-wb-theme='dark'] .site-header {
  background: rgba(15, 23, 42, 0.5);
  border-bottom-color: rgba(255, 255, 255, 0.12);
}

.front-layout[data-wb-theme='dark'] .brand-text {
  color: rgba(255, 255, 255, 0.95);
}

.front-layout[data-wb-theme='dark'] .nav-item {
  color: rgba(255, 255, 255, 0.72);
}

.front-layout[data-wb-theme='dark'] .nav-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.front-layout[data-wb-theme='dark'] .nav-item.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.16);
}

.front-layout[data-wb-theme='dark'] .btn-ghost {
  color: rgba(255, 255, 255, 0.8);
}

.front-layout[data-wb-theme='dark'] .btn-ghost:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.front-layout[data-wb-theme='dark'] .user-chip {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.18);
}

.front-layout[data-wb-theme='dark'] .user-name {
  color: rgba(255, 255, 255, 0.9);
}

.front-layout[data-wb-theme='dark'] .site-footer {
  background: rgba(15, 23, 42, 0.45);
  border-top-color: rgba(255, 255, 255, 0.1);
}

.front-layout[data-wb-theme='dark'] .footer-inner {
  color: rgba(255, 255, 255, 0.55);
}

.footer-inner {
  max-width: var(--container);
  margin: 0 auto;
  padding: var(--space-6);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

.dot {
  color: var(--ink-300);
}

/* ---------- 回到顶部 ---------- */
.back-top {
  position: fixed;
  right: 28px;
  bottom: 160px;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  color: var(--color-text-muted);
  display: grid;
  place-items: center;
  cursor: pointer;
  z-index: 998;
  box-shadow: var(--shadow-sm);
  transition: color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}
.back-top:hover {
  color: var(--brand-600);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.ring {
  position: absolute;
  inset: -1px;
  width: calc(100% + 2px);
  height: calc(100% + 2px);
  transform: rotate(-90deg);
  pointer-events: none;
}
.ring-track {
  fill: none;
  stroke: transparent;
  stroke-width: 2;
}
.ring-bar {
  fill: none;
  stroke: var(--brand-500);
  stroke-width: 2;
  stroke-linecap: round;
  opacity: 0.85;
  transition: stroke-dasharray 0.15s linear;
}

.backtop-enter-active,
.backtop-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.backtop-enter-from,
.backtop-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.92);
}

@media (max-width: 768px) {
  .header-inner {
    gap: var(--space-4);
    padding: 0 var(--space-4);
  }
  .brand-text {
    display: none;
  }
  .nav-item {
    padding: 8px 10px;
    font-size: var(--text-sm);
  }
  .btn {
    padding: 0 14px;
  }
  .back-top {
    right: 18px;
    bottom: 148px;
    width: 42px;
    height: 42px;
  }
}
</style>
