import { post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

/** 提现分页列表（对齐 AdminWithdrawController POST admin/withdraw/page） */
export function getWithdrawPage(data = {}) {
  return post('admin/withdraw/page', data).then(resolveBusinessResponse).then(resolvePageData)
}

/** 提现审核（对齐 AdminWithdrawController POST admin/withdraw/audit） */
export function auditWithdraw(data) {
  return post('admin/withdraw/audit', data).then(resolveBusinessResponse)
}
