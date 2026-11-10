<template>
  <div class="wb-root" aria-hidden="true">
    <div
      v-for="g in GRADIENTS"
      :key="g"
      class="wb-gradient"
      :class="['wb-g-' + g, { 'is-active': g === activeGradient }]"
    ></div>
    <div class="wb-glow wb-glow-sun" :class="{ 'is-active': scene === 'sunny' }"></div>
    <div class="wb-glow wb-glow-moon" :class="{ 'is-active': scene === 'night' }"></div>
    <canvas ref="canvasRef" class="wb-canvas"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useWeather } from '../../composables/useWeather'

const { scene, isDay } = useWeather()

const GRADIENTS = ['sunny', 'cloudy', 'rainy', 'rainy-night', 'snowy', 'night'] as const
type GradientKey = (typeof GRADIENTS)[number]

/** 雨夜（自动模式下的夜间降雨）使用暗色渐变 */
const activeGradient = computed<GradientKey>(() =>
  scene.value === 'rainy' && !isDay.value ? 'rainy-night' : scene.value
)

const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
let rafId = 0
let w = 0
let h = 0
let resizeTimer = 0
let particles: (() => void)[] = []
const reducedMotion =
  typeof window !== 'undefined' && window.matchMedia
    ? window.matchMedia('(prefers-reduced-motion: reduce)')
    : null

/* ---------- 粒子构造器 ---------- */
function rand(min: number, max: number) {
  return min + Math.random() * (max - min)
}

function buildParticles(target: GradientKey) {
  particles = []
  if (!ctx) return
  if (target === 'rainy' || target === 'rainy-night') {
    // 斜向雨线
    for (let i = 0; i < 140; i++) {
      const len = rand(10, 22)
      const speed = rand(9, 16)
      const opacity = rand(0.15, 0.45)
      let x = rand(0, w)
      let y = rand(-h, h)
      particles.push(() => {
        y += speed
        x += speed * 0.15
        if (y > h + len) {
          y = -len - rand(0, 40)
          x = rand(-20, w)
        }
        ctx!.strokeStyle = `rgba(190, 210, 232, ${opacity})`
        ctx!.lineWidth = 1
        ctx!.beginPath()
        ctx!.moveTo(x, y)
        ctx!.lineTo(x - len * 0.15, y - len)
        ctx!.stroke()
      })
    }
  } else if (target === 'snowy') {
    // 正弦摇摆雪花
    for (let i = 0; i < 120; i++) {
      const r = rand(1, 3.2)
      const speed = rand(0.5, 1.6)
      const phaseSpeed = rand(0.008, 0.025)
      const opacity = rand(0.5, 0.95)
      let x = rand(0, w)
      let y = rand(-h, h)
      let phase = rand(0, Math.PI * 2)
      particles.push(() => {
        y += speed
        phase += phaseSpeed
        x += Math.sin(phase) * 0.4
        if (y > h + r) {
          y = -r - rand(0, 30)
          x = rand(0, w)
        }
        ctx!.fillStyle = `rgba(255, 255, 255, ${opacity})`
        ctx!.beginPath()
        ctx!.arc(x, y, r, 0, Math.PI * 2)
        ctx!.fill()
      })
    }
  } else if (target === 'night') {
    // 闪烁星星（分布在上部 75% 区域）
    for (let i = 0; i < 90; i++) {
      const r = rand(0.5, 1.6)
      const baseOp = rand(0.3, 1)
      const twinkleSpeed = rand(0.008, 0.03)
      const x = rand(0, w)
      const y = rand(0, h * 0.75)
      let phase = rand(0, Math.PI * 2)
      particles.push(() => {
        phase += twinkleSpeed
        const opacity = baseOp * (0.55 + 0.45 * Math.sin(phase))
        ctx!.fillStyle = `rgba(235, 242, 255, ${opacity})`
        ctx!.beginPath()
        ctx!.arc(x, y, r, 0, Math.PI * 2)
        ctx!.fill()
      })
    }
  } else if (target === 'cloudy') {
    // 慢速漂移云团（径向渐变椭圆）
    for (let i = 0; i < 6; i++) {
      const rx = rand(120, 260)
      const ry = rand(40, 80)
      const speed = rand(0.15, 0.45)
      const opacity = rand(0.12, 0.28)
      const y = rand(h * 0.08, h * 0.6)
      let x = rand(-rx, w)
      particles.push(() => {
        x += speed
        if (x - rx > w) x = -rx
        const grad = ctx!.createRadialGradient(x, y, 0, x, y, rx)
        grad.addColorStop(0, `rgba(255, 255, 255, ${opacity})`)
        grad.addColorStop(1, 'rgba(255, 255, 255, 0)')
        ctx!.save()
        ctx!.translate(x, y)
        ctx!.scale(1, ry / rx)
        ctx!.translate(-x, -y)
        ctx!.fillStyle = grad
        ctx!.beginPath()
        ctx!.arc(x, y, rx, 0, Math.PI * 2)
        ctx!.fill()
        ctx!.restore()
      })
    }
  } else if (target === 'sunny') {
    // 漂浮光尘（缓慢上升 + 微摆）
    for (let i = 0; i < 40; i++) {
      const r = rand(1, 2.5)
      const speed = rand(0.1, 0.35)
      const opacity = rand(0.15, 0.4)
      const phaseSpeed = rand(0.005, 0.02)
      let x = rand(0, w)
      let y = rand(0, h)
      let phase = rand(0, Math.PI * 2)
      particles.push(() => {
        y -= speed
        phase += phaseSpeed
        x += Math.sin(phase) * 0.3
        if (y < -r) {
          y = h + r
          x = rand(0, w)
        }
        ctx!.fillStyle = `rgba(255, 224, 170, ${opacity})`
        ctx!.beginPath()
        ctx!.arc(x, y, r, 0, Math.PI * 2)
        ctx!.fill()
      })
    }
  }
}

