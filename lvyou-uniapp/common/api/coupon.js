/**
 * 优惠券 API（路径对齐 AppCouponController）
 */
import { get, postJson } from './request.js';

const PREFIX = '/app/coupon';

/** 领券中心 */
export function fetchCouponCenter() {
	return get(`${PREFIX}/center`);
}

/** 领取优惠券 */
export function claimCoupon(templateId) {
	return postJson(`${PREFIX}/claim`, { templateId });
}

/** 我的优惠券（status: available | used | expired） */
export function fetchMyCoupons(status) {
	const params = {};
	if (status) params.status = status;
	return get(`${PREFIX}/mine`, params);
}

/** 下单可用优惠券 */
export function fetchAvailableCoupons(scope, amount) {
	const params = {};
	if (scope) params.scope = scope;
	if (amount != null && amount !== '') params.amount = amount;
	return get(`${PREFIX}/available`, params);
}

/** 可用优惠券数量 */
export function fetchCouponCount() {
	return get(`${PREFIX}/count`);
}
