<template>
  <div class="auth-wrap">
    <div class="auth-card surface">
      <div class="auth-head">
        <span class="auth-mark">A</span>
        <h1 class="auth-title">欢迎回来</h1>
        <p class="auth-sub">登录后可管理项目、博客与个人资料</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" prefix-icon="User" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" prefix-icon="Lock" type="password" placeholder="密码" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth-submit" @click="handleLogin" :loading="loading">登录</el-button>
        </el-form-item>
      </el-form>

      <div class="auth-foot">
        还没有账号？<router-link to="/register">去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = { username: [{ required: true, message: '请输入用户名', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }] }

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) { /* error handled by interceptor */ }
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
