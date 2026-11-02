<template>
  <div class="ai-chat">
    <button class="chat-toggle" :class="{ open: visible }" @click="toggleChat" aria-label="AI 助手">
      <span class="chat-pulse"></span>
      <span class="chat-pulse delay"></span>
      <el-icon :size="24"><ChatDotRound /></el-icon>
    </button>

    <transition name="bubble">
      <div v-if="hintVisible" class="chat-hint" @click="toggleChat">
        <span class="hint-dot"></span>有问必答，点我聊聊
      </div>
    </transition>

    <el-drawer v-model="visible" :size="drawerSize" direction="rtl" :with-header="true" class="ai-drawer">
      <template #header>
        <div class="drawer-head">
          <span class="drawer-avatar">AI</span>
          <div>
            <p class="drawer-title">AI 助手</p>
            <p class="drawer-sub">有什么可以帮你？</p>
          </div>
        </div>
      </template>

      <div class="chat-container">
        <div class="messages" ref="messagesRef">
          <div v-if="!messages.length" class="chat-empty">
            <p class="chat-empty-title">开始对话</p>
            <p class="faint">可以问我关于这个站点、项目或博客内容的问题</p>
          </div>

          <div v-for="msg in messages" :key="msg.id" :class="['message', msg.role]">
            <div class="message-content">{{ msg.content }}</div>
          </div>
          <div v-if="loading" class="message assistant">
            <div class="message-content typing">思考中...</div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="input"
            placeholder="输入消息，回车发送..."
            @keyup.enter="sendMessage"
            :disabled="loading"
            size="large"
          >
            <template #append>
              <el-button @click="sendMessage" :loading="loading" :disabled="!input.trim()">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { chatWithAi } from '../api'

const visible = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref<{ id: number; role: string; content: string }[]>([])
const messagesRef = ref<HTMLElement>()
let msgId = 0

const drawerSize = computed(() => (window.innerWidth < 640 ? '100%' : '420px'))

/* 首次进入的小气泡提示 */
const hintVisible = ref(false)
let hintTimer: number | undefined

function toggleChat() {
  visible.value = !visible.value
  hintVisible.value = false
  try { sessionStorage.setItem('ai-hint-seen', '1') } catch { /* ignore */ }
}

onMounted(() => {
  let seen = ''
  try { seen = sessionStorage.getItem('ai-hint-seen') || '' } catch { /* ignore */ }
  if (!seen) {
    hintTimer = window.setTimeout(() => (hintVisible.value = true), 3500)
  }
})

onBeforeUnmount(() => {
  if (hintTimer) window.clearTimeout(hintTimer)
})

async function sendMessage() {
  if (!input.value.trim() || loading.value) return
  const userMsg = input.value.trim()
  input.value = ''
  messages.value.push({ id: ++msgId, role: 'user', content: userMsg })
  loading.value = true
  await nextTick()
  scrollToBottom()

  try {
    const response = await chatWithAi({ message: userMsg })
    console.log('[AI Chat] 响应状态:', response.status, response.headers.get('content-type'))

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(errorText || `HTTP ${response.status}`)
    }

    const reader = response.body?.getReader()
    if (!reader) {
      throw new Error('No readable stream')
    }

    const decoder = new TextDecoder()
    let assistantContent = ''
    const assistantMsgId = ++msgId
    messages.value.push({ id: assistantMsgId, role: 'assistant', content: '' })
    // 获取响应式代理对象的索引
    const assistantMsgIndex = messages.value.length - 1
    loading.value = false

    let buffer = ''
    let chunkIndex = 0
    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        console.log('[AI Chat] 流读取结束')
        break
      }

      const raw = decoder.decode(value, { stream: true })
      console.log(`[AI Chat] 收到 chunk #${chunkIndex++}:`, JSON.stringify(raw))
      buffer += raw

      // SSE format: each event is "data:content\n\n"
      // Split by double newline to get complete events
      const events = buffer.split('\n\n')
      // Keep the last incomplete chunk in buffer
      buffer = events.pop() || ''

      for (const event of events) {
        const lines = event.split('\n')
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.substring(5)
            if (data.trim() === '[DONE]') continue
            assistantContent += data
          }
        }
      }

      // 通过数组索引修改响应式代理对象，触发 Vue 更新
      messages.value[assistantMsgIndex].content = assistantContent
      await nextTick()
      scrollToBottom()
    }

    // Process any remaining buffer
    if (buffer.trim()) {
      const lines = buffer.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.substring(5)
          if (data.trim() === '[DONE]') continue
          assistantContent += data
        }
      }
      messages.value[assistantMsgIndex].content = assistantContent
    }

    if (!assistantContent.trim()) {
      messages.value[assistantMsgIndex].content = '暂无回复'
    }
  } catch (e: any) {
    messages.value.push({ id: ++msgId, role: 'assistant', content: '抱歉，发生了错误，请稍后重试。' })
  } finally {
    loading.value = false
  }
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}
</script>

