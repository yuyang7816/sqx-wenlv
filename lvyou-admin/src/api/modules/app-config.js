import { get, resolveBusinessResponse } from '@/api/http'

/** 公开聚合配置（产品名称/客服/邀请） */
export function fetchAppConfigInfoApi() {
  // 避免浏览器 304 返回空 body，导致 productName 无法写入
  return get('/app/config/info', { _t: Date.now() }, { silentError: true }).then(resolveBusinessResponse)
}
