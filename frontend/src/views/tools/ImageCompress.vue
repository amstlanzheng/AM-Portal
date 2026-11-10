<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="items.length > 0">
    <template #panel>
      <div
        class="drop-zone" :class="{ dragging }"
        @click="fileInput?.click()"
        @dragover.prevent="dragging = true"
        @dragleave.prevent="dragging = false"
        @drop.prevent="onDrop"
      >
        <el-icon :size="32"><Plus /></el-icon>
        <p class="drop-text">点击选择或拖拽图片到此处</p>
        <p class="drop-hint">支持 JPG / PNG / WebP，可多选，全程本地处理</p>
        <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/webp" multiple hidden @change="onPick" />
      </div>

      <div v-if="items.length" class="compress-controls">
        <div class="quality-row">
          <span class="quality-label">压缩质量</span>
          <el-slider v-model="quality" :min="1" :max="100" :step="1" style="flex: 1" />
          <span class="quality-value">{{ quality }}</span>
        </div>
        <div class="actions-row">
          <el-button type="primary" :disabled="busy || !pendingCount" :loading="busy" @click="compressAll">
            {{ busy ? `压缩中 ${doneCount}/${items.length}…` : `开始压缩（${pendingCount} 张待处理）` }}
          </el-button>
          <el-button @click="clearAll">清空</el-button>
        </div>
      </div>

      <p v-if="oversizeHint" class="size-hint">⚠ {{ oversizeHint }}</p>
      <p v-if="rejectHint" class="size-hint reject">✕ {{ rejectHint }}</p>
    </template>

    <template #result>
      <div class="img-list">
        <div v-for="(item, i) in items" :key="i" class="img-card">
          <div class="img-thumb">
            <img :src="item.compressedUrl || item.url" :alt="item.name" />
            <span v-if="item.status === 'done'" class="ratio-badge" :class="{ worse: item.ratio! >= 0 }">
              {{ item.ratio! < 0 ? `↓ ${Math.abs(Math.round(item.ratio! * 100))}%` : `↑ ${Math.round(item.ratio! * 100)}%` }}
            </span>
            <span v-else-if="item.status === 'processing'" class="ratio-badge working">处理中…</span>
          </div>
          <div class="img-info">
            <p class="img-name" :title="item.name">{{ item.name }}</p>
            <p class="img-meta">
              <span>{{ formatSize(item.size) }}</span>
              <template v-if="item.status === 'done'">
                <span class="arrow">→</span>
                <span class="after">{{ formatSize(item.compressedSize!) }}</span>
              </template>
            </p>
            <div class="img-actions">
              <el-button v-if="item.status === 'done'" size="small" type="primary" @click="download(item)">下载</el-button>
              <el-button size="small" @click="removeItem(i)">移除</el-button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'

const tool = findTool('image-compress')!

interface Item {
  name: string
  size: number
  type: string
  url: string
  file: File
  status: 'pending' | 'processing' | 'done'
  blob?: Blob
  compressedSize?: number
  compressedUrl?: string
  ratio?: number
}

const fileInput = ref<HTMLInputElement>()
const dragging = ref(false)
const quality = ref(75)
const items = ref<Item[]>([])
const busy = ref(false)
const rejectHint = ref('')
const oversizeHint = ref('')

const pendingCount = computed(() => items.value.filter(i => i.status === 'pending').length)
const doneCount = computed(() => items.value.filter(i => i.status === 'done').length)

const ALLOWED = ['image/jpeg', 'image/png', 'image/webp']

function addFiles(files: FileList | File[]) {
  rejectHint.value = ''
  const rejected: string[] = []
  const oversize: string[] = []
  for (const f of Array.from(files)) {
    if (!ALLOWED.includes(f.type)) { rejected.push(f.name); continue }
    if (f.size > 20 * 1024 * 1024) oversize.push(f.name)
    items.value.push({
      name: f.name, size: f.size, type: f.type, file: f,
      url: URL.createObjectURL(f), status: 'pending'
    })
  }
  if (rejected.length) rejectHint.value = `已忽略非图片文件：${rejected.join('、')}（仅支持 JPG/PNG/WebP）`
  oversizeHint.value = oversize.length ? `以下图片超过 20MB，处理可能较慢：${oversize.join('、')}` : ''
}

