<template>
  <div class="tool-page">
    <!-- 介绍区 -->
    <section class="tool-intro">
      <div class="intro-head">
        <span class="intro-icon"><el-icon :size="22"><component :is="icon" /></el-icon></span>
        <div>
          <h1 class="intro-title">{{ title }}</h1>
          <p class="intro-text">{{ intro }}</p>
        </div>
      </div>
      <p class="intro-usage">使用方法：{{ usage }}</p>
    </section>

    <!-- 操作区 -->
    <section class="tool-panel" v-if="$slots.panel">
      <slot name="panel" />
    </section>

    <!-- 结果区 -->
    <section class="tool-result">
      <template v-if="resultReady !== false">
        <slot name="result" />
      </template>
      <p v-else class="result-empty">结果将显示在这里</p>
    </section>

    <!-- 隐私与免责 -->
    <p class="privacy-note">🔒 所有数据均在本地计算，不涉及服务器存储。</p>
    <p class="privacy-note disclaimer">本工具计算/生成的数据仅用于本地展示，不用于任何商业用途，不保证结果的准确性，请自行判断参考。{{ extraNote }}</p>
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  title: string
  intro: string
  usage: string
  icon: Component
  /** false 时结果区显示占位文案 */
  resultReady?: boolean
  extraNote?: string
}>()
</script>

<style scoped>
.tool-page {
  max-width: var(--container);
  margin: 0 auto;
  padding: var(--space-8) var(--space-6) var(--space-10);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.tool-intro,
.tool-panel,
.tool-result {
  background: var(--surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
}

.intro-head {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
}

.intro-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  box-shadow: var(--shadow-accent);
}

.intro-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-text-strong);
  letter-spacing: -0.01em;
  margin: 0 0 6px;
}

.intro-text {
  margin: 0;
  font-size: var(--text-base);
  color: var(--color-text-muted);
  line-height: 1.7;
}

.intro-usage {
  margin: var(--space-4) 0 0;
  padding: 10px var(--space-4);
  border-left: 3px solid var(--brand-500);
  background: var(--brand-50);
  border-radius: 0 8px 8px 0;
  font-size: var(--text-sm);
  color: var(--color-text);
}

.result-empty {
  margin: 0;
  text-align: center;
  padding: var(--space-8) 0;
  color: var(--color-text-faint);
  font-size: var(--text-sm);
}

.privacy-note {
  margin: 0;
  text-align: center;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

.privacy-note.disclaimer {
  margin-top: 2px;
}

@media (max-width: 768px) {
  .tool-page {
    padding: var(--space-5) var(--space-4) var(--space-8);
  }
  .tool-intro,
  .tool-panel,
  .tool-result {
    padding: var(--space-4);
  }
}
</style>
