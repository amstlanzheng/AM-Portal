<template>
  <div class="front-layout">
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
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="footer-inner">
        <span>© {{ year }} {{ siteInfo.site_title || 'AM Portal' }}</span>
        <span class="dot">·</span>
        <span>Personal Portal &amp; Blog</span>
      </div>
    </footer>

    <!-- AI 聊天窗口 -->
    <AiChat />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getHomePage } from '../api'
import AiChat from '../components/AiChat.vue'

const route = useRoute()
const userStore = useUserStore()
const siteInfo = ref<any>({})
const year = new Date().getFullYear()

const navItems = [
  { path: '/', label: '首页' },
  { path: '/projects', label: '项目' },
  { path: '/blogs', label: '博客' }
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
  box-shadow: 0 8px 18px -10px rgba(37, 99, 235, 0.7);
}
.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 22px -12px rgba(37, 99, 235, 0.8);
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
}

/* ---------- 页脚 ---------- */
.site-footer {
  border-top: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.6);
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
}
</style>
