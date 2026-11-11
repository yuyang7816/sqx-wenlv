import { post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

/** 注销申请分页列表 */
export function getCancelAccountPage(data = {}) {
  return post('admin/base/cancelAccount/page', data).then(resolveBusinessResponse).then(resolvePageData)
}

/** 注销审核（通过/驳回） */
export function auditCancelAccount(data) {
  return post('admin/base/cancelAccount/audit', data).then(resolveBusinessResponse)
}
