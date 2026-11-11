/**
 * 产品名称（来自 sys_config.sys_server_name，GET /app/config/info → productName）
 */
import { fetchConfigInfo } from '@/common/api/config.js';

export const DEFAULT_PRODUCT_NAME = '省钱兄文旅';
const STORAGE_KEY = 'app_product_name';

export function getProductName() {
	const cached = uni.getStorageSync(STORAGE_KEY);
	return cached || DEFAULT_PRODUCT_NAME;
}

export function setProductName(name) {
	const next = String(name || '').trim();
	if (next) {
		uni.setStorageSync(STORAGE_KEY, next);
	}
}

export function getLogoChar(name = getProductName()) {
	const trimmed = String(name || '').trim();
	return trimmed.charAt(0) || '省';
}

/** 拉取并缓存产品名称，失败时保留本地/默认值 */
export async function loadProductName() {
	try {
		const res = await fetchConfigInfo();
		const payload = res?.data ?? res;
		if (payload?.productName) {
			setProductName(payload.productName);
			return payload.productName;
		}
	} catch (_) {
		// ignore
	}
	return getProductName();
}
