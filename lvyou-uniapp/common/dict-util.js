/**
 * 业务字典工具（C 端筛选项/下拉，禁止私造未种子化 code）
 */
import { fetchDictItems } from '@/common/api/dict.js';
import {
	ORDER_CATEGORY_GOODS,
	ORDER_CATEGORY_HOTEL,
	ORDER_CATEGORY_ROUTE
} from '@/common/order-ui.js';

/** 与 server LvyouDictTypes / sys_dict.type 一致 */
export const DICT_TYPES = {
	MERCHANT_TYPE: 'lvyou_merchant_type',
	ORDER_STATUS: 'lvyou_order_status',
	ORDER_FILTER_HOTEL: 'lvyou_order_filter_hotel',
	ORDER_FILTER_GOODS: 'lvyou_order_filter_goods',
	ORDER_FILTER_ROUTE: 'lvyou_order_filter_route',
	RESERVATION_FILTER: 'lvyou_reservation_filter',
	BANNER_CLASSIFY: 'lvyou_banner_classify',
	COUPON_SCOPE: 'lvyou_coupon_scope'
};

const cache = new Map();

/** 订单分类 → 列表 Tab 字典 type */
export function getOrderFilterDictType(category) {
	if (category === ORDER_CATEGORY_GOODS) return DICT_TYPES.ORDER_FILTER_GOODS;
	if (category === ORDER_CATEGORY_ROUTE) return DICT_TYPES.ORDER_FILTER_ROUTE;
	return DICT_TYPES.ORDER_FILTER_HOTEL;
}

/**
 * 加载字典项（带内存缓存）
 * @returns {Promise<Array<{code:string,label:string,value:string,sort:number}>>}
 */
export async function loadDictItems(type) {
	if (!type) return [];
	if (cache.has(type)) {
		return cache.get(type);
	}
	try {
		const res = await fetchDictItems(type);
		const items = res.code === 0 && Array.isArray(res.data) ? res.data : [];
		cache.set(type, items);
		return items;
	} catch (e) {
		return [];
	}
}

/** 清除字典缓存（下拉刷新时可调用） */
export function clearDictCache(type) {
	if (type) {
		cache.delete(type);
		return;
	}
	cache.clear();
}

/** Tab 索引 → 服务端 status 参数 */
export function resolveStatusFromDictItems(items, tabIndex) {
	const item = items[tabIndex];
	if (!item || !item.code || item.code === 'all') {
		return undefined;
	}
	return item.code;
}

/** 字典项 → Tab 展示文案列表 */
export function dictItemsToTabLabels(items) {
	if (!items.length) return [];
	return items.map((item) => item.label || item.code);
}
