/**
 * 编解码与哈希纯函数层（纯前端零依赖）
 * 可能失败的函数统一抛 Error，页面层 catch 后显示错误提示
 */

/* ---------- Base64（UTF-8 安全） ---------- */

export function base64Encode(text: string): string {
  const bytes = new TextEncoder().encode(text)
  let bin = ''
  for (let i = 0; i < bytes.length; i++) bin += String.fromCharCode(bytes[i])
  return btoa(bin)
}

export function base64Decode(b64: string): string {
  const cleaned = b64.replace(/\s+/g, '')
  if (!/^[A-Za-z0-9+/]*={0,2}$/.test(cleaned) || cleaned.length % 4 !== 0) {
    throw new Error('不是合法的 Base64 字符串')
  }
  let bin: string
  try {
    bin = atob(cleaned)
  } catch {
    throw new Error('不是合法的 Base64 字符串')
  }
  const bytes = new Uint8Array(bin.length)
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i)
  try {
    return new TextDecoder('utf-8', { fatal: true }).decode(bytes)
  } catch {
    throw new Error('解码结果不是合法 UTF-8 文本')
  }
}

/* ---------- URL 编解码（双模式） ---------- */

export type UrlEncodeMode = 'component' | 'full'

export function urlEncode(text: string, mode: UrlEncodeMode): string {
  return mode === 'component' ? encodeURIComponent(text) : encodeURI(text)
}

export function urlDecode(text: string, mode: UrlEncodeMode): string {
  try {
    return mode === 'component' ? decodeURIComponent(text) : decodeURI(text)
  } catch {
    throw new Error('不是合法的百分号编码字符串')
  }
}

/* ---------- HTML 实体 ---------- */

