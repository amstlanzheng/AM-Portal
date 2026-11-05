<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'

// 挂载后按「个人偏好 > 站点默认 > 内置默认」异步校正主题
onMounted(() => {
  useUserStore().syncTheme()
})
</script>
