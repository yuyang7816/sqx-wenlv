/**
 * 设备定位与城市解析
 */
import {
	fetchReverseGeocode
} from './api/map.js';

export const HOME_LOCATION_LABEL_KEY = 'home_location_label';

/** 将逆地理编码结果格式化为首页展示的市级名称（不用区县） */
export function formatAddressCityLabel(address) {
	if (!address) {
		return '';
	}
	const city = String(address.city || '').replace(/[市区縣]$/, '').trim();
	if (city) {
		return city;
	}
	return String(address.province || '').replace(/(省|自治区|特别行政区|市)$/, '').trim();
}

function requestDeviceLocation(options = {}) {
	return new Promise((resolve, reject) => {
		uni.getLocation({
			type: 'gcj02',
			altitude: true,
			geocode: true,
			isHighAccuracy: true,
			...options,
			success: resolve,
			fail: reject
		});
	});
}

/** 获取设备 GCJ-02 经纬度（普通精度） */
export function getDeviceLocation() {
	return requestDeviceLocation();
}

/**
 * 高精度自动定位（微信小程序 isHighAccuracy，失败时降级普通定位）
 * @returns {Promise<{latitude:number, longitude:number, accuracy?:number, highAccuracy:boolean}>}
 */
export async function getAccurateDeviceLocation() {
	try {
		const res = await requestDeviceLocation({
			isHighAccuracy: true,
			highAccuracyExpireTime: 5000
		});
		return {
			...res,
			highAccuracy: true
		};
	} catch (e) {
		const res = await requestDeviceLocation({
			isHighAccuracy: false
		});
		return {
			...res,
			highAccuracy: false
		};
	}
}

/** 格式化位置展示文案 */
export function formatLocationDisplayLabel(city, district) {
	const cityText = String(city || '').replace(/[市区縣]$/, '').trim();
	const districtText = String(district || '').trim();
	if (cityText && districtText && !districtText.startsWith(cityText)) {
		return `${cityText} · ${districtText}`;
	}
	return cityText || districtText || '';
}

/** 读取上次定位成功的城市名 */
export function readCachedCityLabel() {
	try {
		return uni.getStorageSync(HOME_LOCATION_LABEL_KEY) || '';
	} catch (e) {
		return '';
	}
}

/** 缓存定位成功的城市名 */
export function cacheCityLabel(label) {
	if (!label) {
		return;
	}
	try {
		uni.setStorageSync(HOME_LOCATION_LABEL_KEY, label);
	} catch (e) {
		// ignore storage failures
	}
}

/** GCJ-02 转 WGS84（BigDataCloud 使用 WGS84） */
function gcj02ToWgs84(lat, lng) {
	const a = 6378245.0;
	const ee = 0.006693421622965943;
	const pi = Math.PI;
	const transformLat = (x, y) => {
		let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320.0 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	};
	const transformLng = (x, y) => {
		let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
		return ret;
	};
	const latNum = Number(lat);
	const lngNum = Number(lng);
	if (!Number.isFinite(latNum) || !Number.isFinite(lngNum)) {
		return {
			lat,
			lng
		};
	}
	if (lngNum < 72.004 || lngNum > 137.8347 || latNum < 0.8293 || latNum > 55.8271) {
		return {
			lat: latNum,
			lng: lngNum
		};
	}
	const dLat = transformLat(lngNum - 105.0, latNum - 35.0);
	const dLng = transformLng(lngNum - 105.0, latNum - 35.0);
	const radLat = latNum / 180.0 * pi;
	let magic = Math.sin(radLat);
	magic = 1 - ee * magic * magic;
	const sqrtMagic = Math.sqrt(magic);
	const mgLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
	const mgLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
	return {
		lat: latNum - mgLat,
		lng: lngNum - mgLng
	};
}

/** 客户端直连备用逆地理编码（后端 Key 失效或超限时兜底） */
function resolveCityLabelFromOpenApi(lat, lng) {
	const wgs84 = gcj02ToWgs84(lat, lng);
	const url =
		`https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${encodeURIComponent(wgs84.lat)}&longitude=${encodeURIComponent(wgs84.lng)}&localityLanguage=zh`;
	return new Promise((resolve, reject) => {
		uni.request({
			url,
			method: 'GET',
			timeout: 15000,
			success: (res) => {
				if (res.statusCode !== 200 || !res.data) {
					reject(new Error('定位解析失败'));
					return;
				}
				const label = formatAddressCityLabel({
					city: res.data.city,
					province: res.data.principalSubdivision
				});
				if (!label) {
					reject(new Error('定位解析失败'));
					return;
				}
				resolve(label);
			},
			fail: reject
		});
	});
}

/**
 * 使用 uni-app 官方 uni.openLocation 打开系统/内置地图导航（GCJ-02 坐标）
 * @see https://uniapp.dcloud.net.cn/api/location/map.html#openlocation
 */
export function openMapNavigation(options = {}) {
	const latitude = Number(options.latitude);
	const longitude = Number(options.longitude);
	const name = String(options.name || '').trim();
	const address = String(options.address || '').trim();
	const scale = Number(options.scale) || 18;

	if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
		const hint = address || name || '暂无导航信息';
		uni.showToast({ title: hint, icon: 'none' });
		return Promise.reject(new Error('invalid coordinates'));
	}

	return new Promise((resolve, reject) => {
		uni.openLocation({
			latitude,
			longitude,
			scale,
			name: name || '目的地',
			address,
			success: resolve,
			fail: (err) => {
				uni.showToast({ title: address || name || '打开导航失败', icon: 'none' });
				reject(err);
			}
		});
	});
}

/** 获取当前位置对应的城市展示名 */
export async function resolveCurrentCityLabel() {
	const location = await getDeviceLocation();
	const lat = String(location.latitude);
	const lng = String(location.longitude);

	try {
		const res = await fetchReverseGeocode({
			lat,
			lng
		});
		if (res.code === 0 && res.data) {
			const label = formatAddressCityLabel(res.data);
			if (label) {
				cacheCityLabel(label);
				return label;
			}
		}
	} catch (e) {
		// 后端失败时走客户端兜底
	}

	const fallbackLabel = await resolveCityLabelFromOpenApi(lat, lng);
	cacheCityLabel(fallbackLabel);
	return fallbackLabel;
}
