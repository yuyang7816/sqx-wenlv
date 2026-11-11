/**
 * 优惠券 UI 文案与纯工具（无 mock 数据）
 */

export const COUPON_SELECT_RESULT_KEY = 'ry_coupon_select_result';

export const COUPON_SCOPE_ALL = 'all';
export const COUPON_SCOPE_HOTEL = 'hotel';
export const COUPON_SCOPE_ROUTE = 'route';
export const COUPON_SCOPE_GOODS = 'goods';

export const COUPON_STATUS_AVAILABLE = 'available';
export const COUPON_STATUS_USED = 'used';
export const COUPON_STATUS_EXPIRED = 'expired';

export const COUPON_UI = {
	centerTitle: '领券中心',
	myTitle: '我的优惠券',
	selectTitle: '选择优惠券',
	tabAvailable: '未使用',
	tabUsed: '已使用',
	tabExpired: '已过期',
	claimBtn: '立即领取',
	claimedBtn: '已领取',
	useBtn: '去使用',
	goCenter: '去领券中心',
	goMyCoupons: '查看我的优惠券',
	emptyCenter: '暂无可领取的优惠券',
	emptyMy: '暂无优惠券',
	emptySelect: '暂无可用优惠券',
	notUseCoupon: '不使用优惠券',
	confirmSelect: '确认',
	labelCoupon: '优惠券',
	labelDiscount: '优惠抵扣',
	selectHint: '张可用',
	noAvailable: '暂无可用',
	minAmountPrefix: '满',
	minAmountSuffix: '元可用',
	validUntil: '有效期至',
	scopeAll: '全场通用',
	scopeHotel: '酒店预订',
	scopeRoute: '旅游线路',
	scopeGoods: '非遗好物',
	claimSuccess: '领取成功',
	claimFail: '领取失败',
	statusUsed: '已使用',
	statusExpired: '已过期',
	centerTip: '领取后可在下单时使用',
	myTip: '下单时在确认页选择可用优惠券',
	inapplicable: '未满足使用条件'
};

export function formatCouponScope(scope) {
	const map = {
		[COUPON_SCOPE_ALL]: COUPON_UI.scopeAll,
		[COUPON_SCOPE_HOTEL]: COUPON_UI.scopeHotel,
		[COUPON_SCOPE_ROUTE]: COUPON_UI.scopeRoute,
		[COUPON_SCOPE_GOODS]: COUPON_UI.scopeGoods
	};
	return map[scope] || COUPON_UI.scopeAll;
}

export function formatCouponLimit(coupon) {
	return `${COUPON_UI.minAmountPrefix}${coupon.minAmount}${COUPON_UI.minAmountSuffix}`;
}

export function mapBookingTypeToCouponScope(type) {
	if (type === 'hotel') return COUPON_SCOPE_HOTEL;
	if (type === 'route') return COUPON_SCOPE_ROUTE;
	return COUPON_SCOPE_ALL;
}

export function setCouponSelectResult(result) {
	try {
		uni.setStorageSync(COUPON_SELECT_RESULT_KEY, result);
	} catch (e) {
		/* ignore */
	}
}

export function consumeCouponSelectResult() {
	try {
		const result = uni.getStorageSync(COUPON_SELECT_RESULT_KEY);
		uni.removeStorageSync(COUPON_SELECT_RESULT_KEY);
		return result || null;
	} catch (e) {
		return null;
	}
}
