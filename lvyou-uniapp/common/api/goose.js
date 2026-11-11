/**
 * 灰鹅美食 API（路径对齐 AppGooseController）
 */
import { get } from './request.js';
import { appendLocationParams } from '../location-params.js';

const PREFIX = '/app/goose';

/** 商家列表（分页，公开接口，仅正常营业） */
export function fetchGooseList(params = {}) {
	return get(`${PREFIX}/list`, appendLocationParams({
		page: params.page || 1,
		limit: params.limit || 10,
		...(params.keyword ? { keyword: params.keyword } : {})
	}, params));
}

/** 商家详情（公开接口，含招牌菜预览与营业时间） */
export function fetchGooseDetail(id) {
	return get(`${PREFIX}/${id}`);
}

/** 全部在售菜单（公开接口） */
export function fetchGooseMenu(id) {
	return get(`${PREFIX}/${id}/menu`);
}

/** 菜品详情（公开接口，含轮播图） */
export function fetchGooseDishDetail(serviceId) {
	return get(`${PREFIX}/dish/${serviceId}`);
}