function onPick(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files) addFiles(input.files)
  input.value = ''
}

function onDrop(e: DragEvent) {
  dragging.value = false
  if (e.dataTransfer?.files) addFiles(e.dataTransfer.files)
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

/** Canvas 重绘导出：PNG 无损格式质量参数不生效，压缩率可能为 0 或负（重新编码） */
function compressOne(item: Item): Promise<void> {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      canvas.width = img.naturalWidth
      canvas.height = img.naturalHeight
      canvas.getContext('2d')!.drawImage(img, 0, 0)
      canvas.toBlob((blob) => {
        if (blob) {
          item.blob = blob
          item.compressedSize = blob.size
          item.compressedUrl = URL.createObjectURL(blob)
          item.ratio = (blob.size - item.size) / item.size
        }
        item.status = 'done'
        URL.revokeObjectURL(img.src)
        resolve()
      }, item.type, quality.value / 100)
    }
    img.onerror = () => { item.status = 'done'; resolve() }
    img.src = item.url
  })
}

async function compressAll() {
  busy.value = true
  for (const item of items.value) {
    if (item.status !== 'pending') continue
    item.status = 'processing'
    await compressOne(item)
  }
  busy.value = false
}

function download(item: Item) {
  if (!item.blob) return
  const a = document.createElement('a')
  a.href = item.compressedUrl!
  a.download = item.name.replace(/(\.\w+)?$/, m => m ? `.min${m}` : '.min')
  a.click()
}

function removeItem(i: number) {
  const item = items.value[i]
  URL.revokeObjectURL(item.url)
  if (item.compressedUrl) URL.revokeObjectURL(item.compressedUrl)
  items.value.splice(i, 1)
}

function clearAll() {
  items.value.forEach(i => {
    URL.revokeObjectURL(i.url)
    if (i.compressedUrl) URL.revokeObjectURL(i.compressedUrl)
  })
  items.value = []
}
</script>

<style scoped>
.drop-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: var(--space-8) var(--space-4);
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.drop-zone:hover,
.drop-zone.dragging {
  border-color: var(--brand-500);
  background: var(--brand-50);
}

.drop-text {
  margin: 0;
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text);
}

.drop-hint {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

.compress-controls {
  margin-top: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.quality-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.quality-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
  flex-shrink: 0;
}

.quality-value {
  min-width: 32px;
  text-align: right;
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--brand-600);
  font-variant-numeric: tabular-nums;
}

.actions-row {
  display: flex;
  gap: var(--space-3);
}

.size-hint {
  margin: var(--space-2) 0 0;
  font-size: var(--text-xs);
  color: var(--el-color-warning);
}

.size-hint.reject {
  color: var(--el-color-danger);
}

/* ---------- 结果 ---------- */
.img-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--space-4);
}

.img-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--surface);
}

.img-thumb {
  position: relative;
  height: 150px;
  background: var(--surface-muted);
  display: grid;
  place-items: center;
}

.img-thumb img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.ratio-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 10px;
  border-radius: var(--radius-pill);
  font-size: var(--text-xs);
  font-weight: 700;
  color: #fff;
  background: var(--el-color-success);
}

.ratio-badge.worse {
  background: var(--el-color-warning);
}

.ratio-badge.working {
  background: var(--brand-600);
}

.img-info {
  padding: var(--space-3) var(--space-4) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.img-name {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-strong);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.img-meta {
  margin: 0;
  display: flex;
  gap: 6px;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  font-variant-numeric: tabular-nums;
}

.img-meta .after {
  color: var(--brand-600);
  font-weight: 600;
}

.img-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: 4px;
}
</style>
