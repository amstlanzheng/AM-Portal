import type { Directive } from 'vue'

type RevealValue = number | { delay?: number; distance?: number }

let observer: IntersectionObserver | null = null

function getObserver() {
  if (observer || typeof IntersectionObserver === 'undefined') return observer
  observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
          observer?.unobserve(entry.target)
        }
      }
    },
    { threshold: 0.12, rootMargin: '0px 0px -40px 0px' }
  )
  return observer
}

/**
 * v-reveal / v-reveal="120" / v-reveal="{ delay: 120, distance: 24 }"
 * 元素进入视口时淡入上移，只触发一次。
 */
export const reveal: Directive<HTMLElement, RevealValue> = {
  mounted(el, binding) {
    const value = binding.value
    const delay = typeof value === 'number' ? value : value?.delay ?? 0
    const distance = typeof value === 'object' ? value?.distance ?? 16 : 16

    el.classList.add('reveal')
    el.style.setProperty('--reveal-distance', `${distance}px`)
    if (delay) el.style.transitionDelay = `${delay}ms`

    const io = getObserver()
    if (!io) {
      el.classList.add('is-visible')
      return
    }
    io.observe(el)
  },
  unmounted(el) {
    observer?.unobserve(el)
  }
}
