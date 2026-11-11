/**
 * 公共交通 API（实时公交，路径对齐 AppTransportController）
 */
import { get } from './request.js';
import { buildLocationListQuery } from '../location-params.js';
import { buildTransportRequestParams } from '../transport-context.js';

const PREFIX = '/app/transport';

/** 公交线路列表（分页，基于定位 + 附近站点实时到站） */
export async function fetchTransportRouteList(params = {}) {
	const query = params.lat && params.lng
		? params
		: await buildLocationListQuery(params);
	return get(`${PREFIX}/routes`, {
		page: query.page || 1,
		limit: query.limit || 10,
		lat: query.lat,
		lng: query.lng,
		...(query.city ? { city: query.city } : {}),
		...(query.district ? { district: query.district } : {}),
		...(query.address ? { address: query.address } : {}),
		...(query.locationName ? { locationName: query.locationName } : {}),
		...(query.keyword ? { keyword: query.keyword } : {})
	});
}

/** 公交线路时刻表 */
export async function fetchTransportSchedule(routeId, routeContext = {}) {
	const params = await buildTransportRequestParams(routeContext);
	return get(`${PREFIX}/routes/${encodeURIComponent(routeId)}/schedule`, params);
}

/** 公交线路站点 */
export async function fetchTransportStations(routeId, routeContext = {}) {
	const params = await buildTransportRequestParams(routeContext);
	return get(`${PREFIX}/routes/${encodeURIComponent(routeId)}/stations`, params);
}

/** 公交线路实时轨迹 */
export async function fetchTransportTrack(routeId, routeContext = {}) {
	const params = await buildTransportRequestParams(routeContext);
	return get(`${PREFIX}/routes/${encodeURIComponent(routeId)}/track`, params);
}
