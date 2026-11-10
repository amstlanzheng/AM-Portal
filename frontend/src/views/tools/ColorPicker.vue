<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="true">
    <template #panel>
      <div class="picker-panel">
        <div class="swatch-col">
          <label class="native-picker" :title="hex">
            <input type="color" :value="hex" @input="onNativePick" />
            <span class="native-hint">点击选色</span>
          </label>
          <div class="current-swatch" :style="{ background: hex }">{{ hex.toUpperCase() }}</div>
        </div>

        <div class="format-col">
          <div v-for="f in formats" :key="f.key + '-' + inputEpoch" class="format-row">
            <span class="format-label">{{ f.label }}</span>
            <input
              class="format-input"
              :value="f.value"
              spellcheck="false"
              @focus="editing = f.key"
              @blur="commitFormat(f.key, ($event.target as HTMLInputElement).value)"
              @keydown.enter="($event.target as HTMLInputElement).blur()"
            />
            <el-button size="small" text type="primary" @click="copyValue(f.value, f.key)">{{ copied && lastCopied === f.key ? '已复制 ✓' : '复制' }}</el-button>
          </div>
          <transition name="fade">
            <p v-if="formatError" class="format-error">「{{ formatError }}」不是合法的 {{ editingLabel }} 色值，已回退</p>
          </transition>
        </div>
      </div>
    </template>

    <template #result>
      <h3 class="section-title">明暗变体</h3>
      <div class="swatch-row">
        <button
          v-for="v in shades" :key="v.hex"
          class="swatch" :style="{ background: v.hex }"
          :title="`${v.label} ${v.hex.toUpperCase()}`"
          @click="copy(v.hex.toUpperCase())"
        >
          <span class="swatch-tag" :class="{ light: v.light }">{{ v.label }}</span>
        </button>
      </div>

      <h3 class="section-title spaced">协调配色</h3>
      <div class="harmony-row">
        <div class="harmony-group">
          <p class="group-label">互补色</p>
          <button class="swatch big" :style="{ background: complementary }" :title="complementary.toUpperCase()" @click="copy(complementary.toUpperCase())">
            <span class="swatch-tag light">{{ complementary.toUpperCase() }}</span>
          </button>
        </div>
        <div class="harmony-group">
          <p class="group-label">邻近色</p>
          <div class="swatch-row">
            <button v-for="h in analogous" :key="h" class="swatch big" :style="{ background: h }" :title="h.toUpperCase()" @click="copy(h.toUpperCase())">
              <span class="swatch-tag light">{{ h.toUpperCase() }}</span>
            </button>
          </div>
        </div>
      </div>
      <p class="copy-hint">点击任意色块即可复制其 HEX 值</p>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { useCopy } from '../../composables/useCopy'

const tool = findTool('color-picker')!
const { copied, copy } = useCopy()
const lastCopied = ref('')

/* ---------- 颜色转换 ---------- */
interface RGB { r: number; g: number; b: number }
interface HSL { h: number; s: number; l: number }

