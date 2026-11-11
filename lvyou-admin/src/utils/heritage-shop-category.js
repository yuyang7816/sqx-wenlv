/** 兜底分类（接口不可用时） */
export const HERITAGE_SHOP_CATEGORY_FALLBACK = [
  { label: '陶艺', value: 'pottery', code: 'pottery', name: '陶艺' },
  { label: '纺织', value: 'textile', code: 'textile', name: '纺织' },
  { label: '综合', value: 'culture', code: 'culture', name: '综合' },
]

export function normalizeShopCategoryOptions(options) {
  const list = Array.isArray(options) && options.length ? options : HERITAGE_SHOP_CATEGORY_FALLBACK
  return list.map((item) => ({
    code: item.code || item.value,
    name: item.name || item.label,
    label: item.label || item.name,
    value: item.value || item.code,
    sort: item.sort ?? 0,
  }))
}

export function resolveShopCategoryLabel(category, options) {
  if (!category) return '—'
  const list = normalizeShopCategoryOptions(options)
  return list.find((item) => item.value === category)?.label || category
}

export function resolveDefaultShopCategoryCode(options) {
  const list = normalizeShopCategoryOptions(options)
  return list[0]?.value || 'culture'
}

export function resolveShopCategoryFromCraft(craft, fallback) {
  if (!craft) return fallback || 'culture'
  return craft.shopCategory || fallback || 'culture'
}
