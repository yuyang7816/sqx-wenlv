import { get, post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

/** 支付流水分页（对齐 AdminPaymentController POST admin/payment/page） */
export function getPaymentPage(data = {}) {
  return post('admin/payment/page', data).then(resolveBusinessResponse).then(resolvePageData)
}

/** 支付流水详情（对齐 AdminPaymentController GET admin/payment/info） */
export function getPaymentInfo(payNo, businessType) {
  const params = new URLSearchParams({ payNo })
  if (businessType) params.set('businessType', businessType)
  return get(`admin/payment/info?${params.toString()}`).then(resolveBusinessResponse)
}
