import request from '../utils/request'

// 首页
export const getHomePage = () => request.get('/home')
export const getSiteInfo = () => request.get('/home/site-info')
export const getHomeRecentProjects = (limit: number = 5) => request.get('/home/recent-projects', { params: { limit } })
export const getHomeRecentBlogs = (limit: number = 5) => request.get('/home/recent-blogs', { params: { limit } })

// 认证
export const login = (data: any) => request.post('/auth/login', data)
export const register = (data: any) => request.post('/auth/register', data)
export const refreshToken = (refreshToken: string) => request.post('/auth/refresh', null, { params: { refreshToken } })

// 用户
export const getCurrentUser = () => request.get('/users/me')
export const updateProfile = (data: any) => request.put('/users/me/profile', null, { params: data })
export const updateAvatar = (avatarUrl: string) => request.put('/users/me/avatar', null, { params: { avatarUrl } })
export const listUsers = (params: any) => request.get('/users/admin/list', { params })
export const approveUser = (id: number) => request.put(`/users/admin/${id}/approve`)
export const disableUser = (id: number) => request.put(`/users/admin/${id}/disable`)
export const setAdmin = (id: number, isAdmin: boolean) => request.put(`/users/admin/${id}/admin`, null, { params: { isAdmin } })
export const adminChangePassword = (id: number, data: { newPassword: string }) => request.put(`/users/admin/${id}/password`, data)
export const adminResetPassword = (id: number) => request.put(`/users/admin/${id}/password/reset`)
export const changeMyPassword = (data: { oldPassword: string, newPassword: string }) => request.put('/users/me/password', data)

// 项目
export const listProjects = (params: any) => request.get('/projects', { params })
export const getProject = (id: number) => request.get(`/projects/${id}`)
export const getRecentProjects = (limit: number = 5) => request.get('/projects/recent', { params: { limit } })
export const addProject = (data: any) => request.post('/admin/projects', data)
export const updateProject = (data: any) => request.put('/admin/projects', data)
export const deleteProject = (id: number) => request.delete(`/admin/projects/${id}`)
export const setProjectTop = (id: number, isTop: boolean) => request.put(`/admin/projects/${id}/top`, null, { params: { isTop } })
export const setProjectVisible = (id: number, isVisible: boolean) => request.put(`/admin/projects/${id}/visible`, null, { params: { isVisible } })

// 博客
export const listBlogs = (params: any) => request.get('/blogs', { params })
export const getBlog = (id: number) => request.get(`/blogs/${id}`)
export const getRecentBlogs = (limit: number = 5) => request.get('/blogs/recent', { params: { limit } })
export const listCategories = () => request.get('/blogs/categories')
export const listTags = () => request.get('/blogs/tags')
export const addBlog = (data: any, tagIds?: number[]) => request.post('/admin/blogs', data, { params: { tagIds } })
export const updateBlog = (data: any, tagIds?: number[]) => request.put('/admin/blogs', data, { params: { tagIds } })
export const deleteBlog = (id: number) => request.delete(`/admin/blogs/${id}`)
export const addCategory = (data: any) => request.post('/admin/blogs/categories', data)
export const deleteCategory = (id: number) => request.delete(`/admin/blogs/categories/${id}`)
export const addTag = (data: any) => request.post('/admin/blogs/tags', data)
export const deleteTag = (id: number) => request.delete(`/admin/blogs/tags/${id}`)
export const reviewBlog = (id: number, data: { reviewStatus: string, reviewRemark?: string }) => request.put(`/admin/blogs/${id}/review`, data)
export const listAdminBlogs = (params: any) => request.get('/admin/blogs', { params })
export const detectSensitiveWords = (text: string) => request.post('/admin/sensitive-words/detect', { text })

// 敏感词
export const listSensitiveWords = (params: any) => request.get('/admin/sensitive-words', { params })
export const addSensitiveWord = (data: any) => request.post('/admin/sensitive-words', data)
export const updateSensitiveWord = (data: any) => request.put('/admin/sensitive-words', data)
export const deleteSensitiveWord = (id: number) => request.delete(`/admin/sensitive-words/${id}`)
export const setSensitiveWordEnabled = (id: number, enabled: boolean) => request.put(`/admin/sensitive-words/${id}/enabled`, null, { params: { enabled } })
export const batchAddSensitiveWords = (words: any[]) => request.post('/admin/sensitive-words/batch', words)

// AI
export const chatWithAi = (data: any) => fetch('/api/ai/chat', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  },
  body: JSON.stringify(data)
})
export const createConversation = () => request.post('/ai/conversations')
export const listConversations = () => request.get('/ai/conversations')
export const getMessages = (id: number) => request.get(`/ai/conversations/${id}/messages`)
export const deleteConversation = (id: number) => request.delete(`/ai/conversations/${id}`)

// Prompt
export const listPrompts = (params: any) => request.get('/admin/prompts', { params })
export const addPrompt = (data: any) => request.post('/admin/prompts', data)
export const updatePrompt = (data: any) => request.put('/admin/prompts', data)
export const deletePrompt = (id: number) => request.delete(`/admin/prompts/${id}`)
export const setPromptEnabled = (id: number, enabled: boolean) => request.put(`/admin/prompts/${id}/enabled`, null, { params: { enabled } })
export const setPromptDefault = (id: number) => request.put(`/admin/prompts/${id}/default`)

// 文件
export const uploadFile = (file: File, directory: string = 'other') => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/files/upload', formData, { params: { directory }, headers: { 'Content-Type': 'multipart/form-data' } })
}
export const listFiles = (params: any) => request.get('/admin/files', { params })
export const deleteFile = (id: number) => request.delete(`/admin/files/${id}`)
export const batchDeleteFiles = (ids: number[]) => Promise.all(ids.map(id => deleteFile(id)))

// 系统配置
export const getHomeConfig = () => request.get('/admin/config/home')
export const updateHomeConfig = (data: any) => request.put('/admin/config/home', data)
export const getAllConfigs = () => request.get('/admin/config/all')
export const setConfig = (key: string, value: string) => request.put(`/admin/config/${key}`, { value })

// 仪表盘
export const getDashboard = () => request.get('/admin/dashboard')

// 日志
export const listLogs = (params: any) => request.get('/admin/logs', { params })
