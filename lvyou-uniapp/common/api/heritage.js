/**
 * 非遗店铺与工艺 API（路径对齐 AppHeritageShopController / AppHeritageCraftController）
 */
import { get, postJson } from './request.js';
import { PLACEHOLDER_LOGO } from '../theme.js';
import { appendLocationParams } from '../location-params.js';
import {
	SPEC_MODE_SINGLE,
	SPEC_MODE_MULTI,
	normalizeAppSkus,
	aggregateSkus,
	findSkuByName
} from '../merchant-goods-spec.js';
import { normalizeShopShipping } from '../heritage-shipping.js';

export { findSkuByName };

const SHOP_PREFIX = '/app/heritage/shop';
const SHOP_CATEGORY_PREFIX = '/app/heritage/shop/category';
const CRAFT_PREFIX = '/app/heritage/craft';
const GOODS_PREFIX = '/app/goods';

/** 好店分类选项（公开接口，仅启用） */
export function fetchShopCategoryOptions() {
	return get(`${SHOP_CATEGORY_PREFIX}/options`);
}

/** 工艺列表（分页，公开接口，仅上架） */
export function fetchCraftList(params = {}) {
	return get(`${CRAFT_PREFIX}/list`, appendLocationParams({
		page: params.page || 1,
		limit: params.limit || 10,
		...(params.name ? { name: params.name } : {}),
		...(params.tag ? { tag: params.tag } : {})
	}, params));
}

/** 工艺详情（公开接口） */
export function fetchCraftDetail(id) {
	return get(`${CRAFT_PREFIX}/${id}`);
}

/** 店铺列表（分页，公开接口，仅营业中） */
export function fetchShopList(params = {}) {
	return get(`${SHOP_PREFIX}/list`, appendLocationParams({
		page: params.page || 1,
		limit: params.limit || 10,
		...(params.name ? { name: params.name } : {}),
		...(params.category && params.category !== 'all' ? { category: params.category } : {})
	}, params));
}

/** 店铺详情（公开接口） */
export function fetchShopDetail(id) {
	return get(`${SHOP_PREFIX}/${id}`);
}

/** 店铺商品预览（公开接口，shop-detail 首屏好物） */
export function fetchShopGoodsPreview(shopId, limit = 5) {
	return get(`${GOODS_PREFIX}/list`, {
		shopId,
		page: 1,
		limit
	});
}

/** 映射工艺列表项到页面字段 */
export function mapCraftItem(record) {
	return {
		id: record.id,
		name: record.name || '',
		desc: record.desc || '',
		level: record.level || '',
		levelLabel: record.levelLabel || '',
		tag: record.tag || (Array.isArray(record.tags) && record.tags[0]) || '',
		tags: record.tags || (record.tag ? [record.tag] : []),
		icon: record.icon || 'star-fill',
		coverFrom: record.coverFrom || '#8D6E63',
		coverTo: record.coverTo || '#5D4037',
		cover: record.cover || PLACEHOLDER_LOGO,
		bannerImages: Array.isArray(record.bannerImages)
			? record.bannerImages.filter(Boolean)
			: (record.cover ? [record.cover] : []),
		overview: record.overview || '',
		feature: record.feature || '',
		culture: record.culture || ''
	};
}

/** 映射店铺列表项到页面字段 */
export function mapShopItem(record) {
	return {
		id: record.id,
		name: record.name || '',
		address: record.address || '',
		latitude: record.latitude != null ? Number(record.latitude) : null,
		longitude: record.longitude != null ? Number(record.longitude) : null,
		tag: record.tag || '',
		tags: record.tags || [],
		category: record.category || '',
		intro: record.intro || '',
		introDetail: record.introDetail || '',
		desc: record.desc || '',
		coverFrom: record.coverFrom || '#B45D2D',
		coverTo: record.coverTo || '#8D4E2A',
		cover: record.cover || PLACEHOLDER_LOGO,
		bannerImages: Array.isArray(record.bannerImages)
			? record.bannerImages.filter(Boolean)
			: (record.cover ? [record.cover] : []),
		hours: record.hours || '',
		phone: record.phone || '',
		rating: record.rating != null ? String(record.rating) : '',
		reviewCount: record.reviewCount || 0,
		services: record.services || [],
		craftId: record.craftId,
		craftName: record.craftName || ''
	};
}