function hexToRgb(hex: string): RGB | null {
  const m = hex.trim().match(/^#?([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/)
  if (!m) return null
  let s = m[1]
  if (s.length === 3) s = [...s].map(c => c + c).join('')
  return { r: parseInt(s.slice(0, 2), 16), g: parseInt(s.slice(2, 4), 16), b: parseInt(s.slice(4, 6), 16) }
}

function rgbToHex({ r, g, b }: RGB): string {
  const to = (n: number) => Math.round(Math.min(255, Math.max(0, n))).toString(16).padStart(2, '0')
  return `#${to(r)}${to(g)}${to(b)}`
}

function rgbToHsl({ r, g, b }: RGB): HSL {
  const rn = r / 255, gn = g / 255, bn = b / 255
  const max = Math.max(rn, gn, bn), min = Math.min(rn, gn, bn)
  const l = (max + min) / 2
  let h = 0, s = 0
  if (max !== min) {
    const d = max - min
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
    if (max === rn) h = ((gn - bn) / d + (gn < bn ? 6 : 0)) / 6
    else if (max === gn) h = ((bn - rn) / d + 2) / 6
    else h = ((rn - gn) / d + 4) / 6
  }
  return { h: Math.round(h * 360), s: Math.round(s * 100), l: Math.round(l * 100) }
}

function hslToRgb({ h, s, l }: HSL): RGB {
  const sn = s / 100, ln = l / 100
  const c = (1 - Math.abs(2 * ln - 1)) * sn
  const hp = ((h % 360) + 360) % 360 / 60
  const x = c * (1 - Math.abs((hp % 2) - 1))
  const [r1, g1, b1] =
    hp < 1 ? [c, x, 0] : hp < 2 ? [x, c, 0] : hp < 3 ? [0, c, x] :
    hp < 4 ? [0, x, c] : hp < 5 ? [x, 0, c] : [c, 0, x]
  const m = ln - c / 2
  return { r: (r1 + m) * 255, g: (g1 + m) * 255, b: (b1 + m) * 255 }
}

/* ---------- 状态 ---------- */
const hex = ref('#14B8A6')
const editing = ref<'hex' | 'rgb' | 'hsl'>('hex')
const formatError = ref('')
/** 非法输入回退时递增，强制输入框按绑定值重建 */
const inputEpoch = ref(0)

const rgb = computed<RGB>(() => hexToRgb(hex.value)!)
const hsl = computed<HSL>(() => rgbToHsl(rgb.value))

const formats = computed(() => [
  { key: 'hex' as const, label: 'HEX', value: hex.value.toUpperCase() },
  { key: 'rgb' as const, label: 'RGB', value: `rgb(${Math.round(rgb.value.r)}, ${Math.round(rgb.value.g)}, ${Math.round(rgb.value.b)})` },
  { key: 'hsl' as const, label: 'HSL', value: `hsl(${hsl.value.h}, ${hsl.value.s}%, ${hsl.value.l}%)` }
])

const editingLabel = computed(() => ({ hex: 'HEX', rgb: 'RGB', hsl: 'HSL' })[editing.value])

function onNativePick(e: Event) {
  formatError.value = ''
  hex.value = (e.target as HTMLInputElement).value
}

/** 手输色值提交：非法时回退上一次有效值并提示 */
function commitFormat(key: 'hex' | 'rgb' | 'hsl', raw: string) {
  editing.value = key
  const text = raw.trim()
  try {
    if (key === 'hex') {
      const v = hexToRgb(text)
      if (!v) throw new Error(text)
      hex.value = rgbToHex(v)
    } else if (key === 'rgb') {
      const m = text.match(/^rgba?\(\s*(\d{1,3})\s*[,\s]\s*(\d{1,3})\s*[,\s]\s*(\d{1,3})/)
      if (!m) throw new Error(text)
      const v = { r: +m[1], g: +m[2], b: +m[3] }
      if ([v.r, v.g, v.b].some(n => n > 255)) throw new Error(text)
      hex.value = rgbToHex(v)
    } else {
      const m = text.match(/^hsla?\(\s*(\d{1,3})\s*[,\s]\s*(\d{1,3})%\s*[,\s]\s*(\d{1,3})%/)
      if (!m) throw new Error(text)
      hex.value = rgbToHex(hslToRgb({ h: +m[1], s: +m[2], l: +m[3] }))
    }
    formatError.value = ''
  } catch {
    formatError.value = text || '（空）'
    inputEpoch.value++
  }
}

/* ---------- 变体与配色 ---------- */
const shades = computed(() => {
  const { h, s } = hsl.value
  return [95, 85, 70, 50, 35, 25, 15].map(l => {
    const v = rgbToHex(hslToRgb({ h, s, l }))
    return { hex: v, label: l === 50 ? '原色' : `L ${l}%`, light: l <= 45 }
  })
})

const complementary = computed(() => rgbToHex(hslToRgb({ ...hsl.value, h: hsl.value.h + 180 })))

const analogous = computed(() =>
  [-30, 30].map(d => rgbToHex(hslToRgb({ ...hsl.value, h: hsl.value.h + d })))
)

async function copyValue(text: string, key: string) {
  lastCopied.value = key
  await copy(text)
}
</script>

<style scoped>
.picker-panel {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: var(--space-6);
  align-items: start;
}

.swatch-col {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  align-items: center;
}

.native-picker {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.native-picker input[type='color'] {
  width: 96px;
  height: 96px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface);
  cursor: pointer;
  padding: 4px;
}

.native-hint {
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

.current-swatch {
  width: 100%;
  padding: var(--space-2) 0;
  border-radius: var(--radius-pill);
  text-align: center;
  font-size: var(--text-sm);
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  border: 1px solid var(--color-border);
}

.format-col {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.format-row {
  display: grid;
  grid-template-columns: 48px 1fr auto;
  align-items: center;
  gap: var(--space-3);
}

.format-label {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--color-text-muted);
}

.format-input {
  height: 36px;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  color: var(--color-text-strong);
  outline: none;
}

.format-input:focus {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--brand-500) 15%, transparent);
}

.format-error {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--el-color-danger);
}

/* ---------- 结果区 ---------- */
.section-title {
  margin: 0 0 var(--space-3);
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--color-text-strong);
}

.section-title.spaced {
  margin-top: var(--space-6);
}

.swatch-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.swatch {
  position: relative;
  width: 72px;
  height: 56px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: transform 0.15s ease;
}

.swatch.big {
  width: 120px;
  height: 64px;
}

.swatch:hover {
  transform: translateY(-2px);
}

.swatch-tag {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 4px;
  text-align: center;
  font-size: 10px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.4);
}

.swatch-tag.light {
  color: rgba(0, 0, 0, 0.65);
  text-shadow: none;
}

.harmony-row {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.harmony-group .group-label {
  margin: 0 0 6px;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.copy-hint {
  margin: var(--space-5) 0 0;
  text-align: center;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .picker-panel {
    grid-template-columns: 1fr;
  }
  .swatch-col {
    flex-direction: row;
    justify-content: center;
  }
  .current-swatch {
    width: auto;
    flex: 1;
  }
}
</style>
