import { ref, onUnmounted } from 'vue'

/**
 * 统一复制反馈：复制成功后 copied 变 true，2 秒自动还原
 * 按钮文字据此显示「已复制 ✓」
 */
export function useCopy() {
  const copied = ref(false)
  let timer: number | undefined

  async function copy(text: string): Promise<boolean> {
    let ok = false
    try {
      await navigator.clipboard.writeText(text)
      ok = true
    } catch {
      // 旧环境回退
      try {
        const ta = document.createElement('textarea')
        ta.value = text
        ta.style.position = 'fixed'
        ta.style.opacity = '0'
        document.body.appendChild(ta)
        ta.select()
        ok = document.execCommand('copy')
        document.body.removeChild(ta)
      } catch { ok = false }
    }
    if (ok) {
      copied.value = true
      if (timer) clearTimeout(timer)
      timer = window.setTimeout(() => { copied.value = false }, 2000)
    }
    return ok
  }

  onUnmounted(() => { if (timer) clearTimeout(timer) })

  return { copied, copy }
}
