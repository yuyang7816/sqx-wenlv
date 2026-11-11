/**
 * 旅游线路 API（路径对齐 AppRouteController）
 */
import { get, postJson } from './request.js';
import { appendLocationParams } from '../location-params.js';

const PREFIX = '/app/route';

/** 线路列表（分页，公开接口；可按 merchantId / dayCount / minDayCount 筛选） */
export function fetchRouteList(params = {}) {
	const query = {
		page: params.page || 1,
		limit: params.limit || 10
	};
	if (params.name) query.name = params.name;
	if (params.merchantId) query.merchantId = params.merchantId;
	if (params.dayCount != null && params.dayCount !== '') query.dayCount = params.dayCount;
	if (params.minDayCount != null && params.minDayCount !== '') query.minDayCount = params.minDayCount;
	return get(`${PREFIX}/list`, appendLocationParams(query, params));
}

/** 线路详情（公开接口，登录时记录足迹） */
export function fetchRouteDetail(id) {
	return get(`${PREFIX}/${id}`);
}

/** 完整行程安排（公开接口） */
export function fetchRouteItinerary(id) {
	return get(`${PREFIX}/${id}/itinerary`);
}

/** 旅行社门店评价列表（公开接口） */
export function fetchRouteStoreReviews(merchantId, params = {}) {
	return get(`${PREFIX}/store/${merchantId}/reviews`, {
		page: params.page || 1,
		limit: params.limit || 10
	});
}

/** 提交旅行社门店评价（须 App JWT + 已完成订单 orderId） */
export function submitRouteStoreReview(merchantId, payload) {
	return postJson(`${PREFIX}/store/${merchantId}/reviews`, payload);
}

/** 映射评价项 */
export function mapRouteReviewItem(record) {
	return {
		id: record.id,
		user: record.user || '',
		avatar: record.avatar || '',
		content: record.content || '',
		time: record.time || '',
		rating: record.rating != null ? Number(record.rating) : 5,
		images: Array.isArray(record.images) ? record.images : []
	};
}
