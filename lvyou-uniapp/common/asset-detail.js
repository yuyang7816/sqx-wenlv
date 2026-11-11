/**
 * 详情页收藏/足迹辅助（对接 /app/favorite、服务端详情 recordAsync 足迹）
 */
import { hasToken } from './api/auth.js';
import { fetchFavoriteList, toggleFavorite as apiToggleFavorite } from './api/asset.js';

/** 足迹由服务端详情接口异步写入，前端无需再调 */
export function logFootprint() {
	/* no-op */
}

/** 查询是否已收藏（需登录） */
export async function checkFavorited(targetType, targetId) {
	if (!hasToken() || !targetType || targetId == null) return false;
	try {
		const res = await fetchFavoriteList({ page: 1, limit: 20, targetType });
		if (res.code === 0 && res.data?.records) {
			return res.data.records.some(
				(item) => String(item.targetId ?? item.id) === String(targetId)
			);
		}
	} catch (e) {
		/* ignore */
	}
	return false;
}

/**
 * 切换收藏状态
 * @returns {boolean|null} true/false=新状态；null=未登录或请求失败
 */
export async function toggleFavoriteItem(item) {
	if (!hasToken()) {
		uni.showToast({ title: '请先登录', icon: 'none' });
		return null;
	}
	const targetType = item.moduleType || item.targetType;
	const targetId = item.id ?? item.targetId;
	if (!targetType || targetId == null) return null;
	try {
		const res = await apiToggleFavorite({ targetType, targetId });
		if (res.code === 0 && res.data) {
			return Boolean(res.data.favorited);
		}
		uni.showToast({ title: res.msg || '操作失败', icon: 'none' });
	} catch (e) {
		uni.showToast({ title: '网络异常', icon: 'none' });
	}
	return null;
}

/** 兼容旧 mock 签名：同步返回 false，请改用 checkFavorited */
export function isFavorited() {
	return false;
}

/** 兼容旧 mock 签名：请改用 toggleFavoriteItem */
export function toggleFavorite(item) {
	toggleFavoriteItem(item);
	return false;
}
