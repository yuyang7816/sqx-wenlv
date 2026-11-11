import { get, post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

/** 订单分页列表（对齐 AdminOrderController POST admin/order/page） */
export function getOrderPage(data = {}) {
  return post('admin/order/page', data).then(resolveBusinessResponse).then(resolvePageData)
}

/** 订单详情（对齐 AdminOrderController GET admin/order/{id}） */
export function getOrderDetail(id) {
  return get(`admin/order/${id}`).then(resolveBusinessResponse)
}

/** 订单操作：cancel/complete/refund（对齐 AdminOrderController POST admin/order/action） */
export function postOrderAction(data) {
  return post('admin/order/action', data).then(resolveBusinessResponse)
}

/** 线路订单强制退款预览（R8，对齐 GET admin/order/{id}/refund-preview） */
export function getRouteRefundPreview(id, refundRatio) {
  return get(`admin/order/${id}/refund-preview`, { refundRatio }).then(resolveBusinessResponse)
}
