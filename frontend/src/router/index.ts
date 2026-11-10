import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('../layouts/FrontLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'projects', name: 'Projects', component: () => import('../views/Projects.vue') },
      { path: 'projects/:id', name: 'ProjectDetail', component: () => import('../views/ProjectDetail.vue') },
      { path: 'blogs', name: 'Blogs', component: () => import('../views/Blogs.vue') },
      { path: 'blogs/:id', name: 'BlogDetail', component: () => import('../views/BlogDetail.vue') },
      { path: 'tools', name: 'ToolsIndex', component: () => import('../views/tools/ToolsIndex.vue') },
      { path: 'tools/calorie', name: 'ToolCalorie', component: () => import('../views/tools/CalorieCalculator.vue') },
      { path: 'tools/image-compress', name: 'ToolImageCompress', component: () => import('../views/tools/ImageCompress.vue') },
      { path: 'tools/format-convert', name: 'ToolFormatConvert', component: () => import('../views/tools/FormatConvert.vue') },
      { path: 'tools/json-format', name: 'ToolJsonFormat', component: () => import('../views/tools/JsonFormat.vue') },
      { path: 'tools/md-wechat', name: 'ToolMdWechat', component: () => import('../views/tools/MdWechat.vue') },
      { path: 'tools/password', name: 'ToolPassword', component: () => import('../views/tools/PasswordGenerator.vue') },
      { path: 'tools/color-picker', name: 'ToolColorPicker', component: () => import('../views/tools/ColorPicker.vue') },
      { path: 'tools/time-convert', name: 'ToolTimeConvert', component: () => import('../views/tools/TimeConvert.vue') },
      { path: 'tools/base64', name: 'ToolBase64', component: () => import('../views/tools/Base64Codec.vue') },
      { path: 'tools/url-encode', name: 'ToolUrlEncode', component: () => import('../views/tools/UrlCodec.vue') },
      { path: 'tools/html-entity', name: 'ToolHtmlEntity', component: () => import('../views/tools/HtmlEntity.vue') },
      { path: 'tools/hash', name: 'ToolHash', component: () => import('../views/tools/HashTool.vue') },
      { path: 'tools/uuid', name: 'ToolUuid', component: () => import('../views/tools/UuidGenerator.vue') },
      { path: 'tools/unicode', name: 'ToolUnicode', component: () => import('../views/tools/UnicodeCodec.vue') },
      { path: 'fun', name: 'FunIndex', component: () => import('../views/tools/FunIndex.vue') },
      { path: 'fun/typing', name: 'FunTyping', component: () => import('../views/tools/TypingTest.vue') },
      { path: 'fun/reaction', name: 'FunReaction', component: () => import('../views/tools/ReactionTest.vue') },
      { path: 'fun/mbti', name: 'FunMbti', component: () => import('../views/tools/MbtiQuiz.vue') },
      { path: 'fun/clicker', name: 'FunClicker', component: () => import('../views/tools/ClickCounter.vue') },
      { path: 'login', name: 'Login', component: () => import('../views/Login.vue') },
      { path: 'register', name: 'Register', component: () => import('../views/Register.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { requiresAuth: true } },
    ]
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue') },
      { path: 'projects', name: 'AdminProjects', component: () => import('../views/admin/Projects.vue') },
      { path: 'blogs', name: 'AdminBlogs', component: () => import('../views/admin/Blogs.vue') },
      { path: 'tips', name: 'AdminTips', component: () => import('../views/admin/Tips.vue') },
      { path: 'quiz', name: 'AdminQuiz', component: () => import('../views/admin/Quiz.vue') },
      { path: 'sensitive-words', name: 'AdminSensitiveWords', component: () => import('../views/admin/SensitiveWords.vue') },
      { path: 'prompts', name: 'AdminPrompts', component: () => import('../views/admin/Prompts.vue') },
      { path: 'files', name: 'AdminFiles', component: () => import('../views/admin/Files.vue') },
      { path: 'config', name: 'AdminConfig', component: () => import('../views/admin/Config.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const isAdmin = localStorage.getItem('isAdmin') === 'true'

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && !isAdmin) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
