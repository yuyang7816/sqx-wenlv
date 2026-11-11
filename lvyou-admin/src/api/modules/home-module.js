import { createPageApi } from '@/api/admin'
import { post, resolveBusinessResponse } from '@/api/http'

/** 首页模块配置 CRUD（对齐 AdminHomeModuleController） */
export const homeModuleApi = createPageApi('admin/homeModule')

export function updateHomeModuleStateApi(data) {
  return post('admin/homeModule/updateState', data).then(resolveBusinessResponse)
}
