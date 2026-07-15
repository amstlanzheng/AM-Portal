<template>
  <div class="ai-chat">
    <div class="chat-toggle" @click="visible = !visible">
      <el-icon :size="24"><ChatDotRound /></el-icon>
    </div>
    <el-drawer v-model="visible" title="AI 助手" direction="rtl" size="400px">
      <div class="chat-container">
        <div class="messages" ref="messagesRef">
          <div v-for="msg in messages" :key="msg.id" :class="['message', msg.role]">
            <div class="message-content">{{ msg.content }}</div>
          </div>
          <div v-if="loading" class="message assistant">
            <div class="message-content typing">思考中...</div>
          </div>
        </div>
        <div class="chat-input">
          <el-input v-model="input" placeholder="输入消息..." @keyup.enter="sendMessage" :disabled="loading">
            <template #append>
              <el-button @click="sendMessage" :loading="loading">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { chatWithAi } from '../api'

const visible = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref<{ id: number; role: string; content: string }[]>([])
const messagesRef = ref<HTMLElement>()
let msgId = 0

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
.chat-toggle { position: fixed; bottom: 30px; right: 30px; width: 56px; height: 56px; border-radius: 50%; background: #409eff; color: #fff; display: flex; align-items: center; justify-content: center; cursor: pointer; box-shadow: 0 4px 12px rgba(64,158,255,0.4); z-index: 999; }
.chat-toggle:hover { transform: scale(1.1); }
.chat-container { display: flex; flex-direction: column; height: calc(100vh - 80px); }
.messages { flex: 1; overflow-y: auto; padding: 10px; }
.message { margin-bottom: 12px; }
.message.user { text-align: right; }
.message-content { display: inline-block; max-width: 80%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.6; white-space: pre-wrap; }
.message.user .message-content { background: #409eff; color: #fff; border-bottom-right-radius: 4px; }
.message.assistant .message-content { background: #f4f4f5; color: #333; border-bottom-left-radius: 4px; }
.typing { animation: blink 1s infinite; }
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }
.chat-input { padding: 10px; border-top: 1px solid #eee; }
</style>
