<template>
  <div class="tools-index">
    <header class="page-head">
      <h1>趣味测试</h1>
      <p>打字、反应力、人格测试、手速挑战，测完把成绩复制去分享。</p>
    </header>

    <div class="tool-grid">
      <router-link
        v-for="tool in funTools"
        :key="tool.name"
        :to="`/fun/${tool.name}`"
        class="tool-card"
      >
        <span class="card-icon"><el-icon :size="24"><component :is="tool.icon" /></el-icon></span>
        <div class="card-body">
          <h3>{{ tool.title }}</h3>
          <p>{{ tool.desc }}</p>
        </div>
        <el-icon class="card-arrow"><ArrowRight /></el-icon>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowRight } from '@element-plus/icons-vue'
import { funTools } from './toolMeta'
</script>

<style scoped>
.tools-index {
  max-width: var(--container);
  margin: 0 auto;
  padding: var(--space-8) var(--space-6) var(--space-12);
}

.page-head h1 {
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-text-strong);
  letter-spacing: -0.02em;
  margin: 0 0 6px;
}

.page-head p {
  margin: 0 0 var(--space-6);
  color: var(--color-text-muted);
  font-size: var(--text-base);
}

.tool-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--space-4);
}

.tool-card {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.tool-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--brand-300);
}

.card-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  box-shadow: var(--shadow-accent);
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-body h3 {
  margin: 0 0 6px;
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-text-strong);
}

.card-body p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-arrow {
  color: var(--color-text-faint);
  margin-top: 14px;
  transition: transform 0.2s ease, color 0.2s ease;
}

.tool-card:hover .card-arrow {
  transform: translateX(3px);
  color: var(--brand-500);
}

@media (max-width: 768px) {
  .tools-index {
    padding: var(--space-5) var(--space-4) var(--space-10);
  }
}
</style>
