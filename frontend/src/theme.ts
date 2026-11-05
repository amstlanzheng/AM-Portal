/**
 * 界面主题系统：预设色板定义 + 运行时换肤
 * 色阶与 design.md D3 一致（Tailwind 基准，琥珀 600 加深一档保证按钮白字对比度）
 */

export interface ThemePalette {
  key: string
  name: string
  /** 色阶：700(hover 深) / 600(主色) / 500(亮) / 100(浅) / 50(最浅底) */
  colors: Record<'700' | '600' | '500' | '100' | '50', string>
}

export const THEMES: ThemePalette[] = [
  {
    key: 'teal',
    name: '青绿',
    colors: { '700': '#0F766E', '600': '#0D9488', '500': '#14B8A6', '100': '#CCFBF1', '50': '#F0FDFA' }
  },
  {
    key: 'blue',
    name: '蔚蓝',
    colors: { '700': '#1D4ED8', '600': '#2563EB', '500': '#3B82F6', '100': '#DBEAFE', '50': '#EFF6FF' }
  },
  {
    key: 'violet',
    name: '紫罗兰',
    colors: { '700': '#6D28D9', '600': '#7C3AED', '500': '#8B5CF6', '100': '#EDE9FE', '50': '#F5F3FF' }
  },
  {
    key: 'rose',
    name: '玫红',
    colors: { '700': '#BE123C', '600': '#E11D48', '500': '#F43F5E', '100': '#FFE4E6', '50': '#FFF1F2' }
  },
  {
    key: 'amber',
    name: '琥珀',
    colors: { '700': '#92400E', '600': '#B45309', '500': '#D97706', '100': '#FEF3C7', '50': '#FFFBEB' }
  },
  {
    key: 'slate',
    name: '石墨',
    colors: { '700': '#334155', '600': '#475569', '500': '#64748B', '100': '#E2E8F0', '50': '#F8FAFC' }
  }
]

export const DEFAULT_THEME = 'teal'

export function getTheme(key: string | null | undefined): ThemePalette {
  return THEMES.find(t => t.key === key) ?? THEMES[0]
}

export function isValidTheme(key: string | null | undefined): boolean {
  return !!key && THEMES.some(t => t.key === key)
}

/** 应用主题：覆盖品牌色阶变量，全站语义层与 Element Plus 层自动跟随 */
export function applyTheme(key: string | null | undefined): string {
  const theme = getTheme(key)
  const root = document.documentElement
  root.style.setProperty('--brand-700', theme.colors['700'])
  root.style.setProperty('--brand-600', theme.colors['600'])
  root.style.setProperty('--brand-500', theme.colors['500'])
  root.style.setProperty('--brand-100', theme.colors['100'])
  root.style.setProperty('--brand-50', theme.colors['50'])
  root.dataset.theme = theme.key
  localStorage.setItem('theme', theme.key)
  return theme.key
}