/* ---------- 画布尺寸 ---------- */
function resizeCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  w = window.innerWidth
  h = window.innerHeight
  canvas.width = Math.round(w * dpr)
  canvas.height = Math.round(h * dpr)
  canvas.style.width = w + 'px'
  canvas.style.height = h + 'px'
  ctx = canvas.getContext('2d')
  if (ctx) ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  buildParticles(activeGradient.value)
}

function onResize() {
  window.clearTimeout(resizeTimer)
  resizeTimer = window.setTimeout(resizeCanvas, 200)
}

/* ---------- rAF 循环 ---------- */
function tick() {
  if (!ctx) return
  ctx.clearRect(0, 0, w, h)
  for (const draw of particles) draw()
  rafId = requestAnimationFrame(tick)
}

function startLoop() {
  if (reducedMotion?.matches) return
  if (!rafId) rafId = requestAnimationFrame(tick)
}

function stopLoop() {
  if (rafId) {
    cancelAnimationFrame(rafId)
    rafId = 0
  }
}

function onVisibility() {
  if (document.hidden) {
    stopLoop()
  } else {
    startLoop()
  }
}

function onMotionChange() {
  if (reducedMotion?.matches) {
    stopLoop()
  } else {
    startLoop()
  }
}

watch(activeGradient, () => {
  buildParticles(activeGradient.value)
})

onMounted(() => {
  resizeCanvas()
  startLoop()
  window.addEventListener('resize', onResize)
  document.addEventListener('visibilitychange', onVisibility)
  reducedMotion?.addEventListener('change', onMotionChange)
})

onBeforeUnmount(() => {
  stopLoop()
  window.clearTimeout(resizeTimer)
  window.removeEventListener('resize', onResize)
  document.removeEventListener('visibilitychange', onVisibility)
  reducedMotion?.removeEventListener('change', onMotionChange)
})
</script>

<style scoped>
.wb-root {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

/* ---------- 渐变底色层（crossfade 1.2s） ---------- */
.wb-gradient {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 1.2s ease;
}

.wb-gradient.is-active {
  opacity: 1;
}

/* 场景配色为功能性天空色（非品牌色） */
.wb-g-sunny {
  background: linear-gradient(180deg, #aedcf0 0%, #dff1fa 48%, #fdf2df 100%);
}
.wb-g-cloudy {
  background: linear-gradient(180deg, #b6c3d1 0%, #d5dde5 52%, #edf0f4 100%);
}
.wb-g-rainy {
  background: linear-gradient(180deg, #8fa3b5 0%, #a9bbc9 50%, #ccd6df 100%);
}
.wb-g-rainy-night {
  background: linear-gradient(180deg, #2c3949 0%, #46586b 52%, #66798c 100%);
}
.wb-g-snowy {
  background: linear-gradient(180deg, #cfdcea 0%, #e7eff7 52%, #f9fcff 100%);
}
.wb-g-night {
  background: linear-gradient(180deg, #0f1a33 0%, #1b2a50 55%, #2e4270 100%);
}

/* ---------- 光晕层（晴天太阳 / 夜晚月亮） ---------- */
.wb-glow {
  position: absolute;
  top: -12%;
  right: 6%;
  width: 44vmin;
  height: 44vmin;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 1.2s ease;
  animation: wb-breathe 7s ease-in-out infinite;
}

.wb-glow.is-active {
  opacity: 1;
}

.wb-glow-sun {
  background: radial-gradient(
    circle,
    rgba(255, 236, 190, 0.85) 0%,
    rgba(255, 224, 160, 0.4) 38%,
    rgba(255, 215, 140, 0) 70%
  );
}

.wb-glow-moon {
  top: 6%;
  right: 10%;
  width: 34vmin;
  height: 34vmin;
  background: radial-gradient(
    circle,
    rgba(235, 242, 255, 0.7) 0%,
    rgba(200, 216, 245, 0.28) 40%,
    rgba(200, 216, 245, 0) 72%
  );
}

@keyframes wb-breathe {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.06);
  }
}

/* ---------- 粒子层 ---------- */
.wb-canvas {
  position: absolute;
  inset: 0;
}

/* ---------- 减少动效：静态渐变 ---------- */
@media (prefers-reduced-motion: reduce) {
  .wb-glow {
    animation: none;
  }
  .wb-gradient {
    transition: none;
  }
}
</style>
