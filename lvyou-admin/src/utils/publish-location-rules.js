/** 管理端发布表单：必须通过地图选点 */

export function hasValidCoordinate(value) {
  if (value == null || value === '') return false
  const num = Number(value)
  return Number.isFinite(num)
}

export function hasValidLatLng(latitude, longitude) {
  if (!hasValidCoordinate(latitude) || !hasValidCoordinate(longitude)) return false
  const lat = Number(latitude)
  const lng = Number(longitude)
  return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180
}

export const publishLocationRules = [
  {
    validator: (_rule, value, callback) => {
      if (!hasValidCoordinate(value)) {
        callback(new Error('请在地图上搜索并选择地址'))
        return
      }
      callback()
    },
    trigger: 'change',
  },
]

/**
 * 历史手填地址无坐标时清空，强制重新地图选点。
 * 仅在表单打开后执行一次，避免选点分次写入 lat/lng 时被中间渲染清掉。
 */
export function sanitizePublishLocation(form) {
  if (!form || typeof form !== 'object') return form
  if (form._locationSanitized) return form
  form._locationSanitized = true
  if (!hasValidLatLng(form.latitude, form.longitude)) {
    form.latitude = null
    form.longitude = null
    form.address = ''
  } else {
    form.latitude = Number(form.latitude)
    form.longitude = Number(form.longitude)
  }
  return form
}

/** 地图选点结果一次性写回表单，避免分字段 emit 的中间态 */
export function applyPublishLocation(form, payload = {}) {
  if (!form || typeof form !== 'object') return form
  const latitude = payload.latitude
  const longitude = payload.longitude
  const address = String(payload.storeAddress || payload.address || '').trim()
  if (!hasValidLatLng(latitude, longitude)) {
    form.latitude = null
    form.longitude = null
    form.address = ''
    return form
  }
  form.latitude = Number(latitude)
  form.longitude = Number(longitude)
  form.address = address
  form._locationSanitized = true
  return form
}

export function ensurePublishLocation(form, fieldLabel = '位置') {
  if (!hasValidLatLng(form?.latitude, form?.longitude)) {
    throw new Error(`请在地图上搜索并选择${fieldLabel}`)
  }
  const address = String(form.address || '').trim()
  if (!address) {
    throw new Error(`请在地图上搜索并选择${fieldLabel}`)
  }
  form.latitude = Number(form.latitude)
  form.longitude = Number(form.longitude)
  form.address = address
  return true
}
