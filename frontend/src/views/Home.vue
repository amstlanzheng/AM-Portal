<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="profile-card">
          <div class="profile">
            <el-avatar :size="80" :src="siteInfo.site_avatar" />
            <div class="profile-info">
              <h1>{{ siteInfo.site_title || 'AM Portal' }}</h1>
              <p class="subtitle">{{ siteInfo.site_subtitle || '个人展示平台' }}</p>
              <p class="desc">{{ siteInfo.site_description || '一个集项目展示、个人博客、AI助手于一体的个人网站' }}</p>
              <div class="links">
                <a v-if="siteInfo.site_github" :href="siteInfo.site_github" target="_blank"><el-icon><Link /></el-icon> GitHub</a>
                <a v-if="siteInfo.site_gitee" :href="siteInfo.site_gitee" target="_blank"><el-icon><Link /></el-icon> Gitee</a>
                <a v-if="siteInfo.site_email" :href="'mailto:' + siteInfo.site_email"><el-icon><Message /></el-icon> {{ siteInfo.site_email }}</a>
              </div>
              <div class="tech-stack" v-if="siteInfo.site_tech_stack">
                <el-tag v-for="tech in siteInfo.site_tech_stack.split(',')" :key="tech" size="small" style="margin-right:6px">{{ tech.trim() }}</el-tag>
              </div>
            </div>
          </div>
        </el-card>

        <el-card style="margin-top:20px">
          <template #header><h2>最近项目</h2></template>
          <div v-loading="projectsLoading" element-loading-text="加载中..." style="min-height:80px">
            <el-empty v-if="!projectsLoading && !recentProjects.length" description="暂无项目" :image-size="80" />
            <el-row :gutter="16">
              <el-col :span="8" v-for="project in recentProjects" :key="project.id">
                <el-card shadow="hover" class="card-hover" @click="$router.push('/projects/' + project.id)">
                  <el-image :src="project.cover" style="width:100%;height:150px" fit="cover" />
                  <h3 style="margin:10px 0 5px">{{ project.name }}</h3>
                  <p style="color:#999;font-size:13px">{{ project.summary }}</p>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <el-card style="margin-top:20px">
          <template #header><h2>最近博客</h2></template>
          <div v-loading="blogsLoading" element-loading-text="加载中..." style="min-height:80px">
            <el-empty v-if="!blogsLoading && !recentBlogs.length" description="暂无博客" :image-size="80" />
            <el-card v-for="blog in recentBlogs" :key="blog.id" shadow="hover" class="card-hover" style="margin-bottom:12px;cursor:pointer" @click="$router.push('/blogs/' + blog.id)">
              <h3>{{ blog.title }}</h3>
              <p style="color:#999;font-size:13px;margin-top:6px">{{ blog.summary }}</p>
              <div style="margin-top:8px;color:#bbb;font-size:12px">{{ blog.publishTime }}</div>
            </el-card>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><h3>技术栈</h3></template>
          <div v-if="siteInfo.site_tech_stack">
            <el-tag v-for="tech in siteInfo.site_tech_stack.split(',')" :key="tech" style="margin:4px">{{ tech.trim() }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSiteInfo, getHomeRecentProjects, getHomeRecentBlogs } from '../api'

const siteInfo = ref<any>({})
const recentProjects = ref<any[]>([])
const recentBlogs = ref<any[]>([])
const projectsLoading = ref(false)
const blogsLoading = ref(false)

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
.profile { display: flex; gap: 24px; align-items: flex-start; }
.profile-info h1 { font-size: 24px; margin-bottom: 4px; }
.subtitle { color: #666; font-size: 16px; margin-bottom: 8px; }
.desc { color: #999; font-size: 14px; margin-bottom: 12px; }
.links { display: flex; gap: 16px; margin-bottom: 12px; }
.links a { display: flex; align-items: center; gap: 4px; font-size: 14px; }
</style>
