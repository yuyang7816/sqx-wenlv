import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

/** 目的地下拉与筛选（按已匹配的目的地查询，不提供手工维护页） */
export const destinationApi = createPageApi('admin/destination')

/** 更新目的地上下架/状态 */
export function updateDestinationStateApi(data) {
  return post('admin/destination/updateState', data).then(resolveBusinessResponse)
}
