import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const isAdmin = ref(localStorage.getItem('isAdmin') === 'true')
  const userInfo = ref<any>(null)

  async function login(loginData: { username: string; password: string }) {
    const res: any = await request.post('/auth/login', loginData)
    token.value = res.data.accessToken
    localStorage.setItem('token', res.data.accessToken)
    localStorage.setItem('refreshToken', res.data.refreshToken)
    await fetchUserInfo()
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
  }

  return { token, username, isAdmin, userInfo, login, register, fetchUserInfo, logout }
})
