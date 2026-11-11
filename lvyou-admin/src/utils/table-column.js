const IMAGE_PROP_PATTERN = /(image|cover|avatar|logo|poster|banner|photo|pic|thumbnail|qr|thumb)/i

const NON_IMAGE_PROPS = new Set(['description', 'classification', 'category'])

/** 判断字符串是否像图片 URL */
export function looksLikeImageUrl(value) {
  if (value == null || value === '') return false
  const text = String(value).trim().replace(/\\/g, '/')
  if (!text) return false
  if (/^data:image\//i.test(text)) return true
  if (/\.(png|jpe?g|gif|webp|bmp|svg|avif)(\?|#|$)/i.test(text)) return true
  if (text.startsWith('http://') || text.startsWith('https://') || text.startsWith('//')) {
    return text.includes('/upload') || text.includes('/file/') || /\.(png|jpe?g|gif|webp|bmp|svg|avif)/i.test(text)
  }
  if (text.startsWith('/')) {
    return text.includes('/upload') || text.includes('/file/') || /\.(png|jpe?g|gif|webp|bmp|svg|avif)/i.test(text)
  }
  return false
}

/** 从单元格值提取全部图片 URL */
export function collectImageUrls(value) {
  if (value == null || value === '') return []
  if (Array.isArray(value)) {
    return value.flatMap((item) => collectImageUrls(item))
  }
  if (typeof value === 'object') {
    if (value.image) return collectImageUrls(value.image)
    if (value.url) return collectImageUrls(value.url)
    if (value.path) return collectImageUrls(value.path)
    return []
  }
  return looksLikeImageUrl(value) ? [String(value).trim().replace(/\\/g, '/')] : []
}

/** 列表列是否应渲染为图片 */
export function shouldRenderImageCell(column, value) {
  if (column?.type === 'text') return false
  if (column?.type === 'image' || column?.type === 'images') {
    return collectImageUrls(value).length > 0
  }
  if (!column?.prop) return false
  if (NON_IMAGE_PROPS.has(column.prop)) return false
  if (column.prop === 'icon') {
    return looksLikeImageUrl(value)
  }
  if (IMAGE_PROP_PATTERN.test(column.prop)) {
    return collectImageUrls(value).length > 0
  }
  return looksLikeImageUrl(value)
}

/** 传给 el-table-column 的属性（formatter/type 由自定义单元格处理） */
export function getColumnBind(column = {}) {
  const bind = { ...column }
  delete bind.formatter
  delete bind.type
  return bind
}

/** 普通文本单元格展示 */
export function formatColumnCell(column, row) {
  const value = row?.[column.prop]
  if (typeof column?.formatter === 'function') {
    const formatted = column.formatter(row, column, value)
    return formatted == null || formatted === '' ? '-' : formatted
  }
  return value == null || value === '' ? '-' : value
}
