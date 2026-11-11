import { PLACEHOLDER_LOGO } from '@/common/theme.js';

const MODULE_ICON = {
	route: 'list-dot',
	hotel: 'home-fill',
	goods: 'bag-fill',
	scenic: 'photo-fill',
	goose: 'coupon-fill',
	activity: 'calendar-fill',
	heritage: 'star-fill'
};

const MODULE_COLOR_KEY = {
	route: 'route',
	hotel: 'hotel',
	goods: 'heritageShop',
	scenic: 'scenic',
	goose: 'goose',
	activity: 'activity',
	heritage: 'heritage'
};

export function getAssetModuleIcon(moduleType) {
	return MODULE_ICON[moduleType] || 'star-fill';
}

export function getAssetModuleColorKey(moduleType) {
	return MODULE_COLOR_KEY[moduleType] || 'default';
}

export function formatAssetTime(ts) {
	if (!ts) return '';
	const date = new Date(ts);
	if (Number.isNaN(date.getTime())) return '';
	const now = Date.now();
	const diff = now - date.getTime();
	if (diff < 60000) return '刚刚';
	if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
	if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
	if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;
	const month = String(date.getMonth() + 1).padStart(2, '0');
	const day = String(date.getDate()).padStart(2, '0');
	return `${date.getFullYear()}-${month}-${day}`;
}

export function formatAssetPrice(item) {
	const price = Number(item.price);
	if (!price || Number.isNaN(price)) return '';
	return String(price);
}

export function assetPriceSuffix(moduleType) {
	if (moduleType === 'route' || moduleType === 'hotel') return '起';
	if (moduleType === 'scenic') return '';
	return '';
}

/** 收藏/足迹卡片价格前缀：活动仍为参考价，线路/酒店等在线售卖不展示「参考」 */
export function assetPriceLabel(moduleType) {
	if (moduleType === 'activity') return '参考价';
	return '';
}

/** 规范化服务端收藏/足迹列表项（无 mock 补全） */
export function enrichAssetItem(item) {
	if (!item) return null;
	const moduleType = item.moduleType || item.targetType;
	return {
		...item,
		moduleType,
		id: item.id ?? item.targetId,
		cover: item.cover || PLACEHOLDER_LOGO,
		desc: item.desc || '',
		location: item.location || item.address || ''
	};
}

export function enrichAssetList(list) {
	return (Array.isArray(list) ? list : []).map(enrichAssetItem).filter(Boolean);
}
