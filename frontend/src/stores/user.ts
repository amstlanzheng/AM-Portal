import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request'
import { applyTheme, isValidTheme } from '../theme'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const isAdmin = ref(localStorage.getItem('isAdmin') === 'true')
  const userInfo = ref<any>(null)

  /** 按「个人偏好 > 站点默认 > 内置默认」校正当前主题 */
  async function syncTheme() {
    if (token.value) {
      if (!userInfo.value) await fetchUserInfo()
      if (isValidTheme(userInfo.value?.theme)) {
        applyTheme(userInfo.value.theme)
        return
      }
    }
    try {
      const res: any = await request.get('/home/site-info')
      const siteTheme = res.data?.site_theme
      applyTheme(isValidTheme(siteTheme) ? siteTheme : null)
    } catch {
      // 接口失败时保持 localStorage 缓存主题
    }
  }

  async function login(loginData: { username: string; password: string }) {
    const res: any = await request.post('/auth/login', loginData)
    token.value = res.data.accessToken
    localStorage.setItem('token', res.data.accessToken)
    localStorage.setItem('refreshToken', res.data.refreshToken)
    await fetchUserInfo()
    syncTheme()
  }

  async function register(registerData: { username: string; password: string; nickname?: string; email?: string }) {
    await request.post('/auth/register', registerData)
  }

  async function fetchUserInfo() {
    try {
      const res: any = await request.get('/users/me')
      userInfo.value = res.data
      username.value = res.data.username
      isAdmin.value = res.data.isAdmin
      localStorage.setItem('username', res.data.username)
      localStorage.setItem('isAdmin', String(res.data.isAdmin))
    } catch {
      // ignore
    }
  }

  function logout() {
    token.value = ''
    username.value = ''
    isAdmin.value = false
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('username')
    localStorage.removeItem('isAdmin')
    // 登出后回到站点默认配色
    syncTheme()
  }

  return { token, username, isAdmin, userInfo, login, register, fetchUserInfo, logout, syncTheme }
})
