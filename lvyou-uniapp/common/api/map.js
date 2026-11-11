/**
 * 地图 API（路径对齐 MapController）
 */
import { get } from './request.js';
import { appendLocationParams } from '../location-params.js';

/** 地图 POI 分页列表（公开接口，聚合景区/美食/停车/交通） */
export function fetchMapPois(params = {}) {
	return get('/app/map/pois', appendLocationParams({
		page: params.page || 1,
		limit: params.limit || 20,
		...(params.type ? { type: params.type } : {}),
		...(params.keyword ? { keyword: params.keyword } : {})
	}, params));
}

/**
 * 腾讯地图地点搜索（公开接口，返回地图平台可检索 POI，非业务库）
 * GET /app/map/placeSearch
 */
export function fetchMapPlaceSearch(params = {}) {
	return get('/app/map/placeSearch', {
		...(params.keyword ? { keyword: params.keyword } : {}),
		lat: params.lat,
		lng: params.lng,
		...(params.radius != null ? { radius: params.radius } : {}),
		...(params.filter ? { filter: params.filter } : {}),
		...(params.mode ? { mode: params.mode } : {}),
		...(params.autoExtend != null ? { autoExtend: params.autoExtend } : { autoExtend: 0 }),
		page: params.page || 1,
		limit: params.limit || 20
	});
}

/** 地址联想 GET /app/map/suggest */
export function fetchMapSuggest(params = {}) {
	return get('/app/map/suggest', {
		keyword: params.keyword,
		...(params.lat != null ? { lat: params.lat } : {}),
		...(params.lng != null ? { lng: params.lng } : {})
	});
}

/** 路线规划 GET /app/map/direction */
export function fetchMapDirection(params = {}) {
	return get('/app/map/direction', {
		fromLat: params.fromLat,
		fromLng: params.fromLng,
		toLat: params.toLat,
		toLng: params.toLng,
		mode: params.mode || 'driving',
		...(params.multiPlan != null ? { multiPlan: params.multiPlan } : { multiPlan: true })
	});
}

/** 反向地理编码（根据经纬度获取城市/地址信息，公开接口） */
export function fetchReverseGeocode(params = {}) {
	return get('/app/map/reverseGeocode', {
		lat: params.lat,
		lng: params.lng
	});
}

/** 正向地理编码（根据地址获取经纬度，公开接口） GET /app/map/geocode */
export function fetchMapGeocode(params = {}) {
	return get('/app/map/geocode', {
		address: params.address
	});
}
