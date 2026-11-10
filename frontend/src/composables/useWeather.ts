import { ref, computed } from 'vue'

export type WeatherScene = 'sunny' | 'cloudy' | 'rainy' | 'snowy' | 'night'
export type WeatherMode = 'auto' | 'manual'

const MANUAL_KEY = 'wb-manual-scene'
const DEFAULT_COORDS = { latitude: 39.9042, longitude: 116.4074 }
const REFRESH_MS = 30 * 60 * 1000
const LOCATE_TIMEOUT = 5000

export const SCENE_DESC: Record<WeatherScene, string> = {
  sunny: '晴',
  cloudy: '多云',
  rainy: '雨',
  snowy: '雪',
  night: '晴夜'
}

/** WMO weather_code → 场景（0 晴、1-3/45/48 云、51-67/80-82/95-99 雨、71-77/85-86 雪） */
function codeToScene(code: number, day: boolean): WeatherScene {
  if (code === 0) return day ? 'sunny' : 'night'
  if (code >= 1 && code <= 3) return 'cloudy'
  if (code === 45 || code === 48) return 'cloudy'
  if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82) || (code >= 95 && code <= 99)) return 'rainy'
  if ((code >= 71 && code <= 77) || code === 85 || code === 86) return 'snowy'
  return 'cloudy'
}

/* ---------- 模块级单例状态 ---------- */
const scene = ref<WeatherScene>('sunny')
const isDay = ref(true)
const temperature = ref<number | null>(null)
const desc = ref('')
const mode = ref<WeatherMode>('auto')
const error = ref(false)
const loading = ref(false)

let initialized = false
let lastFetchAt = 0
let refreshTimer: number | null = null

function applyWeather(code: number, day: boolean, temp: number) {
  isDay.value = day
  temperature.value = Math.round(temp)
  scene.value = codeToScene(code, day)
  desc.value = SCENE_DESC[scene.value]
  error.value = false
  lastFetchAt = Date.now()
}

/** geolocation 优先，拒绝/失败/超时（5s）静默回退默认坐标 */
function getCoords(): Promise<{ latitude: number; longitude: number }> {
  return new Promise((resolve) => {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      resolve(DEFAULT_COORDS)
      return
    }
    let settled = false
    const finish = (coords: { latitude: number; longitude: number }) => {
      if (settled) return
      settled = true
      window.clearTimeout(timer)
      resolve(coords)
    }
    const timer = window.setTimeout(() => finish(DEFAULT_COORDS), LOCATE_TIMEOUT)
    navigator.geolocation.getCurrentPosition(
      (pos) => finish({ latitude: pos.coords.latitude, longitude: pos.coords.longitude }),
      () => finish(DEFAULT_COORDS),
      { timeout: LOCATE_TIMEOUT, maximumAge: 10 * 60 * 1000 }
    )
  })
}

async function fetchWeather() {
  if (mode.value === 'manual') return
  loading.value = true
  try {
    const coords = await getCoords()
    const url =
      `https://api.open-meteo.com/v1/forecast?latitude=${coords.latitude}&longitude=${coords.longitude}` +
      `&current=temperature_2m,weather_code,is_day&timezone=auto`
    const res = await fetch(url)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    const cur = data?.current
    if (!cur || typeof cur.weather_code !== 'number' || typeof cur.temperature_2m !== 'number') {
      throw new Error('bad payload')
    }
    applyWeather(cur.weather_code, cur.is_day === 1, cur.temperature_2m)
  } catch {
    error.value = true
    scene.value = 'sunny'
    isDay.value = true
    temperature.value = null
    desc.value = ''
  } finally {
    loading.value = false
  }
}

function isStale() {
  return Date.now() - lastFetchAt > REFRESH_MS
}

function scheduleRefresh() {
  if (refreshTimer !== null) window.clearInterval(refreshTimer)
  refreshTimer = window.setInterval(() => {
    if (mode.value === 'auto') fetchWeather()
  }, REFRESH_MS)
}

function onVisibility() {
  if (document.visibilityState === 'visible' && mode.value === 'auto' && isStale()) {
    fetchWeather()
  }
}

function loadManual() {
  const saved = localStorage.getItem(MANUAL_KEY)
  if (saved === 'sunny' || saved === 'cloudy' || saved === 'rainy' || saved === 'snowy' || saved === 'night') {
    mode.value = 'manual'
    scene.value = saved
    isDay.value = saved !== 'night'
    desc.value = SCENE_DESC[saved]
  }
}

function setManualScene(s: WeatherScene) {
  mode.value = 'manual'
  scene.value = s
  isDay.value = s !== 'night'
  desc.value = SCENE_DESC[s]
  error.value = false
  localStorage.setItem(MANUAL_KEY, s)
}

function resetToAuto() {
  mode.value = 'auto'
  localStorage.removeItem(MANUAL_KEY)
  fetchWeather()
}

/** 暗主题：夜晚、雨夜 */
const wbTheme = computed<'light' | 'dark'>(() => {
  if (scene.value === 'night') return 'dark'
  if (scene.value === 'rainy' && !isDay.value) return 'dark'
  return 'light'
})

export function useWeather() {
  if (!initialized && typeof window !== 'undefined') {
    initialized = true
    loadManual()
    if (mode.value === 'auto') fetchWeather()
    scheduleRefresh()
    document.addEventListener('visibilitychange', onVisibility)
  }
  return {
    scene,
    isDay,
    temperature,
    desc,
    mode,
    error,
    loading,
    wbTheme,
    setManualScene,
    resetToAuto,
    retry: fetchWeather
  }
}
