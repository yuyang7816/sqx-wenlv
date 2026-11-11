/**
 * 收藏 / 足迹 API（路径对齐 AppFavoriteController / AppFootprintController）
 */
import { get, postJson } from './request.js';

const FAVORITE_PREFIX = '/app/favorite';
const FOOTPRINT_PREFIX = '/app/footprint';
const PAGE_SIZE = 10;

/** 用户端详情页路径（对齐 mobile-design 路由） */
const DETAIL_PATH_BUILDERS = {
	scenic: (id) => `/mobile-design/scenic/detail?id=${id}`,
	hotel: (id) => `/mobile-design/hotel/detail?id=${id}`,
	route: (id) => `/mobile-design/route/detail?id=${id}`,
	goods: (id) => `/mobile-design/heritage/goods-detail?id=${id}`,
	goose: (id) => `/mobile-design/goose/detail?id=${id}`,
	activity: (id) => `/mobile-design/activity/detail?id=${id}`
};

/** 服务端旧路径前缀修正为 mobile-design */
const LEGACY_PATH_REWRITES = [
	['/pages/scenic/detail', '/mobile-design/scenic/detail'],
	['/pages/hotel/detail', '/mobile-design/hotel/detail'],
	['/pages/route/detail', '/mobile-design/route/detail'],
	['/pages/heritage/goods-detail', '/mobile-design/heritage/goods-detail'],
	['/pages/goose/detail', '/mobile-design/goose/detail'],
	['/pages/activity/detail', '/mobile-design/activity/detail']
];

/** 收藏列表（分页，targetType 可选） */
export function fetchFavoriteList(params = {}) {
	const data = {
		page: params.page || 1,
		limit: params.limit || PAGE_SIZE
	};
	if (params.targetType) data.targetType = params.targetType;
	return get(`${FAVORITE_PREFIX}/list`, data);
}

/** 足迹列表（分页，targetType 可选） */
export function fetchFootprintList(params = {}) {
	const data = {
		page: params.page || 1,
		limit: params.limit || PAGE_SIZE
	};
	if (params.targetType) data.targetType = params.targetType;
	return get(`${FOOTPRINT_PREFIX}/list`, data);
}

/** 收藏切换（新增 / 取消） */
export function toggleFavorite(payload) {
	return postJson(`${FAVORITE_PREFIX}/toggle`, {
		targetType: payload.targetType,
		targetId: payload.targetId
	});
}

/** 解析列表项跳转路径 */
export function resolveAssetPath(item) {
	if (!item) return '';
	const moduleType = item.moduleType || item.targetType;
	const id = item.id || item.targetId;
	let path = item.path || '';

	for (const [from, to] of LEGACY_PATH_REWRITES) {
		if (path.startsWith(from)) {
			path = path.replace(from, to);
			break;
		}
	}

	if (path && path.startsWith('/mobile-design/')) return path;

	const builder = DETAIL_PATH_BUILDERS[moduleType];
	if (builder && id != null) return builder(id);
	return path;
}

export { PAGE_SIZE as ASSET_PAGE_SIZE };
