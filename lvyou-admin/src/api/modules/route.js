import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

/** 旅游线路 CRUD（对齐 AdminRouteController POST admin/route/*） */
export const routeApi = createPageApi('admin/route')

/** 更新线路上下架状态 */
export function updateRouteStateApi(data) {
  return post('admin/route/updateState', data).then(resolveBusinessResponse)
}
