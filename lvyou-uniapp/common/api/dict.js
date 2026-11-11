/**
 * 业务字典 API（路径对齐 AppDictController）
 */
import { get } from './request.js';

/** 按 type 拉取字典项（公开接口） */
export function fetchDictItems(type) {
	return get('/app/base/dict/items', { type });
}
