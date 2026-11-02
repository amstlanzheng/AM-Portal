<template>
  <div class="auth-wrap">
    <div class="auth-card surface">
      <div class="auth-head">
        <span class="auth-mark">A</span>
        <h1 class="auth-title">创建账号</h1>
        <p class="auth-sub">注册后需等待管理员审核通过</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" prefix-icon="User" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" prefix-icon="Lock" type="password" placeholder="密码（至少 6 位）" show-password />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" prefix-icon="Message" placeholder="邮箱（可选）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth-submit" @click="handleRegister" :loading="loading">注册</el-button>
        </el-form-item>
      </el-form>

      <div class="auth-foot">
        已有账号？<router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '', email: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 50, message: '用户名长度3-50', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }]
}

async function handleRegister() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.register(form)
    ElMessage.success('注册成功，请等待管理员审核')
    router.push('/login')
  } catch { /* error handled by interceptor */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - var(--header-h) - 120px);
  padding: var(--space-8) var(--space-4);
}

.auth-card {
  width: 100%;
  max-width: 420px;
  padding: var(--space-10) var(--space-8);
}

.auth-head {
  text-align: center;
  margin-bottom: var(--space-8);
}

.auth-mark {
  display: inline-grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 13px;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  box-shadow: var(--shadow-accent);
  margin-bottom: var(--space-4);
}

.auth-title {
  font-size: var(--text-2xl);
  font-weight: 700;
}

.auth-sub {
  margin-top: 8px;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.auth-submit {
  width: 100%;
  height: 44px;
  font-size: var(--text-base);
  border-radius: 10px;
}

.auth-foot {
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.auth-foot a {
  font-weight: 500;
}
</style>
