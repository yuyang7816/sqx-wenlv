/** 列表状态列：数字 → 简体中文文案（统一简体，避免裸数字） */

export const BANNER_STATE_LABEL = {
  1: '显示',
  2: '隐藏',
}

export const ENABLE_DISABLE_LABEL = {
  1: '启用',
  0: '禁用',
}

export const ON_SHELF_LABEL = {
  1: '上架',
  0: '下架',
}

export const DISPLAY_LABEL = {
  1: '展示',
  0: '隐藏',
}

export const YES_NO_LABEL = {
  1: '是',
  0: '否',
}

export const NOTICE_STATE_LABEL = {
  1: '已发布',
  0: '草稿',
}

export const DESTINATION_STATUS_LABEL = {
  0: '禁用',
  1: '已上线',
  2: '即将上线',
}

/** 非遗店铺：1 营业 / 0 歇业 */
export const SHOP_OPEN_LABEL = {
  1: '营业',
  0: '歇业',
}

export const ROOM_SALE_LABEL = {
  1: '在售',
  0: '下架',
}

export const COUPON_TEMPLATE_STATUS_LABEL = {
  1: '可领取',
  0: '已下架',
}

export function isSameStatus(current, next) {
  return Number(current) === Number(next)
}

export function mapStatusLabel(value, map, fallback = '-') {
  if (value == null || value === '') return fallback
  const key = Number(value)
  if (Number.isFinite(key) && Object.prototype.hasOwnProperty.call(map, key)) {
    return map[key]
  }
  return fallback
}

/** CrudPage 列 formatter：(row, column, value) */
export function createStatusFormatter(map, prop) {
  return (row, column, value) => {
    const raw = value ?? row?.[prop ?? column?.prop]
    return mapStatusLabel(raw, map)
  }
}

/** 优先展示 label 字段，缺失时按 status 数字映射 */
export function createLabelFallbackFormatter(map, options = {}) {
  const { labelProp = 'statusLabel', statusProp = 'status' } = options
  return (row, column, value) => {
    const label = value ?? row?.[labelProp]
    if (label != null && label !== '') return label
    return mapStatusLabel(row?.[statusProp], map)
  }
}

/** 选项列表 value → label（如分类编码） */
export function createOptionLabelFormatter(options, valueProp) {
  const map = Object.fromEntries((options || []).map((item) => [item.value, item.label]))
  return (row, column, value) => {
    const raw = value ?? row?.[valueProp ?? column?.prop]
    if (raw == null || raw === '') return '-'
    return map[raw] || raw
  }
}

export const formatBannerState = createStatusFormatter(BANNER_STATE_LABEL, 'state')
export const formatEnableDisable = createStatusFormatter(ENABLE_DISABLE_LABEL, 'status')
export const formatOnShelf = createStatusFormatter(ON_SHELF_LABEL, 'status')
export const formatDisplayStatus = createStatusFormatter(DISPLAY_LABEL, 'status')
export const formatYesNo = createStatusFormatter(YES_NO_LABEL, 'isDefault')
export const formatNoticeState = createLabelFallbackFormatter(NOTICE_STATE_LABEL, {
  labelProp: 'stateLabel',
  statusProp: 'state',
})
export const formatDestinationStatus = createLabelFallbackFormatter(DESTINATION_STATUS_LABEL)
export const formatOnShelfLabel = createLabelFallbackFormatter(ON_SHELF_LABEL)
export const formatShopOpen = createLabelFallbackFormatter(SHOP_OPEN_LABEL)
export const formatRoomSale = createLabelFallbackFormatter(ROOM_SALE_LABEL)
export const formatCouponStatus = createLabelFallbackFormatter(COUPON_TEMPLATE_STATUS_LABEL)
