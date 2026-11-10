<template>
  <ToolPageLayout :title="tool.title" :intro="tool.intro" :usage="tool.usage" :icon="tool.icon" :result-ready="resultReady">
    <template #panel>
      <div class="codec-toolbar">
        <div class="codec-actions">
          <el-button size="small" type="primary" :disabled="!input" @click="run('escape')">转义 →</el-button>
          <el-button size="small" :disabled="!input" @click="run('unescape')">← 还原</el-button>
        </div>
        <el-button size="small" text @click="swap" :disabled="!output">↑↓ 结果转输入</el-button>
      </div>
      <textarea
        v-model="input"
        class="codec-input"
        placeholder='输入含 < > & " 的文本进行转义，或粘贴实体序列（如 &lt;div&gt;）进行还原'
        spellcheck="false"
      ></textarea>
    </template>

    <template #result>
      <div v-if="output" class="codec-output-wrap">
        <div class="output-head">
          <span class="output-flag">{{ lastOp === 'escape' ? '转义结果' : '还原结果' }}</span>
          <el-button size="small" text type="primary" @click="copy(output)">{{ copied ? '已复制 ✓' : '复制' }}</el-button>
        </div>
        <pre class="codec-output">{{ output }}</pre>
      </div>
    </template>
  </ToolPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import ToolPageLayout from '../../components/tools/ToolPageLayout.vue'
import { findTool } from './toolMeta'
import { useCopy } from '../../composables/useCopy'
import { htmlEscape, htmlUnescape } from '../../utils/codec'

const tool = findTool('html-entity')!
const { copied, copy } = useCopy()

const input = ref('')
const output = ref('')
const lastOp = ref<'escape' | 'unescape'>('escape')

const resultReady = computed(() => output.value !== '')

function run(op: 'escape' | 'unescape') {
  lastOp.value = op
  output.value = op === 'escape' ? htmlEscape(input.value) : htmlUnescape(input.value)
}

function swap() {
  input.value = output.value
  output.value = ''
}
</script>

<style scoped>
.codec-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.codec-actions {
  display: flex;
  gap: var(--space-2);
}

.codec-input {
  width: 100%;
  min-height: 180px;
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  line-height: 1.6;
  color: var(--color-text-strong);
  resize: vertical;
  outline: none;
}

.codec-input:focus {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--brand-500) 15%, transparent);
}

.output-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.output-flag {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--brand-600);
}

.codec-output {
  margin: 0;
  max-height: 320px;
  overflow: auto;
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--surface-muted);
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: var(--text-sm);
  line-height: 1.65;
  color: var(--color-text-strong);
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
