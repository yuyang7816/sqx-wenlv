/**
 * 腾讯地图配置（Key 必须来自 manifest.json，禁止硬编码或走后端配置）
 */
import manifest from '../manifest.json';

const MAP_PICKER_BASE = 'https://apis.map.qq.com/tools/locpicker';

function readPath(source, keys) {
	if (!source) return '';
	let current = source;
	for (const key of keys) {
		if (current == null || current[key] == null) {
			return '';
		}
		current = current[key];
	}
	return String(current || '').trim();
}

/** 从 manifest.json 读取腾讯地图 Key */
export function getTencentMapKey() {
	let key = '';
	// #ifdef MP-WEIXIN
	key = readPath(manifest, ['mp-weixin', 'sdkConfigs', 'maps', 'tencent', 'key']);
	// #endif
	// #ifdef H5
	key = readPath(manifest, ['h5', 'sdkConfigs', 'maps', 'tencent', 'key']);
	// #endif
	// #ifdef APP-PLUS
	key = readPath(manifest, ['app-plus', 'distribute', 'sdkConfigs', 'maps', 'tencent', 'key']);
	// #endif
	if (!key) {
		key = readPath(manifest, ['h5', 'sdkConfigs', 'maps', 'tencent', 'key']);
	}
	return key;
}

/** 腾讯地图 Referer（使用 manifest appid） */
export function getMapReferer() {
	return String(manifest.appid || 'lvyou-uniapp').trim();
}

/** 构建腾讯位置选择器 URL */
export function buildLocationPickerUrl(options = {}) {
	const key = getTencentMapKey();
	if (!key) {
		throw new Error('未在 manifest.json 配置腾讯地图 Key');
	}
	const params = [
		'search=1',
		'type=1',
		`key=${encodeURIComponent(key)}`,
		`referer=${encodeURIComponent(getMapReferer())}`,
	];
	const lat = options.lat ?? options.latitude;
	const lng = options.lng ?? options.longitude;
	if (lat != null && lng != null && lat !== '' && lng !== '') {
		params.push(`coord=${encodeURIComponent(`${lat},${lng}`)}`);
	}
	return `${MAP_PICKER_BASE}?${params.join('&')}`;
}

export const MAP_LOCATION_PICKED_EVENT = 'merchant-map-location-picked';
