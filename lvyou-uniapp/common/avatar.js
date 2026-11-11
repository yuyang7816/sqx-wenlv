/**
 * 默认头像（来自 sys_config.sys_default_avatar，GET /app/config/info → defaultAvatar）
 */
import { fetchConfigInfo } from '@/common/api/config.js';

export const FALLBACK_DEFAULT_AVATAR = '';

const STORAGE_KEY = 'app_default_avatar';

export function getDefaultAvatar() {
	try {
		const cached = uni.getStorageSync(STORAGE_KEY);
		return cached || FALLBACK_DEFAULT_AVATAR;
	} catch (e) {
		return FALLBACK_DEFAULT_AVATAR;
	}
}

export function setDefaultAvatar(url) {
	const next = String(url || '').trim();
	if (next) {
		uni.setStorageSync(STORAGE_KEY, next);
	}
}

/** 用户未上传头像时返回默认头像 URL */
export function resolveAvatar(url) {
	const trimmed = String(url || '').trim();
	return trimmed || getDefaultAvatar();
}

/** 判断当前展示 URL 是否为默认头像（保存资料时可写空字符串） */
export function isDefaultAvatar(url) {
	const trimmed = String(url || '').trim();
	if (!trimmed) return true;
	return trimmed === getDefaultAvatar() || trimmed === FALLBACK_DEFAULT_AVATAR;
}

export async function loadDefaultAvatar() {
	try {
		const res = await fetchConfigInfo();
		const payload = res?.data ?? res;
		if (payload?.defaultAvatar) {
			setDefaultAvatar(payload.defaultAvatar);
			return payload.defaultAvatar;
		}
	} catch (_) {
		// ignore
	}
	return getDefaultAvatar();
}
