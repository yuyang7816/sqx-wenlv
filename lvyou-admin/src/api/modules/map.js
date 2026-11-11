import { get, resolveBusinessResponse } from '@/api/http'

/** 管理端地图选点配置 */
export function getMapPickerConfig() {
  return get('admin/map/picker-config').then(resolveBusinessResponse)
}

/** 反向地理编码（经纬度 → 地址） */
export function reverseGeocode(lat, lng) {
  return get('app/map/reverseGeocode', { lat, lng }).then(resolveBusinessResponse)
}

/** 正向地理编码（地址 → 经纬度） */
export function geocodeAddress(address) {
  return get('app/map/geocode', { address }).then(resolveBusinessResponse)
}
