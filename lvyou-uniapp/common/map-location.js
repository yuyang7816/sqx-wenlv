/**
 * 地图选点结果解析（地区/详细地址）
 */
import { fetchReverseGeocode } from '@/common/api/map.js';
import { formatStoreAddress } from '@/common/merchant-onboarding.js';

export function formatRegionFromAddress(addr) {
	if (!addr) return '';
	const parts = [addr.province, addr.city, addr.district].filter(Boolean);
	const unique = [];
	for (const part of parts) {
		if (unique.length && unique[unique.length - 1] === part) continue;
		unique.push(part);
	}
	return unique.join('');
}

export function deriveAddressDetail(fullAddress, region, fallback = '') {
	const full = String(fullAddress || '').trim();
	const regionText = String(region || '').trim();
	if (full && regionText && full.startsWith(regionText)) {
		const detail = full.slice(regionText.length).trim();
		if (detail) return detail;
	}
	return String(fallback || full || '').trim();
}

/** 从完整地址文本提取省市区（无逆地理接口时使用） */
export function parseRegionFromAddress(address) {
	const text = String(address || '').trim();
	if (!text) return '';
	const province = (text.match(/(.+?(?:省|自治区|特别行政区))/) || [])[1] || '';
	const city = parseCityFromAddress(text);
	const district = parseDistrictFromAddress(text);
	return formatRegionFromAddress({ province, city, district }) || text.slice(0, 20);
}

/** 从地址提取城市（如「长沙市」） */
export function parseCityFromAddress(address) {
	const text = String(address || '').trim();
	if (!text) return '';
	const direct = text.match(/(?:省|自治区|特别行政区)(.+?(?:市|自治州|地区|盟))/);
	if (direct && direct[1]) return direct[1];
	const fallback = text.match(/(.+?(?:市|自治州|地区|盟))/);
	return fallback ? fallback[1] : '';
}

/** 从地址提取区县（如「岳麓区」） */
export function parseDistrictFromAddress(address) {
	const text = String(address || '').trim();
	if (!text) return '';
	const afterCity = text.match(/(?:市|自治州|地区|盟)(.+?(?:区|县|旗))/);
	if (afterCity && afterCity[1]) return afterCity[1];
	const fallback = text.match(/(.+?(?:区|县|旗))/);
	return fallback ? fallback[1] : '';
}

export async function resolveLocationAddress(lat, lng, fallbackDetail = '') {
	const fallback = String(fallbackDetail || '').trim();
	let storeRegion = parseRegionFromAddress(fallback);
	let storeAddressDetail = fallback;
	try {
		const res = await fetchReverseGeocode({ lat, lng });
		if (res.code === 0 && res.data) {
			const region = formatRegionFromAddress(res.data);
			if (region) storeRegion = region;
			const detail = deriveAddressDetail(res.data.address, storeRegion, fallback);
			if (detail) storeAddressDetail = detail;
		}
	} catch (e) {
		/* 坐标仍可用，保留本地解析结果 */
	}
	const formLike = { storeRegion, storeAddressDetail };
	return {
		latitude: Number(lat),
		longitude: Number(lng),
		storeRegion,
		storeAddressDetail,
		storeAddress: formatStoreAddress(formLike),
	};
}

/** uni.chooseLocation 选点结果写入入驻表单 */
export async function applyChooseLocationResult(res) {
	const name = String(res?.name || '').trim();
	const address = String(res?.address || '').trim();
	const fallbackDetail = name || address;
	const resolved = await resolveLocationAddress(res.latitude, res.longitude, fallbackDetail);
	if (!resolved.storeAddressDetail) {
		resolved.storeAddressDetail = fallbackDetail;
	}
	if (!resolved.storeRegion) {
		resolved.storeRegion = parseRegionFromAddress(address || fallbackDetail);
	}
	resolved.storeAddress = formatStoreAddress(resolved);
	return resolved;
}
