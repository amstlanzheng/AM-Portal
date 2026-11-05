<template>
  <div class="page-container">
    <header class="page-head">
      <div>
        <p class="eyebrow">Account</p>
        <h1 class="page-title">个人资料</h1>
        <p class="page-sub">管理你的公开信息与账号安全</p>
      </div>
    </header>

    <div class="profile-grid">
      <section class="surface panel">
        <h2 class="panel-title">基本信息</h2>
        <div class="avatar-row">
          <el-avatar :size="64" :src="form.avatar" class="profile-avatar" />
          <div class="avatar-meta">
            <p class="avatar-name">{{ form.nickname || userStore.username || '未设置昵称' }}</p>
            <button class="text-btn" @click="handleUploadAvatar">更换头像</button>
            <input type="file" ref="fileInput" style="display:none" accept="image/*" @change="onFileChange" />
          </div>
        </div>

        <el-form :model="form" label-position="top" class="profile-form">
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" placeholder="展示在头像旁的名字" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="用于接收通知" />
          </el-form-item>
          <el-form-item label="手机">
            <el-input v-model="form.phone" placeholder="选填" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存修改</el-button>
          </el-form-item>
        </el-form>
      </section>

      <div class="right-column">
        <section class="surface panel">
          <h2 class="panel-title">安全设置</h2>
          <div class="security-row">
            <div>
              <p class="security-label">登录密码</p>
              <p class="faint">建议定期更换，长度不少于 6 位</p>
            </div>
            <el-button @click="openPasswordDialog">修改密码</el-button>
          </div>
        </section>

        <section class="surface panel">
          <h2 class="panel-title">界面配色</h2>
          <p class="faint theme-hint">选择偏好的界面主题，仅对本账号生效</p>
          <div class="theme-grid">
            <button
              v-for="t in THEMES"
              :key="t.key"
              class="theme-swatch"
              :class="{ active: currentTheme === t.key }"
              @click="handleTheme(t.key)"
            >
              <span class="swatch-dot" :style="{ background: t.colors['600'] }">
                <span v-if="currentTheme === t.key" class="swatch-check">✓</span>
              </span>
              <span class="swatch-name">{{ t.name }}</span>
            </button>
            <button class="theme-swatch" :class="{ active: !currentTheme }" @click="handleFollowSite">
              <span class="swatch-dot follow">站</span>
              <span class="swatch-name">跟随站点</span>
            </button>
          </div>
        </section>
      </div>
    </div>

    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="420px">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-position="top">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { updateProfile, updateAvatar, uploadFile, changeMyPassword, updateMyTheme } from '../api'
import { ElMessage, type FormInstance } from 'element-plus'
import { THEMES, getTheme, applyTheme } from '../theme'

const router = useRouter()
const userStore = useUserStore()
const fileInput = ref<HTMLInputElement>()
const form = reactive({ nickname: '', email: '', phone: '', avatar: '' })
const currentTheme = ref('')

const pwdDialogVisible = ref(false)
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

onMounted(async () => {
  if (userStore.userInfo) {
    form.nickname = userStore.userInfo.nickname || ''
    form.email = userStore.userInfo.email || ''
    form.phone = userStore.userInfo.phone || ''
    form.avatar = userStore.userInfo.avatar || ''
    currentTheme.value = userStore.userInfo.theme || ''
  }
})

async function handleTheme(key: string) {
  const prev = currentTheme.value
  currentTheme.value = key
  applyTheme(key)
  try {
    await updateMyTheme(key)
    if (userStore.userInfo) userStore.userInfo.theme = key
    ElMessage.success(`已切换为「${getTheme(key).name}」主题`)
  } catch {
    currentTheme.value = prev
    if (prev) applyTheme(prev)
    else userStore.syncTheme()
    ElMessage.error('主题保存失败，已还原')
  }
}

async function handleFollowSite() {
  const prev = currentTheme.value
  currentTheme.value = ''
  try {
    await updateMyTheme('')
    if (userStore.userInfo) userStore.userInfo.theme = null
    await userStore.syncTheme()
    ElMessage.success('已恢复跟随站点默认配色')
  } catch {
    currentTheme.value = prev
    applyTheme(prev)
    ElMessage.error('操作失败，已还原')
  }
}

function handleUploadAvatar() { fileInput.value?.click() }

async function onFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const res: any = await uploadFile(file, 'avatar')
  form.avatar = res.data.url
  await updateAvatar(res.data.url)
  ElMessage.success('头像更新成功')
}

async function handleSave() {
  await updateProfile({ nickname: form.nickname, email: form.email, phone: form.phone })
  ElMessage.success('资料更新成功')
  userStore.fetchUserInfo()
}

function openPasswordDialog() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdDialogVisible.value = true
}

async function handleChangePassword() {
  await pwdFormRef.value?.validate()
  await changeMyPassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
  pwdDialogVisible.value = false
  ElMessage.success('密码修改成功，请重新登录')
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  router.push('/login')
}
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(0, 1fr);
  gap: var(--space-5);
  align-items: start;
}

.panel {
  padding: var(--space-6);
}

.panel-title {
  font-size: var(--text-lg);
  font-weight: 650;
  margin-bottom: var(--space-5);
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding-bottom: var(--space-5);
  margin-bottom: var(--space-5);
  border-bottom: 1px solid var(--color-border);
}

.profile-avatar {
  border: 3px solid #fff;
  box-shadow: var(--shadow-sm);
  background: var(--brand-50);
  color: var(--brand-600);
  font-weight: 600;
}

.avatar-name {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text-strong);
  margin-bottom: 4px;
}

.text-btn {
  border: none;
  background: transparent;
  padding: 0;
  font-size: var(--text-sm);
  font-family: inherit;
  color: var(--brand-600);
  cursor: pointer;
}
.text-btn:hover {
  text-decoration: underline;
}

.profile-form {
  max-width: 460px;
}

.security-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--surface-muted);
}

.right-column {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.theme-hint {
  margin: -12px 0 16px;
}

.theme-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.theme-swatch {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: var(--space-2);
  border-radius: var(--radius-sm);
  font-family: inherit;
  min-width: 64px;
}

.theme-swatch:hover {
  background: var(--surface-muted);
}

.swatch-dot {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 0 0 2px rgba(255, 255, 255, 0.35);
}

.swatch-dot.follow {
  background: var(--surface-muted);
  border: 1px dashed var(--color-text-faint);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  box-shadow: none;
}

.theme-swatch.active .swatch-dot {
  outline: 2px solid var(--brand-600);
  outline-offset: 2px;
}

.swatch-check {
  color: #fff;
  font-weight: 700;
  font-size: var(--text-sm);
  line-height: 1;
}

.swatch-name {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.theme-swatch.active .swatch-name {
  color: var(--brand-600);
  font-weight: 600;
}

.security-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-strong);
  margin-bottom: 2px;
}

@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