/** 映射商品预览项 */
export function mapGoodsPreviewItem(record) {
	return {
		id: record.id,
		name: record.name || '',
		cover: record.cover || record.coverImage || PLACEHOLDER_LOGO,
		price: record.price != null ? String(record.price) : '0'
	};
}

/** 商品列表（分页，公开接口） */
export function fetchGoodsList(params = {}) {
	const data = {
		page: params.page || 1,
		limit: params.limit || 10
	};
	if (params.shopId) {
		data.shopId = params.shopId;
	}
	if (params.name) {
		data.name = params.name;
	}
	if (params.sort && params.sort !== 'all') {
		data.sort = params.sort;
	}
	if (params.priceOrder) {
		data.priceOrder = params.priceOrder;
	}
	if (params.category && params.category !== 'all') {
		data.category = params.category;
	}
	return get(`${GOODS_PREFIX}/list`, data);
}

/** 商品详情（公开接口） */
export function fetchGoodsDetail(id) {
	return get(`${GOODS_PREFIX}/${id}`);
}

/** 商品评价列表（分页，公开接口） */
export function fetchGoodsReviews(goodsId, params = {}) {
	return get(`${GOODS_PREFIX}/${goodsId}/reviews`, {
		page: params.page || 1,
		limit: params.limit || 10
	});
}

/** 提交商品评价（须 App JWT + 已完成订单 orderId） */
export function submitGoodsReview(goodsId, payload) {
	return postJson(`${GOODS_PREFIX}/${goodsId}/reviews`, payload);
}

/** 映射商品列表项 */
export function mapGoodsItem(record) {
	return {
		id: record.id,
		shopId: record.shopId,
		shopName: record.shopName || '',
		name: record.name || '',
		desc: record.desc || '',
		cover: record.cover || PLACEHOLDER_LOGO,
		price: record.price != null ? String(record.price) : '0',
		priceFrom: Boolean(record.priceFrom),
		sales: record.sales || 0,
		category: record.category || ''
	};
}

/** 映射商品详情 */
export function mapGoodsDetail(record) {
	const banners = Array.isArray(record.banners) && record.banners.length
		? record.banners.map((item) => ({ image: item.image || PLACEHOLDER_LOGO }))
		: [{ image: record.cover || PLACEHOLDER_LOGO }];
	const basePrice = record.price != null ? Number(record.price) : 0;
	const baseStock = record.stock || 0;
	const specState = normalizeAppSkus(record.specs, basePrice, baseStock);
	const specMode = record.specMode === SPEC_MODE_MULTI || specState.mode === SPEC_MODE_MULTI
		? SPEC_MODE_MULTI
		: SPEC_MODE_SINGLE;
	const skus = specState.skus;
	const { minPrice } = aggregateSkus(skus.length ? skus : [{ price: basePrice }]);
	const displayPrice = specMode === SPEC_MODE_MULTI && skus.length
		? String(minPrice)
		: (record.price != null ? String(record.price) : '0');
	return {
		id: record.id,
		shopId: record.shopId,
		shopName: record.shopName || '',
		shopCover: record.shopCover || PLACEHOLDER_LOGO,
		shopBannerImages: Array.isArray(record.shopBannerImages)
			? record.shopBannerImages.filter(Boolean)
			: (record.shopCover ? [record.shopCover] : []),
		shopShipping: normalizeShopShipping(record.shopShipping),
		shopAddress: record.shopAddress || '',
		shopPhone: record.shopPhone || '',
		name: record.name || '',
		desc: record.desc || '',
		cover: record.cover || PLACEHOLDER_LOGO,
		price: displayPrice,
		priceFrom: Boolean(record.priceFrom) || (specMode === SPEC_MODE_MULTI && skus.length > 1),
		sales: record.sales || 0,
		stock: record.stock || 0,
		rating: record.rating || '',
		specMode,
		specs: skus,
		detail: record.detail || '',
		banners,
		detailImages: Array.isArray(record.detailImages) ? record.detailImages.filter(Boolean) : []
	};
}

/** 映射评价项 */
export function mapReviewItem(record) {
	return {
		id: record.id,
		user: record.user || '',
		content: record.content || '',
		time: record.time || ''
	};
}
