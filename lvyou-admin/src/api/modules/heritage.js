import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

/** 非遗店铺 CRUD（对齐 AdminHeritageShopController POST admin/heritage/shop/*） */
export const heritageShopApi = createPageApi('admin/heritage/shop')

/** 非遗工艺 CRUD（对齐 AdminHeritageCraftController POST admin/heritage/craft/*） */
export const heritageCraftApi = createPageApi('admin/heritage/craft')

/** 非遗商品 CRUD（对齐 AdminGoodsController POST admin/goods/*） */
export const heritageGoodsApi = createPageApi('admin/goods')

/** 非遗好店分类 CRUD（对齐 AdminHeritageShopCategoryController POST admin/heritage/shop/category/*） */
export const heritageShopCategoryApi = createPageApi('admin/heritage/shop/category')

/** 更新店铺营业状态 */
export function updateHeritageShopStateApi(data) {
  return post('admin/heritage/shop/updateState', data).then(resolveBusinessResponse)
}

/** 更新工艺上下架状态 */
export function updateHeritageCraftStateApi(data) {
  return post('admin/heritage/craft/updateState', data).then(resolveBusinessResponse)
}

/** 强制上下架非遗好物（同步商户服务状态） */
export function updateGoodsStateApi(data) {
  return post('admin/goods/updateState', data).then(resolveBusinessResponse)
}

/** 启用中的非遗好店分类选项 */
export function fetchHeritageShopCategoryOptionsApi() {
  return post('admin/heritage/shop/category/options', {}).then(resolveBusinessResponse)
}

/** 更新非遗好店分类启用状态 */
export function updateHeritageShopCategoryStateApi(data) {
  return post('admin/heritage/shop/category/updateState', data).then(resolveBusinessResponse)
}
