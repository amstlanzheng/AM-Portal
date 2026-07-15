<template>
  <div class="front-layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <h1>{{ siteInfo.site_title || 'AM Portal' }}</h1>
        </div>
        <el-menu mode="horizontal" :default-active="activeMenu" router class="nav-menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/projects">项目</el-menu-item>
          <el-menu-item index="/blogs">博客</el-menu-item>
        </el-menu>
        <div class="header-right">
          <template v-if="userStore.token">
            <el-dropdown>
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar" />
                {{ userStore.username }}
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
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
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

const activeMenu = computed(() => route.path)

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
.front-layout { min-height: 100vh; }
.header { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 0 20px; position: sticky; top: 0; z-index: 100; }
.header-content { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; height: 60px; }
.logo { cursor: pointer; margin-right: 40px; }
.logo h1 { font-size: 20px; color: #409eff; white-space: nowrap; }
.nav-menu { flex: 1; border: none; }
.header-right { display: flex; align-items: center; gap: 10px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.main { max-width: 1200px; margin: 20px auto; padding: 0 20px; min-height: calc(100vh - 100px); }
</style>