export function htmlEscape(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

export function htmlUnescape(text: string): string {
  // textarea 不渲染不执行脚本，无 XSS 面
  const ta = document.createElement('textarea')
  ta.innerHTML = text
  return ta.value
}

/* ---------- Unicode 编解码 ---------- */

export function unicodeEscape(text: string): string {
  let out = ''
  for (const ch of text) {
    const code = ch.codePointAt(0)!
    if (code < 0x80) {
      out += ch
    } else if (code <= 0xffff) {
      out += '\\u' + code.toString(16).padStart(4, '0')
    } else {
      // 代理对（emoji 等增补平面字符）
      out += '\\u' + ch.charCodeAt(0).toString(16).padStart(4, '0')
      out += '\\u' + ch.charCodeAt(1).toString(16).padStart(4, '0')
    }
  }
  return out
}

export function unicodeUnescape(text: string): string {
  if (/\\u(?![0-9a-fA-F]{4})/.test(text)) {
    throw new Error('存在非法的 \\u 转义序列（需 4 位十六进制）')
  }
  return text.replace(/\\u([0-9a-fA-F]{4})/g, (_, hex) => String.fromCharCode(parseInt(hex, 16)))
}

/* ---------- MD5（RFC 1321 内嵌实现） ---------- */

const MD5_K = new Uint32Array([
  0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee,
  0xf57c0faf, 0x4787c62a, 0xa8304613, 0xfd469501,
  0x698098d8, 0x8b44f7af, 0xffff5bb1, 0x895cd7be,
  0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821,
  0xf61e2562, 0xc040b340, 0x265e5a51, 0xe9b6c7aa,
  0xd62f105d, 0x02441453, 0xd8a1e681, 0xe7d3fbc8,
  0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed,
  0xa9e3e905, 0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a,
  0xfffa3942, 0x8771f681, 0x6d9d6122, 0xfde5380c,
  0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70,
  0x289b7ec6, 0xeaa127fa, 0xd4ef3085, 0x04881d05,
  0xd9d4d039, 0xe6db99e5, 0x1fa27cf8, 0xc4ac5665,
  0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039,
  0x655b59c3, 0x8f0ccc92, 0xffeff47d, 0x85845dd1,
  0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1,
  0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391
])

const MD5_S = [
  7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
  5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
  4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
  6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
]

function rotl32(x: number, c: number): number {
  return (x << c) | (x >>> (32 - c))
}

function leHex(word: number): string {
  let out = ''
  for (let i = 0; i < 4; i++) {
    out += ((word >>> (i * 8)) & 0xff).toString(16).padStart(2, '0')
  }
  return out
}

export function md5(text: string): string {
  const msg = new TextEncoder().encode(text)
  const bitLen = msg.length * 8

  // 补齐：追加 0x80，补零至长度 ≡ 56 (mod 64)，末尾 8 字节小端位长度
  let paddedLen = msg.length + 1
  while (paddedLen % 64 !== 56) paddedLen++
  paddedLen += 8
  const buf = new Uint8Array(paddedLen)
  buf.set(msg)
  buf[msg.length] = 0x80
  const dv = new DataView(buf.buffer)
  dv.setUint32(paddedLen - 8, bitLen >>> 0, true)
  dv.setUint32(paddedLen - 4, Math.floor(bitLen / 0x100000000), true)

  let a0 = 0x67452301
  let b0 = 0xefcdab89
  let c0 = 0x98badcfe
  let d0 = 0x10325476
  const M = new Int32Array(16)

  for (let off = 0; off < paddedLen; off += 64) {
    for (let i = 0; i < 16; i++) M[i] = dv.getInt32(off + i * 4, true)
    let A = a0
    let B = b0
    let C = c0
    let D = d0
    for (let i = 0; i < 64; i++) {
      let F: number
      let g: number
      if (i < 16) {
        F = (B & C) | (~B & D)
        g = i
      } else if (i < 32) {
        F = (D & B) | (~D & C)
        g = (5 * i + 1) % 16
      } else if (i < 48) {
        F = B ^ C ^ D
        g = (3 * i + 5) % 16
      } else {
        F = C ^ (B | ~D)
        g = (7 * i) % 16
      }
      const tmp = D
      D = C
      C = B
      B = (B + rotl32((A + F + MD5_K[i] + M[g]) | 0, MD5_S[i])) | 0
      A = tmp
    }
    a0 = (a0 + A) | 0
    b0 = (b0 + B) | 0
    c0 = (c0 + C) | 0
    d0 = (d0 + D) | 0
  }

  return leHex(a0) + leHex(b0) + leHex(c0) + leHex(d0)
}

/* ---------- SHA（Web Crypto） ---------- */

export type ShaAlgorithm = 'SHA-1' | 'SHA-256' | 'SHA-384' | 'SHA-512'

export function webCryptoAvailable(): boolean {
  return typeof crypto !== 'undefined' && !!crypto.subtle && typeof crypto.subtle.digest === 'function'
}

export async function sha(algorithm: ShaAlgorithm, text: string): Promise<string> {
  const buf = await crypto.subtle.digest(algorithm, new TextEncoder().encode(text))
  const bytes = new Uint8Array(buf)
  let out = ''
  for (let i = 0; i < bytes.length; i++) out += bytes[i].toString(16).padStart(2, '0')
  return out
}

/* ---------- UUID v4 ---------- */

export interface UuidOptions {
  uppercase: boolean
  hyphens: boolean
}

export function genUuids(count: number, opts: UuidOptions): string[] {
  const out: string[] = []
  const bytes = new Uint8Array(16)
  for (let n = 0; n < count; n++) {
    crypto.getRandomValues(bytes)
    bytes[6] = (bytes[6] & 0x0f) | 0x40 // version 4
    bytes[8] = (bytes[8] & 0x3f) | 0x80 // variant 10xx
    let hex = ''
    for (let i = 0; i < 16; i++) hex += bytes[i].toString(16).padStart(2, '0')
    if (opts.hyphens) {
      hex = `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`
    }
    out.push(opts.uppercase ? hex.toUpperCase() : hex)
  }
  return out
}
