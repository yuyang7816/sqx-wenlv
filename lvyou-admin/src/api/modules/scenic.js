import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

/** 旅游景区 CRUD（对齐 AdminScenicController POST admin/scenic/*） */
export const scenicApi = createPageApi('admin/scenic')

/** 景区分类 CRUD（对齐 AdminScenicCategoryController POST admin/scenic/category/*） */
export const scenicCategoryApi = createPageApi('admin/scenic/category')

/** 更新景区上下架状态（控制 C 端 list/map 是否展示） */
export function updateScenicStateApi(data) {
  return post('admin/scenic/updateState', data).then(resolveBusinessResponse)
}

/** 启用中的景区分类选项 */
export function fetchScenicCategoryOptionsApi() {
  return post('admin/scenic/category/options', {}).then(resolveBusinessResponse)
}

/** 更新景区分类启用状态 */
export function updateScenicCategoryStateApi(data) {
  return post('admin/scenic/category/updateState', data).then(resolveBusinessResponse)
}
