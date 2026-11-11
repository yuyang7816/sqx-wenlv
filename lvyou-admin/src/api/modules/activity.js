import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

/** 精彩活动 CRUD（对齐 AdminActivityController POST admin/activity/*） */
export const activityApi = createPageApi('admin/activity')

/** 更新活动上下架状态（控制 C 端 list 是否展示） */
export function updateActivityStateApi(data) {
  return post('admin/activity/updateState', data).then(resolveBusinessResponse)
}
