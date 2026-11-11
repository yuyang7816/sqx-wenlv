/**
 * 餐饮预约 API（路径对齐 AppCanteenReservationController）
 */
import { get, postJson } from './request.js';
import { getAppLocationParams } from '../location-params.js';

const PREFIX = '/app/reservation/canteen';

/** 映射列表/详情项字段 */
export function mapCanteenRecord(item) {
	if (!item) return null;
	return {
		id: String(item.id),
		reservationNo: item.reservationNo || '',
		shopName: item.shopName || '',
		merchantId: item.merchantId ? String(item.merchantId) : '',
		shopCover: item.shopCover || '',
		dishName: item.dishName || '',
		merchantName: item.merchantName || '',
		contactName: item.contactName || '',
		phone: item.phone || '',
		guestCount: item.guestCount || 1,
		reserveTime: item.reserveTime || '',
		address: item.address || '',
		remark: item.remark || '',
		status: item.status || 'pending',
		statusLabel: item.statusLabel || '',
		createdAt: item.createdAt || ''
	};
}

/** 提交餐饮预约（需 App JWT，自动附带当前定位） */
export async function submitCanteenReservation(payload) {
	const location = await getAppLocationParams();
	const data = { ...payload };
	if (location.lat && location.lng) {
		data.lat = location.lat;
		data.lng = location.lng;
	}
	return postJson(`${PREFIX}/submit`, data);
}

/** 餐饮预约列表（分页，需 App JWT） */
export function fetchCanteenList(params = {}) {
	const data = {
		page: params.page || 1,
		limit: params.limit || 10
	};
	if (params.status && params.status !== 'all') {
		data.status = params.status;
	}
	return get(`${PREFIX}/list`, data);
}

/** 餐饮预约详情（需 App JWT） */
export function fetchCanteenDetail(id) {
	return get(`${PREFIX}/${id}`);
}

/** 待确认预约数量（我的页角标，需 App JWT） */
export function fetchCanteenPendingCount() {
	return get(`${PREFIX}/pending-count`);
}

/** 取消餐饮预约（需 App JWT） */
export function cancelCanteenReservation(id) {
	return postJson(`${PREFIX}/${id}/cancel`, {});
}

/** 修改餐饮预约（需 App JWT） */
export function updateCanteenReservation(id, payload) {
	return postJson(`${PREFIX}/${id}/update`, payload);
}
