<template>
  <div class="page-container" style="display:flex;justify-content:center;align-items:center;min-height:60vh">
    <el-card style="width:400px">
      <h2 style="text-align:center;margin-bottom:24px">注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
        <el-form-item prop="username"><el-input v-model="form.username" prefix-icon="User" placeholder="用户名" /></el-form-item>
        <el-form-item prop="password"><el-input v-model="form.password" prefix-icon="Lock" type="password" placeholder="密码" show-password /></el-form-item>
        <el-form-item prop="email"><el-input v-model="form.email" prefix-icon="Message" placeholder="邮箱(可选)" /></el-form-item>
        <el-form-item><el-button type="primary" style="width:100%" @click="handleRegister" :loading="loading">注册</el-button></el-form-item>
      </el-form>
      <div style="text-align:center"><router-link to="/login">已有账号？去登录</router-link></div>
    </el-card>
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
