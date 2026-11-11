import { post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

/** 邀请关系分页（对齐 AdminInviteController POST admin/invite/relation/page） */
export function getInviteRelationPage(data = {}) {
  return post('admin/invite/relation/page', data).then(resolveBusinessResponse).then(resolvePageData)
}

/** 佣金记录分页（对齐 AdminInviteController POST admin/invite/commission/page） */
export function getInviteCommissionPage(data = {}) {
  return post('admin/invite/commission/page', data).then(resolveBusinessResponse).then(resolvePageData)
}
