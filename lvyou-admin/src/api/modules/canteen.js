import { get, post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

/** 餐饮预约分页（对齐 AdminCanteenController POST admin/canteen/page） */
export function getCanteenPage(data = {}) {
  return post('admin/canteen/page', data).then(resolveBusinessResponse).then(resolvePageData)
}

/** 餐饮预约详情（对齐 AdminCanteenController GET admin/canteen/{id}） */
export function getCanteenDetail(id) {
  return get(`admin/canteen/${id}`).then(resolveBusinessResponse)
}

/** 餐饮预约操作：confirm/cancel/complete（对齐 AdminCanteenController POST admin/canteen/action） */
export function postCanteenAction(data) {
  return post('admin/canteen/action', data).then(resolveBusinessResponse)
}
