import { get, post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

/** 商户入驻申请分页（对齐 AdminMerchantController POST admin/merchant/apply/page） */
export function getMerchantApplyPage(data = {}) {
  return post('admin/merchant/apply/page', data).then(resolveBusinessResponse).then(resolvePageData)
}

/** 商户入驻申请详情（对齐 AdminMerchantController GET admin/merchant/apply/{id}） */
export function getMerchantApplyDetail(id) {
  return get(`admin/merchant/apply/${id}`).then(resolveBusinessResponse)
}

/** 审核商户入驻申请（对齐 AdminMerchantController POST admin/merchant/audit） */
export function auditMerchantApply(data) {
  return post('admin/merchant/audit', data).then(resolveBusinessResponse)
}

/** 商户分页列表（对齐 AdminMerchantController POST admin/merchant/page） */
export function getMerchantPage(data = {}) {
  return post('admin/merchant/page', data).then(resolveBusinessResponse).then(resolvePageData)
}

/** 商户详情（对齐 AdminMerchantController GET admin/merchant/{id}） */
export function getMerchantDetail(id) {
  return get(`admin/merchant/${id}`).then(resolveBusinessResponse)
}

/** 商户状态调整（对齐 AdminMerchantController POST admin/merchant/adjust） */
export function adjustMerchant(data) {
  return post('admin/merchant/adjust', data).then(resolveBusinessResponse)
}

/** 新增商户（对齐 AdminMerchantController POST admin/merchant/save） */
export function saveMerchant(data) {
  return post('admin/merchant/save', data).then(resolveBusinessResponse)
}

/** 更新商户（对齐 AdminMerchantController POST admin/merchant/update） */
export function updateMerchant(data) {
  return post('admin/merchant/update', data).then(resolveBusinessResponse)
}

/** 删除商户（对齐 AdminMerchantController POST admin/merchant/delete） */
export function deleteMerchant(data) {
  return post('admin/merchant/delete', data).then(resolveBusinessResponse)
}

/** 入驻类型列表（对齐 AdminMerchantStoreTypeController GET admin/merchant/store-type/list） */
export function getMerchantStoreTypeList() {
  return get('admin/merchant/store-type/list').then(resolveBusinessResponse)
}

/** 新增入驻类型 */
export function saveMerchantStoreType(data) {
  return post('admin/merchant/store-type/save', data).then(resolveBusinessResponse)
}

/** 更新入驻类型 */
export function updateMerchantStoreType(data) {
  return post('admin/merchant/store-type/update', data).then(resolveBusinessResponse)
}

/** 删除入驻类型 */
export function deleteMerchantStoreType(id) {
  return post(`admin/merchant/store-type/delete?id=${id}`).then(resolveBusinessResponse)
}

/** 调整商户保证金 POST admin/merchant/deposit/adjust */
export function adjustMerchantDeposit(data) {
  return post('admin/merchant/deposit/adjust', data).then(resolveBusinessResponse)
}

/** 审核商户退保申请 POST admin/merchant/deposit/refund/audit */
export function auditMerchantDepositRefund(data) {
  return post('admin/merchant/deposit/refund/audit', data).then(resolveBusinessResponse)
}
