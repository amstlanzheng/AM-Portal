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
