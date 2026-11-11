/**
 * 精彩活动 API（路径对齐 AppActivityController）
 */
import { get, post } from './request.js';
import { appendLocationParams } from '../location-params.js';

const PREFIX = '/app/activity';

/** 活动列表（分页，公开接口） */
export function fetchActivityList(params = {}) {
	return get(`${PREFIX}/list`, appendLocationParams({
		page: params.page || 1,
		limit: params.limit || 10,
		...(params.name ? { name: params.name } : {}),
		...(params.status && params.status !== 'all' ? { status: params.status } : {})
	}, params));
}

/** 活动详情（公开接口，登录时返回 enrolled） */
export function fetchActivityDetail(id) {
	return get(`${PREFIX}/${id}`);
}

/** 活动报名（需 App JWT） */
export function enrollActivity(id) {
	return post(`${PREFIX}/${id}/enroll`);
}
