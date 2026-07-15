<template>
  <div>
    <h2 style="margin-bottom:20px">系统配置</h2>
    <el-card>
      <el-form :model="form" label-width="120px" style="max-width:600px">
        <el-form-item label="网站头像">
          <div class="avatar-upload">
            <el-avatar :size="80" :src="form.site_avatar" />
            <el-upload :show-file-list="false" :before-upload="handleAvatarUpload" accept="image/*">
              <el-button size="small" type="primary">上传头像</el-button>
            </el-upload>
            <el-input v-model="form.site_avatar" placeholder="或填写图片URL" style="width:280px" />
          </div>
        </el-form-item>
        <el-form-item label="网站标题"><el-input v-model="form.site_title" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.site_subtitle" /></el-form-item>
        <el-form-item label="个人简介"><el-input v-model="form.site_description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="Github"><el-input v-model="form.site_github" /></el-form-item>
        <el-form-item label="Gitee"><el-input v-model="form.site_gitee" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.site_email" /></el-form-item>
        <el-form-item label="QQ"><el-input v-model="form.site_qq" /></el-form-item>
        <el-form-item label="技术栈"><el-input v-model="form.site_tech_stack" placeholder="逗号分隔" /></el-form-item>
        <el-form-item label="AI 开放"><el-switch v-model="aiEnabled" active-value="true" inactive-value="false" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSave" :loading="saving">保存</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getHomeConfig, updateHomeConfig, setConfig, uploadFile } from '../../api'
import { ElMessage } from 'element-plus'

const form = reactive({ site_title: '', site_subtitle: '', site_description: '', site_github: '', site_gitee: '', site_email: '', site_qq: '', site_tech_stack: '', site_avatar: '' })
const aiEnabled = ref('true')
const saving = ref(false)

onMounted(async () => {
  try {
    const res: any = await getHomeConfig()
    Object.assign(form, res.data || {})
    aiEnabled.value = res.data?.ai_enabled || 'true'
  } catch { /* ignore */ }
})

async function handleAvatarUpload(file: File) {
  try {
    const res: any = await uploadFile(file, 'avatar')
    form.site_avatar = res.data?.url || ''
    ElMessage.success('头像上传成功')
  } catch {
    ElMessage.error('头像上传失败')
  }
  return false
}

async function handleSave() {
  saving.value = true
  try {
    await updateHomeConfig(form)
    await setConfig('ai_enabled', aiEnabled.value)
    ElMessage.success('保存成功')
  } catch { /* error handled */ }
  finally { saving.value = false }
}
</script>

<style scoped>
.avatar-upload { display: flex; align-items: center; gap: 12px; }
</style>
