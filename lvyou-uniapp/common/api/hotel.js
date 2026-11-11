/**
 * 酒店 API（路径对齐 AppHotelController）
 */
import { get, postJson } from './request.js';
import { appendLocationParams } from '../location-params.js';

const PREFIX = '/app/hotel';

/** 酒店列表（分页，公开接口） */
export function fetchHotelList(params = {}) {
	const query = {
		page: params.page || 1,
		limit: params.limit || 10
	};
	if (params.category && params.category !== 'all') {
		query.category = params.category;
	}
	if (params.sort && params.sort !== 'featured') {
		query.sort = params.sort;
	}
	if (params.priceOrder) {
		query.priceOrder = params.priceOrder;
	}
	if (params.distanceOrder) {
		query.distanceOrder = params.distanceOrder;
	}
	return get(`${PREFIX}/list`, appendLocationParams(query, params));
}

/** 酒店详情（公开接口，登录时记录足迹） */
export function fetchHotelDetail(id) {
	return get(`${PREFIX}/${id}`);
}

/** 房型列表（公开接口，仅在售） */
export function fetchHotelRooms(hotelId) {
	return get(`${PREFIX}/${hotelId}/rooms`);
}

/** 房型详情（公开接口） */
export function fetchHotelRoomDetail(hotelId, roomId) {
	return get(`${PREFIX}/${hotelId}/rooms/${roomId}`);
}

/** 酒店评价列表（公开接口） */
export function fetchHotelReviews(hotelId, params = {}) {
	return get(`${PREFIX}/${hotelId}/reviews`, {
		page: params.page || 1,
		limit: params.limit || 10
	});
}

/** 提交酒店评价（须 App JWT + 已完成订单 orderId） */
export function submitHotelReview(hotelId, payload) {
	return postJson(`${PREFIX}/${hotelId}/reviews`, payload);
}

/** 映射评价项 */
export function mapReviewItem(record) {
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
