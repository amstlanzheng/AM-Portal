<template>
  <div ref="wrapRef" class="wb-badge-wrap">
    <transition name="wb-panel">
      <div v-if="panelOpen" class="wb-panel">
        <div class="wb-panel-title">天气背景</div>
        <button
          v-for="opt in OPTIONS"
          :key="opt.value"
          class="wb-option"
          :class="{ active: isActive(opt.value) }"
          @click="choose(opt.value)"
        >
          <span class="wb-opt-icon" v-html="opt.value === 'auto' ? ICONS[scene] : ICONS[opt.value]"></span>
          <span class="wb-opt-label">{{ opt.label }}</span>
          <span v-if="opt.value === 'auto'" class="wb-opt-tag">实时</span>
        </button>
        <button v-if="error && mode === 'auto'" class="wb-retry" @click="retry">重试获取天气</button>
      </div>
    </transition>

    <button
      class="wb-badge"
      :class="{ 'is-error': error && mode === 'auto' }"
      :title="error && mode === 'auto' ? '天气不可用，点击查看' : '当前天气，点击切换背景'"
      @click="panelOpen = !panelOpen"
    >
      <span class="wb-badge-icon" v-html="badgeIcon"></span>
      <span v-if="temperature !== null" class="wb-temp">{{ temperature }}°</span>
      <span class="wb-desc">{{ badgeDesc }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useWeather, type WeatherScene } from '../../composables/useWeather'

const { scene, temperature, desc, mode, error, setManualScene, resetToAuto, retry } = useWeather()

const panelOpen = ref(false)
const wrapRef = ref<HTMLElement | null>(null)

const ICONS: Record<WeatherScene, string> = {
  sunny:
    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="4.2" fill="currentColor" stroke="none"/><path d="M12 2.5v2.2M12 19.3v2.2M5.3 5.3l1.6 1.6M17.1 17.1l1.6 1.6M2.5 12h2.2M19.3 12h2.2M5.3 18.7l1.6-1.6M17.1 6.9l1.6-1.6"/></svg>',
  cloudy:
    '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6.8 18.5a4.3 4.3 0 0 1-.4-8.58A5.8 5.8 0 0 1 17.7 11a3.4 3.4 0 0 1-.9 6.7z"/></svg>',
  rainy:
    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M6.8 13.5a4 4 0 0 1-.4-7.98A5.5 5.5 0 0 1 17 7.2a3 3 0 0 1-.2 6.3H6.8z" fill="currentColor" stroke="none"/><path d="M8 16.5l-1 3.2M12.4 16.5l-1 3.2M16.8 16.5l-1 3.2"/></svg>',
  snowy:
    '<svg viewBox="0 0 24 24" fill="none"><path d="M6.8 13.5a4 4 0 0 1-.4-7.98A5.5 5.5 0 0 1 17 7.2a3 3 0 0 1-.2 6.3H6.8z" fill="currentColor"/><g stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><path d="M8 17.4v.01M12 19.6v.01M16 17.4v.01M10 21.4v.01M14 21.4v.01"/></g></svg>',
  night:
    '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20.2 14.3A8.3 8.3 0 0 1 9.7 3.8a8.3 8.3 0 1 0 10.5 10.5z"/><circle cx="17.2" cy="5.4" r="1.1"/><circle cx="20" cy="8.6" r="0.7"/></svg>'
}

type OptionValue = 'auto' | WeatherScene
const OPTIONS: { value: OptionValue; label: string }[] = [
  { value: 'auto', label: '跟随实时' },
  { value: 'sunny', label: '晴天' },
  { value: 'cloudy', label: '多云' },
  { value: 'rainy', label: '雨天' },
  { value: 'snowy', label: '雪天' },
  { value: 'night', label: '夜晚' }
]

const badgeIcon = computed(() => ICONS[scene.value])
const badgeDesc = computed(() => {
  if (error.value && mode.value === 'auto') return '天气不可用'
  if (mode.value === 'manual') return desc.value + ' · 手动'
  return desc.value || '…'
})

function isActive(value: OptionValue) {
  if (value === 'auto') return mode.value === 'auto'
  return mode.value === 'manual' && scene.value === value
}

function choose(value: OptionValue) {
  if (value === 'auto') {
    resetToAuto()
  } else {
    setManualScene(value)
  }
}

function onDocMouseDown(e: MouseEvent) {
  if (panelOpen.value && wrapRef.value && !wrapRef.value.contains(e.target as Node)) {
    panelOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('mousedown', onDocMouseDown)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onDocMouseDown)
})
</script>

<style scoped>
.wb-badge-wrap {
  position: fixed;
  left: 24px;
  bottom: 24px;
  z-index: 997;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

/* ---------- 角标胶囊 ---------- */
.wb-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 38px;
  padding: 0 14px 0 12px;
  border: 1px solid rgba(120, 140, 160, 0.25);
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: var(--shadow-sm);
  font-family: inherit;
  font-size: var(--text-sm);
  color: var(--color-text);
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.wb-badge:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.wb-badge.is-error {
  color: var(--color-text-muted);
}

.wb-badge-icon {
  width: 20px;
  height: 20px;
  display: grid;
  place-items: center;
  color: var(--brand-600);
}

.wb-badge-icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.wb-temp {
  font-weight: 650;
  font-variant-numeric: tabular-nums;
}

.wb-desc {
  color: var(--color-text-muted);
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---------- 切换面板 ---------- */
.wb-panel {
  min-width: 168px;
  padding: var(--space-3);
  border: 1px solid rgba(120, 140, 160, 0.22);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.wb-panel-title {
  font-size: var(--text-xs);
  color: var(--color-text-faint);
  padding: 2px 8px 6px;
}

.wb-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 7px 8px;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-family: inherit;
  font-size: var(--text-sm);
  color: var(--color-text);
  cursor: pointer;
  text-align: left;
  transition: background-color 0.15s ease;
}

.wb-option:hover {
  background: var(--surface-muted);
}

.wb-option.active {
  background: var(--brand-50);
  color: var(--brand-600);
  font-weight: 600;
}

.wb-opt-icon {
  width: 18px;
  height: 18px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.wb-opt-icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.wb-opt-label {
  flex: 1;
}

.wb-opt-tag {
  font-size: var(--text-xs);
  color: var(--color-text-faint);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-pill);
  padding: 0 6px;
  line-height: 18px;
}

.wb-option.active .wb-opt-tag {
  color: var(--brand-600);
  border-color: color-mix(in srgb, var(--brand-600) 35%, transparent);
}

.wb-retry {
  margin-top: 6px;
  padding: 6px 8px;
  border: 1px dashed var(--color-border);
  border-radius: 8px;
  background: transparent;
  font-family: inherit;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: color 0.15s ease, border-color 0.15s ease;
}

.wb-retry:hover {
  color: var(--brand-600);
  border-color: color-mix(in srgb, var(--brand-600) 40%, transparent);
}

/* ---------- 面板展开动画 ---------- */
.wb-panel-enter-active,
.wb-panel-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.wb-panel-enter-from,
.wb-panel-leave-to {
  opacity: 0;
  transform: translateY(6px) scale(0.97);
}

@media (max-width: 768px) {
  .wb-badge-wrap {
    left: 16px;
    bottom: 68px;
  }
  .wb-badge {
    height: 34px;
    padding: 0 12px 0 10px;
  }
  .wb-desc {
    max-width: 64px;
  }
}
</style>
