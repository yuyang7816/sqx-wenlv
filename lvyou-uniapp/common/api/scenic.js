/**
 * 景区 API（路径对齐 AppScenicController）
 */
import { get } from './request.js';
import { appendLocationParams } from '../location-params.js';

const PREFIX = '/app/scenic';

/** 启用中的景区分类（筛选 Tab） */
export function fetchScenicCategories() {
	return get(`${PREFIX}/categories`);
}

/** 景区列表（分页，公开接口，仅上架） */
export function fetchScenicList(params = {}) {
	return get(`${PREFIX}/list`, appendLocationParams({
		page: params.page || 1,
		limit: params.limit || 10,
		...(params.name ? { name: params.name } : {}),
		...(params.category && params.category !== 'all' ? { category: params.category } : {})
	}, params));
}

/** 景区详情（公开接口，登录时记录足迹） */
export function fetchScenicDetail(id) {
	return get(`${PREFIX}/${id}`);
}
