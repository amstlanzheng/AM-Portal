<template>
  <div class="page-container" style="display:flex;justify-content:center;align-items:center;min-height:60vh">
    <el-card style="width:400px">
      <h2 style="text-align:center;margin-bottom:24px">登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
        <el-form-item prop="username"><el-input v-model="form.username" prefix-icon="User" placeholder="用户名" /></el-form-item>
        <el-form-item prop="password"><el-input v-model="form.password" prefix-icon="Lock" type="password" placeholder="密码" show-password @keyup.enter="handleLogin" /></el-form-item>
        <el-form-item><el-button type="primary" style="width:100%" @click="handleLogin" :loading="loading">登录</el-button></el-form-item>
      </el-form>
      <div style="text-align:center"><router-link to="/register">没有账号？去注册</router-link></div>
    </el-card>
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
