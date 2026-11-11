/**
 * 列表/首页 API 统一携带用户经纬度与城市信息（GCJ-02，自动定位）
 */
import { fetchReverseGeocode } from './api/map.js';
import {
	formatLocationDisplayLabel,
	getAccurateDeviceLocation
} from './location.js';

let cachedLocation = null;

/** 获取当前设备定位参数（高精度 GPS + 后端逆地理编码区县） */
export async function getAppLocationParams(forceRefresh = false) {
	if (!forceRefresh && cachedLocation) {
		return { ...cachedLocation };
	}

	const location = await getAccurateDeviceLocation();
	const lat = String(location.latitude);
	const lng = String(location.longitude);
	let city = '';
	let district = '';
	let label = '';

	try {
		const res = await fetchReverseGeocode({ lat, lng });
		if (res.code === 0 && res.data) {
			city = res.data.city || '';
			district = res.data.district || '';
			label = formatLocationDisplayLabel(city, district);
		}
	} catch (e) {
		// 坐标仍可用于公交查询，逆地理仅影响展示
	}

	cachedLocation = {
		lat,
		lng,
		city,
		district,
		label,
		accuracy: location.accuracy,
		highAccuracy: location.highAccuracy,
		getLocationRaw: { ...location }
	};
	return { ...cachedLocation };
}

/** 合并定位参数到查询对象 */
export function appendLocationParams(data = {}, locationParams = {}) {
	const next = { ...data };
	const keys = ['lat', 'lng', 'city', 'district', 'address', 'locationName', 'destinationId'];
	for (const key of keys) {
		if (locationParams[key] != null && locationParams[key] !== '') {
			next[key] = locationParams[key];
		}
	}
	return next;
}

/** 列表页通用：基础分页 + 自动定位参数 */
export async function buildLocationListQuery(base = {}, forceRefresh = false) {
	const location = await getAppLocationParams(forceRefresh);
	return appendLocationParams(base, location);
}

/** @deprecated 兼容旧引用，等同 appendLocationParams */
export function appendTransportLocationParams(base = {}, location = {}) {
	return appendLocationParams(base, location);
}
