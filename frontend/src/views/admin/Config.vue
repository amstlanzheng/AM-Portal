<template>
  <div>
    <h2 class="admin-page-title" style="margin-bottom:20px">系统配置</h2>
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
        <el-form-item label="站点默认配色">
          <div class="site-theme-picker">
            <div class="theme-grid">
              <button
                v-for="t in THEMES"
                :key="t.key"
                type="button"
                class="theme-swatch"
                :class="{ active: form.site_theme === t.key }"
                @click="form.site_theme = t.key"
              >
                <span class="swatch-dot" :style="{ background: t.colors['600'] }">
                  <span v-if="form.site_theme === t.key" class="swatch-check">✓</span>
                </span>
                <span class="swatch-name">{{ t.name }}</span>
              </button>
            </div>
            <p class="theme-tip">仅影响未设置个人偏好的用户；已登录用户可在「个人中心」自选配色</p>
          </div>
        </el-form-item>
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
import { THEMES, DEFAULT_THEME } from '../../theme'

const form = reactive({ site_title: '', site_subtitle: '', site_description: '', site_github: '', site_gitee: '', site_email: '', site_qq: '', site_tech_stack: '', site_avatar: '', site_theme: DEFAULT_THEME })
const aiEnabled = ref('true')
const saving = ref(false)

onMounted(async () => {
  try {
    const res: any = await getHomeConfig()
    Object.assign(form, res.data || {})
    if (!form.site_theme) form.site_theme = DEFAULT_THEME
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

.site-theme-picker { width: 100%; }

.theme-grid { display: flex; flex-wrap: wrap; gap: 4px; }

.theme-swatch {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  font-family: inherit;
  min-width: 60px;
}

.theme-swatch:hover { background: var(--el-fill-color-light); }

.swatch-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 0 0 2px rgba(255, 255, 255, 0.35);
}

.theme-swatch.active .swatch-dot {
  outline: 2px solid var(--brand-600);
  outline-offset: 2px;
}

.swatch-check { color: #fff; font-weight: 700; font-size: 13px; line-height: 1; }

.swatch-name { font-size: 12px; color: var(--el-text-color-secondary); }

.theme-swatch.active .swatch-name { color: var(--brand-600); font-weight: 600; }

.theme-tip { font-size: 12px; color: var(--el-text-color-secondary); margin-top: 8px; }
</style>
