import { get, resolveBusinessResponse } from '@/api/http'

/** 业务字典项（对齐 AdminDictQueryController） */
export function fetchDictItems(type) {
  return get('admin/base/dict/items', { type }).then(resolveBusinessResponse)
}
