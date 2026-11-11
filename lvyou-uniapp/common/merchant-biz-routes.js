/**
 * 商户业务页按入驻类型分包路由
 * hotel / restaurant / travel / culture 各自独立分包，便于分业态维护
 */
import { normalizeStoreType } from '@/common/merchant-onboarding.js';

const PACKAGE_ROOT = {
	hotel: 'merchant-hotel',
	restaurant: 'merchant-restaurant',
	travel: 'merchant-travel',
	culture: 'merchant-culture'
};

/** @typedef {'services'|'service-edit'|'store-decor'|'orders'|'order-detail'|'room-standards'|'room-standard-edit'} MerchantBizPage */

/**
 * @param {string} storeType
 * @param {MerchantBizPage} page
 * @param {Record<string, string|number>|string} [query]
 */
export function getMerchantBizPath(storeType, page, query) {
	const key = normalizeStoreType(storeType) || 'hotel';
	const root = PACKAGE_ROOT[key] || PACKAGE_ROOT.hotel;
	let url = `/${root}/${page}`;
	if (query == null || query === '') return url;
	if (typeof query === 'string') {
		const q = query.startsWith('?') ? query.slice(1) : query;
		return q ? `${url}?${q}` : url;
	}
	const parts = Object.entries(query)
		.filter(([, v]) => v !== undefined && v !== null && v !== '')
		.map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`);
	return parts.length ? `${url}?${parts.join('&')}` : url;
}

export function getMerchantBizRoot(storeType) {
	const key = normalizeStoreType(storeType) || 'hotel';
	return PACKAGE_ROOT[key] || PACKAGE_ROOT.hotel;
}

export { PACKAGE_ROOT };
