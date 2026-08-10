<template>
  <div class="page-container">
    <el-card>
      <template #header><h2>个人资料</h2></template>
      <el-form :model="form" label-width="80px" style="max-width:500px">
        <el-form-item label="头像">
          <el-avatar :size="64" :src="form.avatar" />
          <el-button style="margin-left:12px" @click="handleUploadAvatar">上传头像</el-button>
          <input type="file" ref="fileInput" style="display:none" accept="image/*" @change="onFileChange" />
        </el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSave">保存</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header><h2>安全设置</h2></template>
      <el-button type="primary" @click="openPasswordDialog">修改密码</el-button>
    </el-card>

    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="420px">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px">
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
import { updateProfile, updateAvatar, uploadFile, changeMyPassword } from '../api'
import { ElMessage, type FormInstance } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const fileInput = ref<HTMLInputElement>()
const form = reactive({ nickname: '', email: '', phone: '', avatar: '' })

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
  }
})

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