<style scoped>
/* 悬浮按钮 */
.chat-toggle {
  position: fixed;
  right: 28px;
  bottom: 28px;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  box-shadow: 0 14px 30px -12px rgba(37, 99, 235, 0.7);
  cursor: pointer;
  z-index: 999;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}
.chat-toggle:hover {
  transform: translateY(-2px) scale(1.04);
  box-shadow: 0 18px 34px -12px rgba(37, 99, 235, 0.8);
}
.chat-toggle.open {
  transform: scale(0.94);
}

/* 旋转光环 */
.chat-toggle::before {
  content: "";
  position: absolute;
  inset: -5px;
  border-radius: 50%;
  background: conic-gradient(from 0deg, transparent 0 58%, rgba(37, 99, 235, 0.6) 76%, transparent 88%);
  animation: chat-spin 3.6s linear infinite;
  z-index: -1;
  pointer-events: none;
}

@keyframes chat-spin {
  to { transform: rotate(360deg); }
}

/* 呼吸光晕 */
.chat-pulse {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2px solid rgba(37, 99, 235, 0.45);
  animation: chat-pulse 2.8s ease-out infinite;
  pointer-events: none;
}
.chat-pulse.delay {
  animation-delay: 1.4s;
}

@keyframes chat-pulse {
  0% {
    transform: scale(1);
    opacity: 0.7;
  }
  70% {
    transform: scale(1.55);
    opacity: 0;
  }
  100% {
    transform: scale(1.55);
    opacity: 0;
  }
}

/* 气泡提示 */
.chat-hint {
  position: fixed;
  right: 28px;
  bottom: 96px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 16px;
  border-radius: var(--radius-pill);
  background: var(--surface);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-md);
  font-size: var(--text-sm);
  color: var(--color-text);
  cursor: pointer;
  z-index: 999;
  transition: all 0.2s ease;
}
.chat-hint:hover {
  border-color: var(--brand-100);
  color: var(--brand-600);
}

.hint-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--brand-500);
  box-shadow: 0 0 0 0 rgba(37, 99, 235, 0.5);
  animation: hint-dot 1.8s ease-out infinite;
}

@keyframes hint-dot {
  0% { box-shadow: 0 0 0 0 rgba(37, 99, 235, 0.45); }
  100% { box-shadow: 0 0 0 8px rgba(37, 99, 235, 0); }
}

.bubble-enter-active,
.bubble-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.bubble-enter-from,
.bubble-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.96);
}

/* 抽屉头 */
.drawer-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.drawer-avatar {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 11px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
}

.drawer-title {
  font-size: var(--text-base);
  font-weight: 650;
  color: var(--color-text-strong);
  line-height: 1.3;
}

.drawer-sub {
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

/* 会话区 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4) var(--space-2) var(--space-4) 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.chat-empty {
  margin: auto;
  text-align: center;
  padding: var(--space-8) 0;
}
.chat-empty-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text-strong);
  margin-bottom: 4px;
}

.message {
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message-content {
  max-width: 84%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: var(--text-sm);
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.message.user .message-content {
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  border-bottom-right-radius: 6px;
  box-shadow: 0 10px 20px -14px rgba(37, 99, 235, 0.9);
}

.message.assistant .message-content {
  color: var(--color-text);
  background: var(--surface-muted);
  border: 1px solid var(--color-border);
  border-bottom-left-radius: 6px;
}

.typing {
  color: var(--color-text-faint);
  animation: blink 1.2s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.45; }
}

.chat-input {
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);
}

@media (max-width: 640px) {
  .chat-toggle {
    right: 18px;
    bottom: 18px;
    width: 52px;
    height: 52px;
  }
}
</style>
