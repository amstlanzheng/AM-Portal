<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="sidebar-logo">
        <h2 v-if="!isCollapse">AM Portal</h2>
        <span v-else>AM</span>
      </div>
      <el-menu :default-active="activeMenu" router :collapse="isCollapse" background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon><span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon><span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/projects">
          <el-icon><Folder /></el-icon><span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/blogs">
          <el-icon><Document /></el-icon><span>博客管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/sensitive-words">
          <el-icon><Warning /></el-icon><span>敏感词管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/prompts">
          <el-icon><ChatDotRound /></el-icon><span>Prompt管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/files">
          <el-icon><FolderOpened /></el-icon><span>文件管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/config">
          <el-icon><Setting /></el-icon><span>系统配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <el-icon class="collapse-btn" @click="isCollapse = !isCollapse"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
        <el-breadcrumb><el-breadcrumb-item :to="{ path: '/' }">前台</el-breadcrumb-item><el-breadcrumb-item>后台管理</el-breadcrumb-item></el-breadcrumb>
        <div style="margin-left:auto"><el-button @click="$router.push('/')">返回前台</el-button></div>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isCollapse = ref(false)
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.admin-layout { min-height: 100vh; }
.sidebar { background: #304156; transition: width 0.3s; overflow: hidden; }
.sidebar-logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; }
.sidebar-logo h2 { font-size: 18px; }
.admin-header { display: flex; align-items: center; gap: 16px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.collapse-btn { cursor: pointer; font-size: 20px; }
</style>
