import {
  getSystemConfigListApi,
  refreshSystemConfigApi,
  saveSystemConfigApi,
  updateSystemConfigApi,
} from '@/api/modules/system-config'

/** 平台运营配置分组（品牌 / 客服 / 协议 / 邀请） */
export const PLATFORM_CONFIG_TYPES = ['kefu', 'agreement', 'invite', 'wallet', 'transport', 'order', 'route_order', 'logistics', 'pay']

/** 品牌配置键（归属 sys 分组，在平台配置页单独维护） */
export const BRAND_CONFIG_KEY = 'sys_server_name'

/**
 * 加载指定 paramType 的配置项列表
 * @returns {Promise<Record<string, Array>>}
 */
export async function loadPlatformConfigGroups() {
  const groups = await getSystemConfigListApi()
  const result = {}
  PLATFORM_CONFIG_TYPES.forEach((type) => {
    const group = (groups || []).find((item) => item.paramType === type)
    result[type] = group?.list || group?.sysConfigEntityList || []
  })
  const sysGroup = (groups || []).find((item) => item.paramType === 'sys')
  result.sys = sysGroup?.list || sysGroup?.sysConfigEntityList || []
  return result
}

/**
 * 将配置项列表转为 paramKey → entity 映射
 * @param {Array} items
 */
export function indexConfigByKey(items = []) {
  const map = {}
  items.forEach((item) => {
    if (item?.paramKey) map[item.paramKey] = item
  })
  return map
}

/**
 * 批量更新有变更的配置项并刷新缓存
 * @param {Array<{ id: number, paramKey: string, paramValue: string, status?: string, remark?: string, paramType?: string, paramTypeDesc?: string }>} items
 */
export async function batchUpdatePlatformConfig(items = [], creates = []) {
  const updateTasks = items.filter((item) => item?.id).map((item) => updateSystemConfigApi({ ...item }))
  const createTasks = creates
    .filter((item) => item?.paramKey && String(item.paramValue ?? '').trim())
    .map((item) => saveSystemConfigApi({ ...item }))
  const tasks = [...updateTasks, ...createTasks]
  if (tasks.length) await Promise.all(tasks)
  await refreshSystemConfigApi()
}
