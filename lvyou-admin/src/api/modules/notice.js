import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

/** 公告管理 CRUD（路径对齐 AdminNoticeController） */
export const noticeApi = createPageApi('admin/notice')

/** 发布 / 撤回草稿 */
export function updateNoticeStateApi(data) {
  return post('admin/notice/updateState', data).then(resolveBusinessResponse)
}

/** 置顶 / 取消置顶（全局仅一条） */
export function updateNoticePinnedApi(data) {
  return post('admin/notice/updatePinned', data).then(resolveBusinessResponse)
}
