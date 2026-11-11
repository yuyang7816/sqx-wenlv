/**
 * 订单类型常量（与后端 OrderType / tb_order.order_type 一致）
 */
export const ORDER_TYPE = {
	HOTEL: 'hotel',
	ROUTE: 'route',
	GOODS: 'goods',
	CULTURE: 'culture',
	SERVICE: 'service',
	RESTAURANT: 'restaurant'
};

/** C 端订单 Tab 类型（列表页三个 Tab） */
export const USER_TAB_TYPE = {
	HOTEL: 'hotel',
	GOODS: 'goods',
	ROUTE: 'route'
};

export const ORDER_TYPE_LABEL = {
	[ORDER_TYPE.HOTEL]: '酒店订单',
	[ORDER_TYPE.ROUTE]: '线路预约',
	[ORDER_TYPE.GOODS]: '非遗好物',
	[ORDER_TYPE.CULTURE]: '非遗好物',
	[ORDER_TYPE.SERVICE]: '服务订单',
	[ORDER_TYPE.RESTAURANT]: '餐饮预约'
};

export function normalizeOrderType(raw) {
	if (!raw) return '';
	const value = String(raw).trim().toLowerCase();
	if (value === 'travel') return ORDER_TYPE.ROUTE;
	return value;
}

/** 解析权威 orderType（优先后端字段，兜底推断） */
export function resolveOrderType(record) {
	if (!record) return ORDER_TYPE.GOODS;
	const fromType = normalizeOrderType(record.orderType);
	if (fromType) return fromType;
	const fromCategory = normalizeOrderType(record.orderCategory);
	if (fromCategory) return fromCategory;
	if (record.checkIn || record.checkOut || record.roomName || record.nights) {
		return ORDER_TYPE.HOTEL;
	}
	if (record.tripDate || record.routeName) return ORDER_TYPE.ROUTE;
	return ORDER_TYPE.GOODS;
}

/** orderType → C 端 Tab 分类 */
export function orderTypeToUserTab(orderType) {
	const type = normalizeOrderType(orderType);
	if (type === ORDER_TYPE.HOTEL) return USER_TAB_TYPE.HOTEL;
	if (type === ORDER_TYPE.ROUTE) return USER_TAB_TYPE.ROUTE;
	if (type === ORDER_TYPE.GOODS || type === ORDER_TYPE.CULTURE || type === ORDER_TYPE.SERVICE) {
		return USER_TAB_TYPE.GOODS;
	}
	return '';
}

export function orderTypeLabel(orderType) {
	const type = normalizeOrderType(orderType);
	return ORDER_TYPE_LABEL[type] || '';
}

export function isHotelFlow(orderType) {
	return normalizeOrderType(orderType) === ORDER_TYPE.HOTEL;
}

export function isRouteFlow(orderType) {
	return normalizeOrderType(orderType) === ORDER_TYPE.ROUTE;
}

export function isGoodsFlow(orderType) {
	const type = normalizeOrderType(orderType);
	return type === ORDER_TYPE.GOODS || type === ORDER_TYPE.CULTURE || type === ORDER_TYPE.SERVICE;
}

export function isRestaurantFlow(orderType) {
	return normalizeOrderType(orderType) === ORDER_TYPE.RESTAURANT;
}

/** 需在线支付的订单类型 */
export function isOnlinePayFlow(orderType) {
	const type = normalizeOrderType(orderType);
	return isHotelFlow(type) || isRouteFlow(type) || isGoodsFlow(type);
}
