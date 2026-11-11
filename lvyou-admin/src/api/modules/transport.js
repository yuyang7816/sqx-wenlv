import { get, post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

/** 公交线路 CRUD（对齐 AdminTransportController POST admin/transport/route/*） */
export const transportRouteApi = createPageApi('admin/transport/route')

/** 更新公交线路上下架状态 */
export function updateTransportRouteStateApi(data) {
  return post('admin/transport/route/updateState', data).then(resolveBusinessResponse)
}

/**
 * 编辑弹窗补充加载站点（App 公开接口，按 routeCode；仅上架线路可返回数据）
 * GET /app/transport/routes/{routeCode}/stations
 */
export function fetchTransportStations(routeCode) {
  return get(`app/transport/routes/${encodeURIComponent(routeCode)}/stations`, {}, { silentError: true }).then(
    resolveBusinessResponse,
  )
}

/**
 * 编辑弹窗补充加载班次（App 公开接口，按 routeCode；仅上架线路可返回数据）
 * GET /app/transport/routes/{routeCode}/schedule
 */
export function fetchTransportSchedule(routeCode) {
  return get(`app/transport/routes/${encodeURIComponent(routeCode)}/schedule`, {}, { silentError: true }).then(
    resolveBusinessResponse,
  )
}
