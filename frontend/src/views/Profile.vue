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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { updateProfile, updateAvatar, uploadFile } from '../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const fileInput = ref<HTMLInputElement>()
const form = reactive({ nickname: '', email: '', phone: '', avatar: '' })

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
</script>
